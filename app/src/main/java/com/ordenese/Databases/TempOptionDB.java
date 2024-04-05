package com.ordenese.Databases;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.ordenese.DataSets.UserProductOptionDataSet;

import java.util.ArrayList;

public class TempOptionDB extends SQLiteOpenHelper {


    private final static String name = "TempOptionDataBase";
    private final static int version = 1;
    private final static String TABLE_NAME = "temp_option_table";
    private final static String OPTION_INDEX = "option_index";
    private final static String BRANCH_ID = "branch_id";
    private final static String SECTION_ID = "section_id";
    private final static String PRODUCT_ID = "product_id";
    private final static String OPTION_ID = "option_id";
    private final static String PARENT_OPTION_ID = "parent_option_id";
    private final static String OPTION_TYPE = "option_type";
    private final static String PRICE = "price";

    private final static String CREATE = "create table " + TABLE_NAME + " (" + OPTION_INDEX + " text," +
            BRANCH_ID + " text," + SECTION_ID + " text," + PRODUCT_ID + " text," + PARENT_OPTION_ID + " text," + OPTION_ID + " text," + OPTION_TYPE + " text," /*+ OPTION_REQUIRE + " text," +
            MINIMUM_LIMIT + " text," + MAXIMUM_LIMIT + " text," + IS_OFFER*/ /*+ " text," */ + PRICE + " text);";
           /* + OFFER_PRICE + " text," + OPTION + " text);";*/

    private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private final static String DELETE_TABLE = "DELETE FROM " + TABLE_NAME;
    private final static String SELECT = "select ";
    private final static String FROM = "from ";
    private final static String WHERE = "where ";
    private final static String AND = " and ";

    private Cursor mCursor;

    private static TempOptionDB sInstance;

