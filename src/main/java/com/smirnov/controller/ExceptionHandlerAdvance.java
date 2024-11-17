package com.smirnov.controller;

import com.smirnov.exception.MailException;
import com.smirnov.configuration.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ExceptionHandlerAdvance {

    private final TelegramBot telegramBot;

    @ExceptionHandler
    public String notFoundExceptionException(MailException e) {
        return sendMessage(e);
    }


    private String sendMessage(Exception exception) {
        String exceptionMessage = exception.getMessage();
        log.error(exceptionMessage);
        telegramBot.sendMessageException(exceptionMessage);
        return exceptionMessage;
    }
}
