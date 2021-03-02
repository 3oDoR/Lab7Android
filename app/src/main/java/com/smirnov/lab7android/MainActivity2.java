package com.smirnov.lab7android;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.smirnov.lab7android.databinding.Task2Binding;


public class MainActivity2 extends AppCompatActivity {
    Task2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = Task2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("thread in MainActivity", Thread.currentThread().getName());
                        Intent intent = new Intent(MainActivity2.this, DownloadService.class).putExtra("url", "https://mymeizu.md/wp-content/uploads/2016/11/servisy-google-meizu.jpg");
                        DownloadService.enqueueWork(MainActivity2.this,intent);
                    }
                });
    }
}