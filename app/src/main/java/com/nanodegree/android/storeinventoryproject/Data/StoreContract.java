package com.nanodegree.android.storeinventoryproject.Data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jdifuntorum on 8/24/17.
 */

public final class StoreContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private StoreContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.nanodegree.android.storeinventoryproject";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PRODUCTS = "Products";


    public static abstract class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);


        public static final String TABLE_NAME = "ProductData";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_IMAGE = "Image";
        public static final String COLUMN_PRODUCT_NAME = "Name";
        public static final String COLUMN_PRODUCT_DESCRIPTION = "Description";
        public static final String COLUMN_PRODUCT_PRICE = "Price";
        public static final String COLUMN_PRODUCT_EMAIL= "Email";
        public static final String COLUMN_PRODUCT_SUPPLIER = "Supplier";
        public static final String COLUMN_PRODUCT_STOCK = "Stock";

        public static final int SUPPLIER_UNKNOWN = 0;
        public static final int SUPPLIER_GENERAL_MILLS = 1;
        public static final int SUPPLIER_CORE_MARK = 2;
    }
}
