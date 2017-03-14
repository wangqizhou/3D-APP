package com.evistek.gallery.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.evistek.gallery.R;

/**
 * Author: Weixiang Zhang
 * Email: wxzhang@evistek.com
 * Date: 2016/10/25.
 */

public class CompatibilityChecker {

    public static boolean check(Context context) {
        return true;

//        PanelConfig.PanelDevice dev = PanelConfig.getInstance(context).findDevice();
//        if (dev != null && !dev.getModel().equalsIgnoreCase("Dummy")) {
//            return true;
//        }
//
//        return false;
    }

    public static void notifyDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.compatibility_msg)
                .setTitle(R.string.compatibility_title);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
