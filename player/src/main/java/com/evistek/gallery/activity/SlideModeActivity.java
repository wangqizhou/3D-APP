package com.evistek.gallery.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.evistek.gallery.R;
import com.evistek.gallery.model.LocalImage;
import com.evistek.gallery.net.BitmapLoadManager;
import com.evistek.gallery.utils.CompatibilityChecker;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SlideModeActivity extends Activity {
    private static final int MSG_SELECT = 0;
    private static final int MSG_NO_SELECT = 1;
    private AbsListView mList;
    private static ArrayList<LocalImage> mLocalImageList = new ArrayList<LocalImage>();
    private ArrayList<String> mCheckedIdList;
    private ArrayList<IsCheckStatus> list;
    private LocalImageAdapter mAdapter;

    @BindView(R.id.local_image_grid_view)
    GridView mGridView;
    @BindView(R.id.all_select)
    TextView mSelectAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slide_local_image);
        ButterKnife.bind(this);

        mList = mGridView;
        mAdapter = new LocalImageAdapter();
        mGridView.setAdapter(mAdapter);
        initData();
    }

    private void initData () {
        mCheckedIdList = new ArrayList<String>();
        list = new ArrayList<IsCheckStatus>();
        for (LocalImage image : mLocalImageList) {
            IsCheckStatus status = new IsCheckStatus(IsCheckStatus.TYPE_NO_CHECKED, image.id);
            list.add(status);
        }
    }


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SELECT :
                    ViewHolder viewHolder = (ViewHolder) msg.obj;
                    int position = msg.arg1;
                    if (list.get(position).type == IsCheckStatus.TYPE_CHECKED) {
                        checkedStatus(viewHolder, position);
                    } else {
                        noCheckedStatus(viewHolder, position);
                    }
                    break;
                case MSG_NO_SELECT :
                    Toast.makeText(getApplicationContext(), R.string.select_images, Toast.LENGTH_SHORT).show();
                    break;
            }

        return true;
        }
    });

    private void checkedStatus (ViewHolder viewHolder, int position) {
        viewHolder.checkBox.setChecked(true);
        viewHolder.image.setAlpha(0.5f);
        viewHolder.checkBox.setVisibility(View.VISIBLE);
        mCheckedIdList.add(String.valueOf(mLocalImageList.get(position).id));
        if (mCheckedIdList.size() == mLocalImageList.size()) {
            mSelectAll.setText(R.string.v2_user_title_cancel);
        }
    }

    private void noCheckedStatus (ViewHolder viewHolder, int position) {
        viewHolder.checkBox.setChecked(false);
        viewHolder.image.setAlpha(1f);
        viewHolder.checkBox.setVisibility(View.INVISIBLE);
        for (int i = 0; i < mCheckedIdList.size(); i++) {
            if (mCheckedIdList.get(i).equals(String.valueOf(mLocalImageList.get(position).id))) {
                mCheckedIdList.remove(i);
                break;
            }
        }
        if (mCheckedIdList.size() < mLocalImageList.size()) {
            mSelectAll.setText(R.string.v2_user_title_select);
        }
    }

    public void setLocalImageList (ArrayList<LocalImage> list) {
        mLocalImageList = list;
    }
    public void backPreviousActivity(View v) {
        super.onBackPressed();
    }

    public void confirmPlaySlideMode(View v) {
        if (mCheckedIdList.isEmpty()) {
            sendMessage(MSG_NO_SELECT, null, -1);
        } else {
            startPlayerActivity();
        }
    }

    public void selectAll(View v) {
        if (mCheckedIdList.size() < mLocalImageList.size()) {
            mCheckedIdList.clear();
            for (LocalImage image : mLocalImageList) {
                mCheckedIdList.add(String.valueOf(image.id));
            }
            for (IsCheckStatus itemStatus : list) {
                itemStatus.setTypeChecked(IsCheckStatus.TYPE_CHECKED);
            }
            mSelectAll.setText(R.string.v2_user_title_cancel);
            mAdapter.notifyDataSetChanged();
        } else {
            mCheckedIdList.clear();
            if (!list.isEmpty()) {
                for (IsCheckStatus status : list) {
                    status.setTypeChecked(IsCheckStatus.TYPE_NO_CHECKED);
                }
            }
            mSelectAll.setText(R.string.v2_user_title_select);
        }
        mAdapter.notifyDataSetChanged();
    }

    public void startPlayerActivity () {
        if (CompatibilityChecker.check(this)) {
            Intent intent = new Intent(this, ImageViewerActivity.class);
            intent.putExtra(ImageViewerActivity.IMAGE_ID, Long.valueOf(mCheckedIdList.get(0)));
            intent.putExtra(ImageViewerActivity.IMAGE_SLIDE, true);
            intent.putStringArrayListExtra(ImageViewerActivity.IMAGE_LIST, mCheckedIdList);
            startActivity(intent);
            finish();
        } else {
            CompatibilityChecker.notifyDialog(this);
        }
    }

    class LocalImageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mLocalImageList.size();
        }

        @Override
        public Object getItem(int position) {
            return mLocalImageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null)
            {
                viewHolder = new ViewHolder();
                convertView = View.inflate(E3DApplication.getInstance(), R.layout.slide_mode_item, null);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.local_image_item);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.local_image_check);
                convertView.setTag(viewHolder);
            } else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (mLocalImageList.get(position).thumbnail != null) {
                BitmapLoadManager.display(viewHolder.image, mLocalImageList.get(position).thumbnail, BitmapLoadManager.URI_TYPE_LOCAL);
            }
            viewHolder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeCheckedStatus(position);
                    sendMessage (MSG_SELECT, viewHolder, position);
                }
            });
            initCheckedStatus(viewHolder, position);
            return convertView;
        }
    }

    private void initCheckedStatus (ViewHolder viewHolder, int position) {
        if (list.get(position).type == IsCheckStatus.TYPE_NO_CHECKED) {
            viewHolder.checkBox.setChecked(false);
            viewHolder.checkBox.setVisibility(View.INVISIBLE);
            viewHolder.image.setAlpha(1f);
        } else {
            viewHolder.checkBox.setChecked(true);
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.image.setAlpha(0.5f);
        }
    }

    private void changeCheckedStatus (int position) {
        if (list.get(position).type == IsCheckStatus.TYPE_NO_CHECKED) {
            list.get(position).setTypeChecked(IsCheckStatus.TYPE_CHECKED);
        } else {
            list.get(position).setTypeChecked(IsCheckStatus.TYPE_NO_CHECKED);
        }
    }

    private void sendMessage (int msgWhat, ViewHolder viewHolder, int position) {
        Message message = mHandler.obtainMessage();
        message.what = msgWhat;
        message.obj = viewHolder;
        message.arg1 = position;
        mHandler.sendMessage(message);
    }

    class ViewHolder {
        public ImageView image;
        public CheckBox checkBox;
    }

    class IsCheckStatus {
        public static final int TYPE_CHECKED = 0;
        public static final int TYPE_NO_CHECKED = 1;
        int type;
        long imageId;

        public IsCheckStatus(int type, long imageId) {
            this.type = type;
            this.imageId = imageId;
        }

        public void setTypeChecked(int type) {
            this.type = type;
        }

    }
}
