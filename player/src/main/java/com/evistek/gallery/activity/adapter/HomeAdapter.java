package com.evistek.gallery.activity.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.activity.NetImageViewerActivity;
import com.evistek.gallery.activity.StereoVideoDetailActivity;
import com.evistek.gallery.model.Application;
import com.evistek.gallery.model.ContentInfo;
import com.evistek.gallery.model.Image;
import com.evistek.gallery.model.Video;
import com.evistek.gallery.net.BitmapLoadManager;
import com.evistek.gallery.utils.CompatibilityChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ZWX on 2016/8/7.
 */
public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "HomeAdapter";

    public static final int ITEM_TYPE_VIEW_PAGER = 0;
    public static final int ITEM_TYPE_GRID_VIEW = 1;
    public static final int ITEM_TYPE_VIEW_TAG = 2;
    public static final int ITEM_TYPE_LIST_ITEM_VIDEO = 3;
    public static final int ITEM_TYPE_LIST_ITEM_GAME = 4;
    public static final int ITEM_TYPE_END_TAG = 5;

    public static final int TOTAL_SPAN_SIZE = 2;
    public static final int SPAN_SIZE_VIEW_PAGER = TOTAL_SPAN_SIZE;
    public static final int SPAN_SIZE_GRID_VIEW = TOTAL_SPAN_SIZE;
    public static final int SPAN_SIZE_VIEW_TAG = TOTAL_SPAN_SIZE;
    public static final int SPAN_SIZE_LIST_ITEM_VIDEO = 1;
    public static final int SPAN_SIZE_LIST_ITEM_GAME = 1;

    public static final int ITEM_NUM_VIEW_PAGER = 1;
    public static final int ITEM_NUM_GRID_VIEW = 1;
    public static final int ITEM_NUM_VIEW_TAG = 1;
    public static final int ITEM_NUM_LIST_ITEM = 6;

    public static final int ITEM_INDEX_VIEW_PAGER = 0;
    public static final int ITEM_INDEX_GRID_VIEW = ITEM_INDEX_VIEW_PAGER + ITEM_NUM_VIEW_PAGER;

    public static final int ITEM_INDEX_VIEW_TAG_VIDEO = ITEM_INDEX_GRID_VIEW + ITEM_NUM_GRID_VIEW;
    public static final int ITEM_INDEX_LIST_ITEM_VIDEO = ITEM_INDEX_VIEW_TAG_VIDEO + ITEM_NUM_VIEW_TAG;

    public static final int ITEM_INDEX_VIEW_TAG_IMAGE = ITEM_INDEX_LIST_ITEM_VIDEO + ITEM_NUM_LIST_ITEM;
    public static final int ITEM_INDEX_LIST_ITEM_IMAGE = ITEM_INDEX_VIEW_TAG_IMAGE + ITEM_NUM_VIEW_TAG;

    public static final int ITEM_INDEX_END_TAG = ITEM_INDEX_LIST_ITEM_IMAGE + ITEM_NUM_LIST_ITEM;

    private static final int ITEM_NUM = ITEM_INDEX_END_TAG + ITEM_NUM_VIEW_TAG;

    private static final int VIEW_PAGER_NUM = 4;
    private static final int VIEW_PAGER_UPDATE_INTERVAL = 5; //seconds

    private static final int MSG_UPDATE_VIEW_PAGER = 0;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ViewPagerHolder mViewPagerHolder;

    private static final String IMAGE_LIST = "image_list";
    private static final String IMAGE_INDEX = "image_index";
    private static final String IMAGE_CACHEDLIST = "image_cachedList";

    private ArrayList<Video> mStereoVideoList = new ArrayList<Video>();
    private ArrayList<Image> mImageList = new ArrayList<Image>();
    private ArrayList<Image> mDownloadList = new ArrayList<Image>();
    private ArrayList<String> mCachedImagelist = new ArrayList<String>();
    private ArrayList<Application> mGameList = new ArrayList<Application>();
    private ArrayList<Video> mViewPagerVideoList = new ArrayList<>();
    private ArrayList<Image> mViewPagerImageList = new ArrayList<>();

    private ScheduledThreadPoolExecutor mScheduledThread;
    private int mViewPagerIndex = 0;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_UPDATE_VIEW_PAGER:
                    mViewPagerHolder.mViewPager.setCurrentItem(mViewPagerIndex);
                    if (mViewPagerVideoList.size() > 0 && mViewPagerIndex < 2) {
                        mViewPagerHolder.mTextView.setText(mViewPagerVideoList.get(mViewPagerIndex).getName());
                    } else {
                        mViewPagerHolder.mTextView.setText("");
                    }
                    break;
            }

            return true;
        }
    });

    public HomeAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        initScheduledThread();
    }

    public void setStereoVideoList(ArrayList<Video> list) {
        mStereoVideoList = list;
    }

    public void setImageList(ArrayList<Image> list) {
        mImageList = list;
    }

    public void setCachedImageList(ArrayList<String> list) {
        mCachedImagelist = list;
    }

    public void setGameList(ArrayList<Application> list) {
        mGameList = list;
    }

