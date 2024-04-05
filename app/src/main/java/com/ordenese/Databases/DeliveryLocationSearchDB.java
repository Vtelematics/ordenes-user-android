package com.ordenese.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ordenese.DataSets.CuisinesDataSet;
import com.ordenese.DataSets.DeliveryLocationSearchDataSet;

import java.util.ArrayList;

public class DeliveryLocationSearchDB extends SQLiteOpenHelper {

   private final static String name = "DeliveryLocationSearchDataBase";
   private final static int version = 1;
   private final static String TABLE_NAME = "delivery_location_search_table";
   private final static String mIsCurrentLocation = "is_current_location";
   private final static String mIsSearchedAddress = "is_searched_adds";

   private final static String mSearchedAddressNameOnly = "searched_adds_name_only";
   private final static String mSearchedAddressFull = "searched_adds_full";

   private final static String mSearchedAddressLatitude = "searched_adds_latitude";
   private final static String mSearchedAddressLongitude = "searched_adds_longitude";

   private final static String CREATE = "create table " + TABLE_NAME + " (" + mIsCurrentLocation +" text,"+
           mIsSearchedAddress +" text,"+
           mSearchedAddressNameOnly +" text,"+
           mSearchedAddressFull +" text,"+
           mSearchedAddressLatitude +" text,"+
           mSearchedAddressLongitude +" text);";

   private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
   private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
   private final static String SELECT = "select ";
   private final static String FROM = "from ";
   private final static String WHERE = "where ";

   private static DeliveryLocationSearchDB sInstance;

   public static synchronized DeliveryLocationSearchDB getInstance(Context context) {
      if (sInstance == null) {
         sInstance = new DeliveryLocationSearchDB(context.getApplicationContext());
      }
      return sInstance;
   }

   private DeliveryLocationSearchDB(Context context) {
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


   public void add(DeliveryLocationSearchDataSet dLSearchDs) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();
      mContentValues.put(mIsSearchedAddress, dLSearchDs.getmIsSearchedAddress());
      mContentValues.put(mIsCurrentLocation, dLSearchDs.getmIsCurrentLocation());
      mContentValues.put(mSearchedAddressNameOnly, dLSearchDs.getmSearchedAddressNameOnly());
      mContentValues.put(mSearchedAddressFull, dLSearchDs.getmSearchedAddressFull());
      mContentValues.put(mSearchedAddressLatitude, dLSearchDs.getmSearchedAddressLatitude());
      mContentValues.put(mSearchedAddressLongitude, dLSearchDs.getmSearchedAddressLongitude());
      mSqLiteDb.insert(TABLE_NAME, null, mContentValues);

   }

