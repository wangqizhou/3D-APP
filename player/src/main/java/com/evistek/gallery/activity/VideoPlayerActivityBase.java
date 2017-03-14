package com.evistek.gallery.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.evistek.gallery.R;
import com.evistek.gallery.database.MediaDataBase;
import com.evistek.gallery.render.RenderBase;
import com.evistek.gallery.render.RenderBase.OnSurfaceChangedListener;
import com.evistek.gallery.render.VideoRender;
import com.evistek.gallery.utils.EvisUtil;
import com.evistek.gallery.utils.PanelConfig;
import com.evistek.gallery.utils.Utils;
import com.evistek.gallery.view.ControllerView;
import com.evistek.gallery.view.IControl;
import com.evistek.gallery.view.ImageToast;
import com.evistek.gallery.view.VideoSurfaceView;

import java.lang.ref.WeakReference;

public abstract class VideoPlayerActivityBase extends Activity implements IControl, OnSurfaceChangedListener {
    protected static final int MSG_PROGRESS_CHANGED = 0;
    protected static final int MSG_HIDE_CONTROLER = 1;
    protected static final int MSG_SOUND_VIEW = 2;
    protected static final int MSG_BRIGHT_VIEW = 3;
    protected static final int MSG_SEEK_VIEW = 4;
    protected static final int MSG_UPDATE_VIEW = 5;

    protected static final long HIDE_CONTROLER_DELAY = 5000L;
    protected static final long PROGRESS_DELAY = 1000L;
    protected static final int SEEK_PRECISION = 500; // 00 ms per pixel
    protected static final boolean PLAY = true;
    protected static final boolean PAUSE = false;

    protected boolean mIsControllerShow = true;
    protected boolean mIsPaused = false;
    protected boolean mIsLocked = false;
    protected boolean mIsDragging = false;
    protected boolean mIsCompleted = false;
    protected boolean mIsPrepared = false;
    protected boolean mSaveTag = false;
    protected VideoSurfaceView mSurfaceView = null;
    protected GestureDetector mGestureDetector = null;
    protected ControllerView mControllerView;
    protected UpdateViewHandler mHandler;
    protected ImageToast mToast;

    protected static int mScreenWidth = 0;
    protected static int mScreenHeight = 0;

    protected float moveDistanceY = 0f;
    protected float moveDistanceX = 0f;
    protected boolean mIsInScroll = false;
    protected static long mVideoDuration = 0;
    protected static int mCurrentPosition = 0;
    protected static int mSeekTime = 0;
    protected AudioManager mAudioManager;

