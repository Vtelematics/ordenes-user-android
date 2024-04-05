package com.ordenese.Databases;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ordenese.DataSets.FilterDbDataSet;

import java.util.ArrayList;

public class ARFiltersDBTemp extends SQLiteOpenHelper {

   private final static String name = "FiltersTempDataBase";
   private final static int version = 1;
   private final static String TABLE_NAME = "filters_temp_table";
   private final static String FILTER_ID = "filter_id";
   private final static String FILTER_TYPE_ID = "filter_type_id";
   private final static String ITEM_TYPE = "item_type";

   private final static String CREATE = "create table " + TABLE_NAME + " (" + FILTER_ID +" text,"+ FILTER_TYPE_ID +" text,"+ ITEM_TYPE +" text);";
   private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
   private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
   private final static String SELECT = "select ";
   private final static String FROM = "from ";
   private final static String WHERE = "where ";
   private final static String AND = " and ";


   private static ARFiltersDBTemp sInstance;

   public static synchronized ARFiltersDBTemp getInstance(Context context) {
      if (sInstance == null) {
         sInstance = new ARFiltersDBTemp(context.getApplicationContext());
      }
      return sInstance;
   }

   private ARFiltersDBTemp(Context context) {
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


   public void addFilters(FilterDbDataSet filterDbDs) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();
      mContentValues.put(FILTER_ID, filterDbDs.getmFilterId());
      mContentValues.put(FILTER_TYPE_ID, filterDbDs.getmFilterTypeId());
      mContentValues.put(ITEM_TYPE, filterDbDs.getmItemType());
      mSqLiteDb.insert(TABLE_NAME, null, mContentValues);

   }

   public void updateFilters(FilterDbDataSet filterDbDs) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();
      mContentValues.put(FILTER_ID, filterDbDs.getmFilterId());
      mContentValues.put(FILTER_TYPE_ID, filterDbDs.getmFilterTypeId());
      mContentValues.put(ITEM_TYPE, filterDbDs.getmItemType());
      mSqLiteDb.update(TABLE_NAME, mContentValues,null, null);
   }

   public Boolean isFilterSelected(FilterDbDataSet filterDbDs) {

      String mQuery = SELECT + "* " + FROM + TABLE_NAME+" "+WHERE+ FILTER_TYPE_ID +"="+filterDbDs.getmFilterTypeId();
      Cursor mCursor;
      String mResult = "";
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            mResult = mCursor.getString(mCursor.getColumnIndex(FILTER_TYPE_ID));
         }
         mCursor.close();
      }
      return (mResult != null && !mResult.equals(""));
   }

   public void removeItemFromFilterList(FilterDbDataSet filterDbDs) {

      SQLiteDatabase db = this.getWritableDatabase();
      db.delete(TABLE_NAME, FILTER_ID +"="+filterDbDs.getmFilterId(), null);

   }



   public FilterDbDataSet getFilter() {
      String mQuery = SELECT + "* " + FROM + TABLE_NAME;
      Cursor mCursor;
      FilterDbDataSet mResult = new FilterDbDataSet();
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            mResult.setmFilterId(mCursor.getString(mCursor.getColumnIndex(FILTER_ID)));
            mResult.setmFilterTypeId(mCursor.getString(mCursor.getColumnIndex(FILTER_TYPE_ID)));
            mResult.setmItemType(mCursor.getString(mCursor.getColumnIndex(ITEM_TYPE)));
         }
         mCursor.close();
      }
      return mResult;
   }

   public ArrayList<FilterDbDataSet> getFiltersList() {
      String mQuery = SELECT + "* " + FROM + TABLE_NAME;
      Cursor mCursor;
      ArrayList<FilterDbDataSet> cuisinesList = new ArrayList<>();
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            FilterDbDataSet mResult = new FilterDbDataSet();
            mResult.setmFilterId(mCursor.getString(mCursor.getColumnIndex(FILTER_ID)));
            mResult.setmFilterTypeId(mCursor.getString(mCursor.getColumnIndex(FILTER_TYPE_ID)));
            mResult.setmItemType(mCursor.getString(mCursor.getColumnIndex(ITEM_TYPE)));
            cuisinesList.add(mResult);
         }
         mCursor.close();
      }
      return cuisinesList;
   }

   public boolean isFilterIdExists(String filterId) {

      String mQuery = SELECT + "* " + FROM + TABLE_NAME + " " + WHERE + FILTER_ID + "=" + filterId;

      String mResult = "";
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      Cursor mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            mResult = mCursor.getString(mCursor.getColumnIndex(FILTER_TYPE_ID));
         }
         mCursor.close();
      }
      return (mResult != null &&!mResult.equals(""));

   }

   @SuppressLint("Range")
   public boolean isFilterTypeIdExists(String filterId, String filterTypeId) {

      String mQuery = SELECT + "* " + FROM + TABLE_NAME + " " + WHERE + FILTER_ID + "=" + filterId +  AND + FILTER_TYPE_ID + "=" + filterTypeId;

      String mResult = "";
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      Cursor mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            mResult = mCursor.getString(mCursor.getColumnIndex(FILTER_TYPE_ID));
         }
         mCursor.close();
      }
      return (mResult != null &&!mResult.equals(""));

   }


   public void deleteFilterTypeId(String filterId, String filterTypeId) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      mSqLiteDb.delete(TABLE_NAME, FILTER_ID + "=" + filterId + AND + FILTER_TYPE_ID + "=" + filterTypeId, null);

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

   public void toPrint() {

      int rtrt = 0;

      String mQuery = SELECT +"* "+ FROM + TABLE_NAME;

      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      Cursor mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {

         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

            //Log.e("**********************","*************************");

            if( mCursor.getString(mCursor.getColumnIndex(FILTER_ID)) != null &&
                    !mCursor.getString(mCursor.getColumnIndex(FILTER_ID)).isEmpty()){
               Log.d(rtrt+" FILTER_ID ",mCursor.getString(mCursor.getColumnIndex(FILTER_ID)));
            }else {
               Log.d(rtrt+" FILTER_ID ","Empty or null..");
            }

            if( mCursor.getString(mCursor.getColumnIndex(FILTER_TYPE_ID)) != null &&
                    !mCursor.getString(mCursor.getColumnIndex(FILTER_TYPE_ID)).isEmpty()){
               Log.d(rtrt+" FILTER_TYPE_ID ",mCursor.getString(mCursor.getColumnIndex(FILTER_TYPE_ID)));
            }else {
               Log.d(rtrt+" FILTER_TYPE_ID ","Empty or null..");
            }

            if( mCursor.getString(mCursor.getColumnIndex(ITEM_TYPE)) != null &&
                    !mCursor.getString(mCursor.getColumnIndex(ITEM_TYPE)).isEmpty()){
               Log.d(rtrt+" ITEM_TYPE ",mCursor.getString(mCursor.getColumnIndex(ITEM_TYPE)));
            }else {
               Log.d(rtrt+" ITEM_TYPE ","Empty or null..");
            }

            rtrt++;
         }

         mCursor.close();
      }else {
         ////Log.e("***toPrint()****","empty.");
      }


   }


}
