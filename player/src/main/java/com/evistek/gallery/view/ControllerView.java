package com.evistek.gallery.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.media.AudioManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.NetVideoPlayerActivity;
import com.evistek.gallery.activity.VideoPlayerActivity;
import com.evistek.gallery.render.RenderBase;
import com.evistek.gallery.render.ShaderBase;

import java.util.ArrayList;
import java.util.HashMap;

public class ControllerView
{
    private static final int EXTRA_WINDOW_POSX = 0;
    private static final int EXTRA_WINDOW_POSY = 0;
    private static final int SEEKBAR_MAX_RANGE = 1000;

    private static int sExtraHeight;

    private Context mContext;
    private LayoutInflater mInflater;

    private ImageButton mBackButton;
    private TextView mNameView;
    private RadioGroup mRadioGroup;
    // Only for Internet media
    private ImageButton mVrButton;

    private AlertDialog mMenuDialog;
    private GridView mMenuGrid;
    private View mMenuView;
    private int mCheckedId;

    private View mExtraView;
    private PopupWindow mExtraWindow;
    private IControl mControl;
    // only for video
    private boolean isVideo;
    private AudioManager mAudioManager;
    private ImageButton mVolume;
    private boolean mIsVolumeShow;
    private ImageButton mDepth;
    private boolean mIsDepthShow;
    private RelativeLayout mSettingsLayout;
    // local media or Internet media
    private boolean mIsLocal;

    private SeekBar mSeekBar;
    private TextView mPlayedTextView;
    private TextView mDurationTextView;
    private LinearLayout mSeekBarLayout;

    private ImageButton mScaleButton;
    private ImageButton mPreviousButton;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;
    private LinearLayout mButtonsLayout;

    private ImageButton mLockButton;

    private LinearLayout mBottomLayout;

    private VerticalSeekBar mVolumeSeekBar;
    private PopupWindow mVolumeWindow;
    private VerticalSeekBar mDepthSeekBar;
    private PopupWindow mDepthWindow;

    private View mControllerView;
    private PopupWindow mControllerWindow;

    private int mExtraMenuWidth;

    public ControllerView(Context context, boolean video) {
        this(context, video, true);
    }

    public ControllerView(Context context, boolean video, boolean isLocal) {
        mContext = context;
        isVideo = video;
        mIsLocal = isLocal;
        mInflater = LayoutInflater.from(mContext);
        initExtraView();
        initMenuView();
        if (isVideo) {
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            initControlView();
            initVerticalWindow();
        }
    }

    public void setControl(IControl control) {
        mControl = control;
    }

