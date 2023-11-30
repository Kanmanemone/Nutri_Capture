package com.example.nutri_capture_last;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/* 콜백 함수에 대한 정의: https://bestkingit.tistory.com/74
 * '콜백 함수'와 '리스너 메소드'와의 차이: https://crazykim2.tistory.com/630
 * 출처: https://everyshare.tistory.com/m/27
 * ↓ ↓ ↓ */
public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private OnItemTouchHelperListener listener;

    public ItemTouchHelperCallback(OnItemTouchHelperListener listener) {
        this.listener = listener;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Vertical Orientation 리니어 레이아웃 등에서 위 아래로만 스와이프 되게끔 할 때는 이 코드 사용 //int drag_flags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int drag_flags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
        int swipe_flags = ItemTouchHelper.START | ItemTouchHelper.END;

        // /* onMove와 onSwipe 둘 다 사용 */return makeMovementFlags(drag_flags, swipe_flags);
        /* onMove만 사용 (onSwipe 사용안함) */ return makeMovementFlags(drag_flags,0);
        // /* onSwipe만 사용 (onMove 사용안함) */ return makeMovementFlags(0,swipe_flags);
    }

    /* isLongPressDragEnabled() -> true를 반환하도록 설정하면 롱클릭을 감지한다.
     * ↓ ↓ ↓ */
    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return listener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onItemSwipe(viewHolder.getAdapterPosition());
    }

}// End of ItemTouchHelperCallback()