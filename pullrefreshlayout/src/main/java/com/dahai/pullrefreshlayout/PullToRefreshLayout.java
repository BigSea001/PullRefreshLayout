package com.dahai.pullrefreshlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 创建时间： 2019/1/15
 * 作者：大海
 * 描述：
 */
public class PullToRefreshLayout extends ViewGroup {

    public static final int STATUS_INITIAL = 0;//初始状态
    public static final int STATUS_PULL = 1;//下拉状态
    public static final int STATUS_PULL_PREPARE = 2;//下拉释放刷新状态
    public static final int STATUS_REFRESH = 3;//刷新状态
    public static final int STATUS_REFRESH_COMPLETE = 4;//刷新状态

    // 当前状态
    private int currStatus;
    // 下拉偏移量
    private float mOffsetY;
    // 最后按下的y
    private int mLastPosY = 0;
    // 当前mTargetView的Top值
    private int mCurrentY = 0;
    // 头部的高度
    private int mHeaderHeight;

    private View mHeaderView;
    private View mTargetView;

    private Scroller mScroller;
    private IHeaderView iHeaderView;
    private OnRefreshListener onRefreshListener;

    private float resistance = 2f;
    // 下拉超过头部的高度后回到头部位置的时间
    private int closeDuration = 200;
    // 头部隐藏起来的时间
    private int headerCloseDuration = 200;

    private boolean isOnTouch = false;

    // 是否给子View取消了事件
    private boolean mIsSendCancelEvent = false;
    // 是否给子View发送了点击事件
    private boolean mIsSendDownEvent = false;
    private MotionEvent mLastMotionEvent;

    public PullToRefreshLayout(Context context) {
        this(context, null);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        ensureTargetView();

        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderHeight = mHeaderView.getMeasuredHeight();
        }

        if (mTargetView != null) {
            measureChild(mTargetView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        ensureTargetView();

        int offsetY = mCurrentY;

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if (mHeaderView != null) {
            int top = paddingTop + offsetY - mHeaderView.getMeasuredHeight();
            int right = paddingLeft + mHeaderView.getMeasuredWidth();
            int bottom = top + mHeaderView.getMeasuredHeight();

            mHeaderView.layout(paddingLeft, top, right, bottom);
        }

        if (mTargetView != null) {
            int top = paddingTop + offsetY;
            int right = paddingLeft + mTargetView.getMeasuredWidth();
            int bottom = top + mTargetView.getMeasuredHeight();

            mTargetView.layout(paddingLeft, top, right, bottom);
        }
    }

    @Override
    protected void onFinishInflate() {
        ensureTargetView();
        super.onFinishInflate();
    }

    private void ensureTargetView() {
        if (mTargetView == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (!view.equals(mHeaderView)) {
                    mTargetView = view;
                    break;
                }
            }
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        // 还有滚动
        if (mScroller.computeScrollOffset()) {
            updateView(mOffsetY - mScroller.getCurrY());
            mOffsetY = mScroller.getCurrY();

            iHeaderView.onUIPositionChange(currStatus, -mTargetView.getTop());
            invalidate();
        } else {
            if (currStatus == STATUS_REFRESH_COMPLETE) {
                if (mTargetView.getTop() <= 0) {
                    currStatus = STATUS_INITIAL;
                }
            }
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        ensureTargetView();

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isOnTouch = true;
                // 清空滚动
                mScroller.forceFinished(true);

                mCurrentY = mTargetView.getTop();
                mLastPosY = (int) event.getY();
                mLastMotionEvent = event;

                // 如果 mTargetView没在顶部就直接拦截
                if (checkIsPull()) {
                    return true;
                }

                return super.dispatchTouchEvent(event);
            case MotionEvent.ACTION_MOVE:

                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }

                // 在下拉过程中，并且已经下拉出来了，发送取消事件给子View，子View不处理该事件
                if (!mIsSendCancelEvent && mOffsetY > 0 && checkIsPull()) {
                    sendCancelEvent();
                }

                mOffsetY = event.getY() - mLastPosY;

                mLastPosY = (int) event.getY();

                // 能下拉刷新
                if (mOffsetY >= 0 && !childCanScrollUp(mTargetView)) {
                    mIsSendDownEvent = false;
                    updateView(mOffsetY / resistance);
                    changeUiStatus();
                    iHeaderView.onUIPositionChange(currStatus, mTargetView.getTop());
                    return true;
                }
                //在下拉出来的状态往上滑的过程
                if (mOffsetY < 0 && checkIsPull()) {
                    updateView(mOffsetY);
                    changeUiStatus();
                    iHeaderView.onUIPositionChange(currStatus, -mTargetView.getTop());
                    return true;
                }

                // 没在下拉状态时，让子View接收事件
                if (!mIsSendDownEvent && !checkIsPull()) {
                    mIsSendDownEvent = true;
                    sendDownEvent();
                }

                return super.dispatchTouchEvent(event);

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                mIsSendCancelEvent = false;
                mIsSendDownEvent = false;
                isOnTouch = false;

                mLastPosY = (int) event.getY();

                changeUiStatus();
                iHeaderView.onUIPositionChange(currStatus, -mTargetView.getTop());

                if (currStatus == STATUS_REFRESH && mCurrentY > mHeaderHeight) {
                    // 回到刷新该有的位置
                    resetLocation(mCurrentY - mHeaderHeight, mHeaderHeight, closeDuration);
                    mCurrentY = mTargetView.getTop();
                }
                if (currStatus == STATUS_REFRESH_COMPLETE) {
                    refreshComplete();
                }

                if (currStatus == STATUS_PULL) {
                    resetLocation(mCurrentY, 0, headerCloseDuration);
                }
                return super.dispatchTouchEvent(event);
        }

        return true;
    }

