package com.ordenese.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ordenese.DataSets.CuisinesDataSet;

import java.util.ArrayList;

public class ARCuisinesDBTemp extends SQLiteOpenHelper {

   private final static String name = "CuisinesDataBaseTemp";
   private final static int version = 1;
   private final static String TABLE_NAME = "cuisines_table_temp";
   private final static String CUISINE_NAME = "cuisine_name";
   private final static String CUISINE_ID = "cuisines_id";
   private final static String CREATE = "create table " + TABLE_NAME + " (" + CUISINE_ID +" text,"+ CUISINE_NAME +" text);";
   private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
   private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
   private final static String SELECT = "select ";
   private final static String FROM = "from ";
   private final static String WHERE = "where ";


   private static ARCuisinesDBTemp sInstance;

   public static synchronized ARCuisinesDBTemp getInstance(Context context) {
      if (sInstance == null) {
         sInstance = new ARCuisinesDBTemp(context.getApplicationContext());
      }
      return sInstance;
   }

   private ARCuisinesDBTemp(Context context) {
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


   public void addCuisines(CuisinesDataSet cuisinesListDs) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();
      mContentValues.put(CUISINE_ID, cuisinesListDs.getCuisine_id());
      mContentValues.put(CUISINE_NAME, cuisinesListDs.getName());
      mSqLiteDb.insert(TABLE_NAME, null, mContentValues);

   }

   public void updateCuisines(CuisinesDataSet cuisinesListDs) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();
      mContentValues.put(CUISINE_ID, cuisinesListDs.getCuisine_id());
      mContentValues.put(CUISINE_NAME, cuisinesListDs.getName());
      mSqLiteDb.update(TABLE_NAME, mContentValues,null, null);
   }

   public Boolean isCuisineSelected(CuisinesDataSet cuisinesListDs) {

      String mQuery = SELECT + "* " + FROM + TABLE_NAME+" "+WHERE+ CUISINE_ID +"="+cuisinesListDs.cuisine_id;
      Cursor mCursor;
      String mResult = "";
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            mResult = mCursor.getString(mCursor.getColumnIndex(CUISINE_ID));
         }
         mCursor.close();
      }
      return (mResult != null && !mResult.equals(""));
   }

   public void removeItemFromCuisineList(CuisinesDataSet cuisinesListDs) {

      SQLiteDatabase db = this.getWritableDatabase();
      db.delete(TABLE_NAME, CUISINE_ID+"="+cuisinesListDs.cuisine_id, null);

   }



   public CuisinesDataSet getCuisine() {
      String mQuery = SELECT + "* " + FROM + TABLE_NAME;
      Cursor mCursor;
      CuisinesDataSet mResult = new CuisinesDataSet();
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            mResult.setCuisine_id(mCursor.getString(mCursor.getColumnIndex(CUISINE_ID)));
            mResult.setName(mCursor.getString(mCursor.getColumnIndex(CUISINE_NAME)));
         }
         mCursor.close();
      }
      return mResult;
   }

   public ArrayList<CuisinesDataSet> getCuisineList() {
      String mQuery = SELECT + "* " + FROM + TABLE_NAME;
      Cursor mCursor;
      ArrayList<CuisinesDataSet> cuisinesList = new ArrayList<>();
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            CuisinesDataSet mResult = new CuisinesDataSet();
            mResult.setCuisine_id(mCursor.getString(mCursor.getColumnIndex(CUISINE_ID)));
            mResult.setName(mCursor.getString(mCursor.getColumnIndex(CUISINE_NAME)));
            cuisinesList.add(mResult);
         }
         mCursor.close();
      }
      return cuisinesList;
   }


   public void deleteCuisinesDB() {

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

            if( mCursor.getString(mCursor.getColumnIndex(CUISINE_ID)) != null &&
                    !mCursor.getString(mCursor.getColumnIndex(CUISINE_ID)).isEmpty()){
               Log.d(rtrt+" CUISINE_ID ",mCursor.getString(mCursor.getColumnIndex(CUISINE_ID)));
            }else {
               Log.d(rtrt+" CUISINE_ID ","Empty or null..");
            }

            if( mCursor.getString(mCursor.getColumnIndex(CUISINE_NAME)) != null &&
                    !mCursor.getString(mCursor.getColumnIndex(CUISINE_NAME)).isEmpty()){
               Log.d(rtrt+" CUISINE_NAME ",mCursor.getString(mCursor.getColumnIndex(CUISINE_NAME)));
            }else {
               Log.d(rtrt+" CUISINE_NAME ","Empty or null..");
            }

            rtrt++;
         }

         mCursor.close();
      }else {
         ////Log.e("***toPrint()****","empty.");
      }


   }


}
