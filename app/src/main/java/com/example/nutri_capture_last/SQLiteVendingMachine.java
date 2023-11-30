package com.example.nutri_capture_last;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

final public class SQLiteVendingMachine {

    static public void createTableIfNotExsist(SQLiteDatabase database, String tableName, String[][] columnNameAndType) {
        /* <참조: SQL문 예시>
            sql = "CREATE TABLE IF NOT EXISTS" + " " + tableName + " " + "("
                    + "_id integer PRIMARY KEY autoincrement" + "," + " "
                    + "name text" + "," + " "
                    + "total_amount REAL" + "," + " "
                    + "amount_per_serving REAL" + "," + " "
                    + "calories REAL" + "," + " "
                    + "soduim REAL" + "," + " "
                    + "carbohydrate REAL" + "," + " "
                    + "sugars REAL" + "," + " "
                    + "total_fat REAL" + "," + " "
                    + "trans_fat REAL" + "," + " "
                    + "saturated_fat REAL" + "," + " "
                    + "cholesterol REAL" + "," + " "
                    + "protein REAL)";
        */

        String sql= "CREATE TABLE IF NOT EXISTS" + " " + tableName + " " + "("
                + "_id integer PRIMARY KEY autoincrement" + "," + " ";

        String sql2 = "";
        for(String[] str : columnNameAndType) {
            sql2 = sql2 + str[0] + " " + str[1]+ "," + " ";
        }// End of for문

        int lastIndex = sql2.lastIndexOf("," + " ");
        sql2 = sql2.substring(0, lastIndex) + ")";
        sql = sql + sql2;

        database.execSQL(sql);

    }// End of openOrCreateTable()

    static public Boolean isTableExsist(SQLiteDatabase database, String tableName) {
        /* 테이블 존재 유무 확인 1: https://hashcode.co.kr/questions/1438/sqlite%EC%97%90%EC%84%9C-%ED%95%B4%EB%8B%B9-%ED%85%8C%EC%9D%B4%EB%B8%94%EC%9D%B4-%EC%A1%B4%EC%9E%AC%ED%95%98%EB%8A%94%EC%A7%80-%ED%99%95%EC%9D%B8%EC%9D%80-%EC%96%B4%EB%96%BB%EA%B2%8C%ED%95%98%EB%82%98%EC%9A%94
         * 테이블 존재 유무 확인 2: https://onlyican.tistory.com/218 */

        String tableName2 = "\'" +tableName +"\'";
        String sqlExsist = "SELECT NAME FROM sqlite_master WHERE" + " " + "type='table' AND NAME=" + tableName2;
        Cursor cursor = database.rawQuery(sqlExsist, null);

        if(cursor.getCount()==0) {
            cursor.close();
            return false;
        } else {
            cursor.close();
            return true;
        }

    }// End of isTableExsist()

    static public void dropTable(SQLiteDatabase database, String tableName) {
        if(isTableExsist(database, tableName) == true) {
            database.execSQL("DROP TABLE" + " " + tableName);
        } else {
            return;
        }
    }


    static public void addRecord(SQLiteDatabase database, String tableName, String[][] selectedColumnNameAndTypeAndValue) {
        /* <참조: SQL문 예시>
        String product_name = "";
        String sqlRecord = "INSERT INTO" + " " + tableName + " "
                + "(name, total_amount, amount_per_serving, calories, soduim, carbohydrate, sugars, total_fat, trans_fat, saturated_fat, cholesterol, protein)"
                + " " + "values" + " " + "("
                + attachSingleQuotationMarks(product_name) + "," + " "
                + StringMaker.toMakeItLookGood(eatenAmount) + "," + " "
                + StringMaker.toMakeItLookGood(amountPerServing) + "," + " "
                + StringMaker.toMakeItLookGood(eatenNowKcal) + "," + " "
                + StringMaker.toMakeItLookGood(eatenSodium) + "," + " "
                + StringMaker.toMakeItLookGood(eatenCarbohydrate) + "," + " "
                + StringMaker.toMakeItLookGood(eatenSugars) + "," + " "
                + StringMaker.toMakeItLookGood(eatenTotalFat) + "," + " "
                + StringMaker.toMakeItLookGood(eatenTransFat) + "," + " "
                + StringMaker.toMakeItLookGood(eatenSaturatedFat) + "," + " "
                + StringMaker.toMakeItLookGood(eatenCholesterol) + "," + " "
                + StringMaker.toMakeItLookGood(eatenProtein) + ")";
        */

        String sql = "INSERT INTO" + " " + tableName;
        String sql2 = "";
        String sql3 = "";

        for(String[] str : selectedColumnNameAndTypeAndValue) {
            // about sql2
            sql2 = sql2 + str[0] + "," + " ";

            // about sql3
            if(str[1].equals("text")) { // 값의 TYPE 이 "text"라면...
                sql3 = sql3 + "\'" + str[2] + "\'" + "," + " ";
            } else {
                sql3 = sql3 + str[2] + "," + " ";
            }
        }// End of for문

        int lastIndex;
        lastIndex = sql2.lastIndexOf("," + " ");
        sql2 = "(" + sql2.substring(0, lastIndex) + ")";
        lastIndex = sql3.lastIndexOf("," + " ");
        sql3 = "(" + sql3.substring(0, lastIndex) + ")";

        sql = sql + " " +  sql2 + " " + "VALUES" + " " + sql3;
        /*디버그용*/ android.util.Log.i("addRecord()의 sql문", sql);
        database.execSQL(sql);

    }// End of addRecord()

