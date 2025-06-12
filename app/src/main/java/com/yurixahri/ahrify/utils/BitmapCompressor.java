package com.yurixahri.ahrify.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class BitmapCompressor {

    public enum Format {
        JPEG(Bitmap.CompressFormat.JPEG),
        PNG(Bitmap.CompressFormat.PNG),
        WEBP(Bitmap.CompressFormat.WEBP);

        private final Bitmap.CompressFormat androidFormat;

        Format(Bitmap.CompressFormat format) {
            this.androidFormat = format;
        }

        public Bitmap.CompressFormat toAndroidFormat() {
            return androidFormat;
        }
    }

    public static Bitmap compress(Bitmap original, Format format, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        original.compress(format.toAndroidFormat(), quality, stream);
        byte[] byteArray = stream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public static Bitmap resizeKeepRatio(Bitmap original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) (maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) (maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(original, finalWidth, finalHeight, true);
    }

    public static byte[] toByteArray(Bitmap original, Format format, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        original.compress(format.toAndroidFormat(), quality, stream);
        return stream.toByteArray();
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public static Bitmap blobToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public static Bitmap base64ToBitmap(String base64Str) {
        byte[] decoded = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // or JPEG
        byte[] bytes = stream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}
