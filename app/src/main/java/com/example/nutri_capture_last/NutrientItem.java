package com.example.nutri_capture_last;

import android.os.Parcel;
import android.os.Parcelable;

public class NutrientItem implements Parcelable {
    
    String name;
    int level;
    int power;
    
    /* 생성자 (오른쪽 클릭 - Generate - Constructor)
     * ↓ ↓ ↓ */
    public NutrientItem(String name, int level, int power) {
        this.name = name;
        this.level = level;
        this.power = power;
    }

    protected NutrientItem(Parcel in) {
        name = in.readString();
        level = in.readInt();
        power = in.readInt();
    }

    public static final Parcelable.Creator<NutrientItem> CREATOR = new Parcelable.Creator<NutrientItem>() {
        @Override
        public NutrientItem createFromParcel(Parcel in) {
            return new NutrientItem(in);
        }

        @Override
        public NutrientItem[] newArray(int size) {
            return new NutrientItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(level);
        parcel.writeInt(power);
    }

    /* Getter와 Setter
     * ↓ ↓ ↓ */
    // ◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼

    /* Getter (오른쪽 클릭 - Generate - Constructor - Getter)
     * ↓ ↓ ↓ */
    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getPower() {
        return power;
    }

    /* Setter (오른쪽 클릭 - Generate - Constructor - Setter)
     * ↓ ↓ ↓ */
    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setPower(int power) {
        this.power = power;
    }

    /* 응용 메소드
     * ↓ ↓ ↓ */
    // ◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼




}// End of EquipmentItem

