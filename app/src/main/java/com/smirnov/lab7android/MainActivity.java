package com.smirnov.lab7android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.smirnov.lab7android.databinding.Task1Binding;


public class MainActivity extends AppCompatActivity {

    Task1Binding task1Binding;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        task1Binding = Task1Binding.inflate(getLayoutInflater());
        setContentView(task1Binding.getRoot());

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("Message");
                task1Binding.textView.setText(message);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("broadcast"));
        task1Binding.button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("thread in MainActivity", Thread.currentThread().getName());
                        Intent intent = new Intent(MainActivity.this,
                                DownloadService.class).putExtra("url",
                                "https://mymeizu.md/wp-content/uploads/" +
                                        "2016/11/servisy-google-meizu.jpg");
                        DownloadService.enqueueWork(MainActivity.this,intent);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}