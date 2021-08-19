package com.example.makesketch.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.example.makesketch.R;
import com.github.dhaval2404.imagepicker.ImagePicker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tvOpen=findViewById(R.id.tvOpen);
        tvOpen.setOnClickListener(v -> {
            ImagePicker.Companion.with(this)
                    .crop()
                    .maxResultSize(getWidth(), getHeight())
                    .start();
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== Activity.RESULT_OK){
            if (data != null) {
                Intent intent=new Intent(this,PicEditorActivity.class);
                intent.setData(data.getData());
                startActivityForResult(intent,111);
            }
        }
    }

    public int getWidth(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
    public int getHeight(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels-72;
    }

}