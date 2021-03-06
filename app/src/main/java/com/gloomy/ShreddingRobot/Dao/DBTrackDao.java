package com.gloomy.ShreddingRobot.Dao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

/**
 * DAO for table DBTRACK.
 */
public class DBTrackDao extends AbstractDao<DBTrack, Long> {

    public static final String TABLENAME = "DBTRACK";

    /**
     * Properties of entity DBTrack.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property MaxSpeed = new Property(1, Double.class, "maxSpeed", false, "MAX_SPEED");
        public final static Property AvgSpeed = new Property(2, Double.class, "avgSpeed", false, "AVG_SPEED");
        public final static Property MaxAirTime = new Property(3, Double.class, "maxAirTime", false, "MAX_AIR_TIME");
        public final static Property LocationName = new Property(4, String.class, "locationName", false, "LOCATION_NAME");
        public final static Property Date = new Property(5, java.util.Date.class, "date", false, "DATE");
    };

    public DBTrackDao(DaoConfig config) {
        super(config);
    }

    public DBTrackDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'DBTRACK' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'MAX_SPEED' REAL," + // 1: maxSpeed
                "'AVG_SPEED' REAL," + // 2: avgSpeed
                "'MAX_AIR_TIME' REAL," + // 3: maxAirTime
                "'LOCATION_NAME' TEXT," + // 4: locationName
                "'DATE' INTEGER);"); // 5: date
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'DBTRACK'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, DBTrack entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }

        Double maxSpeed = entity.getMaxSpeed();
        if (maxSpeed != null) {
            stmt.bindDouble(2, maxSpeed);
        }

        Double avgSpeed = entity.getAvgSpeed();
        if (avgSpeed != null) {
            stmt.bindDouble(3, avgSpeed);
        }

        Double maxAirTime = entity.getMaxAirTime();
        if (maxAirTime != null) {
            stmt.bindDouble(4, maxAirTime);
        }

        String locationName = entity.getLocationName();
        if (locationName != null) {
            stmt.bindString(5, locationName);
        }

        java.util.Date date = entity.getDate();
        if (date != null) {
            stmt.bindLong(6, date.getTime());
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }

    /** @inheritdoc */
    @Override
    public DBTrack readEntity(Cursor cursor, int offset) {
        DBTrack entity = new DBTrack( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getDouble(offset + 1), // maxSpeed
                cursor.isNull(offset + 2) ? null : cursor.getDouble(offset + 2), // avgSpeed
                cursor.isNull(offset + 3) ? null : cursor.getDouble(offset + 3), // maxAirTime
                cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // locationName
                cursor.isNull(offset + 5) ? null : new java.util.Date(cursor.getLong(offset + 5))
        );
        return entity;
    }

    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, DBTrack entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMaxSpeed(cursor.isNull(offset + 1) ? null : cursor.getDouble(offset + 1));
        entity.setAvgSpeed(cursor.isNull(offset + 2) ? null : cursor.getDouble(offset + 2));
        entity.setMaxAirTime(cursor.isNull(offset + 3) ? null : cursor.getDouble(offset + 3));
        entity.setLocationName(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setDate(cursor.isNull(offset + 5) ? null : new java.util.Date(cursor.getLong(offset + 5)));
    }

    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(DBTrack entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }

    /** @inheritdoc */
    @Override
    public Long getKey(DBTrack entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override
    protected boolean isEntityUpdateable() {
        return true;
    }

}
