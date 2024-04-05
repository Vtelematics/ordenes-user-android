package com.ordenese.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ordenese.DataSets.CheckOutDBDataSet;
import com.ordenese.DataSets.LoginDataSet;

public class CheckOutDetailsDB extends SQLiteOpenHelper {

   private final static String name = "CheckOutDetailsDataBase";
   private final static int version = 1;
   private final static String TABLE_NAME = "check_out_details_table";

   private final static String COUPON_CODE = "coupon_code";
   private final static String COUPON_ID = "coupon_id";
   private final static String ADDRESS_ID = "address_id";
   private final static String PAYMENT_LIST_ID = "payment_list_id";
   private final static String PAYMENT_CODE = "payment_code";
   private final static String CONTACT_LESS_DELIVERY = "contact_less_delivery";


   private final static String CREATE = "create table " + TABLE_NAME + " (" + COUPON_ID + " text," +COUPON_CODE + " text," +ADDRESS_ID + " text," +
           PAYMENT_LIST_ID + " text," +
           CONTACT_LESS_DELIVERY + " text," +
           PAYMENT_CODE + " text);";

   private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
   private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
   private final static String SELECT = "select ";
   private final static String FROM = "from ";

   // private Cursor mCursor;

   private static CheckOutDetailsDB sInstance;

   public static synchronized CheckOutDetailsDB getInstance(Context context) {
      if (sInstance == null) {
         sInstance = new CheckOutDetailsDB(context.getApplicationContext());
      }
      return sInstance;
   }

   private CheckOutDetailsDB(Context context) {
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


   public void add(CheckOutDBDataSet checkOutDBDs) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();
      mContentValues.put(COUPON_CODE, checkOutDBDs.getCouponCode());
      mContentValues.put(COUPON_ID, checkOutDBDs.getCouponId());

      mContentValues.put(ADDRESS_ID, checkOutDBDs.getAddressId());
      mContentValues.put(PAYMENT_LIST_ID, checkOutDBDs.getPaymentListId());

      mContentValues.put(CONTACT_LESS_DELIVERY, checkOutDBDs.getContactLessDeliveryChecked());

      mContentValues.put(PAYMENT_CODE, checkOutDBDs.getPaymentCode());

      mSqLiteDb.insert(TABLE_NAME, null, mContentValues);


   }

  /* public void update(CheckOutDBDataSet checkOutDBDs) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();
      mContentValues.put(COUPON_CODE, checkOutDBDs.getCouponCode());
      mContentValues.put(COUPON_ID, checkOutDBDs.getCouponId());

      mContentValues.put(ADDRESS_ID, checkOutDBDs.getAddressId());
      mContentValues.put(PAYMENT_LIST_ID, checkOutDBDs.getPaymentListId());

      mContentValues.put(CONTACT_LESS_DELIVERY, checkOutDBDs.getContactLessDeliveryChecked());

       mContentValues.put(PAYMENT_CODE, checkOutDBDs.getPaymentCode());

      mSqLiteDb.update(TABLE_NAME, mContentValues, "",null);


   }*/

   public void updateCouponCode(String couponCode) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();

      mContentValues.put(COUPON_CODE, couponCode);

