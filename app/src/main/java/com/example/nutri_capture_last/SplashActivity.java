package com.example.nutri_capture_last;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_TIME = 1000;

    // SharedPreferences 객체 및 에디터 생성
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //(SharedPreferences) 객체와 에디터 연결
        pref = getSharedPreferences("Pref", MODE_PRIVATE);
        editor = pref.edit();

        //핸들러 관련 내용은 일단 보지 말고, 딜레이를 줘서 몇 초 뒤에 실행을 하기 위한 코드라고만 생각하자.
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(checkAppFirstRun()){
                    startActivity(new Intent(getApplication(), FirstUserInformationActivity.class));
                    SplashActivity.this.finish();
                } else {
                    startActivity(new Intent(getApplication(),MainActivity.class));
                    SplashActivity.this.finish();
                }
            }
        }, SPLASH_DISPLAY_TIME);
    }//onCreate

    //스플래쉬 액티비티에서는 뒤로가기 버튼 눌러도 응답없게 만들기
    @Override
    public void onBackPressed(){
    }

    private Boolean checkAppFirstRun() {
        boolean isAppFirstRun = pref.getBoolean("isAppFirstRun",true);
        return isAppFirstRun;
    }//End of checkFirstRun()

}//SplashActivity
