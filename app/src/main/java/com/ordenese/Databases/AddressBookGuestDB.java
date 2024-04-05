package com.ordenese.Databases;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ordenese.DataSets.PickupGuestDataSet;

public class AddressBookGuestDB extends SQLiteOpenHelper {

    private final static String name = "AddressBookGuest";
    private final static int version = 1;
    private final static String TABLE_NAME = "address_book_guest";
    private final static String F_name = "F_name";
    private final static String L_name = "L_name";
    private final static String email_id = "email_id";
    private final static String country_code = "country_code";
    private final static String mobile_number = "mobile_number";
    private final static String CREATE = "create table " + TABLE_NAME + " (" + F_name + " text," +
            L_name + " text," +
            email_id + " text," +
            country_code + " text," +
            mobile_number + " text);";

    private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
    private final static String SELECT = "select ";

    private final static String FROM = "from ";

    private static AddressBookGuestDB sInstance;

    public static synchronized AddressBookGuestDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AddressBookGuestDB(context.getApplicationContext());
        }
        return sInstance;
    }

    private AddressBookGuestDB(Context context) {
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

    public void add(PickupGuestDataSet areaGeoCode) {
        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(F_name, areaGeoCode.getF_name());
        mContentValues.put(L_name, areaGeoCode.getL_name());
        mContentValues.put(email_id, areaGeoCode.getEmail_id());
        mContentValues.put(country_code, areaGeoCode.getCountry_code());
        mContentValues.put(mobile_number, areaGeoCode.getMobile_number());
        mSqLiteDb.insert(TABLE_NAME, null, mContentValues);

    }

    public void update(PickupGuestDataSet areaGeoCode) {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(F_name, areaGeoCode.getF_name());
        mContentValues.put(L_name, areaGeoCode.getL_name());
        mContentValues.put(email_id, areaGeoCode.getEmail_id());
        mContentValues.put(country_code, areaGeoCode.getCountry_code());
        mContentValues.put(mobile_number, areaGeoCode.getMobile_number());
        mSqLiteDb.update(TABLE_NAME, mContentValues, null, null);
    }


    @SuppressLint("Range")
    public PickupGuestDataSet getDetails() {
        String mQuery = SELECT + "* " + FROM + TABLE_NAME;
        Cursor mCursor;
        PickupGuestDataSet mResult = new PickupGuestDataSet();
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult.setF_name(mCursor.getString(mCursor.getColumnIndex(F_name)));
                mResult.setL_name(mCursor.getString(mCursor.getColumnIndex(L_name)));
                mResult.setEmail_id(mCursor.getString(mCursor.getColumnIndex(email_id)));
                mResult.setCountry_code(mCursor.getString(mCursor.getColumnIndex(country_code)));
                mResult.setMobile_number(mCursor.getString(mCursor.getColumnIndex(mobile_number)));
            }
            mCursor.close();
        }
        return mResult;
    }


    public void deleteDB() {
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
