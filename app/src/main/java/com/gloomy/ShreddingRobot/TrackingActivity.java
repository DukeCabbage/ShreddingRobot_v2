package com.gloomy.ShreddingRobot;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gloomy.ShreddingRobot.Dao.DBTrack;
import com.gloomy.ShreddingRobot.Dao.DBTrackDao;
import com.gloomy.ShreddingRobot.Utility.BaseFragmentActivity;
import com.gloomy.ShreddingRobot.Utility.Constants;
import com.gloomy.ShreddingRobot.Utility.DaoManager;
import com.gloomy.ShreddingRobot.Widget.CustomGauge;
import com.gloomy.ShreddingRobot.Widget.MeinTextView;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;


public class TrackingActivity extends BaseFragmentActivity {
    private static final String TAG = "TrackingActivity";
    private static final int SENSOR_UPDATE_TIME_IN_MILLISECONDS = 50;
    private static final int TRACK_TIMER_INTERVAL_IN_MINUTES = 1;
    private static final int TRACK_TIMER_INTERVAL_IN_MILLISECONDS = TRACK_TIMER_INTERVAL_IN_MINUTES*60*1000;
    private static final int ALTITUDE_AVERAGING_QUEUE_SIZE = 20;
    private static final int AUTOOFF_ALTI_THRESHOLD = 200;
    private static final int AUTOOFF_TIME_THRESHOLD = 1800*1000;

    private DecimalFormat sig3 = new DecimalFormat("@@@");
    private DecimalFormat sig2 = new DecimalFormat("@@");
    private DecimalFormat dff = new DecimalFormat("0.00");

    boolean tracking;
    boolean freeFalling;
    boolean noGraSensor;

    private DBTrackDao trackDao;
    public DBTrack curTrack;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    private SensorManager mSensorManager;
    private Sensor mGraSensor;
    private Sensor mAccSensor;

    private Timer sensorTimer, trackTimer;
    private SensorTimerTask sensorTimerTask;
    private TrackTimerTask trackTimerTask;
    private int autoOff_countDown;
    private int veloUnit, toggleOff, toggleAlti;
    private double[] graReading;
    private double[] accReading;
    private double graMag, accMag;

    private Date trackDate;
    private int trackLength;
    private double countTime, maxAirTime;
    private double curSpeed, maxSpeed;
    private ArrayBlockingQueue<Double> rawAltData;
    private double curAltitude, altitude_min;

    private RelativeLayout speedGaugeLayout;
    private MeinTextView speedTV;
    private CustomGauge speedGauge;

    private MeinTextView tv_trackLength;
    private MeinTextView tv_airTimeTimer, tv_maxAirTimeTimer;

    private RelativeLayout stopBtnLayout;
    private Button stopBtn;

    private LinearLayout btnLayout;
    private MeinTextView repeatBtn, continueBtn;

