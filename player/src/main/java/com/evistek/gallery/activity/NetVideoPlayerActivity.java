package com.evistek.gallery.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.evistek.gallery.database.MediaDataBase;
import com.evistek.gallery.model.PlayRecord;
import com.evistek.gallery.model.PlayRecordModel;
import com.evistek.gallery.model.Video;
import com.evistek.gallery.net.NetWorkService;
import com.evistek.gallery.net.callback.PlayRecordCallback;
import com.evistek.gallery.net.callback.UserCallBack;
import com.evistek.gallery.render.VideoRender;
import com.evistek.gallery.user.User;
import com.evistek.gallery.utils.EvisUtil;
import com.evistek.gallery.utils.Utils;
import com.evistek.gallery.view.ControllerView;

import org.rajawali3d.vr.activity.GvrVideoActivity;

import java.util.ArrayList;

public class NetVideoPlayerActivity extends VideoPlayerActivityBase {
    private static final String TAG = "NetVideoPlayerActivity";

    public static final String VIDEO_URL = "video_url";
    public static final String VIDEO = "video";

    private String mVideoUrl;
    private PlayRecord mPlayRecord;
    private User mUser;
    private Video mVideo;
    private ArrayList<Video> mVideoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mUser = E3DApplication.getInstance().getUser();
        receiveIntent();

        if (mControllerView == null) {
            mControllerView = new ControllerView(this, true, false);
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
        if (mPlayRecord != null) {
            updatePlayRecord(mPlayRecord);
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void receiveIntent() {
        mVideoUrl = getIntent().getStringExtra(VIDEO_URL);
        mVideo = (Video)getIntent().getSerializableExtra(VIDEO);

        // only this video
        mVideoList = new ArrayList<>();
        mVideoList.add(mVideo);

        initPlayRecord(mVideo);
    }

    @Override
    protected void launchMediaPlayer() {
        super.launchMediaPlayer();

        int type = getSourceType();
        if (mSurfaceView.setDataSource(this, mVideoUrl)) {
            if (type == MediaDataBase.SRC_TYPE_2D) {
                mControllerView.setRadioChecked(VideoRender.RENDER_MODE_2D);
            } else {
                mControllerView.setRadioChecked(VideoRender.RENDER_MODE_3D);
            }
            mVideoDuration = mSurfaceView.getDuration();
            setContentView(mSurfaceView);
        }
        mControllerView.setNameView(getFileName(mVideoUrl));
        if (mPlayRecord != null) {
            addPlayRecord(mPlayRecord);
        }
    }

    @Override
    protected int getSourceType() {
        return MediaDataBase.SRC_TYPE_3D;
    }

    @Override
    protected void resetPlayPosition() {
        Utils.saveValue(mVideoUrl, 0L);
    }

    @Override
    protected void saveCurrentPosition() {
        if (mSurfaceView == null) {
            return;
        }

        long position = mSurfaceView.getCurrentPosition();
        if (position > 0) {
            Utils.saveValue(mVideoUrl, position);
        }
    }

    @Override
    protected void resumePlayPosition() {
        long position = Utils.getValue(mVideoUrl, 0L);
        if (position > 0) {
            mSurfaceView.seekTo(Long.valueOf(position).intValue());
        }
    }

    @Override
    public void previous() {

    }

    @Override
    public void next() {

    }

    private String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    private void initPlayRecord(Video video) {
        if (mUser.isLogin) {
            mPlayRecord = new PlayRecord();
            mPlayRecord.setUserId(mUser.id);
            mPlayRecord.setVideoId(video.getId());
            mPlayRecord.setVideoName(video.getName());
            mPlayRecord.setClient(getPackageName());
            try {
                mPlayRecord.setClientVersion(
                        getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).versionName);
            } catch (PackageManager.NameNotFoundException e) {
                mPlayRecord.setClientVersion("");
                e.printStackTrace();
            }
        }
    }

    private void addPlayRecord(PlayRecord playRecord) {
        if (Utils.isNetworkAvailable()) {
            NetWorkService.addPlayRecord(playRecord, new PlayRecordCallback() {
                @Override
                public void onResult(int code, PlayRecordModel msg) {
                    Log.e(TAG, "addPlayRecord code: " + code + " msg: " + msg);
                }
            });
        }
    }

    private void updatePlayRecord(PlayRecord playRecord) {
        if (Utils.isNetworkAvailable()) {
            NetWorkService.updatePlayRecordDuration(playRecord, new UserCallBack() {
                @Override
                public void onResult(int code, String msg) {
                    Log.e(TAG, "updatePlayRecord code: " + code + " msg: " + msg);
                }
            });
        }
    }

    public void startVrActivity() {
        Intent intent = new Intent(this, GvrVideoActivity.class);
        intent.putExtra(GvrVideoActivity.INTENT_LOCAL_PATH, mVideoUrl);//local physical path
        intent.putExtra(GvrVideoActivity.INTENT_NET_VIDEO, mVideo);
        intent.putExtra(GvrVideoActivity.INTENT_PLAY_PATTERN, GvrVideoActivity.PATTERN_TYPE_1);
        intent.putExtra(GvrVideoActivity.INTENT_VIDEO_LIST, mVideoList);
        startActivity(intent);
        finish();
    }
}
