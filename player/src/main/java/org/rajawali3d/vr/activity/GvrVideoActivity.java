package org.rajawali3d.vr.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.model.LocalVideo;
import com.evistek.gallery.model.PlayRecord;
import com.evistek.gallery.model.PlayRecordModel;
import com.evistek.gallery.model.Video;
import com.evistek.gallery.net.NetWorkService;
import com.evistek.gallery.net.callback.PlayRecordCallback;
import com.evistek.gallery.net.callback.UserCallBack;
import com.evistek.gallery.user.User;
import com.evistek.gallery.utils.Utils;
import com.evistek.gallery.view.ControlLayer;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardDeviceParams;
import com.google.vrtoolkit.cardboard.Distortion;
import com.google.vrtoolkit.cardboard.FieldOfView;
import com.google.vrtoolkit.cardboard.ScreenParams;

import org.rajawali3d.vr.listener.GvrTouchEventListener;
import org.rajawali3d.vr.listener.VideoTimeListener;
import org.rajawali3d.vr.renderer.GvrVideoRenderer;
import org.rajawali3d.vr.view.MyGvrView;
import org.rajawali3d.vr.view.RajawaliGvrView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by evis on 2016/9/6.
 */
public class GvrVideoActivity extends CardboardActivity implements VideoTimeListener, GvrTouchEventListener {
    private static final String TAG = "VideoActivity";
    public static final String INTENT_NET_VIDEO = "net_video";
    public static final String INTENT_VIDEO_LIST = "video_list";
    public static final String INTENT_LOCAL_VIDEO = "local_video";
    public static final String INTENT_LOCAL_PATH = "local_path";
    public static final String INTENT_PLAY_PATTERN = "pattern";
    private static final int MSG_TOTAL_TIME = 0;
    private static final int MSG_ELAPSED_TIME = 1;
    private static final int MSG_HIDE_PROGRESSBAR = 2;
    private static final int MSG_SHOW_PROGRESSBAR = 3;
    private static final int MSG_ENABLE_SEEKBAR = 4;
    private static final int MSG_PLAYER_FAILURE = 5;
    private static final int MSG_HIDE_PAUSE = 6;
    private static final int MSG_SHOW_PAUSE = 7;
    private static final String DEFAULT_TIME = "00:00:00";

    private static final int[] PLAY_PATTERN_1 = {
            GvrVideoRenderer.PLAY_MODE_VR,
            GvrVideoRenderer.PLAY_MODE_FULLSCREEN,
            GvrVideoRenderer.PLAY_MODE_3D,
            GvrVideoRenderer.PLAY_MODE_2D
    };

    private static final int[] PLAY_PATTERN_2 = {
            GvrVideoRenderer.PLAY_MODE_VR,
            GvrVideoRenderer.PLAY_MODE_FULLSCREEN,
            GvrVideoRenderer.PLAY_MODE_2D
    };

    private static final int[] PLAY_PATTERN_3 = {
            GvrVideoRenderer.PLAY_MODE_3D,
            GvrVideoRenderer.PLAY_MODE_VR,
            GvrVideoRenderer.PLAY_MODE_FULLSCREEN,
            GvrVideoRenderer.PLAY_MODE_2D
    };

    public static final int PATTERN_TYPE_1 = 1;
    public static final int PATTERN_TYPE_2 = 2;
    public static final int PATTERN_TYPE_3 = 3;

    private static final String BT_DEVICE_VR_PARK = "VR-PARK";

