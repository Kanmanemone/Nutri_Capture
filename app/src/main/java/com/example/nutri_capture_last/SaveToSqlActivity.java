package com.example.nutri_capture_last;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Text;

import java.lang.ref.Reference;

public class SaveToSqlActivity extends AppCompatActivity {

    Context context = this;

    // SharedPreferences 객체 및 에디터 생성
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ProductInfomation deliveredProductInfomation;

    // 위젯 변수 이름들
    TextView nowKcalView, standardKcalView, /* numberView, */ amountView, carbohydrateView,
            carbohydratePercentView, totalFatView, totalFatPercentView, proteinView, proteinPercentView,
            sugarsView, sugarsPercentView, saturatedFatView, saturatedFatPercentView, cholesterolView,
            cholesterolPercentView, sodiumView, sodiumPercentView, transFatView;
    Button buttonExit, buttonSave;
    // SeekBar 관련
    SeekBar seekBar;
    TextView seekText2;
    // 다시 찍기
    ImageButton imageButton;

    // 위젯 변수 이름에 View를 빼고, 내부적인 계산을 위해서 내가 쓸 변수 이름들
    float nowKcal=0, standardKcal, /* number, */ amount=0, carbohydrate=0,
            carbohydratePercent, totalFat=0, totalFatPercent, protein, proteinPercent,
            sugars=0, sugarsPercent, saturatedFat=0, saturatedFatPercent, cholesterol=0,
            cholesterolPercent, sodium=0, sodiumPercent, transFat=0;

    float amountPerServing;

    String eatenNowKcal, eatenAmount, eatenCarbohydrate, eatenTotalFat, eatenProtein,
            eatenSugars, eatenSaturatedFat, eatenCholesterol, eatenSodium, eatenTransFat;

    // 제품 이름 저장 관련
    String productName = "";
    EditText productNameText2;

    // 카드들 Clickable로 만들고 -> 클릭 시 다이어로그로 값 변경 가능
    CardView
            card1, // nowKcalView
            card2, // amountView
            card3, // carbohydrateView, carbohydratePercentView
            card4, // totalFatView, totalFatPercentView
            card5, // proteinView, proteinPercentView
            card6, // sugarsView, sugarsPercentView
            card7, // saturatedFatView, saturatedFatPercentView
            card8, // cholesterolView, cholesterolPercentView
            card9, // sodiumView, sodiumPercentView
            card10 ; // transFatView

    // CardView 클릭리스너에서 값 변경후 updateNutrientView(progress)하기 위해서 seekbar의 progress를 전역으로 선언
    int progress = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        // 카메라로 찍었던 정보 받아오기
        Intent intent = getIntent();
        deliveredProductInfomation = intent.getExtras().getParcelable("capturedInfo");

        // 받아온 정보 관련 계산 작업 -> 이 메소드의 결과로 제품 '전체'(1회 제공량이 아니라, 전체)의 영양소 값 저장됨
        analysisParcelable();

        // 받아온 정보 받았으니 이제 삭제.
        intent.removeExtra("capturedInfo");

        //(SharedPreferences) 객체와 에디터 연결
        pref = getSharedPreferences("Pref", MODE_PRIVATE);
        editor = pref.edit();

        // 위젯들 인플레이트 및 버튼클릭리스너 생성
        inflateWidget();

        int firstProgress;
        if(amountPerServing==-1) {
             firstProgress =100;
        } else {
            firstProgress = decideFirstProgress();
        }

