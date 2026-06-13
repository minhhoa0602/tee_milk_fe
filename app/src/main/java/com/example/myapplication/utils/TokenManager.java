package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences("TeeMilkPrefs", Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString("USER_TOKEN", token).apply();
    }

    public String getToken() {

        //return prefs.getString("USER_TOKEN", null);

        // Copy nguyên chuỗi eyJhbGci... dán vào trong dấu ngoặc kép này
        return "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJob2FtaW5odnUwMDFAZ21haWwuY29tIiwiaWF0IjoxNzgxMjUwNTQwLCJleHAiOjE3ODEzMzY5NDB9.qE5YId13_NEUvMk_8aO3wG0U84BfzPXM6bl106IMQG0";
    }

    public void clearToken() {
        prefs.edit().remove("USER_TOKEN").apply();
    }
}