    private Context context;
    private MyGvrView view;
    private ControlLayer controlLayer;
    private GvrVideoRenderer renderer;
    private Video video;
    private LocalVideo localVideo;
    private int videoDuration;
    private int playedTime;
    private Handler handler;
    private Handler inputHandler;
    private String uriString;
    private int playMode = GvrVideoRenderer.PLAY_MODE_VR;
    private int playPatternType;
    private int[] playPattern;
    private int index;
    private User user;
    private PlayRecord playRecord;
    private ArrayList<Video> videoList = new ArrayList<>();
    private ArrayList<LocalVideo> localVideoList = new ArrayList<>();
    private int videoIndex;
    private String btDevice = "unknown";
    private Map<Integer, Integer> vrparkMap = new HashMap<Integer, Integer>();
    private AudioManager audioManager;
    private boolean isModeChanging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        view = new MyGvrView(this);
        view.setSettingsButtonEnabled(false);
        view.setVRModeEnabled(true);
        view.setDistortionCorrectionEnabled(false);
        view.setAlignmentMarkerEnabled(false);
        setContentView(view);
        setCardboardView(view);

        user = E3DApplication.getInstance().getUser();
        receiveIntent();

        renderer = new GvrVideoRenderer(GvrVideoActivity.this, Uri.parse(uriString), playMode);
        view.setRenderer(renderer);
        view.setSurfaceRenderer(renderer);
        renderer.setVideoTimeListener(this);
        view.addTouchEventListener(renderer);
        view.addTouchEventListener(this);

        initControllerView();

        handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {

                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                /*
                    subtract time zone, otherwise we will get 08:05:10 instead of 00:05:10.
                 */
                int time = msg.arg1 - TimeZone.getDefault().getRawOffset();

                switch (msg.what) {
                    case MSG_ELAPSED_TIME:
                        controlLayer.setElapsedTime(formatter.format(time));
                        break;
                    case MSG_TOTAL_TIME:
                        controlLayer.setTotalTime(formatter.format(time));
                        break;
                    case MSG_SHOW_PROGRESSBAR:
                        controlLayer.setProgressBarVisibility(View.VISIBLE);
                        break;
                    case MSG_HIDE_PROGRESSBAR:
                        controlLayer.setProgressBarVisibility(View.INVISIBLE);
                        break;
                    case MSG_HIDE_PAUSE:
                        controlLayer.setPauseViewVisibility(View.INVISIBLE);
                        break;
                    case MSG_SHOW_PAUSE:
                        controlLayer.setPauseViewVisibility(View.VISIBLE);
                        break;
                    case MSG_ENABLE_SEEKBAR:
                        controlLayer.setSeekBarEnable(true);
                        controlLayer.setSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    renderer.setMediaPlayerSeekTo(progress);
                                    seekBar.setProgress(progress);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                        break;
                    case MSG_PLAYER_FAILURE:
                        controlLayer.setProgressBarVisibility(View.INVISIBLE);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(R.string.player_init_fail);
                        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case AlertDialog.BUTTON_POSITIVE:
                                        finish();
                                        break;
                                }
                            }
                        };
                        builder.setPositiveButton(R.string.ok, listener);
                        AlertDialog ad = builder.create();
                        ad.show();
                        break;
                    default:
                        break;
                }

                return true;
            }
        });

        inputHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                switch (msg.what) {

                }

                return false;
            }
        });

        checkBluetoothDevice();
        printParameters(view);
        //updateParameters(view);
    }

    @Override
    public void onBackPressed() {

        if (playRecord != null) {
            updatePlayRecord(playRecord);
        }

        super.onBackPressed();
    }

    private void receiveIntent() {
        video =  (Video) getIntent().getSerializableExtra(INTENT_NET_VIDEO);
        localVideo = (LocalVideo) getIntent().getSerializableExtra(INTENT_LOCAL_VIDEO);
        if (video != null) {
            //local physical path
            uriString = getIntent().getStringExtra(INTENT_LOCAL_PATH);
            videoList = (ArrayList<Video>) getIntent().getSerializableExtra(INTENT_VIDEO_LIST);
            for (int i = 0; i < videoList.size(); i++) {
                if (videoList.get(i).getUrl().equals(video.getUrl())) {
                    videoIndex = i;
                }
            }

            initPlayRecord(video);
        } else if (localVideo != null) {
            uriString = localVideo.path;
            localVideoList = (ArrayList<LocalVideo>) getIntent().getSerializableExtra(INTENT_VIDEO_LIST);
            for (int i = 0; i < localVideoList.size(); i++) {
                if (localVideoList.get(i).id == localVideo.id) {
                    videoIndex = i;
                }
            }
            playRecord = null;
        }

        playPatternType = getIntent().getIntExtra(INTENT_PLAY_PATTERN, PATTERN_TYPE_1);
        initPlayPattern(playPatternType);
        if (playMode != GvrVideoRenderer.PLAY_MODE_VR) {
            view.setVRModeEnabled(false);
        }
    }

    private void initControllerView() {
        controlLayer = new ControlLayer(this);
        controlLayer.setModeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlayMode();
            }
        });

        addContentView(controlLayer.getView(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        controlLayer.updateButtonView(playMode);
    }

    private void printParameters(RajawaliGvrView gvrView) {

        // CardboardView Params
        int width = gvrView.getWidth();
        int height = gvrView.getHeight();
        float interpupillaryDistance = gvrView.getInterpupillaryDistance();
        float cameraDistance = gvrView.getCameraDistance();

        Log.e(TAG, "=============CardboardView Params===============");
        Log.e(TAG, "Width: " + width);
        Log.e(TAG, "Height: " + height);
        Log.e(TAG, "InterpupillaryDistance: " + interpupillaryDistance);
        Log.e(TAG, "CameraDistance: " + cameraDistance);

        // Device parameters
        CardboardDeviceParams viewerParams = gvrView.getCardboardDeviceParams();
        String model = viewerParams.getModel();
        String vendor = viewerParams.getVendor();
        Distortion distortion = viewerParams.getDistortion();
        float[] coefficients = distortion.getCoefficients();
        float interLensDistance = viewerParams.getInterLensDistance();
        float screenToLenDistance = viewerParams.getScreenToLensDistance();
        FieldOfView fov = viewerParams.getLeftEyeMaxFov();

        Log.e(TAG, "=============DeviceParams===============");
        Log.e(TAG, "Model: " + model);
        Log.e(TAG, "Vendor: " + vendor);
        Log.e(TAG, "Distortion k1: " + coefficients[0] + " k2: " + coefficients[1]);
        Log.e(TAG, "InterLensDistance: " + interLensDistance);
        Log.e(TAG, "ScreenToLenDistance: " + screenToLenDistance);
        Log.e(TAG, "Fov l: " + fov.getLeft() + " t: " + fov.getTop() +
                " r: " + fov.getRight() + " b: " + fov.getBottom());

        // Screen Parameters
        ScreenParams screenParams = gvrView.getScreenParams();
        int screenWidth = screenParams.getWidth();
        int screenHeight = screenParams.getHeight();
        float screenWidthMeter = screenParams.getWidthMeters();
        float screenHeightMeter = screenParams.getHeightMeters();

        Log.e(TAG, "=============ScreenParams===============");
        Log.e(TAG, "Width: " + screenWidth + " WidthMeters: " + screenWidthMeter);
        Log.e(TAG, "Height: " + screenHeight + " HeightMeters: " + screenHeightMeter);
    }

    private void updateParameters(RajawaliGvrView gvrView) {
        CardboardDeviceParams viewerParams = gvrView.getCardboardDeviceParams();
        Distortion distortion = viewerParams.getDistortion();
        distortion.setCoefficients(new float[]{0.30f, 0.30f});
        gvrView.updateCardboardDeviceParams(viewerParams);
    }

    private void initPlayPattern(int patternType) {
        switch (patternType) {
            case PATTERN_TYPE_1:
                playPattern = PLAY_PATTERN_1;
                break;
            case PATTERN_TYPE_2:
                playPattern = PLAY_PATTERN_2;
                break;
            case PATTERN_TYPE_3:
                playPattern = PLAY_PATTERN_3;
                break;
        }

        index = 0;
        playMode = playPattern[index];
    }

    private void changePlayMode() {
        int tempIndex = (index + 1) % playPattern.length;
        int next = playPattern[tempIndex];

        switch (next) {
            case GvrVideoRenderer.PLAY_MODE_FULLSCREEN:
                if (renderer.setPlayMode(GvrVideoRenderer.PLAY_MODE_FULLSCREEN)) {
                    view.setVRModeEnabled(false);
                    playMode = next;
                    index = tempIndex;
                } else {
                    Toast.makeText(context, R.string.buffering, Toast.LENGTH_SHORT).show();
                }
                break;
            case GvrVideoRenderer.PLAY_MODE_3D:
                if (renderer.setPlayMode(GvrVideoRenderer.PLAY_MODE_3D)) {
                    view.setVRModeEnabled(false);
                    playMode = next;
                    index = tempIndex;
                } else {
                    Toast.makeText(context, R.string.buffering, Toast.LENGTH_SHORT).show();
                }
                break;
            case GvrVideoRenderer.PLAY_MODE_2D:
                if (renderer.setPlayMode(GvrVideoRenderer.PLAY_MODE_2D)) {
                    view.setVRModeEnabled(false);
                    playMode = next;
                    index = tempIndex;
                } else {
                    Toast.makeText(context, R.string.buffering, Toast.LENGTH_SHORT).show();
                }
                break;
            case GvrVideoRenderer.PLAY_MODE_VR:
                if (renderer.setPlayMode(GvrVideoRenderer.PLAY_MODE_VR)) {
                    view.setVRModeEnabled(true);
                    playMode = next;
                    index = tempIndex;
                } else {
                    Toast.makeText(context, R.string.buffering, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }

        controlLayer.updateLayout(next);
    }

    @Override
    public void onVideoInit(int length) {
        controlLayer.setSeekBarMax(length);
        videoDuration = length;

        NetWorkService.updatePlayCount(uriString);
        if (playRecord != null) {
            addPlayRecord(playRecord);
        }

        sendMessage(MSG_TOTAL_TIME, length);
        sendMessage(MSG_HIDE_PROGRESSBAR, 0);
        sendMessage(MSG_ENABLE_SEEKBAR, 0);
    }

    @Override
    public void listenTime(int time) {
        controlLayer.setSeekBarProgress(time);
        sendMessage(MSG_ELAPSED_TIME, time);

        if (time > playedTime) {
            sendMessage(MSG_HIDE_PROGRESSBAR, 0);
        } else if (time == playedTime) {
            sendMessage(MSG_SHOW_PROGRESSBAR, 0);
        }

        playedTime = time;
    }

    @Override
    public void onBufferedPercent(int percent) {
        controlLayer.setSeekBarSecondaryProgress(videoDuration * percent / 100);
    }

    @Override
    public void onInitializationFail() {
        sendMessage(MSG_PLAYER_FAILURE, 0);
    }

    @Override
    public void onTouch(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            int visibility = controlLayer.getVisibility();
            if (visibility == View.INVISIBLE) {
                controlLayer.setVisibility(View.VISIBLE);
            } else if (visibility == View.VISIBLE) {
                controlLayer.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void sendMessage(int what, int arg1) {
        Message msg = handler.obtainMessage();
        msg.what = what;
        msg.arg1 = arg1;
        handler.sendMessage(msg);
    }

    private void initPlayRecord(Video video) {
        if (user.isLogin) {
            playRecord = new PlayRecord();
            playRecord.setUserId(user.id);
            playRecord.setVideoId(video.getId());
//            playRecord.setCoverUrl(video.getPreview1Url());
            playRecord.setVideoName(video.getName());
            playRecord.setClient(getPackageName());
            try {
                playRecord.setClientVersion(
                        getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).versionName);
            } catch (PackageManager.NameNotFoundException e) {
                playRecord.setClientVersion("");
                e.printStackTrace();
            }
        }
    }

    private void addPlayRecord(PlayRecord playRecord) {
        if (Utils.isNetworkAvailable()) {
            NetWorkService.addPlayRecord(playRecord, new PlayRecordCallback() {
                @Override
                public void onResult(int code, PlayRecordModel msg) {
                    Log.e(TAG, "addPlayRecord code: " + code + " msg: " + msg);
                }
            });
        }
    }

    private void updatePlayRecord(PlayRecord playRecord) {
        if (Utils.isNetworkAvailable()) {
            NetWorkService.updatePlayRecordDuration(playRecord, new UserCallBack() {
                @Override
                public void onResult(int code, String msg) {
                    Log.e(TAG, "updatePlayRecord code: " + code + " msg: " + msg);
                }
            });
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (renderer == null) {
            return super.onKeyDown(keyCode, event);
        }

        boolean handled = false;
        String path;
        switch (mapKeycode(keyCode, btDevice)) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                handled = true;

                if (!isModeChanging) {
                    if (renderer.isPlaying()) {
                        sendMessage(MSG_SHOW_PAUSE, 0);
                    } else {
                        sendMessage(MSG_HIDE_PAUSE, 0);
                    }

                    renderer.pause();
                }
                isModeChanging = false;
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                handled = true;
                path = getPreviousVideo();
                if (path != null) {
                    sendMessage(MSG_HIDE_PAUSE, 0);
                    sendMessage(MSG_SHOW_PROGRESSBAR, 0);
                    renderer.play(path);
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                handled = true;
                path = getNextVideo();
                if (path != null) {
                    sendMessage(MSG_HIDE_PAUSE, 0);
                    sendMessage(MSG_SHOW_PROGRESSBAR, 0);
                    renderer.play(path);
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                handled = true;
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                handled = true;
                break;
        }

        if (handled) {
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (renderer == null) {
            return super.onKeyDown(keyCode, event);
        }

        boolean handled = false;
        switch (mapKeycode(keyCode, btDevice)) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                handled = true;
                if (event.isLongPress()) {
                    isModeChanging = true;
                    changePlayMode();
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                handled = true;
                renderer.rewind(playedTime - videoDuration / 300);
                break;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                handled = true;
                renderer.fastForward(playedTime + videoDuration / 300);
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                handled = true;
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                handled = true;
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                handled = true;
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                handled = true;
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
                break;
        }

        if (handled) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private String getNextVideo() {
        if (videoList != null && videoList.size() > 0) {
            videoIndex = (videoIndex + 1) % videoList.size();
            return videoList.get(videoIndex).getUrl();
        }

        if (localVideoList != null && localVideoList.size() > 0) {
            videoIndex = (videoIndex + 1) % localVideoList.size();
            return localVideoList.get(videoIndex).path;
        }

        return null;
    }

    private String getPreviousVideo() {
        if (videoList != null && videoList.size() > 0) {
            if (--videoIndex < 0) {
                videoIndex = videoList.size() - 1;
            }
            return videoList.get(videoIndex).getUrl();
        }

        if (localVideoList != null && localVideoList.size() > 0) {
            if (--videoIndex < 0) {
                videoIndex = localVideoList.size() - 1;
            }
            return localVideoList.get(videoIndex).path;
        }

        return null;
    }

    private void checkBluetoothDevice() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> deviceSet = btAdapter.getBondedDevices();
        for (BluetoothDevice device: deviceSet) {
            if (device.getName().equals(BT_DEVICE_VR_PARK)) {
                btDevice = device.getName();
                initKeycodeMap(btDevice);
            }

            Log.i(TAG, "BT name: " + device.getName() +
                    " type: " + device.getType() +
                    " state: " + device.getBondState());
        }
    }

    private void initKeycodeMap(String device) {
        switch (device) {
            case BT_DEVICE_VR_PARK:
                vrparkMap.put(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, KeyEvent.KEYCODE_MEDIA_REWIND);
                vrparkMap.put(KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD);
                break;
            default:
                break;
        }
    }

    private int mapKeycode(int keycode, String device) {
        int mappedKeycode = keycode;
        switch (device) {
            case BT_DEVICE_VR_PARK:
                if (vrparkMap.containsKey(keycode)) {
                    mappedKeycode = vrparkMap.get(keycode);
                }
                break;
            default:
                break;
        }

        return mappedKeycode;
    }
}
