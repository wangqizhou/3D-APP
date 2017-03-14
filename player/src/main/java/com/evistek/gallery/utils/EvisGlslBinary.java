package com.evistek.gallery.utils;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class EvisGlslBinary {
    private static final String TAG = "EvisGlslBinary";

    public static final String GLSL_TYPE_IMAGE2D = "IMAGE2D";
    public static final String GLSL_TYPE_IMAGE2D3D = "IMAGE2D3D";
    public static final String GLSL_TYPE_IMAGE3D = "IMAGE3D";
    public static final String GLSL_TYPE_IMAGE3D2D = "IMAGE3D2D";
    public static final String GLSL_TYPE_VIDEO2D = "VIDEO2D";
    public static final String GLSL_TYPE_VIDEO2D3D = "VIDEO2D3D";
    public static final String GLSL_TYPE_VIDEO3D = "VIDEO3D";
    public static final String GLSL_TYPE_VIDEO3D2D = "VIDEO3D2D";

    public static final String DEFAULT_FILE_NAME = "EvisGlsl.bin";

    private String mHeaderFlag = "EVISGLSL";
    private static int HEADER_FLAG_LEN = 8;

    private String mHeaderGPUType = "";
    private static int HEADER_GPU_TYPE_LEN = 8;

    private int mHeaderSize;
    private static int HEADER_SIZE_LEN = 8;

    private int mHeaderItemNumber = 0;
    private static int HEADER_ITEM_NUMBER_LEN = 8;

    private static int HEADER_GLSL_TYPE_LEN = 16;
    private static int HEADER_GLSL_SIZE_LEN = 8;
    private static int HEADER_GLSL_OFFSET_LEN = 8;
    private static int HEADER_GLSL_ITEM_INFO_LEN =
                        HEADER_GLSL_TYPE_LEN + HEADER_GLSL_SIZE_LEN + HEADER_GLSL_OFFSET_LEN;

    private int mCurrentDataOffset;
    private int mCurrentTotalLength;

    private byte[] mBuffer = null;
    private ArrayList<GlslItem> mItems = null;

    public EvisGlslBinary() {
        mItems = new ArrayList<EvisGlslBinary.GlslItem>();

        mHeaderItemNumber = 0;

        mHeaderSize = HEADER_FLAG_LEN + HEADER_GPU_TYPE_LEN
                        + HEADER_SIZE_LEN + HEADER_ITEM_NUMBER_LEN;
        mCurrentDataOffset = mHeaderSize;

        mCurrentTotalLength = mHeaderSize;
    }

    public void allocateBuffer(int size) {
        mBuffer = new byte[size];
    }

    public byte[] getBuffer() {
        return mBuffer;
    }

    public void setGPUType(String gpu) {
        mHeaderGPUType = gpu;
    }

    public void addItem(GlslItem item) {
        mHeaderItemNumber++;
        mHeaderSize += HEADER_GLSL_ITEM_INFO_LEN;
        mCurrentDataOffset += HEADER_GLSL_ITEM_INFO_LEN;

        if (!mItems.isEmpty()) {
            for (GlslItem i: mItems) {
                i.mOffset += HEADER_GLSL_ITEM_INFO_LEN;
            }
        }

        item.mOffset = mCurrentDataOffset;
        mItems.add(item);

        mCurrentDataOffset += item.mLength;
        mCurrentTotalLength += (HEADER_GLSL_ITEM_INFO_LEN + item.mLength);
    }

    public int write(String fileName) {
        mBuffer = new byte[mCurrentTotalLength];
        int dstPos = 0;
        int copyLen = 0;

        //Copy header flag "EVISGLSL"
        copyLen = mHeaderFlag.length();
        System.arraycopy(mHeaderFlag.getBytes(), 0, mBuffer, dstPos, copyLen);
        dstPos += HEADER_FLAG_LEN;

        //Copy GPU Type
        copyLen = mHeaderGPUType.length();
        System.arraycopy(mHeaderGPUType.getBytes(), 0, mBuffer, dstPos, copyLen);
        dstPos += HEADER_GPU_TYPE_LEN;

        //Copy HeaderSize
        byte[] tempBuf = intToByte(mHeaderSize);
        copyLen = tempBuf.length;
        System.arraycopy(tempBuf, 0, mBuffer, dstPos, copyLen);
        dstPos += HEADER_SIZE_LEN;

        //Copy GLSL Item Number
        tempBuf = intToByte(mHeaderItemNumber);
        copyLen = tempBuf.length;
        System.arraycopy(tempBuf, 0, mBuffer, dstPos, copyLen);
        dstPos += HEADER_ITEM_NUMBER_LEN;

        //Copy GLSL Item
        for (int i = 0; i < mHeaderItemNumber; i++) {
            GlslItem item = mItems.get(i);

            //Copy type
            copyLen = item.mName.length();
            System.arraycopy(item.mName.getBytes(), 0, mBuffer, dstPos, copyLen);
            dstPos += HEADER_GLSL_TYPE_LEN;

            //Copy GLSL size
            tempBuf = intToByte(item.mLength);
            copyLen = tempBuf.length;
            System.arraycopy(tempBuf, 0, mBuffer, dstPos, copyLen);
            dstPos += HEADER_GLSL_SIZE_LEN;

            //Copy GLSL offset
            tempBuf = intToByte(item.mOffset);
            copyLen = tempBuf.length;
            System.arraycopy(tempBuf, 0, mBuffer, dstPos, copyLen);
            dstPos += HEADER_GLSL_OFFSET_LEN;
        }

        //Copy GLSL Data
        for (GlslItem item: mItems) {
            Log.i(TAG, "write name: " + item.mName + " len: " + item.mLength + " offset: " + item.mOffset);
            System.arraycopy(item.mData, 0, mBuffer, item.mOffset, item.mLength);
        }

        writeToSdcard(mBuffer, fileName);
        return 0;
    }

    public int write() {
        return write(DEFAULT_FILE_NAME);
    }

    public int read(byte[] srcBuffer) {
        parseGlslInfo(srcBuffer);
        return 0;
    }

    public int parse() {
        parseGlslInfo(mBuffer);
        return 0;
    }

    public int getItemOffset(String name) {
        if (!mItems.isEmpty()) {
            for (GlslItem item: mItems) {
                if (item.mName.equals(name))
                    return item.mOffset;
            }
        }

        return -1;
    }

    public int getItemLength(String name) {
        if (!mItems.isEmpty()) {
            for (GlslItem item: mItems) {
                if (item.mName.equals(name))
                    return item.mLength;
            }
        }

        return -1;
    }

    public int getItemNumber() {
        return mItems.size();
    }

    private byte[] intToByte(int value) {
        byte[] buffer = new byte[4];

        buffer[3] = (byte)((value >> 24) & 0xFF);
        buffer[2] = (byte)((value >> 16) & 0xFF);
        buffer[1] = (byte)((value >> 8) & 0xFF);
        buffer[0] = (byte)(value & 0xFF);

        return buffer;
    }

    private static int byteToInt(byte[] buffer) {
        int number = 0;

        number |= ((buffer[0] << 0) & 0xFF);
        number |= ((buffer[1] << 8) & 0xFF00);
        number |= ((buffer[2] << 16) & 0xFF0000);
        number |= ((buffer[3] << 24) & 0xFF000000);
        return number;
    }

    private void writeToSdcard(byte[] shaderbin, String fileName)
    {
        try {
            String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(sdPath + "/" + fileName);
            OutputStream out = null;
            out = new FileOutputStream(file, false);
            for (byte temp : shaderbin)
                out.write(temp);
            out.close();
        } catch (IOException x) {
            throw new RuntimeException("Sorry not able to read filename:");
        }
    }

    private void parseGlslInfo(final byte[] buffer) {
        ByteArrayInputStream is = new ByteArrayInputStream(buffer);
        byte[] tempBuffer = new byte[16];

        is.skip(HEADER_FLAG_LEN + HEADER_GPU_TYPE_LEN + HEADER_SIZE_LEN);

        is.read(tempBuffer, 0, HEADER_ITEM_NUMBER_LEN);
        int itemNum = byteToInt(tempBuffer);
        Arrays.fill(tempBuffer, (byte) '\0');

        for (int i = 0; i < itemNum; i++) {
            is.read(tempBuffer, 0, HEADER_GLSL_TYPE_LEN);
            String name = "";
            for (int j = 0; j < HEADER_GLSL_TYPE_LEN; j++) {
                if (tempBuffer[j] != (byte)0) {
                    name += Character.valueOf((char) tempBuffer[j]);
                }
            }
            Arrays.fill(tempBuffer, (byte) 0);

            is.read(tempBuffer, 0, HEADER_GLSL_SIZE_LEN);
            int length = byteToInt(tempBuffer);
            Arrays.fill(tempBuffer, (byte) 0);

            is.read(tempBuffer, 0, HEADER_GLSL_OFFSET_LEN);
            int offset = byteToInt(tempBuffer);
            Arrays.fill(tempBuffer, (byte) 0);

            Log.i(TAG, "name: " + name + " length: " + length + " offset: " + offset);
            mItems.add(new GlslItem(name, offset, length));
        }

        try {
            is.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Save buffer
        if (mBuffer == null || mBuffer.length < buffer.length) {
            mBuffer = new byte[buffer.length];
        }
        System.arraycopy(buffer, 0, mBuffer, 0, buffer.length);
    }

    public static class GlslItem {
        public String mName;
        public byte[] mData;
        public int mLength;
        public int mOffset;

        public GlslItem(String name, byte[] data, int actualLenght) {
            mName = name;
            if (data.length >= actualLenght) {
                mData = new byte[actualLenght];
                System.arraycopy(data, 0, mData, 0, actualLenght);
                mLength = actualLenght;
            } else {
                Log.e(TAG, "data buffer size is error. name: " +  name);
            }
        }

        public GlslItem(String name, int offset, int length) {
            mName = name;
            mOffset = offset;
            mLength = length;
            mData = null;
        }
    }
}
