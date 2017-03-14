package com.evistek.gallery.net;

import android.widget.ImageView;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.E3DApplication;
import com.squareup.picasso.Picasso;

import java.io.File;

public class BitmapLoadManager {
    public static final int URI_TYPE_LOCAL = 0;
    public static final int URI_TYPE_REMOTE = 1;

    public static void display(ImageView imageView, String uriString, int type, int placeholderResId) {
        /*
         *  Uncomment this to debug
         *  Red indicator means loading image from Internet.
         *  Green indicator means loading image from memory.
         *  Blue indicator means loading image from disk.
         */
        //Picasso.with(E3DApplication.getInstance()).setIndicatorsEnabled(true);

        switch (type) {
            case URI_TYPE_REMOTE:
                Picasso.with(E3DApplication.getInstance())
                        .load(uriString)
                        .placeholder(placeholderResId)
                        .into(imageView);
                break;
            case URI_TYPE_LOCAL:
                Picasso.with(E3DApplication.getInstance())
                        .load(new File(uriString))
                        .placeholder(placeholderResId)
                        .into(imageView);
                break;
        }
    }

    public static void display(ImageView imageView, String uriString) {
        display(imageView, uriString, URI_TYPE_REMOTE, R.drawable.home_place_holder);
    }

    public static void display(ImageView imageView, String uriString, int uriType) {
        display(imageView, uriString, uriType, R.drawable.home_place_holder);
    }
}
