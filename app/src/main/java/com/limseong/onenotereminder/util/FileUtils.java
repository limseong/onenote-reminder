package com.limseong.onenotereminder.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    public static void saveFileGson(Context context, String fileName, Object data, Gson gson)
            throws IOException {
        String json = gson.toJson(data);
        FileUtils.saveFile(context, fileName, json);
    }

    public static <T> T loadFileGson(Context context, String fileName, Class<T> clazz, Gson gson) {
        T loadedObject = null;
        try {
            byte[] data = FileUtils.loadFile(context, fileName);
            String json = new String(data);
            loadedObject = gson.fromJson(json, clazz);
        }
        catch (FileNotFoundException notFound) {
            // if the file doesn't exists
            ;
        }
        catch (IOException e) {
            // something is wrong
            Log.e("FileUtils", "loadFileGson() for" + clazz.getName() + " failed.", e);
        }

        return loadedObject;
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