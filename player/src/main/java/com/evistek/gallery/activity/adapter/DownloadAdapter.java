package com.evistek.gallery.activity.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.model.Task;
import com.evistek.gallery.net.BitmapLoadManager;
import com.evistek.gallery.service.DownloadService;

import java.util.List;

public class DownloadAdapter extends BaseAdapter {
    private static final String TAG = "DownloadAdapter";
    private static final String KB_PER_SECOND = "KB/s";
    private static final String MB_PER_SECOND = "MB/s";
    private static final String MB = " M";
    private static final String GB = " G";
    private Context mContext;
    private List<Task> mTasks;

    public DownloadAdapter(Context context, DownloadService service, List<Task> tasks) {
        mContext = context;
        mTasks = tasks;
    }

    @Override
    public int getCount() {
        if (mTasks != null)
            return mTasks.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mTasks != null) {
            return mTasks.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            convertView = View.inflate(mContext, R.layout.download_list_item, null);
            viewHolder.mIcon = (ImageView) convertView.findViewById(R.id.download_list_item_img);
            viewHolder.mName = (TextView) convertView.findViewById(R.id.download_list_item_name);
            viewHolder.mSize = (TextView) convertView.findViewById(R.id.download_list_item_size);
            viewHolder.mProgress = (TextView) convertView.findViewById(R.id.download_list_item_percent);
            viewHolder.mRate = (TextView) convertView.findViewById(R.id.download_list_item_rate);
            viewHolder.mPrgressBar = (ProgressBar) convertView.findViewById(R.id.download_list_item_progress);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mName.setText(mTasks.get(position).getName());
        viewHolder.mSize.setText(Formatter.formatFileSize(E3DApplication.getInstance(), mTasks.get(position).getSize()));
        int progress = mTasks.get(position).getProgress();
        viewHolder.mPrgressBar.setProgress(progress);
        viewHolder.mProgress.setText(
                formatString(mContext.getResources().getString(R.string.download_waiting), progress));
        if (mTasks.get(position).getCoverUrl().equals(viewHolder.mIcon.getTag())){

        }else{
            BitmapLoadManager.display(viewHolder.mIcon, mTasks.get(position).getCoverUrl());
            viewHolder.mIcon.setTag(mTasks.get(position).getCoverUrl());
        }

        switch(mTasks.get(position).getStatus()) {
        case Task.STATUS_INIT:
            viewHolder.mProgress.setText(
                    formatString(mContext.getResources().getString(R.string.download_waiting),
                            mTasks.get(position).getProgress()));
            viewHolder.mProgress.setTextColor(Color.BLACK);
            viewHolder.mRate.setText(0 + KB_PER_SECOND);
            break;
        case Task.STATUS_START:
            viewHolder.mProgress.setText(
                    formatString(mContext.getResources().getString(R.string.download_downloading),
                            mTasks.get(position).getProgress()));
            viewHolder.mProgress.setTextColor(Color.RED);
            int rate = mTasks.get(position).getRate();
            if (rate < 1024) {
                viewHolder.mRate.setText(rate + KB_PER_SECOND);
            } else {
                rate /= 1024;
                viewHolder.mRate.setText(rate + MB_PER_SECOND);
            }
            break;
        case Task.STATUS_PAUSE:
            viewHolder.mProgress.setText(
                    formatString(mContext.getResources().getString(R.string.download_paused),
                            mTasks.get(position).getProgress()));
            viewHolder.mProgress.setTextColor(Color.RED);
            viewHolder.mRate.setText(0 + KB_PER_SECOND);
            break;
        case Task.STATUS_CANCEL:
            viewHolder.mProgress.setText(mContext.getResources().getString(R.string.download_cancelled));
            viewHolder.mProgress.setTextColor(Color.RED);
            viewHolder.mRate.setText(0 + KB_PER_SECOND);
            break;
        case Task.STATUS_COMPLETE:
            viewHolder.mProgress.setText(
                    formatString(mContext.getResources().getString(R.string.download_finish),
                            mTasks.get(position).getProgress()));
            viewHolder.mProgress.setTextColor(Color.BLUE);
            viewHolder.mRate.setText(0 + KB_PER_SECOND);
            break;
        }

        return convertView;
    }

    static class ViewHolder {
        public ImageView mIcon;
        public TextView mName;
        public TextView mSize;
        public TextView mProgress;
        public TextView mRate;
        public ProgressBar mPrgressBar;
    }

    private String formatString(String string, int progess) {
        return String.format("%s % 5d %%", string, progess);
    }
}
