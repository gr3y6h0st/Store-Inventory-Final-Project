package com.nanodegree.android.storeinventoryproject.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry;

/**
 * Created by jdifuntorum on 8/24/17.
 */

public class StoreDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "StoreInventory.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ", ";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ProductEntry.TABLE_NAME + "("
                    + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ProductEntry.COLUMN_PRODUCT_IMAGE + " TEXT NOT NULL, "
                    + ProductEntry.COLUMN_PRODUCT_NAME + TEXT_TYPE + " TEXT NOT NULL" + COMMA_SEP
                    + ProductEntry.COLUMN_PRODUCT_DESCRIPTION + TEXT_TYPE + " TEXT NOT NULL" + COMMA_SEP
                    + ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                    + ProductEntry.COLUMN_PRODUCT_SUPPLIER + " INTEGER NOT NULL, "
                    + ProductEntry.COLUMN_PRODUCT_EMAIL + TEXT_TYPE + "TEXT NOT NULL" + COMMA_SEP
                    + ProductEntry.COLUMN_PRODUCT_STOCK + " INTEGER NOT NULL);";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;

    public StoreDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
        // this database is only a cache for online data, so its upgrade policy
        // is simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}