    protected static float mCurrent2DBrightness;
    protected PanelConfig.PanelDevice mDevice;
    protected int mExtraWindowHeight;
    protected int mControlWindowHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_player);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        init();
        launchMediaPlayer();
    }

    abstract protected void receiveIntent();
    abstract protected int getSourceType();
    abstract protected void resetPlayPosition();
    abstract protected void saveCurrentPosition();
    abstract protected void resumePlayPosition();

    private static class UpdateViewHandler extends Handler
    {
        private WeakReference<VideoPlayerActivityBase> mActivity;

        public UpdateViewHandler(VideoPlayerActivityBase activity) {
            mActivity = new WeakReference<VideoPlayerActivityBase>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoPlayerActivityBase theActivity = mActivity.get();
            if (theActivity == null) {
                return;
            }
            switch (msg.what) {
            case MSG_PROGRESS_CHANGED:
                if (theActivity.mIsControllerShow && !theActivity.mIsDragging) {
                    theActivity.updateProgress();
                    sendEmptyMessageDelayed(MSG_PROGRESS_CHANGED, PROGRESS_DELAY);
                }
                break;
            case MSG_HIDE_CONTROLER:
                theActivity.hideController();
                break;
            case MSG_SOUND_VIEW:
                theActivity.mToast.setIcon(R.drawable.ic_volume);
                theActivity.mToast.setInfo(msg.arg1 + "%");
                theActivity.mToast.show();
                break;
            case MSG_BRIGHT_VIEW:
                theActivity.mToast.setIcon(R.drawable.ic_brightness);
                theActivity.mToast.setInfo(msg.arg1 + "%");
                theActivity.mToast.show();
                break;
            case MSG_SEEK_VIEW:
                if (msg.arg1 > 0) {
                    theActivity.mToast.setIcon(R.drawable.ic_controler_seek_forward);
                } else {
                    theActivity.mToast.setIcon(R.drawable.ic_controler_seek_back);
                }
                theActivity.mToast.setInfo(Utils.stringForTime(mSeekTime));
                theActivity.mToast.show();
                break;
            case MSG_UPDATE_VIEW:
                theActivity.showController();
                theActivity.hideControllerDelay();
                break;
            }
            super.handleMessage(msg);
        }
    }

    private void init() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mToast = new ImageToast(this);
        mHandler = new UpdateViewHandler(this);
        RenderBase.registerOnSurfaceChangedListener(this);
        mCurrent2DBrightness = getActivityBrightness();

        getScreenSize();
        mControlWindowHeight = (int) getResources().getDimension(R.dimen.control_window_height);
        mExtraWindowHeight = (int) getResources().getDimension(R.dimen.extra_window_height);
        mGestureDetector = new GestureDetector(this, new SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (!mIsControllerShow) {
                    showController();
                    hideControllerDelay();
                } else {
                    cancelDelayHide();
                    hideController();
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                int type = getSourceType();

                switch (getPlayMode()) {
                case VideoRender.RENDER_MODE_2D:
                case VideoRender.RENDER_MODE_3D2D:
                    if (type == MediaDataBase.SRC_TYPE_3D)
                        mControllerView.setRadioChecked(VideoRender.RENDER_MODE_3D);
                    break;
                case VideoRender.RENDER_MODE_2D3D:
                case VideoRender.RENDER_MODE_3D:
                    if (type == MediaDataBase.SRC_TYPE_3D)
                        mControllerView.setRadioChecked(VideoRender.RENDER_MODE_3D2D);
                    break;
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (e1 == null || e2 == null) {
                    return false;
                }
                int percent = 0;
                Message message = new Message();
                boolean update = false;
                if ((e1.getX() < mScreenWidth / 2) && (Math.abs(distanceY) > Math.abs(distanceX))) {
                    moveDistanceY += distanceY;
                    int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int adj = (int) (moveDistanceY * maxVolume / mScreenHeight);
                    int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    message.what = MSG_SOUND_VIEW;
                    if (Math.abs(adj) > 1) {
                        update = true;
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                currentVolume + adj, 0);
                        moveDistanceY = 0;
                        percent = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100
                                / maxVolume;
                    }
                } else if ((e1.getX() > mScreenWidth / 2 && e1.getX() < mScreenWidth * 0.85)
                        && (Math.abs(distanceY) > Math.abs(distanceX))) {
                    moveDistanceY += distanceY;
                    int adj = (int) (moveDistanceY * 255 / mScreenHeight);
                    message.what = MSG_BRIGHT_VIEW;
                    if (Math.abs(adj) > 1) {
                        update = true;
                        float b = getActivityBrightness() + adj / 255f;
                        if (b > 1.0)
                            b = 1.0f;
                        else if (b < 0.0)
                            b = 0.0f;

                        setActivityBrightness(b);
                        moveDistanceY = 0;
                        percent = (int) (getActivityBrightness() * 100);
                    }
                } else if (Math.abs(distanceY / distanceX) < Math.tan(Math.PI / 12)
                        && (e1.getX() > mScreenWidth * 0.15 && e1.getX() < mScreenWidth * 0.85)) {
                    update = true;
                    mIsInScroll = true;
                    mCurrentPosition = mSurfaceView.getCurrentPosition();
                    moveDistanceX += distanceX;
                    int adj = (int) (moveDistanceX * SEEK_PRECISION);
                    message.what = MSG_SEEK_VIEW;
                    percent = -adj;
                    if (percent + mCurrentPosition <= 0) {
                        percent = -mCurrentPosition; // seek to start position
                    } else if (percent + mCurrentPosition > mVideoDuration) {
                        percent = (int) (mVideoDuration - mCurrentPosition);
                    }
                    mSeekTime = percent + mCurrentPosition;
                }

                if (update) {
                    message.arg1 = percent;
                    mHandler.sendMessage(message);
                }
                return true;
            }
        });

    }

    @SuppressLint("NewApi")
    private void getScreenSize() {
        mDevice = PanelConfig.getInstance(this).findDevice();
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
    }

    protected void hideController() {
        mControllerView.hideController();
        mIsControllerShow = false;
    }

    protected void hideControllerDelay() {
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLER, HIDE_CONTROLER_DELAY);
    }

    private void showController() {
        updateProgress();
        mControllerView.showController(mSurfaceView, mScreenWidth, mExtraWindowHeight, mScreenWidth, mControlWindowHeight);
        mIsControllerShow = true;
        mHandler.sendEmptyMessage(MSG_PROGRESS_CHANGED);
    }

    protected void cancelDelayHide() {
        mHandler.removeMessages(MSG_HIDE_CONTROLER);
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
        mCurrent2DBrightness = getActivityBrightness();
        if (mDevice != null && mDevice.isBarrier() && mDevice.getNode() != null) {
            float brightness = FACTOR * mCurrent2DBrightness;
            if (brightness > WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                brightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            }
            Utils.setBrightness(this, brightness);
        }
    }

    protected void launchMediaPlayer() {
        mIsPrepared = false;
        if (mSurfaceView != null) {
            mSurfaceView.stopPlayback();
            mSurfaceView = null;
        }
        mSurfaceView = new VideoSurfaceView(this);
        mSurfaceView.setOnErrorListener(mOnErrorListener);
        mSurfaceView.setOnPreparedListener(mOnPreparedListener);
        mSurfaceView.setOnCompletionListener(mOnCompletionListener);
        if (mControllerView == null) {
            mControllerView = new ControllerView(this, true);
            mControllerView.setControl(this);
        }

        Looper.myQueue().addIdleHandler(new IdleHandler() {
            @Override
            public boolean queueIdle() {
                if (mSurfaceView.isShown()) {
                    mControllerView.showController(mSurfaceView, mScreenWidth, mExtraWindowHeight,
                            mScreenWidth, mControlWindowHeight);
                }
                return false;
            }
        });
        hideControllerDelay();
    }

    private void updateProgress() {
        int i = mSurfaceView.getCurrentPosition();
        mControllerView.updateProgress(i);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        if (!mIsLocked) {
            result = mGestureDetector.onTouchEvent(event);
            if (!result) {
                result = super.onTouchEvent(event);
            }

            if (event.getAction() == MotionEvent.ACTION_UP && mIsInScroll) {
                mIsInScroll = false;
                moveDistanceX = 0;
                mSurfaceView.seekTo(mSeekTime);
            }
        } else {
            cancelDelayHide();
            showController();
            hideControllerDelay();
        }
        return result;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        getScreenSize();
        if (mIsControllerShow) {
            cancelDelayHide();
            hideController();
            showController();
            hideControllerDelay();
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        cancelDelayHide();
        hideController();
        if (mSurfaceView != null) {
            if (mSurfaceView.isPlaying()) {
                mSurfaceView.stopPlayback();
            }
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (mSurfaceView != null) {
            mSurfaceView.onResume();

            if (!mIsPaused) {
                mSurfaceView.start();
                mControllerView.setPlayIcon(PAUSE);
                hideControllerDelay();
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }

            int playMode = mSurfaceView.getPlayMode();
            if (playMode == VideoRender.RENDER_MODE_3D || playMode == VideoRender.RENDER_MODE_2D3D) {
                EvisUtil.enablePanel3D(true);
            }
        }
        super.onResume();
    }

    private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer arg0) {
            mIsPrepared = true;
            mIsCompleted = false;
            int i = mSurfaceView.getDuration();
            mVideoDuration = i;
            mControllerView.setDuration(i);
            resumePlayPosition();
            mSurfaceView.start();
            mControllerView.setPlayIcon(PAUSE);
            hideControllerDelay();
            mHandler.sendEmptyMessage(MSG_PROGRESS_CHANGED);
        }
    };

    private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mIsCompleted = true;
            resetPlayPosition();
            if (Utils.getValue(Utils.SHARED_VIDEO_AUTO_NEXT, false)) {
                next();
                Toast.makeText(VideoPlayerActivityBase.this, R.string.menu_autonext, Toast.LENGTH_SHORT)
                        .show();
            } else {
                back();
            }
        }
    };

    private OnErrorListener mOnErrorListener = new OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mSurfaceView.stopPlayback();
            new AlertDialog.Builder(VideoPlayerActivityBase.this).setTitle(R.string.error)
                    .setMessage(R.string.error_message)
                    .setPositiveButton(R.string.ok, new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            back();
                        }
                    }).setCancelable(false).show();
            return false;
        }
    };

    @Override
    public void play() {
        cancelDelayHide();
        if (mIsPaused) {
            mSurfaceView.start();
            mControllerView.setPlayIcon(PAUSE);
            hideControllerDelay();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            mSurfaceView.pause();
            mControllerView.setPlayIcon(PLAY);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        mIsPaused = !mIsPaused;
    }

    @Override
    public void lock() {
        cancelDelayHide();
        mIsLocked = !mIsLocked;
        mControllerView.setLocked(mIsLocked);
        hideControllerDelay();
    }

    @Override
    public void setDragging(boolean isDrag, int newPos) {
        if (newPos < 0) {
            if (isDrag) {
                cancelDelayHide();
            } else {
                hideControllerDelay();
            }
            return;
        }
        mIsDragging = isDrag;
        if (isDrag) {
            cancelDelayHide();
        } else {
            mSurfaceView.seekTo(newPos);
            updateProgress();
            mHandler.sendEmptyMessage(MSG_PROGRESS_CHANGED);
            hideControllerDelay();
        }
    }

    @Override
    public void back() {
        onBackPressed();
    }

    @Override
    public void setPlayMode(int mode) {
        cancelDelayHide();
        mSurfaceView.setPlayMode(mode);

        switch (mode) {
        case VideoRender.RENDER_MODE_2D:
        case VideoRender.RENDER_MODE_3D2D:
            setActivityBrightness(mCurrent2DBrightness);
            break;
        case VideoRender.RENDER_MODE_2D3D:
        case VideoRender.RENDER_MODE_3D:
            compensateBrightness();
            break;
        default:
            break;
        }

        hideControllerDelay();
    }

    @Override
    public void scale() {
        cancelDelayHide();
        mSurfaceView.mRenderer.setKeepAR(!mSurfaceView.mRenderer.isKeepAR());
        hideControllerDelay();
    }

    @Override
    public boolean isKeepAR() {
        return mSurfaceView.mRenderer.isKeepAR();
    }

    @Override
    public void previous() {
        // TODO Auto-generated method stub
    }

    @Override
    public void next() {
        // TODO Auto-generated method stub
    }

    @Override
    public int getPlayMode() {
        return mSurfaceView.getPlayMode();
    }

    @Override
    public void startMenu(int position, int which) {
        switch (position) {
        case 0: // Left-Right
            // L->R
            // which = 0, format is LR. which = 1, format is RL
            mSurfaceView.setLRVideoFormat(which);
            break;
        case 1: // Eye tracer
            break;
        case 2: // 3D type
            break;
        }
    }

    @Override
    public void showVolumeWindow() {
        cancelDelayHide();
        mControllerView.showVolumeWindow(mSurfaceView, mScreenHeight);
        hideControllerDelay();
    }

    @Override
    public void hideVolumeWindow() {
        cancelDelayHide();
        mControllerView.hideVolumeWindow();
        hideControllerDelay();
    }

    @Override
    public void showDepthWindow() {
        cancelDelayHide();
        mControllerView.showDepthWindow(mSurfaceView, mScreenHeight);
        hideControllerDelay();
    }

    @Override
    public void hideDepthWindow() {
        cancelDelayHide();
        mControllerView.hideDepthWindow();
        hideControllerDelay();
    }

    @Override
    public void setPlayDepth(float depth) {
        mSurfaceView.setPlayDepth(depth);
    }

    @Override
    public float getPlayDepth() {
        return mSurfaceView.getPlayDepth();
    }

    @Override
    public float getMaxPlayDepth() {
        return mSurfaceView.getMaxPlayDepth();
    }

    @Override
    public void OnChanged(int width, int height) {
        mScreenWidth = width;
        mScreenHeight = height;
        if (mHandler != null && mIsControllerShow) {
            mHandler.sendEmptyMessage(MSG_UPDATE_VIEW);
        }
    }
}
