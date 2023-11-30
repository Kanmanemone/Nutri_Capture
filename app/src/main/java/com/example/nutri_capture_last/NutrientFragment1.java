package com.example.nutri_capture_last;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class NutrientFragment1 extends Fragment {
    View nutrientFragment_v;

    // SharedPreferences 객체 및 에디터 생성
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    // 위젯 이름
    /*
    TextView nowKcal, standardKcal, number, amount, carbohydrate,
            carbohydratePercent, totalFat, totalFatPercent, protein, proteinPercent,
            sugars, sugarsPercent, saturatedFat, saturatedFatPercent, cholesterol,
            cholesterolPercent, sodium, sodiumPercent, transFat;
     */
    TextView textEmpty;
    Button buttonReset, buttonAdd;

    // 내부 계산에 쓸 값
    /*
    float amountValue, nowKcalValue, sodiumValue, carbohydrateValue, sugarsValue,
            totalFatValue, transFatValue, saturatedFatValue, cholesterolValue, proteinValue;
     */

    long itemCount = 0;

    // 리사이클러뷰
    NutrientItemAdapter adapter;
    RecyclerView recyclerView;
    ItemTouchHelper helper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //(SharedPreferences) 객체와 에디터 연결
        pref = this.getActivity().getSharedPreferences("Pref", MODE_PRIVATE);
        editor = pref.edit();


    }//End of onCreate()

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        nutrientFragment_v = inflater.inflate(R.layout.fragment_nutrient_1, container, false);

        //리사이클러뷰 어댑터
        inflateAdapter();
        // 위젯 인플레이트 및 버튼 클릭 리스너 설정
        inflateWidget();

        updateRecyclerView();
        updateArrayList();

        return nutrientFragment_v;
    }// End of onCreateView()

    @Override
    public void onStart() {
        super.onStart();

        // MainActivity의 툴바 속 TextView의 Text 변경 등등 ...
        ((MainActivity) MainActivity.context_main).workAfterReplaceFragmentTo(this);

        // SharedPreference에서 불러온 날짜에 맞추어 영양 정보 갱신
        updateRecyclerView();
        updateArrayList();

    }//End of onStart

    public void inflateAdapter() {
        adapter = new NutrientItemAdapter();
        adapter.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(NutrientItemAdapter.myViewHolder holder, View view, int position) {
                editItem(position);
            }

            @Override
            public void onItemDeleteClick(NutrientItemAdapter.myViewHolder holder, View view, int position) {
                if(position != RecyclerView.NO_POSITION) {
                    ArrayList<ProductInfomation> tmpArrayList = adapter.getItemArrayList();
                    tmpArrayList.remove(position);

                    adapter.setItemArrayList(tmpArrayList);

                    /*SQL*/ setDataToSQL(adapter.getItemArrayList()); // 여기서 테이블에 record없으면 테이블 삭제시켜버림

                    updateRecyclerView();
                    //updateArrayList();
                    adapter.notifyItemRemoved(position);

                }
            }
        });
    }

    private void editItem(int position) {
        Dialog dialog1 = new productDialog(getContext(), position);
        dialog1.show();
    }

    public void inflateWidget() {
        // 인플레이트
        // nowKcal = nutrientFragment_v.findViewById(R.id.text0111);
        // standardKcal = nutrientFragment_v.findViewById(R.id.text0112);
        // number = nutrientFragment_v.findViewById(R.id.text021);
        // amount = nutrientFragment_v.findViewById(R.id.text0221);
        // carbohydrate = nutrientFragment_v.findViewById(R.id.text031);
        // carbohydratePercent = nutrientFragment_v.findViewById(R.id.text0321);
        // totalFat = nutrientFragment_v.findViewById(R.id.text041);
        // totalFatPercent = nutrientFragment_v.findViewById(R.id.text0421);
        // protein = nutrientFragment_v.findViewById(R.id.text051);
        // proteinPercent = nutrientFragment_v.findViewById(R.id.text0521);
        // sugars = nutrientFragment_v.findViewById(R.id.text061);
        // sugarsPercent = nutrientFragment_v.findViewById(R.id.text0621);
        // saturatedFat = nutrientFragment_v.findViewById(R.id.text071);
        // saturatedFatPercent = nutrientFragment_v.findViewById(R.id.text0721);
        // cholesterol = nutrientFragment_v.findViewById(R.id.text081);
        // cholesterolPercent = nutrientFragment_v.findViewById(R.id.text0821);
        // sodium = nutrientFragment_v.findViewById(R.id.text091);
        // sodiumPercent = nutrientFragment_v.findViewById(R.id.text0921);
        // transFat = nutrientFragment_v.findViewById(R.id.text1021);
        buttonAdd = nutrientFragment_v.findViewById(R.id.buttonAdd);
        buttonReset = nutrientFragment_v.findViewById(R.id.buttonReset);
        textEmpty = nutrientFragment_v.findViewById(R.id.textEmpty);

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
                    dialog1.getWindow().setLayout((int) (x * 0.7f), (int) (y * 0.3f));

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

                            dialog1.dismiss();

                            // View 업데이트
                            itemCount = 0;
                            updateRecyclerView();
                            updateArrayList();

                            Toast.makeText(getContext(), "초기화 완료", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        });

        TextView modeText3 = nutrientFragment_v.findViewById(R.id.modeText3);
        modeText3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("nutrientViewMode", 2);
                editor.apply();
                ((MainActivity) MainActivity.context_main).changeFragmentTo("nutrientFragment2");
            }
        });

        // 리사이클러뷰 관련
        recyclerView = nutrientFragment_v.findViewById(R.id.recycler1);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);

        OnItemTouchHelperListener touchHelperListener = new OnItemTouchHelperListener() {

            @Override
            public boolean onItemMove(int from_position, int to_position) {
                ArrayList<ProductInfomation> tmpArrayList = adapter.getItemArrayList();

                //이동할 객체 저장
                ProductInfomation tmpItem = tmpArrayList.get(from_position);
                //이동할 객체 삭제
                tmpArrayList.remove(from_position);
                //이동하고 싶은 position에 추가
                tmpArrayList.add(to_position, tmpItem);

                //Adapter에 데이터 이동알림
                adapter.notifyItemMoved(from_position, to_position);

                adapter.setItemArrayList(tmpArrayList);
                setDataToSQL(tmpArrayList);

                return true;
            }

            @Override
            public void onItemSwipe(int position) {
                ArrayList<ProductInfomation> tmpArrayList = adapter.getItemArrayList();

                tmpArrayList.remove(position);
                adapter.notifyItemRemoved(position);

                adapter.setItemArrayList(tmpArrayList);
                setDataToSQL(tmpArrayList);
            }
        };// End of OnItemTouchHelperListener touchHelperListener = ...

        helper = new ItemTouchHelper(new ItemTouchHelperCallback(touchHelperListener));
        helper.attachToRecyclerView(recyclerView);

    }// End of inflateWidget()

    public void updateRecyclerView() {
        // SQLite 관련
        // 데이터베이스 열기
        SQLiteDatabase database = getActivity().openOrCreateDatabase("ggu", MODE_PRIVATE, null);

        // 테이블 이름으로 조회해보고, 없으면 View에 죄다 0 넣고 갱신
        String tableName = StringMaker.makeDateStringToTableName(StringMaker.returnDateStringInSharedPreference());
        Boolean tableExsist = SQLiteVendingMachine.isTableExsist(database, tableName);
        if (tableExsist == false) {

            textEmpty.setVisibility(View.VISIBLE);
            itemCount = 0;

        } else { // if(tableExsist == true)

            textEmpty.setVisibility(View.INVISIBLE);
            // 횟수(number) 관련 = table 속 record의 갯수
            itemCount = DatabaseUtils.queryNumEntries(database, tableName);
        }
        database.close();
    }

    public void updateArrayList() {
        ArrayList<ProductInfomation> tmpArrayList = SQLiteVendingMachine.pullArrayListBodilyFromTable(getActivity(), StringMaker.makeDateStringToTableName(StringMaker.returnDateStringInSharedPreference()), "ggu");
        adapter.setItemArrayList(tmpArrayList);
        recyclerView.setAdapter(adapter);
    }



    public void setDataToSQL(ArrayList<ProductInfomation> arrayList) {
        SQLiteVendingMachine.pushArrayListBodilyToTable(getActivity(), arrayList, StringMaker.makeDateStringToTableName(StringMaker.returnDateStringInSharedPreference()), "ggu");
    }// End of setDataToSQL()


    public void showSoftKeyboard(View view) { //https://developer.android.com/training/keyboard-input/visibility, ★☆ https://gogorchg.tistory.com/entry/Android-Fragment-%EC%97%90%EC%84%9C-showSoftInput%EC%9D%B4-%EC%95%88%EB%A8%B9%ED%9E%90-%EB%95%8C ★☆

        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (view.requestFocus()) { // = view에 focus를 넣는 시도가 성공하면
                    InputMethodManager imm = (InputMethodManager)
                            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        },100);
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        if (view.hasFocus()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); // ★☆ https://stackoverflow.com/questions/21573586/hidesoftinputfromwindow-not-working ★☆, https://stackoverflow.com/questions/19069448/null-pointer-error-with-hidesoftinputfromwindow
        }
    }

    class productDialog extends Dialog {
        // 1
        Button buttonBack;
        Button buttonNext;
        Button buttonDelete;

        // 2
        EditText textTitle;
        TextView statusText;
        EditText text0000101; // 칼로리
        EditText edit0000301; // 중량

        // 3 (Inflate 방식 출처: https://lopicit.tistory.com/179)
        EditText[] nutriEdit = new EditText[8]; // 탄수화물 ~ 트랜스지방의 EditText 8개
        TextView[][] nutriText = new TextView[8][3]; // 8 x 3 개, 8 x (상태("● "), 텍스트("탄수화물"), 퍼센트(%) 값)

        public productDialog(@NonNull Context context, int position) {
            super(context);

            ProductInfomation orinalProductInformation = adapter.getItem(position);
            //ProductInfomation myProductInformation = adapter.getItem(position);
            ProductInfomation copyOfProductInformation = new ProductInfomation
                    (
                            orinalProductInformation.getDate(), //1
                            orinalProductInformation.getName(), //2
                            orinalProductInformation.getTotalAmount(), //3
                            orinalProductInformation.getAmountPerServing(), //4
                            orinalProductInformation.getMaxServingNumber(), //5
                            orinalProductInformation.getEatenServingNumber(), //6
                            orinalProductInformation.getCalories(), //7
                            orinalProductInformation.getSoduim(),  //8
                            orinalProductInformation.getCarbohydrate(), //9
                            orinalProductInformation.getSugars(), //10
                            orinalProductInformation.getTotalFat(), //11
                            orinalProductInformation.getTransFat(), //12
                            orinalProductInformation.getSaturatedFat(), //13
                            orinalProductInformation.getCholesterol(), //14
                            orinalProductInformation.getProtein() //15
                    );

            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.setContentView(R.layout.dialog_product);
            this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // 모서리를 둥글게 하기 위함 (https://jhshjs.tistory.com/60)
            int x = Resources.getSystem().getDisplayMetrics().widthPixels; //(https://citynetc.tistory.com/208), (https://stackoverflow.com/questions/4743116/get-screen-width-and-height-in-android)
            int y = Resources.getSystem().getDisplayMetrics().heightPixels;
            this.getWindow().setLayout((int) (x * 0.9f), (int) (y * 0.7f));

            // 1: 위젯 인플레이트 - 버튼
            buttonBack = this.findViewById(R.id.buttonBack);
            buttonBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            buttonNext = this.findViewById(R.id.buttonNext);
            buttonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // (Android add/replace Items within RecyclerView) https://stackoverflow.com/questions/33411903/android-add-replace-items-within-recyclerview

                    ArrayList<ProductInfomation> tmpArrayList = adapter.getItemArrayList();
                    tmpArrayList.set(position, copyOfProductInformation);
                    adapter.setItemArrayList(tmpArrayList);
                    /*SQL*/ setDataToSQL(adapter.getItemArrayList()); // 여기서 테이블에 record없으면 테이블 삭제시켜버림
                    updateRecyclerView();
                    adapter.notifyItemChanged(position);

                    // 값들 아이템 저장
                    // adapter에 값 replace해주고
                    // push to SQLite
                    // NutrientFragment1를 onStart? 아무튼 업데이트
                    dismiss();
                    // notify()는 안 해줘도 됨. 애니메이션 필요 없는 작업임.
                }
            });
            buttonDelete = this.findViewById(R.id.buttonDelete);
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(position != RecyclerView.NO_POSITION) {
                        ArrayList<ProductInfomation> tmpArrayList = adapter.getItemArrayList();
                        tmpArrayList.remove(position);

                        adapter.setItemArrayList(tmpArrayList);

                        /*SQL*/ setDataToSQL(adapter.getItemArrayList()); // 여기서 테이블에 record없으면 테이블 삭제시켜버림

                        updateRecyclerView();
                        //updateArrayList();
                        adapter.notifyItemRemoved(position);

                    }
                    dismiss();
                }
            });

            // 2: 위젯 인플레이트  - 기타 정보 (이름, 칼로리, 중량, ...)

            statusText = this.findViewById(R.id.statusText);
            textTitle = this.findViewById(R.id.textTitle);
            text0000101 = this.findViewById(R.id.text0000101);
            edit0000301 = this.findViewById(R.id.edit0000301);

            // 3: 위젯 인플레이트 - 핵심 영양 정보 (탄, 단, 지, ...)
            int[] nutriEdit_id = {
                        R.id.edit0010301,
                        R.id.edit0020301,
                        R.id.edit0030301,
                        R.id.edit0040301,
                        R.id.edit0050301,
                        R.id.edit0060301,
                        R.id.edit0070301,
                        R.id.edit0080301,
                    };
            int[][] nutriText_id = {
                    {R.id.text0010101, R.id.text0010102, R.id.text0010201}, // 상태("● "), 텍스트("탄수화물"), 퍼센트(%) 값 순
                    {R.id.text0020101, R.id.text0020102, R.id.text0020201},
                    {R.id.text0030101, R.id.text0030102, R.id.text0030201},
                    {R.id.text0040101, R.id.text0040102, R.id.text0040201},
                    {R.id.text0050101, R.id.text0050102, R.id.text0050201},
                    {R.id.text0060101, R.id.text0060102, R.id.text0060201},
                    {R.id.text0070101, R.id.text0070102, R.id.text0070201},
                    {R.id.text0080101, R.id.text0080102, R.id.text0080201},

                };
            /**
             * nutriEdit[index1]: 사용자가 수정 가능
             * nutriText[index1][index2]: nutriEdit[index1]에 종속
             * nutriText[index1][0]: 상태("● ")
             * nutriText[index1][1]: 텍스트("탄수화물")
             * nutriText[index1][2]: 퍼센트(%) 값
             */
            for(int i=0; i<8 ;i++) {
                final int index1 = i;
                nutriEdit[index1] = (EditText) findViewById(nutriEdit_id[index1]);

                for(int j=0; j<3; j++) {
                    final int index2 = j;
                    nutriText[index1][index2] = (TextView) findViewById(nutriText_id[index1][index2]);
                }
            }

            // OnFocusChangeListener() && textChangedListener 설정

            textTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        String string = textTitle.getText().toString();
                        if(string.equals("이름 없음")) {
                            textTitle.setText("");
                        }
                    } else {
                        String string = textTitle.getText().toString();
                        if(string.equals("") || (string==null)) {
                            textTitle.setText(orinalProductInformation.getName().toString());
                        }
                    }
                }
            });
            textTitle.addTextChangedListener(new TextWatcher() { // editText.addTextChangedListener(new TextWatcher() {...}): https://kanzler.tistory.com/54
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // 텍스트가 입력하기 전에 Call back

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 텍스트가 변경될 때마다 Call back
                    String string = s.toString();

                    if(string.equals("") || (string==null)) {
                        string = orinalProductInformation.getName().toString();
                    }

                    copyOfProductInformation.setName(string);

                    textTitle.getBackground().clearColorFilter();


                    if(theseTwoProductInformationIsSame(copyOfProductInformation, orinalProductInformation)) {
                        buttonNext.setClickable(false);
                        //buttonNext.setForeground(null);
                        buttonNext.setText("변경사항 없음");
                        buttonNext.setBackgroundResource(R.drawable.round_shape_grey);
                    } else {
                        buttonNext.setClickable(true);
                        //buttonNext.setForeground(); // android:foreground="?attr/selectableItemBackground"
                        buttonNext.setText("저장");
                        buttonNext.setBackgroundResource(R.drawable.round_shape_purple);
                    }

                    setStatusColor(copyOfProductInformation, statusText);
                }

                @Override
                public void afterTextChanged(Editable s) { // https://stackoverflow.com/questions/58189222/how-to-use-textwatcher-with-query-in-android
                    // 텍스트 입력이 모두 끝났을때 Call back


                }
            });

            text0000101.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        String string = text0000101.getText().toString();
                        if(string.equals("0")) {
                            text0000101.setText("");
                        }
                    } else {
                        String string = text0000101.getText().toString();
                        if((!StringMaker.isRationalNumberExceptNegative(string))) {
                            text0000101.setText("0");
                        }
                    }
                }
            });
            text0000101.addTextChangedListener(new TextWatcher() { // editText.addTextChangedListener(new TextWatcher() {...}): https://kanzler.tistory.com/54
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // 텍스트가 입력하기 전에 Call back

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 텍스트가 변경될 때마다 Call back
                    String string = s.toString();

                    if(StringMaker.isRationalNumberExceptNegative(string) || string.equals("")) {
                        if(string.equals("")) {
                            string = "0";
                        }

                        copyOfProductInformation.setCalories(Float.parseFloat(string));

                        text0000101.getBackground().clearColorFilter();


                        if(theseTwoProductInformationIsSame(copyOfProductInformation, orinalProductInformation)) {
                            buttonNext.setClickable(false);
                            //buttonNext.setForeground(null);
                            buttonNext.setText("변경사항 없음");
                            buttonNext.setBackgroundResource(R.drawable.round_shape_grey);
                        } else {
                            buttonNext.setClickable(true);
                            //buttonNext.setForeground(); // android:foreground="?attr/selectableItemBackground"
                            buttonNext.setText("저장");
                            buttonNext.setBackgroundResource(R.drawable.round_shape_purple);
                        }

                        setStatusColor(copyOfProductInformation, statusText);

                    } else {
                        text0000101.getBackground().setColorFilter(new BlendModeColorFilter(ContextCompat.getColor(context, R.color.red), BlendMode.SRC_ATOP));
                        // ㄴ (setColorFilter is deprecated on API29) https://stackoverflow.com/questions/56716093/setcolorfilter-is-deprecated-on-api29
                        // ㄴ (How can I get color-int from color resource?) https://stackoverflow.com/questions/5271387/how-can-i-get-color-int-from-color-resource
                    }
                }

                @Override
                public void afterTextChanged(Editable s) { // https://stackoverflow.com/questions/58189222/how-to-use-textwatcher-with-query-in-android
                    // 텍스트 입력이 모두 끝났을때 Call back

                }
            });

            edit0000301.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        String string = edit0000301.getText().toString();
                        if(string.equals("0")) {
                            edit0000301.setText("");
                        }
                    } else {
                        String string = edit0000301.getText().toString();
                        if((!StringMaker.isRationalNumberExceptNegative(string))) {
                            edit0000301.setText("0");
                        }
                    }
                }
            });

            edit0000301.addTextChangedListener(new TextWatcher() { // editText.addTextChangedListener(new TextWatcher() {...}): https://kanzler.tistory.com/54
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // 텍스트가 입력하기 전에 Call back

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 텍스트가 변경될 때마다 Call back
                    String string = s.toString();

                    if(StringMaker.isRationalNumberExceptNegative(string) || string.equals("")) {
                        if(string.equals("")) {
                            string = "0";
                        }

                        copyOfProductInformation.setTotalAmount(Float.parseFloat(string));

                        edit0000301.getBackground().clearColorFilter();


                        if(theseTwoProductInformationIsSame(copyOfProductInformation, orinalProductInformation)) {
                            buttonNext.setClickable(false);
                            //buttonNext.setForeground(null);
                            buttonNext.setText("변경사항 없음");
                            buttonNext.setBackgroundResource(R.drawable.round_shape_grey);
                        } else {
                            buttonNext.setClickable(true);
                            //buttonNext.setForeground(); // android:foreground="?attr/selectableItemBackground"
                            buttonNext.setText("저장");
                            buttonNext.setBackgroundResource(R.drawable.round_shape_purple);
                        }

                        setStatusColor(copyOfProductInformation, statusText);

                    } else {
                        edit0000301.getBackground().setColorFilter(new BlendModeColorFilter(ContextCompat.getColor(context, R.color.red), BlendMode.SRC_ATOP));
                        // ㄴ (setColorFilter is deprecated on API29) https://stackoverflow.com/questions/56716093/setcolorfilter-is-deprecated-on-api29
                        // ㄴ (How can I get color-int from color resource?) https://stackoverflow.com/questions/5271387/how-can-i-get-color-int-from-color-resource
                    }

                }

                @Override
                public void afterTextChanged(Editable s) { // https://stackoverflow.com/questions/58189222/how-to-use-textwatcher-with-query-in-android
                    // 텍스트 입력이 모두 끝났을때 Call back

                }
            });


            for(int i=0; i<8; i++) {
                final int index = i;

                nutriEdit[index].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (hasFocus) {
                            String string = nutriEdit[index].getText().toString();
                            if(string.equals("0")) {
                                nutriEdit[index].setText("");
                            }
                        } else {
                            String string = nutriEdit[index].getText().toString();
                            if((!StringMaker.isRationalNumberExceptNegative(string))) {
                                nutriEdit[index].setText("0");
                            }
                        }
                    }
                });


                nutriEdit[index].addTextChangedListener(new TextWatcher() { // editText.addTextChangedListener(new TextWatcher() {...}): https://kanzler.tistory.com/54
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // 텍스트가 입력하기 전에 Call back

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // 텍스트가 변경될 때마다 Call back

                        String string = s.toString();
                        if(StringMaker.isRationalNumberExceptNegative(string) || string.equals("")) {
                            if(string.equals("")) {
                                string = "0";
                            }
                            nutriEdit[index].getBackground().clearColorFilter();

                            String percent = null;
                            switch (index) {
                                case 0:
                                    copyOfProductInformation.setCarbohydrate(Float.parseFloat(string));
                                    percent = Calculator.toMakeItPercent(string, "탄수화물");
                                    //myProductInformation.setCarbohydrate(Float.parseFloat((nutriEdit[index].getText().toString())));
                                    //percent = Calculator.toMakeItPercent(nutriEdit[index].getText().toString(), "탄수화물");
                                    break;
                                case 1:
                                    copyOfProductInformation.setTotalFat(Float.parseFloat(string));
                                    percent = Calculator.toMakeItPercent(string, "지방");
                                    //myProductInformation.setTotalFat(Float.parseFloat((nutriEdit[index].getText().toString())));
                                    //percent = Calculator.toMakeItPercent(nutriEdit[index].getText().toString(), "지방");
                                    break;
                                case 2:
                                    copyOfProductInformation.setProtein(Float.parseFloat(string));
                                    percent = Calculator.toMakeItPercent(string, "단백질");
                                    //myProductInformation.setProtein(Float.parseFloat((nutriEdit[index].getText().toString())));
                                    //percent = Calculator.toMakeItPercent(nutriEdit[index].getText().toString(), "단백질");
                                    break;
                                case 3:
                                    copyOfProductInformation.setSugars(Float.parseFloat(string));
                                    percent = Calculator.toMakeItPercent(string, "당류");
                                    //myProductInformation.setSugars(Float.parseFloat((nutriEdit[index].getText().toString())));
                                    //percent = Calculator.toMakeItPercent(nutriEdit[index].getText().toString(), "당류");
                                    break;
                                case 4:
                                    copyOfProductInformation.setSaturatedFat(Float.parseFloat(string));
                                    percent = Calculator.toMakeItPercent(string, "포화지방");
                                    //myProductInformation.setSaturatedFat(Float.parseFloat((nutriEdit[index].getText().toString())));
                                    //percent = Calculator.toMakeItPercent(nutriEdit[index].getText().toString(), "포화지방");
                                    break;
                                case 5:
                                    copyOfProductInformation.setCholesterol(Float.parseFloat(string));
                                    percent = Calculator.toMakeItPercent(string, "콜레스테롤");
                                    //myProductInformation.setCholesterol(Float.parseFloat((nutriEdit[index].getText().toString())));
                                    //percent = Calculator.toMakeItPercent(nutriEdit[index].getText().toString(), "콜레스테롤");
                                    break;
                                case 6:
                                    copyOfProductInformation.setSoduim(Float.parseFloat(string));
                                    percent = Calculator.toMakeItPercent(string, "나트륨");
                                    //myProductInformation.setSoduim(Float.parseFloat((nutriEdit[index].getText().toString())));
                                    //percent = Calculator.toMakeItPercent(nutriEdit[index].getText().toString(), "나트륨");
                                    break;
                                case 7:
                                    //percent = Calculator.toMakeItPercent(string, "트랜스지방");
                                    copyOfProductInformation.setTransFat(Float.parseFloat(string));
                                    // percent = Calculator.toMakeItPercent(nutriEdit[index].getText().toString(), "트랜스지방");
                                    if(0<Float.parseFloat(string)) {
                                        percent = "999";
                                    } else {
                                        percent = "0";
                                    }
                                    break;
                                default:
                                    break;
                            }

                            /*디버그용*/ android.util.Log.i("productDialog: index and percent", String.valueOf(index) + ", " +String.valueOf(percent));
                            nutriText[index][2].setText(percent); // 퍼센트(%) 값
                            if (Calculator.getColorByPercent(percent).equals("orange")) {

                                nutriText[index][2].setTextColor(ContextCompat.getColor(context, R.color.orange1));
                                nutriText[index][0].setTextColor(ContextCompat.getColor(context, R.color.orange1)); // 상태("● ")
                                nutriText[index][1].setTextColor(ContextCompat.getColor(context, R.color.black)); // 텍스트("탄수화물")
                            } else if(Calculator.getColorByPercent(percent).equals("red")) {
                                nutriText[index][2].setTextColor(ContextCompat.getColor(context, R.color.red));
                                nutriText[index][0].setTextColor(ContextCompat.getColor(context, R.color.red)); // 상태("● ")
                                nutriText[index][1].setTextColor(ContextCompat.getColor(context, R.color.black)); // 텍스트("탄수화물")
                            } else {
                                nutriText[index][2].setTextColor(ContextCompat.getColor(context, R.color.grey_basic));
                                nutriText[index][0].setTextColor(ContextCompat.getColor(context, R.color.grey_basic)); // 상태("● ")
                                nutriText[index][1].setTextColor(ContextCompat.getColor(context, R.color.grey_basic)); // 텍스트("탄수화물")
                            }


                            /*디버그용*/ android.util.Log.i("theseTwoProductInformationItemDifferent() 직전", "empty msg");
                            if(theseTwoProductInformationIsSame(copyOfProductInformation, orinalProductInformation)) {
                                buttonNext.setClickable(false);
                                //buttonNext.setForeground(null);
                                buttonNext.setText("변경사항 없음");
                                buttonNext.setBackgroundResource(R.drawable.round_shape_grey);
                            } else {
                                buttonNext.setClickable(true);
                                //buttonNext.setForeground(); // android:foreground="?attr/selectableItemBackground"
                                buttonNext.setText("저장");
                                buttonNext.setBackgroundResource(R.drawable.round_shape_purple);
                            }


                            setStatusColor(copyOfProductInformation, statusText);



                        } else {
                            nutriEdit[index].getBackground().setColorFilter(new BlendModeColorFilter(ContextCompat.getColor(context, R.color.red), BlendMode.SRC_ATOP));
                            // ㄴ (setColorFilter is deprecated on API29) https://stackoverflow.com/questions/56716093/setcolorfilter-is-deprecated-on-api29
                            // ㄴ (How can I get color-int from color resource?) https://stackoverflow.com/questions/5271387/how-can-i-get-color-int-from-color-resource
                            // ㄴ nutriEdit[7].getBackground().clearColorFilter();

                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) { // https://stackoverflow.com/questions/58189222/how-to-use-textwatcher-with-query-in-android
                        // 텍스트 입력이 모두 끝났을때 Call back


                    }
                });


            }

            //android:textColor="@android:color/tab_indicator_text" 기본컬러 (https://stackoverflow.com/questions/6468602/what-is-default-color-for-text-in-textview)

            // 초깃값 설정
            textTitle.setText(copyOfProductInformation.getName()); // 제품 이름
            text0000101.setText(StringMaker.toMakeItLookGood(String.valueOf(copyOfProductInformation.getCalories()))); // 칼로리
            edit0000301.setText(StringMaker.toMakeItLookGood(String.valueOf(copyOfProductInformation.getTotalAmount()))); // 중량
            nutriEdit[0].setText(StringMaker.toMakeItLookGood(String.valueOf(copyOfProductInformation.getCarbohydrate())));
            nutriEdit[1].setText(StringMaker.toMakeItLookGood(String.valueOf(copyOfProductInformation.getTotalFat())));
            nutriEdit[2].setText(StringMaker.toMakeItLookGood(String.valueOf(copyOfProductInformation.getProtein())));
            nutriEdit[3].setText(StringMaker.toMakeItLookGood(String.valueOf(copyOfProductInformation.getSugars())));
            nutriEdit[4].setText(StringMaker.toMakeItLookGood(String.valueOf(copyOfProductInformation.getSaturatedFat())));
            nutriEdit[5].setText(StringMaker.toMakeItLookGood(String.valueOf(copyOfProductInformation.getCholesterol())));
            nutriEdit[6].setText(StringMaker.toMakeItLookGood(String.valueOf(copyOfProductInformation.getSoduim())));
            nutriEdit[7].setText(StringMaker.toMakeItLookGood(String.valueOf(copyOfProductInformation.getTransFat())));
            setStatusColor(copyOfProductInformation, statusText);


        }// End of Counstructor

        public boolean theseTwoProductInformationIsSame(ProductInfomation product1, ProductInfomation product2) {
            /*디버그용*/ android.util.Log.i("theseTwoProductInformationItemDifferent() - 시작", "empty msg");

            if((product1.getName().equals(product2.getName()))
                    &&(product1.getDate().equals(product2.getDate()))
                    &&(Float.compare(product1.getTotalAmount(), product2.getTotalAmount()) == 0)
                    &&(Float.compare(product1.getTotalFat(), product2.getTotalFat()) == 0)
                    &&(Float.compare(product1.getCarbohydrate(), product2.getCarbohydrate()) == 0)
                    &&(Float.compare(product1.getProtein(), product2.getProtein()) == 0)
                    &&(Float.compare(product1.getCalories(), product2.getCalories()) == 0)
                    &&(Float.compare(product1.getSugars(), product2.getSugars()) == 0)
                    &&(Float.compare(product1.getSaturatedFat(), product2.getSaturatedFat()) == 0)
                    &&(Float.compare(product1.getCholesterol(), product2.getCholesterol()) == 0)
                    &&(Float.compare(product1.getSoduim(), product2.getSoduim()) == 0)
                    &&(Float.compare(product1.getTransFat(), product2.getTransFat()) == 0))
            {
                return true;
            } else {
                return false;
            }

        }

        public void setStatusColor(ProductInfomation item, TextView statusText) {

            String[] tmp = {
                    Calculator.getColorByPercent(Calculator.toMakeItPercent(item.getCarbohydrate(), "탄수화물")),
                    Calculator.getColorByPercent(Calculator.toMakeItPercent(item.getTotalFat(), "지방")),
                    Calculator.getColorByPercent(Calculator.toMakeItPercent(item.getProtein(), "단백질")),
                    Calculator.getColorByPercent(Calculator.toMakeItPercent(item.getSugars(), "당류")),
                    Calculator.getColorByPercent(Calculator.toMakeItPercent(item.getSaturatedFat(), "포화지방")),
                    Calculator.getColorByPercent(Calculator.toMakeItPercent(item.getCholesterol(), "콜레스테롤")),
                    Calculator.getColorByPercent(Calculator.toMakeItPercent(item.getSoduim(), "나트륨")),
            };

            for(String s : tmp) {
                if(s.equals("red")) {
                    statusText.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    return;
                }
            }

            if(0 < item.getTransFat()) {
                statusText.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                return;
            }

            for(String s : tmp) {
                if(s.equals("orange")) {
                    statusText.setTextColor(ContextCompat.getColor(getContext(), R.color.orange1));
                    return;
                }
            }

            statusText.setTextColor(ContextCompat.getColor(getContext(), R.color.green2));
            return;
        }


    }


}// End of Class