    static public float getSumOfColumn(SQLiteDatabase database, String tableName, String columnName) {
        // Column의 Sum 도출하기 위한 Cursor 생성
        String sql = "SELECT sum(" + columnName + ") FROM" + " " + tableName;
        Cursor cursor = database.rawQuery(sql, null);

        cursor.moveToFirst();
        float sumValue=cursor.getFloat(0);
        cursor.close();

        return sumValue;

    }// End of getRecord()

    /* Nutri Capture 2.0 에서부터 추가됨
     * ↓ ↓ ↓ */
    // ◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼

    static public void pushArrayListBodilyToTable(ContextWrapper contextWrapper, ArrayList<ProductInfomation> arrayList, String tableName, String databaseName) {

        /*디버그용*/ android.util.Log.i("putArrayListBodilyToTable()", "데이터베이스 이름: " + databaseName);

        /* 났던 에러: android.database.sqlite.SQLiteCantOpenDatabaseException: Cannot open database 'database1': Unknown reason; cannot examine filesystem
         * 해결: https://www.google.com/search?q=android+studio+sqlite+without+context&oq=android+studio+sqlite+without+context&aqs=chrome..69i57j33i160l2.8643j0j4&sourceid=chrome&ie=UTF-8
         * ContextWrapper를 매개변수에 추가했다.
         * 그냥 Context를 써도 되는 거 같긴 한데, Nutri_Capture_Last에서는 ContextWrapper를 써서 나도 그냥 그걸로 변경했다.
         * 참조: https://s2choco.tistory.com/10 (ContextWrapper)
         * ↓ ↓ ↓ */
        SQLiteDatabase database = contextWrapper.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE,null);

        /* 통째로(Bodily) 넣는, 원시적인 메소드이기 때문에 기존 테이블을 삭제하고 다시 만든다.
         * 나중에 좀 더 고급 메소드로 업그레이드할 예정.
         * 이 메소드를 쓴다고, 처리 시간이 많이 지연되지는 않겠지만, 내가 찝찝하다.
         * ↓ ↓ ↓ */
        SQLiteVendingMachine.dropTable(database, tableName);

        if(arrayList.size() == 0) {
            return; //arrayList에 아무것도 없으면 table을 드랍시키고 그냥 종료
        }

        String[][] columnNameAndType = {
                {"name","text"}, // 1
                {"total_amount", "REAL"}, // 2
                {"amount_per_serving", "REAL"}, // 3
                {"calories", "REAL"}, // 4
                {"soduim", "REAL"}, // 5
                {"carbohydrate", "REAL"}, // 6
                {"sugars", "REAL"}, // 7
                {"total_fat", "REAL"}, // 8
                {"trans_fat", "REAL"}, // 9
                {"saturated_fat", "REAL"}, // 10
                {"cholesterol", "REAL"}, // 11
                {"protein", "REAL"}}; // 12
        SQLiteVendingMachine.createTableIfNotExsist(database, tableName, columnNameAndType);

        /* 향상된 for문: https://java119.tistory.com/107
         * 1. "인덱스를 사용하지 못한다."는 단점은 원래 알고 있었지만,
         * 2. "배열이나 ArrayList의 값을 사용할 순 있어도 절대 수정할 수는 없다."라는 새로운 단점이 있다고 한다.
         * 3. 어차피 여기서는 사용만 할 거라서 딱히 상관없지만, 뭔가 신기한 단점이다. 어떤 원리로 그런 단점이 생긴 건지도 궁금하고.
         * ↓ ↓ ↓ */
        for(ProductInfomation item : arrayList) {
            String[][] columnNameAndTypeAndValue = {
                    {"name","text", item.getName()},
                    {"total_amount", "REAL", StringMaker.toMakeItLookGood(item.getTotalAmount())},
                    {"amount_per_serving", "REAL", StringMaker.toMakeItLookGood(item.getAmountPerServing())},
                    {"calories", "REAL", StringMaker.toMakeItLookGood(item.getCalories())},
                    {"soduim", "REAL", StringMaker.toMakeItLookGood(item.getSoduim())},
                    {"carbohydrate", "REAL", StringMaker.toMakeItLookGood(item.getCarbohydrate())},
                    {"sugars", "REAL", StringMaker.toMakeItLookGood(item.getSugars())},
                    {"total_fat", "REAL", StringMaker.toMakeItLookGood(item.getTotalFat())},
                    {"trans_fat", "REAL", StringMaker.toMakeItLookGood(item.getTransFat())},
                    {"saturated_fat", "REAL", StringMaker.toMakeItLookGood(item.getSaturatedFat())},
                    {"cholesterol", "REAL", StringMaker.toMakeItLookGood(item.getCholesterol())},
                    {"protein", "REAL", StringMaker.toMakeItLookGood(item.getProtein())}};
            SQLiteVendingMachine.addRecord(database, tableName, columnNameAndTypeAndValue);
        }// End of for()

