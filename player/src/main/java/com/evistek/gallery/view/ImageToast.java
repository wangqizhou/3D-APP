package com.evistek.gallery.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.evistek.gallery.R;

public class ImageToast
{
    private Toast mToast;
    private Context mContext;
    private TextView mValue;

    public ImageToast(Context context) {
        mContext = context;
        initToast();
    }

    private void initToast() {
        mToast = new Toast(mContext);
        View layout = LayoutInflater.from(mContext).inflate(R.layout.toast, null);
        mValue = (TextView) layout.findViewById(R.id.value);
        mToast.setView(layout);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.setDuration(Toast.LENGTH_SHORT);
    }

    public void setIcon(int id) {
        Drawable image = mContext.getResources().getDrawable(id);
        image.setBounds(0, 0, 50, 50);
        if (mValue != null) {
            mValue.setCompoundDrawables(null, image, null, null);
        }
    }

    public void setInfo(String msg) {
        if (mValue != null) {
            mValue.setText(msg);
        }
    }

    public void show() {
        mToast.show();
    }

    public void cancel() {
        mToast.cancel();
    }
}