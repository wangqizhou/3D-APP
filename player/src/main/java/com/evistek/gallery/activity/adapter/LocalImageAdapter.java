package com.evistek.gallery.activity.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.activity.ImageViewerActivity;
import com.evistek.gallery.database.MediaDataBase;
import com.evistek.gallery.model.LocalImage;
import com.evistek.gallery.net.BitmapLoadManager;
import com.evistek.gallery.utils.CompatibilityChecker;
import com.evistek.gallery.utils.MediaScannerNotifier;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ymzhao on 2016/9/21.
 */

public class LocalImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int ITEM_TYPE_CARD_VIEW = 0;
    public static final int ITEM_TYPE_END_TAG = 1;
    public static final int IMAGE_SPAN_SIZE = 1;
    public static final int END_TAG_SPAN_SIZE = 2;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<LocalImage> mLocalImageList = new ArrayList<LocalImage>();
    ArrayList<String> mIdList;
    private MediaScannerNotifier mMediaNotifier;
    boolean mIsSlide = false;

    private static final int MSG_DELETE_ITEM = 0;
    private static final int MSG_DELETE_FAIL = 1;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DELETE_ITEM:
                    mIdList.remove(msg.arg1);
                    mLocalImageList.remove(msg.arg1);
                    notifyItemRemoved(msg.arg1);
                    notifyItemRangeChanged(msg.arg1, mLocalImageList.size() - msg.arg1);
                    break;
                case MSG_DELETE_FAIL:
                    showDeleteFailureDialog();
                    break;
            }

            return true;
        }
    });

    public LocalImageAdapter (Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mIdList = new ArrayList<String>();
        mMediaNotifier = new MediaScannerNotifier(mContext);
    }

    public void setLocalImageList (ArrayList<LocalImage> list) {
        mLocalImageList = list;
        if (!mLocalImageList.isEmpty()) {
            for (LocalImage image : mLocalImageList) {
                mIdList.add(String.valueOf(image.id));
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        View view = null;
        switch (viewType) {
            case ITEM_TYPE_CARD_VIEW:
                view  = mLayoutInflater.inflate(R.layout.local_image_card_view, parent, false);
                viewHolder = new LocalImageViewHolder(view);
                break;
            case ITEM_TYPE_END_TAG:
                view = mLayoutInflater.inflate(R.layout.end_tag, parent, false);
                viewHolder = new EndTagHolder(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof LocalImageViewHolder) {
            final LocalImageViewHolder viewHolder = (LocalImageViewHolder)holder;
            final LocalImage image = mLocalImageList.get(position);
            if (image.thumbnail != null) {
                BitmapLoadManager.display(viewHolder.mImage, image.thumbnail,
                        BitmapLoadManager.URI_TYPE_LOCAL, R.drawable.bg_placeholder);
            }

            if (E3DApplication.getInstance().getDataBase().getSrcType(image.id) == MediaDataBase.SRC_TYPE_3D) {
                viewHolder.m3DOverlay.setVisibility(View.VISIBLE);
            } else {
                viewHolder.m3DOverlay.setVisibility(View.GONE);
            }

            viewHolder.mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPlayerActivity(position);
                }
            });
            viewHolder.mImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showDeleteDialog(image, position);
                    return false;
                }
            });

        } else if (holder instanceof EndTagHolder) {
            ((EndTagHolder)holder).mTextView.setText(R.string.end_tag_name);
        }

    }

    public void startPlayerActivity (int position) {
        if (CompatibilityChecker.check(mContext)) {
            Intent intent = new Intent(E3DApplication.getInstance(), ImageViewerActivity.class);
            intent.putExtra(ImageViewerActivity.IMAGE_ID, mLocalImageList.get(position).id);
            intent.putExtra(ImageViewerActivity.IMAGE_SLIDE, mIsSlide);
            intent.putStringArrayListExtra(ImageViewerActivity.IMAGE_LIST, mIdList);
            mContext.startActivity(intent);
        } else {
            CompatibilityChecker.notifyDialog(mContext);
        }
    }

    @Override
    public int getItemCount() {
        return mLocalImageList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mLocalImageList.size() ) {
            return ITEM_TYPE_CARD_VIEW;
        } else if (position == mLocalImageList.size()) {
            return ITEM_TYPE_END_TAG;
        }
        return super.getItemViewType(position);
    }

    static class EndTagHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.end_tag)
        TextView mTextView;

        public EndTagHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    static class LocalImageViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.local_image_card_view)
        ImageView mImage;
        @BindView(R.id.image_3d_overlay)
        ImageView m3DOverlay;

        public LocalImageViewHolder (View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private void sendMessage(int what, int arg1) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        message.arg1 = arg1;
        mHandler.sendMessage(message);
    }

    private void showDeleteDialog(final LocalImage image, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(image.name)
                .setTitle(R.string.delete_dir);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File file = new File(image.path);
                        if (file.isFile() && file.exists()) {

                            if (file.delete()){
                                mMediaNotifier.scanFile(image.path, null);
                                sendMessage(MSG_DELETE_ITEM, position);
                            } else {
                                sendMessage(MSG_DELETE_FAIL, 0);
                            }
                        }
                    }
                }).start();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeleteFailureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.delete_fail_msg)
                .setTitle(R.string.delete_fail);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
