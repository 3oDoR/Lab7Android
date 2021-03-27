package com.smirnov.lab7android;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.smirnov.lab7android.databinding.ActivityMainBinding;


public class MainActivity3 extends AppCompatActivity {
    ActivityMainBinding binding;
    BroadcastReceiver broadcastReceiver;
    Messenger boundServiceMessenger = null;
    String url = "https://pbs.twimg.com/profile_images/1045580248467886080/_uwwJdr3.jpg";


    Messenger messenger = new Messenger(new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 89) {
                binding.textView.setText(msg.getData().getString("ANSWER"));
            }
        }
    });

    private final ServiceConnection ServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("MainActivity", "Service connected");
            boundServiceMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("MainActivity", "Service disconnected");
            boundServiceMessenger = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.button.setOnClickListener(
                v -> {
                    Message message = Message.obtain(null, 89, "TO_SERVICE");
                    Bundle bundle = new Bundle();
                    message.replyTo = messenger;
                    bundle.putString("URL", url);
                    message.setData(bundle);
                    try {
                        boundServiceMessenger.send(message);
                    } catch (RemoteException e) {
                        Log.e("Error", e.getMessage());
                        e.printStackTrace();
                    }
                });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("MESSAGE");
                binding.textView.setText(message);
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter("BROADCAST"));
        binding.button2.setOnClickListener(
                v -> {
                    Log.d("thread in MainActivity", Thread.currentThread().getName());
                    startService(new Intent(MainActivity3.this, DownloadServiceTask3.class).putExtra("URL", url));
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(MainActivity3.this, DownloadServiceTask3.class);
        bindService(intent, ServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}