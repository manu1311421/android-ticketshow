package manuel.ticketshow;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
//import android.widget.Toast;


public class main extends AppCompatActivity {

    private static String PREFERENCES_KEY_IMAGE_PATH = "image_path";
    private static String PREFERENCES_KEY_BRIGHTNESS_MANAGEMENT = "brightness_management";
    private static String PREFERENCES_KEY_PERMISSION_WRITE_SETTINGS = "permission_write_settings";
    private static String PREFERENCES_KEY_PERMISSION_READ_EXTERNAL_STORAGE = "permission_external_storage";

    private static int DEVICE_BRIGHTNESS = 60;
    private static int APP_BRIGHTNESS = 200;
    private static int DEVICE_BRIGHTNESS_MODE = 1;
    private static int APP_BRIGHTNESS_MODE = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;

//    private static final int PERMISSIONS_REQUEST_WRITE_SETTINGS = 0;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static int REQUEST_CODE_SELECT_IMAGE = 2;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
//            case PERMISSIONS_REQUEST_WRITE_SETTINGS: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//                    preferences.edit().putBoolean(PREFERENCES_KEY_BRIGHTNESS_MANAGEMENT, true).apply();
//                    preferences.edit().putBoolean(PREFERENCES_KEY_PERMISSION_WRITE_SETTINGS, true).apply();
//
//                } else {
//                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//                    preferences.edit().putBoolean(PREFERENCES_KEY_BRIGHTNESS_MANAGEMENT, false).apply();
//                    preferences.edit().putBoolean(PREFERENCES_KEY_PERMISSION_WRITE_SETTINGS, false).apply();
//                }
//                return;
//            }

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
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Check if we should manage brightness
        boolean brightness_management = preferences.getBoolean(PREFERENCES_KEY_BRIGHTNESS_MANAGEMENT, false);
        boolean permissions_write_settings = preferences.getBoolean(PREFERENCES_KEY_PERMISSION_WRITE_SETTINGS, false);
        if (permissions_write_settings) {
            Log.d("onResume", "Permission WRITE_SETTINGS ok");
            if (brightness_management) {
                Log.d("onResume", "Brightness management active");
                startManageBrightness();
            } else {
                Log.d("onResume", "Brightness management not active");
            }
        } else {
            Log.d("onResume", "Permission WRITE_SETTINGS not granted");
        }

        // Check if an image has already been selected
        String image_path = preferences.getString(PREFERENCES_KEY_IMAGE_PATH, "");
        if (!image_path.equals("")) {
            Log.d("onResume", "Image path set");
            showImage(image_path);
        } else {
            // Todo: implement user notification to select a ticket
            Log.d("onResume", "Image path not set");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Check if we should manage brightness
        boolean manage_brightness = preferences.getBoolean(PREFERENCES_KEY_BRIGHTNESS_MANAGEMENT, false);
        if (manage_brightness) {
            endManageBrightness();
        }
    }

    private void checkWriteSettingsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this)) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                preferences.edit().putBoolean(PREFERENCES_KEY_PERMISSION_WRITE_SETTINGS, true).apply();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                        .setData(Uri.parse("package:" + this.getPackageName()))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
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
        return true;
    }

    private int getDeviceBrightnessMode() {
        int brightness_mode = 1;  // Fallback value
        try {
            brightness_mode = android.provider.Settings.System.getInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Throwable ignored) {
        }
        return brightness_mode;
    }

    private int getDeviceBrightness() {
        int brightness = 200;  // Fallback value
        try {
            brightness = android.provider.Settings.System.getInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (Throwable ignored) {
        }
        return brightness;
    }

    private void setBrightnessMode(int brightness_mode) {
        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, brightness_mode);
    }

    private void setBrightness(int brightness) {
        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    private void startManageBrightness() {
        DEVICE_BRIGHTNESS_MODE = getDeviceBrightnessMode();
        DEVICE_BRIGHTNESS = getDeviceBrightness();
        if (DEVICE_BRIGHTNESS_MODE != APP_BRIGHTNESS_MODE) {
            setBrightnessMode(APP_BRIGHTNESS_MODE);
        }
        setBrightness(APP_BRIGHTNESS);
    }

    private void endManageBrightness() {
        setBrightness(DEVICE_BRIGHTNESS);
        if (DEVICE_BRIGHTNESS_MODE != APP_BRIGHTNESS_MODE) {
            setBrightnessMode(DEVICE_BRIGHTNESS_MODE);
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
                    boolean permission_write_settings = preferences.getBoolean(PREFERENCES_KEY_PERMISSION_WRITE_SETTINGS, false);
                    if (!permission_write_settings) {
                        Log.d("onOptionItemSelected", "Permission WRITE_SETTINGS not granted, ask for permission");
                        checkWriteSettingsPermission();
                    }
                    if (permission_write_settings) {
                        preferences.edit().putBoolean(PREFERENCES_KEY_BRIGHTNESS_MANAGEMENT, true).apply();
                        startManageBrightness();
                    }
                } else {
                    Log.d("onOptionItemSelected", "Brightness management turned on: turn off");
                    endManageBrightness();
                    preferences.edit().putBoolean(PREFERENCES_KEY_BRIGHTNESS_MANAGEMENT, false).apply();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
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

}
