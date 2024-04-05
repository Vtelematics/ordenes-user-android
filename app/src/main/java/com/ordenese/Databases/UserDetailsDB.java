package com.ordenese.Databases;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ordenese.DataSets.LoginDataSet;

public class UserDetailsDB extends SQLiteOpenHelper {

    private final static String name = "UserDetailsDataBase";
    private final static int version = 1;
    private final static String TABLE_NAME = "user_details_table";

    private final static String CUSTOMER_KEY = "customer_key";
    private final static String CUSTOMER_ID = "customer_id";
    private final static String FIRST_NAME = "first_name";
    private final static String LAST_NAME = "last_name";
    private final static String EMAIL = "email";
    private final static String MOBILE = "mobile";
    private final static String MOBILE_COUNTRY_CODE_ID = "mobile_country_code_id";
    private final static String MOBILE_COUNTRY_CODE = "mobile_country_code";
    private final static String IMAGE = "image";

    private final static String CREATE = "create table " + TABLE_NAME + " (" + CUSTOMER_ID + " integer primary key," +
            CUSTOMER_KEY + " text," +FIRST_NAME + " text," + LAST_NAME + " text," +
            EMAIL + " text," +MOBILE + " text,"+ MOBILE_COUNTRY_CODE_ID + " text," + IMAGE + " text,"+ MOBILE_COUNTRY_CODE + " text);";

    private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
    private final static String SELECT = "select ";
    private final static String FROM = "from ";

    private static UserDetailsDB sInstance;

