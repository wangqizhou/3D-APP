package com.evistek.gallery.net;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by evis on 2016/9/8.
 */
public class OkHttpWrapper {
    private static final String TAG = "OkHttpWrapper";

    public static final MediaType MEDIA_TYPE_JSON
            = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpWrapper mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mHandler;
    private Gson mGson;

    private OkHttpWrapper() {
        mOkHttpClient = new OkHttpClient();
        mHandler = new Handler(Looper.getMainLooper());

        //The Dateformat of Gson is the same with the server.
        mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    public static OkHttpWrapper getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpWrapper.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpWrapper();
                }
            }
        }

        return mInstance;
    }

    /**
     * get
     * */
    public void getAsync(String url, Map<String, String> parameterMap, List<String> pathList, final RequestCallBack requestCallBack) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        HttpUrl.Builder builder = httpUrl.newBuilder();
        if (pathList != null) {
            for (String path: pathList) {
                builder.addPathSegment(path);
            }
        }
        if (parameterMap != null) {
            for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                builder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        httpUrl = builder.build();
        final Request request = new Request.Builder()
                .url(httpUrl)
                .build();
        call(request, requestCallBack);
    }

    /**
     * post
     * */
    public void postAsync(String url, Map<String, String> parameterMap,  List<String> pathList, Object body, final RequestCallBack requestCallBack) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, mGson.toJson(body));
        HttpUrl.Builder builder = httpUrl.newBuilder();
        if (pathList != null) {
            for (String path: pathList) {
                builder.addPathSegment(path);
            }
        }
        if (parameterMap != null) {
            for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                builder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        httpUrl = builder.build();
        final Request request  = new Request.Builder().url(httpUrl).post(requestBody).build();
        call(request, requestCallBack);
    }

    /**
     * put
     * */
    public void putAsync(String url, Object body, final  RequestCallBack requestCallBack) {
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, mGson.toJson(body));
        final Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();

        call(request, requestCallBack);
    }

    /**
     * delete
     * */
    public void deleteAsync(String url, List<String> pathList, Object body, final  RequestCallBack requestCallBack) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, mGson.toJson(body));
        HttpUrl.Builder builder = httpUrl.newBuilder();
        if (pathList != null) {
            for (String path: pathList) {
                builder.addPathSegment(path);
            }
        }
        httpUrl = builder.build();
        final Request request = new Request.Builder()
                .url(httpUrl)
                .delete(requestBody)
                .build();

        call(request, requestCallBack);
    }

    public void call(Request request,final RequestCallBack requestCallBack){
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleFailureResult(0, e.getMessage(), requestCallBack);
                Log.e(TAG, "onFailure: " + e.getMessage() + " method: " + call.request().method());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String bodyString = response.body().string();
                    if (requestCallBack.type == String.class) {
                        handleSuccessResult(response.code(), bodyString, requestCallBack);
                    } else {
                        Object object = mGson.fromJson(bodyString, requestCallBack.type);
                        handleSuccessResult(response.code(), object, requestCallBack);
                    }
                } else {
                    handleFailureResult(response.code(), response.message(), requestCallBack);
                    Log.e(TAG, "code: " + response.code() + " msg: " + response.message());
                }
            }
        });
    }

    public abstract static class RequestCallBack<T> {
        Type type;

        public RequestCallBack() {
            type = getSuperclassTypeParameter(getClass());
        }

        static Type getSuperclassTypeParameter(Class<?> subclass) {
            Type superclass = subclass.getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }

            Type[] types = ((ParameterizedType) superclass).getActualTypeArguments();
            return types[0];
        }

        public abstract void onResponse(int code, T response);
        public abstract void onFailure(int code, String msg);
    }

    // run on the UI thread
    private void handleSuccessResult(final int code, final Object o, final RequestCallBack callBack) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onResponse(code,o);
                }
            }
        });
    }

    private void handleFailureResult(final int code, final String msg, final RequestCallBack callBack) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onFailure(code, msg);
                }
            }
        });
    }
}
