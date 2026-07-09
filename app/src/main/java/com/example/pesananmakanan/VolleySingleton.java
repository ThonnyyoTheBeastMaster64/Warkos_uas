package com.example.pesananmakanan;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import android.util.LruCache;

public final class VolleySingleton {

    private static VolleySingleton instance;
    private final RequestQueue requestQueue;
    private final ImageLoader imageLoader;

    private VolleySingleton(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        final LruCache<String, Bitmap> cache = new LruCache<>(50);
        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            @Override public Bitmap getBitmap(String url) { return cache.get(url); }
            @Override public void putBitmap(String url, Bitmap bitmap) { cache.put(url, bitmap); }
        });
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) instance = new VolleySingleton(context);
        return instance;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(15000, 1, 1f));
        requestQueue.add(request);
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}