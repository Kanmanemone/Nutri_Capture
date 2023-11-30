package com.example.nutri_capture_last;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirstUserInformationActivity extends AppCompatActivity {

    // SharedPreferences 객체 및 에디터 생성
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    // activity_information에서 사용할 위젯들 이름
    RadioGroup radioGroup;
    EditText editHeight, editWeight;
    ConstraintLayout kcalInfo;
    TextView textKcal;
    Button buttonExit, buttonSave;

    // 데이터 저장
    String userGender = null;
    String userHeight = null;
    String userWeight = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infomation_first);

        //
        // (SharedPreferences) 객체와 에디터 연결
        pref = getSharedPreferences("Pref", MODE_PRIVATE);
        editor = pref.edit();

        // XML 위젯들 인플레이트
        radioGroup = findViewById(R.id.radioGroup);
        buttonExit = findViewById(R.id.buttonExit);
        buttonSave = findViewById(R.id.buttonSave);
        editHeight = findViewById(R.id.textHeight);
        editWeight = findViewById(R.id.textWeight);

        // 라디오 버튼
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radioButton1:
                        userGender = "남자";
                        break;
                    case R.id.radioButton2:
                        userGender = "여자";
                        break;
                }
            }
        });// End of radioGroup.setOnChecked ...

        // 종료 버튼
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });//End of buttonExit.setOnClick ...

        // 저장 버튼
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verifyInputData()) {
                    saveInputDataAndState();
                    startActivity(new Intent(getApplication(),MainActivity.class));
                    FirstUserInformationActivity.this.finish();
                } else {
                    Toast.makeText(FirstUserInformationActivity.this, "사용자 정보를 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });// End of buttonSave.setOnClick ...

    }//End of onCreate()

    private boolean verifyInputData() {

        // 성별
        if(userGender==null) {
            return false;
        }
        if(!(userGender.equals("남자")||userGender.equals("여자"))) {
            return false;
        }

        // 키 (소수점 둘째 자리에서 반올림 한다음에 검증)
        if(editHeight.getText().toString().equals("0")){
            editHeight.setText("");
            return false;
        }
        Pattern GET_NUMBER = Pattern.compile("([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)|([0-9]+)");
        Matcher matcher1 = GET_NUMBER.matcher(editHeight.getText().toString());
        if(!matcher1.matches()) { // 유리수 패턴을 찾지 못했다면 = 유리수가 아니라면
            editHeight.setText("");
            return false;
        }

        // 체중 (소수점 둘째 자리에서 반올림 한다음에 검증)
        if(editWeight.getText().toString().equals("0")){
            editWeight.setText("");
            return false;
        }
        Matcher matcher2 = GET_NUMBER.matcher(editWeight.getText().toString());
        if(!matcher2.matches()) { // 유리수 패턴을 찾지 못했다면 = 유리수가 아니라면
            editWeight.setText("");
            return false;
        }

        // 전부 통과했다면 true 반환
        return true;
    }

    private void saveInputDataAndState() {
        //성별 저장
        editor.putString("userGender",userGender);

        //키 저장
        String editHeightString = editHeight.getText().toString();
        float editHeightfloat = Float.parseFloat(editHeightString);
        String editHeightRound =  String.format("%.1f", editHeightfloat);
        editor.putString("userHeight", editHeightRound);

        //체중 저장
        String editWeightString = editWeight.getText().toString();
        float editWeightfloat = Float.parseFloat(editWeightString);
        String editWeightRound =  String.format("%.1f", editWeightfloat);
        editor.putString("userWeight", editWeightRound);

        //칼로리 일일권장섭취량 저장
        float standardWeight=0;
        if(userGender.equals("남자")) {
            standardWeight = (editHeightfloat/100)*(editHeightfloat/100)*22;
        } else if(userGender.equals("여자")) {
            standardWeight = (editHeightfloat/100)*(editHeightfloat/100)*21;
        }
        float standardKcal = standardWeight*33;
        String standardKcalRound = String.format("%.0f", standardKcal);

        editor.putString("userDayKcal", standardKcalRound);

        // FirstUserInformationActivity가 다시 안나타나도록 설정
        editor.putBoolean("isAppFirstRun", false);

        // 모두 저장
        editor.apply();

    }





}//End of Class
