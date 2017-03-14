package com.evistek.gallery.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.evistek.gallery.R;

import org.rajawali3d.vr.renderer.GvrVideoRenderer;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

/**
 * Created by evis on 2016/9/5.
 */
public class ControlLayer {

    private static final String DEFAULT_TIME = "00:00:00";

    private Context mContext;
    private View mLayoutView;

    @BindView(R.id.relativeLayout_l)
    RelativeLayout mRelativeLayoutLeft;
    @BindView(R.id.relativeLayout_r)
    RelativeLayout mRelativeLayoutRight;
    @BindView(R.id.vrButton_l)
    ImageButton mModeButtonLeft;
    @BindView(R.id.vrButton_r)
    ImageButton mModeButtonRight;
    @BindView(R.id.videoSeekBar_l)
    SeekBar mSeekBarLeft;
    @BindView(R.id.videoSeekBar_r)
    SeekBar mSeekBarRight;
    @BindView(R.id.loadingProgressbar_l)
    ProgressBar mProgressBarLeft;
    @BindView(R.id.loadingProgressbar_r)
    ProgressBar mProgressBarRight;
    @BindView(R.id.elapsedTime_l)
    TextView mElapsedTimeLeft;
    @BindView(R.id.elapsedTime_r)
    TextView mElapsedTimeRight;
    @BindView(R.id.totalTime_l)
    TextView mTotalTimeLeft;
    @BindView(R.id.totalTime_r)
    TextView mTotalTimeRight;
    @BindView(R.id.pause_l)
    ImageView mPauseViewLeft;
    @BindView(R.id.pause_r)
    ImageView mPauseViewRight;

    public ControlLayer(Context context) {
        mContext = context;
        mLayoutView = LayoutInflater.from(context).inflate(R.layout.video_controller_vr, null);
        ButterKnife.bind(this, mLayoutView);

        initView();
    }

    public View getView() {
        return mLayoutView;
    }

    public void setSeekBarEnable(boolean enable) {
        mSeekBarLeft.setEnabled(enable);
        mSeekBarRight.setEnabled(enable);
    }

    public void setSeekBarMax(int max) {
        mSeekBarLeft.setMax(max);
        mSeekBarRight.setMax(max);
    }

    public void setSeekBarProgress(int progress) {
        mSeekBarLeft.setProgress(progress);
        mSeekBarRight.setProgress(progress);
    }

    public void setSeekBarSecondaryProgress(int progress) {
        mSeekBarLeft.setSecondaryProgress(progress);
        mSeekBarRight.setSecondaryProgress(progress);
    }

    public void setElapsedTime(String elapsedTime) {
        mElapsedTimeLeft.setText(elapsedTime);
        mElapsedTimeRight.setText(elapsedTime);
    }

    public void setTotalTime(String totalTime) {
        mTotalTimeLeft.setText(totalTime);
        mTotalTimeRight.setText(totalTime);
    }

    public void setProgressBarVisibility(int visibility) {
        mProgressBarLeft.setVisibility(visibility);
        mProgressBarRight.setVisibility(visibility);
    }

    public void setPauseViewVisibility(int visibility) {
        if (visibility == View.VISIBLE && mLayoutView.getVisibility() == View.INVISIBLE) {
            mLayoutView.setVisibility(View.VISIBLE);
        }
        mPauseViewLeft.setVisibility(visibility);
        mPauseViewRight.setVisibility(visibility);
    }

    public void setVisibility(int visibility) {
        mLayoutView.setVisibility(visibility);
    }

    public int getVisibility() {
        return mLayoutView.getVisibility();
    }

    public void setModeButtonClickListener(View.OnClickListener listener) {
        mModeButtonLeft.setOnClickListener(listener);
        mModeButtonRight.setOnClickListener(listener);
    }

    public void setSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
        mSeekBarLeft.setOnSeekBarChangeListener(listener);
        mSeekBarRight.setOnSeekBarChangeListener(listener);
    }

    public void updateLayout(int playMode) {
        switch (playMode) {
            case GvrVideoRenderer.PLAY_MODE_VR:
            case GvrVideoRenderer.PLAY_MODE_3D:
                if (!mRelativeLayoutRight.isShown()) {
                    mRelativeLayoutRight.setVisibility(View.VISIBLE);
                }
                break;
            case GvrVideoRenderer.PLAY_MODE_2D:
            case GvrVideoRenderer.PLAY_MODE_FULLSCREEN:
                if (mRelativeLayoutRight.isShown()) {
                    mRelativeLayoutRight.setVisibility(View.GONE);
                }
                break;
        }

        updateButtonView(playMode);
    }

    public void updateButtonView(int playMode) {
        switch (playMode) {
            case GvrVideoRenderer.PLAY_MODE_FULLSCREEN:
                mModeButtonLeft.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_fullscreen));
                mModeButtonRight.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_fullscreen));
                break;
            case GvrVideoRenderer.PLAY_MODE_3D:
                mModeButtonLeft.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_3d));
                mModeButtonRight.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_3d));
                break;
            case GvrVideoRenderer.PLAY_MODE_2D:
                mModeButtonLeft.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_2d));
                mModeButtonRight.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_2d));
                break;
            case GvrVideoRenderer.PLAY_MODE_VR:
                mModeButtonLeft.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_vr));
                mModeButtonRight.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_vr));
                break;
            default:
                break;
        }
    }

    private void initView() {
        mElapsedTimeLeft.setText(DEFAULT_TIME);
        mElapsedTimeRight.setText(DEFAULT_TIME);
        mTotalTimeLeft.setText(DEFAULT_TIME);
        mTotalTimeRight.setText(DEFAULT_TIME);
        mSeekBarLeft.setEnabled(false);
        mSeekBarRight.setEnabled(false);
        mPauseViewLeft.setVisibility(View.GONE);
        mPauseViewRight.setVisibility(View.GONE);
    }

}
