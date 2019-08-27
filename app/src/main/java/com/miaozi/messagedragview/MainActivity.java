package com.miaozi.messagedragview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DragView.bindDragView(findViewById(R.id.tv), new DragViewTouchListener.DragViewListener() {
            @Override
            public void dismiss(View view) {
                Log.d("TAG","dismiss");
            }
        });
        DragView.bindDragView(findViewById(R.id.iv), new DragViewTouchListener.DragViewListener() {
            @Override
            public void dismiss(View view) {
                Log.d("TAG","dismiss");
            }
        });
    }
}
