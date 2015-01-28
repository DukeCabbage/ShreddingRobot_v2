package com.gloomy.ShreddingRobot;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.RelativeLayout;

import com.gloomy.ShreddingRobot.Dao.DBTrack;
import com.gloomy.ShreddingRobot.Dao.DBTrackDao;
import com.gloomy.ShreddingRobot.Utility.BaseFragmentActivity;
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
    private static final int SENSOR_UPDATE_TIME_IN_MILLISECONDS = 10;
    private static final int ALTITUDE_AVERAGING_QUEUE_SIZE = 20;
    private static final int AUTOOFF_ALTI_THRESHOLD = 200;
    private static final int AUTOOFF_TIME_THRESHOLD = 1800*1000;

    private DecimalFormat sig3 = new DecimalFormat("@@@");
    private DecimalFormat sig2 = new DecimalFormat("@@");
    private DecimalFormat dff = new DecimalFormat("0.00");

    boolean tracking;
    boolean freeFalling;
    boolean noGraSensor;

    private DaoManager daoManager;
    private DBTrackDao trackDao;
    public DBTrack curTrack;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    private SensorManager mSensorManager;
    private Sensor mGraSensor;
    private Sensor mAccSensor;

    private Timer mTimer;
    private MyTimerTask mTimerTask;
    private int autoOff_countDown;
    private int veloUnit, toggleOff, toggleAlti;
    private double[] graReading;
    private double[] accReading;
    private double graMag, accMag;
    private double countTime, maxAirTime;
    private double curSpeed, maxSpeed;
    private ArrayBlockingQueue<Double> rawAltData;
    private double curAltitude, altitude_min;
    public Button sensorSwitchBtn;
    public MeinTextView tv_airTimeTimer, tv_maxAirTimeTimer, tv_altitude;

    private RelativeLayout speedGaugeLayout;
    private MeinTextView speedTV;
    private CustomGauge speedGauge;

    private Animation animFadeIn, animFadeOut, scaleEnter, scaleExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        findView();
        init();
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
        speedTV.setText("Speed");

        tv_airTimeTimer = (MeinTextView) findViewById(R.id.tv_airTimeTimer);
        tv_maxAirTimeTimer = (MeinTextView) findViewById(R.id.tv_maxAirTimeTimer);
        tv_altitude = (MeinTextView) findViewById(R.id.tv_altitude);

        sensorSwitchBtn = (Button) findViewById(R.id.sensorSwitchBtn);
    }

    private void init() {
        tracking = false;
        freeFalling = false;
        noGraSensor = false;
        rawAltData = new ArrayBlockingQueue<Double> (ALTITUDE_AVERAGING_QUEUE_SIZE);

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        LocationFrag mLocationFrag = new LocationFrag();
        fragmentTransaction.add(mLocationFrag, "mLocationFrag");
        fragmentTransaction.commit();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGraSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (mGraSensor == null) {
            noGraSensor = true;
        }
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        graReading = new double[3];
        accReading = new double[3];

        daoManager = DaoManager.getInstance(_context);
        trackDao = daoManager.getDBTrackDao(DaoManager.TYPE_WRITE);

        animFadeIn = AnimationUtils.loadAnimation(_context, R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(_context, R.anim.fade_out);
        scaleEnter = AnimationUtils.loadAnimation(_context, R.anim.scale_enter);
        scaleExit = AnimationUtils.loadAnimation(_context, R.anim.scale_exit);
    }

    @Override
    public void onBackPressed() {

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
    }

    private void bindEvent() {

        scaleEnter.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                sensorSwitchBtn.setOnClickListener(stopBtnListener);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        scaleExit.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                Intent i = new Intent(_context, TrackResultActivity.class);
                i.putExtra("TRACK_OBJECT" , curTrack);
                startActivity(i);
                finish();
                overridePendingTransition(0, 0);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        sensorSwitchBtn.startAnimation(scaleEnter);
    }

    private View.OnClickListener stopBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopTracking();
            sensorSwitchBtn.startAnimation(scaleExit);
        }
    };

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
                    sensorSwitchBtn.performClick();
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
        resetCountTime();
        tv_airTimeTimer.setText(dff.format(0.0) + " s");
        tv_maxAirTimeTimer.setText(dff.format(0.0) + " s");
        destroyTimer();
        mTimer = new Timer();
        mTimerTask = new MyTimerTask();
        mTimer.scheduleAtFixedRate(mTimerTask, 1000, SENSOR_UPDATE_TIME_IN_MILLISECONDS);
        autoOff_countDown = AUTOOFF_TIME_THRESHOLD*toggleOff;

        //Location fragment
        ((LocationFrag) fragmentManager.findFragmentByTag("mLocationFrag")).startTracking();
        altitude_min = 999999;

        //Initialize DBTrack object
        Random rg = new Random();
        long id = (long) (rg.nextDouble() * 999999);
        curTrack = new DBTrack(id);
        curTrack.setDate(new Date());
    }

    private void stopTracking() {
//        Log.e(TAG, "stop tracking");
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
        curTrack.setMaxAirTime(maxAirTime);
        curTrack.setMaxSpeed(maxSpeed);
    }

    private void destroyTimer() {
        if (mTimer != null) {
            mTimerTask.cancel();
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void resetCountTime() {
//		Log.e(TAG, "reset time");
        if (countTime > maxAirTime) {
            maxAirTime = countTime;
            tv_maxAirTimeTimer.setText(dff.format(maxAirTime) + " s");
        }
        countTime = 0.00;
//        tv_airTimeTimer.setText(dff.format(countTime) + " s");
    }

    class MyTimerTask extends TimerTask {
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
//            Log.e(TAG, "no altitude reading");
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
                        sensorSwitchBtn.performClick();
                    }
                }
                tv_altitude.setText(sig3.format(curAltitude) + " m");
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
}
