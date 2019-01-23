package com.dahai.pullrefreshlayout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;


/**
 * 创建时间： 2019/1/15
 * 作者：大海
 * 描述：
 */
public class DefaultHeader extends RelativeLayout implements PullToRefreshLayout.IHeaderView {

    private LoadingView loadingView;
    private ValueAnimator valueAnimator;

    public DefaultHeader(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.header_view,this);

        loadingView = findViewById(R.id.loading);

        valueAnimator = ValueAnimator.ofInt(0,360*50);
        valueAnimator.setDuration(25_000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                loadingView.setRotation((int) valueAnimator.getAnimatedValue());
            }
        });
    }

    @Override
    public void onUIReset() {
        loadingView.setRotation(0);
    }

    @Override
    public void onUIRefreshPrepare() {
    }

    @Override
    public void onUIRefreshBegin() {
        valueAnimator.start();
    }

    @Override
    public void onUIRefreshComplete() {
        valueAnimator.cancel();
    }

    @Override
    public void onUIPositionChange(int currStatus, float offsetY) {
        if (!valueAnimator.isRunning()) {
            loadingView.setRotation(Math.abs(offsetY));
        }
    }
}
