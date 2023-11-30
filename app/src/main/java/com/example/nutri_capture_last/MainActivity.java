package com.example.nutri_capture_last;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;

public class MainActivity extends AppCompatActivity {

    // 곧 나올 onCreate()에서 =this 안 해주면 NPE 생긴다. 주의.
    public static Context context_main;

    //프래그먼트관련
    private FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction transaction;
    private NutrientFragment1 nutrientFragment1 = new NutrientFragment1();
    private NutrientFragment2 nutrientFragment2 = new NutrientFragment2();
    private InsightFragment insightFragment = new InsightFragment();
    private MeFragment meFragment = new MeFragment();

    // SharedPreferences 객체 및 에디터 생성
    static public SharedPreferences pref;
    SharedPreferences.Editor editor;

    // 다른 클래스 레퍼런스 생성
    static final StringMaker stringMaker = new StringMaker();

    //위젯 변수 이름들
    //툴바 관련
    androidx.appcompat.widget.Toolbar toolbar1;
    Button buttonLeft;
    TextView textTitle;
    Button buttonRight;

    //기타
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context_main = this;

        //(SharedPreferences) 객체와 에디터 연결
        pref = getSharedPreferences("Pref", MODE_PRIVATE);
        editor = pref.edit();

        // 위젯들 인플레이트 및 버튼클릭리스너 생성
        inflateWidget();

        // 오늘 날짜 SharedPreference와 textTitle에 넣기
        editor.putString("date_string", stringMaker.returnTodayDateString());
        editor.apply();
        textTitle.setText(pref.getString("date_string", ""));

        changeFragmentTo("nutrientFragment");

    }//onCreate()

    private void inflateWidget() {
        toolbar1 = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar1);
        buttonLeft = findViewById(R.id.buttonLeft);
        textTitle = findViewById(R.id.textTitle);
        buttonRight = findViewById(R.id.buttonRight);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date_string = null;
                try {
                    date_string = stringMaker.ChangeDateAndReturnString(-1);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                editor.putString("date_string", date_string);
                editor.apply();

                updateDate();
            }
        });

        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date_string = null;
                try {
                    date_string = stringMaker.ChangeDateAndReturnString(1);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                editor.putString("date_string", date_string);
                editor.apply();

                updateDate();
            }
        });

        textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            };
        });// End of textTitle.setOnClickListener()

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId())
                {
                    case R.id.nutrientItem:
                        changeFragmentTo("nutrientFragment");
                        break;
                    // case R.id.insightItem:
                        // changeFragmentTo(insightFragment);
                        // break;
                    case R.id.meItem:
                        changeFragmentTo(meFragment);
                        break;

                }//End of switch()
                return true;
            }// public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
        });// End of bottomNavigationView.setOnNavigationItemSelectedListener()
    }

    private void updateDate() {
        String date_string = pref.getString("date_string", stringMaker.returnTodayDateString());
        TextView textDate = findViewById(R.id.textTitle);
        textDate.setText(date_string);

        int nutrientViewMode = pref.getInt("nutrientViewMode",1);
        if(nutrientViewMode == 1) {
            nutrientFragment1.onStart();
        } else { // if(nutrientViewMode == 2)
            nutrientFragment2.onStart();
        }

    }

    public void changeFragmentTo(Fragment fragment) {

        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayout, fragment, fragment.getClass().toString()).commitAllowingStateLoss();

    }// End of changeFragmentTo()

    public void changeFragmentTo(String fragment) {

        int nutrientViewMode = pref.getInt("nutrientViewMode",1);

        if(nutrientViewMode == 1) {
            transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frameLayout, nutrientFragment1, nutrientFragment1.getClass().toString()).commitAllowingStateLoss();
        } else { // if(nutrientViewMode == 2)
            transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frameLayout, nutrientFragment2, nutrientFragment2.getClass().toString()).commitAllowingStateLoss();
        }


    }// End of changeFragmentTo()

    public void workAfterReplaceFragmentTo(Fragment fragment) {
        if(fragment.getClass().toString().equals(nutrientFragment1.getClass().toString())) {
            textTitle.setText(pref.getString("date_string", ""));
            buttonLeft.setVisibility(View.VISIBLE);
            buttonRight.setVisibility(View.VISIBLE);
        } else if(fragment.getClass().toString().equals(nutrientFragment2.getClass().toString())) {
            textTitle.setText(pref.getString("date_string", ""));
            buttonLeft.setVisibility(View.VISIBLE);
            buttonRight.setVisibility(View.VISIBLE);
        } else if(fragment.getClass().toString().equals(insightFragment.getClass().toString())) {
            textTitle.setText("돌아보기");
            buttonLeft.setVisibility(View.INVISIBLE);
            buttonRight.setVisibility(View.INVISIBLE);
        } else if(fragment.getClass().toString().equals(meFragment.getClass().toString())) {
            textTitle.setText("사용자 정보");
            buttonLeft.setVisibility(View.INVISIBLE);
            buttonRight.setVisibility(View.INVISIBLE);
        }
    }// End of workAfterReplaceFragmentTo()

}//MainActivity


/* ~~~ 코드 저장고 ~~~

1
    textTitle.setOnClickListener(new View.OnClickListener() {
            String dateStringOriginal = pref.getString("date_string", StringMaker.returnTodayDateString());
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener callbackMethod = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month + 1;
                        String monthString;
                        if(1<=month||month<=9) {
                             monthString = "0" + String.valueOf(month);
                        } else {
                            monthString = String.valueOf(month);
                        }

                        String dayOfTheMonthString;
                        if(1<=dayOfMonth||dayOfMonth<=9) {
                            dayOfTheMonthString = "0" + String.valueOf(dayOfMonth);
                        } else {
                            dayOfTheMonthString = String.valueOf(dayOfMonth);
                        }

                        String dateStringNew = String.valueOf(year)+ "년" + " " + monthString + "월" + " " + dayOfTheMonthString + "일" + " " + StringMaker.returnDayOfTheWeek(dateStringOriginal) + "요일";

                        editor.putString("date_string", dateStringNew);
                        editor.apply();

                        nutrientFragment.onStart();
                    }
                };
                DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, callbackMethod, Integer.parseInt(StringMaker.returnYear(dateStringOriginal)), Integer.parseInt(StringMaker.returnMonth(dateStringOriginal)), Integer.parseInt(StringMaker.returnDayOfTheMonth(dateStringOriginal)));
                dialog.show();
            }
        });// End of textTitle.setOnClickListener()

2
    뀨


*/