    public static synchronized UserDetailsDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new UserDetailsDB(context.getApplicationContext());
        }
        return sInstance;
    }

    private UserDetailsDB(Context context) {
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

    public void addUserDetails(LoginDataSet loginDetails) {
        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(CUSTOMER_KEY, loginDetails.getCustomerKey());
        mContentValues.put(CUSTOMER_ID, loginDetails.getCustomerId());
        mContentValues.put(FIRST_NAME, loginDetails.getFirstName());
        mContentValues.put(LAST_NAME, loginDetails.getLastName());
        mContentValues.put(EMAIL, loginDetails.getEmail());
        mContentValues.put(MOBILE, loginDetails.getTelephone());
        mContentValues.put(MOBILE_COUNTRY_CODE_ID, loginDetails.getMobileCountryCodeId());
        mContentValues.put(MOBILE_COUNTRY_CODE, loginDetails.getMobileCountryCode());
        mContentValues.put(IMAGE, loginDetails.getImage());
        mSqLiteDb.insert(TABLE_NAME, null, mContentValues);
    }

    public void updateUserDetails(LoginDataSet loginDetails) {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(CUSTOMER_KEY, loginDetails.getCustomerKey());
        mContentValues.put(CUSTOMER_ID, loginDetails.getCustomerId());
        mContentValues.put(FIRST_NAME, loginDetails.getFirstName());
        mContentValues.put(LAST_NAME, loginDetails.getLastName());
        mContentValues.put(EMAIL, loginDetails.getEmail());
        mContentValues.put(MOBILE, loginDetails.getTelephone());
        mContentValues.put(MOBILE_COUNTRY_CODE_ID, loginDetails.getMobileCountryCodeId());
        mContentValues.put(MOBILE_COUNTRY_CODE, loginDetails.getMobileCountryCode());
        mContentValues.put(IMAGE, loginDetails.getImage());
        mSqLiteDb.update(TABLE_NAME, mContentValues,CUSTOMER_ID+"="+loginDetails.getCustomerId(),null);


    }

   /* public void removeTableCourseData(String tableName,String courseName) {

        SQLiteDatabase db = this.getWritableDatabase();
        //  db.delete(TABLE_NAME, TableName + "=" + tableName+AND+  AlertCourseName+"=" + courseName, null);
        db.delete(TABLE_NAME, TableName + "=?" + AND+  AlertCourseName+"=?", new String[]{tableName,courseName});
        db.update(TABLE_NAME, contentValues, TableName + "=?"+AND+AlertCourseName+"=?"+AND+ ProductId +"=?"+AND+ KitchenId +"=?",  new String[]{tableName,courseName,productId,kitchenId});
    }*/

    public void updateUserImage(String image,String customerId) {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(IMAGE, image);
        mSqLiteDb.update(TABLE_NAME, contentValues,CUSTOMER_ID+"=?", new String[]{customerId});


    }


    @SuppressLint("Range")
    public boolean isUserLoggedIn() {

        String mQuery = SELECT + "* " + FROM + TABLE_NAME;

        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        Cursor mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(CUSTOMER_ID));
            }
            mCursor.close();
        }

        if(mCursor != null && !mCursor.isClosed()){
            mCursor.close();
        }

        return (mResult != null &&!mResult.equals(""));

    }

    @SuppressLint("Range")
    public LoginDataSet getUserDetails() {
        String mQuery = SELECT +"* " + FROM + TABLE_NAME;

        LoginDataSet mResult = new LoginDataSet();
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        Cursor mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult.setCustomerKey(mCursor.getString(mCursor.getColumnIndex(CUSTOMER_KEY)));
                mResult.setCustomerId(mCursor.getString(mCursor.getColumnIndex(CUSTOMER_ID)));
                mResult.setFirstName(mCursor.getString(mCursor.getColumnIndex(FIRST_NAME)));
                mResult.setLastName(mCursor.getString(mCursor.getColumnIndex(LAST_NAME)));
                mResult.setEmail(mCursor.getString(mCursor.getColumnIndex(EMAIL)));
                mResult.setTelephone(mCursor.getString(mCursor.getColumnIndex(MOBILE)));
                mResult.setMobileCountryCodeId(mCursor.getString(mCursor.getColumnIndex(MOBILE_COUNTRY_CODE_ID)));
                mResult.setMobileCountryCode(mCursor.getString(mCursor.getColumnIndex(MOBILE_COUNTRY_CODE)));
                mResult.setImage(mCursor.getString(mCursor.getColumnIndex(IMAGE)));
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



    public void deleteUserDetailsDB() {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        mSqLiteDb.execSQL(DELETE_TABLE);
      //  Log.d("***UserDetailsDB***","Deleted");

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



   /* public void printOptionList() {
        String query = SELECT + "* " + FROM + TABLE_NAME;
        // ArrayList<String> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = db.rawQuery(query, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            Log.d("*********", "***********");
            //  int mIndex = 0;
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                //result.add(mCursor.getString(mCursor.getColumnIndex(PARENT_OPTION_ID)));
               // //Log.e("DBList ", "Option_id : "+mCursor.getString(mCursor.getColumnIndex(OPTION_ID))
                    //    +" Option_position : "+mCursor.getString(mCursor.getColumnIndex(OPTION_POSITION))*//*+" "+
                        mCursor.getString(mCursor.getColumnIndex(CHILD_OPTION_TITLE));
                // result[mIndex] =  mCursor.getString(mCursor.getColumnIndex(OPTION_ID));
            }
            mCursor.close();
            //return result;

        } else {
            Log.d("****DB*****", "******EMPTY*****");
        }
        Log.d("*********", "***********");
        //return result[0] = PreDefinedNames.EMPTY;
    }
    public String[] getOptionList() {
        String query = SELECT +"* "+ FROM + TABLE_NAME;
        ArrayList<String> mTempResult = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = db.rawQuery(query, null);
        if (mCursor != null) {
            // int mIndex = 0;
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mTempResult.add(mCursor.getString(mCursor.getColumnIndex(OPTION_ID)));
                // ++mIndex;
            }
            mCursor.close();
            String[] result = new String[mTempResult.size()];
            for(int a=0;a<mTempResult.size();a++){
                result[a] = mTempResult.get(a);
            }

            return result;
        }
        return null;
    }*/

    /*public void update_option_list(String parentOptionId, String parentIndex,String optionTypeId,String optionTitle) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PARENT_INDEX, parentIndex);
        contentValues.put(OPTION_TYPE_ID, optionTypeId);
        contentValues.put(OPTION_TITLE, optionTitle);
        db.update(TABLE_NAME, contentValues, PARENT_OPTION_ID + "=" + parentOptionId, null);
    }*/

    @SuppressLint("Range")
    public void print() {
        String mQuery = SELECT +"* " + FROM + TABLE_NAME;

        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        Cursor mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

                String CUSTOMERKEY = mCursor.getString(mCursor.getColumnIndex(CUSTOMER_KEY));
                String CUSTOMERID = mCursor.getString(mCursor.getColumnIndex(CUSTOMER_ID));
                String FIRSTNAME = mCursor.getString(mCursor.getColumnIndex(FIRST_NAME));
                String LASTNAME = mCursor.getString(mCursor.getColumnIndex(LAST_NAME));
                String EMAIL_ = mCursor.getString(mCursor.getColumnIndex(EMAIL));
                String MOBILE_ = mCursor.getString(mCursor.getColumnIndex(MOBILE));
                String MOBILECOUNTRY_CODE_ID = mCursor.getString(mCursor.getColumnIndex(MOBILE_COUNTRY_CODE_ID));
                String MOBILECOUNTRY_CODE = mCursor.getString(mCursor.getColumnIndex(MOBILE_COUNTRY_CODE));
                String IMAGE_ = mCursor.getString(mCursor.getColumnIndex(IMAGE));


                if(CUSTOMERKEY != null && !CUSTOMERKEY.isEmpty()){
                    //Log.e("CUSTOMERKEY",CUSTOMERKEY);
                }else {
                    //Log.e("CUSTOMERKEY","empty");
                }
                if(CUSTOMERID != null && !CUSTOMERID.isEmpty()){
                    //Log.e("CUSTOMERID",CUSTOMERID);
                }else {
                    //Log.e("CUSTOMERID","empty");
                }
                if(FIRSTNAME != null && !FIRSTNAME.isEmpty()){
                    //Log.e("FIRSTNAME",FIRSTNAME);
                }else {
                    //Log.e("FIRSTNAME","empty");
                }
                if(LASTNAME != null && !LASTNAME.isEmpty()){
                    //Log.e("LASTNAME",LASTNAME);
                }else {
                    //Log.e("LASTNAME","empty");
                }
                if(EMAIL_ != null && !EMAIL_.isEmpty()){
                    //Log.e("EMAIL_",EMAIL_);
                }else {
                    //Log.e("EMAIL_","empty");
                }
                if(MOBILE_ != null && !MOBILE_.isEmpty()){
                    //Log.e("MOBILE_",MOBILE_);
                }else {
                    //Log.e("MOBILE_","empty");
                }
                if(MOBILECOUNTRY_CODE_ID != null && !MOBILECOUNTRY_CODE_ID.isEmpty()){
                    //Log.e("MOBILECOUNTRY_CODE_ID",MOBILECOUNTRY_CODE_ID);
                }else {
                    //Log.e("MOBILECOUNTRY_CODE_ID","empty");
                }
                if(MOBILECOUNTRY_CODE != null && !MOBILECOUNTRY_CODE.isEmpty()){
                    //Log.e("MOBILECOUNTRY_CODE",MOBILECOUNTRY_CODE);
                }else {
                    //Log.e("MOBILECOUNTRY_CODE","empty");
                }
                if(IMAGE_ != null && !IMAGE_.isEmpty()){
                    Log.e("IMAGE_",IMAGE_);
                }else {
                    Log.e("IMAGE_","empty");
                }





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