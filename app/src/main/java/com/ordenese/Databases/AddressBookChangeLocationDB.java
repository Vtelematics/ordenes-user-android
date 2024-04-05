package com.ordenese.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ordenese.DataSets.AreaGeoCodeDataSet;

public class AddressBookChangeLocationDB extends SQLiteOpenHelper {

   private final static String name = "AddressBookChangeLocationDataBase";
   private final static int version = 1;
   private final static String TABLE_NAME = "address_book_chnage_location_table";
   private final static String PLACE_INDEX = "place_index";
   private final static String PLACE_LATITUDE = "place_latitude";
   private final static String PLACE_LONGITUDE = "place_longitude";
   private final static String PLACE_ADDS = "place_adds";
   private final static String PLACE_ADDS_NAME = "place_adds_name";


   private final static String CREATE = "create table " + TABLE_NAME + " (" + PLACE_LATITUDE + " text," +
           PLACE_LONGITUDE + " text," +
           PLACE_ADDS + " text," +
           PLACE_ADDS_NAME + " text);";

   private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
   private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
   private final static String SELECT = "select ";

   private final static String FROM = "from ";

   private final static String WHERE = "where ";
   private final static String AND = " and ";

   // private Cursor mCursor;


   private static AddressBookChangeLocationDB sInstance;

   public static synchronized AddressBookChangeLocationDB getInstance(Context context) {
      if (sInstance == null) {
         sInstance = new AddressBookChangeLocationDB(context.getApplicationContext());
      }
      return sInstance;
   }

   private AddressBookChangeLocationDB(Context context) {
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


   public void add(AreaGeoCodeDataSet areaGeoCode) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();
      mContentValues.put(PLACE_LATITUDE, areaGeoCode.getmLatitude());
      mContentValues.put(PLACE_LONGITUDE, areaGeoCode.getmLongitude());
      mContentValues.put(PLACE_ADDS, areaGeoCode.getmAddress());
      mContentValues.put(PLACE_ADDS_NAME, areaGeoCode.getmAddsNameOnly());
      mSqLiteDb.insert(TABLE_NAME, null, mContentValues);

   }

