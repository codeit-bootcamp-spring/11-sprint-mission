package com.sprint.mission;

public class TwoDimensionalArray {
    public static void main(String[] args) {
        // 3x3 2차원 배열 선언 및 초기화
        int[][] array = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };

        // 2차원 배열 출력
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                System.out.print(array[i][j] + " ");
            }
            System.out.println();
        }
    }
}