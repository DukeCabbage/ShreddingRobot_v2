package com.gloomy.ShreddingRobot;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gloomy.ShreddingRobot.Utility.BaseActivity;
import com.gloomy.ShreddingRobot.Utility.BitmapWorkerTask;

import java.io.File;

public class SettingActivity extends BaseActivity {
    private static final String TAG = "SettingActivity";
    static final int REQUEST_TAKE_PHOTO  = 1;

    SharedPreferences.Editor editor;
    private Context _context;

    private int veloUnitToggle;
    public TextView[] veloUnit = new TextView[3];
    private int timeOffToggle;
    public TextView[] timeOff = new TextView[3];
    private int altiOffToggle;
    public TextView[] altiOff = new TextView[3];

    private ImageView profilePhoto;
    private Uri photoUri;
    private String photoPath;
    private int profileHeight, profileWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        _context = this;

        editor = pref.edit();

        findView();
        bindEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileImage();

        veloUnitToggle = pref.getInt("VELOCITY_UNIT", 0);
        timeOffToggle = pref.getInt("AUTO_TIME_OFF", 0);
        altiOffToggle = pref.getInt("AUTO_ALTI_OFF", 0);

        for (int i = 0; i < veloUnit.length; i++){
            if (veloUnitToggle == i){
                veloUnit[i].setVisibility(View.VISIBLE);
            } else {
                veloUnit[i].setVisibility(View.GONE);
            }
        }

        for (int i = 0; i < timeOff.length; i++){
            if (timeOffToggle == i){
                timeOff[i].setVisibility(View.VISIBLE);
            } else {
                timeOff[i].setVisibility(View.GONE);
            }
        }

