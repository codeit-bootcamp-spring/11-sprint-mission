package com.sprint.mission;

public class ArrayBasic {
    public static void main(String[] args) {
        // 배열 선언 및 초기화
        int [] number = {10, 20, 30, 40, 50};

        // 배열 요소 출력
        for (int i =0; i < number.length; i++) {
            System.out.println("인덱스 " + i + ": " + number[i]);
        }
    }
}