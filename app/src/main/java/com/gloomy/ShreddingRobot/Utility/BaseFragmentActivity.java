package com.gloomy.ShreddingRobot.Utility;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;


public class BaseFragmentActivity extends FragmentActivity {

    protected SharedPreferences pref;
    protected Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        pref = getApplicationContext().getSharedPreferences("ShreddingSetting", MODE_PRIVATE);
        _context = this;
    }

    public void startActivityWithNoExitAnim(Intent intent) {
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
    }
}
