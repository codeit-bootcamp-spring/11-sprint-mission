package com.sprint.mission;

public class StarPattern {
    public static void main(String[] args) {
        for (int i =1; i <=6; i++) {
            for (int j = 1; j <= i; j++) {
                System.out.print("*");
            }
            System.out.println();
        }
        for (int i = 5; i >= 1; i--) {
            for (int j = 1; j <= i; j++) {
                System.out.print("*");
            }
            System.out.println();
        }//패턴 출력
    }


}