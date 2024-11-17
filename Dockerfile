# Используем официальный образ с Java 17
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR файл в контейнер
COPY target/tgMailBot-0.0.1.jar /app/tgMailBot-0.0.1.jar

# Открываем порт, на котором приложение будет работать
EXPOSE 8080

# Устанавливаем переменные окружения для подключения к почтовому серверу и Telegram боту
# Эти переменные можно будет настроить в Render через UI
ENV USER_NAME=your-email@example.com
ENV PASSWORD_MAIL=your-email-password
ENV HOST_MAIL=your-mail-server.com
ENV BOT_NAME=your-bot-name
ENV BOT_TOKEN=your-bot-token
ENV BOT_CHAT=your-chat-id
ENV BOT_TOPIC_MAIL=your-mail-topic
ENV BOT_TOPIC_EXCEPTION=your-exception-topic

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "tgMailBot-0.0.1.jar"]