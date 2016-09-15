package com.ui.main;

import android.content.Intent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

import com.base.BaseActivity;
import com.base.util.AnimationUtil;
import com.base.util.StatusBarUtil;
import com.ui.home.HomeActivity;
import com.view.widget.FireView;

import butterknife.Bind;

/**
 * Created by baixiaokang on 16/4/28.
 */
public class FlashActivity extends BaseActivity {

    @Bind(R.id.fl_main)
    FrameLayout fl_main;
    @Bind(R.id.view)
    View view;


    @Override
    public int getLayoutId() {
        return R.layout.activity_flash;
    }

    @Override
    public void initView() {
        //设置沉浸式效果
        StatusBarUtil.setTranslucentBackground(this);
        //火焰图片
        FireView mFireView = new FireView(this);
        //动态添加火焰动态图片
        fl_main.addView(mFireView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
        //设置动画
        AlphaAnimation anim = new AlphaAnimation(0.8f, 0.1f);
        anim.setDuration(5000);
        //设置覆盖火焰的动画
        view.startAnimation(anim);
        AnimationUtil.setAnimationListener(anim, () -> {
            startActivity(new Intent(mContext, HomeActivity.class));
            finish();
        });
    }

   /* @Override
    public void initPresenter() {
    }*/
}
