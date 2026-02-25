package com.stepup;


import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.util.Scanner;

public class Main {

    static class LineTooLongException extends RuntimeException {
        public LineTooLongException(String message) {
            super(message);
        }
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
        int maxLength = 0;
        int minLength = Integer.MAX_VALUE;
        boolean hasLines = false;

        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                int length = line.length();

                if (length > 1024) {
                    throw new LineTooLongException("Длина строки превышает 1024 символа: " + length);
                }

                totalLines++;

                if (length > maxLength) {
                    maxLength = length;
                }

                if (length < minLength) {
                    minLength = length;
                }

                hasLines = true;
            }

            if (!hasLines) {
                minLength = 0;
            }

            System.out.println("Общее количество строк в файле: " + totalLines);
            System.out.println("Длина самой длинной строки: " + maxLength);
            System.out.println("Длина самой короткой строки: " + minLength);

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
