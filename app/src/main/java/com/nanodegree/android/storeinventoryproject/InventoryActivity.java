package com.nanodegree.android.storeinventoryproject;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Setup FAB to open InputStoreItem
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, InputStoreItem.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the store inventory products
        ListView storeItemsListView = (ListView) findViewById(R.id.listView);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.emptyView);
        storeItemsListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of product data in the Cursor.
        // There is no product data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        storeItemsListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        storeItemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create new intent to go to {@link InputStoreItem}
                Intent intent = new Intent(InventoryActivity.this, InputStoreItem.class);

                // Form the content URI that represents the specific product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ProductEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.products/products/2"
                // if the product with ID 2 was clicked on.
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                // Launch the {@link InputStoreItem} to display the data for the current product.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    private void insertTestData() {
        insertProduct("Trolli Gummy Eggs", "Best Snack in the World", 2.99, 1, 1, "jayvy19@gmail.com", R.drawable.trolli_eggs);
        insertProduct("Arizona Green Tea", "Best Cheap Drink.... back in Highschool", 2.49, 2, 1, "jayvy19@gmail.com", R.drawable.arizona);
        insertProduct("Lipton Iced Tea", "Mediocre Tea", 1.99, 3, 1, "jayvy19@gmail.com", R.drawable.lipton);
        insertProduct("Bag of Ice", "5lb bag of ice", 0.99, 4, 2, "jayvy19@gmail.com", R.drawable.bagice);

    }

    /**
     * Helper method to insert hardcoded product data into the database. For debugging purposes only.
     */
    private void insertProduct(String name, String description, double price, int stock, int supplier, String email, int imageId) {
        // Create a ContentValues object where column names are the keys,
        // and Toto's product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, name);
        values.put(ProductEntry.COLUMN_PRODUCT_DESCRIPTION, description);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_STOCK, stock);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplier);
        values.put(ProductEntry.COLUMN_PRODUCT_EMAIL, email);

        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(imageId)
                + '/' + getResources().getResourceTypeName(imageId) + '/' + getResources().getResourceEntryName(imageId));

        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageUri.toString());


        // Insert a new row into the provider using the ContentResolver.
        // Use the {@link ProductEntry#CONTENT_URI} to indicate that we want to insert
        // into the products database table.
        // Receive the new content URI that will allow us to access this data in the future.
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful.
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.mainActivity_insert_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.mainActivity_insert_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Prompt the user to confirm that they want to delete everything
     */
    private void deleteAll() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, to delete the product.
                int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
                Log.v("InventoryActivity", rowsDeleted + " rows deleted from product database");
            }
        });
        builder.setNegativeButton(R.string.cancel_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertTestData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_DESCRIPTION,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_EMAIL,
                ProductEntry.COLUMN_PRODUCT_STOCK
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ProductEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link InventoryCursorAdapter} with this new cursor containing updated data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
