/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gloomy.ShreddingRobot.Widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.gloomy.ShreddingRobot.Dao.DBTrack;
import com.gloomy.ShreddingRobot.Dao.DBTrackDao;
import com.gloomy.ShreddingRobot.R;
import com.gloomy.ShreddingRobot.Utility.Constants;
import com.gloomy.ShreddingRobot.Utility.DaoManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CustomArrayAdapter extends ArrayAdapter<DBTrack>{

    private static final String TAG = "CustomArrayAdapter";
    private Context _context;
    private SharedPreferences _pref;

    private LayoutInflater inflater;
    private List<DBTrack> objects;

    HashMap<Long, Boolean> mStaMap = new HashMap<Long, Boolean>();
    HashMap<Integer, Boolean> mIniMap = new HashMap<Integer, Boolean>();

    private int entryAnimQueue = 0;
    private int veloUnit;

    SimpleDateFormat dateF = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.US);
    DecimalFormat dff = new DecimalFormat("0.00");
    DecimalFormat ddf = new DecimalFormat("00.0");

    public CustomArrayAdapter(Context context, List<DBTrack> objects, SharedPreferences pref) {
        super(context, R.layout.history_item_layout, objects);
        this.objects = objects;
        this._context = context;
        this._pref = pref;

        for (int i = 0; i < objects.size(); ++i) {
            mStaMap.put(objects.get(i).getId(), false);
            mIniMap.put(i, false);
        }
        inflater = LayoutInflater.from (context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){

        final ViewHolderItem viewHolder;

        DBTrack mTrack = objects.get(position);
        long trackId = mTrack.getId();
        double maxSpeed = mTrack.getMaxSpeed();
        double maxAirTime = mTrack.getMaxAirTime();
        String mLocation = mTrack.getLocationName();
        Date mDate = mTrack.getDate();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.history_item_layout, parent, false);
            viewHolder = new ViewHolderItem();

            viewHolder.histoMainLayout = (LinearLayout) convertView.findViewById(R.id.histo_main_layout);
            viewHolder.maxAirTime = (MeinTextView) convertView.findViewById(R.id.trackMaxAirTime);
            viewHolder.maxSpeed = (MeinTextView) convertView.findViewById(R.id.trackMaxSpeed);
            viewHolder.maxSpeedUnit = (MeinTextView) convertView.findViewById(R.id.trackMaxSpeedUnit);

            viewHolder.trackLocation = (MeinTextView) convertView.findViewById(R.id.trackLocation);
            viewHolder.arrowUpDown = (MeinTextView) convertView.findViewById(R.id.arrow_up_down);
            viewHolder.trackDate = (MeinTextView) convertView.findViewById(R.id.trackDate);
            viewHolder.arrowUpDown.setText(Constants.ICON_ARROW_DOWN);

            viewHolder.expanding_layout = (RelativeLayout)convertView.findViewById(R.id.expanding_layout);
            viewHolder.shareBtn = (TextView) convertView.findViewById(R.id.share_btn);
            viewHolder.deleteBtn = (TextView) convertView.findViewById(R.id.delete_btn);
            viewHolder.shareBtn.setText(Constants.ICON_SHARE);
            viewHolder.deleteBtn.setText(Constants.ICON_DELETE);

            convertView.setTag(viewHolder);

            // Entry Animations with delay between each view
            Animation entryAnim = AnimationUtils.loadAnimation(_context, R.anim.histo_item_enter_bottom);
            entryAnim.setStartOffset(50l * position);
            mIniMap.put(position, true);
            convertView.startAnimation(entryAnim);

        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
            if (!mStaMap.get(trackId)) {
                viewHolder.expanding_layout.setVisibility(View.GONE);
                convertView.setBackgroundColor(_context.getResources().getColor(R.color.history_background));
                viewHolder.arrowUpDown.setText(Constants.ICON_ARROW_DOWN);
            } else {
                viewHolder.expanding_layout.setVisibility(View.VISIBLE);
                convertView.setBackgroundColor(_context.getResources().getColor(R.color.history_selected_background));
                viewHolder.arrowUpDown.setText(Constants.ICON_ARROW_UP);
            }

            //Entry animation when new views appear from bottom
            if (mIniMap.get(position) == false) {
                Animation entryAnim = AnimationUtils.loadAnimation(_context, R.anim.histo_item_enter_bottom);
                entryAnim.setStartOffset(100l*entryAnimQueue+100l);
                entryAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        entryAnimQueue++;
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (entryAnimQueue > 0){
                            entryAnimQueue--;
                        }
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });

                convertView.startAnimation(entryAnim);
                mIniMap.put(position, true);
            }
        }

        viewHolder.maxAirTime.setText(dff.format(maxAirTime));
        veloUnit = _pref.getInt("VELOCITY_UNIT", 0);
        switch (veloUnit) {
            case 0:
                viewHolder.maxSpeed.setText(ddf.format(maxSpeed * 3.6));
                viewHolder.maxSpeedUnit.setText("km/h");
                break;
            case 1:
                viewHolder.maxSpeed.setText(ddf.format(maxSpeed));
                viewHolder.maxSpeedUnit.setText("m/s");
                break;
            case 2:
                viewHolder.maxSpeed.setText(ddf.format(maxSpeed * 2.236));
                viewHolder.maxSpeedUnit.setText("mi/h");
                break;
        }

        viewHolder.trackDate.setText(dateF.format(mDate));
        viewHolder.trackLocation.setText(mLocation);

        viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(_context);
                builder.setTitle(_context.getString(R.string.delete_track_title));
                builder.setMessage(_context.getString(R.string.delete_track_message))
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                delelteTrack(position);
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
        });

        return convertView;
    }

    // Calls when data in "objects" changes
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public boolean getStat(int pos) {
        return mStaMap.get(getTrackId(pos));
    }

    public long getTrackId(int pos) {
        return objects.get(pos).getId();
    }

    public void setStat(int pos, boolean newStat) {
        mStaMap.put(objects.get(pos).getId(), newStat);
    }

    public void printAllStat() {
        for (long id: mStaMap.keySet()){
            Log.e(TAG, id+": "+mStaMap.get(id));
        }
    }

    public void delelteTrack(int position) {
        if (position<0||position>objects.size()) {
            return;
        }
        DaoManager daoManager = DaoManager.getInstance(_context);
        DBTrackDao trackDao = daoManager.getDBTrackDao(DaoManager.TYPE_READ);
        trackDao.delete(objects.get(position));
        objects.remove(position);
        notifyDataSetChanged();
    }

    public static class ViewHolderItem {
        private LinearLayout histoMainLayout;
        private MeinTextView maxAirTime;
        private MeinTextView maxSpeed;
        private MeinTextView maxSpeedUnit;

        private MeinTextView arrowUpDown;
        private MeinTextView trackDate;
        private MeinTextView trackLocation;

        private RelativeLayout expanding_layout;
        private TextView shareBtn;
        private TextView deleteBtn;

    }

    public void resetEntryAnim(){
        for (int i = 0; i < objects.size(); ++i) {
            mIniMap.put(i, false);
        }
    }
}
