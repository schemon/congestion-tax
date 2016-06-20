package com.example.simarv.toll.db;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by simarv on 2016-06-15.
 */
public class TransitionHistory {
	private final Context context;

	public TransitionHistory(Context context) {
		this.context = context.getApplicationContext();
	}

	public void setLastTransition(String transitionName) {
		getSharedPreferences().edit().putString("last_transition", transitionName).apply();
	}

	public String getLastTransition() {
		return getSharedPreferences().getString("last_transition", "UnknownTransition");
	}

	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences("transition_history", Context.MODE_PRIVATE);
	}

}
