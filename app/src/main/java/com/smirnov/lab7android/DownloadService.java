package com.smirnov.lab7android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class DownloadService extends JobIntentService {

    static final int JOB_ID = 1000;
    Random random;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String url = intent.getStringExtra("URL");
        Log.d("onHandleWork", Thread.currentThread().getName());
        if (url == null) {
            sendBroadcast(new Intent("BROADCAST").putExtra("MESSAGE", "path = null"));
        } else {
            String path = download(url);
            sendBroadcast(new Intent("BROADCAST").putExtra("MESSAGE", path));
        }
        stopSelf(JOB_ID);
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
        } catch (IOException e){
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return getApplicationContext().getFileStreamPath(name).getAbsolutePath();
    }

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, DownloadService.class, JOB_ID, work);
    }
}
