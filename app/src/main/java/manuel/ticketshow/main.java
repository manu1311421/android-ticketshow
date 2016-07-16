package manuel.ticketshow;

import android.Manifest;
import android.app.Activity;
//import android.content.Context;
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
//import android.util.Log;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

//import android.view.WindowManager;
import android.widget.ImageView;
//import android.widget.Toast;

//import java.security.Permission;


public class main extends AppCompatActivity {

//    private static int DEVICE_BRIGHTNESS = 60;
//    private static int APP_BRIGHTNESS = 200;
//    private static int DEVICE_BRIGHTNESS_MODE = 1;
//    private static int APP_BRIGHTNESS_MODE = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;

//    private static final int PERMISSIONS_REQUEST_WRITE_SETTINGS = 0;
//    private static boolean MANAGE_BRIGHTNESS = false;

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;


    private static int IMAGE_REQUEST = 1;


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
//            case PERMISSIONS_REQUEST_WRITE_SETTINGS: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    MANAGE_BRIGHTNESS = true;
//                } else {
//                    MANAGE_BRIGHTNESS = false;
//                }
//                return;
//            }

            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    preferences.edit().putBoolean("READ_EXTERNAL_STORAGE", true).apply();
                } else {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    preferences.edit().putBoolean("READ_EXTERNAL_STORAGE", true).apply();
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


//        if (MANAGE_BRIGHTNESS) {
//        Log.d("x", "Managing Brightness");
//        DEVICE_BRIGHTNESS_MODE = getDeviceBrightnessMode();
//        DEVICE_BRIGHTNESS = getDeviceBrightness();
//        setAppBrightnessMode();
//        setAppBrightness();
//        }

        // Check if an image has already been selected
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String image_path = preferences.getString("IMAGE_PATH", "");
        if (!image_path.equals("")) {
            Log.d("onResume", "image path set");
            showImage(image_path);
        }
        else {
            // Todo: implement user notification to select a ticket
            Log.d("onResume", "image path not set");
        }
    }

//    private void checkWriteSettingsPermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS)
//                != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.WRITE_SETTINGS)) {
//
//            } else {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.WRITE_SETTINGS},
//                        PERMISSIONS_REQUEST_WRITE_SETTINGS);
//            }
//        }
//    }

    private void checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (MANAGE_BRIGHTNESS) {
//        restoreDeviceBrightness();
//        restoreDeviceBrightnessMode();
//        }
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

//    private int getDeviceBrightnessMode() {
//        int brightness_mode = 1;
//        try {
//            brightness_mode = android.provider.Settings.System.getInt(getContentResolver(),
//                    android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE);
//        } catch (Throwable ignored) {
//        }
//        return brightness_mode;
//    }

//    private int getDeviceBrightness() {
//        int brightness = 200;
//        try {
//            brightness = android.provider.Settings.System.getInt(getContentResolver(),
//                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
//        } catch (Throwable ignored) {
//        }
//        return brightness;
//    }

//    private void setAppBrightnessMode() {
//        if (DEVICE_BRIGHTNESS_MODE != APP_BRIGHTNESS_MODE) {
//            Log.d("x", "Came here");
//            setBrightnessMode(APP_BRIGHTNESS_MODE);
//        }
//    }

//    private void setAppBrightness() {
//        setBrightness(APP_BRIGHTNESS);
//    }

//    private void setBrightnessMode(int brightness_mode) {
//        android.provider.Settings.System.putInt(getContentResolver(),
//                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, brightness_mode);
//    }

//    private void setBrightness(int brightness) {
//        android.provider.Settings.System.putInt(getContentResolver(),
//                android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);
//    }

//    private void restoreDeviceBrightnessMode() {
//        if (DEVICE_BRIGHTNESS_MODE != APP_BRIGHTNESS_MODE) {
//            setBrightnessMode(DEVICE_BRIGHTNESS_MODE);
//        }
//    }

//    private void restoreDeviceBrightness() {
//        setBrightness(DEVICE_BRIGHTNESS);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.select_ticket) {
            selectImage();
            return true;
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

        startActivityForResult(chooserIntent, IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

            String image_path = cursor.getString(columnIndex);
            cursor.close();

            // Store the image path
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putString("IMAGE_PATH", image_path).apply();

            showImage(image_path);
        }
    }

    private void showImage(String image_path) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean media_access = preferences.getBoolean("READ_EXTERNAL_STORAGE", false);
        if (media_access) {
            Log.d("showImage", "permissions ok, updating image");
            ImageView imageview = (ImageView) findViewById(R.id.image_view);
            imageview.setImageBitmap(BitmapFactory.decodeFile(image_path));
        } else {
            Log.d("showImage", "request permissions");
            checkReadExternalStoragePermission();
        }
    }

}
