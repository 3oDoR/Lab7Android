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
    BroadcastReceiver br;
    Messenger boundServiceMessenger = null;
    private Boolean connected = false;
    Messenger messenger = new Messenger(new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                binding.textView.setText(msg.getData().getString(" "));
            }
            super.handleMessage(msg);
        }
    });

    private ServiceConnection ServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundServiceMessenger = new Messenger(service);
            connected = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundServiceMessenger = null;
            connected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message message = Message.obtain(null, 1);
                        Bundle data = new Bundle();
                        data.putString("url", "https://mymeizu.md/wp-content/uploads/2016" +
                                "/11/servisy-google-meizu.jpg");
                        message.replyTo = messenger;
                        message.setData(data);
                        try {
                            boundServiceMessenger.send(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("Message");
                binding.textView.setText(message);
            }
        };

        registerReceiver(br, new IntentFilter("broadcast"));
        binding.button2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("thread in MainActivity", Thread.currentThread().getName());
                        Intent intent = new Intent(MainActivity3.this,
                                DownloadServiceTask3.class).putExtra("url",
                                "https://mymeizu.md/wp-content/uploads/2016/" +
                                        "11/servisy-google-meizu.jpg");
                        startService(intent);
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(MainActivity3.this, DownloadServiceTask3.class);
        bindService(intent, ServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (connected) {
            unbindService(ServiceConnection);
            connected = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }
}