package com.gloomy.ShreddingRobot;

import com.gloomy.ShreddingRobot.Dao.DBTrack;
import com.gloomy.ShreddingRobot.Dao.DBTrackDao;
import com.gloomy.ShreddingRobot.Utility.BaseActivity;
import com.gloomy.ShreddingRobot.Utility.DaoManager;
import com.gloomy.ShreddingRobot.Widget.CustomArrayAdapter;
import com.gloomy.ShreddingRobot.Widget.ExpandingListView;

import java.util.ArrayList;
import android.os.Bundle;
import android.os.Handler;

public class HistoryActivity extends BaseActivity {

    private ArrayList<DBTrack> mTrackList;
    private DaoManager daoManager;
    private DBTrackDao trackDao;

    CustomArrayAdapter adapter;
    ExpandingListView listView;

    boolean noEntryAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        noEntryAnim = false;

        daoManager = DaoManager.getInstance(this);
        trackDao = daoManager.getDBTrackDao(DaoManager.TYPE_READ);
        mTrackList = (ArrayList<DBTrack>) trackDao.queryBuilder().orderDesc(DBTrackDao.Properties.Date).list();

        adapter = new CustomArrayAdapter(this, mTrackList, pref);
        listView = (ExpandingListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        if (!noEntryAnim) {
            listView.enableListView(false);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    listView.enableListView(true);
                    noEntryAnim = true;
                }
            }, 800);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        noEntryAnim = false;
        adapter.resetEntryAnim();
    }
}