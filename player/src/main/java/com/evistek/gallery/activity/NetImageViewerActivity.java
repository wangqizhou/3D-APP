package com.evistek.gallery.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.evistek.gallery.R;
import com.evistek.gallery.cache.ImageLoader;
import com.evistek.gallery.model.Image;
import com.evistek.gallery.net.Config;
import com.evistek.gallery.net.DownloadManager;
import com.evistek.gallery.net.callback.DownloadCallback;
import com.evistek.gallery.render.ImageRender;
import com.evistek.gallery.render.RenderBase;
import com.evistek.gallery.render.RenderBase.OnSurfaceChangedListener;
import com.evistek.gallery.utils.EvisUtil;
import com.evistek.gallery.utils.PanelConfig;
import com.evistek.gallery.utils.Utils;
import com.evistek.gallery.view.ControllerView;
import com.evistek.gallery.view.IControl;
import com.evistek.gallery.view.RoundProgressBar;

import java.util.ArrayList;

public class NetImageViewerActivity extends Activity implements IControl, OnSurfaceChangedListener {
    private static final String TAG = "NetImageViewerActivity";
    private static final int PROGRESSBAR_WIDTH = 300;
    private static final int PROGRESSBAR_HEIGHT = 300;
    private static final int FLING_VELOCITY_X = 3000;

    private static final int MSG_UPDATE_VIEW = 0;
    private static final int MSG_DOWNLOAD_DONE = 1;
    private static final int MSG_UPDATE_PROGRESS = 2;
    private static final int MSG_FIRSTIMAGE_DOWNLOAD_DONE = 3;

    private static float sCurrent2DBrightness;

    private Context mContext;
    private GLSurfaceView mGLSurfaceView;
    private ImageRender mRender;
    private ControllerView mControllerView;
    private PanelConfig.PanelDevice mDevice;
    private GestureDetector mGestureDetector;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private int mExtraWindowHeight;
    private int mControlWindowHeight;
    private boolean isControllerShow = false;

    private float startDistance = 0;
    private PointF startMidPoint = new PointF();
    private PointF startPoint = new PointF();

    private int mImageIndex;
    private String mImageUrl;
    private static final String IMAGE_LIST = "image_list";
    private static final String IMAGE_INDEX = "image_index";
    private static final String IMAGE_CACHEDLIST = "image_cachedList";

    private String mImageCoverUrlFile = Config.DISK_CACHE_PATH + "myImgCoverUrl";
    private String mImageUrlListFile = Config.DISK_CACHE_PATH + "myImageUrl";
    private String mCachedImageListFile = Config.DISK_CACHE_PATH + "cachedImageUrl";
    private ArrayList<String> mImageIdList = new ArrayList<String>();
    private ArrayList<Image> mImageList = new ArrayList<Image>();
    private ArrayList<String> mCachedImagelist = new ArrayList<String>();

    private LayoutInflater mInflater;
    private RoundProgressBar mProgressBar;
    private TextView mProgressBarTextView;
    private PopupWindow mDownloadProgress;
    private DownloadManager mDownloadManager;

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_VIEW:
                showController();
                break;
            case MSG_DOWNLOAD_DONE:
                hideProgressBar();

