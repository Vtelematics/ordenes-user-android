package com.ordenese.Databases;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ordenese.DataSets.FilterDbDataSet;

import java.util.ArrayList;

public class WishListDB extends SQLiteOpenHelper {

    private final static String name = "WishListDataBase";
    private final static int version = 1;
    private final static String TABLE_NAME = "wishlist_table";
    private final static String vendor_id = "vendor_id";
    private final static String CREATE = "create table " + TABLE_NAME + " (" + vendor_id + " text);";
    private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
    private final static String SELECT = "select ";
    private final static String FROM = "from ";
    private final static String WHERE = "where ";

    private static WishListDB sInstance;

    public static synchronized WishListDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new WishListDB(context.getApplicationContext());
        }
        return sInstance;
    }

    private WishListDB(Context context) {
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


    public void add_vendor_id(String vd) {
        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(vendor_id, vd);
        mSqLiteDb.insert(TABLE_NAME, null, mContentValues);
    }

    public void removeFromFavouriteList(String v_id) {
        SQLiteDatabase mDB = this.getWritableDatabase();
        mDB.delete(TABLE_NAME, vendor_id + "=" + v_id, null);
    }

    public void update_vendor_id(String v_id) {
        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(vendor_id, v_id);
        mSqLiteDb.update(TABLE_NAME, mContentValues, null, null);
    }

    @SuppressLint("Range")
    public String get_vendor_id() {
        String mQuery = SELECT + vendor_id + " " + FROM + TABLE_NAME;
        Cursor mCursor;
        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(vendor_id));
            }
            mCursor.close();
            return mResult;
        }
        return null;
    }

    @SuppressLint("Range")
    public Boolean isSelected(String v_id) {
        String mQuery = SELECT + "* " + FROM + TABLE_NAME + " " + WHERE + vendor_id + "=" + v_id;
        Cursor mCursor;
        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(vendor_id));
            }
            mCursor.close();
        }
        return (mResult != null && !mResult.equals(""));
    }

    public void delete_vendor_idDB() {
        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        mSqLiteDb.execSQL(DELETE_TABLE);
    }

    @SuppressLint("Range")
    public ArrayList<String> getWishList() {

        ArrayList<String> mUserFavouriteList = new ArrayList<>();

        String mQuery = SELECT + "* " + FROM + TABLE_NAME;

        SQLiteDatabase mDB = this.getReadableDatabase();
        Cursor mCursor = mDB.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

                // Log.e("for RESTAURANT_ID ",mCursor.getString(mCursor.getColumnIndex(RESTAURANT_ID)));

//                mUserFavouriteListDs.setRestaurantId(mCursor.getString(mCursor.getColumnIndex(RESTAURANT_ID)));
                mUserFavouriteList.add(mCursor.getString(mCursor.getColumnIndex(vendor_id)));

            }
            mCursor.close();
        }
        return mUserFavouriteList;

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