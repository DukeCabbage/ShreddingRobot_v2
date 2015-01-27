package com.gloomy.ShreddingRobot.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;

public class BaseActivity extends Activity {

    protected SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        pref = getApplicationContext().getSharedPreferences("ShreddingSetting", MODE_PRIVATE);
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
