package com.smirnov.lab7android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class DownloadServiceTask3 extends Service {
    String url;
    Messenger messenger;
    Random random = new Random();

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        messenger = new Messenger(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 89) {
                    Log.d("thread", Thread.currentThread().getName());
                    new DownloadAsyncTask(msg.replyTo).execute(msg.getData().getString("URL", url));
                }
            }
        });
        return messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        url = intent.getStringExtra("URL");
        if (url != null) {
            try {
                sendBroadcast(new Intent("BROADCAST").putExtra("MESSAGE", new DownloadAsyncTask(null).execute(url).get()));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            sendBroadcast(new Intent("BROADCAST").putExtra("MESSAGE", "path = null"));
            stopSelf(startId);
        }
        return START_NOT_STICKY;
    }


    public String download(String url) {
        String path = null;
        random = new Random();
        try {
            InputStream in = new java.net.URL(url).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            path = save(bitmap, "img" + random.nextInt(1000000));
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return path;
    }

    public String save(Bitmap bitmap, String name) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = this.getApplicationContext().openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return getApplicationContext().getFileStreamPath(name).getAbsolutePath();
    }


    class DownloadAsyncTask extends AsyncTask<String, Void, String> {
        Messenger receiver;

        DownloadAsyncTask(Messenger receiver) {
            this.receiver = receiver;
        }


        @Override
        protected String doInBackground(String... strings) {
            Log.d("doInBackground", Thread.currentThread().getName());
            return download(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Message message = Message.obtain(null, 89, "TO_ACTIVITY");
            Bundle bundle = new Bundle();
            bundle.putString("ANSWER", s);
            message.setData(bundle);
            try {
                if (receiver != null) {
                    receiver.send(message);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}