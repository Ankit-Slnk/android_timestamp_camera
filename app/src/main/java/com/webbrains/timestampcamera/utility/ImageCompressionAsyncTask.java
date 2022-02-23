package com.webbrains.timestampcamera.utility;

import android.os.AsyncTask;

import java.io.File;

public abstract class ImageCompressionAsyncTask extends AsyncTask<String, Void, File> {

    @Override
    protected File doInBackground(String... strings) {
        if (strings.length == 0 || strings[0] == null)
            return null;
        return ImageUtils.compressImage(strings[0]);
    }

    protected abstract void onPostExecute(File file);
}
