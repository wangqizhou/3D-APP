package com.evistek.gallery.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.activity.MainActivity;
import com.evistek.gallery.database.DBManager;
import com.evistek.gallery.model.Product;
import com.evistek.gallery.net.Config;
import com.evistek.gallery.net.NetWorkService;
import com.evistek.gallery.net.callback.AdsCallback;
import com.evistek.gallery.net.json.JsonRespAds;
import com.evistek.gallery.utils.Utils;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SplashScreenActivity extends Activity
{
    private static final String TAG = "SplashScreenActivity";
    private static final int LOAD_NET_IMAGE = 0;
    private static final int LOAD_LOCAL_IMAGE = 1;
    private static final boolean DEBUG = false;
    private static final int SPLASH_DELAY_TIME = 3000;

    @BindView(R.id.imageView)
    ImageView imageView;

    private List<Product> adsList;
    private Picasso picasso;
    private OkHttpClient okHttpClient = new OkHttpClient();
    private Runnable runnable;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_NET_IMAGE:
                    int index = new Random().nextInt(adsList.size());
                    picasso.load(adsList.get(index).getImgUrl())
                            .error(R.drawable.bg_splash_screen_top)
                            .into(imageView);
                    break;
                case LOAD_LOCAL_IMAGE:
                    imageView.setImageResource(R.drawable.bg_splash_screen_top);
                    break;
            }

            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen);
        ButterKnife.bind(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Utils.isNetworkAvailable()) {
            okHttpClient.setConnectTimeout(1000, TimeUnit.MILLISECONDS);
            okHttpClient.setReadTimeout(1000, TimeUnit.MILLISECONDS);
            okHttpClient.setWriteTimeout(1000, TimeUnit.MILLISECONDS);

            picasso = new Picasso.Builder(this).downloader(new OkHttpDownloader(okHttpClient)).build();
            getAdsData();
        } else {
            sendMessage(LOAD_LOCAL_IMAGE);
        }

        final DBManager manager = E3DApplication.getInstance().getManager();
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start_time = System.currentTimeMillis();
                manager.sync();
                long end_time = System.currentTimeMillis();
                if (DEBUG) {
                    Log.d(TAG, "Init cost time is : " + (end_time - start_time) + "ms");
                }
            }
        }).start();

        mHandler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                startViewer();
            }
        }, SPLASH_DELAY_TIME);
    }

    private void getAdsData() {
        adsList = new ArrayList<Product>();
        String url = Config.APP_PRODUCT;
        NetWorkService.getAllAds(url,new AdsCallback() {
            @Override
            public void onResult(int code, List<Product> jsonResp) {
                if (code == 200) {
                    adsList = jsonResp;
                    sendMessage(LOAD_NET_IMAGE);

                } else {
                    sendMessage(LOAD_LOCAL_IMAGE);
                }
            }
        });
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }

    private void startViewer() {
        //Intent intent = new Intent(this, MediaMenuActivity.class);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        mHandler.removeCallbacks(runnable);
    }
}
