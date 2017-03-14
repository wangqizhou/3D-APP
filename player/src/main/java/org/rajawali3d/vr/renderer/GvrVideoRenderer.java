package org.rajawali3d.vr.renderer;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.evistek.gallery.R;
import com.google.vrtoolkit.cardboard.Eye;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.vr.activity.GvrVideoActivity;
import org.rajawali3d.vr.listener.GvrTouchEventListener;
import org.rajawali3d.vr.listener.VideoTimeListener;

import java.io.IOException;

/**
 * Created by evis on 2016/9/6.
 */
public class GvrVideoRenderer extends RajawaliGvrRenderer implements GvrTouchEventListener {

    private static final String TAG = "VideoRenderer";
    public static final int PLAY_MODE_VR = 0;
    public static final int PLAY_MODE_FULLSCREEN = 1;
    public static final int PLAY_MODE_3D = 2;
    public static final int PLAY_MODE_2D = 3;
    public static final int PLAY_MODE_NUM = 4;

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private GvrVideoActivity gvrVideoActivity;
    private Uri videoUri;
    private int mode = NONE;
    private float posX1, posX2, posY1, posY2;
    private float distance1, distance2;
    private VideoTimeListener listener;
    private MediaPlayer mediaPlayer;
    private boolean isMediaPlayerError;
    private StreamingTexture streamingTexture;
    private boolean vrMode;
    private boolean enableTouchControl;
    private Material material;
    private long elapsedRealtime;
    private double deltaTime;
    private Matrix4 eyeMatrix = new Matrix4();
    private Quaternion eyeQuaternion = new Quaternion();
    private Quaternion rotateQuaternion = new Quaternion();
    private Sphere sphere;
    private ScreenQuad screenQuad;
    private int playMode;
    private int surfaceHeight;
    private int surfaceWidth;

    private float angleX = 0;
    private float angleY = 0;
    private float angleZ = 0;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    public GvrVideoRenderer(Activity activity) {
        super(activity.getApplicationContext());
        gvrVideoActivity = (GvrVideoActivity) activity;
        playMode = PLAY_MODE_VR;
        isMediaPlayerError = false;
    }

    public GvrVideoRenderer(Activity activity, Uri videoUri, int playMode) {
        super(activity.getApplicationContext());
        this.videoUri = videoUri;
        gvrVideoActivity = (GvrVideoActivity) activity;
        this.playMode = playMode;
    }

    public void enableTouchControl(boolean enable) {
        enableTouchControl = enable;
    }

    public boolean setPlayMode(int mode) {
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            return false;
        }

        getCurrentScene().clearChildren();

        switch(mode) {
            case PLAY_MODE_3D:
                screenQuad.setScaleY(0.5);
                getCurrentScene().addChild(screenQuad);
                break;
            case PLAY_MODE_VR:
                getCurrentScene().addChild(sphere);
                break;
            case PLAY_MODE_FULLSCREEN:
                getCurrentScene().addChild(sphere);
                break;
            case PLAY_MODE_2D:
                //screenQuad.setScaleX(2);
                //screenQuad.moveRight(0.5);
                adjustAspectRatio(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
                screenQuad.setScaleX(scaleX);
                screenQuad.setScaleY(scaleY);
                getCurrentScene().addChild(screenQuad);
                break;
            default:
                break;
        }

        return true;
    }

    private void initObject() {
        sphere = new Sphere(100, 128, 64);
        sphere.setScaleX(-1);
        sphere.setMaterial(material);

        screenQuad = new ScreenQuad(true);
        screenQuad.setMaterial(material);
        screenQuad.setScaleY(0.5);

        if (playMode == PLAY_MODE_VR) {
            getCurrentScene().addChild(sphere);
            getCurrentCamera().setPosition(Vector3.ZERO);
            getCurrentCamera().setFieldOfView(75);
        } else if (playMode == PLAY_MODE_3D) {
            getCurrentScene().addChild(screenQuad);
        }
    }

