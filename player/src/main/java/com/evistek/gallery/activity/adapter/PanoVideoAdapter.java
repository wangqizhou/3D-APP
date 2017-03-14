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

import org.rajawali3d.vr.activity.GvrVideoActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PanoVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_CARD_VIEW = 0;
    private static final int ITEM_TYPE_END_TAG = 1;

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private ArrayList<Video> mVideoList;

    public PanoVideoAdapter(Context context) {
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
                coverUrl = video.getPortraitCoverUrl();
            }
            holder.mName.setText(name);
            holder.mDuration.setText(Utils.stringForTime(video.getDuration()));
            holder.mPlayCount.setText(video.getDownloadCount() + mContext.getString(R.string.detail_play_count_suffix));
            holder.mUpdateTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:ss:mm").format(video.getCreateTime()));
            holder.mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startVideoDetailActivity(video, GvrVideoActivity.PATTERN_TYPE_2);
                }
            });
            BitmapLoadManager.display(holder.mImage, coverUrl);
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

    public class EndTagHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public EndTagHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.end_tag);
        }
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImage;
        public TextView mName;
        public TextView mDuration;
        public TextView mPlayCount;
        public TextView mUpdateTime;

        public CardViewHolder(View view) {
            super(view);

            mImage = (ImageView) view.findViewById(R.id.video_card_view_image);
            mName = (TextView) view.findViewById(R.id.video_card_view_name);
            mDuration = (TextView) view.findViewById(R.id.video_card_view_duration);
            mPlayCount = (TextView) view.findViewById(R.id.video_card_view_play_count);
            mUpdateTime = (TextView) view.findViewById(R.id.video_card_view_update_time);
        }
    }
}