//    public void setViewPagerList(ArrayList<Video> list) {
//        mViewPagerList = list;
//    }


    public interface OnGridViewClickListener {
        void onGridViewClick(int position);
    }

    private OnGridViewClickListener mOnGridViewClickListener;

    public void setOnGridViewClickListener(OnGridViewClickListener l) {
        mOnGridViewClickListener = l;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_VIEW_PAGER) {
            mViewPagerHolder = new ViewPagerHolder(mLayoutInflater.inflate(R.layout.viewpager, parent, false));
            return mViewPagerHolder;
        } else if (viewType == ITEM_TYPE_GRID_VIEW) {
            return new GridViewHolder(mLayoutInflater.inflate(R.layout.gridview, parent, false));
        } else if (viewType == ITEM_TYPE_VIEW_TAG) {
            return new ViewTagHolder(mLayoutInflater.inflate(R.layout.view_tag, parent, false));
        } else if (viewType == ITEM_TYPE_LIST_ITEM_VIDEO) {
            return new ViewItemHolder(mLayoutInflater.inflate(R.layout.view_item, parent, false));
        } else if (viewType == ITEM_TYPE_LIST_ITEM_GAME) {
            return new ViewItemHolder(mLayoutInflater.inflate(R.layout.view_item, parent, false));
        } else if (viewType == ITEM_TYPE_END_TAG) {
            return new EndTagHolder(mLayoutInflater.inflate(R.layout.end_tag, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewPagerHolder) {
            bindViewPagerData((ViewPagerHolder) holder, position);
        } else if (holder instanceof GridViewHolder) {
            bindGridViewData((GridViewHolder) holder, position);
        } else if (holder instanceof ViewTagHolder) {
            bindViewTagData((ViewTagHolder)holder, position);
        } else if (holder instanceof ViewItemHolder) {
            bindViewItemData((ViewItemHolder)holder, position);
        } else if (holder instanceof EndTagHolder) {
            ((EndTagHolder)holder).mTextView.setText(R.string.end_tag_name);
        }
    }

    @Override
    public int getItemCount() {
        return ITEM_NUM;
    }

    @Override
    public int getItemViewType(int position) {

        if (position >= ITEM_INDEX_VIEW_PAGER && position < ITEM_INDEX_VIEW_PAGER + ITEM_NUM_VIEW_PAGER) {
            return ITEM_TYPE_VIEW_PAGER;
        } else if (position >= ITEM_INDEX_GRID_VIEW && position < ITEM_INDEX_GRID_VIEW + ITEM_NUM_GRID_VIEW) {
            return ITEM_TYPE_GRID_VIEW;
        } else if (position >= ITEM_INDEX_VIEW_TAG_VIDEO && position < ITEM_INDEX_VIEW_TAG_VIDEO + ITEM_NUM_VIEW_TAG) {
            return ITEM_TYPE_VIEW_TAG;
        } else if (position >= ITEM_INDEX_LIST_ITEM_VIDEO && position < ITEM_INDEX_LIST_ITEM_VIDEO + ITEM_NUM_LIST_ITEM) {
            return ITEM_TYPE_LIST_ITEM_VIDEO;
        } else if (position >= ITEM_INDEX_VIEW_TAG_IMAGE && position < ITEM_INDEX_VIEW_TAG_IMAGE + ITEM_NUM_VIEW_TAG) {
            return ITEM_TYPE_VIEW_TAG;
        } else if (position >= ITEM_INDEX_LIST_ITEM_IMAGE && position < ITEM_INDEX_LIST_ITEM_IMAGE + ITEM_NUM_LIST_ITEM) {
            return ITEM_TYPE_LIST_ITEM_VIDEO;
        } else if (position >= ITEM_INDEX_END_TAG && position < ITEM_INDEX_END_TAG + ITEM_NUM_VIEW_TAG) {
            return ITEM_TYPE_END_TAG;
        }

        return super.getItemViewType(position);
    }

    private void bindViewPagerData(ViewPagerHolder holder, int position) {
        holder.mViewPager.setAdapter(new ViewPagerAdapter(null));
        holder.mViewPager.setCurrentItem(mViewPagerIndex);
        if (mViewPagerVideoList.size() > 0 && mViewPagerIndex < 2) {
            holder.mTextView.setText(mViewPagerVideoList.get(mViewPagerIndex).getName());
        } else {
            holder.mTextView.setText("");
        }
    }

    private void bindGridViewData(GridViewHolder holder, int position) {
        holder.mGridView.setAdapter(new GridViewAdapter());
    }

    private void bindViewTagData(ViewTagHolder holder, int position) {
        switch (position) {
            case ITEM_INDEX_VIEW_TAG_VIDEO:
                holder.mTextView.setText(R.string.movie);
                holder.mIndicatorView.setBackgroundResource(R.color.tag_indicator_vr);
                break;
            case ITEM_INDEX_VIEW_TAG_IMAGE:
                holder.mTextView.setText(R.string.image);
                holder.mIndicatorView.setBackgroundResource(R.color.tag_indicator_3d);
                break;
        }
    }

    private void bindViewItemData(ViewItemHolder holder, int position) {
        if (position >= ITEM_INDEX_LIST_ITEM_VIDEO && position < ITEM_INDEX_LIST_ITEM_VIDEO + ITEM_NUM_LIST_ITEM) {
            bindStereoVideo(holder, position);
        } else if (position >= ITEM_INDEX_LIST_ITEM_IMAGE && position < ITEM_INDEX_LIST_ITEM_IMAGE + ITEM_NUM_LIST_ITEM) {
            bindImage(holder, position);
        } else {
            holder.mImageView.setImageResource(R.drawable.home_place_holder);
            holder.mTextView.setText("");
        }
    }

    private void bindStereoVideo(ViewItemHolder holder, int position) {
        if (mStereoVideoList.size() >= ITEM_NUM_LIST_ITEM) {
            final Video video = mStereoVideoList.get(position - ITEM_INDEX_LIST_ITEM_VIDEO);
            String name = video.getName();
            String coverUrl = "";
            if (video.getPreview1Url() != null ) {
                coverUrl = video.getPreview1Url();
            } else {
                coverUrl = video.getPreview1Url();
            }
            holder.mTextView.setText(name);
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startVideoDetailActivity(video, 0, mStereoVideoList);
                }
            });

            BitmapLoadManager.display(holder.mImageView, coverUrl);
        }
    }

    private void bindImage(ViewItemHolder holder, int position) {
        if (mImageList.size() >= ITEM_NUM_LIST_ITEM) {
            final Image image = mImageList.get(position - ITEM_INDEX_LIST_ITEM_IMAGE);
            String name = image.getName();
            String coverUrl = "";
            if (image.getThumbnail() != null ) {
                coverUrl = image.getThumbnail();
            } else {
                coverUrl = image.getUrl();
            }

            // Need not show name
            holder.mTextView.setVisibility(View.GONE);
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDownloadList.clear();
                    mDownloadList.add(image);
                    startImageViewerActivity(0, mDownloadList);
                }
            });

            BitmapLoadManager.display(holder.mImageView, coverUrl);
        }
    }

    private void startImageViewerActivity(long index, ArrayList<Image> list) {
        if (CompatibilityChecker.check(mContext)) {
            Intent intent = new Intent(E3DApplication.getInstance(), NetImageViewerActivity.class);
            intent.putExtra(IMAGE_INDEX, index);
            intent.putExtra(IMAGE_LIST, list);
            intent.putExtra(IMAGE_CACHEDLIST, mCachedImagelist);
            mContext.startActivity(intent);
        } else {
            CompatibilityChecker.notifyDialog(mContext);
        }
    }

    private void initScheduledThread() {
        mScheduledThread = new ScheduledThreadPoolExecutor(1);

        mScheduledThread.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int currentItem = mViewPagerHolder.mViewPager.getCurrentItem();
                mViewPagerIndex = (currentItem + 1) % VIEW_PAGER_NUM;
                sendMessage(MSG_UPDATE_VIEW_PAGER);
            }
        }, VIEW_PAGER_UPDATE_INTERVAL, VIEW_PAGER_UPDATE_INTERVAL, TimeUnit.SECONDS);
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }

    // TODO: goto StereoVideoDetailActivity for all videos for now
    private void startVideoDetailActivity(Video video, int pattern, ArrayList<Video> videoList) {
        Intent intent = new Intent(E3DApplication.getInstance(), StereoVideoDetailActivity.class);
        intent.putExtra(StereoVideoDetailActivity.INTENT_VIDEO, video);
        intent.putExtra(StereoVideoDetailActivity.INTENT_PATTERN, pattern);
        intent.putExtra(StereoVideoDetailActivity.INTENT_VIDEO_LIST, videoList);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.getApplicationContext().startActivity(intent);
    }

    class ViewPagerHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.view_pager)
        ViewPager mViewPager;
        @BindView(R.id.view_pager_indicator)
        LinearLayout mIndicatorLayout;
        @BindView(R.id.view_pager_name)
        TextView mTextView;

        public ViewPagerHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            View indicatorView = null;
            for (int i = 0; i < VIEW_PAGER_NUM; i++) {
                indicatorView = new View(mContext);
                indicatorView.setBackgroundResource(R.drawable.view_pager_seclect);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(15, 15);
                if (i != 0) {
                    params.leftMargin = 15;
                }
                indicatorView.setEnabled(i == 0 ? true : false);
                indicatorView.setLayoutParams(params);
                mIndicatorLayout.addView(indicatorView);
            }

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    int count = mIndicatorLayout.getChildCount();
                    for (int i = 0; i < count; i++) {
                        mIndicatorLayout.getChildAt(i).setEnabled(position == i);
                    }

                    if (mViewPagerVideoList.size() > 0 && position < 2) {
                        mViewPagerHolder.mTextView.setText(mViewPagerVideoList.get(position).getName());
                    } else {
                        mViewPagerHolder.mTextView.setText("");
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    class GridViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.grid_view)
        GridView mGridView;

        public GridViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mOnGridViewClickListener != null) {
                        mOnGridViewClickListener.onGridViewClick(position);
                    }
                }
            });
        }
    }

    class ViewTagHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.view_tag)
        TextView mTextView;
        @BindView(R.id.view_tag_indicator)
        View mIndicatorView;

        public ViewTagHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    class EndTagHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.end_tag)
        TextView mTextView;

        public EndTagHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    class ViewItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.view_item_image)
        ImageView mImageView;
        @BindView(R.id.view_item_name)
        TextView mTextView;

        public  ViewItemHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private void generateViewPagerData() {
        if (mStereoVideoList.size() >= VIEW_PAGER_NUM / 2 &&
                mImageList.size() >= VIEW_PAGER_NUM / 2) {

            Comparator VideoComparator = new Comparator<Video>() {
                @Override
                public int compare(Video lhs, Video rhs) {
                    if (lhs.getDownloadCount() > rhs.getDownloadCount()) {
                        return 1;
                    } else if (lhs.getDownloadCount() == rhs.getDownloadCount()) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            };

            Comparator ImageComparator = new Comparator<Image>() {
                @Override
                public int compare(Image lhs, Image rhs) {
                    if (lhs.getDownloadCount() > rhs.getDownloadCount()) {
                        return 1;
                    } else if (lhs.getDownloadCount() == rhs.getDownloadCount()) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            };

            List<Video> stereoVideoCopy = new ArrayList<Video>(Arrays.asList(new Video[mStereoVideoList.size()]));
            List<Image> imageCopy = new ArrayList<Image>(Arrays.asList(new Image[mImageList.size()]));
            Collections.copy(stereoVideoCopy, mStereoVideoList);
            Collections.copy(imageCopy, mImageList);
            Collections.sort(stereoVideoCopy, VideoComparator);
            Collections.sort(imageCopy, ImageComparator);

            mViewPagerVideoList.clear();
            mViewPagerImageList.clear();
            for (int i = 0; i < VIEW_PAGER_NUM / 2; i++) {
                mViewPagerVideoList.add(stereoVideoCopy.get(i));
            }
            for (int i = 0; i < VIEW_PAGER_NUM / 2; i++) {
                mViewPagerImageList.add(imageCopy.get(i));
            }
        }
    }

    public class ViewPagerAdapter extends PagerAdapter {

        private List<View> mViewList;

        public ViewPagerAdapter(List<View> views) {
            if ((mViewPagerVideoList.size() + mViewPagerImageList.size()) < VIEW_PAGER_NUM) {
                generateViewPagerData();
            }

            mViewList = new ArrayList<View>();
            if ((mViewPagerVideoList.size() + mViewPagerImageList.size()) >= VIEW_PAGER_NUM) {
                for (int i = 0; i < VIEW_PAGER_NUM/2; i++) {
                    final Video vdo = mViewPagerVideoList.get(i);
//                    final Image img = mViewPagerImageList.get(i);

                    View view = mLayoutInflater.inflate(R.layout.viewpager_item, null);
                    ImageView imageView = (ImageView) view.findViewById(R.id.view_pager_image);
                    ImageView flagView = (ImageView) view.findViewById(R.id.view_pager_flag);

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                          if (contentType.equals("image")) {
//                              mDownloadList.clear();
//                              mDownloadList.add(img);
//                              startImageViewerActivity(0,mDownloadList);
//                          } else if (contentType.equals("video")) {
                              startVideoDetailActivity(vdo,0,mStereoVideoList);
//                          }
                        }
                    });

                    String url="";
//                    if (i < VIEW_PAGER_NUM / 2) {
                        url = vdo.getPreview1Url();
                        flagView.setImageResource(R.drawable.movie_flag);
//                    } else {
//                        url = img.getCoverUrl();
//                        flagView.setImageResource(R.drawable.pic_flag);
//                    }
                    BitmapLoadManager.display(imageView, url, BitmapLoadManager.URI_TYPE_REMOTE,
                            R.drawable.home_place_holder_big);
                    mViewList.add(view);
                }
                for (int i = 0; i < VIEW_PAGER_NUM/2; i++) {
//                    final Video vdo = mViewPagerVideoList.get(i);
                    final Image img = mViewPagerImageList.get(i);

                    View view = mLayoutInflater.inflate(R.layout.viewpager_item, null);
                    ImageView imageView = (ImageView) view.findViewById(R.id.view_pager_image);
                    ImageView flagView = (ImageView) view.findViewById(R.id.view_pager_flag);

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            if (contentType.equals("image")) {
                                mDownloadList.clear();
                                mDownloadList.add(img);
                                startImageViewerActivity(0,mDownloadList);
//                            } else if (contentType.equals("video")) {
//                                startVideoDetailActivity(vdo,0,mStereoVideoList);
//                            }
                        }
                    });

                    String url="";
//                    if (i < VIEW_PAGER_NUM / 2) {
//                        url = vdo.getPreview1Url();
//                        flagView.setImageResource(R.drawable.movie_flag);
//                    } else {
                        url = img.getThumbnail();
                        flagView.setImageResource(R.drawable.pic_flag);
//                    }
                    BitmapLoadManager.display(imageView, url, BitmapLoadManager.URI_TYPE_REMOTE,
                            R.drawable.home_place_holder_big);
                    mViewList.add(view);
                }
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view = mViewList.get(position);
            //如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
            ViewParent vp = view.getParent();
            if (vp != null) {
                ViewGroup parent = (ViewGroup) vp;
                parent.removeView(view);
            }
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public class GridViewAdapter extends BaseAdapter {
        private List<Pair<Integer, Integer>> mData;

        public GridViewAdapter() {
            mData = new ArrayList<Pair<Integer, Integer>>();

            mData.add(new Pair<Integer, Integer>(R.drawable.icon_pic, R.string.image));
            mData.add(new Pair<Integer, Integer>(R.drawable.icon_3d, R.string.movie));
            mData.add(new Pair<Integer, Integer>(R.drawable.icon_mv, R.string.mv));
            mData.add(new Pair<Integer, Integer>(R.drawable.icon_vr, R.string.vr));
            mData.add(new Pair<Integer, Integer>(R.drawable.icon_local, R.string.v2_local_resource));
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.gridview_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.mIcon.setImageResource(mData.get(position).first);
            holder.mName.setText(mData.get(position).second);
            return convertView;
        }

        class ViewHolder {
            @BindView(R.id.grid_view_icon)
            ImageView mIcon;
            @BindView(R.id.grid_view_name)
            TextView mName;

            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
}
