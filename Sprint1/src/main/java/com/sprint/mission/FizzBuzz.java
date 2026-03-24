package com.sprint.mission;

public class FizzBuzz {
    public static void main(String[] args) {
        for (int i = 1; i <= 100; i++)
            if (i % 15 == 0) {
                System.out.println("FizzBuzz");
                // "FizzBuzz" 출력
            }
            else if (i % 3 == 0){
                System.out.println("Fizz");
                // "Fizz" 출력
            }
            else if (i % 5 == 0){
                System.out.println("Buzz");
                // "Buzz" 출력
            }
            else {
                System.out.println(i);
                // 숫자 i 출력
            }
        // 1부터 100까지 반복하며 조건에 따라 출력
    }

}
