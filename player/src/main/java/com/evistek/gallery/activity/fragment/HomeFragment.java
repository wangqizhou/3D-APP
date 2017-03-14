package com.evistek.gallery.activity.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.MainActivity;
import com.evistek.gallery.activity.adapter.ErrorAdapter;
import com.evistek.gallery.activity.adapter.HomeAdapter;
import com.evistek.gallery.model.Category;
import com.evistek.gallery.model.Image;
import com.evistek.gallery.model.Video;
import com.evistek.gallery.net.Config;
import com.evistek.gallery.net.NetWorkService;
import com.evistek.gallery.net.callback.CategoryCallback;
import com.evistek.gallery.net.callback.ImageCallback;
import com.evistek.gallery.net.callback.VideoCallback;
import com.evistek.gallery.net.json.JsonRespCategory;
import com.evistek.gallery.net.json.JsonRespImage;
import com.evistek.gallery.net.json.JsonRespVideo;
import com.evistek.gallery.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnHomeFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment{
    private static final String TAG = "HomeFragment";

    private static final String CONTENT_TYPE_VIDEO = "video";
    private static final String CONTENT_TYPE_IMAGE = "image";
    // Video
    private static final String CATEGORY_MOVIE = "电影";
    private static final String CATEGORY_MV = "MV";
    // Image
    private static final String CATEGORY_VIEW = "景物";
    private static final String CATEGORY_PEOPLE = "人物";
    private static final String CATEGORY_BUIDING = "建筑";
    private static final String CATEGORY_ANIMAL = "动物";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int MSG_GET_CATEGORY_DONE = 0;
    private static final int MSG_GET_STEREO_VIDEO_DONE = 1;
    private static final int MSG_GET_IMAGE_DONE = 2;
    private static final int MSG_REFRESH_DATA = 4;

    // This should be the same with the GridView item in HomeAdapter.
    public static final int INDEX_GRIDVIEW_IMAGE = 0;
    public static final int INDEX_GRIDVIEW_MOVIE = 1;
    public static final int INDEX_GRIDVIEW_MV = 2;
    public static final int INDEX_GRIDVIEW_PANO_VIDEO = 3;
    public static final int INDEX_LOCAL_RESOURCE = 4;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MainActivity mActivity;

    private Context mContext;
    private OnHomeFragmentInteractionListener mListener;

    private HomeAdapter mHomeAdapter;
    private GridLayoutManager mGridLayoutManager;
    private ErrorAdapter mErrorAdapter;

    private List<Category> mVideoCategoryList = new ArrayList<Category>();
    private List<Category> mImageCategoryList = new ArrayList<Category>();
    private static ArrayList<Video> mStereoVideoList = new ArrayList<Video>();
    private static ArrayList<Image> mImageList = new ArrayList<>();
    private ArrayList<String> mCachedImagelist;
    private String mCachedImageListFile = Config.DISK_CACHE_PATH + "cachedImageUrl";

    private boolean mIsStereoVideoGot = false;
    private boolean mIsImageGot = false;

    private Unbinder mUnbinder;
    private View mRootView;
    @BindView(R.id.home_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_CATEGORY_DONE:
                    fetchVideoList(CATEGORY_MOVIE);
                    fetchImageList(CATEGORY_VIEW);
                    break;
                case MSG_GET_STEREO_VIDEO_DONE:
                    mIsStereoVideoGot = true;
                    break;
                case MSG_GET_IMAGE_DONE:
                    mIsImageGot = true;
                    break;
                case MSG_REFRESH_DATA:
                    if(Utils.isNetworkAvailable()) {
                        mRecyclerView.setAdapter(mHomeAdapter);
                        fetchData();
                    } else {
                        mRecyclerView.setAdapter(mErrorAdapter);
                        if (mSwipeRefreshLayout != null) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    break;
            }

            if (mIsStereoVideoGot && mIsImageGot) {
                mHomeAdapter.setStereoVideoList(mStereoVideoList);
                mHomeAdapter.setImageList(mImageList);
                mHomeAdapter.setCachedImageList(mCachedImagelist);
                mHomeAdapter.notifyDataSetChanged();
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                mIsStereoVideoGot = false;
            }

            return false;
        }
    });

    private HomeAdapter.OnGridViewClickListener mOnGridViewClickListener = new HomeAdapter.OnGridViewClickListener() {
        @Override
        public void onGridViewClick(int position) {
            if (mListener != null) {
                mListener.onHomeFragmentInteraction(position);
            }
        }
    };

    public HomeFragment() {
        mContext = getContext();
        mCachedImagelist = (ArrayList<String>) Utils.readObjectFromFile(mCachedImageListFile);
        if (mCachedImagelist == null) {
            mCachedImagelist = new ArrayList<String>();
        }
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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

        mIsStereoVideoGot = false;

        mHomeAdapter = new HomeAdapter(getContext());
        mHomeAdapter.setOnGridViewClickListener(mOnGridViewClickListener);
        mErrorAdapter = new ErrorAdapter(getContext());

        mActivity =(MainActivity) getActivity();
        mActivity.showToolbar();

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
            mRootView = inflater.inflate(R.layout.fragment_home, container, false);
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

        mGridLayoutManager = new GridLayoutManager(mContext, HomeAdapter.TOTAL_SPAN_SIZE);
        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int size = 0;
                switch(mHomeAdapter.getItemViewType(position)) {
                    case HomeAdapter.ITEM_TYPE_VIEW_PAGER:
                        size = HomeAdapter.SPAN_SIZE_VIEW_PAGER;
                        break;
                    case HomeAdapter.ITEM_TYPE_GRID_VIEW:
                        size = HomeAdapter.SPAN_SIZE_GRID_VIEW;
                        break;
                    case HomeAdapter.ITEM_TYPE_VIEW_TAG:
                        size = HomeAdapter.SPAN_SIZE_VIEW_TAG;
                        break;
                    case HomeAdapter.ITEM_TYPE_LIST_ITEM_VIDEO:
                        size = HomeAdapter.SPAN_SIZE_LIST_ITEM_VIDEO;
                        break;
                    case HomeAdapter.ITEM_TYPE_LIST_ITEM_GAME:
                        size = HomeAdapter.SPAN_SIZE_LIST_ITEM_GAME;
                        break;
                    case HomeAdapter.ITEM_INDEX_END_TAG:
                        size = HomeAdapter.SPAN_SIZE_VIEW_TAG;
                        break;
                    default:
                        size = HomeAdapter.TOTAL_SPAN_SIZE;
                }

                return size;
            }
        });
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        if (Utils.isNetworkAvailable()) {
            mRecyclerView.setAdapter(mHomeAdapter);
        } else {
            mRecyclerView.setAdapter(mErrorAdapter);
        }

        mActivity.showToolbar();

        // To avoid refresh when switch back to this fragment
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
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
//            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHomeFragmentInteractionListener) {
            mListener = (OnHomeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static List<Video> getStereoVideoList() {
        return mStereoVideoList;
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
    public interface OnHomeFragmentInteractionListener {
        void onHomeFragmentInteraction(int position);
    }

    private void filterCategory(List<Category> list) {
        for(Category c: list) {

            switch (c.getType()) {
                case CONTENT_TYPE_VIDEO:
                    if (c.getName().equals(CATEGORY_MV) ||
                            c.getName().equals(CATEGORY_MOVIE)) {
                        mVideoCategoryList.add(c);
                    }
                    break;
                case CONTENT_TYPE_IMAGE:
                    mImageCategoryList.add(c);
                    break;
            }
        }

    }

    private void fetchCategoryList() {
        String contentType = "all";

        NetWorkService.getCateGoryList(contentType, new CategoryCallback() {
            @Override
            public void onResult(int code, List<Category> JsonResp) {
                if (code == 200) {
                    filterCategory(JsonResp);
                    sendMessage(MSG_GET_CATEGORY_DONE);
                } else {
                    Log.e(TAG, "Failed to fetch category list, code: " + code);
                }
            }
        });

    }

    private void fetchVideoList(final String categoryName) {
        int pageSize = 6;
        int categoryId = -1;

        for (Category c: mVideoCategoryList) {
            if (c.getName().equals(categoryName)) {
                categoryId = c.getId();
                break;
            }
        }

        if (categoryId != -1) {
            NetWorkService.getVideoList(Config.FIRST_PAGE, pageSize, categoryId, new VideoCallback() {
                @Override
                public void onResult(int code, List<Video> JsonResp) {
                    if (code == 200) {
                        switch (categoryName) {
                            case CATEGORY_MOVIE:
                                mStereoVideoList = (ArrayList<Video>) JsonResp;
                                sendMessage(MSG_GET_STEREO_VIDEO_DONE);
                                break;
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch video list, code: " + code);
                    }
                }
            });
        }
    }

    private void fetchImageList(final String categoryName) {
        int pageSize = 6;
        int categoryId = -1;

        for (Category c: mImageCategoryList) {
            if (c.getName().equals(categoryName)) {
                categoryId = c.getId();
                break;
            }
        }

        if (categoryId != -1) {
            NetWorkService.getImageList(Config.FIRST_PAGE, pageSize, categoryId, new ImageCallback() {
                @Override
                public void onResult(int code, List<Image> JsonResp) {
                    if (code == 200) {
                        switch (categoryName) {
                            case CATEGORY_VIEW:
                                mImageList = (ArrayList<Image>) JsonResp;
                                sendMessage(MSG_GET_IMAGE_DONE);
                                break;
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch image list, code: " + code);
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
