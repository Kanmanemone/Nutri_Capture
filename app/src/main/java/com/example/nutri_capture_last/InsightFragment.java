package com.example.nutri_capture_last;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InsightFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View insightFragment_v = inflater.inflate(R.layout.fragment_insight, container, false);



        return insightFragment_v;
    }// End of onCreateView()

    @Override
    public void onStart() {
        super.onStart();

        // MainActivity의 툴바 속 TextView의 Text 변경 등등 ...
        ((MainActivity)MainActivity.context_main).workAfterReplaceFragmentTo(this);

    }//End of onStart

}// End of Class