      mSqLiteDb.update(TABLE_NAME, mContentValues, "",null);


   }

   public void updateCouponId(String couponId) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();

      mContentValues.put(COUPON_ID, couponId);

      mSqLiteDb.update(TABLE_NAME, mContentValues, "",null);


   }

   public void updatePaymentListId(String paymentListId) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();

      mContentValues.put(PAYMENT_LIST_ID, paymentListId);

      mSqLiteDb.update(TABLE_NAME, mContentValues, "",null);


   }

   public void updatePaymentCode(String paymentCode) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();

      mContentValues.put(PAYMENT_CODE, paymentCode);

      mSqLiteDb.update(TABLE_NAME, mContentValues, "",null);

   }

   public void updateAddressId(String addressId) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();

      mContentValues.put(ADDRESS_ID, addressId);

      mSqLiteDb.update(TABLE_NAME, mContentValues, "",null);


   }

   public void updateContactLessDelivery(String contactLessDelivery) {

      SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
      ContentValues mContentValues = new ContentValues();

      mContentValues.put(CONTACT_LESS_DELIVERY, contactLessDelivery);

      mSqLiteDb.update(TABLE_NAME, mContentValues, "",null);


   }

   public CheckOutDBDataSet getDetails() {

      String mQuery = SELECT +"* " + FROM + TABLE_NAME;

      CheckOutDBDataSet mResult = new CheckOutDBDataSet();
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      Cursor mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

            mResult.setCouponCode(mCursor.getString(mCursor.getColumnIndex(COUPON_CODE)));
            mResult.setCouponId(mCursor.getString(mCursor.getColumnIndex(COUPON_ID)));

            mResult.setAddressId(mCursor.getString(mCursor.getColumnIndex(ADDRESS_ID)));
            mResult.setPaymentListId(mCursor.getString(mCursor.getColumnIndex(PAYMENT_LIST_ID)));

            mResult.setContactLessDeliveryChecked(mCursor.getString(mCursor.getColumnIndex(CONTACT_LESS_DELIVERY)));

            mResult.setPaymentCode(mCursor.getString(mCursor.getColumnIndex(PAYMENT_CODE)));

         }

         if(!mCursor.isClosed()){
            mCursor.close();
         }
         return mResult;
      }
      if(mCursor != null && !mCursor.isClosed()){
         mCursor.close();
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


   public void print() {
      String mQuery = SELECT +"* " + FROM + TABLE_NAME;

      LoginDataSet mResult = new LoginDataSet();
      SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
      Cursor mCursor = mSqLiteDb.rawQuery(mQuery, null);
      if (mCursor != null && mCursor.getCount() > 0) {
         for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

            String COUPONCODE = mCursor.getString(mCursor.getColumnIndex(COUPON_CODE));
            String COUPONID = mCursor.getString(mCursor.getColumnIndex(COUPON_ID));

            String ADDRESSID = mCursor.getString(mCursor.getColumnIndex(ADDRESS_ID));
            String PAYMENTLISTID = mCursor.getString(mCursor.getColumnIndex(PAYMENT_LIST_ID));

            String CONTACTLESSDELIVERY = mCursor.getString(mCursor.getColumnIndex(CONTACT_LESS_DELIVERY));

            String PAYMENTCODE = mCursor.getString(mCursor.getColumnIndex(PAYMENT_CODE));

            if(COUPONCODE != null && !COUPONCODE.isEmpty()){
               //Log.e("COUPONCODE",COUPONCODE);
            }else {
               //Log.e("COUPONCODE","empty");
            }
            if(COUPONID != null && !COUPONID.isEmpty()){
               //Log.e("COUPONID",COUPONID);
            }else {
               //Log.e("COUPONID","empty");
            }

            if(ADDRESSID != null && !ADDRESSID.isEmpty()){
               //Log.e("ADDRESSID",ADDRESSID);
            }else {
               //Log.e("ADDRESSID","empty");
            }
            if(PAYMENTLISTID != null && !PAYMENTLISTID.isEmpty()){
               //Log.e("PAYMENTLISTID",PAYMENTLISTID);
            }else {
               //Log.e("PAYMENTLISTID","empty");
            }

            if(PAYMENTCODE != null && !PAYMENTCODE.isEmpty()){
               //Log.e("PAYMENTCODE",PAYMENTCODE);
            }else {
               //Log.e("PAYMENTCODE","empty");
            }

            if(CONTACTLESSDELIVERY != null && !CONTACTLESSDELIVERY.isEmpty()){
               //Log.e("CONTACTLESSDELIVERY",CONTACTLESSDELIVERY);
            }else {
               //Log.e("CONTACTLESSDELIVERY","empty");
            }


            Log.d("*******************","**************************");


         }

         if(!mCursor.isClosed()){
            mCursor.close();
         }

      }
      if(mCursor != null && !mCursor.isClosed()){
         mCursor.close();
      }

   }
}
