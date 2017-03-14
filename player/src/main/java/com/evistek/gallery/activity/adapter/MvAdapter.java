package com.evistek.gallery.activity.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.StereoVideoDetailActivity;
import com.evistek.gallery.model.Video;
import com.evistek.gallery.net.BitmapLoadManager;
import com.evistek.gallery.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by evis on 2016/8/11.
 */
public class MvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_CARD_VIEW = 0;
    private static final int ITEM_TYPE_END_TAG = 1;

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private ArrayList<Video> mVideoList;

    public MvAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        mVideoList = new ArrayList<Video>();
    }

    public void setVideoList(ArrayList<Video> list) {
        mVideoList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case ITEM_TYPE_CARD_VIEW:
                viewHolder = new CardViewHolder(mLayoutInflater.inflate(R.layout.video_card_view, parent, false));
                break;
            case ITEM_TYPE_END_TAG:
                viewHolder = new EndTagHolder(mLayoutInflater.inflate(R.layout.end_tag, parent, false));
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof CardViewHolder) {
            bindVideoData((CardViewHolder)holder, position);
        } else if (holder instanceof EndTagHolder) {
            ((EndTagHolder)holder).mTextView.setText(R.string.end_tag_name);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (position < mVideoList.size()) {
            return ITEM_TYPE_CARD_VIEW;
        } else if (position == mVideoList.size()) {
            return ITEM_TYPE_END_TAG;
        }

        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mVideoList.size() + 1;
    }

    private void bindVideoData(CardViewHolder holder, int position) {
        if (position < mVideoList.size()) {
            final Video video = mVideoList.get(position);
            String name = video.getName();
            String coverUrl = "";
            if (video.getPreview1Url() != null) {
                coverUrl = video.getPreview1Url();
            } else {
                coverUrl = video.getPreview1Url();
            }
            holder.mName.setText(name);
            holder.mDuration.setText(Utils.stringForTime(video.getDuration()));
            holder.mPlayCount.setText(video.getDownloadCount() + mContext.getString(R.string.detail_play_count_suffix));
            holder.mUpdateTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:ss:mm").format(video.getCreateTime()));
            holder.mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startVideoDetailActivity(video, 0);
                }
            });

            BitmapLoadManager.display(holder.mImage, coverUrl,
                    BitmapLoadManager.URI_TYPE_REMOTE, R.drawable.home_place_holder_big);
        }
    }

    private void startVideoDetailActivity(Video video, int pattern) {
        Intent intent = new Intent(mContext, StereoVideoDetailActivity.class);
        intent.putExtra(StereoVideoDetailActivity.INTENT_VIDEO, video);
        intent.putExtra(StereoVideoDetailActivity.INTENT_PATTERN, pattern);
        intent.putExtra(StereoVideoDetailActivity.INTENT_VIDEO_LIST, mVideoList);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.getApplicationContext().startActivity(intent);
    }

    static class EndTagHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.end_tag)
        TextView mTextView;

        public EndTagHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.video_card_view_image)
        ImageView mImage;
        @BindView(R.id.video_card_view_name)
        TextView mName;
        @BindView(R.id.video_card_view_duration)
        TextView mDuration;
        @BindView(R.id.video_card_view_play_count)
        TextView mPlayCount;
        @BindView(R.id.video_card_view_update_time)
        TextView mUpdateTime;

        public CardViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
