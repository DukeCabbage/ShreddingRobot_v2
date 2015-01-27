package com.gloomy.ShreddingRobot.Utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
    private final static String TAG = "BitmapWorkerTask";

    private final WeakReference<ImageView> imageViewReference;
    private String imagePath;
    private int reqHeight, reqWidth;

    public BitmapWorkerTask(ImageView imageView, String imagePath, int reqHeight, int reqWidth) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
        this.imagePath = imagePath;
        this.reqHeight = reqHeight;
        this.reqWidth = reqWidth;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Integer... params) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final  BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
//        Log.e(TAG, "reqHeight: "+reqHeight+", reqWidth: "+reqWidth);
//        Log.e(TAG, "outHeight: "+height+", outWidth: "+width);

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.e(TAG, "Sample Size: " + inSampleSize);
        return inSampleSize;
    }
}