    @Override
    protected void initScene() {
        mediaPlayer = MediaPlayer.create(getContext(), videoUri);
        if (mediaPlayer == null) {
            Log.e(TAG, "Player initialization failed!");
            notifyInitializationFail();
            return;
        }

        streamingTexture = new StreamingTexture("texture", mediaPlayer);
        material = new Material();
        material.setColorInfluence(0);
        try {
            material.addTexture(streamingTexture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        initObject(); // initialize Sphere and ScreenQuad

        mediaPlayer.start();
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i("Media Player Status", "Completed");
                mp.stop();
                mp.release();
                gvrVideoActivity.finish();
            }
        });

        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                notifyBufferedTime(percent);
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                isMediaPlayerError = true;
                Toast.makeText(gvrVideoActivity, R.string.loading_fail, Toast.LENGTH_LONG).show();
                Log.e(TAG, "MediaPlayer onError what: " + what + " extra: " +extra + " isPlaying: " + mp.isPlaying());
                return false;
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                notifyVideoInit(mediaPlayer.getDuration());
                mp.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            }
        });

        notifyVideoInit(mediaPlayer.getDuration());
    }

    public void setVideoTimeListener(VideoTimeListener listener) {
        this.listener = listener;
    }

    public void notifyVideoInit(int length) {
        listener.onVideoInit(length);
    }

    public void notifyTime(int time) {
        listener.listenTime(time);
    }

    public void notifyBufferedTime(int percent) {
        listener.onBufferedPercent(percent);
    }

    public void notifyInitializationFail() { listener.onInitializationFail(); }

    public void setMediaPlayerSeekTo(int progress) {
        mediaPlayer.seekTo(progress);
    }

    @Override
    protected void onRender(long elapsedRealTime, double deltaTime) {
        this.elapsedRealtime = elapsedRealTime;
        this.deltaTime = deltaTime;
        streamingTexture.update();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            notifyTime(mediaPlayer.getCurrentPosition());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!isMediaPlayerError) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);
        surfaceWidth = width;
        surfaceHeight = height;
    }

    @Override
    public void onRenderSurfaceDestroyed(SurfaceTexture surfaceTexture) {
        super.onRenderSurfaceDestroyed(surfaceTexture);
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    public void onTouch(MotionEvent e) {
        if (!enableTouchControl) {
            return;
        }

        int action = e.getAction();
        double fieldOfView = getCurrentCamera().getFieldOfView();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                posX1 = e.getX();
                posY1 = e.getY();
                mode = DRAG;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    posX2 = e.getX();
                    posY2 = e.getY();

                    if (posX2 - posX1 > 0) {
                        angleX = angleX + (posX2 - posX1) / 10;
                    } else if (posX1 - posX2 > 0) {
                        angleX = angleX - (posX1 - posX2) / 10;
                    }
                    if (posY2 - posY1 > 0) {
                        angleY = angleY + (posY2 - posY1) / 10;
                    } else if (posY1 - posY2 > 0) {
                        angleY = angleY - (posY1 - posY2) / 10;
                    }

                    if (Math.abs(posX2 - posX1) > 15 || Math.abs(posY2 - posY1) > 15) {
                        posX1 = posX2;
                        posY1 = posY2;
                    }
                } else if (mode == ZOOM) {
                    distance1 = calculateDistance(e);
                    if (distance1 - distance2 > 0) {
                        if (fieldOfView < 130) {
                            fieldOfView = fieldOfView + (distance1 - distance2) / 10;
                            getCurrentCamera().setFieldOfView(fieldOfView);
                        }
                        distance2 = distance1;
                    } else if (distance2 - distance1 > 0) {
                        if (fieldOfView > 20) {
                            fieldOfView = fieldOfView - (distance2 - distance1) / 10;
                            getCurrentCamera().setFieldOfView(fieldOfView);
                        }
                        distance2 = distance1;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                distance1 = calculateDistance(e);
                distance2 = calculateDistance(e);
                break;
            case MotionEvent.ACTION_CANCEL:
            default:
                break;
        }
    }

    private float calculateDistance(MotionEvent e) {
        float x = e.getX(0) - e.getX(1);
        float y = e.getY(0) - e.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public void onDrawEye(Eye eye) {
        eyeMatrix.setAll(eye.getEyeView());
        eyeQuaternion.fromMatrix(eyeMatrix);
        rotateQuaternion.fromEuler(angleX, angleY, angleZ);
        getCurrentCamera().setOrientation(eyeQuaternion);
        getCurrentCamera().rotate(rotateQuaternion);
        render(elapsedRealtime, deltaTime);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void pause() {
        if (mediaPlayer != null)
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
    }

    public void fastForward(int msec) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(msec);
        }
    }

    public void rewind(int msec) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(msec);
        }
    }

    public void play(String uri) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(uri);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void adjustAspectRatio(int videoWidth,int videoHeight) {
        int viewWidth = surfaceWidth;
        int viewHeight = surfaceHeight;
        double aspectRatio = (double) videoHeight / (double)videoWidth;

        if (surfaceHeight > (int) ((double)surfaceWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            viewWidth = surfaceWidth;
            viewHeight = (int) ((double)surfaceWidth * aspectRatio);
        } else if(surfaceHeight == (int) ((double)surfaceWidth * aspectRatio)) {
            viewWidth = surfaceWidth;
            viewHeight = surfaceHeight;
        } else {
            // limited by short height; restrict width
            viewWidth = (int) ((double)surfaceHeight / aspectRatio);
            viewHeight = surfaceHeight;
        }

        scaleX = viewWidth * 1.0f / surfaceWidth;
        scaleY = viewHeight * 1.0f / surfaceHeight;
    }
}
