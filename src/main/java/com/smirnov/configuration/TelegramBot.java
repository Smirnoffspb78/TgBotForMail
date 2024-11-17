package com.smirnov.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    /**
     * Имя бота.
     */

    private final String botName;

    /**
     * Токен/
     */
    private final String botToken;
    /**
     * Чат для отправки сообщение.
     */
    private final long chatId;

    /**
     * Топик для сообщений из почты
     */
    private final Integer mailTopic;
    /**
     * Топик для получения сообщений об ошибках.
     */
    private final Integer exceptionTopic;

    public TelegramBot(@Value("${bot.token}") String botToken,
                       @Value("${bot.name}") String botName,
                       @Value("${bot.сhat}") long chatId,
                       @Value("${bot.mailTopic}") Integer mailTopic,
                       @Value("${bot.exceptionTopic}") Integer exceptionTopic) {
        this.chatId = chatId;
        this.mailTopic = mailTopic;
        this.botToken = botToken;
        this.botName = botName;
        this.exceptionTopic = exceptionTopic;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }


    @Override
    public String getBotToken() {
        return botToken;
    }

    /**
     * Точка входа для обмена сообщениями.
     *
     * @param update Входящее сообщение.
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            switch (messageText) {
                case "/start" -> {
                    message.setText("Привет, %s! Я помощник психолога".formatted(update.getMessage().getChat().getFirstName()));
                    sendMessage(message);
                }
                default -> {
                    message.setText("Нет такой команды");
                    sendMessage(message);
                }
            }
        }
    }

    public void sendMessage(SendMessage message) {
        try {
            execute(message);
            log.info("Чат id " + message.getChatId());
        } catch (TelegramApiException e) {
            log.error("Error during sending message {} to chatId {}", message.getText(), message.getChatId(), e);
            log.error("Меню не отправлено");
        }
    }

    public void sendMessageMail(String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setMessageThreadId(mailTopic);
        message.setText(text);
        sendMessage(message);
    }

    public void sendMessageException(String exceptionMessage) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setMessageThreadId(exceptionTopic);
        message.setText(exceptionMessage);
        sendMessage(message);
        log.info("Сообщение об ошибке направлено");
    }
}