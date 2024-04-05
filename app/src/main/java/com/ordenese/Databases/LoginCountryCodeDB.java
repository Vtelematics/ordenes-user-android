package com.ordenese.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ordenese.DataSets.LoginCountryCodeDataSet;


public class LoginCountryCodeDB extends SQLiteOpenHelper {

    final static String name = "login_country_code_db";
    final static int version = 1;
    final static String TABLE_NAME_LOGIN = "login_country_code_table";

    final static String COUNTRY_ID = "country_id";
    final static String PHONE_CODE = "phone_code";

    final static String DROP_TABLE_LANGUAGE = "DROP TABLE IF EXISTS " + TABLE_NAME_LOGIN;
    final static String DELETE_TABLE_LANGUAGE = "DELETE FROM " + TABLE_NAME_LOGIN;
    final static String SELECT_VALUE_SELECT = "select ";
    final static String SELECT_VALUE_FROM = "from ";
    Cursor cursor;

    final static String CREATE_ACCOUNT_NEW = "create table " + TABLE_NAME_LOGIN + "(" + COUNTRY_ID + " integer primary key,"+ PHONE_CODE +" text);";

    private static LoginCountryCodeDB sInstance;

    public static synchronized LoginCountryCodeDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LoginCountryCodeDB(context.getApplicationContext());
        }
        return sInstance;
    }

    private LoginCountryCodeDB(Context context) {
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

    public Boolean insert(LoginCountryCodeDataSet loginNCDs) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COUNTRY_ID, loginNCDs.getCountryId());
        contentValues.put(PHONE_CODE, loginNCDs.getPhoneCode());
        db.insert(TABLE_NAME_LOGIN, null, contentValues);
        return true;
    }


    public boolean check_selected() {
        String query = SELECT_VALUE_SELECT + COUNTRY_ID + " " + SELECT_VALUE_FROM + TABLE_NAME_LOGIN;
        String result = null;
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.rawQuery(query, null);
        if (cursor != null) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                result = cursor.getString(cursor.getColumnIndex(COUNTRY_ID));
            }
            cursor.close();
        }
        return (result != null);
    }



    public LoginCountryCodeDataSet get_Details() {
        String select = SELECT_VALUE_SELECT +"* " + SELECT_VALUE_FROM + TABLE_NAME_LOGIN;
        LoginCountryCodeDataSet result = new LoginCountryCodeDataSet();
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.rawQuery(select, null);
        if (cursor != null) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                result.setCountryId(cursor.getString(cursor.getColumnIndex(COUNTRY_ID)));
                result.setPhoneCode(cursor.getString(cursor.getColumnIndex(PHONE_CODE)));
            }
            cursor.close();
        }else {
            result.setCountryId("");
            result.setPhoneCode("");
        }
        return result;
    }

    public void delete_detail() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DELETE_TABLE_LANGUAGE);
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}
