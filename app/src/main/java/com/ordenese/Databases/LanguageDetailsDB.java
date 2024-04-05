package com.ordenese.Databases;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ordenese.DataSets.LanguageDataSet;

public class LanguageDetailsDB extends SQLiteOpenHelper {

    final static String name = "language_details_db";
    final static int version = 1;
    final static String TABLE_NAME_LANGUAGE_DETAILS = "language_details_table";

    final static String LANGUAGE_ID = "language_id";
    final static String LANGUAGE_CODE = "language_code";

    final static String DROP_TABLE_LANGUAGE = "DROP TABLE IF EXISTS " + TABLE_NAME_LANGUAGE_DETAILS;
    final static String DELETE_TABLE_LANGUAGE = "DELETE FROM " + TABLE_NAME_LANGUAGE_DETAILS;
    final static String SELECT_VALUE_SELECT = "select ";
    final static String SELECT_VALUE_FROM = "from ";
    Cursor cursor;

    final static String CREATE_ACCOUNT_NEW = "create table " + TABLE_NAME_LANGUAGE_DETAILS + "(" + LANGUAGE_ID + " integer primary key,"+ LANGUAGE_CODE+" text);";

    private static LanguageDetailsDB sInstance;

    public static synchronized LanguageDetailsDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LanguageDetailsDB(context.getApplicationContext());
        }
        return sInstance;
    }

    private LanguageDetailsDB(Context context) {
        super(context, name, null, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ACCOUNT_NEW);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_LANGUAGE);
        onCreate(db);
    }

    public Boolean insert_language_detail(LanguageDataSet language) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LANGUAGE_ID, language.getLanguageId());
        contentValues.put(LANGUAGE_CODE, language.getCode());
        db.insert(TABLE_NAME_LANGUAGE_DETAILS, null, contentValues);
        return true;
    }


    public boolean check_language_selected() {
        String query = SELECT_VALUE_SELECT + LANGUAGE_ID+ " " + SELECT_VALUE_FROM + TABLE_NAME_LANGUAGE_DETAILS;
        String result = null;
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.getCount() > 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                result = cursor.getString(cursor.getColumnIndex(LANGUAGE_ID));
            }
            cursor.close();
        }
        return (result != null);
    }



    public LanguageDataSet get_language_Details() {

        String select = SELECT_VALUE_SELECT +"* " + SELECT_VALUE_FROM + TABLE_NAME_LANGUAGE_DETAILS;
        LanguageDataSet result = new LanguageDataSet();
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.rawQuery(select, null);
        if (cursor != null) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                result.setLanguageId(cursor.getString(cursor.getColumnIndex(LANGUAGE_ID)));
                result.setCode(cursor.getString(cursor.getColumnIndex(LANGUAGE_CODE)));
            }
            cursor.close();
        }else {
            result.setLanguageId("");
            result.setCode("");
        }
        return result;

    }

    public void delete_language_detail() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DELETE_TABLE_LANGUAGE);
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}