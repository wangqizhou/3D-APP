package com.evistek.gallery.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.evistek.gallery.R;
import com.evistek.gallery.model.Favorite;
import com.evistek.gallery.model.FavoriteModel;
import com.evistek.gallery.model.PlayRecord;
import com.evistek.gallery.model.PlayRecordModel;
import com.evistek.gallery.model.Task;
import com.evistek.gallery.model.Video;
import com.evistek.gallery.net.BitmapLoadManager;
import com.evistek.gallery.net.NetWorkService;
import com.evistek.gallery.net.callback.FavoriteCallback;
import com.evistek.gallery.net.callback.UserCallBack;
import com.evistek.gallery.net.json.JsonRespFavorite;
import com.evistek.gallery.service.DownloadService;
import com.evistek.gallery.user.User;
import com.evistek.gallery.utils.CompatibilityChecker;
import com.evistek.gallery.utils.Utils;

import org.rajawali3d.vr.activity.GvrVideoActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StereoVideoDetailActivity extends Activity {
    private static final String TAG = "StereoVideoDetail";
    public static final String INTENT_VIDEO = "video";
    public static final String INTENT_PATTERN = "pattern";
    public static final String INTENT_REMOVED_ID = "removedId";
    public static final String INTENT_VIDEO_LIST = "videoList";

    private static final int MSG_ADD_FAVORITE = 0;
    private static final int MSG_DELETE_FAVORITE = 1;

    private Context mContext;
    private Video mVideo;
    private int mPattern;
    private String mUrl;
    private User mUser;
    private FavoriteModel mFavorite;
    private Favorite favorite;
    private PlayRecordModel mPlayRecord;
    private int mFavoriteIDRemoved;
    private ArrayList<Video> mVideoList = new ArrayList<>();
    private boolean mIsDownLoad;
    private DownloadService.OnSuccessListener mDownLoadListener;
    private String mVideoUrl;
    private boolean mIsDownLoading;
    private DownloadService mDownloadService;
    private List<Task> mTasks = new ArrayList<Task>();

    @BindView(R.id.stereo_video_detail_image)
    ImageView mVideoImage;
    @BindView(R.id.stereo_video_detail_play)
    ImageView mDownload;
    @BindView(R.id.stereo_video_detail_name)
    TextView mVideoName;
    @BindView(R.id.stereo_video_detail_favorite)
    ImageView mFavoriteView;
    @BindView(R.id.stereo_video_detail_actors)
    TextView mActors;
    @BindView(R.id.stereo_video_detail_time)
    TextView mTime;
    @BindView(R.id.stereo_video_detail_location)
    TextView mLocation;
    @BindView(R.id.stereo_video_detail_play_count)
    TextView mPlayCount;
    @BindView(R.id.stereo_video_detail_intro)
    TextView mIntro;
    @BindView(R.id.stereo_video_detail_back)
    ImageView mBack;
    @BindView(R.id.stereo_video_detail_title_layout)
    LinearLayout mTitleLayout;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_FAVORITE:
                    mFavorite = (FavoriteModel) msg.obj;
                    mUser.favorites.add(mFavorite);
                    mFavoriteView.setImageResource(R.drawable.favorite_enabled);
                    break;
                case MSG_DELETE_FAVORITE:
                    mFavoriteIDRemoved = mFavorite.getVideo().getId();
                    mUser.favorites.remove(mFavorite);
                    mFavorite = null;
                    mFavoriteView.setImageResource(R.drawable.favorite_disabled);
                    break;
            }

            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stereo_video_detail);
        ButterKnife.bind(this);

        mContext = this;
        mFavoriteIDRemoved = -1;
        mUser = E3DApplication.getInstance().getUser();
        receiveIntent();
        initView();
    }

    @Override
    protected void onResume() {

        mUser = E3DApplication.getInstance().getUser();
        favorite = new Favorite();
        mFavorite = getFavorite(mVideo);
        if (mUser.isLogin && mFavorite != null) {
            mFavoriteView.setImageResource(R.drawable.favorite_enabled);
        } else {
            mFavoriteView.setImageResource(R.drawable.favorite_disabled);
        }

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(INTENT_REMOVED_ID, mFavoriteIDRemoved);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private void initView() {
        mVideoName.setText(mVideo.getName());
        mActors.setText(mVideo.getActors());
        mTime.setText(new SimpleDateFormat("yyyy-MM-dd").format(mVideo.getReleaseTime()));
        mLocation.setText(mVideo.getLocation());
        mPlayCount.setText(String.valueOf(mVideo.getDownloadCount()) + getString(R.string.detail_play_count_suffix));
        mIntro.setText(mVideo.getIntroduction());

        mDownloadService = E3DApplication.getInstance().getDownloadService();
        mTasks = mDownloadService.getTasks();
        for (Task task : mTasks) {
            if (task.getUrl().equals(mVideo.getUrl())) {
                mIsDownLoading = true;
                break;
            }
        }
        judgeIsDownLoad();

        if(mIsDownLoad){
            mDownload.setImageResource(R.drawable.ic_play_overlay);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            int marginTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            layoutParams.setMargins(0, marginTop, 0, 0);
            mTitleLayout.setLayoutParams(layoutParams);
        }

        mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsDownLoading) {
                    Toast.makeText(getApplicationContext(), R.string.download_joined, Toast.LENGTH_SHORT).show();
                } else if (mIsDownLoad) {
                    startPlayerActivity();
                } else {
                    startDownLoad();
                }
            }
        });

        mFavoriteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable()) {
                    if (mUser.isLogin) {
                        if (mFavorite != null) {
                            favorite.setUserId(mFavorite.getUserId());
                            favorite.setTime(mFavorite.getTime());
                            favorite.setId(mFavorite.getId());
                            favorite.setVideoId(mFavorite.getVideo().getId());
                            favorite.setVideoName(mFavorite.getVideo().getName());
                            deleteFavorite(favorite);
                        } else {
                            addFavorite(mVideo);
                        }
                    } else {
                        showLoginAlertDialog();
                    }
                } else {
                        showAlertDialog();
                    }
                }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mDownLoadListener = new DownloadService.OnSuccessListener() {

            @Override
            public void onSuccess(String filePath) {
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                String targetName = mVideo.getUrl().substring(mVideo.getUrl().lastIndexOf("/") + 1);
                if (targetName.equals(fileName)) {
                    mIsDownLoad = true;
                    mIsDownLoading = false;
                    mVideoUrl = filePath;
                    mDownload.setImageResource(R.drawable.ic_play_overlay);
                }
            }
        };
        E3DApplication.getInstance().getDownloadService().setOnSuccessListener(mDownLoadListener);

        BitmapLoadManager.display(mVideoImage, mVideo.getPreview1Url());
    }

    private void receiveIntent() {
        mVideo = (Video) getIntent().getSerializableExtra(INTENT_VIDEO);
        mPattern = getIntent().getIntExtra(INTENT_PATTERN, GvrVideoActivity.PATTERN_TYPE_1);
        mUrl = mVideo.getUrl();
        mVideoList = (ArrayList<Video>) getIntent().getSerializableExtra(INTENT_VIDEO_LIST);
    }

    private void startPlayerActivity() {
        if (CompatibilityChecker.check(this)) {
            Intent intent;
            if (mPattern != 0) {
                intent = new Intent(this, GvrVideoActivity.class);
                intent.putExtra(GvrVideoActivity.INTENT_LOCAL_PATH, mVideoUrl);//local physical path
                intent.putExtra(GvrVideoActivity.INTENT_NET_VIDEO, mVideo);
                intent.putExtra(GvrVideoActivity.INTENT_PLAY_PATTERN, GvrVideoActivity.PATTERN_TYPE_2);
                intent.putExtra(GvrVideoActivity.INTENT_VIDEO_LIST, mVideoList);
            } else {
                intent = new Intent(E3DApplication.getInstance(), NetVideoPlayerActivity.class);
                intent.putExtra(NetVideoPlayerActivity.VIDEO_URL, mVideoUrl);
                intent.putExtra(NetVideoPlayerActivity.VIDEO, mVideo);
            }
            startActivity(intent);
        } else {
            CompatibilityChecker.notifyDialog(this);
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.detail_alert_dialog_content)
                .setTitle(R.string.network_unavailable);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLoginAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.need_login)
                .setTitle(R.string.not_login);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addFavorite(Video video) {
        final Favorite favorite = new Favorite();
        favorite.setUserId(mUser.id);
        favorite.setVideoId(video.getId());
        favorite.setVideoName(video.getName());
        final FavoriteModel favoriteModel = new FavoriteModel();
        favoriteModel.setVideo(video);
        favoriteModel.setUserId(mUser.id);
        NetWorkService.addFavorite(favorite, new FavoriteCallback() {
            @Override
            public void onResult(int code, FavoriteModel jsonResp) {
                favoriteModel.setTime(jsonResp.getTime());
                favoriteModel.setId(jsonResp.getId());
                if (code == 200) {
                    sendMessage(MSG_ADD_FAVORITE, favoriteModel);
                } else {
                    Log.e(TAG, "add favorite fail");
                }
            }
        });
    }

    private void deleteFavorite(final Favorite favorite) {
        final FavoriteModel favoriteModel = new FavoriteModel();
        favoriteModel.setUserId(mUser.id);
        favoriteModel.setVideo(mVideo);

        favorite.setUserId(mUser.id);
        favorite.setVideoId(mVideo.getId());
        favorite.setVideoName(mVideo.getName());

        NetWorkService.deleteFavorite(favorite.getId(), new UserCallBack() {
            @Override
            public void onResult(int code, String jsonResp) {
                if (code == 200) {
                    sendMessage(MSG_DELETE_FAVORITE, favorite);
                } else {
                    Log.e(TAG, "fail to delete favorite, code: " + code);
                }
            }
        });
    }

    private void judgeIsDownLoad() {
        String rightFileName = mVideo.getUrl().substring(mVideo.getUrl().lastIndexOf("/") + 1);
        String dirName = E3DApplication.getInstance().getDownloadService().getDownloadDir();
        mVideoUrl = dirName + rightFileName;
        if (new File(mVideoUrl).exists()) {
            mIsDownLoading = false;
            mIsDownLoad = true;
        }
    }

    private void startDownLoad() {
        Task task = new Task();
        if (mVideo.getPreview1Url() != null){
            task.setCoverUrl(mVideo.getPreview1Url());
        } else {
            task.setCoverUrl(mVideo.getPreview1Url());
        }
        task.setId(mVideo.getId());
        task.setName(mVideo.getName());
        task.setSize((int) mVideo.getSize());
        task.setProgress(0);
        task.setStatus(Task.STATUS_INIT);
        task.setUrl(mVideo.getUrl());
        task.setPhysicalPath(mVideoUrl);
        task.setCategoryId(mVideo.getCategoryId());
        task.setCategoryName(mVideo.getCategoryName());
        E3DApplication.getInstance().getDownloadService().addTask(task);
        mIsDownLoading = true;
    }

    private FavoriteModel getFavorite(Video video) {
        List<FavoriteModel> favorites = mUser.favorites;
        for(FavoriteModel f: favorites) {
            if (f.getVideo().getId() == video.getId()) {
                return f;
            }
        }

        return null;
    }

    private void sendMessage(int what, Object obj) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        message.obj = obj;
        mHandler.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        E3DApplication.getInstance().getDownloadService().removeOnSuccessListener(mDownLoadListener);
    }
}
