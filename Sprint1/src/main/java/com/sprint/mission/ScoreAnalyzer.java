package com.sprint.mission;

public class ScoreAnalyzer {
    public static void main(String[] args) {
        // 배열 선언 및 초기화
        int[] score = {85, 90, 78, 92 ,88};

        // 평균 계산
        int sum = 0;
        for (int s : score) {
            sum += s;
        }
        double average = (double) sum / score.length;
        int highCnt = 0;
        int lowCnt = 0;

        // 평균보다 높은/낮은 점수 개수 세기
        for (int s : score) {
            if (s > average) {
                highCnt++;
            }
            else if (s < average) {
                lowCnt++;
            }
        }

        System.out.printf("평균: %.1f\n", average);
        System.out.println("평균보다 높은 점수 개수:" + highCnt);
        System.out.println("평균보다 낮은 점수 개수:" + lowCnt);
    }
}