    public static synchronized TempOptionDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TempOptionDB(context.getApplicationContext());
        }
        return sInstance;
    }

    private TempOptionDB(Context context) {
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


    public void addProductOption(UserProductOptionDataSet userProductOptionDs) {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(OPTION_INDEX, userProductOptionDs.getOptionIndex());
        mContentValues.put(BRANCH_ID, userProductOptionDs.getBranchId());
        mContentValues.put(SECTION_ID, userProductOptionDs.getSectionId());
        mContentValues.put(PRODUCT_ID, userProductOptionDs.getProductId());
        mContentValues.put(OPTION_ID, userProductOptionDs.getOptionId());
        mContentValues.put(PARENT_OPTION_ID, userProductOptionDs.getParentOptionId());
        mContentValues.put(OPTION_TYPE, userProductOptionDs.getOptionType());
        //  mContentValues.put(OPTION_REQUIRE, userProductOptionDs.getOptionRequire());
        //  mContentValues.put(MINIMUM_LIMIT, userProductOptionDs.getMinimumLimit());
        //  mContentValues.put(MAXIMUM_LIMIT, userProductOptionDs.getMaximumLimit());
        // mContentValues.put(IS_OFFER, userProductOptionDs.getIsOffer());
        mContentValues.put(PRICE, userProductOptionDs.getPrice());
        // mContentValues.put(OFFER_PRICE, userProductOptionDs.getOfferPrice());
        //  mContentValues.put(OPTION, userProductOptionDs.getOption());
        mSqLiteDb.insert(TABLE_NAME, null, mContentValues);
    }


    public void updateProductOption(UserProductOptionDataSet userProductOptionDs, String branchId, String sectionId, String productId, String parentOptionIndex) {

        //   BRANCH_ID,PRODUCT_ID,OPTION_ID,OPTION_REQUIRE,MINIMUM_LIMIT,MAXIMUM_LIMIT,IS_OFFER,PRICE,OFFER_PRICE,OPTION
        // setOptionIndex,setRestaurantId,setProductId,setParentOptionId,setOptionId,setPrice
        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
        //  mContentValues.put(OPTION_PRODUCT_INDEX, userProductOptionDs.getOptionProductIndex());
        mContentValues.put(OPTION_INDEX, userProductOptionDs.getOptionIndex());
        mContentValues.put(BRANCH_ID, userProductOptionDs.getBranchId());
        mContentValues.put(SECTION_ID, userProductOptionDs.getSectionId());
        mContentValues.put(PRODUCT_ID, userProductOptionDs.getProductId());
        mContentValues.put(OPTION_ID, userProductOptionDs.getOptionId());
        mContentValues.put(PARENT_OPTION_ID, userProductOptionDs.getParentOptionId());
        mContentValues.put(OPTION_TYPE, userProductOptionDs.getOptionType());
        //  mContentValues.put(OPTION_REQUIRE, userProductOptionDs.getOptionRequire());
        //  mContentValues.put(MINIMUM_LIMIT, userProductOptionDs.getMinimumLimit());
        //  mContentValues.put(MAXIMUM_LIMIT, userProductOptionDs.getMaximumLimit());
        // mContentValues.put(IS_OFFER, userProductOptionDs.getIsOffer());
        mContentValues.put(PRICE, userProductOptionDs.getPrice());
        // mContentValues.put(OFFER_PRICE, userProductOptionDs.getOfferPrice());
        //  mContentValues.put(OPTION, userProductOptionDs.getOption());
        //  mSqLiteDb.update(TABLE_NAME, mContentValues, OPTION_INDEX + "=" + optionIndex, null);
        mSqLiteDb.update(TABLE_NAME, mContentValues, BRANCH_ID + "=" + branchId + AND + SECTION_ID + "=" + sectionId + AND + PRODUCT_ID + "=" + productId + AND + PARENT_OPTION_ID + "=" + parentOptionIndex, null);


        //  mSqLiteDb.update(TABLE_NAME, mContentValues, BRANCH_ID+"="+branchId+AND+SECTION_ID+"="+sectionId+AND+PRODUCT_ID+"="+productId+AND+PRODUCT_INDEX+"="+productIndex, null);
        // UserCartDBDataSet userCartDs, String branchId,String sectionId,String productId,String productIndex) {

    }
    public void deleteProductOption(String branchId, String sectionId, String productId, String parentOptionIndex) {
        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();

        // Define the WHERE clause to identify the row to delete
        String whereClause = BRANCH_ID + "=? AND " + SECTION_ID + "=? AND " + PRODUCT_ID + "=? AND " + PARENT_OPTION_ID + "=?";

        // Define the values for the WHERE clause
        String[] whereArgs = {branchId, sectionId, productId, parentOptionIndex};

        // Perform the deletion
        mSqLiteDb.delete(TABLE_NAME, whereClause, whereArgs);
    }
    @SuppressLint("Range")
    public boolean isOptionExists(String branchId, String sectionId, String productId, String parentOptionId) {

        String mQuery = SELECT + "* " + FROM + TABLE_NAME + " " + WHERE + BRANCH_ID + "=" + branchId + /*AND + SECTION_ID + "=" + sectionId +*/ AND + PRODUCT_ID + "=" + productId + AND + PARENT_OPTION_ID + "=" + parentOptionId;

        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(OPTION_INDEX));
            }
            mCursor.close();
        }
        return (mResult != null &&!mResult.equals(""));

    }

    public boolean isOptionValueExists(String branchId, String sectionId, String productId, String parentOptionId, String optionValueId) {

        String mQuery = SELECT + "* " + FROM + TABLE_NAME + " " + WHERE + BRANCH_ID + "=" + branchId + AND + SECTION_ID + "=" + sectionId + AND + PRODUCT_ID + "="
                + productId + AND + PARENT_OPTION_ID + "=" + parentOptionId + AND + OPTION_ID + "=" + optionValueId;

        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(OPTION_ID));
            }
            mCursor.close();
        }
        return (mResult != null &&!mResult.equals(""));

    }


    public void deleteProductOptionValueId(String branchId, String sectionId, String productId, String parentOptionId, String optionValueId) {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        mSqLiteDb.delete(TABLE_NAME, BRANCH_ID + "=" + branchId + AND + SECTION_ID + "=" + sectionId
                + AND + PRODUCT_ID + "=" + productId + AND + PARENT_OPTION_ID + "=" + parentOptionId + AND + OPTION_ID + "=" + optionValueId, null);

    }


    public int getProductOptionCount(String branchId, String sectionId, String productId, String parentOptionId) {

        String mQuery = SELECT + "* " + FROM + TABLE_NAME + " " + WHERE + BRANCH_ID + "=" + branchId +/* AND + SECTION_ID + "=" + sectionId +*/
                AND + PRODUCT_ID + "=" + productId + AND + PARENT_OPTION_ID + "=" + parentOptionId;
        int mResult = 0;
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult++;
            }
            mCursor.close();
        }
        return mResult;

    }



    @SuppressLint("Range")
    public ArrayList<UserProductOptionDataSet> getAllOptionsList() {


        // OPTION_INDEX,BRANCH_ID,SECTION_ID,PRODUCT_ID,OPTION_ID,PARENT_OPTION_ID,OPTION_TYPE

        String mQuery = SELECT +"* "+ FROM + TABLE_NAME;
        ArrayList<UserProductOptionDataSet> mResult = new ArrayList<>();
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

                UserProductOptionDataSet mUserProductOptionDs = new UserProductOptionDataSet();
                mUserProductOptionDs.setOptionIndex(mCursor.getString(mCursor.getColumnIndex(OPTION_INDEX)));
                mUserProductOptionDs.setBranchId(mCursor.getString(mCursor.getColumnIndex(BRANCH_ID)));
                mUserProductOptionDs.setSectionId(mCursor.getString(mCursor.getColumnIndex(SECTION_ID)));
                mUserProductOptionDs.setProductId(mCursor.getString(mCursor.getColumnIndex(PRODUCT_ID)));
                mUserProductOptionDs.setParentOptionId(mCursor.getString(mCursor.getColumnIndex(PARENT_OPTION_ID)));
                mUserProductOptionDs.setOptionId(mCursor.getString(mCursor.getColumnIndex(OPTION_ID)));
                mUserProductOptionDs.setOptionType(mCursor.getString(mCursor.getColumnIndex(OPTION_TYPE)));
                mUserProductOptionDs.setPrice(mCursor.getString(mCursor.getColumnIndex(PRICE)));
                mResult.add(mUserProductOptionDs);

            }
            mCursor.close();
        }
        return mResult;

    }


    public void toPrint() {
        String mQuery = SELECT + "* " + FROM + TABLE_NAME;

        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {

          //  Log.d("**************", "***************");

            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

              //  //Log.e("*******************", "*******************");

                String mOptionInd = mCursor.getString(mCursor.getColumnIndex(OPTION_INDEX));
                String mRestaurantId = mCursor.getString(mCursor.getColumnIndex(BRANCH_ID));
                String mSecId = mCursor.getString(mCursor.getColumnIndex(SECTION_ID));
                String mProId = mCursor.getString(mCursor.getColumnIndex(PRODUCT_ID));
                String mPOPId = mCursor.getString(mCursor.getColumnIndex(PARENT_OPTION_ID));
                String mOPId = mCursor.getString(mCursor.getColumnIndex(OPTION_ID));
                String mOpType = mCursor.getString(mCursor.getColumnIndex(OPTION_TYPE));
                String mPrice = mCursor.getString(mCursor.getColumnIndex(PRICE));

                //  String sdfsf = mPrice;

                /* //Log.e("********TEmp***********", "*********OptionDB**********");
                if(!mOptionInd.isEmpty()){
                    Log.d("OptionIndex ", mOptionInd);
                }if(!mRestaurantId.isEmpty()){
                    Log.d("RestaurantId ", mRestaurantId);
                }if(!mSecId.isEmpty()){
                    Log.d("SectionId ", mSecId);
                }if(!mProId.isEmpty()){
                    Log.d("ProductId ", mProId);
                }if(!mPOPId.isEmpty()){
                    Log.d("ParentOptionId ", mPOPId);
                }if(!mOPId.isEmpty()){
                    Log.d("OptionId ", mOPId);
                }if(!mOpType.isEmpty()){
                    Log.d("OptionType ", mOpType);
                }if(!mPrice.isEmpty()){
                    Log.d("Price ", mPrice);
                }*/


              //  //Log.e("*******************", "*******************");

            }
            mCursor.close();

           // Log.d("**************", "***************");


        } else {
          //  //Log.e("***toPrint()****", "empty.");
        }

    }

    public void deleteProductOptionDB() {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        mSqLiteDb.execSQL(DELETE_TABLE);
       // //Log.e("***TempOptionDB***", "Deleted");

    }

    public void removeBranchCartDetails(String branchId/*,String sectionId,String productId*/) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, BRANCH_ID + "=" + branchId/*+AND+SECTION_ID+"="+sectionId+AND+PRODUCT_ID+"="+productId*/, null);

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



     /* public String getOptionProductIndex(String branchId,String sectionId,String productId) {

        String mQuery = SELECT + "* " + FROM + TABLE_NAME+" "+WHERE+BRANCH_ID+"="+branchId+AND+SECTION_ID+"="+sectionId+AND+PRODUCT_ID+"="+productId;

        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(OPTION_PRODUCT_INDEX));
            }
            mCursor.close();
        }
        return mResult;

    }*/



   /* public void removeOptionEntryByOptionProductInded(String optionProductIndex) {

        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        mSqLiteDb.delete(TABLE_NAME, OPTION_PRODUCT_INDEX + "=" + optionProductIndex, null);

    }*/




  /*  public ArrayList<Integer> getAllCartOptionIndexes() {

        String mQuery = SELECT +"* "+ FROM + TABLE_NAME;

        ArrayList<Integer> mResultList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = db.rawQuery(mQuery, null);
        if (mCursor != null) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResultList.add(mCursor.getInt(mCursor.getColumnIndex(OPTION_PRODUCT_INDEX)));
            }
            mCursor.close();
            // return result;
        }
        return mResultList;
    }*/


    /*public void updateSingleProductOption(UserProductOptionDataSet userProductOptionDs,String optionIndex) {


        SQLiteDatabase mSqLiteDb = this.getWritableDatabase();
        ContentValues mContentValues = new ContentValues();
       // mContentValues.put(BRANCH_ID, userProductOptionDs.getRestaurantId());
       // mContentValues.put(PRODUCT_ID, userProductOptionDs.getProductId());
        mContentValues.put(OPTION_ID, userProductOptionDs.getOptionId());
        mContentValues.put(PARENT_OPTION_ID, userProductOptionDs.getParentOptionId());
        mContentValues.put(OPTION_TYPE, userProductOptionDs.getOptionType());
       // mContentValues.put(OPTION_REQUIRE, userProductOptionDs.getOptionRequire());
      //  mContentValues.put(MINIMUM_LIMIT, userProductOptionDs.getMinimumLimit());
      //  mContentValues.put(MAXIMUM_LIMIT, userProductOptionDs.getMaximumLimit());
       // mContentValues.put(IS_OFFER, userProductOptionDs.getIsOffer());
        mContentValues.put(PRICE, userProductOptionDs.getPrice());
      //  mContentValues.put(OFFER_PRICE, userProductOptionDs.getOfferPrice());
       // mContentValues.put(OPTION, userProductOptionDs.getOption());
        mSqLiteDb.update(TABLE_NAME, mContentValues, OPTION_INDEX + "=" + optionIndex, null);


    }*/

   /* public UserProductOptionDataSet getSingleProductOption(String optionIndex) {

        String mQuery = SELECT +"* "+ FROM + TABLE_NAME+" "+WHERE+OPTION_INDEX+"="+optionIndex;
        UserProductOptionDataSet mResult = new UserProductOptionDataSet();
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

                mResult.setOptionIndex(mCursor.getString(mCursor.getColumnIndex(OPTION_INDEX)));
                mResult.setRestaurantId(mCursor.getString(mCursor.getColumnIndex(BRANCH_ID)));
                mResult.setProductId(mCursor.getString(mCursor.getColumnIndex(PRODUCT_ID)));
                mResult.setParentOptionId(mCursor.getString(mCursor.getColumnIndex(PARENT_OPTION_ID)));
                mResult.setOptionId(mCursor.getString(mCursor.getColumnIndex(OPTION_ID)));
                mResult.setOptionType(mCursor.getString(mCursor.getColumnIndex(OPTION_TYPE)));
                mResult.setPrice(mCursor.getString(mCursor.getColumnIndex(PRICE)));
            }
            mCursor.close();
        }
        return mResult;

    }*/

   /* public ArrayList<UserProductOptionDataSet> getMultiProductOption(String optionIndex) {

        String mQuery = SELECT +"* "+ FROM + TABLE_NAME+" "+WHERE+OPTION_INDEX+"="+optionIndex;
        ArrayList<UserProductOptionDataSet> mResult = new ArrayList<>();
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

                UserProductOptionDataSet mUserProductOptionDs = new UserProductOptionDataSet();
                mUserProductOptionDs.setOptionIndex(mCursor.getString(mCursor.getColumnIndex(OPTION_INDEX)));
                mUserProductOptionDs.setRestaurantId(mCursor.getString(mCursor.getColumnIndex(BRANCH_ID)));
                mUserProductOptionDs.setProductId(mCursor.getString(mCursor.getColumnIndex(PRODUCT_ID)));
                mUserProductOptionDs.setParentOptionId(mCursor.getString(mCursor.getColumnIndex(PARENT_OPTION_ID)));
                mUserProductOptionDs.setOptionId(mCursor.getString(mCursor.getColumnIndex(OPTION_ID)));
                mUserProductOptionDs.setOptionType(mCursor.getString(mCursor.getColumnIndex(OPTION_TYPE)));
                mUserProductOptionDs.setPrice(mCursor.getString(mCursor.getColumnIndex(PRICE)));
                mResult.add(mUserProductOptionDs);

            }
            mCursor.close();
        }
        return mResult;

    }*/

    /*public ArrayList<UserProductOptionDataSet> getAllOptionsList(String optionIndex) {

        String mQuery = SELECT +"* "+ FROM + TABLE_NAME+" "+WHERE+OPTION_INDEX+"="+optionIndex;
        ArrayList<UserProductOptionDataSet> mResult = new ArrayList<>();
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

                UserProductOptionDataSet mUserProductOptionDs = new UserProductOptionDataSet();
                mUserProductOptionDs.setOptionProductIndex(mCursor.getString(mCursor.getColumnIndex(OPTION_PRODUCT_INDEX)));
                mUserProductOptionDs.setOptionIndex(mCursor.getString(mCursor.getColumnIndex(OPTION_INDEX)));
                mUserProductOptionDs.setRestaurantId(mCursor.getString(mCursor.getColumnIndex(BRANCH_ID)));
                mUserProductOptionDs.setProductId(mCursor.getString(mCursor.getColumnIndex(PRODUCT_ID)));
                mUserProductOptionDs.setParentOptionId(mCursor.getString(mCursor.getColumnIndex(PARENT_OPTION_ID)));
                mUserProductOptionDs.setOptionId(mCursor.getString(mCursor.getColumnIndex(OPTION_ID)));
                mUserProductOptionDs.setOptionType(mCursor.getString(mCursor.getColumnIndex(OPTION_TYPE)));
                mUserProductOptionDs.setPrice(mCursor.getString(mCursor.getColumnIndex(PRICE)));
                mResult.add(mUserProductOptionDs);

            }
            mCursor.close();
        }
        return mResult;

    }*/



   /* public String getOptionType(String optionIndex) {

        String mQuery = SELECT + "* " + FROM + TABLE_NAME+" "+WHERE+OPTION_INDEX+"="+optionIndex;

        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(OPTION_TYPE));
            }
            mCursor.close();
        }
        return mResult;

    }*/

   /* public String getParentOptionId(String optionIndex) {

        String mQuery = SELECT + "* " + FROM + TABLE_NAME+" "+WHERE+OPTION_INDEX+"="+optionIndex;

        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(PARENT_OPTION_ID));
            }
            mCursor.close();
        }
        return mResult;

    }*/




   /* public boolean isOptionExists(String optionIndex,String branchId, String sectionId, String productId) {

        String mQuery = SELECT + "* " + FROM + TABLE_NAME+" "+WHERE+OPTION_INDEX+"="+optionIndex;

        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(OPTION_INDEX));
            }
            mCursor.close();
        }
        return (!mResult.equals(""));

    }*/


    /*public boolean isOptionExists(String optionIndex) {

        String mQuery = SELECT + "* " + FROM + TABLE_NAME+" "+WHERE+OPTION_INDEX+"="+optionIndex;

        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(OPTION_INDEX));
            }
            mCursor.close();
        }
        return (!mResult.equals(""));

    }*/

   /* public boolean isMultiOptionExists(String optionIndex,String optionId) {

        String mQuery = SELECT + "* " + FROM + TABLE_NAME+" "+WHERE+OPTION_INDEX+"="+optionIndex+
                             AND+OPTION_ID+"="+optionId ;

        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(OPTION_ID));
            }
            mCursor.close();
        }
        return (!mResult.equals(""));

    }*/



    /*public boolean isRestaurantHaveCartItem() {

        String mQuery = SELECT + "* " + FROM + TABLE_NAME;

        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(BRANCH_ID));
            }
            mCursor.close();
        }
        return (!mResult.equals(""));

    }

    public boolean isOptionSelected() {

        String mQuery = SELECT + "* " + FROM + TABLE_NAME;

        String mResult = "";
        SQLiteDatabase mSqLiteDb = this.getReadableDatabase();
        mCursor = mSqLiteDb.rawQuery(mQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mResult = mCursor.getString(mCursor.getColumnIndex(OPTION_ID));
            }
            mCursor.close();
        }
        return (!mResult.equals(""));

    }*/



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
