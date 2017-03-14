package com.evistek.gallery.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.adapter.DownloadAdapter;
import com.evistek.gallery.model.Task;
import com.evistek.gallery.model.Video;
import com.evistek.gallery.service.DownloadService;
import com.evistek.gallery.service.DownloadService.OnCompleteListener;
import com.evistek.gallery.service.DownloadService.OnFailureListener;
import com.evistek.gallery.service.DownloadService.OnProgressListener;
import com.evistek.gallery.service.DownloadService.OnSuccessListener;
import com.evistek.gallery.utils.CompatibilityChecker;

import org.rajawali3d.vr.activity.GvrVideoActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadActivity extends Activity
        implements OnProgressListener, OnSuccessListener, OnFailureListener, View.OnClickListener, OnCompleteListener {

    public static final String DOWNLOAD_URL = "download_url";
    private static final String TAG = "DownloadActivity";
    private Context mContext;

    @BindView(R.id.download_listview)
    ListView mListView;
    @BindView(R.id.download_emptyView)
    TextView mTextView;
    @BindView(R.id.download_back)
    ImageView mBackView;
    @BindView(R.id.download_cancelAll)
    Button mCancelAllButton;

    private DownloadAdapter mAdapter;
    private Video mVideo;
    private List<Task> mTasks = new ArrayList<Task>();
    private List<Video> mVideoList = new ArrayList<>();

    private DownloadService mDownloadService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);

        mContext = this;
        mDownloadService = E3DApplication.getInstance().getDownloadService();
        mDownloadService.setOnProgressListener(DownloadActivity.this);
        mDownloadService.setOnSuccessListener(DownloadActivity.this);
        mDownloadService.setOnCompleteListener(DownloadActivity.this);
        mDownloadService.setOnFailureListener(DownloadActivity.this);
        mTasks = mDownloadService.getTasks();

        initView();
    }

    private void initView() {
        mTextView.setText(R.string.download_empty);
        mListView.setEmptyView(mTextView);

        mAdapter = new DownloadAdapter(mContext, mDownloadService, mTasks);
        mListView.setAdapter(mAdapter);

        mBackView.setOnClickListener(this);
        mCancelAllButton.setOnClickListener(this);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mDownloadService.getTaskStatus(position) == Task.STATUS_START) {
                    mDownloadService.cancelCurrentTask();
                    mAdapter.notifyDataSetChanged();
                } else if (mDownloadService.getTaskStatus(position) == Task.STATUS_PAUSE) {
                    mDownloadService.resumeTask();
                    mTasks = mDownloadService.getTasks();
                    mAdapter.notifyDataSetChanged();
                } else if (mTasks.get(position).getStatus() == Task.STATUS_COMPLETE) {
                    startVideoPlayerActivity(position);
                }
            }
        });
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new Builder(DownloadActivity.this);
                builder.setTitle(getApplicationContext().getResources().getString(R.string.downloadDeletetip));
                builder.setMessage(getApplicationContext().getResources().getString(R.string.delete_ensure));
                builder.setPositiveButton(getApplicationContext().getResources().getString(R.string.ok),
                        new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDownloadService.removeTask(position);
                        mTasks = mDownloadService.getTasks();
                        mAdapter = new DownloadAdapter(mContext, mDownloadService, mTasks);
                        mListView.setAdapter(mAdapter);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(getApplicationContext().getResources().getString(R.string.cancel),
                        new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return true;
            }
        });
    }

    public void startVideoPlayerActivity(int position) {
        if (CompatibilityChecker.check(this)) {
            mVideo = new Video();
            mVideo.setPreview1Url(mTasks.get(position).getCoverUrl());
            mVideo.setId((int) mTasks.get(position).getId());
            mVideo.setName(mTasks.get(position).getName());
            mVideo.setCategoryId(mTasks.get(position).getCategoryId());
            mVideo.setUrl(mTasks.get(position).getUrl());
            mVideo.setCategoryName(mTasks.get(position).getCategoryName());
            mVideoList.add(mVideo);

            Intent intent;
            int index = mVideo.getCategoryName().indexOf("VR");
            if (index != -1) {
                intent = new Intent(this, GvrVideoActivity.class);
                intent.putExtra(GvrVideoActivity.INTENT_LOCAL_PATH, mTasks.get(position).getPhysicalPath());
                intent.putExtra(GvrVideoActivity.INTENT_NET_VIDEO, mVideo);
                intent.putExtra(GvrVideoActivity.INTENT_PLAY_PATTERN, GvrVideoActivity.PATTERN_TYPE_2);
                intent.putExtra(GvrVideoActivity.INTENT_VIDEO_LIST, (ArrayList<Video>)mVideoList);
            } else {
                intent = new Intent(E3DApplication.getInstance(), NetVideoPlayerActivity.class);
                intent.putExtra(NetVideoPlayerActivity.VIDEO_URL, mTasks.get(position).getPhysicalPath());
                intent.putExtra(NetVideoPlayerActivity.VIDEO, mVideo);
            }
            startActivity(intent);
        } else {
            CompatibilityChecker.notifyDialog(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSuccess(String filePath) {
    }

    @Override
    public void onProgress(int index, int progress) {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFailure(int code, String msg) {
    }

    @Override
    public void onComplete() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.download_back:
            super.onBackPressed();
            break;
        case R.id.download_cancelAll:
            mDownloadService.cancelAll();
            mAdapter.notifyDataSetChanged();
            break;
        }
    }

}
