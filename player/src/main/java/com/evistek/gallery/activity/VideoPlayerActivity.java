package com.evistek.gallery.activity;

import android.content.Intent;
import android.os.Bundle;

import com.evistek.gallery.database.MediaDataBase;
import com.evistek.gallery.model.LocalVideo;
import com.evistek.gallery.render.VideoRender;
import com.evistek.gallery.utils.EvisUtil;
import com.evistek.gallery.utils.Utils;
import com.evistek.gallery.view.ControllerView;

import org.rajawali3d.vr.activity.GvrVideoActivity;

import java.util.ArrayList;

public class VideoPlayerActivity extends VideoPlayerActivityBase
{
    private static final String TAG = "VideoPlayerActivity";
    public static final String VIDEO_ID = "video_id";
    public static final String VIDEO_LIST = "video_list";
    public static final String VIDEO_PATH = "video_path";
    public static final String VIDEO_POSITION = "position";

    public static final String INTENT_LOCAL_VIDEO = "local_video";
    public static final String INTENT_LOCAL_VIDEO_LIST = "local_video_list";

    private static int mIndex = -1;
    private static long mVideoId;
    private static ArrayList<String> mVideoList;
    private String mPath;
    private LocalVideo mLocalVideo;
    private ArrayList<LocalVideo> mLocalVideoList;

    private boolean mInManualOpen;
    private MediaDataBase mDataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDataBase = MediaDataBase.getInstance(this);
        receiveIntent();

        if (mControllerView == null) {
            mControllerView = new ControllerView(this, true);
            mControllerView.setControl(this);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (!mIsPrepared) {
            return;
        }
        if (mIsControllerShow) {
            cancelDelayHide();
            hideController();
        }
        if (mSurfaceView != null) {
            if (!mIsCompleted) {
                mSaveTag = true;
                saveCurrentPosition();
            }
            if (mSurfaceView.isPlaying()) {
                mSurfaceView.stopPlayback();
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        if (mSurfaceView != null) {
            if (!mSaveTag && !mIsCompleted) {
                mSaveTag = false;
                saveCurrentPosition();
            }
            mSurfaceView.onPause();
            EvisUtil.enablePanel3D(false);
            mControllerView.setPlayIcon(PLAY);
        }
        super.onPause();
    }

    @Override
    protected void receiveIntent() {
        mVideoList = getIntent().getStringArrayListExtra(VIDEO_LIST);

        mLocalVideo = (LocalVideo) getIntent().getSerializableExtra(INTENT_LOCAL_VIDEO);
        mLocalVideoList = (ArrayList<LocalVideo>) getIntent().getSerializableExtra(INTENT_LOCAL_VIDEO_LIST);
        if (mLocalVideo != null) {
            mVideoId = mLocalVideo.id;

            if (mVideoId == -1L) {
                mPath = getIntent().getStringExtra(VIDEO_PATH);
                mVideoId = Utils.getMediaId(mPath, true);
                mInManualOpen = true;
            } else {
                mIndex = mVideoList.indexOf(String.valueOf(mVideoId));
                mInManualOpen = false;
            }
        }
    }

    @Override
    protected void launchMediaPlayer() {
        super.launchMediaPlayer();

        if (mPath != null && mIndex == -1) {
            if (mSurfaceView.setDataSource(this, mPath)) {
                int type = 0;
                if (mDataBase.isExisting(mVideoId)) {
                    type = mDataBase.getSrcType(mVideoId);
                } else {
                    type = MediaDataBase.SRC_TYPE_2D;
                    mVideoDuration = Utils.getVideoDuration(mPath);
                }

                if (type == MediaDataBase.SRC_TYPE_3D)
                    mControllerView.setRadioChecked(VideoRender.RENDER_MODE_2D);
                else
                    mControllerView.setRadioChecked(VideoRender.RENDER_MODE_3D);

                setContentView(mSurfaceView);
            }
            mControllerView.setNameView(mPath.substring(mPath.lastIndexOf('/') + 1));
        } else {

            String path = Utils.getMediaPath(mVideoId, true);
            String name = Utils.getMediaName(mVideoId, true);
            mVideoDuration = Utils.getVideoDuration(path);

            int type = mDataBase.getSrcType(mVideoId);
            if (mSurfaceView.setDataSource(this, path)) {
                if (type == MediaDataBase.SRC_TYPE_2D)
                    mControllerView.setRadioChecked(VideoRender.RENDER_MODE_2D);
                else
                    mControllerView.setRadioChecked(VideoRender.RENDER_MODE_3D);
                setContentView(mSurfaceView);
            }
            mControllerView.setNameView(name);
        }
        Utils.saveValue(Utils.SHARED_VIDEO_LAST_PLAYED, mVideoId);
    }

    @Override
    protected int getSourceType() {
        return mDataBase.getSrcType(mVideoId);
    }

    @Override
    public void previous() {
        if (mInManualOpen) {
            return;
        }

        cancelDelayHide();
        if (mIsPrepared) {
            if (!mIsCompleted) {
                saveCurrentPosition();
            }
            --mIndex;
            if (mIndex < 0) {
                mIndex = 0;
            }
            if (mVideoList != null) {
               mVideoId = Long.valueOf(mVideoList.get(mIndex));
            }
            mSurfaceView.stopPlayback();
            launchMediaPlayer();
        }
        hideControllerDelay();
    }

    @Override
    public void next() {
        if (mInManualOpen) {
            return;
        }

        cancelDelayHide();
        if (mIsPrepared) {
            if (!mIsCompleted) {
                saveCurrentPosition();
            }
            ++mIndex;
            int size = mVideoList.size();
            if (mIndex >= size) {
                mIndex = size - 1;
            }
            if (mVideoList != null) {
                mVideoId = Long.valueOf(mVideoList.get(mIndex));
             }
            mSurfaceView.stopPlayback();
            launchMediaPlayer();
        }
        hideControllerDelay();
    }

    @Override
    protected void saveCurrentPosition() {
        if (mSurfaceView == null) {
            return;
        }

        int position = mSurfaceView.getCurrentPosition();
        if (position > 0) {
            mDataBase.updatePosition(mVideoId, Integer.valueOf(position).longValue());
        }
    }

    @Override
    protected void resetPlayPosition() {
        mDataBase.updatePosition(mVideoId, 0);
    }

    @Override
    protected void resumePlayPosition() {
        if (mDataBase.isExisting(mVideoId)) {
            long position = mDataBase.getPosition(mVideoId);
            if (position > 0) {
                mSurfaceView.seekTo(Long.valueOf(position).intValue());
            }
        }
    }

    public void startVrActivity() {
        Intent intent = new Intent(this, GvrVideoActivity.class);
        mLocalVideo = mLocalVideoList.get(mIndex);
        intent.putExtra(GvrVideoActivity.INTENT_LOCAL_VIDEO, mLocalVideo);
        intent.putExtra(GvrVideoActivity.INTENT_PLAY_PATTERN, GvrVideoActivity.PATTERN_TYPE_1);
        intent.putExtra(GvrVideoActivity.INTENT_VIDEO_LIST, mLocalVideoList);
        startActivity(intent);
        finish();
    }
}
