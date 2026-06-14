package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.api.RetrofitClient;

public class TokenManager {
    private SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences("TeeMilkPrefs", Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString("USER_TOKEN", token).apply();
    }

    public String getToken() {
        return prefs.getString("USER_TOKEN", null);
    }

    public void clearToken() {
        prefs.edit().remove("USER_TOKEN").apply();
        RetrofitClient.reset();
    }
}
