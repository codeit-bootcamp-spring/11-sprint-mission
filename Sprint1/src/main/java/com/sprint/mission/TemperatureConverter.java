package com.sprint.mission;

public class TemperatureConverter {
    public static void main(String[] args) {
        int celsius = 25;

        double 화씨 = (celsius * 9.0/5.0) + 32.0;
        // 화씨 변환 (정확한 실수 계산)
        double 켈빈 = celsius + 273.15;
        // 켈빈 변환

        System.out.println("섭씨: " + celsius );
        System.out.println("화씨: " + 화씨 );
        System.out.println("켈빈: " + 켈빈 );
        // 결과 출력
    }
}
