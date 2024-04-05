package com.ordenese.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ordenese.DataSets.AreaGeoCodeDataSet;

public class AreaGeoCodeDB extends SQLiteOpenHelper {

    private final static String name = "UserAreaGeoCodeDataBase";
    private final static int version = 1;
    private final static String TABLE_NAME = "user_area_geocode_table";
    private final static String USER_LATITUDE = "user_latitude";
    private final static String USER_LONGITUDE = "user_longitude";
    private final static String USER_AREA_ADDS = "user_area_adds";
    private final static String USER_NEW_ADDS = "user_new_adds";
    private final static String CREATE = "create table " + TABLE_NAME + " (" + USER_LATITUDE +" text,"+USER_LONGITUDE +" text,"+USER_AREA_ADDS +" text,"+ USER_NEW_ADDS+" text);";
    private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
    private final static String SELECT = "select ";
    private final static String FROM = "from ";


    private static AreaGeoCodeDB sInstance;

    public static synchronized AreaGeoCodeDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AreaGeoCodeDB(context.getApplicationContext());
        }
        return sInstance;
    }

    private AreaGeoCodeDB(Context context) {
        super(context, name, null, version);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDb) {
        sqLiteDb.execSQL(CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDb, int oldVersion, int newVersion) {
        sqLiteDb.execSQL(DROP_TABLE);
        onCreate(sqLiteDb);
    }


    public void addUserAreaGeoCode(AreaGeoCodeDataSet areaGeoCode) {
        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(USER_LATITUDE, areaGeoCode.getmLatitude());
        mContentValues.put(USER_LONGITUDE, areaGeoCode.getmLongitude());
        mContentValues.put(USER_AREA_ADDS, areaGeoCode.getmAddress());
        mContentValues.put(USER_NEW_ADDS, areaGeoCode.getmAddsNameOnly());
        mSqLiteDb.insert(TABLE_NAME, null, mContentValues);

    }

    public void updateUserAreaGeoCode(AreaGeoCodeDataSet areaGeoCode) {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(USER_LATITUDE, areaGeoCode.getmLatitude());
        mContentValues.put(USER_LONGITUDE, areaGeoCode.getmLongitude());
        mContentValues.put(USER_AREA_ADDS, areaGeoCode.getmAddress());
        mContentValues.put(USER_NEW_ADDS, areaGeoCode.getmAddsNameOnly());
        mSqLiteDb.update(TABLE_NAME, mContentValues,null, null);

    }


    public Boolean isAreaGeoCodeSelected() {
        String mQuery = SELECT + "* " + FROM + TABLE_NAME;
        Cursor mCursor;
        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(USER_AREA_ADDS));
            }
            mCursor.close();
        }
        return (mResult != null && !mResult.equals(""));
    }



    public AreaGeoCodeDataSet getUserAreaGeoCode() {
        String mQuery = SELECT + "* " + FROM + TABLE_NAME;
        Cursor mCursor;
        AreaGeoCodeDataSet mResult = new AreaGeoCodeDataSet();
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult.setmLatitude(mCursor.getString(mCursor.getColumnIndex(USER_LATITUDE)));
                mResult.setmLongitude(mCursor.getString(mCursor.getColumnIndex(USER_LONGITUDE)));
                mResult.setmAddress(mCursor.getString(mCursor.getColumnIndex(USER_AREA_ADDS)));
                mResult.setmAddsNameOnly(mCursor.getString(mCursor.getColumnIndex(USER_NEW_ADDS)));
            }
            mCursor.close();
        }
        return mResult;
    }

    public void print() {
        String mQuery = SELECT + "* " + FROM + TABLE_NAME;
        Cursor mCursor;
       // AreaGeoCodeDataSet mResult = new AreaGeoCodeDataSet();
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

                String USER__LATITUDE = mCursor.getString(mCursor.getColumnIndex(USER_LATITUDE));
                String USER__LONGITUDE = mCursor.getString(mCursor.getColumnIndex(USER_LONGITUDE));
                String USER__AREA_ADDS = mCursor.getString(mCursor.getColumnIndex(USER_AREA_ADDS));
                String USER__NEW_ADDS = mCursor.getString(mCursor.getColumnIndex(USER_NEW_ADDS));

                if(USER__LATITUDE != null && !USER__LATITUDE.isEmpty()){
                    //Log.e("USER__LATITUDE",USER__LATITUDE);
                }else {
                    //Log.e("USER__LATITUDE","NULL or EMPTY");
                }

                if(USER__LONGITUDE != null && !USER__LONGITUDE.isEmpty()){
                    //Log.e("USER__LONGITUDE",USER__LONGITUDE);
                }else {
                    //Log.e("USER__LONGITUDE","NULL or EMPTY");
                }

                if(USER__AREA_ADDS != null && !USER__AREA_ADDS.isEmpty()){
                    //Log.e("USER__AREA_ADDS",USER__AREA_ADDS);
                }else {
                    //Log.e("USER__AREA_ADDS","NULL or EMPTY");
                }

                if(USER__NEW_ADDS != null && !USER__NEW_ADDS.isEmpty()){
                    //Log.e("USER__NEW_ADDS",USER__NEW_ADDS);
                }else {
                    //Log.e("USER__NEW_ADDS","NULL or EMPTY");
                }

            }
            mCursor.close();
        }

    }


    public void deleteUserAreaGeoCodeDB() {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        mSqLiteDb.execSQL(DELETE_TABLE);
        // Log.d("***UserAreaDB***","Deleted");

    }

    public int getSizeOfList() {
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(mSqLiteDb, TABLE_NAME);
    }


    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

}