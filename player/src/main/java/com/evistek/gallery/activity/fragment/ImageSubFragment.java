package com.evistek.gallery.activity.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.adapter.ErrorAdapter;
import com.evistek.gallery.activity.adapter.ImageAdapter;
import com.evistek.gallery.model.Category;
import com.evistek.gallery.model.Image;
import com.evistek.gallery.net.Config;
import com.evistek.gallery.net.NetWorkService;
import com.evistek.gallery.net.callback.CategoryCallback;
import com.evistek.gallery.net.callback.ImageCallback;
import com.evistek.gallery.net.json.JsonRespCategory;
import com.evistek.gallery.net.json.JsonRespImage;
import com.evistek.gallery.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by evis on 2016/9/21.
 */

public class ImageSubFragment extends Fragment{
    private static final String TAG = "ImageSubFragment";

    private static final int MSG_GET_CATEGORY_DONE = 0;
    private static final int MSG_GET_IMAGE_DONE = 1;
    private static final int MSG_REFRESH_DATA = 2;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String category = "";

    private String mImageCoverUrlFile = Config.DISK_CACHE_PATH + "myImgCoverUrl";
    private String mImageUrlListFile = Config.DISK_CACHE_PATH + "myImageUrl";
    private String mCachedImageListFile = Config.DISK_CACHE_PATH + "cachedImageUrl";
    private ArrayList<String> mCachedImagelist;
    private List<Category> mCategoryList = new ArrayList<Category>();
    private ArrayList<Image> mImageList = new ArrayList<Image>();

    private OnFragmentInteractionListener mListener;
    private LinearLayoutManager mLinearLayoutManager;
    private ImageAdapter mAdapter;
    private ErrorAdapter mErrorAdapter;
    private boolean mIsImageGot = false;

    private Unbinder mUnbinder;
    private View mRootView;
    @BindView(R.id.image_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.image_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.image_progress_bar)
    ProgressBar mProgressBar;

    public ImageSubFragment(){
        mCachedImagelist = (ArrayList<String>) Utils.readObjectFromFile(mCachedImageListFile);
        if (mCachedImagelist == null) {
            mCachedImagelist = new ArrayList<String>();
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_CATEGORY_DONE:
                    fetchImageList(category);
                    break;
                case MSG_GET_IMAGE_DONE:
                    mIsImageGot = true;
                    break;
                case MSG_REFRESH_DATA:
                    if (Utils.isNetworkAvailable()) {
                        mRecyclerView.setAdapter(mAdapter);
                        fetchData();
                    } else {
                        mRecyclerView.setAdapter(mErrorAdapter);
                        if (mSwipeRefreshLayout != null) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                    break;
            }

            if (mIsImageGot) {
                mAdapter.setImageList(mImageList,mCachedImagelist);
                mAdapter.notifyDataSetChanged();
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
                mIsImageGot = false;
            }

            return true;
        }
    });

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1
     * @param param2
     * @return
     */
    public static ImageSubFragment newInstance(String param1, String param2) {
        ImageSubFragment fragment = new ImageSubFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        category = mParam2;
        mAdapter = new ImageAdapter(getContext());
        mErrorAdapter = new ErrorAdapter(getContext());

        mIsImageGot = false;

        fetchData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.imagefragment_layout, container, false);
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
        if (Utils.isNetworkAvailable()) {
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mRecyclerView.setAdapter(mErrorAdapter);
            mProgressBar.setVisibility(View.INVISIBLE);
        }

        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onPause() {
        mSwipeRefreshLayout.clearAnimation();
        super.onPause();
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void fetchCategoryList() {
        String contentType = "image";
        NetWorkService.getCateGoryList(contentType, new CategoryCallback() {
            @Override
            public void onResult(int code, List<Category> JsonResp) {
                if (code == 200) {
                    for(Category c: JsonResp){
                        mCategoryList.add(c);
                    }
                    sendMessage(MSG_GET_CATEGORY_DONE);
                } else {
                    Log.e(TAG, "Failed to fetch category list, code: " + code);
                }
            }
        });
    }

    private void fetchImageList(final String categoryName) {
        String contentType = "image";
        int categoryId = -1;

        for (Category c: mCategoryList) {
            if (c.getName().equals(categoryName)) {
                categoryId = c.getId();
                break;
            }
        }

        if (categoryId != -1) {
            NetWorkService.getImageList(Config.FIRST_PAGE, Config.PAGE_SIZE, categoryId, new ImageCallback() {
                @Override
                public void onResult(int code, List<Image> JsonResp) {
                    if (code == 200) {
                        mImageList = (ArrayList<Image>) JsonResp;
                        sendMessage(MSG_GET_IMAGE_DONE);
                    } else {
                        Log.e(TAG, "Failed to fetch Image list, code: " + code);
                    }
                }
            });
        }
    }

    private void fetchData() {
        fetchCategoryList();
    }
    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }
}
