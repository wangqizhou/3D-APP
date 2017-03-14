package com.evistek.gallery.view;

public interface IControl
{
    public void scale();
    public boolean isKeepAR();
    public void play();
    public void lock();
    public void previous();
    public void next();
    public void back();
    public void setPlayMode(int mode);
    public int getPlayMode();
    public void setDragging(boolean isDragging, int newPos);
    public void startMenu(int position, int which);
    public void showVolumeWindow();
    public void hideVolumeWindow();
    public void showDepthWindow();
    public void hideDepthWindow();
    public void setPlayDepth(float depth);
    public float getPlayDepth();
    public float getMaxPlayDepth();
}
