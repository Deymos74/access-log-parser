package com.stepup;


import java.io.*;
import java.util.Scanner;

public class Main {

    static class LineTooLongException extends RuntimeException {
        public LineTooLongException(String message) {
            super(message);
        }
    }

    static class LogEntry {
        String ipAddress;
        String dateTime;
        String requestMethod;
        String requestPath;
        int responseCode;
        int responseSize;
        String referer;
        String userAgent;
        String botName;

        @Override
        public String toString() {
            return String.format(
                    "IP: %s | Дата: %s | Запрос: %s %s | Код: %d | Размер: %d | Bot: %s",
                    ipAddress != null ? ipAddress : "N/A",
                    dateTime != null ? dateTime : "N/A",
                    requestMethod != null ? requestMethod : "N/A",
                    requestPath != null ? requestPath : "N/A",
                    responseCode,
                    responseSize,
                    botName != null ? botName : "не бот"
            );
        }
    }

    public static String extractBotName(String userAgent) {
        if (userAgent == null || userAgent.isEmpty() || userAgent.equals("-")) {
            return null;
        }

        if (userAgent.contains("Googlebot")) {
            return "Googlebot";
        }

        if (userAgent.contains("YandexBot")) {
            return "YandexBot";
        }

        try {
            int firstBracketIndex = userAgent.indexOf('(');
            int lastBracketIndex = userAgent.indexOf(')', firstBracketIndex);

            if (firstBracketIndex == -1 || lastBracketIndex == -1) {
                return null;
            }

            String firstBrackets = userAgent.substring(firstBracketIndex + 1, lastBracketIndex);
            String[] parts = firstBrackets.split(";");

            if (parts.length >= 2) {
                String fragment = parts[1].trim();
                int slashIndex = fragment.indexOf('/');
                if (slashIndex != -1) {
                    return fragment.substring(0, slashIndex).trim();
                }
                return fragment;
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        return null;
    }

    public static LogEntry parseLogLine(String line) {
        LogEntry entry = new LogEntry();

        try {
            String[] parts = line.split(" ");

            if (parts.length < 10) {
                return entry;
            }

            entry.ipAddress = parts[0];

            for (int i = 0; i < parts.length; i++) {
                if (parts[i].startsWith("[")) {
                    String dateWithBracket = parts[i];
                    entry.dateTime = dateWithBracket.substring(1, dateWithBracket.length() - 1);
                    break;
                }
            }

            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("\"GET\"") || parts[i].equals("GET") ||
                        parts[i].equals("\"POST\"") || parts[i].equals("POST")) {
                    entry.requestMethod = parts[i].replace("\"", "");
                    if (i + 1 < parts.length) {
                        entry.requestPath = parts[i + 1];
                    }
                    break;
                }
            }

            for (int i = 0; i < parts.length; i++) {
                try {
                    int code = Integer.parseInt(parts[i]);
                    if (code >= 100 && code < 600) {
                        entry.responseCode = code;
                        if (i + 1 < parts.length) {
                            try {
                                entry.responseSize = Integer.parseInt(parts[i + 1]);
                            } catch (NumberFormatException e) {
                                entry.responseSize = 0;
                            }
                        }
                        break;
                    }
                } catch (NumberFormatException e) {
                    // Продолжаем поиск
                }
            }

            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i].matches("\\d+") && parts[i + 1].startsWith("\"")) {
                    String refererWithQuote = parts[i + 1];
                    if (refererWithQuote.length() > 1) {
                        entry.referer = refererWithQuote.substring(1, refererWithQuote.length() - 1);
                        if (entry.referer.equals("-")) {
                            entry.referer = null;
                        }
                    }
                    break;
                }
            }

            StringBuilder userAgentBuilder = new StringBuilder();
            boolean inUserAgent = false;

            for (int i = 0; i < parts.length; i++) {
                if (parts[i].contains("\"Mozilla") || parts[i].contains("Mozilla") ||
                        parts[i].contains("\"-\"") && i == parts.length - 2) {
                    inUserAgent = true;
                }

                if (inUserAgent) {
                    userAgentBuilder.append(parts[i]);
                    if (i < parts.length - 1) {
                        userAgentBuilder.append(" ");
                    }
                }
            }

            String ua = userAgentBuilder.toString().trim();
            if (!ua.isEmpty()) {
                ua = ua.replace("\"", "");
                if (!ua.equals("-")) {
                    entry.userAgent = ua;
                    entry.botName = extractBotName(entry.userAgent);
                }
            }

        } catch (Exception e) {
            // Игнорируем ошибки парсинга
        }

        return entry;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите путь к файлу: ");
        String filePath = scanner.nextLine();

        System.out.println("Чтение файла: " + filePath);

        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("Файл не существует: " + filePath);
            scanner.close();
            return;
        }

        if (file.isDirectory()) {
            System.out.println("Указанный путь является папкой, а не файлом: " + filePath);
            scanner.close();
            return;
        }

        int totalLines = 0;
        int googlebotCount = 0;
        int yandexbotCount = 0;

        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                totalLines++;

                if (line.length() > 1024) {
                    throw new LineTooLongException("Длина строки превышает 1024 символа: " + line.length());
                }

                LogEntry entry = parseLogLine(line);

                if (entry.botName != null) {
                    if (entry.botName.equals("Googlebot")) {
                        googlebotCount++;
                    } else if (entry.botName.equals("YandexBot")) {
                        yandexbotCount++;
                    }
                }
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("Файл успешно прочитан. Всего строк: " + totalLines);
            System.out.println("=".repeat(60));

            System.out.println("\nСТАТИСТИКА ПО БОТАМ:");
            System.out.println("-".repeat(40));
            System.out.printf("Googlebot: %d запросов\n", googlebotCount);
            System.out.printf("YandexBot: %d запросов\n", yandexbotCount);

            if (totalLines > 0) {
                double googlebotShare = (double) googlebotCount / totalLines * 100;
                double yandexbotShare = (double) yandexbotCount / totalLines * 100;
                double totalBotsShare = (double) (googlebotCount + yandexbotCount) / totalLines * 100;


                System.out.println("-".repeat(40));
                System.out.printf("Доля Googlebot: %.2f%%\n", googlebotShare);
                System.out.printf("Доля YandexBot: %.2f%%\n", yandexbotShare);
                System.out.printf("Общая доля ботов: %.2f%%\n", totalBotsShare);
            }
            System.out.println("=".repeat(60));

        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден: " + filePath);
            e.printStackTrace();
        } catch (LineTooLongException e) {
            System.out.println("Ошибка: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Произошла ошибка при чтении файла");
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}

//C:\Users\AFedorenko\Documents\access.log