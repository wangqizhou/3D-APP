package org.rajawali3d.vr.listener;

/**
 * Created by neo-202 on 2016-03-23.
 */
public interface VideoTimeListener {
    void onVideoInit(int length);
    void listenTime(int time);
    void onBufferedPercent(int percent);
    void onInitializationFail();
}
