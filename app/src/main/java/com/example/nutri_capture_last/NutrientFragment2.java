package com.example.nutri_capture_last;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class NutrientFragment2 extends Fragment {

    View nutrientFragment_v;

    // SharedPreferences 객체 및 에디터 생성
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    // 위젯 이름
    TextView nowKcal, standardKcal, number, amount, carbohydrate,
            carbohydratePercent, totalFat, totalFatPercent, protein, proteinPercent,
            sugars, sugarsPercent, saturatedFat, saturatedFatPercent, cholesterol,
            cholesterolPercent, sodium, sodiumPercent, transFat;
    Button buttonReset, buttonAdd;

    // 내부 계산에 쓸 값
    float amountValue, nowKcalValue, sodiumValue, carbohydrateValue, sugarsValue,
        totalFatValue, transFatValue, saturatedFatValue, cholesterolValue, proteinValue;

    long itemCount = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //(SharedPreferences) 객체와 에디터 연결
        pref = this.getActivity().getSharedPreferences("Pref", MODE_PRIVATE);
        editor = pref.edit();


    }//End of onCreate()

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        nutrientFragment_v = inflater.inflate(R.layout.fragment_nutrient_2, container, false);

        // 위젯 인플레이트 및 버튼 클릭 리스너 설정
        inflateWidget();

        return nutrientFragment_v;
    }// End of onCreateView()

    @Override
    public void onStart() {
        super.onStart();

        // MainActivity의 툴바 속 TextView의 Text 변경 등등 ...
        ((MainActivity)MainActivity.context_main).workAfterReplaceFragmentTo(this);

        // SharedPreference에서 불러온 날짜에 맞추어 영양 정보 갱신
        updateNutrient();

    }//End of onStart

    public void inflateWidget() {
        // 인플레이트
        nowKcal = nutrientFragment_v.findViewById(R.id.text0111);
        standardKcal = nutrientFragment_v.findViewById(R.id.text0112);
        number = nutrientFragment_v.findViewById(R.id.text021);
        amount = nutrientFragment_v.findViewById(R.id.text0221);
        carbohydrate = nutrientFragment_v.findViewById(R.id.text031);
        carbohydratePercent = nutrientFragment_v.findViewById(R.id.text0321);
        totalFat = nutrientFragment_v.findViewById(R.id.text041);
        totalFatPercent = nutrientFragment_v.findViewById(R.id.text0421);
        protein = nutrientFragment_v.findViewById(R.id.text051);
        proteinPercent = nutrientFragment_v.findViewById(R.id.text0521);
        sugars = nutrientFragment_v.findViewById(R.id.text061);
        sugarsPercent = nutrientFragment_v.findViewById(R.id.text0621);
        saturatedFat = nutrientFragment_v.findViewById(R.id.text071);
        saturatedFatPercent = nutrientFragment_v.findViewById(R.id.text0721);
        cholesterol = nutrientFragment_v.findViewById(R.id.text081);
        cholesterolPercent = nutrientFragment_v.findViewById(R.id.text0821);
        sodium = nutrientFragment_v.findViewById(R.id.text091);
        sodiumPercent = nutrientFragment_v.findViewById(R.id.text0921);
        transFat = nutrientFragment_v.findViewById(R.id.text1021);
        buttonAdd = nutrientFragment_v.findViewById(R.id.buttonAdd);
        buttonReset = nutrientFragment_v.findViewById(R.id.buttonReset);

        // 클릭리스너
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), CameraActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); //(참조: https://medium.com/@saqwzx88/%EC%95%A1%ED%8B%B0%EB%B9%84%ED%8B%B0-%EC%8A%A4%ED%83%9D-%EA%B4%80%EB%A6%AC-3e5219510ac7)
                startActivity(intent);
            }
        });
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (itemCount == 0) {

                    Toast.makeText(getContext(), "항목이 없습니다.", Toast.LENGTH_SHORT).show();

                } else {

                    //dialog (https://jhshjs.tistory.com/59)
                    Dialog dialog1 = new Dialog(getContext());
                    dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog1.setContentView(R.layout.dialog_confirm);

                    dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 모서리를 둥글게 하기 위함 (https://jhshjs.tistory.com/60)
                    int x = Resources.getSystem().getDisplayMetrics().widthPixels; //(https://citynetc.tistory.com/208), (https://stackoverflow.com/questions/4743116/get-screen-width-and-height-in-android)
                    int y = Resources.getSystem().getDisplayMetrics().heightPixels;
                    dialog1.getWindow().setLayout((int)(x * 0.7f), (int)(y * 0.3f));

                    Button buttonBack = dialog1.findViewById(R.id.buttonBack);
                    Button buttonNext = dialog1.findViewById(R.id.buttonNext);
                    TextView textTitle = dialog1.findViewById(R.id.textTitle);
                    TextView textContent1 = dialog1.findViewById(R.id.textContent1);

                    textTitle.setText("초기화");
                    textContent1.setText("항목 " + String.valueOf(itemCount) + "개를 삭제할까요?");

                    dialog1.show();

                    buttonBack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog1.dismiss();
                        }
                    });

                    buttonNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // SQLite 관련
                            // 데이터베이스 열기
                            SQLiteDatabase database = getActivity().openOrCreateDatabase("ggu", MODE_PRIVATE, null);

                            String tableName = StringMaker.makeDateStringToTableName(StringMaker.returnDateStringInSharedPreference());
                            SQLiteVendingMachine.dropTable(database, tableName);

                            database.close();

                            // View 업데이트
                            updateNutrient();
                            itemCount = 0;

                            dialog1.dismiss();

                            Toast.makeText(getContext(), "초기화 완료", Toast.LENGTH_SHORT).show();
                        }
                    });
                }



            }
        });

        TextView modeText1 = nutrientFragment_v.findViewById(R.id.modeText1);
        modeText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("nutrientViewMode", 1);
                editor.apply();
                ((MainActivity) MainActivity.context_main).changeFragmentTo("nutrientFragment1");
            }
        });

    }// End of inflateWidget()

    private void updateNutrient() {
        //ShardPreference 관련
        standardKcal.setText(" / " + pref.getString("userDayKcal", "") + " kcal");

        // SQLite 관련
        // 데이터베이스 열기
        SQLiteDatabase database = getActivity().openOrCreateDatabase("ggu", MODE_PRIVATE, null);

        // 테이블 이름으로 조회해보고, 없으면 View에 죄다 0 넣고 갱신
        String tableName = StringMaker.makeDateStringToTableName(StringMaker.returnDateStringInSharedPreference());
        Boolean tableExsist = SQLiteVendingMachine.isTableExsist(database, tableName);
        if (tableExsist == false) {
            amountValue=0;
            nowKcalValue=0;
            carbohydrateValue=0;
            sodiumValue=0;
            sugarsValue=0;
            totalFatValue=0;
            transFatValue=0;
            saturatedFatValue=0;
            cholesterolValue=0;
            proteinValue=0;

            itemCount = 0;
            number.setText("0회");

        } else {

            // sum값 구해서 변수에 저장
            amountValue = SQLiteVendingMachine.getSumOfColumn(database,tableName,"total_amount");
            nowKcalValue = SQLiteVendingMachine.getSumOfColumn(database,tableName,"calories");
            carbohydrateValue = SQLiteVendingMachine.getSumOfColumn(database,tableName,"carbohydrate");
            sodiumValue = SQLiteVendingMachine.getSumOfColumn(database,tableName,"soduim");
            sugarsValue = SQLiteVendingMachine.getSumOfColumn(database,tableName,"sugars");
            totalFatValue = SQLiteVendingMachine.getSumOfColumn(database,tableName,"total_fat");
            transFatValue = SQLiteVendingMachine.getSumOfColumn(database,tableName,"trans_fat");
            saturatedFatValue = SQLiteVendingMachine.getSumOfColumn(database,tableName,"saturated_fat");
            cholesterolValue = SQLiteVendingMachine.getSumOfColumn(database,tableName,"cholesterol");
            proteinValue = SQLiteVendingMachine.getSumOfColumn(database,tableName,"protein");

            // 횟수(number) 관련 = table 속 record의 갯수
            itemCount = DatabaseUtils.queryNumEntries(database, tableName);
            number.setText(String.valueOf(itemCount)+"회");

        }

        database.close();

        // 절대값 관련
        amount.setText(StringMaker.toMakeItLookGood(amountValue)+"g(ml)");
        nowKcal.setText(StringMaker.toMakeItLookGood(nowKcalValue));
        sodium.setText(StringMaker.toMakeItLookGood(sodiumValue)+"mg");
        carbohydrate.setText(StringMaker.toMakeItLookGood(carbohydrateValue)+"g");
        sugars.setText(StringMaker.toMakeItLookGood(sugarsValue)+"g");
        totalFat.setText(StringMaker.toMakeItLookGood(totalFatValue)+"g");
        transFat.setText(StringMaker.toMakeItLookGood(transFatValue));
        saturatedFat.setText(StringMaker.toMakeItLookGood(saturatedFatValue)+"g");
        cholesterol.setText(StringMaker.toMakeItLookGood(cholesterolValue)+"mg");
        protein.setText(StringMaker.toMakeItLookGood(proteinValue)+"g");


        //퍼센트값 관련
        carbohydratePercent.setText(Calculator.toMakeItPercent(carbohydrateValue, "탄수화물"));
        Calculator.setTextViewColor(carbohydratePercent,Float.parseFloat(Calculator.toMakeItPercent(carbohydrateValue, "탄수화물")), getContext());
        totalFatPercent.setText(Calculator.toMakeItPercent(totalFatValue, "지방"));
        Calculator.setTextViewColor(totalFatPercent,Float.parseFloat(Calculator.toMakeItPercent(totalFatValue, "지방")), getContext());
        proteinPercent.setText(Calculator.toMakeItPercent(proteinValue, "단백질"));
        Calculator.setTextViewColor(proteinPercent,Float.parseFloat(Calculator.toMakeItPercent(proteinValue, "단백질")), getContext());
        sugarsPercent.setText(Calculator.toMakeItPercent(sugarsValue, "당류"));
        Calculator.setTextViewColor(sugarsPercent,Float.parseFloat(Calculator.toMakeItPercent(sugarsValue, "당류")), getContext());
        saturatedFatPercent.setText(Calculator.toMakeItPercent(saturatedFatValue, "포화지방"));
        Calculator.setTextViewColor(saturatedFatPercent,Float.parseFloat(Calculator.toMakeItPercent(saturatedFatValue, "포화지방")), getContext());
        cholesterolPercent.setText(Calculator.toMakeItPercent(cholesterolValue, "콜레스테롤"));
        Calculator.setTextViewColor(cholesterolPercent,Float.parseFloat(Calculator.toMakeItPercent(cholesterolValue, "콜레스테롤")), getContext());
        sodiumPercent.setText(Calculator.toMakeItPercent(sodiumValue, "나트륨"));
        Calculator.setTextViewColor(sodiumPercent,Float.parseFloat(Calculator.toMakeItPercent(sodiumValue, "나트륨")), getContext());

        if(transFatValue ==0) {
            
            transFat.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
        } else {
            transFat.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
        }

    }// End of updateNutrient()


    /*
    private void setTextViewColor(TextView textView, float percent) {
        if((30<=percent)&&(percent<70)) {
            textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.orange1));
        } else if (70<=percent) {
            textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
        } else {
            textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
        }
    }// End of setTextViewColor
     */

}// End of Class