   public void update(DeliveryLocationSearchDataSet dLSearchDs) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();
      mContentValues.put(mIsSearchedAddress, dLSearchDs.getmIsSearchedAddress());
      mContentValues.put(mIsCurrentLocation, dLSearchDs.getmIsCurrentLocation());
      mContentValues.put(mSearchedAddressNameOnly, dLSearchDs.getmSearchedAddressNameOnly());
      mContentValues.put(mSearchedAddressFull, dLSearchDs.getmSearchedAddressFull());
      mContentValues.put(mSearchedAddressLatitude, dLSearchDs.getmSearchedAddressLatitude());
      mContentValues.put(mSearchedAddressLongitude, dLSearchDs.getmSearchedAddressLongitude());
      mSqLiteDb.update(TABLE_NAME, mContentValues,null, null);
   }

   /*public Boolean isSearchedAddsSelected() {

      String mQuery = SELECT + "* " + FROM + TABLE_NAME+" "+WHERE+ mIsSearchedAddress +"="+cuisinesListDs.cuisine_id;
      Cursor mCursor;
      String mResult = "";
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            mResult = mCursor.getString(mCursor.getColumnIndex(mIsSearchedAddress));
         }
         mCursor.close();
      }
      return (mResult != null && !mResult.equals(""));
   }

   public void removeItemFromList(CuisinesDataSet cuisinesListDs) {

      SQLiteDatabase db = this.getWritableDatabase();
      db.delete(TABLE_NAME, mIsSearchedAddress +"="+cuisinesListDs.cuisine_id, null);

   }*/



   public DeliveryLocationSearchDataSet getData() {
      String mQuery = SELECT + "* " + FROM + TABLE_NAME;
      Cursor mCursor;
      DeliveryLocationSearchDataSet mResult = new DeliveryLocationSearchDataSet();
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            mResult.setmIsSearchedAddress(mCursor.getString(mCursor.getColumnIndex(mIsSearchedAddress)));
            mResult.setmIsCurrentLocation(mCursor.getString(mCursor.getColumnIndex(mIsCurrentLocation)));
            mResult.setmSearchedAddressNameOnly(mCursor.getString(mCursor.getColumnIndex(mSearchedAddressNameOnly)));
            mResult.setmSearchedAddressFull(mCursor.getString(mCursor.getColumnIndex(mSearchedAddressFull)));
            mResult.setmSearchedAddressLatitude(mCursor.getString(mCursor.getColumnIndex(mSearchedAddressLatitude)));
            mResult.setmSearchedAddressLongitude(mCursor.getString(mCursor.getColumnIndex(mSearchedAddressLongitude)));
         }
         mCursor.close();
      }
      return mResult;
   }

  /* public ArrayList<CuisinesDataSet> getList() {
      String mQuery = SELECT + "* " + FROM + TABLE_NAME;
      Cursor mCursor;
      ArrayList<CuisinesDataSet> cuisinesList = new ArrayList<>();
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            CuisinesDataSet mResult = new CuisinesDataSet();
            mResult.setCuisine_id(mCursor.getString(mCursor.getColumnIndex(mIsSearchedAddress)));
            mResult.setName(mCursor.getString(mCursor.getColumnIndex(mIsCurrentLocation)));
            cuisinesList.add(mResult);
         }
         mCursor.close();
      }
      return cuisinesList;
   }*/


   public void deleteDB() {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      mSqLiteDb.execSQL(DELETE_TABLE);
      //  //Log.d("***UserCuisinesDB***","Deleted");

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

            if( mCursor.getString(mCursor.getColumnIndex(mIsSearchedAddress)) != null &&
                    !mCursor.getString(mCursor.getColumnIndex(mIsSearchedAddress)).isEmpty()){
               Log.d(rtrt+" mIsSearchedAddress ",mCursor.getString(mCursor.getColumnIndex(mIsSearchedAddress)));
            }else {
               Log.d(rtrt+" mIsSearchedAddress ","Empty or null..");
            }

            if( mCursor.getString(mCursor.getColumnIndex(mIsCurrentLocation)) != null &&
                    !mCursor.getString(mCursor.getColumnIndex(mIsCurrentLocation)).isEmpty()){
               Log.d(rtrt+" mIsCurrentLocation ",mCursor.getString(mCursor.getColumnIndex(mIsCurrentLocation)));
            }else {
               Log.d(rtrt+" mIsCurrentLocation ","Empty or null..");
            }

            if( mCursor.getString(mCursor.getColumnIndex(mSearchedAddressNameOnly)) != null &&
                    !mCursor.getString(mCursor.getColumnIndex(mSearchedAddressNameOnly)).isEmpty()){
               Log.d(rtrt+" mSearchedAddressNameOnly ",mCursor.getString(mCursor.getColumnIndex(mSearchedAddressNameOnly)));
            }else {
               Log.d(rtrt+" mSearchedAddressNameOnly ","Empty or null..");
            }

            if( mCursor.getString(mCursor.getColumnIndex(mSearchedAddressFull)) != null &&
                    !mCursor.getString(mCursor.getColumnIndex(mSearchedAddressFull)).isEmpty()){
               Log.d(rtrt+" mSearchedAddressFull ",mCursor.getString(mCursor.getColumnIndex(mSearchedAddressFull)));
            }else {
               Log.d(rtrt+" mSearchedAddressFull ","Empty or null..");
            }

            if( mCursor.getString(mCursor.getColumnIndex(mSearchedAddressLatitude)) != null &&
                    !mCursor.getString(mCursor.getColumnIndex(mSearchedAddressLatitude)).isEmpty()){
               Log.d(rtrt+" mSearchedAddressLatitude ",mCursor.getString(mCursor.getColumnIndex(mSearchedAddressLatitude)));
            }else {
               Log.d(rtrt+" mSearchedAddressLatitude ","Empty or null..");
            }

            if( mCursor.getString(mCursor.getColumnIndex(mSearchedAddressLongitude)) != null &&
                    !mCursor.getString(mCursor.getColumnIndex(mSearchedAddressLongitude)).isEmpty()){
               Log.d(rtrt+" mSearchedAddressLongitude ",mCursor.getString(mCursor.getColumnIndex(mSearchedAddressLongitude)));
            }else {
               Log.d(rtrt+" mSearchedAddressLongitude ","Empty or null..");
            }

            rtrt++;
         }

         mCursor.close();
      }else {
         ////Log.e("***toPrint()****","empty.");
      }


   }

}
