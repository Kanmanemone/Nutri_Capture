package com.example.nutri_capture_last;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public final class Calculator extends Activity {

    // SharedPreferences 에디터 생성
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        editor = MainActivity.pref.edit();

    }//onCreate

     public static String toMakeItPercent(float inputedNotPercentNumber, String kind) {
        float userDayKcal = Float.parseFloat(MainActivity.pref.getString("userDayKcal", ""));
        String percentString="Error";

        float i;
        if(kind.equals("탄수화물")){
            i = 324;
        } else if(kind.equals("지방")) {
            i = 54;
        } else if(kind.equals("단백질")) {
            i = 55;
        } else if(kind.equals("당류")) {
            i = 100;
        } else if(kind.equals("포화지방")) {
            i = 15;
        } else if(kind.equals("콜레스테롤")) {
            i = 300;
        } else if(kind.equals("나트륨")) {
            i = 2000;
        } else {
            return percentString;
        }

        float f1 = (inputedNotPercentNumber/i)*100*(2000/userDayKcal);
        percentString = StringMaker.toMakeItLookGood(f1);

        return percentString;
    }

    public static String toMakeItPercent(String inputedNotPercentNumberString, String kind) {
        float inputedNotPercentNumber = Float.parseFloat(inputedNotPercentNumberString);
        return Calculator.toMakeItPercent(inputedNotPercentNumber, kind);
    }// End of toMakeItPercent()

    public static void setTextViewColor(TextView textView, float percent, Context context) {
        if((30<=percent)&&(percent<60)) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.orange1));
        } else if (60<=percent) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            textView.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
    }// End of setTextViewColor

    public static String getColorByPercent(float percent) {
        String tmpString = "";

        if((30<=percent)&&(percent<60)) {
            tmpString = "orange";
        } else if (60<=percent) {
            tmpString = "red";
        } else {
            tmpString = "black";
        }

        return tmpString;
    }// End of setTextViewColor

    public static String getColorByPercent(String percent) {
        float tmp = Float.parseFloat(percent);
        return getColorByPercent(tmp);
    }// End of setTextViewColor

    public static void causeDeliberateError() {
        int zero = 0;
        int anyInt = 1;
        int errorInt = 0;
        errorInt = anyInt/zero;
    }
}
