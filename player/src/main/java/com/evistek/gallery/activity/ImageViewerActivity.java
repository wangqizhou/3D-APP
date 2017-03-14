package com.evistek.gallery.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.evistek.gallery.R;
import com.evistek.gallery.cache.ImageLoader;
import com.evistek.gallery.database.MediaDataBase;
import com.evistek.gallery.render.ImageRender;
import com.evistek.gallery.render.RenderBase;
import com.evistek.gallery.utils.EvisUtil;
import com.evistek.gallery.utils.PanelConfig;
import com.evistek.gallery.utils.Utils;
import com.evistek.gallery.view.ControllerView;
import com.evistek.gallery.view.IControl;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class ImageViewerActivity extends Activity implements IControl, RenderBase.OnSurfaceChangedListener
{
    public static final String IMAGE_ID = "image_id";
    public static final String IMAGE_LIST = "image_list";
    public static final String IMAGE_PATH = "image_path";
    public static final String IMAGE_SLIDE = "image_slide";

    private static final int MSG_UPDATE_VIEW = 0;
    private static final int MSG_NEXT_IMAGE = 1;
    private static final int MSG_PREVIOUS_IMAGE = 2;

    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    private GLSurfaceView mGLSurfaceView;
    private ImageRender mRender;
    private GestureDetector mGestureDetector;
    private ControllerView mControllerView;
    private MediaDataBase mImageData = E3DApplication.getInstance().getDataBase();
    private boolean isControllerShow = false;

    private static final int mIntentRequestCode = 0x123456;

    private static final int FLING_VELOCITY_X = 3000;
    private static final long SLIDE_DELAY = 2000;

    private static int mIndex = -1;
    private static long mImageId;
    private static boolean isSlideShow;
    private static ArrayList<String> mImageList;
    private String mPath;

    private static float sCurrent2DBrightness;
    private PanelConfig.PanelDevice mDevice;
    private int mExtraWindowHeight;
    private int mControlWindowHeight;

    private boolean mInManualOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(2);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mGLSurfaceView);

        mDevice = PanelConfig.getInstance(this).findDevice();
        sCurrent2DBrightness = getActivityBrightness();
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

        mControllerView = new ControllerView(this, false);
        mControllerView.setControl(this);

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

        String name = null;
        mImageId = getIntent().getLongExtra(IMAGE_ID, -1L);
        mImageList = getIntent().getStringArrayListExtra(IMAGE_LIST);
        isSlideShow = getIntent().getBooleanExtra(IMAGE_SLIDE, false);
        if (mImageId == -1L) {
            mPath = getIntent().getStringExtra(IMAGE_PATH);
            name = mPath.substring(mPath.lastIndexOf('/') + 1);
            mImageId = Utils.getMediaId(mPath, false);
            mInManualOpen = true;
        } else {
            mIndex = mImageList.indexOf(String.valueOf(mImageId));
            mInManualOpen = false;
        }

        name = Utils.getMediaName(mImageId, false);
        playId();
        mControllerView.setNameView(name);
        mLoadImage.start();
        if (isSlideShow) {
            mSlideHandler.sendEmptyMessageDelayed(MSG_NEXT_IMAGE, SLIDE_DELAY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        RenderBase.registerOnSurfaceChangedListener(this);
    }

    private Handler mSlideHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isSlideShow) {
                next();
                sendEmptyMessageDelayed(MSG_NEXT_IMAGE, SLIDE_DELAY);
            }
            super.handleMessage(msg);
        }
    };

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what) {
            case MSG_UPDATE_VIEW:
                showController();
                break;
            case MSG_NEXT_IMAGE:
            case MSG_PREVIOUS_IMAGE:
                Bitmap bitmap = ImageLoader.getInstance(ImageViewerActivity.this).loadImage(mImageId);
                if (mLoadHandler != null) {
                    Message loadMsg = mLoadHandler.obtainMessage();
                    mLoadHandler.sendMessage(loadMsg);
                }
                if (bitmap != null) {
                    int type = mImageData.getSrcType(mImageId);
                    if (type == MediaDataBase.SRC_TYPE_3D) {
                        mControllerView.setRadioChecked(ImageRender.RENDER_MODE_3D);
                    } else if (type == MediaDataBase.SRC_TYPE_2D) {
                        mControllerView.setRadioChecked(ImageRender.RENDER_MODE_2D);
                    } else {
                        Log.e("PhotoViewerActivity", "isSbs error");
                    }
                    mRender.updateTextureImage(bitmap);
                }

                mControllerView.setNameView(Utils.getMediaName(mImageId, false));

                Runtime rt = Runtime.getRuntime();
                long totalMemory = rt.totalMemory();
                long maxMemory = rt.maxMemory();
                long rate = totalMemory * 100L / maxMemory;
                Log.i("Tag", "Memory used(total/max) : " + rate + "%");
                if (rate > 85L) {
                    System.gc();
                }
                break;
            }

            return true;
        }
    });

    private Handler mLoadHandler;
    private Thread mLoadImage = new Thread() {
        @Override
        public void run() {
            Looper.prepare();
            mLoadHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    ImageLoader il = ImageLoader.getInstance(ImageViewerActivity.this);
                    if (mIndex == 0) {
                        long id2 = Long.valueOf(mImageList.get(mIndex + 1));
                        il.loadImage(id2);
                    } else if (mIndex == mImageList.size() - 1) {
                        long id1 = Long.valueOf(mImageList.get(mIndex - 1));
                        il.loadImage(id1);
                    } else {
                        long id1 = Long.valueOf(mImageList.get(mIndex - 1));
                        long id2 = Long.valueOf(mImageList.get(mIndex + 1));
                        il.loadImage(id1);
                        il.loadImage(id2);
                    }
                }
            };
            Looper.loop();
        }
    };

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
            int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth,
            int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
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

    private float startDistance = 0;
    private PointF startMidPoint = new PointF();
    private PointF startPoint = new PointF();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isSlideShow) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            isSlideShow = false;
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        EvisUtil.enablePanel3D(false);
        if (isSlideShow) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            isSlideShow = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRender != null) {
            if (mRender.getRenderMode() == ImageRender.RENDER_MODE_2D3D
                    || mRender.getRenderMode() == ImageRender.RENDER_MODE_3D) {
                EvisUtil.enablePanel3D(true);
            }
            mRender.requestRender();
        }
    }

    @Override
    protected void onDestroy() {
        if (mControllerView != null) {
            mControllerView.hideController();
        }
        if (mLoadHandler != null) {
            mLoadHandler.getLooper().quit();
        }
        super.onDestroy();
    }

    private void playId() {
        Bitmap bitmap = ImageLoader.getInstance(this).loadImage(mImageId);

        int type = mImageData.getSrcType(mImageId);
        if (type == MediaDataBase.SRC_TYPE_3D) {
            mControllerView.setRadioChecked(ImageRender.RENDER_MODE_3D);
        } else if (type == 0) {
            mControllerView.setRadioChecked(ImageRender.RENDER_MODE_2D);
        } else {
            Log.e("PhotoViewerActivity", "isSbs error");
        }
        mRender.updateTextureImage(bitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == mIntentRequestCode && data != null) {
            Uri uri = data.getData();
            if (uri != null) {

                Bitmap bitmap = ImageLoader.getInstance(this).loadImage(uri);

                if (EvisUtil.isSbs(bitmap) == 1)
                    mRender.setRenderMode(ImageRender.RENDER_MODE_3D);
                else if (EvisUtil.isSbs(bitmap) == 0)
                    mRender.setRenderMode(ImageRender.RENDER_MODE_2D);
                else
                    Log.e("PhotoViewerActivity", "isSbs error");
                mRender.updateTextureImage(bitmap);
            }
        }
    }

    private void hideController() {
        isControllerShow = false;
        mControllerView.hideController();
    }

    private void showController() {
        isControllerShow = true;
        mControllerView.setRadioChecked(mRender.getRenderMode());
        mControllerView.showController(mGLSurfaceView, mScreenWidth, mExtraWindowHeight,
                mScreenWidth, mControlWindowHeight);
    }

    @Override
    public void previous() {
        if (mInManualOpen) {
            return;
        }

        --mIndex;
        if (mIndex < 0) {
            mIndex = 0;
            return;
        }
        if (mImageList != null) {
            mImageId = Long.valueOf(mImageList.get(mIndex));
        }
        mHandler.sendEmptyMessage(MSG_PREVIOUS_IMAGE);
    }

    @Override
    public void next() {
        if (mInManualOpen) {
            return;
        }

        ++mIndex;

        if (mImageList != null) {
            int size = mImageList.size();
            if (isSlideShow) {
                mIndex %= size;
            } else if (mIndex >= size) {
                mIndex = size - 1;
                return;
            }

            mImageId = Long.valueOf(mImageList.get(mIndex));
        }

        mHandler.sendEmptyMessage(MSG_NEXT_IMAGE);
    }

    @Override
    public void scale() {
    }

    @Override
    public boolean isKeepAR() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void play() {
    }

    @Override
    public void lock() {
    }

    @Override
    public void back() {
        if (mControllerView != null) {
            mControllerView.hideController();
        }
        onBackPressed();
        finish();
    }

    @Override
    public void setPlayMode(int mode) {
        mRender.setRenderMode(mode);

        switch (mode) {
        case ImageRender.RENDER_MODE_2D:
        case ImageRender.RENDER_MODE_3D2D:
            setActivityBrightness(sCurrent2DBrightness);
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
        return 0;
    }

    @Override
    public void setDragging(boolean isDragging, int newPos) {
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

    public void hideVolumeWindow() {
        // TODO Auto-generated method stub

    }

    @Override
    public void hideDepthWindow() {
        // TODO Auto-generated method stub

    }

    @Override
    public void showDepthWindow() {
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

    private void setActivityBrightness(float brightness) {
        Utils.setBrightness(this, brightness);
    }

    private float getActivityBrightness() {
        // Activity brightness range: 0 ~ 1
        return Utils.getBrightness(this);
    }

    private void compensateBrightness() {
        final float FACTOR = 2;
        sCurrent2DBrightness = getActivityBrightness();
        if (mDevice != null && mDevice.isBarrier() && mDevice.getNode() != null) {
            float brightness = FACTOR * sCurrent2DBrightness;
            if (brightness > WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                brightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            }
            Utils.setBrightness(this, brightness);
        }
    }

    @Override
    public void OnChanged(int width, int height) {
        if (mScreenWidth != width || mScreenHeight != height) {
            mScreenWidth = width;
            mScreenHeight = height;

            if (mHandler != null && !isSlideShow && isControllerShow)
                mHandler.sendEmptyMessage(MSG_UPDATE_VIEW);
        }
    }
}
