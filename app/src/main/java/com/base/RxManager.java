package com.base;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * 每个Presenter有对应的自己的RxManger对象 在
 * 用于管理RxBus的事件和Rxjava相关代码的生命周期处理
 * 防止内存泄露
 * Created by baixiaokang on 16/4/28.
 */
public class RxManager {

    public RxBus mRxBus = RxBus.$();
    private Map<String, Observable<?>> mObservables = new HashMap<>();// 管理观察源
    //如果你一直在关注代码，你可能会注意到你调用的 Observable.subscribe() 的返回值是一个 Subscription 对象。
    // Subscription 类只有两个方法，unsubscribe() 和 isUnsubscribed()。
    // 为了防止可能的内存泄露，在你的 Activity 或 Fragment 的 onDestroy 里，
    // 用 Subscription.isUnsubscribed() 检查你的 Subscription 是否是 unsubscribed。
    // 如果调用了 Subscription.unsubscribe() ，Unsubscribing将会对 items 停止通知给你的 Subscriber，
    // 并允许垃圾回收机制释放对象，防止任何 RxJava 造成内存泄露。如果你正在处理多个 Observables 和 Subscribers，
    // 所有的 Subscription 对象可以添加到 CompositeSubscription，
    // 然后可以使用 CompositeSubscription.unsubscribe() 方法在同一时间进行退订(unsubscribed)。
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();// 管理订阅者者

    //
    public void post(Object tag, Object content) {
        mRxBus.post(tag, content);
    }

    //add 和 on的区别就是  on注册了RxBus   而且不会立刻执行 而add会立刻执行函数
    public void on(String eventName, Action1<Object> action1) {
        Log.e("1", eventName);

        //RxBus注册一个eventName事件返回一个Observable
        Observable<?> mObservable = mRxBus.register(eventName);
        //注册返回的观察源 添加到观察源集合中
        mObservables.put(eventName, mObservable);
        //返回的观察源对象 注册观察者actiopn1 并且设置在主线程中 执行回调方法
        mCompositeSubscription.add(mObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(action1, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        e.printStackTrace();
                    }
                }));
    }

    public void add(Subscription m) {
        mCompositeSubscription.add(m);
    }

    public void clear() {
        mCompositeSubscription.unsubscribe();// 取消订阅
        for (Map.Entry<String, Observable<?>> entry : mObservables.entrySet())
            mRxBus.unregister(entry.getKey(), entry.getValue());// 移除观察
    }
}