        // 화면의 각종 위젯들 Update
        seekBar.setProgress(firstProgress);// <- 여기에 updateNutrientView() 들어 있음.

    }// End of onCreate()

    private void analysisParcelable(){
        // 일단 Parcelable의 주요 정보들 복사
        // 1회 제공량이 -1 이라면 즉, 1회 제공량이 없다면 즉, parcelable의 값을 제품 전체의 값이라면, 즉, 통째로 그대로 가져다 써도 되는 거라면,
        if(deliveredProductInfomation.getAmountPerServing()==-1) {
            nowKcal = deliveredProductInfomation.getCalories();
            amount = deliveredProductInfomation.getTotalAmount();
            carbohydrate = deliveredProductInfomation.getCarbohydrate();
            totalFat = deliveredProductInfomation.getTotalFat();
            protein = deliveredProductInfomation.getProtein();
            sugars = deliveredProductInfomation.getSugars();
            saturatedFat = deliveredProductInfomation.getSaturatedFat();
            cholesterol = deliveredProductInfomation.getCholesterol();
            sodium = deliveredProductInfomation.getSoduim();
            transFat = deliveredProductInfomation.getTransFat();
        }
        // 1회 제공량이 -1이 아니라면 즉, 1회 제공량이 존재한다면 즉, parcelable의 값이 제품의 일부분의 값이면, 즉, 통째로 그대로 가져다쓰면 안되고 약간의 계산이 필요하다면
        else {
            float piggyLevel = deliveredProductInfomation.getTotalAmount()/deliveredProductInfomation.getAmountPerServing();

            nowKcal = deliveredProductInfomation.getCalories()*piggyLevel;
            amount = deliveredProductInfomation.getTotalAmount(); //이건 어느 경우든 영양성분표에 전체값이 적혀있으므로, 어느 경우든 그냥 그대로 가져오면 된다.
            carbohydrate = deliveredProductInfomation.getCarbohydrate()*piggyLevel;
            totalFat = deliveredProductInfomation.getTotalFat()*piggyLevel;
            protein = deliveredProductInfomation.getProtein()*piggyLevel;
            sugars = deliveredProductInfomation.getSugars()*piggyLevel;
            saturatedFat = deliveredProductInfomation.getSaturatedFat()*piggyLevel;
            cholesterol = deliveredProductInfomation.getCholesterol()*piggyLevel;
            sodium = deliveredProductInfomation.getSoduim()*piggyLevel;
            transFat = deliveredProductInfomation.getTransFat()*piggyLevel;

        }

        // 필요한 코드니까, 삭제하지 말 것
        amountPerServing = deliveredProductInfomation.getAmountPerServing();

        // 어찌됐든 이 주석의 위치에서는, 이 제품 '전체'(1회 제공량이 아니라, 전체)의 영양소 값 저장 완료.

    }// End of analysisParcelable()

    private void inflateWidget() {
        // 인플레이트
        nowKcalView = findViewById(R.id.text0111);
        standardKcalView = findViewById(R.id.text0112);
        amountView = findViewById(R.id.text0221);
        carbohydrateView = findViewById(R.id.text031);
        carbohydratePercentView = findViewById(R.id.text0321);
        totalFatView = findViewById(R.id.text041);
        totalFatPercentView = findViewById(R.id.text0421);
        proteinView = findViewById(R.id.text051);
        proteinPercentView = findViewById(R.id.text0521);
        sugarsView = findViewById(R.id.text061);
        sugarsPercentView = findViewById(R.id.text0621);
        saturatedFatView = findViewById(R.id.text071);
        saturatedFatPercentView = findViewById(R.id.text0721);
        cholesterolView = findViewById(R.id.text081);
        cholesterolPercentView = findViewById(R.id.text0821);
        sodiumView = findViewById(R.id.text091);
        sodiumPercentView = findViewById(R.id.text0921);
        transFatView = findViewById(R.id.text1021);
        buttonExit = findViewById(R.id.buttonExit);
        buttonSave = findViewById(R.id.buttonSave);
        // 다시 찍기
        imageButton = findViewById(R.id.imageButton);
        // 시크바 관련
        seekBar = findViewById(R.id.seekBar);
        seekText2 = findViewById(R.id.seekText2);
        // 제품 이름 관련
        productNameText2 = findViewById(R.id.productNameText2);

        // 카드뷰 관련
        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);
        card3 = findViewById(R.id.card3);
        card4 = findViewById(R.id.card4);
        card5 = findViewById(R.id.card5);
        card6 = findViewById(R.id.card6);
        card7 = findViewById(R.id.card7);
        card8 = findViewById(R.id.card8);
        card9 = findViewById(R.id.card9);
        card10 = findViewById(R.id.card10);

        CardView[] cardViewReference = {
                this.card1,
                this.card2,
                this.card3,
                this.card4,
                this.card5,
                this.card6,
                this.card7,
                this.card8,
                this.card9,
                this.card10
        };

        float[] nutrientValueReference = {
                nowKcal,
                amount,
                carbohydrate,
                totalFat,
                protein,
                sugars,
                saturatedFat,
                cholesterol,
                sodium,
                transFat
        };
        String[] nutrientNameReference = {
                "칼로리", // 0
                "중량", // 1
                "탄수화물", // 2
                "지방", // 3
                "단백질", // 4
                "당류", // 5
                "포화지방", // 6
                "콜레스테롤", // 7
                "나트륨", // 8
                "트랜스지방" // 9
        };

        for(int i=0; i<10; i++) {

            final int j = i;

            cardViewReference[j].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //dialog (https://jhshjs.tistory.com/59)
                    Dialog dialog1 = new Dialog(context);
                    dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog1.setContentView(R.layout.dialog_change);

                    dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 모서리를 둥글게 하기 위함 (https://jhshjs.tistory.com/60)
                    int x = Resources.getSystem().getDisplayMetrics().widthPixels; //(https://citynetc.tistory.com/208), (https://stackoverflow.com/questions/4743116/get-screen-width-and-height-in-android)
                    int y = Resources.getSystem().getDisplayMetrics().heightPixels;
                    dialog1.getWindow().setLayout((int) (x * 0.7f), (int) (y * 0.3f));

                    Button buttonBack = dialog1.findViewById(R.id.buttonBack);
                    Button buttonNext = dialog1.findViewById(R.id.buttonNext);
                    TextView textTitle = dialog1.findViewById(R.id.textTitle);
                    TextView textContent1 = dialog1.findViewById(R.id.textContent1);
                    EditText textContent3 = dialog1.findViewById(R.id.textContent3);

                    TextView textContent15 = dialog1.findViewById(R.id.textContent15);
                    TextView textContent4 = dialog1.findViewById(R.id.textContent4);

                    if(j==0) {
                        textContent15.setText(" kcal");
                        textContent4.setText(" kcal");
                    } else if (j==1) {
                        textContent15.setText(" g(ml)");
                        textContent4.setText("g(ml)");
                    } else if ((j==7)||(j==8)) {
                        textContent15.setText(" mg");
                        textContent4.setText("mg");
                    } else {
                        textContent15.setText(" g");
                        textContent4.setText("g");
                    }

                    buttonBack.setText("취소");
                    buttonNext.setText("변경");
                    buttonNext.setBackgroundResource(R.drawable.round_shape_purple);
                    //buttonNext.setBackgroundColor(Color.parseColor("#FFBB86FC"));
                    textTitle.setText(nutrientNameReference[j]);
                    textContent1.setText(StringMaker.toMakeItLookGood(nutrientValueReference[j]));


                    if (!SaveToSqlActivity.this.isFinishing()) {
                        dialog1.show();
                        textContent3.setFocusable(true);
                        showSoftKeyboard(textContent3);
                    }


                    buttonBack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideKeyboard(textContent3);
                            textContent3.clearFocus();
                            dialog1.dismiss();
                        }
                    });

                    buttonNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            nutrientValueReference[j] = Float.parseFloat(textContent3.getText().toString());
                            switch (j) {
                                case 0:
                                    nowKcal = nutrientValueReference[j];
                                    break;
                                case 1:
                                    amount = nutrientValueReference[j];
                                    break;
                                case 2:
                                    carbohydrate = nutrientValueReference[j];
                                    break;
                                case 3:
                                    totalFat = nutrientValueReference[j];
                                    break;
                                case 4:
                                    protein = nutrientValueReference[j];
                                    break;
                                case 5:
                                    sugars = nutrientValueReference[j];
                                    break;
                                case 6:
                                    saturatedFat = nutrientValueReference[j];
                                    break;
                                case 7:
                                    cholesterol  = nutrientValueReference[j];
                                    break;
                                case 8:
                                    sodium = nutrientValueReference[j];
                                    break;
                                case 9:
                                    transFat = nutrientValueReference[j];
                                    break;
                            }

                            hideKeyboard(textContent3);
                            textContent3.clearFocus();
                            updateNutrientView(progress);
                            dialog1.dismiss();
                        }
                    });
                }
            });
        }

        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(productNameText2.hasFocus()) {
                    hideKeyboard(productNameText2);
                    productNameText2.clearFocus();

                } else {
                    SaveToSqlActivity.this.finish();
                }
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(productNameText2.getText().toString().equals("") || productNameText2.getText().toString() == null) {
                    //dialog (https://jhshjs.tistory.com/59)
                    Dialog dialog1 = new Dialog(context);
                    dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog1.setContentView(R.layout.dialog_confirm);

                    dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 모서리를 둥글게 하기 위함 (https://jhshjs.tistory.com/60)
                    int x = Resources.getSystem().getDisplayMetrics().widthPixels; //(https://citynetc.tistory.com/208), (https://stackoverflow.com/questions/4743116/get-screen-width-and-height-in-android)
                    int y = Resources.getSystem().getDisplayMetrics().heightPixels;
                    dialog1.getWindow().setLayout((int) (x * 0.7f), (int) (y * 0.3f));

                    Button buttonBack = dialog1.findViewById(R.id.buttonBack);
                    Button buttonNext = dialog1.findViewById(R.id.buttonNext);
                    TextView textTitle = dialog1.findViewById(R.id.textTitle);
                    TextView textContent1 = dialog1.findViewById(R.id.textContent1);

                    buttonBack.setText("아니요");
                    buttonNext.setText("네");
                    buttonNext.setBackgroundResource(R.drawable.round_shape_purple);
                    //buttonNext.setBackgroundColor(Color.parseColor("#FFBB86FC"));
                    textTitle.setText("이름 설정");
                    textContent1.setText("이름 없이 저장할까요?");




                    if(!SaveToSqlActivity.this.isFinishing()) {
                        dialog1.show();
                    }


                    buttonBack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog1.dismiss();
                            productNameText2.setFocusable(true);
                            showSoftKeyboard(productNameText2);
                        }
                    });

                    buttonNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveToSqlite();
                            SaveToSqlActivity.this.finish();
                        }
                    });

                } else {
                    saveToSqlite();
                    SaveToSqlActivity.this.finish();
                }


            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });
        // seekbar (참조: https://jaejong.tistory.com/2)
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // onProgressChange - Seekbar 값 변경될때마다 호출
                Log.d(TAG, String.format("onProgressChanged 값 변경 중 : progress [%d] fromUser [%b]", progress, fromUser));
                SaveToSqlActivity.this.progress = progress;
                updateNutrientView(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // onStartTeackingTouch - SeekBar 값 변경위해 첫 눌림에 호출
                Log.d(TAG, String.format("onStartTrackingTouch 값 변경 시작 : progress [%d]", seekBar.getProgress()));
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // onStopTrackingTouch - SeekBar 값 변경 끝나고 드래그 떼면 호출
                Log.d(TAG, String.format("onStopTrackingTouch 값 변경 종료: progress [%d]", seekBar.getProgress()));
            }
        });

    }// End of inflateWidget()



    private void updateNutrientView(int progress) {
        float degree = ((float) progress)/100;

        // ShardPreference 관련 (적어도 이 Activity 내에서는 불변)
        standardKcalView.setText(" / " + StringMaker.toMakeItLookGood(pref.getString("userDayKcal", "")) + " kcal");

        // 시크바 오른쪽에 있는 텍스트뷰 (적어도 이 Activity 내에선느 불변)
        //seekText2.setText(StringMaker.toMakeItLookGood(amount)+"g(ml)");
        seekText2.setText(StringMaker.toMakeItLookGood(amount));

        // 이미 analysisParcelable()에서 저장했던 전체 값을 progress에 맞게 덜어냄
        eatenNowKcal = StringMaker.toMakeItLookGood(nowKcal*degree);
        eatenAmount = StringMaker.toMakeItLookGood(amount*degree);
        eatenCarbohydrate = StringMaker.toMakeItLookGood(carbohydrate*degree);
        eatenTotalFat = StringMaker.toMakeItLookGood(totalFat*degree);
        eatenProtein = StringMaker.toMakeItLookGood(protein*degree);
        eatenSugars = StringMaker.toMakeItLookGood(sugars*degree);
        eatenSaturatedFat = StringMaker.toMakeItLookGood(saturatedFat*degree);
        eatenCholesterol = StringMaker.toMakeItLookGood(cholesterol*degree);
        eatenSodium = StringMaker.toMakeItLookGood(sodium*degree);
        eatenTransFat = StringMaker.toMakeItLookGood(transFat*degree);

        // 절댓값 텍스트뷰에 저장 및 색 표현
        nowKcalView.setText(StringMaker.toMakeItLookGood(eatenNowKcal));
        amountView.setText(StringMaker.toMakeItLookGood(eatenAmount));
        carbohydrateView.setText(StringMaker.toMakeItLookGood(eatenCarbohydrate)+"g");
        totalFatView.setText(StringMaker.toMakeItLookGood(eatenTotalFat)+"g");
        proteinView.setText(StringMaker.toMakeItLookGood(eatenProtein)+"g");
        sugarsView.setText(StringMaker.toMakeItLookGood(eatenSugars)+"g");
        saturatedFatView.setText(StringMaker.toMakeItLookGood(eatenSaturatedFat)+"g");
        cholesterolView.setText(StringMaker.toMakeItLookGood(eatenCholesterol)+"mg");
        sodiumView.setText(StringMaker.toMakeItLookGood(eatenSodium)+"mg");
        transFatView.setText(eatenTransFat);

        // 퍼센트값 텍스트뷰에 저장 및 색 설정
        carbohydratePercentView.setText(toMakeItPercent(eatenCarbohydrate, "탄수화물"));
        Calculator.setTextViewColor(carbohydratePercentView, Float.parseFloat(toMakeItPercent(eatenCarbohydrate, "탄수화물")), getApplicationContext());

        totalFatPercentView.setText(toMakeItPercent(eatenTotalFat, "지방"));
        Calculator.setTextViewColor(totalFatPercentView, Float.parseFloat(toMakeItPercent(eatenTotalFat, "지방")), getApplicationContext());

        proteinPercentView.setText(toMakeItPercent(eatenProtein, "단백질"));
        Calculator.setTextViewColor(proteinPercentView, Float.parseFloat(toMakeItPercent(eatenProtein, "단백질")), getApplicationContext());

        sugarsPercentView.setText(toMakeItPercent(eatenSugars, "당류"));
        Calculator.setTextViewColor(sugarsPercentView, Float.parseFloat(toMakeItPercent(eatenSugars, "당류")), getApplicationContext());

        saturatedFatPercentView.setText(toMakeItPercent(eatenSaturatedFat, "포화지방"));
        Calculator.setTextViewColor(saturatedFatPercentView, Float.parseFloat(toMakeItPercent(eatenSaturatedFat, "포화지방")), getApplicationContext());

        cholesterolPercentView.setText(toMakeItPercent(eatenCholesterol, "콜레스테롤"));
        Calculator.setTextViewColor(cholesterolPercentView, Float.parseFloat(toMakeItPercent(eatenCholesterol, "콜레스테롤")), getApplicationContext());

        sodiumPercentView.setText(toMakeItPercent(eatenSodium, "나트륨"));
        Calculator.setTextViewColor(sodiumPercentView, Float.parseFloat(toMakeItPercent(eatenSodium, "나트륨")), getApplicationContext());

        if(!eatenTransFat.equals("0")) {
            transFatView.setTextColor(ContextCompat.getColor(this, R.color.red));
        }

    }// End of updateNutrientView()

    private String toMakeItPercent(float inputedNotPercentNumber, String kind) {
        float userDayKcal = Float.parseFloat(pref.getString("userDayKcal", ""));
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
    private String toMakeItPercent(String inputedNotPercentNumberString, String kind) {
        float inputedNotPercentNumber = Float.parseFloat(inputedNotPercentNumberString);
        return toMakeItPercent(inputedNotPercentNumber, kind);
    }// End of toMakeItPercent()

    private int decideFirstProgress() {

        float calculatedProgress = ((amountPerServing/amount)*100);

        int roundedCalculatedProgress = Math.round(calculatedProgress);

        return roundedCalculatedProgress;
    }// End of decideFirstProgress()

    /*
    private void setTextViewColor(TextView textView, float percent) {
        if((30<=percent)&&(percent<70)) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.orange1));
        } else if (70<=percent) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.red));
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.black));
        }
    }// End of setTextViewColor
     */

    private void saveToSqlite() {
        try {
            // 데이터베이스 만들기
            SQLiteDatabase database = openOrCreateDatabase("ggu", MODE_PRIVATE, null);

            // 테이블 만들기
            String tableName = StringMaker.makeDateStringToTableName(pref.getString("date_string", ""));
            String[][] columnNameAndType = {
                    {"name","text"},
                    {"total_amount", "REAL"},
                    {"amount_per_serving", "REAL"},
                    {"calories", "REAL"},
                    {"soduim", "REAL"},
                    {"carbohydrate", "REAL"},
                    {"sugars", "REAL"},
                    {"total_fat", "REAL"},
                    {"trans_fat", "REAL"},
                    {"saturated_fat", "REAL"},
                    {"cholesterol", "REAL"},
                    {"protein", "REAL"}};
            SQLiteVendingMachine.createTableIfNotExsist(database, tableName, columnNameAndType);

            // 레코드 넣기
            if(productNameText2.getText().toString().equals("")||productNameText2.getText()==null) {
                productName = "이름 없음";
            } else {
                productName = productNameText2.getText().toString();
            }

            String[][] columnNameAndTypeAndValue = {
                    {"name","text", productName},
                    {"total_amount", "REAL", StringMaker.toMakeItLookGood(eatenAmount)},
                    {"amount_per_serving", "REAL", StringMaker.toMakeItLookGood(amountPerServing)},
                    {"calories", "REAL", StringMaker.toMakeItLookGood(eatenNowKcal)},
                    {"soduim", "REAL", StringMaker.toMakeItLookGood(eatenSodium)},
                    {"carbohydrate", "REAL", StringMaker.toMakeItLookGood(eatenCarbohydrate)},
                    {"sugars", "REAL", StringMaker.toMakeItLookGood(eatenSugars)},
                    {"total_fat", "REAL", StringMaker.toMakeItLookGood(eatenTotalFat)},
                    {"trans_fat", "REAL", StringMaker.toMakeItLookGood(eatenTransFat)},
                    {"saturated_fat", "REAL", StringMaker.toMakeItLookGood(eatenSaturatedFat)},
                    {"cholesterol", "REAL", StringMaker.toMakeItLookGood(eatenCholesterol)},
                    {"protein", "REAL", StringMaker.toMakeItLookGood(eatenProtein)}};
            SQLiteVendingMachine.addRecord(database, tableName, columnNameAndTypeAndValue);

            // 데이터베이스 닫기
            database.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }// End of saveToSqlite()

    private String attachSingleQuotationMarks(String original) {
        String attached = "\'" + original +"\'";

        return attached;
    }// End of attachSingleQuotationMarks()

    public void showSoftKeyboard(View view) { //https://developer.android.com/training/keyboard-input/visibility, ★☆ https://gogorchg.tistory.com/entry/Android-Fragment-%EC%97%90%EC%84%9C-showSoftInput%EC%9D%B4-%EC%95%88%EB%A8%B9%ED%9E%90-%EB%95%8C ★☆

        view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (view.requestFocus()) { // = view에 focus를 넣는 시도가 성공하면
                        InputMethodManager imm = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            },100);
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);

        if (view.hasFocus()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); // ★☆ https://stackoverflow.com/questions/21573586/hidesoftinputfromwindow-not-working ★☆, https://stackoverflow.com/questions/19069448/null-pointer-error-with-hidesoftinputfromwindow
        }
    }


}// End of Class
