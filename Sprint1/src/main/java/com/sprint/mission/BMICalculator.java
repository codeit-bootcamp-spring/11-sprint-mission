package com.sprint.mission;

public class BMICalculator {
    public static void main(String[] args) {
        double 키 = 1.75;
        double 몸무게 = 70.0;
        // 키와 몸무게 변수 선언 (키: 1.75m, 몸무게: 70kg)

        double bmi = 몸무게 / (키 * 키) ;
        // BMI 계산

        System.out.println("키: " + 키 + "m");
        System.out.println("몸무게: " + 몸무게 + "kg");
        System.out.println("BMI: " + bmi);
        // 결과 출력
    }
}