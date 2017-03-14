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
import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.activity.NetImageViewerActivity;
import com.evistek.gallery.model.Image;
import com.evistek.gallery.net.BitmapLoadManager;
import com.evistek.gallery.utils.CompatibilityChecker;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by evis on 2016/9/21.
 */

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG = "ImageAdapter";
    private static final int ITEM_TYPE_CARD_VIEW = 0;
    private static final int ITEM_TYPE_END_TAG = 1;
    private static final int MSG_DOWNLOAD_DONE = 2;
    private static final int MSG_UPDATE_PROGRESS = 3;

    private static final String IMAGE_INDEX = "image_index";
    private static final String IMAGE_LIST = "image_list";
    private static final String IMAGE_CACHEDLIST = "image_cachedList";

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<String> mCachedImagelist;
    private ArrayList<Image> mImageList;
    private ArrayList<String> mIdList;

    public ImageAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mImageList = new ArrayList<Image>();
        mIdList = new ArrayList<String>();
        mCachedImagelist = new ArrayList<String>();
    }

    public void setImageList(ArrayList<Image> list, ArrayList<String> cachedImagelist) {
        mImageList = list;
        mCachedImagelist = cachedImagelist;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case ITEM_TYPE_CARD_VIEW:
                viewHolder = new ImageAdapter.CardViewHolder(mLayoutInflater.inflate(R.layout.imagefragment_cardview, parent, false));
                break;
            case ITEM_TYPE_END_TAG:
                viewHolder = new ImageAdapter.EndTagHolder(mLayoutInflater.inflate(R.layout.end_tag, parent, false));
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ImageAdapter.CardViewHolder) {
            bindImageData((ImageAdapter.CardViewHolder)holder, position);
        } else if (holder instanceof ImageAdapter.EndTagHolder) {
            ((ImageAdapter.EndTagHolder)holder).mTextView.setText(R.string.end_tag_name);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (position < mImageList.size()) {
            return ITEM_TYPE_CARD_VIEW;
        } else if (position == mImageList.size()) {
            return ITEM_TYPE_END_TAG;
        }

        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mImageList.size() + 1;
    }

    private void bindImageData(final ImageAdapter.CardViewHolder holder, final int position) {
        if (position < mImageList.size()) {
            final Image image = mImageList.get(position);
            String coverUrl = "";
            coverUrl = image.getThumbnail();
            holder.mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startImageDetailActivity(position);
                }
            });

            BitmapLoadManager.display(holder.mImage, coverUrl,
                    BitmapLoadManager.URI_TYPE_REMOTE, R.drawable.home_place_holder_big);
        }
    }

    private void startImageDetailActivity(long index) {
        if (CompatibilityChecker.check(mContext)) {
            Intent intent = new Intent(E3DApplication.getInstance(), NetImageViewerActivity.class);
            intent.putExtra(IMAGE_INDEX, index);
            intent.putExtra(IMAGE_LIST, mImageList);
            intent.putExtra(IMAGE_CACHEDLIST, mCachedImagelist);
            mContext.startActivity(intent);
        } else {
            CompatibilityChecker.notifyDialog(mContext);
        }
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
        @BindView(R.id.image_card_view_image)
        ImageView mImage;

        public CardViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
