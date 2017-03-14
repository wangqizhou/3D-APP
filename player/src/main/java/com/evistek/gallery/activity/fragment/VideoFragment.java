package com.evistek.gallery.activity.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VideoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoFragment extends Fragment {

    public static final int PAGE_MOVIE = 0;
    public static final int PAGE_MV = 1;
    public static final int PAGE_NUMBER = 3;
    private static final int MSG_SET_PAGE = 0;
    public static final int PAGE_PANO_VIDEO = 2;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private MainActivity mActivity;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private VideoPagerAdapter mPagerAdapter;
    private static int mCurrentPage = -1;
    private OnFragmentInteractionListener mListener;

    private Unbinder mUnbinder;
    private View mRootView;
    @BindView(R.id.video_tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.video_view_pager)
    ViewPager mViewPager;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_PAGE:
                    if (mCurrentPage >= 0 && mCurrentPage < PAGE_NUMBER) {
                        TabLayout.Tab tab = mTabLayout.getTabAt(mCurrentPage);
                        if (tab != null && !tab.isSelected()) {
                            tab.select();
                        }
                    }
                    mCurrentPage = -1;
                    break;
            }

            return true;
        }
    });

    public VideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoFragment newInstance(String param1, String param2) {
        VideoFragment fragment = new VideoFragment();
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
        mActivity =(MainActivity) getActivity();
        mActivity.showToolbar();

        mPagerAdapter = new VideoPagerAdapter(getChildFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_video, container, false);
        }
        mUnbinder = ButterKnife.bind(this, mRootView);

        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        sendMessageDelayed(MSG_SET_PAGE);

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
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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

    public static void setCurrentPage(int page) {
        mCurrentPage = page > PAGE_NUMBER ? PAGE_NUMBER - 1 : page;
    }

    private void sendMessageDelayed(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessageDelayed(message, 32);
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

    private class VideoPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] mFragments = new Fragment[3];

        private final String[] TITLES = {
                getString(R.string.movie),
                getString(R.string.mv),getString(R.string.vr)
        };

        public VideoPagerAdapter(FragmentManager fm) {
            super(fm);
            MvFragment mvFragment = new MvFragment();
            StereoVideoFragment stereoVideoFragment = new StereoVideoFragment();
            PanoVideoFragment panoVideoFragment = new PanoVideoFragment();
            mFragments[0] = stereoVideoFragment;
            mFragments[1] = mvFragment;
            mFragments[2] = panoVideoFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }
    }
}