    /**
     * 子View能否向下滚动
     * @param view view
     * @return true还没到顶部
     */
    private boolean childCanScrollUp(View view) {
        return view.canScrollVertically(-1);
    }

    /**
     * 取消拦截
     */
    private void sendCancelEvent() {
        if (mLastMotionEvent == null) {
            return;
        }
        mIsSendCancelEvent = true;
        MotionEvent last = mLastMotionEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        super.dispatchTouchEvent(e);
    }

    private void sendDownEvent() {
        final MotionEvent last = mLastMotionEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        super.dispatchTouchEvent(e);
    }


    private void changeUiStatus() {
        if (currStatus == STATUS_REFRESH) {
            return;
        }
        if (isOnTouch) {
            if (mCurrentY < mHeaderHeight && currStatus != STATUS_PULL) {
                currStatus = STATUS_PULL;
                iHeaderView.onUIReset();
            }
            if (mCurrentY >= mHeaderHeight && currStatus != STATUS_PULL_PREPARE) {
                currStatus = STATUS_PULL_PREPARE;
                iHeaderView.onUIRefreshPrepare();
            }
        } else {
            if (currStatus == STATUS_PULL_PREPARE) {
                currStatus = STATUS_REFRESH;
                iHeaderView.onUIRefreshBegin();
                onRefreshListener.onRefresh();
            }
        }
    }

    public void refreshComplete() {
        resetLocation(mCurrentY, 0, headerCloseDuration);
        currStatus = STATUS_REFRESH_COMPLETE;
        iHeaderView.onUIRefreshComplete();
    }

    /**
     * 回滚
     * @param distance 滚动距离
     * @param toPos 滚到的位置
     * @param millions 时间
     */
    private void resetLocation(int distance, int toPos, int millions) {
        mScroller.startScroll(0, toPos, 0, distance, millions);
        mOffsetY = toPos;
        postInvalidate();
    }

    /**
     * mTargetView是否在下拉中
     * @return true：在下拉中
     */
    private boolean checkIsPull() {
        int contentMarginTop = mCurrentY;
        return contentMarginTop > 0;
    }

    private void updateView(float y) {
        mTargetView.offsetTopAndBottom((int) y);
        mHeaderView.offsetTopAndBottom((int) y);
        invalidate();
        mCurrentY = mTargetView.getTop();

    }

    /**
     * 设置头部
     * @param headerView headerView,必须实现{@link IHeaderView}
     */
    public void setHeaderView(View headerView) {
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        headerView.setLayoutParams(lp);
        mHeaderView = headerView;
        addView(headerView);
        if (!(headerView instanceof IHeaderView)) {
            throw new RuntimeException("Your header must implements IHeaderView");
        }
        iHeaderView = (IHeaderView) headerView;
    }

    /**
     * 设置刷新监听
     * @param onRefreshListener listener
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void setResistance(float resistance) {
        this.resistance = resistance;
    }

    public void setCloseDuration(int closeDuration) {
        this.closeDuration = closeDuration;
    }

    public void setHeaderCloseDuration(int headerCloseDuration) {
        this.headerCloseDuration = headerCloseDuration;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    public interface IHeaderView {

        /**
         * 重置
         */
        void onUIReset();

        /**
         * 释放可以刷新
         */
        void onUIRefreshPrepare();

        /**
         * 开始刷新
         */
        void onUIRefreshBegin();

        /**
         * 刷新完成
         */
        void onUIRefreshComplete();

        /**
         * 在下拉过程中位置变化
         * @param currStatus 刷新状态
         * @param offsetY {@link PullToRefreshLayout#mCurrentY }
         */
        void onUIPositionChange(int currStatus, float offsetY);
    }
}
