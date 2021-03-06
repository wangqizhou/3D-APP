package com.evistek.gallery.activity.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.evistek.gallery.activity.adapter.PanoVideoAdapter;
import com.evistek.gallery.model.Category;
import com.evistek.gallery.model.Video;
import com.evistek.gallery.net.Config;
import com.evistek.gallery.net.NetWorkService;
import com.evistek.gallery.net.callback.CategoryCallback;
import com.evistek.gallery.net.callback.VideoCallback;
import com.evistek.gallery.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PanoVideoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PanoVideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PanoVideoFragment extends Fragment {
    private static final String TAG = "PanoVideoFragment";
    private static final String CATEGORY_VR = "VR全景";

    private static final int MSG_GET_CATEGORY_DONE = 0;
    private static final int MSG_GET_PANO_VIDEO_DONE = 1;
    private static final int MSG_REFRESH_DATA = 2;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View mRootView;
    private OnFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private PanoVideoAdapter mAdapter;
    private ErrorAdapter mErrorAdapter;
    private ProgressBar mProgressBar;

    private List<Category> mCategoryList = new ArrayList<Category>();
    private ArrayList<Video> mPanoVideoList = new ArrayList<Video>();

    private boolean mIsPanoVideoGot = false;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_CATEGORY_DONE:
                    fetchVideoList(CATEGORY_VR);
                    break;
                case MSG_GET_PANO_VIDEO_DONE:
                    mIsPanoVideoGot = true;
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

            if (mIsPanoVideoGot) {
                mAdapter.setVideoList(mPanoVideoList);
                mAdapter.notifyDataSetChanged();
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }

                mIsPanoVideoGot = false;
            }

            return true;
        }
    });

    public PanoVideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PanoVideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PanoVideoFragment newInstance(String param1, String param2) {
        PanoVideoFragment fragment = new PanoVideoFragment();
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

        mAdapter = new PanoVideoAdapter(getContext());
        mErrorAdapter = new ErrorAdapter(getContext());

        mIsPanoVideoGot = false;

        fetchData();
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
            mRootView = inflater.inflate(R.layout.fragment_pano_video, container, false);
        }
        mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.video_swipe_refresh_layout);
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

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.video_recyclerview);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.video_progress_bar);
        if (Utils.isNetworkAvailable()) {
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mRecyclerView.setAdapter(mErrorAdapter);
            mProgressBar.setVisibility(View.INVISIBLE);
        }


        return mRootView;
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

    private void filterCategory(List<Category> list) {
        for(Category c: list) {
            if (c.getName().equals(CATEGORY_VR)) {
                mCategoryList.add(c);
            }
        }
    }

    private void fetchCategoryList() {
        String contentType = "all";

        NetWorkService.getCateGoryList(contentType, new CategoryCallback() {
            @Override
            public void onResult(int code, List<Category> response) {
                if (code == 200) {
                    filterCategory(response);
                    sendMessage(MSG_GET_CATEGORY_DONE);
                } else {
                    Log.e(TAG, "Failed to fetch category list, code: " + code);
                }
            }
        });

    }

    private void fetchVideoList(final String categoryName) {
        String contentType = "video";
        int categoryId = -1;

        for (Category c: mCategoryList) {
            if (c.getName().equals(categoryName)) {
                categoryId = c.getId();
                break;
            }
        }

        if (categoryId != -1) {
            NetWorkService.getVideoList(Config.FIRST_PAGE, Config.PAGE_SIZE, categoryId, new VideoCallback() {
                @Override
                public void onResult(int code, List<Video> response) {
                    if (code == 200) {
                        switch (categoryName) {
                            case CATEGORY_VR:
                                mPanoVideoList = (ArrayList<Video>) response;
                                sendMessage(MSG_GET_PANO_VIDEO_DONE);
                                break;
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch video list, code: " + code);
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
