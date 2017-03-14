package com.evistek.gallery.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import com.evistek.gallery.R;
import com.evistek.gallery.render.RenderBase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PanelConfig {
    private static final String TAG = "PanelConfig";
    private static final String XML_FILE = "devices.xml";
    private static final String FILE = "/evis/" + XML_FILE;

    private Context mContext = null;
    private XmlParser mXmlParser = null;
    private PanelDevice mPanelDev = null;

    public static PanelConfig mInstance = null;

    private PanelConfig(Context context) {
        mContext = context;
        try {
            InputStream is;
            String externalXml = Environment.getExternalStorageDirectory() + FILE;
            File xmlFile = new File(externalXml);
            if (xmlFile.exists()) {
                Log.i(TAG, "Use external config file: " + FILE);
                is = new BufferedInputStream(
                        new FileInputStream(Environment.getExternalStorageDirectory() + FILE));
            } else {
                Log.i(TAG, "Use internal config file: " + XML_FILE);
                is = mContext.getResources().openRawResource(R.raw.devices);
            }
            mXmlParser = new XmlParser(is);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Can't open devices file: " +  FILE);
            e.printStackTrace();
        }
    }

    public synchronized static PanelConfig getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PanelConfig(context);
        }

        return mInstance;
    }

    public PanelDevice findDevice() {
        if (mPanelDev != null) {
            return mPanelDev;
        }

        PanelDevice dev = null;
        List<PanelDevice> devices = null;

        if (mXmlParser != null)
            devices = mXmlParser.parse();

        if (devices != null) {
            Iterator<PanelDevice> it = devices.iterator();
            while(it.hasNext()) {
                PanelDevice d = it.next();
                if (d.getModel().equals(Build.MODEL)) {
                    Log.i(TAG, "Current device model: " + d.getModel());
                    dev = d;
                    break;
                }
            }

            if (dev == null) {
                Log.e(TAG, "Not supported device: " + Build.MODEL + " use the dummy device");
                dev = new PanelDevice();
                dev.setModel("Dummy");
                dev.setNode("null");
                dev.setCot(0);
                dev.setCover(0);
                dev.setOffset(0);
                dev.setDrawMode(1);
                dev.setFramePacking(0);
                dev.setSizeType(0);
                dev.setSlope(false);
            }

            mPanelDev = dev;
        }

        return dev;
    }

    public class PanelDevice {
        private String mModel;
        private String mNode;
        private float mCot;
        private float mCover;
        private float mOffset;
        private boolean mSlope;
        private int mFramePacking;// 0: Left-Right;  1: Right-Left
        private int mDrawMode;// 0: draw when update; 1: draw always
        private int mSizeType;//0: full screen size (Real size); 1: size excluding navigationBar size

        public PanelDevice() {
            mModel = null;
            mNode = null;
            mCot = 0;
            mCover = 0;
            mOffset = 0;
            mSlope = true;
            mFramePacking = RenderBase.FRAME_PACKING_LEFT_RIGHT;
            mDrawMode = RenderBase.DRAW_MODE_WHEN_NEED;
            mSizeType = RenderBase.SIZE_TYPE_FULL_SCREEN;
        }

        public String getModel() {
            return mModel;
        }

        public void setModel(String mModel) {
            this.mModel = mModel;
        }

        public String getNode() {
            return mNode;
        }

        public void setNode(String mNode) {
            this.mNode = mNode;
        }

        public float getCot() {
            return mCot;
        }

        public void setCot(float mCot) {
            this.mCot = mCot;
        }

        public float getCover() {
            return mCover;
        }

        public void setCover(float mCover) {
            this.mCover = mCover;
        }

        public float getOffset() {
            return mOffset;
        }

        public void setOffset(float mOffset) {
            this.mOffset = mOffset;
        }

        public boolean isSlope() {
            return mSlope;
        }

        public void setSlope(boolean mSlope) {
            this.mSlope = mSlope;
        }

        public boolean isBarrier() {
            //Currently we consider sloping raster as barrier raster.
            return !mSlope;
        }

        public int getFramePacking() {
            return mFramePacking;
        }

        public void setFramePacking(int framePacking) {
            this.mFramePacking = framePacking;
        }

        public int getDrawMode() {
            return mDrawMode;
        }

        public void setDrawMode(int drawMode) {
            this.mDrawMode = drawMode;
        }

        public int getSizeType() {
            return mSizeType;
        }

        public void setSizeType(int sizeType) {
            this.mSizeType = sizeType;
        }
    }

    private class XmlParser {
        private static final String XML_TAG_DEVICES = "devices";
        private static final String XML_TAG_DEVICE = "device";
        private static final String XML_TAG_MODEL = "model";
        private static final String XML_TAG_NODE = "node";
        private static final String XML_TAG_COT = "cot";
        private static final String XML_TAG_COVER = "cover";
        private static final String XML_TAG_OFFSET = "offset";
        private static final String XML_TAG_SLOPE = "slope";
        private static final String XML_TAG_FRAME_PACKING = "framePacking";
        private static final String XML_TAG_DRAW_MODE = "drawMode";
        private static final String XML_TAG_SIZE_TYPE = "sizeType";

        private InputStream mInput;

        public XmlParser(InputStream is) {
            mInput = is;
        }

        public List<PanelDevice> parse() {
            List<PanelDevice> devices = null;
            PanelDevice dev = null;

            XmlPullParser parser = Xml.newPullParser();
            try {
                parser.setInput(mInput, "UTF-8");

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {

                    switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        devices = new ArrayList<PanelDevice>();
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals(XML_TAG_DEVICE)) {
                            dev = new PanelDevice();
                        } else if (parser.getName().equals(XML_TAG_MODEL)) {
                            eventType = parser.next();
                            dev.setModel(parser.getText());
                        } else if (parser.getName().equals(XML_TAG_NODE)) {
                            eventType = parser.next();
                            dev.setNode(parser.getText());
                        } else if (parser.getName().equals(XML_TAG_COT)) {
                            eventType = parser.next();
                            dev.setCot(Float.parseFloat(parser.getText()));
                        } else if (parser.getName().equals(XML_TAG_COVER)) {
                            eventType = parser.next();
                            dev.setCover(Float.parseFloat(parser.getText()));
                        } else if (parser.getName().equals(XML_TAG_OFFSET)) {
                            eventType = parser.next();
                            dev.setOffset(Float.parseFloat(parser.getText()));
                        } else if (parser.getName().equals(XML_TAG_SLOPE)) {
                            eventType = parser.next();
                            dev.setSlope(parser.getText().equals("true"));
                        } else if (parser.getName().equals(XML_TAG_FRAME_PACKING)) {
                            eventType = parser.next();
                            dev.setFramePacking(Integer.parseInt(parser.getText()));
                        } else if (parser.getName().equals(XML_TAG_DRAW_MODE)) {
                            eventType = parser.next();
                            dev.setDrawMode(Integer.parseInt(parser.getText()));
                        } else if (parser.getName().equals(XML_TAG_SIZE_TYPE)) {
                            eventType = parser.next();
                            dev.setSizeType(Integer.parseInt(parser.getText()));
                        } else if (parser.getName().equals(XML_TAG_DEVICES)) {
                            //do nothing
                        } else {
                            Log.e(TAG, "XmlParser can't parser tag: " + parser.getName());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals(XML_TAG_DEVICE)) {
                            Log.i(TAG, "mode: " + dev.getModel() +
                                       " node: " + dev.getNode() +
                                       " cot: " + dev.getCot() +
                                       " cover: " + dev.getCover() +
                                       " offset: " + dev.getOffset() +
                                       " slope: " + dev.isSlope() +
                                       " framePacking: " + dev.getFramePacking() +
                                       " drawMode: " + dev.getDrawMode() +
                                       " sizeType: " + dev.getSizeType());
                            devices.add(dev);
                            dev = null;
                        }
                        break;
                    default:
                        break;
                    }

                    eventType = parser.next();
                }

            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return devices;
        }
    }
}