                mImageUrl = (String) msg.obj;
                playImage();
                break;
            case MSG_UPDATE_PROGRESS:
                int progress = ((Integer)msg.obj).intValue();
                mProgressBar.setProgress(progress);
                break;
            case MSG_FIRSTIMAGE_DOWNLOAD_DONE:
                hideProgressBar();
                if (mImageIndex != -1 && mImageUrl != null && mImageIdList != null) {
                    String url = mImageIdList.get((int) mImageIndex);
                    mControllerView.setNameView(getFileName(url));
                    playImage();
                    Log.i(TAG, "Intent index: " + mImageIndex + " url: " + mImageUrl);
                } else {
                    Log.e(TAG, "Intent info is error, index: " + mImageIndex + " url: " + mImageUrl);
                }
                break;
            }

            return true;
        }
    });

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = E3DApplication.getInstance().getApplicationContext();
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(2);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mGLSurfaceView);

        mInflater = LayoutInflater.from(this);
        View progressView = mInflater.inflate(R.layout.progress_bar, null);
        mProgressBar = (RoundProgressBar) progressView.findViewById(R.id.download_progress);
        mProgressBarTextView = (TextView) progressView.findViewById(R.id.download_progress_tv);
        mProgressBar.setTextColor(Color.WHITE);
        mProgressBarTextView.setTextColor(Color.WHITE);
        mDownloadProgress = new PopupWindow(progressView);

        mDevice = PanelConfig.getInstance(this).findDevice();
        sCurrent2DBrightness = Utils.getBrightness(this);
        mControlWindowHeight = (int) getResources().getDimension(R.dimen.control_window_height);
        mExtraWindowHeight = (int) getResources().getDimension(R.dimen.extra_window_height);

        if (mDevice != null) {
            if (mDevice.getSizeType() == RenderBase.SIZE_TYPE_FULL_SCREEN) {
                Point p = new Point();
                getWindowManager().getDefaultDisplay().getRealSize(p);
                mScreenWidth = p.x;
                mScreenHeight = p.y;
            } else {
                mScreenWidth = getResources().getDisplayMetrics().widthPixels;
                mScreenHeight = getResources().getDisplayMetrics().heightPixels;
            }
        } else {
            mScreenWidth = getResources().getDisplayMetrics().widthPixels;
            mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        }

        mRender = new ImageRender(this);
        mGLSurfaceView.setRenderer(mRender);

        mControllerView = new ControllerView(this, false, true);
        mControllerView.setControl(this);
        mControllerView.setRadioChecked(ImageRender.RENDER_MODE_3D);

        mGestureDetector = new GestureDetector(this, new SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mRender.getRenderMode() == ImageRender.RENDER_MODE_3D) {
                    mControllerView.setRadioChecked(ImageRender.RENDER_MODE_3D2D);
                } else if (mRender.getRenderMode() == ImageRender.RENDER_MODE_3D2D) {
                    mControllerView.setRadioChecked(ImageRender.RENDER_MODE_3D);
                }
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (mRender.getTotalScaleFactor() != 1.0f)
                    return false;

                if (velocityX > FLING_VELOCITY_X) {
                    previous();
                } else if (velocityX < -FLING_VELOCITY_X) {
                    next();
                }
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isControllerShow) {
                    hideController();
                } else {
                    showController();
                }
                return true;
            }
        });
        receiveIntent();
        playImage();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        RenderBase.registerOnSurfaceChangedListener(this);

        mDownloadManager = DownloadManager.getInstance();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EvisUtil.enablePanel3D(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRender != null) {
            if (mRender.getRenderMode() == ImageRender.RENDER_MODE_2D3D
                    || mRender.getRenderMode() == ImageRender.RENDER_MODE_3D) {
                EvisUtil.enablePanel3D(true);
            }
            //mRender.requestRender();
        }
    }

    @Override
    protected void onDestroy() {
        if (mControllerView != null) {
            mControllerView.hideController();
        }

        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            downloadContentThumb(mImageIndex);
        }
    }

    @Override
    public void OnChanged(int width, int height) {
        if (mScreenWidth != width || mScreenHeight != height) {
            mScreenWidth = width;
            mScreenHeight = height;

            if (mHandler != null && isControllerShow)
                mHandler.sendEmptyMessage(MSG_UPDATE_VIEW);
        }

    }

    @Override
    public void scale() {
        // TODO Auto-generated method stub

    }

    @Override
    public void play() {
        // TODO Auto-generated method stub

    }

    @Override
    public void lock() {
        // TODO Auto-generated method stub

    }

    @Override
    public void previous() {
        mDownloadManager.cancel();

        if (--mImageIndex < 0) {
            Toast.makeText(mContext, R.string.no_more_image, Toast.LENGTH_SHORT).show();
            mImageIndex = 0;
        }

        mControllerView.setNameView(getFileName(mImageIdList.get(mImageIndex)));
        mDownloadManager.download(mImageIdList.get(mImageIndex), new ImageDownloadCallback());

        showProgressBar();
    }

    @Override
    public void next() {
        mDownloadManager.cancel();

        if (++mImageIndex > mImageIdList.size() - 1) {
            Toast.makeText(mContext, R.string.no_more_image, Toast.LENGTH_SHORT).show();
            mImageIndex = mImageIdList.size() - 1;
        }

        mControllerView.setNameView(getFileName(mImageIdList.get(mImageIndex)));
        mDownloadManager.download(mImageIdList.get(mImageIndex), new ImageDownloadCallback());

        showProgressBar();
    }

    @Override
    public void back() {
        if (mControllerView != null) {
            mControllerView.hideController();
        }

        mDownloadManager.cancel();
        hideProgressBar();

        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    @Override
    public void setPlayMode(int mode) {
        mRender.setRenderMode(mode);

        switch (mode) {
        case ImageRender.RENDER_MODE_2D:
        case ImageRender.RENDER_MODE_3D2D:
            Utils.setBrightness(this, sCurrent2DBrightness);
            break;
        case ImageRender.RENDER_MODE_2D3D:
        case ImageRender.RENDER_MODE_3D:
            compensateBrightness();
            break;
        default:
            break;
        }
    }

    @Override
    public int getPlayMode() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setDragging(boolean isDragging, int newPos) {
        // TODO Auto-generated method stub

    }

    @Override
    public void startMenu(int position, int which) {
        switch (position) {
        case 0: // Left-Right
            // L->R
            // which = 0, format is LR. which = 1, format is RL
            mRender.setFramePackingFormat(which);
            break;
        case 1: // Eye tracer
            break;
        case 2: // 3D type
            break;
        }

    }

    @Override
    public void showVolumeWindow() {
        // TODO Auto-generated method stub

    }

    @Override
    public void hideVolumeWindow() {
        // TODO Auto-generated method stub

    }

    @Override
    public void showDepthWindow() {
        // TODO Auto-generated method stub

    }

    @Override
    public void hideDepthWindow() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPlayDepth(float depth) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getPlayDepth() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getMaxPlayDepth() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event))
            return true;

        float curDistance = 0;

        switch (event.getAction()) {
        case MotionEvent.ACTION_POINTER_2_DOWN:
            if (event.getPointerCount() == 2) {
                startDistance = distance(event);
                midPoint(startMidPoint, event);
            }
            break;
        case MotionEvent.ACTION_DOWN:
            if (event.getPointerCount() == 1) {
                startPoint.set(event.getX(), event.getY());
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (event.getPointerCount() == 2) {
                curDistance = distance(event);
                mRender.scale(startMidPoint.x, startMidPoint.y, curDistance / startDistance);
                startDistance = curDistance;
            } else if (event.getPointerCount() == 1) {
                mRender.translate(event.getX() - startPoint.x, event.getY() - startPoint.y);
                startPoint.set(event.getX(), event.getY());
            }
            break;
        }
        return true;
    }

    private void hideController() {
        isControllerShow = false;
        mControllerView.hideController();
    }

    private void showController() {
        isControllerShow = true;
        mControllerView.setRadioChecked(mRender.getRenderMode());
        mControllerView.showController(mGLSurfaceView, mScreenWidth, mExtraWindowHeight, mScreenWidth,
                mControlWindowHeight);
    }

    private void receiveIntent() {
        mImageIndex = (int) getIntent().getLongExtra(IMAGE_INDEX, -1L);
        mImageList =(ArrayList<Image>) getIntent().getSerializableExtra(IMAGE_LIST);
        mCachedImagelist = getIntent().getStringArrayListExtra(IMAGE_CACHEDLIST);
        if (mImageList != null) {
            mImageIdList.clear();
            for (Image img : mImageList) {
                mImageIdList.add(img.getUrl());
            }
            Utils.writeObjectToFile(mImageIdList, mImageUrlListFile);
            ArrayList<String> imgCoverUrl = new ArrayList<String>();
            for (Image img : mImageList) {
                imgCoverUrl.add(img.getThumbnail());
            }
            Utils.writeObjectToFile(imgCoverUrl, mImageCoverUrlFile);
        } else {
            if (Utils.isNetworkAvailable() == false) {
                ArrayList<String> imgCoverUrlList = (ArrayList<String>) Utils.readObjectFromFile(mImageCoverUrlFile);
                if (imgCoverUrlList != null) {
                    for (String imgCoverUrl : imgCoverUrlList) {
                        Image img = new Image();
                        img.setThumbnail(imgCoverUrl);
                        mImageList.add(img);
                    }
                    mImageIdList = (ArrayList<String>) Utils.readObjectFromFile(mImageUrlListFile);
                } else {
                    Toast.makeText(E3DApplication.getInstance(), R.string.net_not_available, Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                Toast.makeText(E3DApplication.getInstance(), R.string.load_fail_images, Toast.LENGTH_SHORT)
                        .show();
                Log.e(TAG, "getData error!");
            }
        }
    }

    private void compensateBrightness() {
        final float FACTOR = 2;
        sCurrent2DBrightness = Utils.getBrightness(this);
        if (mDevice != null && mDevice.isBarrier()) {
            float brightness = FACTOR * sCurrent2DBrightness;
            if (brightness > WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                brightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            }
            Utils.setBrightness(this, brightness);
        }
    }

    private void playImage() {
        Bitmap image = null;
        if (mImageUrl != null) {
            image = ImageLoader.getInstance(mContext).loadImage(mImageUrl);
            if (image != null) {
                mControllerView.setRadioChecked(ImageRender.RENDER_MODE_3D);
                mRender.updateTextureImage(image);
            } else {
                Log.e(TAG, "Failed to decode image path: " + mImageUrl);
            }
        }
    }

    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private void showProgressBar() {
        mProgressBar.setProgress(0);
        if (!mDownloadProgress.isShowing()) {
            mDownloadProgress.showAtLocation(mGLSurfaceView, Gravity.TOP, 0, 0);
            mDownloadProgress.update(0, mScreenHeight / 2 - PROGRESSBAR_WIDTH / 2, PROGRESSBAR_WIDTH,
                    PROGRESSBAR_WIDTH);
        }
    }

    private void hideProgressBar() {
        if (mDownloadProgress.isShowing()) {
            mDownloadProgress.dismiss();
        }
    }

    private String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    public void downloadContentThumb(final int position) {
        DownloadManager.getInstance().cancel();
        boolean isCached = false;
        for(String url : mCachedImagelist){
            if(mImageIdList.get(position).equals(url)){
                isCached = true;
                break;
            }
        }
        if (!isCached && Utils.isNetworkAvailable() == false) {
            Toast.makeText(E3DApplication.getInstance(), R.string.net_not_available, Toast.LENGTH_SHORT)
                    .show();
        } else {
            downloadImage(position);
            showProgressBar();
        }
    }

    public void downloadImage(final int position){
        if (mImageIdList != null) {
            DownloadManager.getInstance().download(mImageIdList.get(position), new DownloadCallback() {
                @Override
                public void onResult(int code, String msg) {
                    if (code == 200) {
                        mCachedImagelist.add(mImageIdList.get(position));
                        Utils.writeObjectToFile(mCachedImagelist, mCachedImageListFile);
                        Message message = mHandler.obtainMessage();
                        mImageUrl = msg;
                        message.what = MSG_FIRSTIMAGE_DOWNLOAD_DONE;
                        mHandler.sendMessage(message);
                    } else {
                        Toast.makeText(E3DApplication.getInstance(), R.string.load_fail_images, Toast.LENGTH_SHORT)
                                .show();
                    }
                }

                @Override
                public void onProgress(int progress) {
                    Message message = mHandler.obtainMessage();
                    message.what = MSG_UPDATE_PROGRESS;
                    message.obj = progress;
                    mHandler.sendMessage(message);
                }
            });
        }
    }

    private class ImageDownloadCallback extends DownloadCallback {

        @Override
        public void onResult(int code, String msg) {

            if (code == 200) {
                Message message = new Message();
                message.what = MSG_DOWNLOAD_DONE;
                message.obj = msg;
                mHandler.sendMessage(message);
            } else {
                Log.w(TAG, "code: " + code + " msg: " + msg);
            }
        }

        @Override
        public void onProgress(int progress) {
            Log.i(TAG, "download progress: " + progress + "%");
            Message message = new Message();
            message.what = MSG_UPDATE_PROGRESS;
            message.obj = progress;
            mHandler.sendMessage(message);
        }
    }

    @Override
    public boolean isKeepAR() {
        // TODO Auto-generated method stub
        return false;
    }
}
