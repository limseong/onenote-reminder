package com.limseong.onenotereminder.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    public static String DIR = "";
    private static FileUtils instance = null;

    private FileUtils() {}

    public static synchronized FileUtils getInstance() {
        if (instance == null) {
            instance = new FileUtils();
        }

        return instance;
    }

    public static void saveFile(Context context, String fileName, String data) throws IOException {
        FileOutputStream fos = context.openFileOutput(DIR + fileName, Context.MODE_PRIVATE);
        fos.write(data.getBytes());
        fos.close();
    }

    public static byte[] loadFile(Context context, String fileName) throws IOException {
        FileInputStream fis = context.openFileInput(DIR + fileName);
        byte[] data = new byte[fis.available()];
        while (fis.read(data) != -1) {;}
        fis.close();

        return data;
    }

    public static String getFilesDirAbsolutePath(Context context) {
        File dir = context.getFilesDir();
        return dir.getAbsolutePath();
    }

    public static boolean deleteFile(Context context, String fileName) {
        boolean hasDeleted = context.deleteFile(fileName);
        return hasDeleted;
    }
}