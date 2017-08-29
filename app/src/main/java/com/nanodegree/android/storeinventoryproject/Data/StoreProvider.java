package com.nanodegree.android.storeinventoryproject.Data;

/**
 * Created by jdifuntorum on 8/24/17.
 */

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.CONTENT_AUTHORITY;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.PATH_PRODUCTS;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.COLUMN_PRODUCT_DESCRIPTION;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.COLUMN_PRODUCT_EMAIL;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.COLUMN_PRODUCT_IMAGE;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.COLUMN_PRODUCT_NAME;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.COLUMN_PRODUCT_STOCK;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.TABLE_NAME;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry._ID;

/**
 * {@link ContentProvider} for Store Inventory app.
 */
public class StoreProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = StoreProvider.class.getSimpleName();

    /** Database helper object **/
    private StoreDBHelper mDbHelper;

    /** URI matcher object to match a context URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /** URI matcher code for the content URI for the storeinventorys table */
    public static final int PRODUCTS = 100;

    /** URI matcher code for the content URI for a single storeinventory in the storeinventorys table */
    public static final int PRODUCT_ID = 101;

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.storeinventorys/storeinventorys" will map to the
        // integer code {@link #PRODUCT}. This URI is used to provide access to MULTIPLE rows
        // of the storeinventorys table.
        sUriMatcher.addURI(CONTENT_AUTHORITY, StoreContract.PATH_PRODUCTS, PRODUCTS);

        // The content URI of the form "content://com.example.android.storeinventoryprojecy/storeinventory/#" will map to the
        // integer code {@link #PRODUCT_ID}. This URI is used to provide access to ONE single row
        // of the storeinventorys table.

        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.storeinventorys/storeinventorys/3" matches, but
        // "content://com.example.android.storeinventorys/storeinventorys" (without a number at the end) doesn't match.
        sUriMatcher.addURI(CONTENT_AUTHORITY, StoreContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    // MIME type of {@link #COTENT_URI} for list of storeinventories.
    public static final String CONTENT_LIST_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
    //Mime type of {@link #CONTENT_URI} for single storeinventory.
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY +"/" + PATH_PRODUCTS;


    @Override
    public boolean onCreate() {
        mDbHelper = new StoreDBHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:

                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.storeinventorys/storeinventorys/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = StoreContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the storeinventorys table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        //set notification URI on Cursor
        // so we know what content URI the Cursor was created for.
        //if the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        //check if there's a match
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        //Check values are not null
        String image = values.getAsString(COLUMN_PRODUCT_IMAGE);
        String name = values.getAsString(COLUMN_PRODUCT_NAME);
        String description = values.getAsString(COLUMN_PRODUCT_DESCRIPTION);
        String price = values.getAsString(COLUMN_PRODUCT_PRICE);
        String email = values.getAsString(COLUMN_PRODUCT_EMAIL);
        String stock = values.getAsString(COLUMN_PRODUCT_STOCK);
        int supplier = values.getAsInteger(COLUMN_PRODUCT_SUPPLIER);

        if (name == null || price == null || image == null || stock == null || email == null || description == null || supplier == 0) {
            throw new IllegalArgumentException("Error: One or more values for user data missing.");
        }
        //get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert new user with given values
        long id = database.insert(TABLE_NAME, null, values);

        //If ID = -1, then insert failed. Log error and return null.
        if(id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //notify all listeres that the data has changed for URI
        getContext().getContentResolver().notifyChange(uri, null);

        //Once we know ID of new row in the table, return the new URI
        return ContentUris.withAppendedId(uri, id);
    }
    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                //For PRODUCT_ID code, extract out the ID from URI,
                //so we know which row to update.Selction will be "_id=?" and selection
                //arguments will be a String array containing the actual ID.
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    // update store inventory data with given content values. Apply changes to rows
    // specified in the selection and selection arguments ( which could be 0 or 1 or more store inventories).
    // Return the number of rows that were successfully updated.
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        if (values.size() == 0) {
            return 0;
        }
        //validate product name
        if (values.containsKey(COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("ERROR: Insert Product NAME/LABEL.");
            }
        }
        //validate product values
        if (values.containsKey(COLUMN_PRODUCT_DESCRIPTION)) {
            String description = values.getAsString(COLUMN_PRODUCT_DESCRIPTION);
            if (description == null) {
                throw new IllegalArgumentException("Error: Insert Product Description.");
            }
        }
        // If the {@link COLUMN_PRODUCT_PRICE} key is present,
        // check that the name value is not null.
        if (values.containsKey(COLUMN_PRODUCT_PRICE)) {
            Double price = values.getAsDouble(COLUMN_PRODUCT_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Product requires a description");
            }
        }

        if (values.containsKey(COLUMN_PRODUCT_SUPPLIER)) {
            Integer supplier = values.getAsInteger(COLUMN_PRODUCT_SUPPLIER);
            if (supplier == 0) {
                throw new IllegalArgumentException("Error: Select a Product Supplier.");
            }
        }

        //get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        //perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(StoreContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        //get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Track number of rows deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(TABLE_NAME, selection, selectionArgs);
                break;

            case PRODUCT_ID:
                //delete a single row given by ID in URI
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                return CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}