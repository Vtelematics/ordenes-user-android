
package com.ordenese.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ordenese.DataSets.CheckDBDataSet;
import com.ordenese.DataSets.LoginDataSet;

public class CheckDB extends SQLiteOpenHelper {

   private final static String name = "CheckDB";
   private final static int version = 2;
   private final static String TABLE_NAME = "user_detasdfrsdfils_table";

   private final static String BODY = "body";
   private final static String TITLE = "title";


   private final static String CREATE = "create table " + TABLE_NAME + " (" + TITLE + " text," +
           BODY + " text);";

   private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
   private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
   private final static String SELECT = "select ";
   private final static String FROM = "from ";

   // private Cursor mCursor;

   private static CheckDB sInstance;

   public static synchronized CheckDB getInstance(Context context) {
      if (sInstance == null) {
         sInstance = new CheckDB(context.getApplicationContext());
      }
      return sInstance;
   }

   private CheckDB(Context context) {
      super(context, name, null, version);
   }


   @Override
   public void onCreate(SQLiteDatabase db) {
      db.execSQL(CREATE);
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL(DROP_TABLE);
      onCreate(db);
   }


   public void add(CheckDBDataSet checkDBDataSet) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();
      mContentValues.put(BODY, checkDBDataSet.getBody());
      mContentValues.put(TITLE, checkDBDataSet.getTitle());
      mSqLiteDb.insert(TABLE_NAME, null, mContentValues);


   }



   public CheckDBDataSet getUserDetails() {

      String mQuery = SELECT +"* " + FROM + TABLE_NAME;
      CheckDBDataSet checkDBDataSet = new CheckDBDataSet();
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      Cursor mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            checkDBDataSet.setBody(mCursor.getString(mCursor.getColumnIndex(BODY)));
            checkDBDataSet.setTitle(mCursor.getString(mCursor.getColumnIndex(TITLE)));
         }
         if(!mCursor.isClosed()){
            mCursor.close();
         }
         return checkDBDataSet;
      }
      if(mCursor != null && !mCursor.isClosed()){
         mCursor.close();
      }
      return null;

   }



   public void delete() {

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

            Log.e("**********************","*************************");

            if( mCursor.getString(mCursor.getColumnIndex(BODY)) != null &&
                    !mCursor.getString(mCursor.getColumnIndex(BODY)).isEmpty()){
               Log.d(rtrt+" BODY ",mCursor.getString(mCursor.getColumnIndex(BODY)));
            }else {
               Log.d(rtrt+" BODY ","Empty or null..");
            }

            if( mCursor.getString(mCursor.getColumnIndex(TITLE)) != null &&
                    !mCursor.getString(mCursor.getColumnIndex(TITLE)).isEmpty()){
               Log.d(rtrt+" TITLE ",mCursor.getString(mCursor.getColumnIndex(TITLE)));
            }else {
               Log.d(rtrt+" TITLE ","Empty or null..");
            }

            rtrt++;

         }

         mCursor.close();
      }else {
         Log.e("***CheckDB toPrint()****","empty.");
      }


   }


}