    private void initExtraView() {
        mExtraView = mInflater.inflate(R.layout.extra_menu, null);
        mExtraWindow = new PopupWindow(mExtraView);
        mBackButton = (ImageButton) mExtraView.findViewById(R.id.extra_back);
        mNameView = (TextView) mExtraView.findViewById(R.id.extra_name);
        mRadioGroup = (RadioGroup) mExtraView.findViewById(R.id.extra_radio_group);
        mVrButton = (ImageButton) mExtraView.findViewById(R.id.extra_vr);

        mBackButton.setOnClickListener(mClickListener);

        mVrButton.setOnClickListener(mClickListener);
        if (!isVideo) {
            mVrButton.setVisibility(View.INVISIBLE);
        }

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonId = group.getCheckedRadioButtonId();
                switch (radioButtonId) {
                case R.id.extra_2d:
                    mControl.setPlayMode(RenderBase.RENDER_MODE_2D);
                    if (isVideo) {
                        mDepth.setVisibility(View.GONE);
                    }
                    break;
                case R.id.extra_3d:
                    mControl.setPlayMode(RenderBase.RENDER_MODE_3D);
                    if (isVideo) {
                        mDepth.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.extra_2d_3d:
                    mControl.setPlayMode(RenderBase.RENDER_MODE_2D3D);
                    if (isVideo) {
                        mDepth.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.extra_3d_2d:
                    mControl.setPlayMode(RenderBase.RENDER_MODE_3D2D);
                    if (isVideo) {
                        mDepth.setVisibility(View.GONE);
                    }
                    break;
                }
            }
        });
    }

    public void setRadioChecked(int mode) {
        int id = -1;
        switch (mode) {
        case RenderBase.RENDER_MODE_2D:
            id = R.id.extra_2d;
            break;
        case RenderBase.RENDER_MODE_2D3D:
            id = R.id.extra_2d_3d;
            break;
        case RenderBase.RENDER_MODE_3D:
            id = R.id.extra_3d;
            break;
        case RenderBase.RENDER_MODE_3D2D:
            id = R.id.extra_3d_2d;
            break;
        default:
            break;
        }
        if (id != -1) {
            int currentId = mRadioGroup.getCheckedRadioButtonId();
            if (currentId != id) {
                setButtonChecked(id);
            } else {
                //If the checked radio button is the same one,
                //onCheckedChanged() will not be called.
                //Need set play mode here directly
                mControl.setPlayMode(mode);
            }
        }
    }

    private void initControlView() {
        mControllerView = mInflater.inflate(R.layout.controller, null);
        mControllerWindow = new PopupWindow(mControllerView);

        mPlayedTextView = (TextView) mControllerView.findViewById(R.id.controler_time);
        mDurationTextView = (TextView) mControllerView.findViewById(R.id.controler_duration);
        mScaleButton = (ImageButton) mControllerView.findViewById(R.id.controler_scale);
        mPreviousButton = (ImageButton) mControllerView.findViewById(R.id.controler_previous);
        mPlayButton = (ImageButton) mControllerView.findViewById(R.id.controler_play);
        mNextButton = (ImageButton) mControllerView.findViewById(R.id.controler_next);
        mLockButton = (ImageButton) mControllerView.findViewById(R.id.controler_unclock);
        mVolume = (ImageButton) mControllerView.findViewById(R.id.controler_volume);
        mDepth = (ImageButton) mControllerView.findViewById(R.id.controler_depth);
        mSeekBar = (SeekBar) mControllerView.findViewById(R.id.controler_seekbar);
        mSettingsLayout = (RelativeLayout) mControllerView.findViewById(R.id.layout_ctrl_top);
        mSeekBarLayout = (LinearLayout) mControllerView.findViewById(R.id.layout_ctrl_progress);
        mButtonsLayout = (LinearLayout) mControllerView.findViewById(R.id.layout_button);
        mBottomLayout = (LinearLayout) mControllerView.findViewById(R.id.layout_ctrl_bottom);

        mScaleButton.setOnClickListener(mClickListener);
        mPreviousButton.setOnClickListener(mClickListener);
        mPlayButton.setOnClickListener(mClickListener);
        mNextButton.setOnClickListener(mClickListener);
        mLockButton.setOnClickListener(mClickListener);
        mVolume.setOnClickListener(mClickListener);
        mDepth.setOnClickListener(mClickListener);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

        mIsVolumeShow = false;
        mIsDepthShow = false;

        if (!mIsLocal) {
            mPreviousButton.setVisibility(View.GONE);
            mNextButton.setVisibility(View.GONE);
        }
    }

    private void initMenuView() {
        mCheckedId = ShaderBase.getFramePackingFormat();
        mExtraMenuWidth =  (int) mContext.getResources().getDimension(R.dimen.extra_menu_width);
        String[] menu = mContext.getResources().getStringArray(R.array.menu);
        mMenuView = mInflater.inflate(R.layout.menu_gridview, null);
        mMenuDialog = new AlertDialog.Builder(mContext).create();
        mMenuDialog.setView(mMenuView);
        mMenuDialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU)
                    dialog.dismiss();
                return false;
            }
        });
        mMenuGrid = (GridView) mMenuView.findViewById(R.id.menu_gridview);
        mMenuGrid.setAdapter(getMenuAdapter(menu));
        mMenuGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CHOOSE_POS = position;
                menuSettings();
                mMenuDialog.dismiss();
            }
        });
    }

    private void createBuilder(String title, String[] items, int checkedItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title);
        builder.setSingleChoiceItems(items, checkedItem, mMenuListener);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mControl.startMenu(CHOOSE_POS, CHOOSE_TAG);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog ad = builder.create();
        ad.show();
        Window dialogWindow = ad.getWindow();
        WindowManager.LayoutParams param = dialogWindow.getAttributes();
        param.dimAmount = .0f;
        dialogWindow.setAttributes(param);
    }

    private static int CHOOSE_POS = 0;
    private static int CHOOSE_TAG = 0;

    private DialogInterface.OnClickListener mMenuListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            CHOOSE_TAG = which;
            mCheckedId = which;
        }
    };

    private void menuSettings() {
        if (CHOOSE_POS < 0)
            return;
        String[] menu = mContext.getResources().getStringArray(R.array.menu);
        String[] items = null;
        switch (CHOOSE_POS) {
        case 0:
            items = mContext.getResources().getStringArray(R.array.menuLR);
            break;
        case 1:
            items = mContext.getResources().getStringArray(R.array.menuOnOff);
            break;
        }
        createBuilder(menu[CHOOSE_POS], items, mCheckedId);
    }

    private SimpleAdapter getMenuAdapter(String[] menuNameArray) {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        int length = menuNameArray.length;
        for (int i = 0; i < length; ++i) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemText", menuNameArray[i]);
            data.add(map);
        }
        SimpleAdapter simperAdapter = new SimpleAdapter(mContext, data, R.layout.menu_item,
                new String[] { "itemText" }, new int[] { R.id.menu_item_text });
        return simperAdapter;
    }

    private void showPopMenu() {
        if (mMenuDialog != null) {
            mMenuDialog.show();
            Window dialogWindow = mMenuDialog.getWindow();
            WindowManager.LayoutParams param = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.TOP | Gravity.RIGHT);
            param.x = 0;
            param.y = EXTRA_WINDOW_POSY + sExtraHeight;
            param.width = mExtraMenuWidth;
            param.height = WindowManager.LayoutParams.WRAP_CONTENT;
            param.dimAmount = .0f;
            dialogWindow.setAttributes(param);
        }
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int viewId = v.getId();
            switch (viewId) {
            case R.id.controler_scale:
                mControl.scale();
                if (mControl.isKeepAR()) {
                    mScaleButton.setImageDrawable(
                            mContext.getResources().getDrawable(R.drawable.btn_controller_scale_fullscreen));
                } else {
                    mScaleButton.setImageDrawable(
                            mContext.getResources().getDrawable(R.drawable.btn_controller_scale_auto));
                }
                break;
            case R.id.controler_previous:
                mControl.previous();
                break;
            case R.id.controler_play:
                mControl.play();
                break;
            case R.id.controler_next:
                mControl.next();
                break;
            case R.id.controler_unclock:
                mControl.lock();
                break;
            case R.id.controler_volume:
                if (mIsVolumeShow) {
                    mControl.hideVolumeWindow();
                } else {
                    mControl.showVolumeWindow();
                }
                break;
            case R.id.controler_depth:
                if (mIsDepthShow) {
                    mControl.hideDepthWindow();
                } else {
                    mControl.showDepthWindow();
                }
                break;
            case R.id.extra_back:
                mControl.back();
//                ((Activity) mContext).onBackPressed();
                break;
            case R.id.extra_vr:
                if (mContext instanceof VideoPlayerActivity) {
                    ((VideoPlayerActivity)mContext).startVrActivity();
                } else if (mContext instanceof NetVideoPlayerActivity) {
                    ((NetVideoPlayerActivity)mContext).startVrActivity();
                }

                break;
            }
        }
    };

    private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
        int newposition = 0;
        @Override
        public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
            if (fromUser) {
                newposition = progress * 1000;
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekbar) {
            mControl.setDragging(true, 0);
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mControl.setDragging(false, newposition);
        }
    };

    private OnSeekBarChangeListener mVolumeListener = new OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mControl.setDragging(false, -1);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mControl.setDragging(true, -1);
        }
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            setVolume(progress * getMaxVolume() / 1000);

            //TODO: This is a workaround to prevent controller view from hiding.
            //VerticalSeekBar's onStopTrackingTouch and onStartTrackingTouch can't be called.
            mControl.setDragging(true, -1);
            mControl.setDragging(false, -1);
        }
    };

    private OnSeekBarChangeListener mDepthListener = new OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mControl.setDragging(false, -1);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mControl.setDragging(true, -1);
        }
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float depth;
            //TODO, fix me. Hard coding. mDepthSeekBar.setMax(1000)
            depth = (float)(mControl.getMaxPlayDepth() * progress / 1000);   // Shader.mDEPTH_MAX*(progress/MAX_se);
            mControl.setPlayDepth(depth);

            //TODO: This is a workaround to prevent controller view from hiding.
            //VerticalSeekBar's onStopTrackingTouch and onStartTrackingTouch can't be called.
            mControl.setDragging(true, -1);
            mControl.setDragging(false, -1);
        }
    };

    public void setPlayIcon(boolean play) {
        mPlayButton.setImageResource(play ? R.drawable.btn_controller_play
                : R.drawable.btn_controller_pause);
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void setDuration(int duration) {
        mDurationTextView.setText(stringForTime(duration));
        mSeekBar.setMax(duration / 1000);
    }

    public void updateProgress(int timeMs) {
        mPlayedTextView.setText(stringForTime(timeMs));
        mSeekBar.setProgress(timeMs / 1000);
    }

    public void hideController() {
        if (mExtraWindow.isShowing()) {
            mExtraWindow.dismiss();
        }

        if (mMenuDialog.isShowing()) {
            mMenuDialog.dismiss();
        }

        if (isVideo) {
            if (mControllerWindow.isShowing()) {
                mControllerWindow.dismiss();
            }

            hideVolumeWindow();
            hideDepthWindow();
        }
    }

    public void showController(View parent, int extraWindowWidth, int extraWindowHeight,
                int controlWindowWidth, int controlWindowHeight) {
        if (parent != null && parent.isShown()) {
            if (isVideo) {
                mControllerWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
                mControllerWindow.update(0, 0, controlWindowWidth, controlWindowHeight);
            }
            mExtraWindow.showAtLocation(parent, Gravity.TOP, 0, 0);
            mExtraWindow.update(EXTRA_WINDOW_POSX, EXTRA_WINDOW_POSY, extraWindowWidth, extraWindowHeight);
        }
        sExtraHeight = extraWindowHeight;

    }

    private void initVerticalWindow() {
        View volumeView = mInflater.inflate(R.layout.vertical_seekbar, null);
        mVolumeWindow = new PopupWindow(volumeView);
        mVolumeSeekBar = (VerticalSeekBar) volumeView.findViewById(R.id.vertical_seekbar);
        mVolumeSeekBar.setMax(SEEKBAR_MAX_RANGE);
        mVolumeSeekBar.setProgress(SEEKBAR_MAX_RANGE * getCurrentVolume() / getMaxVolume());
        mVolumeSeekBar.setOnSeekBarChangeListener(mVolumeListener);

        View depthView = mInflater.inflate(R.layout.vertical_seekbar, null);
        mDepthWindow = new PopupWindow(depthView);
        mDepthSeekBar = (VerticalSeekBar) depthView.findViewById(R.id.vertical_seekbar);
        mDepthSeekBar.setMax(SEEKBAR_MAX_RANGE);
        mDepthSeekBar.setProgress(0);
        mDepthSeekBar.setOnSeekBarChangeListener(mDepthListener);
    }

    public void showVolumeWindow(View parent, int height) {
        int progress = SEEKBAR_MAX_RANGE * getCurrentVolume() / getMaxVolume();
        mVolumeSeekBar.setProgress(progress);
        mVolumeWindow.showAtLocation(parent, Gravity.LEFT | Gravity.BOTTOM, 0, 0);
        int[] a = new int[2];
        mVolume.getLocationOnScreen(a);
        mVolumeWindow.update(a[0] + 20, height - a[1], 100, height / 3);
        mVolumeSeekBar.requestFocus();

        mIsVolumeShow = true;
    }

    public void hideVolumeWindow() {
        if (mVolumeWindow.isShowing()) {
            mVolumeWindow.dismiss();
            mIsVolumeShow = false;
        }
    }

    public void showDepthWindow(View parent, int height) {
        int progress = (int) ((mControl.getPlayDepth() / mControl.getMaxPlayDepth()) * SEEKBAR_MAX_RANGE);
        mDepthSeekBar.setProgress(progress);
        mDepthWindow.showAtLocation(parent, Gravity.LEFT | Gravity.BOTTOM, 0, 0);
        int[] a = new int[2];
        mDepth.getLocationOnScreen(a);
        mDepthWindow.update(a[0] + 20, height - a[1], 100, height / 3);
        mDepthSeekBar.requestFocus();

        mIsDepthShow = true;
    }

    public void hideDepthWindow() {
        if (mDepthWindow.isShowing()) {
            mDepthWindow.dismiss();
            mIsDepthShow = false;
        }
    }

    public void setLocked(boolean isLocked) {
        if (isLocked) {
            mExtraView.setVisibility(View.GONE);
            mLockButton.setImageResource(R.drawable.btn_controller_lock);
            mSeekBarLayout.setVisibility(View.INVISIBLE);
            mButtonsLayout.setVisibility(View.INVISIBLE);
            mSettingsLayout.setVisibility(View.INVISIBLE);
            mBottomLayout.setBackgroundResource(R.color.no_color);
            if (mVolumeWindow.isShowing()) {
                mVolumeWindow.dismiss();
            }
            if (mDepthWindow.isShowing()) {
                mDepthWindow.dismiss();
            }
        } else {
            mExtraView.setVisibility(View.VISIBLE);
            mLockButton.setImageResource(R.drawable.btn_controller_unlock);
            mSeekBarLayout.setVisibility(View.VISIBLE);
            mButtonsLayout.setVisibility(View.VISIBLE);
            mSettingsLayout.setVisibility(View.VISIBLE);
            mBottomLayout.setBackgroundResource(R.drawable.bg_controller);
        }
    }

    public void setNameView(String name) {
        mNameView.setText(name);
    }

    public int getMaxVolume() {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public int getCurrentVolume() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public void setVolume(int volume) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    private void setButtonChecked(int id) {
        ((RadioButton) (mExtraView.findViewById(id))).setChecked(true);
    }
}
