package com.example.safezone;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import java.util.HashSet;
import java.util.Set;

public class WhitelistManager {
    private static final String PREF = "whitelist_prefs";
    private static final String KEY_SET = "whitelist_set";
    private static WhitelistManager instance;
    private final SharedPreferences prefs;

    private WhitelistManager(@NonNull Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static WhitelistManager getInstance(@NonNull Context ctx) {
        if (instance == null) instance = new WhitelistManager(ctx);
        return instance;
    }

    public boolean isWhitelisted(String normalizedNumber) {
        Set<String> set = prefs.getStringSet(KEY_SET, new HashSet<>());
        return set.contains(normalizedNumber);
    }

    public void add(String normalizedNumber) {
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_SET, new HashSet<>()));
        set.add(normalizedNumber);
        prefs.edit().putStringSet(KEY_SET, set).apply();
    }

    public void remove(String normalizedNumber) {
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_SET, new HashSet<>()));
        set.remove(normalizedNumber);
        prefs.edit().putStringSet(KEY_SET, set).apply();
    }
}
