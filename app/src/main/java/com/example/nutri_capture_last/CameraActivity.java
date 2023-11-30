package com.example.nutri_capture_last;

import static androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CameraActivity extends AppCompatActivity {

    final int maxVerifyNumber = 10;

    // 권한 관련 상수
    private int REQUEST_CODE_PERMISSIONS = 10; // 임의의 숫자이며, 다른 숫자로도 변경될 수 있다.
    // AndroidManifest.xml에 있는 권한들 써 놓은 것 (하드 코딩)
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA","android.permission.WRITE_EXTERNAL_STORAGE"};

    // 위젯 이름
    Button buttonExit;
    Button buttonSkip;
    PreviewView previewView;
    TextRecognizer recognizer;
    Executor executor;
    TextView infoText;

    // 텍스트 저장용 3차원 ArrayList
    final ArrayList<ArrayList<ArrayList<String>>> arrayList1 = new ArrayList<ArrayList<ArrayList<String>>>();
    final ArrayList<ArrayList<String>> arrayList2 = new ArrayList<ArrayList<String>>();
    final ArrayList<String> arrayList3 = new ArrayList<String>();

    // buttonSkip 관련 메소드 skipCapturing()에서 cameraProvider 객체에 접근하기 위해서 레퍼런스 여기다가 전역으로 선언
    ProcessCameraProvider cameraProvider;

    /* <index 정리>
     * 총 내용량: 0
     * 1회 제공량: 1
     * 칼로리: 2
     * 나트륨: 3
     * 탄수화물: 4
     * 당류: 5
     * 지방: 6
     * 트랜스지방: 7
     * 포화지방: 8
     * 콜레스테롤: 9
     * 단백질: 10 */

    // 값 임시 저장
    String[] nutrientTemporarySave = {"", "", "", "", "",
            "", "", "", "", "",
            ""};
    // 값 검증 카운터
    int[] nutrientVerifyCounter = {0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0};
    // 임시 저장된 영양소 값이 검증 완료 후 여기에 저장
    String[] nutrientFixedSave = {"-1", "-1", "-1", "-1", "-1",
            "-1", "-1", "-1", "-1", "-1",
            "-1"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if(allPermissionGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        // 위젯 인플레이트 및 클릭리스너 설정
        inflateWidget();

        // 진행 상황 가시적 표현을 위한 TextView 위젯인 infoText 관련
        showProgress();

    }// End of onCreate

    private void inflateWidget() {
        // 인플레이트
        buttonExit = findViewById(R.id.buttonExit);
        buttonSkip = findViewById(R.id.buttonSkip);
        previewView = findViewById(R.id.previewView);
        infoText = findViewById(R.id.infoText);

        // 클릭 리스너
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.this.finish();
            }
        });

        buttonSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipCapturing();
            }
        });

    }// End of inflateWidget()

    private boolean allPermissionGranted() {
        // 필요한 권한이 부여되었는지 확인
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }// End of allPermissionGranted()

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 권한 허용 또는 거부 여하에 따라 '카메라 시작' 또는 '앱 종료'
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "카메라 사용 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                CameraActivity.this.finish();
            }
        }
    }// End of onRequestPermissionResult()

    private void startCamera() {
        // CameraX 객체 만듦
        ListenableFuture cameraProviderFuture
                = ProcessCameraProvider.getInstance(this);

        //ML Kit 객체 만듦
        recognizer =
                TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());

        // CameraX 객체 설정
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this)); // 컨텍스트와 연결된 기본 스레드에서 대기 중인 작업을 실행할 실행자(Executor)를 반환
    }// End of startCamera()

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        // 프론트에서 표시할 창 Preview를 build
        Preview preview = new Preview.Builder()
                .build();

        // 앱이 실행될 스마트폰의 어떤 카메라를 사용할 것인가?
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        /* Executor Interface: Runnable 객체를 실행하는 Background 프로세스의 틀. 틀 뿐이기에 implements하여 상세 내용 정의.
         * 별도의 Thread를 생성하지 않는다. 그 말인 즉슨, Executor 객체를 호출한 Thread에서 Runnable를 실행한다는 뜻.
         * Executor는 Thread의 cycle을 관리하는 'Thread poool' 객체를 만들 수 있다.
        자세한 설명: https://jaeryo2357.tistory.com/50 */
        executor = Executors.newSingleThreadExecutor();

        //이미지 분석을 위한 객체 생성
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                //.setTargetResolution(new Size(previewView.getWidth(), previewView.getHeight())) ← 이렇게 하니까 글자 인식의 정확도가 굉장히 낮았다. 해상도를 좀 늘릴 필요가 있다. 그래서 풀HD 해상도로 높이겠다. -> 4000x3000로 변경 (갤럭시노트20울트라 카메라 기준)
                .setTargetResolution(new Size(4000, 3000))
                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST) // 설정: 이미지 분석하는 도중 새로운 이미지가 input되면? 하던 작업 계속 한다. 그리고 imageProxy.close()되면 그제서야 비로소 새로운 이미지를 input 해온다.
                .build();

        //이미지 분석 객체의 상세 내용 정의
        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {

            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                // 프레임 하나에 해당하는 이미지
                @SuppressLint("UnsafeOptInUsageError") Image mediaImage = imageProxy.getImage();

                if(mediaImage != null) {
                    // ML Kit에 넣어 줄 수 있게 InputImage로 포맷, 기울기 정보는 덤.
                    InputImage image =
                            InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                    // ML Kit Vision API로 이미지 전달
                    Task<Text> result =
                            recognizer.process(image)
                                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                                        @Override
                                        public void onSuccess(Text visionText) {
                                            // 텍스트 추출 processing이 성공적으로 완료되었을 때
                                            //1
                                            arrayList1.clear();
                                            for (Text.TextBlock block : visionText.getTextBlocks()) {
                                                //2
                                                arrayList2.clear();
                                                for (Text.Line line: block.getLines()) {
                                                    //3
                                                    arrayList3.clear();
                                                    for (Text.Element element: line.getElements()) {
                                                        arrayList3.add(element.getText());
                                                    }// End of 3
                                                    ArrayList arrayList3Instance = (ArrayList) arrayList3.clone();
                                                    arrayList2.add(arrayList3Instance);
                                                }// End of 2
                                                ArrayList arrayList2Instance = (ArrayList) arrayList2.clone();
                                                arrayList1.add(arrayList2Instance);
                                            }// End of 1
                                        }
                                    })// End of addOnSuccessListener()
                                    .addOnFailureListener(
                                            new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // 텍스트 추출 processing에 실패했을 때

                                                }
                                            });
                }// End of if(mediaImage != null)

                try {
                    traversalArrayList(arrayList1);
                } catch (Exception e) {
                    /*디버그용*/ android.util.Log.i("Exception 발생", e.toString());
                    e.printStackTrace();
                    imageProxy.close();
                }// End of try-catch

                /* runOnUiThread() == "run only On Ui Thread, otherwise, it is reserved."
                 *'동기화 이슈' 때문에 UI 작업은 'UI 스레드'에서만 해야 한다. 현재 스레드가 UI스레드면 즉시 실행, 그게 아니면 실행을 유보한다. */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // "1회 제공량" 제외 전부 Counter가 "maxVerifyNumber"이라면 토스트 메시지 띄우기
                        if(nutrientVerifyCounter[0]==maxVerifyNumber &&
                                nutrientVerifyCounter[2] == maxVerifyNumber &&
                                nutrientVerifyCounter[3] == maxVerifyNumber &&
                                nutrientVerifyCounter[4] == maxVerifyNumber &&
                                nutrientVerifyCounter[5] == maxVerifyNumber &&
                                nutrientVerifyCounter[6] == maxVerifyNumber &&
                                nutrientVerifyCounter[7] == maxVerifyNumber &&
                                nutrientVerifyCounter[8] == maxVerifyNumber &&
                                nutrientVerifyCounter[9] == maxVerifyNumber &&
                                nutrientVerifyCounter[10] == maxVerifyNumber) {

                            // cameraProvider 연결 해제
                            cameraProvider.unbindAll();

                            // 이제 액티비티 넘어가자!
                            Toast.makeText(getApplicationContext(), "측정 완료", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getApplicationContext(), SaveToSqlActivity.class);

                            ProductInfomation productInfomation = new ProductInfomation("", "", Float.parseFloat(nutrientFixedSave[0]), Float.parseFloat(nutrientFixedSave[1]), 0,
                                    0, Float.parseFloat(nutrientFixedSave[2]), Float.parseFloat(nutrientFixedSave[3]), Float.parseFloat(nutrientFixedSave[4]),Float.parseFloat(nutrientFixedSave[5]),
                                    Float.parseFloat(nutrientFixedSave[6]),Float.parseFloat(nutrientFixedSave[7]),Float.parseFloat(nutrientFixedSave[8]),Float.parseFloat(nutrientFixedSave[9]),Float.parseFloat(nutrientFixedSave[10]));

                            /*
                            productInfomation.setName("");
                            productInfomation.setDate("");
                            productInfomation.setTotalAmount(Float.parseFloat(nutrientFixedSave[0]));
                            productInfomation.setAmountPerServing(Float.parseFloat(nutrientFixedSave[1]));
                            productInfomation.setMaxServingNumber(0);
                            productInfomation.setEatenServingNumber(0);
                            productInfomation.setCalories(Float.parseFloat(nutrientFixedSave[2]));
                            productInfomation.setSoduim(Float.parseFloat(nutrientFixedSave[3]));
                            productInfomation.setCarbohydrate(Float.parseFloat(nutrientFixedSave[4]));
                            productInfomation.setSugars(Float.parseFloat(nutrientFixedSave[5]));
                            productInfomation.setTotalFat(Float.parseFloat(nutrientFixedSave[6]));
                            productInfomation.setTransFat(Float.parseFloat(nutrientFixedSave[7]));
                            productInfomation.setSaturatedFat(Float.parseFloat(nutrientFixedSave[8]));
                            productInfomation.setCholesterol(Float.parseFloat(nutrientFixedSave[9]));
                            productInfomation.setProtein(Float.parseFloat(nutrientFixedSave[10]));
                            */

                            /*디버그용*/ android.util.Log.i("카메라 액티비티에서 intent에 넣기 직전 값, 그리고 그 오른쪽에는 nutrientFixedSave[]에 있던 값.",
                                    "총 제공량: " + productInfomation.getTotalAmount() +" / "+ nutrientFixedSave[0] +"\n"+
                                            "1회 제공량: " +productInfomation.getAmountPerServing() +" / "+nutrientFixedSave[1]+"\n"+
                                            "열량: " +productInfomation.getCalories() +" / "+nutrientFixedSave[2]+"\n"+
                                            "나트륨: " +productInfomation.getSoduim() +" / "+nutrientFixedSave[3]+"\n"+
                                            "탄수화물: " +productInfomation.getCarbohydrate()+" / "+ nutrientFixedSave[4]+"\n"+
                                            "당류: " +productInfomation.getSugars() +" / "+nutrientFixedSave[5]+"\n"+
                                            "지방: " +productInfomation.getTotalFat() +" / "+nutrientFixedSave[6]+"\n"+
                                            "트랜스지방: " +productInfomation.getTransFat()+" / "+ nutrientFixedSave[7]+"\n"+
                                            "포화지방: " +productInfomation.getSaturatedFat() +" / "+nutrientFixedSave[8]+"\n"+
                                            "콜레스테롤: " +productInfomation.getCholesterol() +" / "+nutrientFixedSave[9]+"\n"+
                                            "단백질: " +productInfomation.getProtein() +" / "+nutrientFixedSave[10]
                            );


                            // 배열 들 싹 비워줌. (액티비티 스택 관리를 못하니 이렇게라도 해서 Error를 줄여야지 뭐...
                            // 세 배열 모두 사이즈가 11이니, max_index는 10
                            for(int i=0; i<=10;i++) {
                                nutrientTemporarySave[i] = "";
                                nutrientVerifyCounter[i] = 0;
                                nutrientFixedSave[i] = "-1";
                            }


                            intent.putExtra("capturedInfo", productInfomation);
                            intent.setFlags(intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);

                            CameraActivity.this.finish();


                        }
                    }//End of run()
                });// End of runOnUiThread()

                imageProxy.close();

            }// End of analyze()
        });// End of imageAnalysis.setAnalyzer()

        // 교착 상태 방지: bindToLifecycle하기 전에 실행 중인 다른 카메라 인스턴스가 없는지 확인 겸 unbindAll()
        cameraProvider.unbindAll();

        /* 직관성을 위한 3번째 매개변수 설명: ProcessCameraProvider를 가져와서 거기에 use case를 붙이는 데, 그 중 하나가 ImageAnalysis다
        자세한 설명: https://blog.potados.com/dev/mlkit-text-recognotion */
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);

        //3배 줌 (AVD 카메라는 줌 지원안하는 건지 적용 안됨. 실제 Device에서는 적용 됨.)
        camera.getCameraControl().setZoomRatio(3.0F);
        /*디버그용*/ android.util.Log.i("기기가 지원하는 최대/최소 배율", String.valueOf(camera.getCameraInfo().getZoomState().getValue()));

        // Preview 객체와 연결. 연결 해제는 preview.setSurfaceProvider(null);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

    }// End of bindPreview()

    private void traversalArrayList(ArrayList<ArrayList<ArrayList<String>>> arrayList1) throws ArrayIndexOutOfBoundsException, ConcurrentModificationException {

        //1
        for(int index_block=0; index_block<arrayList1.size(); index_block++) {
            //2
            for(int index_line=0; index_line < arrayList1.get(index_block).size(); index_line++) {
                //3
                for (int index_element = 0; index_element < arrayList1.get(index_block).get(index_line).size(); index_element++) {

                    chooseKindOfNutrient(index_block, index_line, index_element);

                }// End of 3
            }// End of 2
        }// End of 1
    }// End of traversalArrayList

    private void chooseKindOfNutrient(int index_block, int index_line, int index_element) {
        /* 먼저 내가 원하는 element가 파악(contains())되면, 그 element의 다음 element 및 다다음 element를 가져와서 합(+)친다.
         * +
         * 그리고 index_element+1가 배열의 범위를 벗어나지는 않았는지를 검사한다. */
        int lineSize = arrayList1.get(index_block).get(index_line).size();
        String element1 = arrayList1.get(index_block).get(index_line).get(index_element);

        // [1]우선 총 내용량부터
        if(element1.contains("내용량")) {
            selectNutrientOfAmount(index_block, index_line);
        }
        // [2]그 다음은 칼로리 정보
        if (element1.contains("kcal")) {
            selectNutrientOfCalories(index_block, index_line);
        }
        // [3]마지막으로, 나머지 일반적인 영양소들
        String[] commonNutrientName = {"나트륨", "탄수화물", "당류", "트랜스지방", "포화지방", "콜레스테롤", "단백질"};
        for (String nutrientName : commonNutrientName) {
            if (element1.contains(nutrientName)) {
                selectNutrientOfCommon(index_block, index_line, index_element, lineSize, nutrientName);
            }
        }// End of for()
        // [5] 인식률 개선을 위해서, "지방"을 따로 본다. ("포화지방"이나 "트랜스지방"가 "지방"을 contain하는 바람에 알고리즘 꼬이는 문제 해결을 위함)
        if(2<=element1.length()) {
            String wordFat = element1.substring(0, 2);
            if (wordFat.equals("지방")) {
                selectNutrientOfFat(index_block, index_line, index_element, lineSize);
            }
        }

    }//End of chooseKindOfNutrient

    private void selectNutrientOfAmount (int index_block, int index_line) {
        // selectNutrientOfCommon (<- 더 아래에 있음)의 주석 참조

        // "내용량"이라는 문자열이 속한 line 전체 불러옴
        String lineString = null;
        for(String element:arrayList1.get(index_block).get(index_line)) {
            lineString = lineString + element;
        }

        // 띄어쓰기 제거
        lineString = lineString.replace(" ", "");

        // "총" 있는지 검사 후에, 그 앞부분은 날림(substring())
        if(lineString.contains("총")) {
            lineString = lineString.substring(lineString.indexOf("총"));
        } else {
            return; // "총" 없으면 return;
        }

        // 단위(g, mg, ml) 있는 지 검사 후에, 그 뒷부분은 날림(substring())
        int stringIndex = -1;
        if(lineString.contains("g")) {
            stringIndex = lineString.indexOf("g");
            lineString = lineString.substring(0, stringIndex+1);
        } else if (lineString.contains("mg")) {
            stringIndex = lineString.indexOf("mg");
            lineString = lineString.substring(0, stringIndex+2);
        } else if (lineString.contains("ml")) {
            stringIndex = lineString.indexOf("ml");
            lineString = lineString.substring(0, stringIndex+2);
        } else if (lineString.contains("mL")) {
            stringIndex = lineString.indexOf("mL"); // 보니까, ml를 굳이 m'L'로 쓰는 식품 제조사가 있다. (이디야 커피...)
            lineString = lineString.substring(0, stringIndex+2);
        } else {
            return;
        }


        // 숫자 있는 지 검사 후에, 그 값 추출
        Pattern GET_NUMBER = Pattern.compile("([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)|([0-9]+)");
        Matcher matcher = GET_NUMBER.matcher(lineString);
        Matcher matcherForCheck = GET_NUMBER.matcher(lineString);
        if(matcherForCheck.find()==false) {
            return; // 숫자 없으면 return;
        }
        String onlyNumber = "";
        while (matcher.find()) {
            onlyNumber = onlyNumber + matcher.group();
        }

        /*디버그용*/ android.util.Log.i("총 내용량 & 숫자 부분", lineString + " & " + onlyNumber);

        verifyValue("총 내용량", onlyNumber);

    }//End of selectNutrientOfAmount()

    private void selectNutrientOfCalories (int index_block, int index_line) {
        // selectNutrientOfCommon (<- 더 아래에 있음)의 주석 참조

        // "kcal"이라는 문자열이 속한 line 전체 불러옴
        String lineString = "";
        for(String element:arrayList1.get(index_block).get(index_line)) {
            lineString = lineString + element;
        }

        // 띄어쓰기 제거
        lineString = lineString.replace(" ", "");

        //"1일 영양성분 기준치에 대한 비율(%)은 2,000kcal 기준이므로 개인의 필요 열량에 따라 다를 수 있습니다." 라는 문구를 가져온 것이라면 return;
        if(lineString.contains("기준")) {
            return;
        } else if(lineString.contains("비율")) {
            return;
        } else if(lineString.contains("%")) {
            return;
        }

        /*디버그용*/ android.util.Log.i("\"당\" 검사 전 lineString ", lineString);

        /* "당"을 자꾸 "8"로 인식하는 문제 때문에 추가한 코드이다.
         * g바로 뒤에 있는 "8"이나,
         * g) 바로 뒤에 있는 "8"은,
         * "당"으로 교체한다. */
        if(lineString.contains("g8")) {
            lineString = lineString.replace("g8","g당");
        } else if (lineString.contains("g)8")) {
            lineString = lineString.replace("g)8","g)당");
        }

        // "당"이 없는 경우
        if(lineString.contains("당")==false) {
            /* (case1) "당"이 없으면 패턴 [숫자+"kcal"] 추출
             * <정규표현식 메모> (테스트용 실습 사이트: https://regex101.com/) <- 주의 - 실습 사이트에서는 이스케이프 문자(\)를 한번 쓰는데, 여기 안드로이드 스튜디오에서는 두번(\\) 써줘야 함. 그 외 차이 없음.
             *     연속(1개 이상)된 수로 시작: ^\\d+
             *     "kcal"로 끝남: kcal$
             *     1자리 이상의 숫자로 시작해서 "kcal"로 끝남: ^\\d+kcal$
             * </정규표현식 메모> */
            Pattern case1 = Pattern.compile("\\d+kcal");
            Matcher matcher1 = case1.matcher(lineString);
            if(matcher1.find()==true) {
                lineString = matcher1.group();
            } else {
                return; // 1자리 이상의 숫자로 시작해서 "kcal"로 끝나는 Pattern 없으면 return;
            }

            // 숫자값 추출
            Pattern GET_NUMBER = Pattern.compile("([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)|([0-9]+)");
            Matcher matcher = GET_NUMBER.matcher(lineString);
            String onlyNumber = "";
            while (matcher.find()) {
                onlyNumber = onlyNumber + matcher.group();
            }

            /*디버그용*/ android.util.Log.i("케이스1 - kcal이 속한 line & 숫자 부분", lineString + " & " + onlyNumber);

            verifyValue("칼로리", onlyNumber);

        } else if(lineString.contains("당")==true) {
            /* (case2) "당"이 있으면, '1회 제공량'이 있는 식품이라는 소리 -> '1회 제공량' 얼마인지 추출
             * <정규표현식 메모> (테스트용 실습 사이트: https://regex101.com/) <- 주의 - 실습 사이트에서는 이스케이프 문자(\)를 한번 쓰는데, 여기 안드로이드 스튜디오에서는 두번(\\) 써줘야 함. 그 외 차이 없음.
             *     1자리 이상의 수로 시작: ^\d.*
             *     그 다음으로 "g" 또는 "mg" 또는 "ml" 포함: (g|mg|ml|mL)
             *     그 다음으로 ")" 있을 수도 있고 없을 수도 있음: \\)?
             *     그 다음으로 1자리 이상의 수 포함: \d.*
             *     "kcal"로 끝남: kcal$
             * </정규표현식 메모> */
            Pattern case2 = Pattern.compile("\\d.*(g|mg|ml|mL)당\\)?\\d.*kcal");
            Matcher matcher2 = case2.matcher(lineString);
            if(matcher2.find()==true) {
                lineString = matcher2.group();
            } else {
                return; // case2에 해당하는 Pattern 없으면 return;
            }
            /* 숫자값 추출
             * 여기서 사용한 정규식에 대한 Reference: https://wookim789.tistory.com/61 */
            Pattern GET_NUMBER = Pattern.compile("([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)|([0-9]+)");
            Matcher matcher = GET_NUMBER.matcher(lineString);

            matcher.find();
            String onlyNumber1 = matcher.group();
            matcher.find();
            String onlyNumber2 = matcher.group(); // matcher.find()는 그 return값이 호출 시마다 달라질 수 있다. 즉, onlyNumber1과 onlyNumber2는 정말 우연이 겹치지 않는 이상은 웬만하면 다른 숫자가 저장될 것이다.

            /*디버그용*/ android.util.Log.i("케이스2 - kcal이 속한 line & 숫자 부분", lineString + " & " + onlyNumber1 + ", " + onlyNumber2);

            verifyValue("1회 제공량", onlyNumber1);
            verifyValue("칼로리", onlyNumber2);

        }//End of else if(lineString.contains("당"))

    }//End of selectNutrientOfCalories()

    private void selectNutrientOfCommon (int index_block, int index_line, int index_element, int lineSize, String nutrientName) {
        if((index_element+1)<lineSize) {
            /* 문자열.added_element.replace(" ", "")로 모든 공백을 제거한다. 이 뒤에 적용할 Pattern이 공백까지 고려하지 않게 만들기 위함이다. (참조함: https://www.delftstack.com/ko/howto/java/how-to-remove-whitespace-from-string-in-java/)
             * +
             * 만약, index_element+2 가 line배열의 범위 내라면 이거까지 더해준다. (element3)
             * +
             * 영양소 이름 앞 부분은 전부 버린다 -> (.substring)으로 원하는 부분만 추출하는 식으로 처리 */
            String element1 = arrayList1.get(index_block).get(index_line).get(index_element);
            String element2 = arrayList1.get(index_block).get(index_line).get(index_element + 1);
            String element3 = "";
            if(index_element+2 < lineSize) {
                element3 = arrayList1.get(index_block).get(index_line).get(index_element + 2);
            }
            String added_element = element1 + element2 + element3;
            added_element = added_element.replace(" ", "");
            added_element = added_element.substring(added_element.indexOf(nutrientName));

            /* 단위(g, mg, ml, kcal)가 있는가?
             * +
             * 그리고 그 단위에 대한 '위치 정보'(index)를 받아오고, 단위 뒤에 있는 모든 부분을 날린다. */
            int stringIndex = -1;
            if(added_element.contains("g")) {
                stringIndex = added_element.indexOf("g");
                added_element = added_element.substring(0, stringIndex+1);
            } else if (added_element.contains("mg")) {
                stringIndex = added_element.indexOf("mg");
                added_element = added_element.substring(0, stringIndex+2);
            } else {
                return;
            }

            /* 숫자가 포함되어 있는가?
             * 정규표현식(Regular Expression): 컴퓨터 과학의 정규언어로부터 유래한 것으로 특정한 규칙을 가진 문자열의 집합을 표현하기 위해 쓰이는 형식언어
             * Pattern 클래스는, 사용자가 정의한 정규표현식을 나타내기 위해서 만들어졌다. (여기서는 0, 1, 2, 3, ... , 9까지만 뽑아내는 Pattern 정의함)
             * Pattern 객체를 초기화하는 유일한 방법은 .compile을 쓰는 것이라고 한다.
             * 정규표현식 문법 이해를 위해 참조한 Post들
             *     1: https://j2doll.tistory.com/646
             *     2: https://curryyou.tistory.com/234
             *     3: https://chrisjune-13837.medium.com/%EC%A0%95%EA%B7%9C%EC%8B%9D-%ED%8A%9C%ED%86%A0%EB%A6%AC%EC%96%BC-%EC%98%88%EC%A0%9C%EB%A5%BC-%ED%86%B5%ED%95%9C-cheatsheet-%EB%B2%88%EC%97%AD-61c3099cdca8
             *     4: https://boilerplate.tistory.com/53
             * +
             * Matcher?
             * Matcher matcher_example = pattern_example.macher(문자열)로 객체 생성 및 초기화한다.
             * 'matcher_example'에는, '문자열'에 'pattern_example'라는 렌즈를 투사시켜서 나온 문자열이 들어간다.
             * 즉, String하고 비슷하다. 다만, Matcher는 find(), group(), matches() 등의 차별화된 메소드를 제공한다.
             * 이 차별화된 메소드들은 문자열 parsing 작업을 용이하게 해준다.
             * 문법을 보면, matcher_example에 담기는 문자열은 원본 문자열의 부분 집합일 거 같겠지만, 그게 아니다.
             * 원본 문자열이 통째로 다 들어가는 것이다. 다만, pattern_example에 대한 정보도 문자열과 함께 저장되는 것이다.
             * 그래서 find() 등 pattern과 string 둘 다 알아야 작동 가능한 메소드를 쓸 수 있는 것이다.
             * +
             * find()는 matcher에 담긴 문자열이 matche r에 담긴 패턴과 일치하는 부분을 추출한 부분 문자열을 찾아낸다.
             * 일단, matcher_example.find(); 패턴이 일치하는 부분이 아예 하나도 없다면 false를 반환한다.
             * 그런데 하나 이상 있다면, true를 반환하고, 그 패턴이 있는 위치로 이동한다 (객체에서 내부적인 cursor가 있는 것으로 보임)
             * 반복문을 통해 한번 더 호출하면, 그 다음으로 일치하는 부분 문자열을 찾아낸다.
             * 즉, 같은 메소드를 2번 써도 return값이 다를 수 있다는 이야기다.
             * 예를 들어, matcher_example에 pattern_example과 일치하는 문자열이 3개 있다고 해보자.
             * 그럼 이때 pattern_example.find() 첫 실행 시 true 반환과 함께 내부적인 cursor 이동
             * 두번째 find() 시 true 반환과 함께 내부적인 cursor 이동
             * 세번째 find() 시 true 반환과 함께 내부적인 cursor 이동
             * 4번째 find() 시 false
             * 이를 통해, while(pattern_example.find()) { ... }가 직관적으로 이해할 수 있다.
             * +
             * while문 안에는 find()로 찾은 부분문자열(내부적인 cursor가 가리키는 부분)을 반환하는 group()이 있다. */
            Pattern GET_NUMBER = Pattern.compile("([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)|([0-9]+)");
            Matcher matcher = GET_NUMBER.matcher(added_element);
            // 숫자 없으면 다음 element로
            Matcher matcherForCheck = GET_NUMBER.matcher(added_element);
            if(matcherForCheck.find()==false) {
                return;
            }
            // 숫자 있으면 onlyNumber 문자열에 저장
            String onlyNumber = "";
            while (matcher.find()) {
                onlyNumber = onlyNumber + matcher.group();
            }

            /*디버그용*/ android.util.Log.i("added_element & onlyNumber", added_element + " & " + onlyNumber);

            verifyValue(nutrientName, onlyNumber);

        }
    }// End of selectNutrientOfCommon()

    private void selectNutrientOfFat(int index_block, int index_line, int index_element, int lineSize) {
        if((index_element+1)<lineSize) {
            String element1 = arrayList1.get(index_block).get(index_line).get(index_element);
            String element2 = arrayList1.get(index_block).get(index_line).get(index_element + 1);
            String element3 = "";
            if(index_element+2 < lineSize) {
                element3 = arrayList1.get(index_block).get(index_line).get(index_element + 2);
            }
            String added_element = element1 + element2 + element3;
            added_element = added_element.replace(" ", "");
            // 영양소 앞 부분 버릴 필요 없음 (애초에 영양소 앞 부분이 "지방" 이어야 이 메소드에 들어올 수 있으니까)

            int stringIndex = -1;
            if(added_element.contains("g")) {
                stringIndex = added_element.indexOf("g");
                added_element = added_element.substring(0, stringIndex+1);
            } else if (added_element.contains("mg")) {
                stringIndex = added_element.indexOf("mg");
                added_element = added_element.substring(0, stringIndex+2);
            } else {
                return;
            }

            Pattern GET_NUMBER = Pattern.compile("([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)|([0-9]+)");
            Matcher matcher = GET_NUMBER.matcher(added_element);
            // 숫자 없으면 return;
            Matcher matcherForCheck = GET_NUMBER.matcher(added_element);
            if(matcherForCheck.find()==false) {
                return;
            }
            // 숫자 있으면 onlyNumber 문자열에 저장
            String onlyNumber = "";
            while (matcher.find()) {
                onlyNumber = onlyNumber + matcher.group();
            }

            /*디버그용*/ android.util.Log.i("added_element & onlyNumber", added_element + " & " + onlyNumber);

            verifyValue("지방", onlyNumber);

        }
    }// End of selectNutrientOfFat()

    // 한 번 들어온 값은, 총 10번의 추가적인 확인 과정을 거친 후 SQLite에 저장될 값으로서 인정한다.
    private void verifyValue(String item, String value) {
        String[] itemArray  = {"총 내용량", "1회 제공량", "칼로리", "나트륨", "탄수화물",
                "당류", "지방", "트랜스지방", "포화지방", "콜레스테롤",
                "단백질"};

        int itemArrayIndex = 0;
        for(String elementOfArray : itemArray) {
            if(item.equals(elementOfArray)) {

                /*디버그용*/ android.util.Log.i("뀨뀨", item + ", " + elementOfArray + ", " + String.valueOf(nutrientVerifyCounter[itemArrayIndex]));

                // Counter가 0이면 그냥 넣는다. 그리고 1증가시킴
                if(nutrientVerifyCounter[itemArrayIndex]==0) {
                    nutrientTemporarySave[itemArrayIndex] = value;
                    nutrientVerifyCounter[itemArrayIndex] = nutrientVerifyCounter[itemArrayIndex]+1;
                }
                /* Counter가 1이면 우선, 기존 값과 비교한다.
                 * 기존 값과 다르다면, 넣지 않고 Counter를 0으로 만들고 return; 한다
                 * 기존 값과 같다면, Counter를 2로 증가시킨다. */
                else if(nutrientVerifyCounter[itemArrayIndex]==1) {
                    if(nutrientTemporarySave[itemArrayIndex].equals(value)) {
                        nutrientVerifyCounter[itemArrayIndex] = nutrientVerifyCounter[itemArrayIndex]+1;
                    } else {
                        nutrientVerifyCounter[itemArrayIndex] = 0;
                        return;
                    }
                }
                /* Counter가 2이면 우선, 기존 값과 비교한다.
                 * 기존 값과 다르다면, 넣지 않고 Counter를 0으로 만들고 return; 한다
                 * 기존 값과 같다면, nutrientFixedSave[]에 Counter가 0이었을 때 저장했던 값을 넣는다.
                 * 그리고, Counter를 3으로 증가시킨다. */
                else if( (1<(nutrientVerifyCounter[itemArrayIndex])) && (nutrientVerifyCounter[itemArrayIndex]<=(maxVerifyNumber-1)) ) {
                    if(nutrientTemporarySave[itemArrayIndex].equals(value)) {
                        nutrientFixedSave[itemArrayIndex] = nutrientTemporarySave[itemArrayIndex];
                        nutrientVerifyCounter[itemArrayIndex] = nutrientVerifyCounter[itemArrayIndex]+1;
                    } else {
                        nutrientVerifyCounter[itemArrayIndex] = 0;
                        return;
                    }
                }
                // Counter가 3이면 바로 return; 한다. 이미 검증이 끝난 영양소 정보이기 때문이다.
                else if(nutrientVerifyCounter[itemArrayIndex]==maxVerifyNumber) {
                    showProgress();
                    return;
                }
            }//End of if(item.equals(elementOfArray))

            itemArrayIndex++;

        }//End of for(String elementOfArray : itemArray)
    }//End of verifyValue()

    private void showProgress() {

        final String original = infoText.getText().toString(); // ✓ 총 내용량 ✓ㆍ✓ kcal ✓ㆍ✓ 나트륨 ✓ㆍ✓ 탄수화물 ✓\n✓ 당류 ✓ㆍ✓ 지방 ✓ㆍ✓ 트랜스지방 ✓ㆍ✓ 포화지방 ✓\n✓ 콜레스테롤 ✓ㆍ✓ 단백질 ✓
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(original);

        /* [1] 0~9 ✓ 총 내용량 ✓ㆍ✓ kcal ✓ㆍ✓ 나트륨 ✓ㆍ✓ 탄수화물 ✓\n✓ 당류 ✓ㆍ✓ 지방 ✓ㆍ✓ 트랜스지방 ✓ㆍ✓ 포화지방 ✓\n✓ 콜레스테롤 ✓ㆍ✓ 단백질 ✓
         * [2] 10~18 ✓ kcal ✓ㆍ✓ 나트륨 ✓ㆍ✓ 탄수화물 ✓\n✓ 당류 ✓ㆍ✓ 지방 ✓ㆍ✓ 트랜스지방 ✓ㆍ✓ 포화지방 ✓\n✓ 콜레스테롤 ✓ㆍ✓ 단백질 ✓
         * [3] 19~26 ✓ 나트륨 ✓ㆍ✓ 탄수화물 ✓\n✓ 당류 ✓ㆍ✓ 지방 ✓ㆍ✓ 트랜스지방 ✓ㆍ✓ 포화지방 ✓\n✓ 콜레스테롤 ✓ㆍ✓ 단백질 ✓
         * [4] 27~35 ✓ 탄수화물 ✓\n✓ 당류 ✓ㆍ✓ 지방 ✓ㆍ✓ 트랜스지방 ✓ㆍ✓ 포화지방 ✓\n✓ 콜레스테롤 ✓ㆍ✓ 단백질 ✓
         * [5] 36~42 ✓ 당류 ✓ㆍ✓ 지방 ✓ㆍ✓ 트랜스지방 ✓ㆍ✓ 포화지방 ✓\n✓ 콜레스테롤 ✓ㆍ✓ 단백질 ✓
         * [6] 43~49 ✓ 지방 ✓ㆍ✓ 트랜스지방 ✓ㆍ✓ 포화지방 ✓\n✓ 콜레스테롤 ✓ㆍ✓ 단백질 ✓
         * [7] 50~59 ✓ 트랜스지방 ✓ㆍ✓ 포화지방 ✓\n✓ 콜레스테롤 ✓ㆍ✓ 단백질 ✓
         * [8] 60~68 ✓ 포화지방 ✓\n✓ 콜레스테롤 ✓ㆍ✓ 단백질 ✓
         * [9] 69~78 ✓ 콜레스테롤 ✓ㆍ✓ 단백질 ✓
         * [10] 79~86 ✓ 단백질 ✓ */

        // 총 내용량
        ForegroundColorSpan fcs01 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
        spannableStringBuilder.setSpan(fcs01, 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (nutrientVerifyCounter[0]==maxVerifyNumber) {
            ForegroundColorSpan fcs012 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.green2));
            spannableStringBuilder.setSpan(fcs012, 2, 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            ForegroundColorSpan fcs013 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
            spannableStringBuilder.setSpan(fcs013, 8, 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        // kcal
        ForegroundColorSpan fcs02 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
        spannableStringBuilder.setSpan(fcs02, 10, 11, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (nutrientVerifyCounter[2]==maxVerifyNumber) {
            ForegroundColorSpan fcs012 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.green2));
            spannableStringBuilder.setSpan(fcs012, 12, 18, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            ForegroundColorSpan fcs013 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
            spannableStringBuilder.setSpan(fcs013, 17, 18, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        // 나트륨
        ForegroundColorSpan fcs03 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
        spannableStringBuilder.setSpan(fcs03, 19, 20, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (nutrientVerifyCounter[3]==maxVerifyNumber) {
            ForegroundColorSpan fcs012 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.green2));
            spannableStringBuilder.setSpan(fcs012, 21, 26, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            ForegroundColorSpan fcs013 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
            spannableStringBuilder.setSpan(fcs013, 25, 26, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        // 탄수화물
        ForegroundColorSpan fcs04 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
        spannableStringBuilder.setSpan(fcs04, 27, 28, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (nutrientVerifyCounter[4]==maxVerifyNumber) {
            ForegroundColorSpan fcs012 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.green2));
            spannableStringBuilder.setSpan(fcs012, 29, 35, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            ForegroundColorSpan fcs013 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
            spannableStringBuilder.setSpan(fcs013, 34, 35, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        // 당류
        ForegroundColorSpan fcs05 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
        spannableStringBuilder.setSpan(fcs05, 36, 37, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (nutrientVerifyCounter[5]==maxVerifyNumber) {
            ForegroundColorSpan fcs012 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.green2));
            spannableStringBuilder.setSpan(fcs012, 38, 42, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            ForegroundColorSpan fcs013 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
            spannableStringBuilder.setSpan(fcs013, 41, 42, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        // 지방
        ForegroundColorSpan fcs06 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
        spannableStringBuilder.setSpan(fcs06, 43, 44, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (nutrientVerifyCounter[6]==maxVerifyNumber) {
            ForegroundColorSpan fcs012 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.green2));
            spannableStringBuilder.setSpan(fcs012, 45, 49, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            ForegroundColorSpan fcs013 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
            spannableStringBuilder.setSpan(fcs013, 48, 49, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        // 트랜스지방
        ForegroundColorSpan fcs07 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
        spannableStringBuilder.setSpan(fcs07, 50, 51, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (nutrientVerifyCounter[7]==maxVerifyNumber) {
            ForegroundColorSpan fcs012 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.green2));
            spannableStringBuilder.setSpan(fcs012, 52, 59, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            ForegroundColorSpan fcs013 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
            spannableStringBuilder.setSpan(fcs013, 58, 59, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        // 포화지방
        ForegroundColorSpan fcs08 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
        spannableStringBuilder.setSpan(fcs08, 60, 61, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (nutrientVerifyCounter[8]==maxVerifyNumber) {
            ForegroundColorSpan fcs012 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.green2));
            spannableStringBuilder.setSpan(fcs012, 62, 68, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            ForegroundColorSpan fcs013 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
            spannableStringBuilder.setSpan(fcs013, 67, 68, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        // 콜레스테롤
        ForegroundColorSpan fcs09 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
        spannableStringBuilder.setSpan(fcs09, 69, 70, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (nutrientVerifyCounter[9]==maxVerifyNumber) {
            ForegroundColorSpan fcs012 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.green2));
            spannableStringBuilder.setSpan(fcs012, 71, 78, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            ForegroundColorSpan fcs013 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
            spannableStringBuilder.setSpan(fcs013, 77, 78, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        // 단백질
        ForegroundColorSpan fcs10 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
        spannableStringBuilder.setSpan(fcs10, 79, 80, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (nutrientVerifyCounter[10]==maxVerifyNumber) {
            ForegroundColorSpan fcs012 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.green2));
            spannableStringBuilder.setSpan(fcs012, 81, 86, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            ForegroundColorSpan fcs013 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha));
            spannableStringBuilder.setSpan(fcs013, 85, 86, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        infoText.setText(spannableStringBuilder);

    }// End of showProgress()

    private void skipCapturing() {
        //runOnUiThread() 속 run()의 코드 그대로 복사&붙여넣기 (if문 부분만 바꿈 + error나는 부분은 전부 주석 처리)
        for(int i=0; i<1; i++) {
            if(nutrientFixedSave[i].equals("-1")) {
                nutrientVerifyCounter[i] = maxVerifyNumber;
                nutrientFixedSave[i] = "100";
            }
        }
        // nutrientFixedSave[2]은 건너뛴다.
        for(int i=2; i<nutrientFixedSave.length; i++) {
            if(nutrientFixedSave[i].equals("-1")) {
                nutrientVerifyCounter[i] = maxVerifyNumber;
                nutrientFixedSave[i] = "0";
            }
        }




        // cameraProvider 연결 해제
        //cameraProvider.unbindAll(); // Null 에러가 남. 이유 불명. 따라서 주석 처리. 원본은 주석 처리 없음.

        // 이제 액티비티 넘어가자!
        //Toast.makeText(getApplicationContext(), "측정 완료", Toast.LENGTH_SHORT).show(); // 에러는 없지만, '넘어가기'를 누른 거니까, '측정 완료'라는 메시지가 뜨면 어색하므로 주석 처리

        Intent intent = new Intent(getApplicationContext(), SaveToSqlActivity.class);

        ProductInfomation productInfomation = new ProductInfomation(
                "", //1
                "", //2
                Float.parseFloat(nutrientFixedSave[0]), //3
                Float.parseFloat(nutrientFixedSave[1]), //4
                0, //5
                0, //6
                Float.parseFloat(nutrientFixedSave[2]), //7
                Float.parseFloat(nutrientFixedSave[3]), //8
                Float.parseFloat(nutrientFixedSave[4]), //9
                Float.parseFloat(nutrientFixedSave[5]), //10
                Float.parseFloat(nutrientFixedSave[6]), //11
                Float.parseFloat(nutrientFixedSave[7]), //12
                Float.parseFloat(nutrientFixedSave[8]), //13
                Float.parseFloat(nutrientFixedSave[9]), //14
                Float.parseFloat(nutrientFixedSave[10])); //15


                        //productInfomation.setName("");
                        //productInfomation.setDate("");
                        //productInfomation.setTotalAmount(Float.parseFloat(nutrientFixedSave[0]));
                        //productInfomation.setAmountPerServing(Float.parseFloat(nutrientFixedSave[1]));
                        //productInfomation.setMaxServingNumber(0);
                        //productInfomation.setEatenServingNumber(0);
                        //productInfomation.setCalories(Float.parseFloat(nutrientFixedSave[2]));
                        //productInfomation.setSoduim(Float.parseFloat(nutrientFixedSave[3]));
                        //productInfomation.setCarbohydrate(Float.parseFloat(nutrientFixedSave[4]));
                        //productInfomation.setSugars(Float.parseFloat(nutrientFixedSave[5]));
                        //productInfomation.setTotalFat(Float.parseFloat(nutrientFixedSave[6]));
                        //productInfomation.setTransFat(Float.parseFloat(nutrientFixedSave[7]));
                        //productInfomation.setSaturatedFat(Float.parseFloat(nutrientFixedSave[8]));
                        //productInfomation.setCholesterol(Float.parseFloat(nutrientFixedSave[9]));
                        //productInfomation.setProtein(Float.parseFloat(nutrientFixedSave[10]));


        android.util.Log.i("카메라 액티비티에서 intent에 넣기 직전 값, 그리고 그 오른쪽에는 nutrientFixedSave[]에 있던 값.",
                "총 제공량: " + productInfomation.getTotalAmount() +" / "+ nutrientFixedSave[0] +"\n"+
                        "1회 제공량: " +productInfomation.getAmountPerServing() +" / "+nutrientFixedSave[1]+"\n"+
                        "열량: " +productInfomation.getCalories() +" / "+nutrientFixedSave[2]+"\n"+
                        "나트륨: " +productInfomation.getSoduim() +" / "+nutrientFixedSave[3]+"\n"+
                        "탄수화물: " +productInfomation.getCarbohydrate()+" / "+ nutrientFixedSave[4]+"\n"+
                        "당류: " +productInfomation.getSugars() +" / "+nutrientFixedSave[5]+"\n"+
                        "지방: " +productInfomation.getTotalFat() +" / "+nutrientFixedSave[6]+"\n"+
                        "트랜스지방: " +productInfomation.getTransFat()+" / "+ nutrientFixedSave[7]+"\n"+
                        "포화지방: " +productInfomation.getSaturatedFat() +" / "+nutrientFixedSave[8]+"\n"+
                        "콜레스테롤: " +productInfomation.getCholesterol() +" / "+nutrientFixedSave[9]+"\n"+
                        "단백질: " +productInfomation.getProtein() +" / "+nutrientFixedSave[10]
        );


        // 배열 들 싹 비워줌. (액티비티 스택 관리를 못하니 이렇게라도 해서 Error를 줄여야지 뭐...
        // 세 배열 모두 사이즈가 11이니, max_index는 10
        for(int i=0; i<=10;i++) {
            nutrientTemporarySave[i] = "";
            nutrientVerifyCounter[i] = 0;
            nutrientFixedSave[i] = "-1";
        }


        intent.putExtra("capturedInfo", productInfomation);
        intent.setFlags(intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);

        CameraActivity.this.finish();

    }

}// End of Class
