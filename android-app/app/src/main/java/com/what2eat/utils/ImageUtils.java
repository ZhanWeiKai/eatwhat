package com.what2eat.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片工具类 - 压缩和处理
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1920;
    private static final int QUALITY = 85;

    /**
     * 压缩图片文件
     * @param context Context
     * @param imageUri 图片URI
     * @return 压缩后的文件路径
     */
    public static String compressImage(Context context, Uri imageUri) {
        try {
            // 获取原始图片
            Bitmap bitmap = getBitmapFromUri(context, imageUri);
            if (bitmap == null) {
                return null;
            }

            // 读取EXIF信息并旋转图片
            bitmap = rotateImageIfRequired(context, bitmap, imageUri);

            // 压缩图片
            bitmap = compressBitmap(bitmap);

            // 保存到临时文件
            String tempPath = getTempImagePath(context);
            saveBitmapToFile(bitmap, tempPath);

            // 回收bitmap
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }

            return tempPath;
        } catch (Exception e) {
            Log.e(TAG, "压缩图片失败", e);
            return null;
        }
    }

    /**
     * 从URI获取Bitmap
     */
    private static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        inputStream.close();

        // 重新打开流读取图片
        inputStream = context.getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        // 计算采样率
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);
        options.inJustDecodeBounds = false;

        inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        return bitmap;
    }

    /**
     * 计算采样率
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * 根据EXIF信息旋转图片
     */
    private static Bitmap rotateImageIfRequired(Context context, Bitmap bitmap, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        ExifInterface exif = new ExifInterface(inputStream);
        inputStream.close();

        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateBitmap(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateBitmap(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateBitmap(bitmap, 270);
            default:
                return bitmap;
        }
    }

    /**
     * 旋转Bitmap
     */
    private static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    /**
     * 压缩Bitmap
     */
    private static Bitmap compressBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // 如果尺寸超过限制，进行缩放
        if (width > MAX_WIDTH || height > MAX_HEIGHT) {
            float scale = Math.min(
                    (float) MAX_WIDTH / width,
                    (float) MAX_HEIGHT / height
            );

            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);

            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }

        return bitmap;
    }

    /**
     * 保存Bitmap到文件
     */
    private static void saveBitmapToFile(Bitmap bitmap, String path) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, bos);

        FileOutputStream fos = new FileOutputStream(path);
        fos.write(bos.toByteArray());
        fos.flush();
        fos.close();
        bos.close();
    }

    /**
     * 获取临时图片路径
     */
    private static String getTempImagePath(Context context) {
        File cacheDir = context.getCacheDir();
        File tempDir = new File(cacheDir, "photos");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        String fileName = "photo_" + System.currentTimeMillis() + ".jpg";
        return new File(tempDir, fileName).getAbsolutePath();
    }

    /**
     * 获取文件大小
     */
    public static long getFileSize(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.length();
        }
        return 0;
    }

    /**
     * 删除临时文件
     */
    public static void deleteTempFile(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
