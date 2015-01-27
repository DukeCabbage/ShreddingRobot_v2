package com.gloomy.ShreddingRobot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.gloomy.ShreddingRobot.Utility.BaseActivity;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final String WECHAT_APP_ID = "wxbec3a7a47b19ed11";

    private Context _context;
    private LinearLayout mainStartBtn, historyBtn, settingBtn;

    boolean noEntryAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.i(TAG, "onCreate");
        _context = this;
        setContentView(R.layout.activity_main);

        findView();
        init();
        bindEvent();
    }

    private void findView() {
        mainStartBtn = (LinearLayout) findViewById(R.id.mainStartBtn);
        historyBtn = (LinearLayout) findViewById(R.id.historyBtn);
        settingBtn = (LinearLayout) findViewById(R.id.settingBtn);
    }

    private void init() {
        noEntryAnim = false;
    }

    private void bindEvent() {
        mainStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLeaveAnimation(0);
            }
        });
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLeaveAnimation(1);
            }
        });
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLeaveAnimation(2);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.i(TAG, "onResume");

        // until better solution is found
        // noEntryAnim prevents from frame skipping
        // which happens when the app is brought back from the background
        if (!noEntryAnim){
            startIntroAnimation();
        }
        noEntryAnim = true;
    }

    private void startIntroAnimation() {
        runOnUiThread(new Runnable(){
            public void run() {
                Animation entryAnim1 = AnimationUtils.loadAnimation(_context, R.anim.main_btn_enter);
                Animation entryAnim2 = AnimationUtils.loadAnimation(_context, R.anim.main_btn_enter);
                Animation entryAnim3 = AnimationUtils.loadAnimation(_context, R.anim.main_btn_enter);

                entryAnim1.setStartOffset(200l);
                entryAnim2.setStartOffset(300l);
                entryAnim3.setStartOffset(400l);

                mainStartBtn.startAnimation(entryAnim1);
                historyBtn.startAnimation(entryAnim2);
                settingBtn.startAnimation(entryAnim3);
            }
        });
    }

    private void startLeaveAnimation(final int index) {
        noEntryAnim = false;
        Animation leaveAnim1;
        Animation leaveAnim2;
        Animation leaveAnim3;

        if (index == 1) {
            leaveAnim1 = AnimationUtils.loadAnimation(_context, R.anim.main_btn_exit_up);
            leaveAnim2 = AnimationUtils.loadAnimation(_context, R.anim.main_btn_exit_up);
            leaveAnim3 = AnimationUtils.loadAnimation(_context, R.anim.main_btn_exit_up);
            leaveAnim2.setStartOffset(100l);
            leaveAnim3.setStartOffset(200l);
        }else{
            leaveAnim1 = AnimationUtils.loadAnimation(_context, R.anim.main_btn_exit);
            leaveAnim2 = AnimationUtils.loadAnimation(_context, R.anim.main_btn_exit);
            leaveAnim3 = AnimationUtils.loadAnimation(_context, R.anim.main_btn_exit);
            leaveAnim2.setStartOffset(300l);
            leaveAnim3.setStartOffset(600l);
        }

        leaveAnim3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if (index == 0) {
                    Intent intent = new Intent(_context, TrackingActivity.class);
                    startActivityWithNoExitAnim(intent);
                } else if (index == 1) {
                    Intent intent = new Intent(_context, HistoryActivity.class);
                    startActivityWithNoExitAnim(intent);
                } else {
                    Intent intent = new Intent(_context, SettingActivity.class);
                    startActivityWithNoExitAnim(intent);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        if (index == 0 || index == 1) {
            mainStartBtn.startAnimation(leaveAnim1);
            historyBtn.startAnimation(leaveAnim2);
            settingBtn.startAnimation(leaveAnim3);
        } else {
            settingBtn.startAnimation(leaveAnim1);
            historyBtn.startAnimation(leaveAnim2);
            mainStartBtn.startAnimation(leaveAnim3);
        }
    }
}