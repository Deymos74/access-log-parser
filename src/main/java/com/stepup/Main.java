package com.stepup;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Введите число:");
        int number = new Scanner(System.in).nextInt();

        System.out.println("Введите число 2:");
        int number2 = new Scanner(System.in).nextInt();

        int raznost = number - number2;
        System.out.println(raznost);

        int sum = number2 + number;
        System.out.println(sum);

        int multiplication = number * number2;
        System.out.println(multiplication);

        double quotient = (double) number / number2;
        System.out.println(quotient);

    }
}