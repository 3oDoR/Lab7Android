package com.smirnov.lab7android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.smirnov.lab7android.databinding.Task1Binding;


public class MainActivity extends AppCompatActivity {

    Task1Binding task1Binding;
    BroadcastReceiver broadcastReceiver;
    String url = "https://pbs.twimg.com/profile_images/1045580248467886080/_uwwJdr3.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        task1Binding = Task1Binding.inflate(getLayoutInflater());
        setContentView(task1Binding.getRoot());

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("MESSAGE");
                task1Binding.textView.setText(message);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("BROADCAST"));
        task1Binding.button.setOnClickListener(
                v -> {
                    Log.d("onClick() ", Thread.currentThread().getName());
                    DownloadService.enqueueWork(MainActivity.this,
                            new Intent(MainActivity.this,
                                    DownloadService.class).putExtra("URL", url));
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}