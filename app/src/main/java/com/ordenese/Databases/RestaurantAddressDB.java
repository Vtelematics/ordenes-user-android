package com.ordenese.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ordenese.DataSets.AddressGeocodeDataSet;

public class RestaurantAddressDB extends SQLiteOpenHelper {

    private final static String name = "Restaurant_Address_DataBase";
    private final static int version = 1;
    private final static String TABLE_NAME = "address_restaurant_table_1";

    private final static String RESTAURANT_ID = "restaurant_id";
    private final static String RESTAURANT_NAME = "restaurant_name";
    private final static String LATITUDE = "latitude";
    private final static String LONGITUDE = "longitude";
    private final static String ADDRESS = "address";
    private final static String GEOCODE = "geocode";

    private final static String PREPARING_TIME = "preparing_time";
    private final static String DELIVERY_TIME = "delivery_time";

    private final static String CHECK_OUT_NOTE = "check_out_note";

    private final static String CREATE = "create table " + TABLE_NAME + " (" + RESTAURANT_ID +" text,"
            + RESTAURANT_NAME +" text,"+ LATITUDE +" text,"+ LONGITUDE +" text,"+ GEOCODE +" text,"
            + PREPARING_TIME +" text,"
            + DELIVERY_TIME +" text,"
            + ADDRESS +" text,"
            + CHECK_OUT_NOTE +" text);";

    private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
    private final static String SELECT = "select ";
    private final static String FROM = " from ";
    private final static String EQUALS = "=";
    private final static String WHERE = " where ";


    private static RestaurantAddressDB sInstance;

    public static synchronized RestaurantAddressDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RestaurantAddressDB(context.getApplicationContext());
        }
        return sInstance;
    }

    private RestaurantAddressDB(Context context) {
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


    public void addRestaurantGeocode(AddressGeocodeDataSet addressGeocodeDs) {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(RESTAURANT_ID, addressGeocodeDs.getRestaurantId());
        mContentValues.put(RESTAURANT_NAME, addressGeocodeDs.getRestaurantName());
        mContentValues.put(LATITUDE, addressGeocodeDs.getLatitude());
        mContentValues.put(LONGITUDE, addressGeocodeDs.getLongitude());
        mContentValues.put(ADDRESS, addressGeocodeDs.getAddress());
        mContentValues.put(GEOCODE, addressGeocodeDs.getGeocode());

        mContentValues.put(PREPARING_TIME, addressGeocodeDs.getPreparingTime());
        mContentValues.put(DELIVERY_TIME, addressGeocodeDs.getDeliveryTime());

        mContentValues.put(CHECK_OUT_NOTE, addressGeocodeDs.getCheckOutNote());

        mSqLiteDb.insert(TABLE_NAME, null, mContentValues);

    }


    public Boolean isGeocodeExists() {
        String mQuery = SELECT + "*" + FROM + TABLE_NAME ;
        Cursor mCursor;
        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(LATITUDE));
            }
            mCursor.close();
        }
        return (mResult!= null && !mResult.equals(""));
    }

    public AddressGeocodeDataSet getGeocodeDetails() {

        String mQuery = SELECT +"*" + FROM + TABLE_NAME ;
        Cursor mCursor;
        AddressGeocodeDataSet mResult = new AddressGeocodeDataSet();
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

                mResult.setRestaurantId(mCursor.getString(mCursor.getColumnIndex(RESTAURANT_ID)));
                mResult.setRestaurantName(mCursor.getString(mCursor.getColumnIndex(RESTAURANT_NAME)));
                mResult.setLatitude(mCursor.getString(mCursor.getColumnIndex(LATITUDE)));
                mResult.setLongitude(mCursor.getString(mCursor.getColumnIndex(LONGITUDE)));
                mResult.setAddress(mCursor.getString(mCursor.getColumnIndex(ADDRESS)));
                mResult.setGeocode(mCursor.getString(mCursor.getColumnIndex(GEOCODE)));

                mResult.setPreparingTime(mCursor.getString(mCursor.getColumnIndex(PREPARING_TIME)));
                mResult.setDeliveryTime(mCursor.getString(mCursor.getColumnIndex(DELIVERY_TIME)));

                mResult.setCheckOutNote(mCursor.getString(mCursor.getColumnIndex(CHECK_OUT_NOTE)));

            }
            mCursor.close();
        }

        return mResult;
    }

    public void deleteGeocodeDB() {
        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        mSqLiteDb.execSQL(DELETE_TABLE);
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

