package com.smirnov.lab7android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class DownloadService extends JobIntentService {

    static final int JOB_ID = 2;

    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d("thread In DownloadService", Thread.currentThread().getName());
        String url = intent.getStringExtra("url");
        if (url == null) {
            sendBroadcast(new Intent("broadcast").putExtra("Message", "path = null"));
            stopSelf();
        } else {
            try {
                String path = download(url);
                sendBroadcast(new Intent("broadcast").putExtra("Message", path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String download(String url) throws IOException {
        Bitmap bitmap;
        String path = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(in);
            path = save(bitmap, "img" + (int) (Math.random() * 10000));
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

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, DownloadService.class, JOB_ID, work);
    }
}
