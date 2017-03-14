package com.evistek.gallery.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.fragment.HomeFragment;
import com.evistek.gallery.model.PlayRecord;
import com.evistek.gallery.model.PlayRecordModel;
import com.evistek.gallery.model.Video;
import com.evistek.gallery.net.BitmapLoadManager;
import com.evistek.gallery.net.Config;
import com.evistek.gallery.net.NetWorkService;
import com.evistek.gallery.net.callback.PlayRecordCallback;
import com.evistek.gallery.net.callback.PlayRecordListCallback;
import com.evistek.gallery.net.callback.UserCallBack;
import com.evistek.gallery.net.callback.VideoCallback;
import com.evistek.gallery.net.json.JsonRespPlayRecord;
import com.evistek.gallery.net.json.JsonRespVideo;
import com.evistek.gallery.user.User;
import com.evistek.gallery.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryActivity extends Activity {
    private static final String TAG = "HistoryActivity";
    private static final int MSG_GET_PLAY_RECORD = 0;
    private static final int MSG_GET_VIDEO = 1;
    private static Boolean mDeleteMode = false;

    private Context mContext;
    private ListAdapter mAdapter;
    private List<Integer> mPositionList = new ArrayList<>();
    private List<PlayRecordModel> mList = new ArrayList<>();
    private List<PlayRecordModel> mSelectList = new ArrayList<>();
    private List<PlayRecord> mPlayRecordList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private LinearLayoutManager mLinearLayoutManager;
    private User mUser;

    @BindView(R.id.v2_user_history_backbt)
    ImageView mBack;
    @BindView(R.id.v2_user_history_recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.v2_user_history_listitem_delete)
    TextView mDeleteBt;
    @BindView(R.id.v2_user_history_listitem_selectall)
    TextView mSelectBt;
    @BindView(R.id.v2_user_history_listitem_edit)
    TextView mEditBt;
    @BindView(R.id.v2_user_history_empty_view)
    RelativeLayout mEmptyLayout;
    @BindView(R.id.v2_empty_icon)
    ImageView mEmptyViewIcon;
    @BindView(R.id.v2_empty_tv)
    TextView mEmptyViewMsg;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_GET_PLAY_RECORD:
                    mAdapter.notifyDataSetChanged();
                    break;
                case MSG_GET_VIDEO:
                    Video video = (Video)msg.obj;
                    startDetailActivity(video);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        mContext = this;
        mLayoutInflater = this.getLayoutInflater();
        mUser = E3DApplication.getInstance().getUser();
        initializeView();
        getData();
    }

    @Override
    public void onBackPressed() {
        if (mDeleteMode) {
            mSelectList.clear();
            mPositionList.clear();
            mDeleteBt.setVisibility(View.GONE);
            mSelectBt.setVisibility(View.GONE);
            mEditBt.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
        mDeleteMode = false;
    }

    private void initializeView() {
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mAdapter = new ListAdapter();
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mAdapter.getItemCount() == 0) {
                    mEmptyLayout.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                } else {
                    mEmptyLayout.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mEmptyViewIcon.setBackgroundResource(R.drawable.v2_play_record);
        mEmptyViewMsg.setText(getString(R.string.empty_playrecord_no));

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDeleteMode) {
                    mSelectList.clear();
                    mPositionList.clear();
                    mDeleteBt.setVisibility(View.GONE);
                    mSelectBt.setVisibility(View.GONE);
                    mEditBt.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                } else {
                    finish();
                }
                mDeleteMode = false;
            }
        });
        mEditBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeleteMode = true;
                mEditBt.setVisibility(View.GONE);
                mDeleteBt.setVisibility(View.VISIBLE);
                mSelectBt.setVisibility(View.VISIBLE);
                mSelectBt.setText(R.string.v2_user_title_select);
                mSelectList.clear();
                mPositionList.clear();
                mAdapter.notifyDataSetChanged();
            }
        });
        mDeleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectList.size() == 0) {
                    new  AlertDialog.Builder(HistoryActivity.this)
                            .setTitle(R.string.downloadDeletetip)
                            .setMessage(R.string.v2_choose_no)
                            .setPositiveButton(R.string.ok,null)
                            .show();
                } else {
                    for (int i=0; i<mSelectList.size() ; i++){
                        PlayRecord playRecord = new PlayRecord();
                        playRecord.setId(mSelectList.get(i).getId());
                        playRecord.setVideoId(mSelectList.get(i).getVideo().getId());
                        playRecord.setVideoName(mSelectList.get(i).getVideo().getName());
                        playRecord.setUserId(mSelectList.get(i).getUserId());
                        playRecord.setStartTime(mSelectList.get(i).getStartTime());
                        playRecord.setEndTime(mSelectList.get(i).getEndTime());
                        playRecord.setClient(mSelectList.get(i).getClient());
                        playRecord.setClientVersion(mSelectList.get(i).getClientVersion());
                        playRecord.setDuration(mSelectList.get(i).getDuration());
                        mPlayRecordList.add(playRecord);
                    }
                    NetWorkService.deletePlayRecords(mPlayRecordList, new UserCallBack() {
                        @Override
                        public void onResult(int code, String jsonResp) {
                            if (code == 200) {
                                for (int i = 0; i < mSelectList.size(); i++) {
                                    mList.remove(mSelectList.get(i));
                                }
                                mSelectList.clear();
                                mPositionList.clear();
                                mAdapter.notifyDataSetChanged();
                                mEditBt.setVisibility(View.VISIBLE);
                                mDeleteBt.setVisibility(View.GONE);
                                mSelectBt.setVisibility(View.GONE);
                                mDeleteMode = false;
                                sendMessage(MSG_GET_PLAY_RECORD, null);
                            }
                        }
                    });
                }
            }
        });
        mSelectBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mList.size() == mSelectList.size() || mSelectBt.getText().equals(mContext.getString(R.string.v2_user_title_cancel))) {
                    mSelectBt.setText(R.string.v2_user_title_select);
                    mSelectList.clear();
                    mPositionList.clear();
                    mAdapter.notifyDataSetChanged();
                } else if (mSelectBt.getText().equals(mContext.getString(R.string.v2_user_title_select))) {
                    for (int i = 0; i < mList.size(); i++) {
                        mSelectList.add(mList.get(i));
                        mPositionList.add(i);
                    }
                    mSelectBt.setText(R.string.v2_user_title_cancel);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void getData() {
        NetWorkService.getPlayRecordsByUserId(mUser.id, Config.FIRST_PAGE, Config.PAGE_SIZE, new PlayRecordListCallback() {
            @Override
            public void onResult(int code, List<PlayRecordModel> jsonResp) {
                if (code == 200) {
                    mList = jsonResp;
                }

                sendMessage(MSG_GET_PLAY_RECORD, null);
            }
        });
    }

    @Override
    protected void onDestroy() {
        mSelectList.clear();
        mPositionList.clear();
        mDeleteMode = false;
        super.onDestroy();
    }

    private int getPattern(Video video) {
        List<Video> stereoList = HomeFragment.getStereoVideoList();
        //List<Video> panoList = HomeFragment.getPanoVideoList();

//        for (Video v: panoList) {
//            if (v.getContentId().equals(video.getContentId())) {
//                //return GvrVideoActivity.PATTERN_TYPE_2;
//                return 0;
//            }
//        }

        for (Video v: stereoList) {
            if (v.getId() == video.getId()) {
                //return GvrVideoActivity.PATTERN_TYPE_3;
                return 0;
            }
        }

        //return GvrVideoActivity.PATTERN_TYPE_1;
        return 0;
    }

    private void startDetailActivity(Video video) {
        Intent intent = new Intent(mContext, StereoVideoDetailActivity.class);
        intent.putExtra(StereoVideoDetailActivity.INTENT_VIDEO, video);
        intent.putExtra(StereoVideoDetailActivity.INTENT_PATTERN, getPattern(video));
        startActivity(intent);
    }

    private void sendMessage(int what, Object obj) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        message.obj = obj;
        mHandler.sendMessage(message);
    }

    private class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public ListAdapter() {
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = mLayoutInflater.from(parent.getContext()).inflate(R.layout.user_history_listitem, parent, false);
            return new CardViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            bindViewHolder((CardViewHolder) holder, position);
        }

        private void bindViewHolder(CardViewHolder holder, final int position) {
            if (!mPositionList.contains(position)) {
                holder.itemView.setAlpha(1.0f);
            } else {
                holder.itemView.setAlpha(0.5f);
            }
            if (mList.get(position).getVideo().getPreview1Url() != null) {
                BitmapLoadManager.display(holder.mImage, mList.get(position).getVideo().getPreview1Url());
            } else {
                holder.mImage.setImageResource(R.drawable.home_place_holder);
            }
            if (mList.get(position).getVideo().getName() != null) {
                holder.mName.setText(mList.get(position).getVideo().getName());
            }
//            holder.mDuration.setText(R.string.v2_user_listitem_time);
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (mList.get(position).getEndTime() != null) {
                holder.mDuration.setText(time.format(mList.get(position).getEndTime()));
            }

            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDeleteMode) {
                        if (mSelectList.contains(mList.get(position))) {
                            mSelectList.remove(mList.get(position));
                            mPositionList.remove((Object) position);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            mSelectList.add(mList.get(position));
                            mPositionList.add(position);
                            mAdapter.notifyDataSetChanged();
                        }
                    } else {
                        NetWorkService.getVideoById(mList.get(position).getVideo().getId(),
                                new VideoCallback() {
                                    @Override
                                    public void onResult(int code, List<Video> JsonResp) {
                                        if (code == 200) {
                                            ArrayList<Video> videos = (ArrayList<Video>) JsonResp;
                                            if (videos != null && videos.size() > 0) {
                                                sendMessage(MSG_GET_VIDEO, videos.get(0));
                                            }
                                        }
                                    }
                                });
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.v2_user_history_cardview)
        CardView mCardView;
        @BindView(R.id.v2_user_history_listitem_icon)
        ImageView mImage;
        @BindView(R.id.v2_user_history_listitem_name)
        TextView mName;
        @BindView(R.id.v2_user_history_listitem_time)
        TextView mDuration;
        @BindView(R.id.v2_user_history_listitem_size)
        TextView mSize;

        public CardViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
