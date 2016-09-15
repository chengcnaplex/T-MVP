package com.base;

import android.support.annotation.NonNull;

import com.base.util.LogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * 用RxJava实现的EventBus
 *
 * @author baixiaokang
 */
public class RxBus {
    private static RxBus instance;

    public static synchronized RxBus $() {
        if (null == instance) {
            instance = new RxBus();
        }
        return instance;
    }

    private RxBus() {
    }

    @SuppressWarnings("rawtypes")
    private ConcurrentHashMap<Object, List<Subject>> subjectMapper = new ConcurrentHashMap<Object, List<Subject>>();

    /**
     * 订阅事件源
     *
     * @param mObservable
     * @param mAction1
     * @return
     */
   // public final Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError) {
    public RxBus OnEvent(Observable<?> mObservable, Action1<Object> mAction1) {
        mObservable.observeOn(AndroidSchedulers.mainThread());
        mObservable.subscribe(mAction1, new Action1<Throwable>() {
            @Override
            public void call(Throwable e) {
                e.printStackTrace();
            }
        });
        return $();
    }

    /**
     * 注册事件源
     *
     * @param tag
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    public <T> Observable<T> register(@NonNull Object tag) {
        //事件源注册到subjectList中,subject是 extends Observable implements Observer
        //  Subscriber Subscriber<T> implements Observer<T>,Subscription
        //Subject
        List<Subject> subjectList = subjectMapper.get(tag);
        //如果事件源对应的 subjectlist是空 创建subjectlist 放到map中
        if (null == subjectList) {
            subjectList = new ArrayList<Subject>();
            subjectMapper.put(tag, subjectList);
        }
        //创建一个subject放到list中,并且放回这个subject
        Subject<T, T> subject;
        subjectList.add(subject = PublishSubject.create());
        LogUtil.d("register", tag + "  size:" + subjectList.size());
        return subject;
    }

    //从map中删除事件源对应的subject
    @SuppressWarnings("rawtypes")
    public void unregister(@NonNull Object tag) {
        List<Subject> subjects = subjectMapper.get(tag);
        if (null != subjects) {
            subjectMapper.remove(tag);
        }
    }

    /**
     * 取消监听
     *
     * @param tag
     * @param observable
     * @return
     */
    //先从subjectlist中删除传参observable 再从map中删除事件源对应的subjectlist
    @SuppressWarnings("rawtypes")
    public RxBus unregister(@NonNull Object tag,
                            @NonNull Observable<?> observable) {
        if (null == observable)
            return $();
        List<Subject> subjects = subjectMapper.get(tag);
        if (null != subjects) {
            subjects.remove((Subject<?, ?>) observable);
            if (isEmpty(subjects)) {
                subjectMapper.remove(tag);
                LogUtil.d("unregister", tag + "  size:" + subjects.size());
            }
        }
        return $();
    }

    public void post(@NonNull Object content) {
        post(content.getClass().getName(), content);
    }

    /**
     * 触发事件
     *
     * @param content
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void post(@NonNull Object tag, @NonNull Object content) {
        LogUtil.d("post", "eventName: " + tag);
        List<Subject> subjectList = subjectMapper.get(tag);
        if (!isEmpty(subjectList)) {
            for (Subject subject : subjectList) {
                subject.onNext(content);
                LogUtil.d("onEvent", "eventName: " + tag);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Collection<Subject> collection) {
        return null == collection || collection.isEmpty();
    }
}
