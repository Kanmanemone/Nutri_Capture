package com.example.nutri_capture_last;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NutrientItemAdapter extends RecyclerView.Adapter<NutrientItemAdapter.myViewHolder> {

    ArrayList<ProductInfomation> itemArrayList = new ArrayList<ProductInfomation>();
    OnItemClickListener listener;

    @NonNull
    @Override
    public NutrientItemAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.item_product, viewGroup, false);
        return new myViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull NutrientItemAdapter.myViewHolder viewHolder, int position) {
        ProductInfomation item = itemArrayList.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return itemArrayList.size();
    }

    static class myViewHolder extends RecyclerView.ViewHolder {
        TextView statusText;
        TextView nameText;
        TextView kcalText;
        ImageButton deleteButton;

        public myViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            statusText = itemView.findViewById(R.id.statusText);
            nameText = itemView.findViewById(R.id.nameText);
            kcalText = itemView.findViewById(R.id.kcalText);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    listener.onItemClick(myViewHolder.this, view, position);
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(listener != null) {
                        listener.onItemDeleteClick(myViewHolder.this, view, position);
                    }
                }
            });

        }// End of constructor of myViewHolder()

        public void setItem(ProductInfomation item) {
            /* SpannableStringBuilder로 Text의 일부분'만' Bold 처리
             * 출처: https://re-build.tistory.com/13
             * ↓ ↓ ↓ */
            SpannableString spannableString1 = new SpannableString("● ");
            String judge = judgeColor(item);
            if(judge.equals("orange")) {
                /* orange1 */ spannableString1.setSpan(new ForegroundColorSpan(Color.parseColor("#FFff9800")), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if(judge.equals("red")) {
                /* red */ spannableString1.setSpan(new ForegroundColorSpan(Color.parseColor("#FFe81919")), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                /* green2 */ spannableString1.setSpan(new ForegroundColorSpan(Color.parseColor("#FF00c33e")), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            statusText.setText(spannableString1);

            String trimmedKcal = StringMaker.toMakeItLookGood(item.getCalories());
            SpannableString spannableString2 = new SpannableString( trimmedKcal + " kcal");
            // spannableString2.setSpan(new ForegroundColorSpan(Color.RED), 0, String.valueOf(trimmedKcal).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            kcalText.setText(spannableString2);

            nameText.setText(item.getName());

        }// End of setItem

        /* statusText 의 색상 결정을 위한 로직
         * ↓ ↓ ↓ */
        // ◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼
        public String judgeColor(ProductInfomation item) {
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
                    return "red";
                }
            }

            if(0 < item.getTransFat()) {
                return "red";
            }

            for(String s : tmp) {
                if(s.equals("orange")) {
                    return "orange";
                }
            }

            return "green";
        }

    }// End of myViewHolder()

    // ◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼

    public void addItem(ProductInfomation item) {
        itemArrayList.add(item);
    }

    public void deleteItem(ProductInfomation item) {
        itemArrayList.remove(item);
    }

    public void setItemAtPosition(int position, ProductInfomation item) {
        itemArrayList.set(position, item);
    }

    public ProductInfomation getItem(int position) {
        return itemArrayList.get(position);
    }

    public ArrayList<ProductInfomation> getItemArrayList() {
        return itemArrayList;
    }

    public void setItemArrayList(ArrayList<ProductInfomation> itemArrayList) {

        this.itemArrayList = itemArrayList;
    }


    /* '인터페이스 레퍼런스를 통해 구현체를 사용하는 방법'(implements 미사용, new 익명 구현 객체 생성이나 외부에서 인터페이스 구현 객체를 받아옴)
     * ↓ ↓ ↓ */
    // ◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }// End of setOnItemClickListener()



}// End of Class
