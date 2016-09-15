package com.api;


import android.util.Log;

import com.App;
import com.base.util.NetWorkUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by baixiaokang on 16/3/9.
 */
public class Api {

    public static final String X_LC_Id = "i7j2k7bm26g7csk7uuegxlvfyw79gkk4p200geei8jmaevmx";
    public static final String X_LC_Key = "n6elpebcs84yjeaj5ht7x0eii9z83iea8bec9szerejj7zy3";
    public static final String BASE_URL = "https://leancloud.cn:443/1.1/";

    public static final int DEFAULT_TIMEOUT = 5;

    public Retrofit retrofit;
    public ApiService service;

    Interceptor mInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            return chain.proceed(chain.request().newBuilder()
                    .addHeader("X-LC-Id", X_LC_Id)
                    .addHeader("X-LC-Key", X_LC_Key)
                    .addHeader("Content-Type", "application/json")
                    .build());
        }
    };


    //构造方法私有
    private Api() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        File cacheFile = new File(App.getAppContext().getCacheDir(), "cache");
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 100); //100Mb

        OkHttpClient okHttpClient =new OkHttpClient.Builder()
                //设置从主机读取数据超时
                .readTimeout(7676, TimeUnit.MILLISECONDS)
                //设置连接主机超时
                .connectTimeout(7676, TimeUnit.MILLISECONDS)
                //设置拦截器
                //拦截请求并在请求的消息头加上特定信息
                .addInterceptor(mInterceptor)
                //拦截请求并且打印日志
                .addInterceptor(interceptor)
                //例如，如果你连接到一个支持压缩的网络服务器你可以使用一个应用拦截器来添加请求实体压缩。
                //缓存请求
                .addNetworkInterceptor(new HttpCacheInterceptor())
                //设置缓存文件夹 和 大小
                .cache(cache)
                .build();


        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").serializeNulls().create();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                //设置返回Observable
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        service = retrofit.create(ApiService.class);
    }

    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final Api INSTANCE = new Api();
    }

    //获取单例
    public static Api getInstance() {
        return SingletonHolder.INSTANCE;
    }

    class HttpCacheInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (!NetWorkUtil.isNetConnected(App.getAppContext())) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
                Log.d("Okhttp", "no network");
            }

            Response originalResponse = chain.proceed(request);
            if (NetWorkUtil.isNetConnected(App.getAppContext())) {
                //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置
                String cacheControl = request.cacheControl().toString();
                return originalResponse.newBuilder()
                        .header("Cache-Control", cacheControl)
                        .removeHeader("Pragma")
                        .build();
            } else {
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=2419200")
                        .removeHeader("Pragma")
                        .build();
            }
        }
    }
}