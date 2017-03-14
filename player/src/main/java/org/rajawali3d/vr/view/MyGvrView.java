package org.rajawali3d.vr.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.rajawali3d.vr.listener.GvrTouchEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evis on 2016/9/6.
 */
public class MyGvrView extends RajawaliGvrView {
    private List<GvrTouchEventListener> listeners = new ArrayList<>();

    public MyGvrView(Context context) {
        super(context);
    }

    public MyGvrView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onTouchEvent(MotionEvent e) {
        notifyTouchEvent(e);
        return super.onTouchEvent(e);
    }

    public void addTouchEventListener(GvrTouchEventListener listener){
        listeners.add(listener);
    }

    public void notifyTouchEvent(MotionEvent e){
        for(GvrTouchEventListener listener : listeners){
            listener.onTouch(e);
        }
    }
}
