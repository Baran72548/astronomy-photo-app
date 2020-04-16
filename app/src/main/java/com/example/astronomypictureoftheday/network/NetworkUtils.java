package com.example.astronomypictureoftheday.network;

import android.content.Context;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.astronomypictureoftheday.R;

import java.net.MalformedURLException;
import java.net.URL;

public class NetworkUtils {
    private static final String BASE_URL = "https://api.nasa.gov/";
    private static final String PLANETARY_PATH = "planetary";
    private static final String APOD_PATH ="apod";
    private static final String API_KEY = "api_key";

    private RequestQueue mRequestQueue;

    private static NetworkUtils sInstance;
    private static final Object LOCK = new Object();
    private static Context mContext;

    private NetworkUtils(Context context) {
        mContext = context.getApplicationContext();
        mRequestQueue = getRequestQueue();
    }

    public static NetworkUtils getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) sInstance = new NetworkUtils(context);
            }
        }
        return sInstance;
    }

    /**
     * Initialization of a new RequestQueue.
     */
    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }

    /**
     * Add the request to RequestQueue.
     */
    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

    /**
     * Cancel all requests.
     */
    public void cancelRequests(String tag) {
        getRequestQueue().cancelAll(tag);
    }

    /**
     * Get a URL of today's data as JsonObject.
     */
    public URL getAstronomyUrl(Context context) {
        Uri.Builder uriBuilder = Uri.parse(BASE_URL).buildUpon();
        Uri uri = uriBuilder
                .appendPath(PLANETARY_PATH)
                .appendPath(APOD_PATH)
                .appendQueryParameter(API_KEY, context.getString(R.string.api_key)).build();
        try {
            URL url = new URL(uri.toString());
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a URL of picked date' data as JsonObject.
     */
    public URL getPickedDateUrl(Context context, String day) {
        Uri.Builder uriBuilder = Uri.parse(BASE_URL).buildUpon();
        Uri uri = uriBuilder
                .appendPath(PLANETARY_PATH)
                .appendPath(APOD_PATH)
                .appendQueryParameter("date", day)
                .appendQueryParameter(API_KEY, context.getString(R.string.api_key)).build();

        try {
            URL url = new URL(uri.toString());
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
