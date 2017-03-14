package com.evistek.gallery.activity.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.activity.LocalResourceActivity;
import com.evistek.gallery.activity.adapter.LocalVideoAdapter;
import com.evistek.gallery.database.MediaDataBase;
import com.evistek.gallery.model.LocalVideo;
import com.evistek.gallery.utils.EvisUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.R.attr.id;
import static com.evistek.gallery.database.MediaDataBase.*;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocalVideoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocalVideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocalVideoFragment extends Fragment {

    private static final String TAG = "LocalVideoFragment";
    public static final String SAVE_PATH =
            E3DApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

    private static final int MSG_REFRESH_DATA = 0;
    private static final int MSG_REFRESH_DATA_DONE = 1;
    private static final int MSG_REFRESH_THUMBNAIL = 2;
    private static final int MSG_REFRESH_THUMBNAIL_DONE = 3;
    private static final int MSG_REFRESH_3D_FLAG = 4;

    private static final Uri URI_VIDEO = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private static final String ORDER_VIDEO = MediaStore.Video.VideoColumns.DATE_ADDED + " DESC ";
    private static String[] PROJECTION_VIDEO = {
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.VideoColumns.TITLE,
            MediaStore.Video.VideoColumns.DISPLAY_NAME,
            MediaStore.Video.VideoColumns.SIZE,
            MediaStore.Video.VideoColumns.DATE_TAKEN,
            MediaStore.Video.VideoColumns.DURATION,
            MediaStore.Video.VideoColumns.WIDTH,
            MediaStore.Video.VideoColumns.HEIGHT
    };

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private LocalResourceActivity mActivity;
    private OnFragmentInteractionListener mListener;
    private LinearLayoutManager mLinearLayoutManager;
    private LocalVideoAdapter mAdapter;
    private ContentResolver mContentResolver;
    private ArrayList<LocalVideo> mLocalVideoList = new ArrayList<LocalVideo>();
    private boolean mIsRefreshThumbnailDone = true;
    protected boolean isCreated = false;

    private Unbinder mUnbinder;
    private View mRootView;
    @BindView(R.id.local_video_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.local_video_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_DATA:
                    if (mIsRefreshThumbnailDone) {
                        mLocalVideoList.clear();
                        fetchData();
                    } else {
                        if (mSwipeRefreshLayout != null) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                    break;
                case MSG_REFRESH_DATA_DONE:
                    mAdapter.setVideoList(mLocalVideoList);
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

    public LocalVideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocalVideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LocalVideoFragment newInstance(String param1, String param2) {
        LocalVideoFragment fragment = new LocalVideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mAdapter = new LocalVideoAdapter(getContext());
        mContentResolver = getContext().getContentResolver();
        fetchData();
        isCreated = true;
    }

    @Override
    public void onPause() {
        mSwipeRefreshLayout.clearAnimation();
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_local_video, container, false);
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

        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (!isCreated) {
            return;
        }

        if (isVisibleToUser) {
            mActivity = (LocalResourceActivity) getActivity();
            mActivity.hideToolbar();
        }else {
            mActivity = (LocalResourceActivity) getActivity();
            mActivity.showToolbar();
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void fetchData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = mContentResolver.query(URI_VIDEO, PROJECTION_VIDEO, null, null, ORDER_VIDEO);
                if (cursor != null) {

                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToNext();

                        LocalVideo localVideo = new LocalVideo();
                        localVideo.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID));
                        localVideo.path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
                        localVideo.title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.TITLE));
                        localVideo.displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME));
                        localVideo.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE));
                        localVideo.date = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_TAKEN));
                        localVideo.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
                        localVideo.width = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.WIDTH));
                        localVideo.height = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.HEIGHT));

                        mLocalVideoList.add(localVideo);
                    }

                    cursor.close();

                    sendMessage(MSG_REFRESH_DATA_DONE);
                } else {
                    Log.e(TAG, "failed to get local video");
                }
            }
        });

        thread.start();
    }

    private void getThumbnails() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mLocalVideoList.size() > 0) {
                    for (int i = 0; i < mLocalVideoList.size(); i++) {
                        LocalVideo video = mLocalVideoList.get(i);
                        if (video.thumbnail == null || video.thumbnail.isEmpty()) {
                            String file = SAVE_PATH + File.separator + "thumbnail" + String.valueOf(video.id);
                            if (new File(file).exists()) {
                                video.thumbnail = file;
                            } else {
                                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(video.path, MediaStore.Video.Thumbnails.MINI_KIND);
                                if (thumbnail != null) {
                                    savePicture(thumbnail, file);
                                    video.thumbnail = file;
                                } else {
                                    Log.e(TAG, "Failed to get thumbnail, path: " + video.path);
                                }
                            }
                        }

                        sendMessage(MSG_REFRESH_THUMBNAIL_DONE, i);
                    }

                    mIsRefreshThumbnailDone = true;

                    checkVideoType();
                }
            }
        });

        thread.start();
    }

    private void checkVideoType() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mLocalVideoList.size() > 0 ) {
                    for (int i = 0; i < mLocalVideoList.size(); i++) {
                        LocalVideo video = mLocalVideoList.get(i);
                        int srcType = SRC_TYPE_2D;
                        if (video.thumbnail != null) {
                            srcType = (EvisUtil.isSbs(BitmapFactory.decodeFile(video.thumbnail)) == 1) ?
                                    SRC_TYPE_3D : SRC_TYPE_2D;
                        }

                        int playType = srcType;
                        MediaDataBase db = E3DApplication.getInstance().getDataBase();
                        MediaDataBase.MediaInfo mediaInfo = db.new MediaInfo(video.id, video.displayName, 0L, srcType, playType, MIME_TYPE_VIDEO);
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

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
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
}
