package com.evistek.gallery.view;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.opengl.GLSurfaceView;

import com.evistek.gallery.render.VideoRender;

public class VideoSurfaceView extends GLSurfaceView {

    public VideoRender mRenderer;
    private MediaPlayer mMediaPlayer;
    private int mDuration;
    private boolean mIsPrepared;

    private OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private OnErrorListener mOnErrorListener;

    public VideoSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(mPreparedListener);
        mIsPrepared = false;
        mDuration = -1;
        mMediaPlayer.setOnCompletionListener(mCompletionListener);
        mMediaPlayer.setOnErrorListener(mErrorListener);
    }

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
        }
    };

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mIsPrepared = true;
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                    return true;
                }
            }
            /*
             * Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog if
             * we're attached to a window. When we're going away and no longer
             * have a window, don't bother showing the user an error.
             */
            if (getWindowToken() != null) {
                // show
            }
            return true;
        }
    };

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    public void setOnCompletionListener(OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    public void setOnErrorListener(OnErrorListener l) {
        mOnErrorListener = l;
    }

    public boolean setDataSource(Context context, String path) {
        String height;
        String width;
        try {
            mMediaPlayer.setDataSource(path);
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(path);
            height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        } catch (Exception e) {
            return false;
        }
        if (mRenderer == null) {
            mRenderer = new VideoRender(context);
            mRenderer.setMediaPlayer(mMediaPlayer);
            setRenderer(mRenderer);
        }
        mRenderer.setVideoSize(Integer.parseInt(width), Integer.parseInt(height));
        return true;
    }

    public void start() {
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.start();
        }
    }

    public int getDuration() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public int getCurrentPosition() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        return -1;
    }

    public void seekTo(int msec) {
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.seekTo(msec);
        }
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    public void pause() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void setPlayMode(int mode) {
        if (mRenderer != null)
            mRenderer.setRenderMode(mode);
    }

    public int getPlayMode() {
        if (mRenderer != null)
            return mRenderer.getRenderMode();
        else
		return 0;
    }

    public void setLRVideoFormat(int leftRight) {
        if (mRenderer != null)
            mRenderer.setFramePackingFormat(leftRight);
    }

    public int getLRVideoFormat() {
        if (mRenderer != null)
            return mRenderer.getFramePackingFormat();
        else
            return 0;
    }

    public void setPlayDepth(float depth) {
        if (mRenderer != null)
            mRenderer.setDepth(depth);
    }

    public float getPlayDepth() {
        if (mRenderer != null)
            return mRenderer.getDepth();
        else
            return 0;
    }

    public float getMaxPlayDepth() {
        if (mRenderer != null)
            return mRenderer.getMaxDepth();
        else
            return 0;
    }

    @Override
    public void onPause() {
        pause();
        //super.onPause();
    }

    @Override
    public void onResume() {
        queueEvent(new Runnable() {
            public void run() {
                if (mMediaPlayer != null && mRenderer != null) {
                    mRenderer.setMediaPlayer(mMediaPlayer);
                }
            }
        });

        if (mRenderer != null && mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mRenderer.requestRender();
        }

        //super.onResume();
    }
}