    private Animation scaleEnter, scaleExit;
    private AnimatorSet btnEntry1, btnEntry2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        findView();
        initView();
        init();
        setupAnimation();
        bindEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        veloUnit = pref.getInt("VELOCITY_UNIT", 0);
        toggleOff = pref.getInt("AUTO_OFF", 0);
        toggleAlti = pref.getInt("ALTI_OFF", 0);
        if (!tracking) {
            startTracking();
        }
    }

    private void findView() {
        speedGaugeLayout = (RelativeLayout) findViewById(R.id.speedGaugeLayout);
        speedGauge = (CustomGauge) speedGaugeLayout.findViewById(R.id.speedGauge);
        speedTV = (MeinTextView) speedGaugeLayout.findViewById(R.id.speedTV);

        tv_trackLength = (MeinTextView) findViewById(R.id.tv_trackLengthTimer);
        tv_airTimeTimer = (MeinTextView) findViewById(R.id.tv_airTimeTimer);
        tv_maxAirTimeTimer = (MeinTextView) findViewById(R.id.tv_maxAirTimeTimer);

        stopBtn = (Button) findViewById(R.id.sensorSwitchBtn);
        stopBtnLayout = (RelativeLayout) stopBtn.getParent();

        btnLayout = (LinearLayout) findViewById(R.id.btn_layout);
        repeatBtn = (MeinTextView) findViewById(R.id.repeat_btn);
        continueBtn = (MeinTextView) findViewById(R.id.continue_btn);
    }

    private void initView() {
        speedTV.setText("Speed");

        btnLayout.setVisibility(View.GONE);
        repeatBtn.setText(Constants.ICON_REPEAT);
        continueBtn.setText(Constants.ICON_ARROW_RIGHT);
    }

    private void init() {
        tracking = false;
        freeFalling = false;
        noGraSensor = false;
        rawAltData = new ArrayBlockingQueue<> (ALTITUDE_AVERAGING_QUEUE_SIZE);

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        LocationFrag mLocationFrag = new LocationFrag();
        fragmentTransaction.add(mLocationFrag, "mLocationFrag");
        fragmentTransaction.commit();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGraSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        noGraSensor = mGraSensor == null;
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        graReading = new double[3];
        accReading = new double[3];

        DaoManager daoManager = DaoManager.getInstance(_context);
        trackDao = daoManager.getDBTrackDao(DaoManager.TYPE_WRITE);
    }

    private void bindEvent() {
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTracking();
                stopBtn.startAnimation(scaleExit);
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackDao.insert(curTrack);
                btnLayout.setVisibility(View.GONE);
                startTracking();
            }
        });
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackDao.insert(curTrack);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (tracking) {
            AlertDialog.Builder builder = new AlertDialog.Builder(_context);
            builder.setTitle(_context.getString(R.string.quit_tracking_title));
            builder.setMessage(_context.getString(R.string.quit_tracking_message))
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            stopTracking();
                            TrackingActivity.this.finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
//        } else {
            //TODO: Implement resume function
        }
    }

    SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            // In this example, alpha is calculated as t / (t + dT),
            // where t is the low-pass filter's time-constant and
            // dT is the event delivery rate.

            final double alpha = 0.7;
//            noGraSensor = true;

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accReading[0] = event.values[0];
                accReading[1] = event.values[1];
                accReading[2] = event.values[2];
                accMag = Math.sqrt(Math.pow(accReading[0], 2) + Math.pow(accReading[1], 2) + Math.pow(accReading[2], 2));

                if(noGraSensor){
                    // Isolate the force of gravity with the low-pass filter.
                    graReading[0] = alpha * graReading[0] + (1 - alpha) * accReading[0];
                    graReading[1] = alpha * graReading[1] + (1 - alpha) * accReading[1];
                    graReading[2] = alpha * graReading[2] + (1 - alpha) * accReading[2];
                    graMag = Math.sqrt(Math.pow(graReading[0], 2) + Math.pow(graReading[1], 2) + Math.pow(graReading[2], 2));
                }
            }

            if (!noGraSensor&&event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                graReading[0] = event.values[0];
                graReading[1] = event.values[1];
                graReading[2] = event.values[2];
                graMag = Math.sqrt(Math.pow(graReading[0], 2) + Math.pow(graReading[1], 2) + Math.pow(graReading[2], 2));
            }

//            if (noGraSensor) {
//                if (accMag < 3.0) {
//                    freeFalling = true;
//                } else {
//                    if (freeFalling) {
//                        resetCountTime();
//                    }
//                    freeFalling = false;
//                }
//            } else {}

            double proj = 0.0;
            for (int axis = 0; axis < 3; axis++) {
                proj += accReading[axis] * graReading[axis];
            }
