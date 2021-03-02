package com.smirnov.lab7android;

import android.annotation.SuppressLint;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

public class DownloadServiceTask3 extends Service {

    String url;
    Messenger messenger;

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        messenger = new Messenger(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 1) {
                    new DownloadAsyncTask(msg.replyTo).execute(msg.getData().getString("url", url));
                }
                super.handleMessage(msg);
            }
        });
        return messenger.getBinder();
    }

    class DownloadAsyncTask extends AsyncTask<String, Void, String> {

        private Messenger receiver;

        DownloadAsyncTask(Messenger receiver) {
            this.receiver = receiver;
        }

        @SuppressLint("LongLogTag")
        @Override
        protected String doInBackground(String... strings) {
            Log.d("thread In DownloadService", Thread.currentThread().getName());
            String urls = strings[0];
            String path = null;
            try {
                path = download(urls);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return path;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Message message = Message.obtain(null, 1);
            Bundle data = new Bundle();
            data.putString("answer", s);
            message.setData(data);
            try {
                if (receiver != null) {
                    receiver.send(message);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("url");
        if (url == null) {
            sendBroadcast("path = null");
            stopSelf(startId);
        } else {
            try {
                sendBroadcast(new DownloadAsyncTask(null).execute(url).get());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return START_NOT_STICKY;
    }

    public String download(String url) throws IOException {
        Bitmap mIcon11 = null;
        String path = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
            path = save(mIcon11, "img" + (int) (Math.random() * 10000));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return path;
    }

    public String save(Bitmap bitmap, String name) {
        FileOutputStream Stream;
        try {
            Stream = this.getApplicationContext().openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, Stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return getApplicationContext().getFileStreamPath(name).getAbsolutePath();
    }

    public void sendBroadcast(String message) {
        sendBroadcast(new Intent("broadcast").putExtra("Message", message));
    }
}