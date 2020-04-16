package com.example.astronomypictureoftheday.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.astronomypictureoftheday.Constants;
import com.example.astronomypictureoftheday.ui.fragments.CalendarDialogFragment;
import com.example.astronomypictureoftheday.utils.DataParser;
import com.example.astronomypictureoftheday.R;
import com.example.astronomypictureoftheday.network.NetworkUtils;
import com.example.astronomypictureoftheday.data.AstronomyData;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 111;

    private NetworkUtils mNetworkUtils;
    private AstronomyData mAstronomyData;
    private String mAstronomyUrl;

    private Date mDay;

    private Menu mMenu;
    private TouchImageView mTouchImageView;
    private WebView mWebView;
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;
    private ProgressBar mProgressBar;
    private LinearLayout mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTouchImageView = findViewById(R.id.astronomy_image_view);
        mWebView = findViewById(R.id.astronomy_web_view);
        mTitleTextView = findViewById(R.id.astronomy_title_text_view);
        mDescriptionTextView = findViewById(R.id.astronomy_description_text_view);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.bottom_sheet_layout);

        mNetworkUtils = NetworkUtils.getInstance(this);

        if (savedInstanceState == null) getTodayInfo();
    }

    /**
     * Saving current state of activity for rotation case.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.BUNDLE_CURRENT_Date, mAstronomyUrl);
    }

    /**
     * Restore the saved state of activity after rotation.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAstronomyUrl = savedInstanceState.getString(Constants.BUNDLE_CURRENT_Date);
        requestInfo(mAstronomyUrl);
    }

    /**
     * Create menu items.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.main_menu, mMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.download_item:
                downloadImage();
                break;
            case R.id.share_item:
                checkPermission();
                break;
            case R.id.about_item:
                showAboutDialog();
                break;
            case R.id.date_picker_item:
                showDatePickerDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Asking for a permission to save data in external storage.
     */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.permission_explanation)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_WRITE_EXTERNAL_STORAGE);
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create();

                alertDialog.show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            if (mAstronomyData.getMediaType().equals("image")) {
                shareImage();
            } else {
                shareVideo();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mAstronomyData.getMediaType().equals("image")) {
                    shareImage();
                } else {
                    shareVideo();
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.permission_explanation, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Get the Uri of image and download it in device by using DownloadManager.
     */
    private void downloadImage() {
        Uri imageUri = getImageUri();

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(imageUri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(getResources().getString(R.string.download_data));
        request.allowScanningByMediaScanner();
        // Notify user after completing download.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "DOWNLOADS");
        request.setMimeType("image/*");

        downloadManager.enqueue(request);
    }

    /**
     * Share video and its title as text message.
     */
    private void shareVideo() {
        String videoUrl = mAstronomyData.getTitle() + "\n\n" + mAstronomyData.getUrl();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, videoUrl);
        intent.setType("text/plain");
        startActivity(intent);
    }

    /**
     * Share image and its title.
     */
    private void shareImage() {
        Uri imageUri = getImageUri();
        String imageTitle = mAstronomyData.getTitle();

        if (imageUri != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, imageTitle);
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.setType("image/*");
            startActivity(Intent.createChooser(intent, "Share Image"));

        } else {
            Toast.makeText(MainActivity.this, R.string.uri_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get image URI to pass it through the intent.
     */
    public Uri getImageUri(){
        Uri imageUri  = Uri.parse(mAstronomyData.getHdUrl());
        return imageUri;
    }

    /**
     * Show a dialog with description about the app.
     */
    private void showAboutDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_description)
                .create();
        alertDialog.show();
    }

    /**
     * Show a DatePickerDialog to see the picture of that day.
     */
    private void showDatePickerDialog() {
        CalendarDialogFragment calendarDialogFragment = CalendarDialogFragment.newInstance();
        calendarDialogFragment.show(getSupportFragmentManager(), "tag");
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        mDay = calendar.getTime();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd", Locale.ENGLISH);
        String pickedDate = simpleDateFormat.format(mDay.getTime());

        getPickedDateInfo(pickedDate);
    }

    /**
     * Build the URL to get data of selected date and pass it to requestInfo() method as parameter.
     * @param day is the picked day by user to view data of that day.
     */
    private void getPickedDateInfo(String day) {
        mAstronomyUrl = mNetworkUtils.getPickedDateUrl(MainActivity.this, day).toString();
        // Make progressBar visible till getting data.
        mProgressBar.setVisibility(View.VISIBLE);
        requestInfo(mAstronomyUrl);
    }

    /**
     * Build the URL to get data of today and pass it to requestInfo() method as parameter.
     */
    private void getTodayInfo() {
        mAstronomyUrl = mNetworkUtils.getAstronomyUrl(MainActivity.this).toString();
        requestInfo(mAstronomyUrl);
    }

    /**
     * Make a JsonRequest to get data.
     */
    private void requestInfo(String astronomyUrl) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                astronomyUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mProgressBar.setVisibility(View.GONE);
                        try {
                            mAstronomyData = DataParser.getAstronomyInfoFromJson(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (mAstronomyData != null) {
                            displayAstronomyData(mAstronomyData);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        jsonObjectRequest.setTag(TAG);
        mNetworkUtils.addToRequestQueue(jsonObjectRequest);
    }

    /**
     * display data of image or video and their title and description.
     * @param astronomyData is holding the response.
     */
    private void displayAstronomyData(AstronomyData astronomyData) {
        if (mAstronomyData.getMediaType().equals("video")) {
            mMenu.findItem(R.id.download_item).setVisible(false);
            mTouchImageView.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            mWebView.loadUrl(mAstronomyData.getUrl());
        } else {
            mMenu.findItem(R.id.download_item).setVisible(true);
            mWebView.setVisibility(View.GONE);
            mTouchImageView.setVisibility(View.VISIBLE);
            Picasso.with(this.getApplicationContext()).load(astronomyData.getHdUrl()).into(mTouchImageView);
        }

        mTitleTextView.setText(astronomyData.getTitle());
        mDescriptionTextView.setText(astronomyData.getDescription());
    }

    /**
     * Check screen orientation to display image or video in full screen in landscape mode.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            showSystemUI();
        }
    }

    /**
     * In landscape mode, hide all other elements and show image/video only.
     */
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);

        mLinearLayout.setVisibility(View.GONE);
    }

    /**
     * In portrait mode, show all elements.
     */
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        mLinearLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mNetworkUtils.cancelRequests(TAG);
    }
}
