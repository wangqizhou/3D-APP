package com.evistek.gallery.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.fragment.LocalImageFragment;
import com.evistek.gallery.activity.fragment.LocalVideoFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocalResourceActivity extends FragmentActivity {
    private localPagerAdapter mPagerAdapter;

    @BindView(R.id.local_view_pager)
    ViewPager mViewPager;
    @BindView(R.id.local_tablayout)
    TabLayout mTabLayout;
    @BindView(R.id.local_resource_toolbar)
    RelativeLayout mTitleLayout;
    @BindView(R.id.slide_play)
    TextView mSlideView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_resource);
        ButterKnife.bind(this);

        mPagerAdapter = new localPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public void hideToolbar(){
        if (mTitleLayout != null) {
            mSlideView.setVisibility(View.GONE);
        }
    }

    public void showToolbar(){
        if (mTitleLayout != null) {
            mSlideView.setVisibility(View.VISIBLE);
        }
    }

    public void backPreviousActivity(View v) {
        super.onBackPressed();
        mViewPager.setAdapter(null);
        mPagerAdapter = null;
        finish();
    }

    public void slideMode(View v) {
        Intent intent = new Intent(this, SlideModeActivity.class);
        startActivity(intent);
    }

    private class localPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] mFragments = new Fragment[2];

        private final String[] TITLES = {
                getString(R.string.tab_image),
                getString(R.string.tab_video),
        };

        public localPagerAdapter(FragmentManager fm) {
            super(fm);
            LocalImageFragment localImageFragment = new LocalImageFragment();
            LocalVideoFragment localVideoFragment = new LocalVideoFragment();
            mFragments[0] = localImageFragment;
            mFragments[1] = localVideoFragment;
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