//            Log.e(TAG, "proj: "+proj);
            if (proj/graMag < 3.0) {
                freeFalling = true;
            } else {
                if (freeFalling) {
                    resetCountTime();
                }
                freeFalling = false;
            }

            if(toggleOff != 0) {
                if (autoOff_countDown == 0) {
                    stopBtn.performClick();
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private void startTracking() {
        Log.e(TAG, "start tracking");
        //Motion Sensor
        if (!noGraSensor) {
            mSensorManager.registerListener(mSensorListener, mGraSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        mSensorManager.registerListener(mSensorListener, mAccSensor, SensorManager.SENSOR_DELAY_NORMAL);
        tracking = true;

        //Timer
        trackDate = new Date();
        trackLength = 0;
        countTime = 0.0;
        maxAirTime = 0.0;

        tv_trackLength.setText(trackLength + " min");
        tv_airTimeTimer.setText(dff.format(0.0) + " s");
        tv_maxAirTimeTimer.setText(dff.format(0.0) + " s");
        destroyTimer();
        trackTimer = new Timer();
        trackTimerTask = new TrackTimerTask();
        trackTimer.scheduleAtFixedRate(trackTimerTask, TRACK_TIMER_INTERVAL_IN_MILLISECONDS, TRACK_TIMER_INTERVAL_IN_MILLISECONDS);

        sensorTimer = new Timer();
        sensorTimerTask = new SensorTimerTask();
        sensorTimer.scheduleAtFixedRate(sensorTimerTask, 1000, SENSOR_UPDATE_TIME_IN_MILLISECONDS);
        autoOff_countDown = AUTOOFF_TIME_THRESHOLD*toggleOff;

        //Location fragment
        ((LocationFrag) fragmentManager.findFragmentByTag("mLocationFrag")).startTracking();
        altitude_min = 999999;

        //Initialize DBTrack object
        Random rg = new Random();
        long id = (long) (rg.nextDouble() * 999999);
        curTrack = new DBTrack(id);
        curTrack.setDate(new Date());

        // Animate stop button enter
        startTrackAnim();
    }

    private void stopTracking() {
        //Motion Sensor
        mSensorManager.unregisterListener(mSensorListener);

        //Timer
        destroyTimer();

        //Location client
        ((LocationFrag) fragmentManager.findFragmentByTag("mLocationFrag")).stopTracking();

        tracking = false;
        summarizeTrack();
    }

    private void summarizeTrack() {
        curTrack.setDate(trackDate);
        curTrack.setMaxAirTime(maxAirTime);
        curTrack.setMaxSpeed(maxSpeed);
    }

    private void destroyTimer() {
        if (sensorTimer != null) {
            sensorTimerTask.cancel();
            sensorTimer.cancel();
            sensorTimer = null;
        }
        if (trackTimer != null) {
            trackTimerTask.cancel();
            trackTimer.cancel();
            trackTimer = null;
        }
    }

    private void resetCountTime() {
//		Log.e(TAG, "reset time");
        if (countTime > maxAirTime) {
            maxAirTime = countTime;
            tv_maxAirTimeTimer.setText(dff.format(maxAirTime) + " s");
        }
        countTime = 0.0;
    }

    class SensorTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (freeFalling) {
                        countTime += ((double) SENSOR_UPDATE_TIME_IN_MILLISECONDS) / 1000.0;
                        tv_airTimeTimer.setText(dff.format(countTime) + " s");
                    }
                    autoOff_countDown -= SENSOR_UPDATE_TIME_IN_MILLISECONDS;
                }
            });
        }
    }

    class TrackTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    trackLength += TRACK_TIMER_INTERVAL_IN_MINUTES;
                    tv_trackLength.setText(trackLength + " min");
                }
            });
        }
    }

    protected void updateSpeed(double newSpeed, double accuracy) {
//        Random rand = new Random();
        if (accuracy > 20) {
            curSpeed = 0.0;
        } else {
            curSpeed = newSpeed;

//            curSpeed = (double) rand.nextInt(50);
            // Update max speed
            if (curSpeed > maxSpeed) {
                maxSpeed = curSpeed;
            }
//            updateGauge(speedGauge, speedTV, curSpeed, maxSpeed);
            Log.e(TAG, "curSpeed: "+curSpeed);
            updateGauge(speedGauge, speedTV, curSpeed * 10.0, maxSpeed * 10.0);

        }
    }

    protected void updateAltimeter(double newAltitude) {
//		Log.e(TAG, "" + newAltitude);
        if (newAltitude == 0.0) {
            Log.e(TAG, "no altitude reading");
        } else {
            curAltitude *= ALTITUDE_AVERAGING_QUEUE_SIZE;
            boolean stabilizedAlt = rawAltData.size() == ALTITUDE_AVERAGING_QUEUE_SIZE;
            if (stabilizedAlt) {
                curAltitude -= rawAltData.poll();
                curAltitude += newAltitude;
                rawAltData.add(newAltitude);

            } else {
                curAltitude += newAltitude;
                rawAltData.add(newAltitude);
            }
            curAltitude /= ALTITUDE_AVERAGING_QUEUE_SIZE;

            if (stabilizedAlt){
                if (curAltitude < altitude_min) {
                    altitude_min = curAltitude;
                } else if (curAltitude > (altitude_min+AUTOOFF_ALTI_THRESHOLD*toggleAlti)) {

                    if(toggleAlti != 0) {
                        stopBtn.performClick();
                    }
                }
//                tv_altitude.setText(sig3.format(curAltitude) + " m");
            }
        }
    }

    private void updateGauge(final CustomGauge gauge, final MeinTextView tv,
                             final double curValue, final double maxValue) {
        final int updateTime = LocationFrag.UPDATE_INTERVAL_IN_MILLISECONDS;
        final int steps = 50;
        final int preValue = gauge.getValue();
        final double increment = (curValue - preValue)/steps;
        final int stepTime = updateTime/steps;

        new Thread() {
            int step;
            public void run() {
                for (step = steps; step >=0; step--) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int stepValue = (int) (curValue - step*increment);
                            gauge.setValue(stepValue, (int) maxValue);
                            switch(veloUnit) {
                                case 1:
                                    tv.setText(sig3.format(0.1*stepValue)+"m/s");
                                    break;
                                case 2:
                                    tv.setText(sig3.format(0.2236*stepValue)+"mi/h");
                                    break;
                                default:
                                    tv.setText(sig3.format(0.36*stepValue)+"km/h");
                                    break;
                            }
                        }
                    });
                    try {
                        Thread.sleep(stepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void setupAnimation() {
        // Stop button entry animation, setup onClickListener
        scaleEnter = AnimationUtils.loadAnimation(_context, R.anim.scale_enter);
        scaleEnter.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                stopBtn.setClickable(false);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                stopBtn.setClickable(true);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        // Stop button exit animation
        scaleExit = AnimationUtils.loadAnimation(_context, R.anim.scale_exit);
        scaleExit.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                stopTrackAnim();
                stopBtnLayout.setVisibility(View.GONE);
                btnLayout.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        // Result option 1 entering from bottom
        btnEntry1 = (AnimatorSet) AnimatorInflater.loadAnimator(_context,
                R.animator.result_btn_enter);
        btnEntry1.setTarget(repeatBtn);
        btnEntry1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                repeatBtn.setVisibility(View.VISIBLE);
                repeatBtn.setAlpha(0.0f);
            }
        });

        // Result option 2 entering from bottom lagging behind
        btnEntry2 = (AnimatorSet) AnimatorInflater.loadAnimator(_context,
                R.animator.result_btn_enter);
        btnEntry2.setTarget(continueBtn);
        btnEntry2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                continueBtn.setVisibility(View.VISIBLE);
                continueBtn.setAlpha(0.0f);
            }
        });
        btnEntry2.setStartDelay(500l);
    }

    private void startTrackAnim() {
        Log.e(TAG, "startTrackAnim");
        stopBtnLayout.setVisibility(View.VISIBLE);
        stopBtn.startAnimation(scaleEnter);
    }

    private void stopTrackAnim(){
        Log.e(TAG, "stopTrackAnim");
        btnEntry1.start();
        btnEntry2.start();
    }
}