   public void update(AreaGeoCodeDataSet areaGeoCode) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();
      mContentValues.put(PLACE_LATITUDE, areaGeoCode.getmLatitude());
      mContentValues.put(PLACE_LONGITUDE, areaGeoCode.getmLongitude());
      mContentValues.put(PLACE_ADDS, areaGeoCode.getmAddress());
      mContentValues.put(PLACE_ADDS_NAME, areaGeoCode.getmAddsNameOnly());
      mSqLiteDb.update(TABLE_NAME, mContentValues, null, null);
   }

   public Boolean isExists(AreaGeoCodeDataSet areaGeoCode) {


      // ////Log.e("**************", "***************");


      //  ////Log.e("Curent Lat", areaGeoCode.getmLatitude());
      //  ////Log.e("Curent Lon", areaGeoCode.getmLongitude());

      // ////Log.e("**************", "***************");

      // toPrint();

      Boolean mIsExists = false;


        /*String mQuery = SELECT + "* " + FROM + TABLE_NAME +" "+WHERE+ PLACE_LATITUDE +"="+
                areaGeoCode.getmLatitude()*//*+AND+PLACE_LONGITUDE+"="+ areaGeoCode.getmLongitude()*//*; */

       /* String mQuery = SELECT + PLACE_LATITUDE +" " + FROM + TABLE_NAME +" "+WHERE+ PLACE_LATITUDE +"="+
                areaGeoCode.getmLatitude()+ AND + PLACE_LONGITUDE+"="+ areaGeoCode.getmLongitude(); */

      // String mQuery = SELECT + PLACE_LATITUDE + AND + PLACE_LONGITUDE + " " + FROM + TABLE_NAME;

      String mQuery = SELECT + "* " + FROM + TABLE_NAME;

      // ////Log.e("mQuery ", mQuery);

      Cursor mCursor;
      AreaGeoCodeDataSet mResult = new AreaGeoCodeDataSet();
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      mCursor = mSqLiteDb.rawQuery(mQuery, null);
      // int rtrt = 0;
      if (mCursor != null && mCursor.getCount() > 0) {

         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

            // mResult.setmLatitude();
            //mResult.setmLongitude();
            //mResult.setmAddress(mCursor.getString(mCursor.getColumnIndex(PLACE_ADDS)));
            // ////Log.e(rtrt + " mCursor ", areaGeoCode.getmLongitude());
            //rtrt++;

            String mExistsLat = mCursor.getString(mCursor.getColumnIndex(PLACE_LATITUDE));
            String mExistsLong = mCursor.getString(mCursor.getColumnIndex(PLACE_LONGITUDE));
            String mCurrentLat = areaGeoCode.getmLatitude();
            String mCurrentLong = areaGeoCode.getmLongitude();

            if (mExistsLat != null && mExistsLong != null &&
                    mExistsLat.equals(mCurrentLat) && mExistsLong.equals(mCurrentLong)) {
               mIsExists = true;
               break;
            }


         }

         mCursor.close();
      }

      return mIsExists;

       /* if(){

        }else {

        }*/

       /* Boolean mIsExists;

        if(mResult.getmLatitude() != null *//*&& mResult.getmLongitude() != null*//*){
            mIsExists = true;
            ////Log.e("mIsExists ","True");
            return mIsExists;
        }else {
            mIsExists = false;
            ////Log.e("mIsExists ","False");
            return mIsExists;
        }*/


   }

   public AreaGeoCodeDataSet getDetails() {
      String mQuery = SELECT + "* " + FROM + TABLE_NAME;
      Cursor mCursor;
      AreaGeoCodeDataSet mResult = new AreaGeoCodeDataSet();
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

            mResult.setmLatitude(mCursor.getString(mCursor.getColumnIndex(PLACE_LATITUDE)));
            mResult.setmLongitude(mCursor.getString(mCursor.getColumnIndex(PLACE_LONGITUDE)));
            mResult.setmAddress(mCursor.getString(mCursor.getColumnIndex(PLACE_ADDS)));
            mResult.setmAddsNameOnly(mCursor.getString(mCursor.getColumnIndex(PLACE_ADDS_NAME)));
         }
         mCursor.close();
      }
      return mResult;
   }

   public void deleteDB() {
      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      mSqLiteDb.execSQL(DELETE_TABLE);
   }

   public void toPrint() {

      Cursor mCursor;

      int rtrt = 0;

      String mQuery = SELECT + "* " + FROM + TABLE_NAME;

      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {

         //Log.d("**************", "***************");

         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {


            if (!mCursor.getString(mCursor.getColumnIndex(PLACE_LATITUDE)).isEmpty()) {
               //Log.d(rtrt + " PLACE_LATITUDE ", mCursor.getString(mCursor.getColumnIndex(PLACE_LATITUDE)));
            }
            if (!mCursor.getString(mCursor.getColumnIndex(PLACE_LONGITUDE)).isEmpty()) {
               //Log.d(rtrt + " PLACE_LONGITUDE ", mCursor.getString(mCursor.getColumnIndex(PLACE_LONGITUDE)));
            }
            if (!mCursor.getString(mCursor.getColumnIndex(PLACE_ADDS)).isEmpty()) {
               //Log.d(rtrt + " PLACE_ADDS ", mCursor.getString(mCursor.getColumnIndex(PLACE_ADDS)));
            }
            if (!mCursor.getString(mCursor.getColumnIndex(PLACE_ADDS_NAME)).isEmpty()) {
               //Log.d(rtrt + " PLACE_ADDS_NAME ", mCursor.getString(mCursor.getColumnIndex(PLACE_ADDS_NAME)));
            }


            rtrt++;
         }

         //Log.d("**************", "***************");

         mCursor.close();
      }


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
