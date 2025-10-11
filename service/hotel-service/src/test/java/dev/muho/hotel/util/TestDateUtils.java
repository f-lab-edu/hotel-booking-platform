package dev.muho.hotel.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 테스트에서 사용하는 동적 날짜 생성 유틸리티 클래스
 * 하드코딩된 날짜 대신 현재 시점을 기준으로 동적으로 날짜를 생성하여
 * 시간이 지나도 테스트가 안정적으로 동작하도록 보장합니다.
 */
public class TestDateUtils {

    // 날짜 포맷터
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 내일 날짜를 반환합니다.
     */
    public static String getTomorrowDate() {
        return LocalDate.now().plusDays(1).format(DATE_FORMATTER);
    }

    /**
     * 현재로부터 N일 후의 날짜를 반환합니다.
     */
    public static String getFutureDatePlusDays(int days) {
        return LocalDate.now().plusDays(days).format(DATE_FORMATTER);
    }

    /**
     * 현재로부터 N일 후의 LocalDate를 반환합니다.
     */
    public static LocalDate getFutureLocalDatePlusDays(int days) {
        return LocalDate.now().plusDays(days);
    }

    /**
     * 5년 전 날짜를 반환합니다.
     */
    public static String getPastDate() {
        return LocalDate.now().minusYears(5).format(DATE_FORMATTER);
    }

    /**
     * 5년 전으로부터 N일 후의 날짜를 반환합니다.
     */
    public static String getPastDatePlusDays(int days) {
        return LocalDate.now().minusYears(5).plusDays(days).format(DATE_FORMATTER);
    }

    /**
     * 현재로부터 N년 후의 날짜를 반환합니다.
     */
    public static String getFutureDateInYears(int years) {
        return LocalDate.now().plusYears(years).format(DATE_FORMATTER);
    }

    /**
     * 다음 윤년의 2월 29일을 찾아서 반환합니다.
     */
    public static String getLeapYearDate() {
        int currentYear = LocalDate.now().getYear();
        int nextLeapYear = currentYear;
        while (!isLeapYear(nextLeapYear) || nextLeapYear <= currentYear) {
            nextLeapYear++;
        }
        return nextLeapYear + "-02-29";
    }

    /**
     * 다음 평년의 2월 29일 (존재하지 않는 날짜)을 반환합니다.
     */
    public static String getInvalidLeapYearDate() {
        int currentYear = LocalDate.now().getYear();
        int nextNonLeapYear = currentYear;
        while (isLeapYear(nextNonLeapYear) || nextNonLeapYear <= currentYear) {
            nextNonLeapYear++;
        }
        return nextNonLeapYear + "-02-29";
    }

    /**
     * 윤년 여부를 판별합니다.
     */
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    /**
     * 오늘 날짜를 String으로 반환합니다.
     */
    public static String getTodayDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * 오늘 날짜를 LocalDate로 반환합니다.
     */
    public static LocalDate getTodayLocalDate() {
        return LocalDate.now();
    }
}