        for (int i = 0; i < altiOff.length; i++){
            if (altiOffToggle == i){
                altiOff[i].setVisibility(View.VISIBLE);
            } else {
                altiOff[i].setVisibility(View.GONE);
            }
        }
    }

    private void findView() {
        veloUnit[0] = (TextView) findViewById(R.id.setToKmH);
        veloUnit[1] = (TextView) findViewById(R.id.setToMS);
        veloUnit[2] = (TextView) findViewById(R.id.setToMpH);

        timeOff[0] = (TextView) findViewById(R.id.timeOp0);
        timeOff[1] = (TextView) findViewById(R.id.timeOp1);
        timeOff[2] = (TextView) findViewById(R.id.timeOp2);

        altiOff[0] = (TextView) findViewById(R.id.altiOp0);
        altiOff[1] = (TextView) findViewById(R.id.altiOp1);
        altiOff[2] = (TextView) findViewById(R.id.altiOp2);

        profilePhoto = (ImageView) findViewById(R.id.profilePhoto);
    }

    private void bindEvent() {

        for (int i=0; i<veloUnit.length; i++){
            veloUnit[i].setOnClickListener(veloUnitOnClickListener);
        }

        for (int i=0; i<timeOff.length; i++){
            timeOff[i].setOnClickListener(timeOffOnClickListener);
        }

        for (int i=0; i<altiOff.length; i++){
            altiOff[i].setOnClickListener(altiOffOnClickListener);
        }

        profilePhoto.setOnLongClickListener(profilePhotoOnLongClickListener);
        profilePhoto.setOnClickListener(profilePhotoOnClickListener);
    }

    private View.OnClickListener veloUnitOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int oldOp, newOp;
            oldOp= veloUnitToggle;
            switch (oldOp){
                case 0:
                    newOp = 1;
                    break;
                case 1:
                    newOp = 2;
                    break;
                case 2:
                    newOp = 0;
                    break;
                default:
                    newOp = 0;
                    break;
            }
            AnimatorSet flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(_context,
                    R.animator.card_flip_top_out);
            flipOut.setTarget(veloUnit[oldOp]);
            flipOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    for (int i=0; i<veloUnit.length; i++){
                        veloUnit[i].setEnabled(false);
                    }
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    for (int i=0; i<veloUnit.length; i++){
                        veloUnit[i].setEnabled(true);
                    }
                    veloUnit[oldOp].setVisibility(View.GONE);
                }
            });
            flipOut.start();

            AnimatorSet flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(_context,
                    R.animator.card_flip_top_in);
            veloUnit[newOp].setVisibility(View.VISIBLE);
            flipIn.setTarget(veloUnit[newOp]);
            editor.putInt("VELOCITY_UNIT", newOp);
            veloUnitToggle = newOp;

            flipIn.start();
            editor.commit();
        }
    };

    private View.OnClickListener timeOffOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int oldOp, newOp;
            oldOp= timeOffToggle;
            switch (oldOp){
                case 0:
                    newOp = 1;
                    break;
                case 1:
                    newOp = 2;
                    break;
                case 2:
                    newOp = 0;
                    break;
                default:
                    newOp = 0;
                    break;
            }
            AnimatorSet flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(_context,
                    R.animator.card_flip_right_out);
            flipOut.setTarget(timeOff[oldOp]);
            flipOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    for (int i=0; i<timeOff.length; i++){
                        timeOff[i].setEnabled(false);
                    }
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    for (int i=0; i<timeOff.length; i++){
                        timeOff[i].setEnabled(true);
                    }
                    timeOff[oldOp].setVisibility(View.GONE);
                }
            });
            flipOut.start();

            AnimatorSet flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(_context,
                    R.animator.card_flip_right_in);
            timeOff[newOp].setVisibility(View.VISIBLE);
            flipIn.setTarget(timeOff[newOp]);
            editor.putInt("AUTO_TIME_OFF", newOp);
            timeOffToggle = newOp;

            flipIn.start();
            editor.commit();
        }
    };

    private View.OnClickListener altiOffOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int oldOp, newOp;
            oldOp= altiOffToggle;
            switch (oldOp){
                case 0:
                    newOp = 1;
                    break;
                case 1:
                    newOp = 2;
                    break;
                case 2:
                    newOp = 0;
                    break;
                default:
                    newOp = 0;
                    break;
            }
            AnimatorSet flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(_context,
                    R.animator.card_flip_right_out);
            flipOut.setTarget(altiOff[oldOp]);
            flipOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    for (int i=0; i<altiOff.length; i++){
                        altiOff[i].setEnabled(false);
                    }
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    for (int i=0; i<altiOff.length; i++){
                        altiOff[i].setEnabled(true);
                    }
                    altiOff[oldOp].setVisibility(View.GONE);
                }
            });
            flipOut.start();

            AnimatorSet flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(_context,
                    R.animator.card_flip_right_in);
            altiOff[newOp].setVisibility(View.VISIBLE);
            flipIn.setTarget(altiOff[newOp]);
            editor.putInt("AUTO_ALTI_OFF", newOp);
            altiOffToggle = newOp;

            flipIn.start();
            editor.commit();
        }
    };

    private View.OnLongClickListener profilePhotoOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            photoPath = pref.getString("PROFILE_PHOTO_PATH", null);
            if (photoPath == null) {
                return false;
            } else {
                profilePhoto.setImageResource(R.drawable.profile_placeholder);
                editor.remove("PROFILE_PHOTO_PATH");
                editor.commit();
                return true;
            }
        }
    };

    private View.OnClickListener profilePhotoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (intent.resolveActivity(getPackageManager()) != null) {
                // Create a file, to which the photo saves
                photoUri = createImageFileUri();

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            } else {
                Log.e(TAG, "Failed to create camera intent");
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                profilePhoto.setImageResource(R.drawable.profile_loading);

                photoPath = photoUri.getPath();
                BitmapWorkerTask task = new BitmapWorkerTask(profilePhoto, photoPath, profileHeight, profileWidth);
                task.execute();

                editor.putString("PROFILE_PHOTO_PATH", photoPath);
                editor.commit();
            }
        }
    }

    private Uri createImageFileUri() {
        // Create an image file name[
        String imageFileName = "profile";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return Uri.fromFile(new File (storageDir, imageFileName));
    }

    private void loadProfileImage() {
        photoPath = pref.getString("PROFILE_PHOTO_PATH", null);
        profileHeight = (int) getResources().getDimension(R.dimen.profile_photo_size);
        profileWidth = (int) getResources().getDimension(R.dimen.profile_photo_size);

        if (photoPath==null) {
            // Default placeholder will be shown
        } else {
            BitmapWorkerTask task = new BitmapWorkerTask(profilePhoto, photoPath, profileHeight, profileWidth);
            task.execute();
        }
    }
}