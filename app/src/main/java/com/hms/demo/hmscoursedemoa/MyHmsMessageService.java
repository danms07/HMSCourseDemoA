package com.hms.demo.hmscoursedemoa;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyHmsMessageService extends HmsMessageService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.i("Token",token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

    }


    Bitmap getNotificationBitmap(String url){
        Bitmap bitmap=getBitmap(url);
        if (bitmap != null) {
            return getResizedBitmap(bitmap,200,400);
        } return null;

    }

    private Bitmap getBitmap(String imageUrl){
        try {
            URL url= new URL(imageUrl);
            HttpURLConnection connection=(HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input  = connection.getInputStream();
            return BitmapFactory.decodeStream(input);


        }catch (Exception e){
            return null;
        }
    }

    private Bitmap getResizedBitmap(Bitmap bitmap, int newHeight,int newWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();

        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, false);
    }
}