        database.close();

    }// End of putArrayListToTable()

    static public ArrayList<ProductInfomation> pullArrayListBodilyFromTable(ContextWrapper contextWrapper, String tableName, String databaseName) {

        /*디버그용*/ android.util.Log.i("pullArrayListBodilyFromTable()", "데이터베이스 이름: " + databaseName);

        /* 데이터베이스 열고, 임시 ArrayList 만들어두기
         * ↓ ↓ ↓ */
        SQLiteDatabase database = contextWrapper.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE,null);
        ArrayList<ProductInfomation> tmpArrayList = new ArrayList<ProductInfomation>();

        /* 테이블 없으면 빈 ArrayList 리턴.
         * ↓ ↓ ↓ */
        if(SQLiteVendingMachine.isTableExsist(database, tableName) == false) {
            /*디버그용*/ android.util.Log.i("pullArrayListBodilyFromTable()", "테이블 없으면 빈 ArrayList 리턴.");
            return tmpArrayList;
        }

        /* Table의 Record를 순회하는 Cursor 생성 후, Cursor를 맨 앞으로 이동
         * 참고: 안드로이드 앱 프로르래밍 8판 500쪽
         * ↓ ↓ ↓ */
        String sql = "SELECT _id, name, total_amount, amount_per_serving, calories, soduim, carbohydrate, sugars, total_fat, trans_fat, saturated_fat, cholesterol, protein FROM" + " " + tableName;
        Cursor cursor = database.rawQuery(sql, null);
        cursor.moveToFirst();

        /* 순회
         * ↓ ↓ ↓ */
        int maxRecordIndex = cursor.getCount() - 1;
        /*디버그용*/ android.util.Log.i("pullArrayListBodilyFromTable()", "cursor.getCount(): " + String.valueOf(cursor.getCount()));
        for(int i=0; i <= maxRecordIndex; i++) {

            /*
            _id // 0
            String inputDate, // x
            String inputName, // 1
            float inputAmount, // 2
            float inputAmountPerServing, // 3
            int inputMaxServingNumber, // x
            int inputEatenServingNumber, // x
            float inputCalories, // 4
            float inputSoduim, // 5
            float inputCarbohydrate, // 6
            float inputSugars, // 7
            float inputTotalFat, // 8
            float inputTransFat, // 9
            float inputSaturatedFat, // 10
            float inputCholesterol, // 11
            float inputProtein) { // 12
             */
            ProductInfomation tmpItem = new ProductInfomation(
                    // cursor.getInt(0)은 _id (SQLiteVendingMachine.createTableIfNotExsist() 참조)
                    "",
                    cursor.getString(1), // 1 inputName
                    cursor.getFloat(2), // 2 inputAmount
                    cursor.getFloat(3), // 3 inputAmountPerServing
                    0,
                    0,
                    cursor.getFloat(4), // 4 inputCalories
                    cursor.getFloat(5), // 5 inputSoduim
                    cursor.getFloat(6), // 6 inputCarbohydrate
                    cursor.getFloat(7), // 7 inputSugars
                    cursor.getFloat(8), // 8 inputTotalFat
                    cursor.getFloat(9), // 9 inputTransFat
                    cursor.getFloat(10), // 10 inputSaturatedFat
                    cursor.getFloat(11), // 11 inputCholesterol
                    cursor.getFloat(12) // 12 inputProtein
            );
            tmpArrayList.add(tmpItem);
            /*디버그용*/ android.util.Log.i("pullArrayListBodilyFromTable()",
                    "Record의 항목: "
                            + cursor.getString(1) + ", \n"
                            + String.valueOf(cursor.getString(2)) + ", \n"
                            + String.valueOf(cursor.getFloat(3)) + ", \n"
                            + String.valueOf(cursor.getFloat(4)) + ", \n"
                            + String.valueOf(cursor.getInt(5)) + ", \n"
                            + String.valueOf(cursor.getInt(6)) + ", \n"
                            + String.valueOf(cursor.getFloat(7)) + ", \n"
                            + String.valueOf(cursor.getFloat(8)) + ", \n"
                            + String.valueOf(cursor.getFloat(9)) + ", \n"
                            + String.valueOf(cursor.getFloat(10)) + ", \n"
                            + String.valueOf(cursor.getFloat(11)) + ", \n"
                            + String.valueOf(cursor.getFloat(12))
            );

            cursor.moveToNext();
        }// End of for()

        cursor.close();
        database.close();
        return tmpArrayList;

    }// End of pullArrayListFromTable()

}// End of Class
