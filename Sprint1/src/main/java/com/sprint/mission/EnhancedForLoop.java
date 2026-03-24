package com.sprint.mission;

public class EnhancedForLoop {
    public static void main(String[] args) {
        // 점수 배열 선언 및 초기화
        int[] score = {85, 90, 78, 92, 88};

        // 향상된 for문으로 출력
        for (int s : score) {
            System.out.println(s + "점");
        }
    }
}