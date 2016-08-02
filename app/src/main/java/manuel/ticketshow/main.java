package manuel.ticketshow;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;


public class main extends AppCompatActivity {
    private static String PREFERENCES_KEY_IMAGE_PATH = "image_path";
    private static String PREFERENCES_KEY_BRIGHTNESS_MANAGEMENT = "brightness_management";
    private static String PREFERENCES_KEY_PERMISSION_READ_EXTERNAL_STORAGE = "permission_external_storage";

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_SELECT_IMAGE = 2;

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Check if we should manage brightness
        boolean brightness_management = preferences.getBoolean(PREFERENCES_KEY_BRIGHTNESS_MANAGEMENT, false);
        if (brightness_management) {
            Log.d("onResume", "Brightness management active");
            startManageBrightness();
        } else {
            Log.d("onResume", "Brightness management not active");
        }

        // Check if an image has already been selected
        String image_path = preferences.getString(PREFERENCES_KEY_IMAGE_PATH, "");
        if (!image_path.equals("")) {
            Log.d("onResume", "Image path set");
            showImage(image_path);
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Please select an image.";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            Log.d("onResume", "Image path not set");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Todo: Explain why we need this permission
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Set checked state of the key according to shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean brightness_management = preferences.getBoolean(PREFERENCES_KEY_BRIGHTNESS_MANAGEMENT, false);
        if (brightness_management) {
            MenuItem menu_item = menu.findItem(R.id.manage_brightness);
            menu_item.setChecked(true);
        } else {
            MenuItem menu_item = menu.findItem(R.id.manage_brightness);
            menu_item.setChecked(false);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    preferences.edit().putBoolean(PREFERENCES_KEY_PERMISSION_READ_EXTERNAL_STORAGE, true).apply();
                } else {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    preferences.edit().putBoolean(PREFERENCES_KEY_PERMISSION_READ_EXTERNAL_STORAGE, false).apply();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.select_ticket: {
                Log.d("onOptionItemSelected", "Registered click on Select Ticket");
                selectImage();
                return true;
            }
            case R.id.manage_brightness: {
                Log.d("onOptionItemSelected", "Registered click on Manage Brightness");
                // Toggle brightness managing
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                boolean manage_brightness = preferences.getBoolean(PREFERENCES_KEY_BRIGHTNESS_MANAGEMENT, false);
                if (!manage_brightness) {
                    Log.d("onOptionItemSelected", "Brightness management turned off: turn on");
                    preferences.edit().putBoolean(PREFERENCES_KEY_BRIGHTNESS_MANAGEMENT, true).apply();
                    startManageBrightness();
                    item.setChecked(true);
                } else {
                    Log.d("onOptionItemSelected", "Brightness management turned on: turn off");
                    endManageBrightness();
                    preferences.edit().putBoolean(PREFERENCES_KEY_BRIGHTNESS_MANAGEMENT, false).apply();
                    item.setChecked(false);
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, REQUEST_SELECT_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_SELECT_IMAGE: {
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        Log.d("onActivityResult", "No data returned in Select Image action");
                        return;
                    }
                    Uri selected_image = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selected_image, filePathColumn,
                            null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                    }
                    int column_index = cursor.getColumnIndex(filePathColumn[0]);
                    String image_path = cursor.getString(column_index);
                    cursor.close();

                    // Store the image path
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    preferences.edit().putString(PREFERENCES_KEY_IMAGE_PATH, image_path).apply();

                    showImage(image_path);

                }
            }
        }
    }

    private void showImage(String image_path) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean permission_read_external_storage = preferences.getBoolean(PREFERENCES_KEY_PERMISSION_READ_EXTERNAL_STORAGE, false);
        if (permission_read_external_storage) {
            Log.d("showImage", "Permissions READ_EXTERNAL_STORAGE ok: show image");
            ImageView imageview = (ImageView) findViewById(R.id.image_view);
            imageview.setImageBitmap(BitmapFactory.decodeFile(image_path));
        } else {
            Log.d("showImage", "Permission READ_EXTERNAL_STORAGE request");
            checkReadExternalStoragePermission();
        }
    }

    private void setBrightness(float brightness) {
        WindowManager.LayoutParams layout_params = getWindow().getAttributes();
        layout_params.screenBrightness = brightness;
        getWindow().setAttributes(layout_params);
    }

    private void startManageBrightness() {
        float app_brightness = 1.0f;
        setBrightness(app_brightness);
    }

    private void endManageBrightness() {
        float device_brightness = -1.0f;
        setBrightness(device_brightness);
    }
}
