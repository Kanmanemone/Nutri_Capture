package com.example.nutri_capture_last;

import android.view.View;

/* 클릭 이벤트는 리싸이클러뷰가 아니라 각 아이템에 발생하게 되므로 뷰홀더 안에서 클릭 이벤트를 처리할 수 있도록 만들어야 한다.
 * 뷰홀더의 생성자로부터 뷰 객체가 전달되므로 이 뷰 객체에 onClickListener를 설정한다
 * 그러면 그 뷰를 클릭했을 때 그 리스너의 onClick 메서드가 호출된다.
 * ★☆ 그런데 클릭 시 행하고 싶은 기능이 변경될 때마다 Adapter를 수정해야 하는 귀찮음이라는 문제점이 생기므로, ★☆
 * ★☆ 어댑터 객체 밖에서 리스너를 설정하고 설정된 리스너 쪽에 이벤트를 기술하는 것이 좋다. 이와 같이 말이다. ★☆
 * ↓ ↓ ↓ */
public interface OnItemClickListener {
    public void onItemClick(NutrientItemAdapter.myViewHolder holder, View view, int position);

    // public void onItemEditClick(NutrientItemAdapter.myViewHolder holder, View view, int position);

    public void onItemDeleteClick(NutrientItemAdapter.myViewHolder holder, View view, int position);
}// End of OnItemClickListener