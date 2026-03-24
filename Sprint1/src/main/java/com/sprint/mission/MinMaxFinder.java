package com.sprint.mission;

public class MinMaxFinder {
    public static void main(String[] args) {
        // 배열 선언 및 초기화
        int[] number = {3, 7, 2, 9, 1, 5, 8};

        int max = number[0];
        int min = number[0];

        for (int i = 1; i < number.length; i++) {
            if (number[i] > max) {
                max = number[i];
            }
            if (number[i] < min) {
                min = number[i];
            }

        }

        // 최댓값과 최솟값 찾기
        System.out.println("최댓값: " + max);
        System.out.println("최솟값: " + min);
    }
}