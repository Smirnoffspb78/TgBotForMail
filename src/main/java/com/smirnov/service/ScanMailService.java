package com.smirnov.service;

import com.smirnov.exception.MailException;
import com.smirnov.configuration.TelegramBot;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.SECONDS;

@Service
@EnableScheduling
@Slf4j
public class ScanMailService {

    private final TelegramBot telegramBot;

    private final String login;
    private final String password;
    private final String host;
    private final int port;
    private final String protocol;
    private final boolean isEnabled;
    private final Properties properties;

    private final Map<Integer, LocalDateTime> cashMap = new ConcurrentHashMap<>();

    public ScanMailService(TelegramBot telegramBot,
                           @Value("${mail.imap.username}") String login,
                           @Value("${mail.imap.password}") String password,
                           @Value("${mail.imap.host}") String host,
                           @Value("${mail.imap.port}") int port,
                           @Value("${mail.imap.protocol}") String protocol,
                           @Value("${mail.imap.ssl.enable}") boolean isEnabled
    ) {
        this.telegramBot = telegramBot;
        this.host = host;
        this.login = login;
        this.password = password;
        this.port = port;
        this.protocol = protocol;
        this.isEnabled = isEnabled;
        this.properties = getProperties();
    }

    @Scheduled(fixedRate = 20, timeUnit = SECONDS)
    public void scanInbox() {
        try {
            Session session = Session.getDefaultInstance(properties, null);
            Store store = session.getStore(protocol);
            store.connect(host, login, password);
            Folder inbox = store.getFolder("СберЗдоровье");
            inbox.open(Folder.READ_ONLY);
            List<Message> messages = getMessageInbox(inbox);
            if (!messages.isEmpty()) {
                for (Message message : messages) {
                    String messageSend = new StringBuilder().append(InternetAddress.toString(message.getFrom()))
                            .append("\n")
                            .append("\n")
                            .append(message.getSentDate())
                            .append("\n")
                            .append("\n")
                            .append(message.getContent())
                            .toString();
                    if (messageSend.length() > 4096) {
                        messageSend = messageSend.substring(0, 4096);
                    }
                    telegramBot.sendMessageMail(messageSend);
                    log.info("Почта просканирована");
                }
            }
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", protocol);
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", port);
        properties.put("mail.imap.ssl.enable", isEnabled);
        return properties;
    }

    private List<Message> getMessageInbox(Folder inbox) {
        try {
            Message[] messagesArray = inbox.getMessages();
            LocalDateTime timeScan = LocalDateTime.now().minusSeconds(3600);
            LocalDateTime timeMessage = timeScan.plusDays(1);
            List<Message> messages = new ArrayList<>();
            int i = messagesArray.length - 1;
            while (timeMessage.isAfter(timeScan)) {
                int messageNumber = messagesArray[i].getMessageNumber();
                if (!cashMap.containsKey(messageNumber)) {
                    timeMessage = messagesArray[i]
                            .getSentDate()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    messages.add(messagesArray[i]);
                    cashMap.put(messageNumber, timeMessage);
                    i--;
                }
            }
            return messages;
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new MailException(e.getMessage());
        }
    }

    @Scheduled(fixedRate = 3600, timeUnit = SECONDS)
    private void clearCash() {
        cashMap.keySet().stream()
                .filter(i -> cashMap.get(i).isBefore(LocalDateTime.now()
                        .minusSeconds(3600)))
                .forEach(cashMap::remove);
    }
}