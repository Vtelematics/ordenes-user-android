package com.ordenese.Databases;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OrderTypeDB extends SQLiteOpenHelper {

    private final static String name = "UserServiceTypeDataBase";
    private final static int version = 1;
    private final static String TABLE_NAME = "user_service_type_table";
    private final static String USER_SERVICE_TYPE = "user_service_type";
    private final static String CREATE = "create table " + TABLE_NAME + " (" + USER_SERVICE_TYPE + " text);";
    private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
    private final static String SELECT = "select ";
    private final static String FROM = "from ";

    private static OrderTypeDB sInstance;

    public static synchronized OrderTypeDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new OrderTypeDB(context.getApplicationContext());
        }
        return sInstance;
    }

    private OrderTypeDB(Context context) {
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


    public void addUserServiceType(String userServiceType) {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(USER_SERVICE_TYPE, userServiceType);
        mSqLiteDb.insert(TABLE_NAME, null, mContentValues);

    }

    public void updateUserServiceType(String userServiceType) {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(USER_SERVICE_TYPE, userServiceType);
        // db.update(TABLE_NAME, contentValues, USER_SERVICE_TYPE + "=" + userServiceType, null);
        mSqLiteDb.update(TABLE_NAME, mContentValues, null, null);

    }


   /* public Boolean isAreaSelected() {
        String mQuery = SELECT + "* " + FROM + TABLE_NAME;
        Cursor mCursor;
        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(USER_SERVICE_TYPE));
            }
            mCursor.close();
        }
        return (!mResult.equals(""));
    }*/


    @SuppressLint("Range")
    public String getUserServiceType() {
        String mQuery = SELECT + USER_SERVICE_TYPE + " " + FROM + TABLE_NAME;
        Cursor mCursor;
        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(USER_SERVICE_TYPE));
            }
            mCursor.close();
            return mResult;
        }
        return null;
    }


    public void deleteUserServiceTypeDB() {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        mSqLiteDb.execSQL(DELETE_TABLE);
        // Log.d("***UserServiceTypeDB***","Deleted");

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