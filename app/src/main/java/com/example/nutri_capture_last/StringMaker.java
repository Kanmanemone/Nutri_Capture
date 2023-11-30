package com.example.nutri_capture_last;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


final public class StringMaker extends Activity {

    // SharedPreferences 에디터 생성
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        editor = MainActivity.pref.edit();

    }//onCreate

    public static String returnTodayDateString() {
        SimpleDateFormat next_time_format = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN);
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());
        Date date = new Date(calendar.getTimeInMillis());
        String date_string = next_time_format.format(date);
        return date_string;
    }

    public static String returnDateStringInSharedPreference() {
        // SharedPreference 에서 날짜 가져오고, 만약 SharedPreference에 관련 정보가 없으면 그냥 오늘 날짜 가져옴
        String date_string = MainActivity.pref.getString("date_string", returnTodayDateString());
        return date_string;
    }


    public static String returnYear(String date_string) {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy년 MM월 dd일");
        Date date = new Date();

        try {
            date = format1.parse(date_string);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat format2 = new SimpleDateFormat("yyyy", Locale.KOREAN);
        String year = format2.format(date);

        return year;
    }

    public static String returnMonth(String date_string) {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy년 MM월 dd일");
        Date date = new Date();

        try {
            date = format1.parse(date_string);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat format2 = new SimpleDateFormat("MM", Locale.KOREAN);
        String month = format2.format(date);

        return month;
    }


    public static String returnDayOfTheMonth(String date_string){
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy년 MM월 dd일");

        Date date = new Date();

        try {
            date = format1.parse(date_string);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat format2 = new SimpleDateFormat("dd", Locale.KOREAN);
        String dayOfTheMonth = format2.format(date);

        return dayOfTheMonth;
    }


    public static String returnDayOfTheWeek(String date_string) {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy년 MM월 dd일");
        Date date = new Date();

        try {
            date = format1.parse(date_string);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat format2 = new SimpleDateFormat("E", Locale.KOREAN);
        String dayOfTheWeek = format2.format(date);

        return dayOfTheWeek;
    }

    public static String ChangeDateAndReturnString(int how_many_days) throws ParseException {
        // SharedPreference 에서 날짜 가져오고, 만약 SharedPreference에 관련 정보가 없으면 그냥 오늘 날짜 가져옴
        String sharedPreferenceString = MainActivity.pref.getString("date_string", returnTodayDateString());
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy년 MM월 dd일");

        Date date = formatter1.parse(sharedPreferenceString);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, how_many_days);

        Date changed_date = calendar.getTime();
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREA);
        String date_string = formatter2.format(changed_date);

        return date_string;
    }

    public static String makeDateStringOnlyNumber(String date_string) throws ParseException {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy년 MM월 dd일");
        Date date = format1.parse(date_string);

        SimpleDateFormat format2 = new SimpleDateFormat("yyyyMMdd", Locale.KOREAN);
        String onlyNumber = format2.format(date);

        return onlyNumber;
    }

    // (예) 2021년 10월 31일 -> table20211031 로 변환 (용도: SQLite의 테이블 이름으로 쓰는 등)
    public static String makeDateStringToTableName(String date_string) {

        try {
            date_string = "table" + makeDateStringOnlyNumber(date_string);
        } catch (ParseException exception) {
            exception.printStackTrace();
        }

        return date_string;
    }

    // float 값을 소수 둘째자리'에서' 반올림하고, 끝이 ".0" 라면 그 부분을 잘라내서 보기 좋게 만든다.
    public static String toMakeItLookGood(float inputedUglyNumber) {
        String handsomeNumber = String.format("%.1f", inputedUglyNumber);

        if(handsomeNumber.contains(".0")) {
            handsomeNumber = handsomeNumber.replace(".0", "");
        }

        return handsomeNumber;
    }
    // 매개변수가 String인 버전
    public static String toMakeItLookGood(String inputedUglyNumberString) {
        float inputedUglyNumber = Float.parseFloat(inputedUglyNumberString);
        return toMakeItLookGood(inputedUglyNumber);
    }
    // End of toMakeItLookGood()

    public static boolean isRationalNumberExceptNegative(TextView textView) {
        Pattern GET_NUMBER = Pattern.compile("([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)|([0-9]+)");
        Matcher matcher1 = GET_NUMBER.matcher(textView.getText().toString());
        if(!matcher1.matches()) { // 유리수 패턴을 찾지 못했다면 = 유리수가 아니라면
            return false;
        } else {
            return true;
        }
    }

    public static boolean isRationalNumberExceptNegative(String string) {
        Pattern GET_NUMBER = Pattern.compile("([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)|([0-9]+)");
        Matcher matcher1 = GET_NUMBER.matcher(string);
        if(!matcher1.matches()) { // 유리수 패턴을 찾지 못했다면 = 유리수가 아니라면
            return false;
        } else {
            return true;
        }
    }

    public static boolean isRationalNumberOnlyPositive(TextView textView) {
        Pattern GET_NUMBER = Pattern.compile("([1-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)|([0-9]+)");
        Matcher matcher1 = GET_NUMBER.matcher(textView.getText().toString());
        if(!matcher1.matches()) { // 유리수 패턴을 찾지 못했다면 = 유리수가 아니라면
            return false;
        } else {
            return true;
        }
    }

    public static boolean isRationalNumberOnlyPositive(String string) {
        Pattern GET_NUMBER = Pattern.compile("([1-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)|([0-9]+)");
        Matcher matcher1 = GET_NUMBER.matcher(string);
        if(!matcher1.matches()) { // 유리수 패턴을 찾지 못했다면 = 유리수가 아니라면
            return false;
        } else {
            return true;
        }
    }

}// End of Class









/* ~~~ 코드 저장고 ~~~

1
    date_string = date_string.replace("년 ", "");
    date_string = date_string.replace("월 ", "");
    date_string = date_string.replace("일 ", "");
    date_string = date_string.replace("요일", "");

    date_string = date_string.replace("월", "");
    date_string = date_string.replace("화", "");
    date_string = date_string.replace("수", "");
    date_string = date_string.replace("목", "");
    date_string = date_string.replace("금", "");
    date_string = date_string.replace("토", "");
    date_string = date_string.replace("일", "");

2
    뀨


*/


