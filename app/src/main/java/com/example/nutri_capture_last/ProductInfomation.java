package com.example.nutri_capture_last;

import android.os.Parcel;
import android.os.Parcelable;

public final class ProductInfomation implements Parcelable {

    String date; //1
    String name; //2
    float totalAmount; //3
    float amountPerServing; //4
    int maxServingNumber; //5
    int eatenServingNumber; //6
    float calories; //7
    float soduim; //8
    float carbohydrate; //9
    float sugars; //10
    float totalFat; //11
    float transFat; //12
    float saturatedFat; //13
    float cholesterol; //14
    float protein; //15

     public ProductInfomation(String inputDate, String inputName, float inputAmount, float inputAmountPerServing, int inputMaxServingNumber,
                             int inputEatenServingNumber, float inputCalories, float inputSoduim, float inputCarbohydrate, float inputSugars,
                             float inputTotalFat, float inputTransFat, float inputSaturatedFat, float inputCholesterol, float inputProtein) {
        this.date = inputDate; //1
        this.name = inputName; //2
        this.totalAmount = inputAmount; //3
        this.amountPerServing = inputAmountPerServing; //4
        this.maxServingNumber = inputMaxServingNumber; //5
        this.eatenServingNumber = inputEatenServingNumber; //6
        this.calories = inputCalories; //7
        this.soduim = inputSoduim;  //8
        this.carbohydrate = inputCarbohydrate; //9
        this.sugars = inputSugars; //10
        this.totalFat = inputTotalFat; //11
        this.transFat = inputTransFat; //12
        this.saturatedFat = inputSaturatedFat; //13
        this.cholesterol = inputCholesterol; //14
        this.protein = inputProtein; //15
    }

    // 복구하는 생성자 writeToParcel 에서 기록한 순서를 똑같이 해줘야함 <- 여기에 달 주석이 아닐 수도... (https://ggari.tistory.com/221)
    protected ProductInfomation(Parcel in) {
        this.date = in.readString(); //1
        this.name = in.readString(); //2
        this.totalAmount = in.readFloat(); //3
        this.amountPerServing = in.readFloat(); //4
        this.maxServingNumber = in.readInt(); //5
        this.eatenServingNumber = in.readInt(); //6
        this.calories = in.readFloat(); //7
        this.soduim = in.readFloat();  //8
        this.carbohydrate = in.readFloat(); //9
        this.sugars = in.readFloat(); //10
        this.totalFat = in.readFloat(); //11
        this.transFat = in.readFloat(); //12
        this.saturatedFat = in.readFloat(); //13
        this.cholesterol = in.readFloat(); //14
        this.protein = in.readFloat(); //15
    }

    public static final Creator<ProductInfomation> CREATOR = new Creator<ProductInfomation>() {
        @Override
        public ProductInfomation createFromParcel(Parcel in) {
            return new ProductInfomation(in);
        }

        @Override
        public ProductInfomation[] newArray(int size) {
            return new ProductInfomation[size];
        }
    };

    //parcel 오브젝트 종류 (https://ggari.tistory.com/221)
    @Override
    public int describeContents() {
        return 0;
    }

    //실제 오브젝트 (https://ggari.tistory.com/221)
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(date); //1
        parcel.writeString(name); //2
        parcel.writeFloat(totalAmount); //3
        parcel.writeFloat(amountPerServing); //4
        parcel.writeInt(maxServingNumber); //5
        parcel.writeInt(eatenServingNumber); //6
        parcel.writeFloat(calories); //7
        parcel.writeFloat(soduim);//8
        parcel.writeFloat(carbohydrate);//9
        parcel.writeFloat(sugars);//10
        parcel.writeFloat(totalFat);//11
        parcel.writeFloat(transFat);//12
        parcel.writeFloat(saturatedFat);//13
        parcel.writeFloat(cholesterol);//14
        parcel.writeFloat(protein);//15
    }

    //1
    public void setDate(String date) {
        this.date = date;
    }
    public String getDate() {
        return date;
    }

    //2
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    //3
    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }
    public float getTotalAmount() {
        return totalAmount;
    }

    //4
    public void setAmountPerServing(float amountPerServing) {
        this.amountPerServing = amountPerServing;
    }
    public float getAmountPerServing() {
        return amountPerServing;
    }

    //5
    public void setMaxServingNumber(int maxServingNumber) {
        this.maxServingNumber = maxServingNumber;
    }
    public int getMaxServingNumber() {
        return maxServingNumber;
    }

    //6
    public void setEatenServingNumber(int eatenServingNumber) {
        this.eatenServingNumber = eatenServingNumber;
    }
    public int getEatenServingNumber() {
        return eatenServingNumber;
    }

    //7
    public void setCalories(float calories) {
        this.calories = calories;
    }
    public float getCalories() {
        return calories;
    }

    //8
    public void setSoduim(float soduim) {
        this.soduim = soduim;
    }
    public float getSoduim() {
        return soduim;
    }

    //9
    public void setCarbohydrate(float carbohydrate) {
        this.carbohydrate = carbohydrate;
    }
    public float getCarbohydrate() {
        return carbohydrate;
    }

    //10
    public void setSugars(float sugars) {
        this.sugars = sugars;
    }
    public float getSugars() {
        return sugars;
    }

    //11
    public void setTotalFat(float totalFat) {
        this.totalFat = totalFat;
    }
    public float getTotalFat() {
        return totalFat;
    }

    //12
    public void setTransFat(float transFat) {
        this.transFat = transFat;
    }
    public float getTransFat() {
        return transFat;
    }

    //13
    public void setSaturatedFat(float saturatedFat) {
        this.saturatedFat = saturatedFat;
    }
    public float getSaturatedFat() {
        return saturatedFat;
    }

    //14
    public void setCholesterol(float cholesterol) {
        this.cholesterol = cholesterol;
    }
    public float getCholesterol() {
        return cholesterol;
    }

    //15
    public void setProtein(float protein) {
        this.protein = protein;
    }
    public float getProtein() {
        return protein;
    }

}//End of Class
