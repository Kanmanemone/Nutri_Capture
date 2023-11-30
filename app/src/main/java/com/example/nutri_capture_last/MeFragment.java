package com.example.nutri_capture_last;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MeFragment extends Fragment {

    // SharedPreferences 객체 및 에디터 생성
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    // 위젯 이름
    RadioGroup radioGroup;
    RadioButton radioButton1, radioButton2;
    TextView textHeight, textWeight, textKcal;
    Button buttonEdit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //(SharedPreferences) 객체와 에디터 연결
        pref = this.getActivity().getSharedPreferences("Pref", MODE_PRIVATE);
        editor = pref.edit();

    }// End of onCreate()

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View meFragment_v = inflater.inflate(R.layout.fragment_me, container, false);

        radioGroup = meFragment_v.findViewById(R.id.radioGroup);
        radioButton1 = meFragment_v.findViewById(R.id.radioButton1);
        radioButton2 = meFragment_v.findViewById(R.id.radioButton2);
        textHeight = meFragment_v.findViewById(R.id.textHeight);
        textWeight = meFragment_v.findViewById(R.id.textWeight);
        textKcal = meFragment_v.findViewById(R.id.textKcal);
        buttonEdit = meFragment_v.findViewById(R.id.buttonEdit);

        if(pref.getString("userGender", "").equals("남자")) {
            radioButton1.setChecked(true);
        } else if(pref.getString("userGender", "").equals("여자")) {
            radioButton2.setChecked(true);
        }

        // "재설정" 버튼
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), EditUserInformationActivity.class);
                startActivity(intent);
            }
        });

        return meFragment_v;
    }// End of onCreateView()

    @Override
    public void onStart() {
        super.onStart();

        // MainActivity의 툴바 속 TextView의 Text 변경 등등 ...
        ((MainActivity)MainActivity.context_main).workAfterReplaceFragmentTo(this);

        if(pref.getString("userGender", "").equals("남자")) {
            radioButton1.setChecked(true);
        } else if(pref.getString("userGender", "").equals("여자")) {
            radioButton2.setChecked(true);
        }
        textHeight.setText(pref.getString("userHeight",""));
        textWeight.setText(pref.getString("userWeight",""));
        textKcal.setText(pref.getString("userDayKcal",""));

    }//End of onStart

}// End of Class
