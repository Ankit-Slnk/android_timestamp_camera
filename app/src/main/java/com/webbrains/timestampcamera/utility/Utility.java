package com.webbrains.timestampcamera.utility;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {

    public static String getCurrentDate(Date date) {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
        String currentTime = new SimpleDateFormat("h:mm a").format(date);
        return currentDate + " • " + currentTime;
    }

    public static File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "camera_timestamp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("camera_timestamp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

//    static public Bitmap applyWaterMarkEffect(Bitmap src, String watermark, int x, int y, int size) {
//        int w = src.getWidth();
//        int h = src.getHeight();
//        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());
//
//        Canvas canvas = new Canvas(result);
//        canvas.drawBitmap(src, 0, 0, null);
//
//        Paint paint = new Paint();
//        paint.setColor(Color.BLACK);
//        paint.setTextSize(size);
//        paint.setAntiAlias(true);
//        canvas.drawText(watermark, x, y, paint);
//
//        return result;
//    }

    public static File bitmapToFile(Bitmap bitmap) {
        File file = getOutputMediaFile();
        try {
            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            Log.e("exists", file.exists() + "");
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return file; // it will return null
        }
    }
}
