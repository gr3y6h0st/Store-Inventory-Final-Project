package com.nanodegree.android.storeinventoryproject;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.*;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.COLUMN_PRODUCT_IMAGE;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.COLUMN_PRODUCT_NAME;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.COLUMN_PRODUCT_STOCK;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry.CONTENT_URI;
import static com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry._ID;

/**
 * Created by jdifuntorum on 8/24/17.
 */

public class InventoryCursorAdapter extends CursorAdapter {
    /**
            * Constructs a new {@link InventoryCursorAdapter}.
            *
            * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.list_item_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.list_item_price);
        TextView stockTextView = (TextView) view.findViewById(R.id.list_item_stock);
        TextView supplierTextView = (TextView) view.findViewById(R.id.list_item_supplier);
        ImageView productImageView = (ImageView) view.findViewById(R.id.list_item_image);
        Button saleButton = (Button) view.findViewById(R.id.mark_sale_button);

        // Find the columns of product attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(_ID);
        int nameColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_PRICE);
        int stockColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_STOCK);
        int supplierColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_SUPPLIER);
        int imageColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_IMAGE);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        final int productStock = cursor.getInt(stockColumnIndex);
        String productImage = cursor.getString(imageColumnIndex);


        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        priceTextView.setText(productPrice);
        stockTextView.setText(String.valueOf(productStock));
        int supplierID = cursor.getInt(supplierColumnIndex);
        productImageView.setImageURI(Uri.parse(productImage));
        final int productId = cursor.getInt(idColumnIndex);

        switch (supplierID) {
            case ProductEntry.SUPPLIER_GENERAL_MILLS:
                supplierTextView.setText(R.string.supplier_general_mills);
                break;
            case ProductEntry.SUPPLIER_CORE_MARK:
                supplierTextView.setText(R.string.supplier_core_mark);
                break;
        }

        // Set a clickListener on sale button
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri currentProductUri = ContentUris.withAppendedId(CONTENT_URI, productId);
                reduceProductQuantity(view, productStock, currentProductUri);
            }
        });
    }

    private void reduceProductQuantity(View view, int productStock, Uri uri) {

        if (productStock > 0) {
            productStock--;

            ContentValues values = new ContentValues();
            values.put(COLUMN_PRODUCT_STOCK, productStock);
            mContext.getContentResolver().update(uri, values, null, null);
        } else {
            Toast.makeText(view.getContext(), "This product has no stock", Toast.LENGTH_SHORT).show();
        }
    }
}


