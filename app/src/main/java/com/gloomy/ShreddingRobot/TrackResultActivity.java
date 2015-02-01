package com.gloomy.ShreddingRobot;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.gloomy.ShreddingRobot.Dao.DBTrack;
import com.gloomy.ShreddingRobot.Dao.DBTrackDao;
import com.gloomy.ShreddingRobot.Utility.BaseActivity;
import com.gloomy.ShreddingRobot.Utility.Constants;
import com.gloomy.ShreddingRobot.Utility.DaoManager;
import com.gloomy.ShreddingRobot.Widget.CustomGauge;
import com.gloomy.ShreddingRobot.Widget.MeinTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

public class TrackResultActivity extends BaseActivity {

    private final String TAG = "TrackResultActivity";
    private static final int GAUGE_STEP_TIME = 40;
    private int veloUnit;

    private DaoManager daoManager;
    private DBTrackDao trackDao;
    public DBTrack curTrack;
    private double maxSpeed, maxAirTime;

    private RelativeLayout speedLayout;
    private CustomGauge speedGauge, timeGauge;
    private MeinTextView speedTV, timeTV;
//    private MeinTextView delete_btn,save_btn;

    private final DecimalFormat speedDF = new DecimalFormat("@@@");
    private final DecimalFormat airTimeDF = new DecimalFormat("0000.");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_result);
        curTrack = getIntent().getParcelableExtra("TRACK_OBJECT");

        maxSpeed = curTrack.getMaxSpeed();
        maxAirTime = curTrack.getMaxAirTime();

        findView();
        init();
        bindEvent();
    }

    private void findView() {
//        delete_btn = (MeinTextView) findViewById(R.id.delete_btn);
//        save_btn = (MeinTextView) findViewById(R.id.save_btn);
//        delete_btn.setText(Constants.ICON_DELETE);
//        save_btn.setText(Constants.ICON_SAVE);

        speedLayout = (RelativeLayout) findViewById(R.id.maxSpeedGaugeLayout);

        speedGauge = (CustomGauge) findViewById(R.id.maxSpeedGauge);
        timeGauge = (CustomGauge) findViewById(R.id.airTimeGauge);
        speedTV = (MeinTextView) findViewById(R.id.maxSpeedTV);
        timeTV = (MeinTextView) findViewById(R.id.airTimeTV);

//        save_btn.setVisibility(View.GONE);
//        delete_btn.setVisibility(View.GONE);
        speedGauge.setVisibility(View.GONE);
        timeGauge.setVisibility(View.GONE);
    }

    private void init() {
        daoManager = DaoManager.getInstance(_context);
        trackDao = daoManager.getDBTrackDao(DaoManager.TYPE_WRITE);
        veloUnit = pref.getInt("VELOCITY_UNIT", 0);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startEntryAnimation();
            }
        }, _context.getResources().getInteger(R.integer.activity_enter_anim));
    }

    private void bindEvent() {
        speedGauge.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startEntryAnimation();
            }
        });
    }

    private Uri SaveImage(Bitmap bitmap) {
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        String filename = "weChatMomentShare.png";
        File file = new File (storageDir, filename);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Uri.fromFile(file);
    }

    private Bitmap getBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }

    private void shareToAll(Uri fileUri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        startActivity(intent);
    }
    private void startEntryAnimation(){
//        Log.e(TAG, "startEntryAnimaiton");

        AnimatorSet zoomIn1 = (AnimatorSet) AnimatorInflater.loadAnimator(_context,
                R.animator.zoom_in_center);
        zoomIn1.setTarget(speedGauge);
        zoomIn1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                speedTV.setVisibility(View.GONE);
                speedGauge.setVisibility(View.VISIBLE);
                speedGauge.setAlpha(0.0f);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                speedTV.setVisibility(View.VISIBLE);
                switch(veloUnit) {
                    case 0:
                        speedTV.setText("0.0km/h");
                        startGaugeAnimation(speedGauge, speedTV, maxSpeed * 10, "km/h");
                        break;
                    case 1:
                        speedTV.setText("0.0m/s");
                        startGaugeAnimation(speedGauge, speedTV, maxSpeed * 10, "m/s");
                        break;
                    case 2:
                        speedTV.setText("0.0mi/h");
                        startGaugeAnimation(speedGauge, speedTV, maxSpeed * 10, "mi/h");
                }
            }
        });
        AnimatorSet zoomIn2 = (AnimatorSet) AnimatorInflater.loadAnimator(_context,
                R.animator.zoom_in_center);
        zoomIn2.setTarget(timeGauge);
        zoomIn2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                timeTV.setVisibility(View.GONE);
                timeGauge.setVisibility(View.VISIBLE);
                timeGauge.setAlpha(0.0f);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                timeTV.setVisibility(View.VISIBLE);
                timeTV.setText("0ms");
                startGaugeAnimation(timeGauge, timeTV, maxAirTime*1000, "ms");
            }
        });
        zoomIn1.setStartDelay(500l);
        zoomIn1.start();
        zoomIn2.start();
    }

    private void startGaugeAnimation(final CustomGauge gauge, final MeinTextView tv,
                                     final double endValue, final String unit){
        final int stepTime;
        stepTime = GAUGE_STEP_TIME*100/gauge.getEndValue();

        final double textValueMultiplier;
        if (unit == "km/h") {
            textValueMultiplier = 0.36;
        } else if (unit == "mi/h") {
            textValueMultiplier = 0.2236;
        } else if(unit == "m/s") {
            textValueMultiplier = 0.1;
        } else {
            textValueMultiplier = 1;
        }

        new Thread() {
            int step;
            public void run() {
                for (step = 0; step < endValue; step++) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                gauge.setValue(step, (int) endValue);
                                tv.setText(speedDF.format(textValueMultiplier*step)+unit);
                            }
                        });
                        Thread.sleep(stepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
