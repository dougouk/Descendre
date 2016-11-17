package com.dan190.descendre.Util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Dan on 16/11/2016.
 */

public class Prefs {
    private static SharedPreferences prefs;

    public static void initialize(Context context){
        prefs = context.getSharedPreferences(context.getPackageName() + "_prefs", 0);
    }

    private static void checkPrefs(){
        if (prefs == null){
            throw new IllegalStateException("Preferences is not initialized");
        }
    }

    public static String getString(String key) {
        return getString(key, null);
    }

    public static String getString(String key, String fallback) {
        checkPrefs();
        return prefs.getString(key, fallback);
    }

    public static void putString(String key, String string) {
        checkPrefs();
        prefs.edit().putString(key, string).apply();
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static int getInt(String key, int fallback) {
        checkPrefs();
        return prefs.getInt(key, fallback);
    }

    public static void putInt(String key, int i) {
        checkPrefs();
        prefs.edit().putInt(key, i).apply();
    }

    public static float getFloat(String key) {
        return getFloat(key, 0f);
    }

    public static float getFloat(String key, float fallback) {
        checkPrefs();
        return prefs.getFloat(key, fallback);
    }

    public static void putFloat(String key, float f) {
        checkPrefs();
        prefs.edit().putFloat(key, f).apply();
    }

    public static long getLong(String key) {
        return getLong(key, 0);
    }

    public static long getLong(String key, long fallback) {
        checkPrefs();
        return prefs.getLong(key, fallback);
    }

    public static void putLong(String key, long l) {
        checkPrefs();
        prefs.edit().putLong(key, l).apply();
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean fallback) {
        checkPrefs();
        return prefs.getBoolean(key, fallback);
    }

    public static void putBoolean(String key, boolean b) {
        checkPrefs();
        prefs.edit().putBoolean(key, b).apply();
    }

    public static Set<String> getStringSet(String key) {
        return getStringSet(key, new HashSet<String>());
    }

    public static Set<String> getStringSet(String key, Set<String> fallback) {
        checkPrefs();
        return prefs.getStringSet(key, fallback);
    }

    public static void putStringSet(String key, Set<String> set) {
        checkPrefs();
        prefs.edit().putStringSet(key, set).apply();
    }

}
