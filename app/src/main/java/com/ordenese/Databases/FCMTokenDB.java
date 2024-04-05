package com.ordenese.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FCMTokenDB extends SQLiteOpenHelper {

   private final static String name = "FCMTokenDataBase";
   private final static int version = 1;
   private final static String TABLE_NAME = "fcm_token_table";
   private final static String FCM_TOKEN = "fcm_token";
   private final static String CREATE = "create table " + TABLE_NAME + " (" + FCM_TOKEN + " text);";
   private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
   private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
   private final static String SELECT = "select ";
   private final static String FROM = "from ";

   private static FCMTokenDB sInstance;

   public static synchronized FCMTokenDB getInstance(Context context) {
      if (sInstance == null) {
         sInstance = new FCMTokenDB(context.getApplicationContext());
      }
      return sInstance;
   }

   private FCMTokenDB(Context context) {
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


   public void add(String fcmToken) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();
      mContentValues.put(FCM_TOKEN, fcmToken);
      mSqLiteDb.insert(TABLE_NAME, null, mContentValues);

   }



   public String getToken() {
      String mQuery = SELECT + FCM_TOKEN + " " + FROM + TABLE_NAME;
      Cursor mCursor;
      String mResult = "";
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            mResult = mCursor.getString(mCursor.getColumnIndex(FCM_TOKEN));
         }
         mCursor.close();
         return mResult;
      }
      return null;
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
