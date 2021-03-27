package com.smirnov.lab7android.Task2;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.smirnov.lab7android.databinding.Task1Binding;
import com.smirnov.lab7android.databinding.Task2Binding;


public class MainActivity extends AppCompatActivity {

    Task2Binding task2Binding;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        task2Binding = Task2Binding.inflate(getLayoutInflater());
        setContentView(task2Binding.getRoot());

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("MESSAGE");
                task2Binding.textView.setText(message);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("BROADCAST"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}