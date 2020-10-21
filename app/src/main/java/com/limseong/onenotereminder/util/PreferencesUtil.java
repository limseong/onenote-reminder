package com.limseong.onenotereminder.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class PreferencesUtil {

    private static final String APP_PREF_NAME = "app_pref";
    private static final String STRING_NO_VALUE = "";
    private static PreferencesUtil instance = null;

    private PreferencesUtil() {}

    public static synchronized PreferencesUtil getInstance() {
        if (instance == null) {
            instance = new PreferencesUtil();
        }
        return instance;
    }

    /**
     * Set preferences with a default Gson object.
     */
    public static void setPreferences(Context context, String key, Object data) {
        Gson defaultGson = new Gson();
        setPreferencesGson(context, key, data, defaultGson);
    }

    /**
     * Serialize the given object with the given Gson and set the value to the preferences.
     */
    public static void setPreferencesGson(Context context, String key, Object data, Gson gson) {
        String json = gson.toJson(data);

        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(key, json);
        editor.commit();
    }

    /**
     * Get an object from preferences with a default Gson object.
     */
    public static <T> T getPreferences(Context context, String key, Class<T> clazz) {
        Gson defaultGson = new Gson();
        return getPreferencesGson(context, key, clazz, defaultGson);
    }

    /**
     * Get an object from preferences with the given Gson.
     */
    public static <T> T getPreferencesGson(Context context, String key, Class<T> clazz, Gson gson) {
        T loadedObject = null;

        String json = getPreferences(context).getString(key, STRING_NO_VALUE);
        if (json != STRING_NO_VALUE) {
            loadedObject = gson.fromJson(json, clazz);
        }

        return loadedObject;
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
    }
}