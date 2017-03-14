package com.evistek.gallery.activity.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.activity.SlideModeActivity;
import com.evistek.gallery.activity.adapter.LocalImageAdapter;
import com.evistek.gallery.database.MediaDataBase;
import com.evistek.gallery.model.LocalImage;
import com.evistek.gallery.utils.EvisUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.evistek.gallery.database.MediaDataBase.MIME_TYPE_IMAGE;
import static com.evistek.gallery.database.MediaDataBase.SRC_TYPE_2D;
import static com.evistek.gallery.database.MediaDataBase.SRC_TYPE_3D;


public class LocalImageFragment extends Fragment {
    private static final String TAG = "LocalImageFragment";
    private static final int MSG_REFRESH_DATA = 0;
    private static final int MSG_REFRESH_DATA_DONE = 1;
    private static final int MSG_REFRESH_THUMBNAIL_DONE = 2;
    private static final int MSG_REFRESH_THUMBNAIL = 3;
    private static final int MSG_REFRESH_3D_FLAG = 4;
    private static final Uri URI_IMAGE = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private static final String SAVE_PATH = E3DApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
    private static boolean mIsRefreshThumbnailDone = true;
    private static final String ORDER_IMAGE = MediaStore.Images.ImageColumns.DATE_ADDED + " DESC";
    private static String[] PROJECTION_IMAGE = {
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.TITLE,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.SIZE,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
    };

    private GridLayoutManager mGridLayoutManager;
    private ContentResolver mContentResolver;
    private SlideModeActivity mSlideModeActivity;
    private ArrayList<LocalImage> mLocalImagesList = new ArrayList<LocalImage>();
    private LocalImageAdapter mAdapter;

    private Unbinder mUnbinder;
    private View mRootView;
    @BindView(R.id.local_image_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.local_image_recyclerview)
    RecyclerView mRecyclerView;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_DATA:
                    if (mIsRefreshThumbnailDone) {
                        mLocalImagesList.clear();
                        fetchData();
                    } else {
                        if (mSwipeRefreshLayout != null) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                    break;
                case MSG_REFRESH_DATA_DONE:
                    mAdapter.setLocalImageList(mLocalImagesList);
                    mSlideModeActivity.setLocalImageList(mLocalImagesList);
                    mAdapter.notifyDataSetChanged();
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    sendMessage(MSG_REFRESH_THUMBNAIL);
                    break;
                case MSG_REFRESH_THUMBNAIL:
                    mIsRefreshThumbnailDone = false;
                    getThumbnails();
                    break;
                case MSG_REFRESH_THUMBNAIL_DONE:
                    int position = msg.arg1;
                    mAdapter.notifyItemChanged(position);
                    break;
                case MSG_REFRESH_3D_FLAG:
                    mAdapter.notifyItemChanged(msg.arg1);
                    break;
            }

            return true;
        }
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new LocalImageAdapter(getContext());
        mSlideModeActivity = new SlideModeActivity();
        mContentResolver = getContext().getContentResolver();
        fetchData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_local_image, container, false);
        }
        mUnbinder = ButterKnife.bind(this, mRootView);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sendMessage(MSG_REFRESH_DATA);
            }
        });

        // This is a workaround to avoid that circle indicator doesn't hide when switch
        // between fragments
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }

        mGridLayoutManager = new GridLayoutManager(getActivity(), 2);
        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mAdapter.getItemViewType(position) == LocalImageAdapter.ITEM_TYPE_END_TAG) {
                    return LocalImageAdapter.END_TAG_SPAN_SIZE;
                }
                return LocalImageAdapter.IMAGE_SPAN_SIZE;
            }
        });
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }

    public void fetchData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = mContentResolver.query(URI_IMAGE, PROJECTION_IMAGE, null, null, ORDER_IMAGE);
                if (cursor != null) {
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToNext();
                        LocalImage localImage = new LocalImage();
                        localImage.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
                        localImage.path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                        localImage.title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.TITLE));
                        localImage.name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                        localImage.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE));
                        localImage.width = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH));
                        localImage.height = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT));
                        localImage.date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));
                        mLocalImagesList.add(localImage);
                    }
                    cursor.close();
                    sendMessage(MSG_REFRESH_DATA_DONE);
                } else {
                    Log.e(TAG, "failed to get local image");
                }
            }
        });
        thread.start();
    }

    private void getThumbnails () {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mLocalImagesList.size() > 0) {
                    for (int i = 0; i < mLocalImagesList.size(); i++) {
                        LocalImage image = mLocalImagesList.get(i);
                        if (image.thumbnail == null || image.thumbnail.isEmpty()) {
                            String file = SAVE_PATH + File.separator + "thumbnail" + String.valueOf(image.id);
                            if (new File(file).exists()) {
                                image.thumbnail = file;
                            } else {
                                Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(mContentResolver, image.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                                if (thumbnail != null) {
                                    savePicture(thumbnail, file);
                                    image.thumbnail = file;

                                } else {
                                    Log.e(TAG, "Failed to get thumbnail, path: " + image.path);
                                }
                            }
                        }
                        sendMessage(MSG_REFRESH_THUMBNAIL_DONE, i);
                    }
                    mIsRefreshThumbnailDone = true;

                    checkImageType();
                }
            }
        }).start();
    }

    private void checkImageType() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mLocalImagesList.size() > 0 ) {
                    for (int i = 0; i < mLocalImagesList.size(); i++) {
                        LocalImage image = mLocalImagesList.get(i);
                        int srcType = SRC_TYPE_2D;
                        if (image.thumbnail != null) {
                            srcType = (EvisUtil.isSbs(BitmapFactory.decodeFile(image.thumbnail)) == 1) ?
                                    SRC_TYPE_3D : SRC_TYPE_2D;
                        }

                        int playType = srcType;
                        MediaDataBase db = E3DApplication.getInstance().getDataBase();
                        MediaDataBase.MediaInfo mediaInfo = db.new MediaInfo(image.id, image.name, 0L, srcType, playType, MIME_TYPE_IMAGE);
                        E3DApplication.getInstance().getDataBase().add(mediaInfo);

                        if (srcType == SRC_TYPE_3D) {
                            sendMessage(MSG_REFRESH_3D_FLAG, i);
                        }
                    }
                }
            }
        });

        thread.start();
    }

    private void sendMessage(int what, int arg1) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        message.arg1 = arg1;
        mHandler.sendMessage(message);
    }

    public String savePicture(Bitmap b, String file) {

        try {
            FileOutputStream fout = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);

            bos.flush();
            bos.close();
            Log.i(TAG, "Save file: " + file);

            return file;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.i(TAG, "Failed to save file: " + file);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
