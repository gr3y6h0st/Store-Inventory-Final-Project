package com.nanodegree.android.storeinventoryproject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nanodegree.android.storeinventoryproject.Data.StoreContract.ProductEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

/**
 * Created by jdifuntorum on 8/24/17.
 */

public class InputStoreItem extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentProductUri;
    private static final int PRODUCT_LOADER = 0;
    private boolean mProductDataHasChanged = false;

    private static final String LOG_TAG = "Error";

    private Uri mImageUri;

    private ImageView mImageView;

    // The request code to store image from the Gallery
    private static final int IMAGE_REQUEST_CODE = 0;


    /** EditText field to enter the product productName */
    private EditText mNameEditText;

    /** EditText field to enter the product's desc */
    private EditText mDescriptionEditText;

    /** EditText field to enter the product's price */
    private EditText mPriceEditText;

    private EditText mEmailEditText;

    private EditText mOrderEditText;

    //Stock Buttons
    private EditText mStockEditText;
    private TextView mStockCurrent;
    private Button mSubtractStock;
    private Button mAddStockButton;

    private Button mOrderButton;

    /** EditText field to enter the product's productSupplier */
    private Spinner mSupplierSpinner;

    /**
     * Gender of the product. The possible values are:
     * 0 for unknown productSupplier, 1 for male, 2 for female.
     */
    private int mSupplier = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_item);

        //USE getIntent() and getData() to get the associated URI
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent does not contain a PRODUCT content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null ) {
            setTitle(getString(R.string.input_item_activity_add_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();

        } else {
            setTitle((getString(R.string.input_item_activity_edit_product)));

            getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }

        // Set title of EditorActivity on which situation we have
        // If the EditorActivity was opened using the ListView item, then we will
        // have uri of pet so change app bar to say "Edit Product"
        // Otherwise if this is a new product, uri is null so change app bar to say "Add a Product"

        // Find all relevant views that we will need to read product input from
        mImageView = (ImageView) findViewById(R.id.input_image);
        mNameEditText = (EditText) findViewById(R.id.input_product_name);
        mDescriptionEditText = (EditText) findViewById(R.id.input_product_description);
        mPriceEditText = (EditText) findViewById(R.id.input_product_price);
        mSupplierSpinner = (Spinner) findViewById(R.id.spinner_supplier);
        mStockCurrent = (TextView) findViewById(R.id.input_current_stock);
        mStockEditText = (EditText) findViewById(R.id.input_stock_change);
        mSubtractStock = (Button) findViewById(R.id.input_stock_minus_button);
        mAddStockButton = (Button) findViewById(R.id.input_stock_plus_button);
        mEmailEditText = (EditText) findViewById(R.id.input_email_address);
        mOrderButton = (Button) findViewById(R.id.input_order_button);
        mOrderEditText = (EditText) findViewById(R.id.input_order_qty);

        mImageView.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierSpinner.setOnTouchListener(mTouchListener);
        mStockCurrent.setOnTouchListener(mTouchListener);
        mStockEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
        mOrderEditText.setOnTouchListener(mTouchListener);
        mOrderButton.setOnTouchListener(mTouchListener);

        // Set a clickListener on minus button
        // Set a clickListener on minus button
        mSubtractStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentProduct = mNameEditText.getText().toString();
                String toastMessage = null;
                int current_stock_amount = parseInt(mStockCurrent.getText().toString());
                int stock_change;
                int new_stock_amount;

                if (mStockEditText.getText().toString().trim().length() == 0) {
                    new_stock_amount = current_stock_amount - 1;
                } else {
                    stock_change = parseInt(mStockEditText.getText().toString());
                    new_stock_amount = current_stock_amount - stock_change;
                }

                if (new_stock_amount == 0) {
                    toastMessage = "Place additional order. Out of stock of " + currentProduct + ".";
                    mStockCurrent.setText(String.valueOf(new_stock_amount));
                } else if (new_stock_amount > 0) {
                    mStockCurrent.setText(String.valueOf(new_stock_amount));
                } else {
                    toastMessage = "Negative Stock isn't possible for " + currentProduct + ".";
                }

                Toast.makeText(view.getContext(), toastMessage, Toast.LENGTH_SHORT).show();
                mStockEditText.setText((""));
            }
        });

            // Set a clickListener on plus button
        mAddStockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String currentProduct = mNameEditText.getText().toString();
                    int current_stock_amount = parseInt(mStockCurrent.getText().toString());
                    int stock_change;
                    int new_stock_amount;

                    if (mStockEditText.getText().toString().trim().length() == 0) {
                        new_stock_amount = current_stock_amount + 1;
                    } else {
                        stock_change = parseInt(mStockEditText.getText().toString());
                        new_stock_amount = current_stock_amount + stock_change;
                    }

                    mStockCurrent.setText(String.valueOf(new_stock_amount));
                    String toastMessage = "Stock increased for " + currentProduct + " now at " + new_stock_amount;

                    Toast.makeText(view.getContext(), toastMessage, Toast.LENGTH_SHORT).show();
                    mStockEditText.setText((""));
                }
            });

        // Set a clickListener on place order button to open up email intent
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String orderAmount = mOrderEditText.getText().toString().trim();
                mOrderEditText.setText("");
                if (orderAmount.length() != 0) {
                    String productName = mNameEditText.getText().toString().trim();

                    String emailAddress = "mailto:" + mEmailEditText.getText().toString().trim();
                    String subjectHeader = "Order For: " + productName;
                    String orderEmailBody = "To whom this may concern, " + "\n\n" + "Our store would like to order " + orderAmount + " units of " + productName + ". " + " \n\n" + "Regards, ";

                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse(emailAddress));
                    intent.putExtra(Intent.EXTRA_SUBJECT, subjectHeader);
                    intent.putExtra(Intent.EXTRA_TEXT, orderEmailBody);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }

                } else {
                    String toastMessage = "Please input an order quantity first.";
                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                }

            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSelector();
            }
        });

        setupSpinner();
    }

    public void imageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mImageUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mImageUri.toString());

                //mImageView.setText(mUri.toString());
                mImageView.setImageBitmap(getBitmapFromUri(mImageUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    /**
     * Setup the dropdown spinner that allows the product to select the productSupplier of the product.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter productSupplierSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        productSupplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSupplierSpinner.setAdapter(productSupplierSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_general_mills))) {
                        mSupplier = ProductEntry.SUPPLIER_GENERAL_MILLS; // general mills productSupplier
                    } else if (selection.equals(getString(R.string.supplier_core_mark))) {
                        mSupplier = ProductEntry.SUPPLIER_CORE_MARK; // cork-mark productSupplier
                    } else {
                        mSupplier = ProductEntry.SUPPLIER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplier = 0; // Unknown
            }
        });
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductDataHasChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Product clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
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
    public void onBackPressed() {
        // If the product data hasn't changed, continue with handling back button press
        if (!mProductDataHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the product.
        // Create a click listener to handle the product confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Product clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Product clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Product clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.input_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.input_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
            // Close the activity
            finish();
        }
    }

    private boolean saveNewProduct(){
        String productNameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String stockString = mStockCurrent.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();

        //Create ContentValues object where column productNames are KEYS,
        //and product's data from inputActivity are values.
        if (mCurrentProductUri == null && TextUtils.isEmpty(productNameString) &&
                TextUtils.isEmpty(productNameString) &&
                TextUtils.isEmpty(descriptionString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(emailString) &&
                TextUtils.isEmpty(stockString) &&
                mSupplier == ProductEntry.SUPPLIER_UNKNOWN) {return true;}

        else if (TextUtils.isEmpty(productNameString)) {
            Toast.makeText(this, R.string.empty_product_name_msg,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(descriptionString)) {
            Toast.makeText(this, R.string.empty_product_desc_msg,
                    Toast.LENGTH_SHORT).show();
            return false;

        } else if (TextUtils.isEmpty(priceString) || parseDouble(priceString) <= 0) {
            Toast.makeText(this, R.string.empty_product_price_msg,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(stockString) || parseInt(stockString) <= 0) {
            Toast.makeText(this, R.string.empty_product_stock_msg,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (mSupplier == ProductEntry.SUPPLIER_UNKNOWN) {
            Toast.makeText(this, R.string.empty_product_supplier_msg,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(emailString)) {
            Toast.makeText(this, R.string.empty_product_email_msg,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (mImageUri == null) {
            Toast.makeText(this, R.string.empty_product_image_msg,
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(ProductEntry.COLUMN_PRODUCT_DESCRIPTION, descriptionString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, mSupplier);
        values.put(ProductEntry.COLUMN_PRODUCT_EMAIL, emailString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductEntry.COLUMN_PRODUCT_STOCK, stockString);

        if (mImageUri == null) {
            mImageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.placeholder_image)
                    + '/' + getResources().getResourceTypeName(R.drawable.placeholder_image) + '/' + getResources().getResourceEntryName(R.drawable.placeholder_image));
        }
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, mImageUri.toString());

        if (mCurrentProductUri == null){

            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // show toast msg depnding on whether or not the insertion was successful
            if (newUri == null) {
                // If new content URI is null then there was an error with insertion.
                Toast.makeText(this, getString(R.string.toast_error_msg), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                Toast.makeText(this, getString(R.string.toast_save_msg), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.

            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.toast_error_msg),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.toast_save_msg),
                        Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_input_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Product clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //Save data to db.
                saveNewProduct();
                //EXIT activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link InventoryActivity}.
                if (!mProductDataHasChanged) {
                    NavUtils.navigateUpFromSameTask(InputStoreItem.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the product.
                // Create a click listener to handle the product confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Product clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(InputStoreItem.this);
                            }
                        };

                // Show a dialog that notifies the product they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_DESCRIPTION,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_EMAIL,
                ProductEntry.COLUMN_PRODUCT_STOCK,
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //validation check on data
        //return if the data is less than 1 product (row).
        if (data == null || data.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int productImageColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int productNameColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int descriptionColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_DESCRIPTION);
            int priceColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int productSupplierColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            int emailColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_EMAIL);
            int stockColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_STOCK);

            // Extract out the values from the Cursor object for the given column index
            String productImage = data.getString(productImageColumnIndex);
            String productName = data.getString(productNameColumnIndex);
            String description = data.getString(descriptionColumnIndex);
            double price = data.getDouble(priceColumnIndex);
            int productSupplier = data.getInt(productSupplierColumnIndex);
            String email = data.getString(emailColumnIndex);
            int stock = data.getInt(stockColumnIndex);

            // Update the views on the screen with the values from the database
            mImageUri = Uri.parse(productImage);
            mImageView.setImageURI(mImageUri);
            mNameEditText.setText(productName);
            mDescriptionEditText.setText(description);
            mPriceEditText.setText(Double.toString(price));
            mEmailEditText.setText(email);
            mStockEditText.setText(Integer.toString(stock));

            switch (productSupplier) {
                case ProductEntry.SUPPLIER_GENERAL_MILLS:
                    mSupplierSpinner.setSelection(1);
                    break;
                case ProductEntry.SUPPLIER_CORE_MARK:
                    mSupplierSpinner.setSelection(2);
                    break;
                default:
                    mSupplierSpinner.setSelection(0);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mImageView.setImageResource(R.drawable.placeholder_image);
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mPriceEditText.setText(Integer.toString(0));
        mEmailEditText.setText("");
        mStockEditText.setText(Integer.toString(0));
        mSupplierSpinner.setSelection(0); // Select "Unknown" productSupplier
    }

}
