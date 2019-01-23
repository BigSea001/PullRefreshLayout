# PullRefreshLayout
# 下拉刷新

#### 使用gradle依赖

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  
dependencies {
	 implementation 'com.github.BigSea001:PullRefreshLayout:v1.0'
}
```
设置头和监听
```
        mRefreshLayout.setHeaderView(new DefaultHeader(this));
        mRefreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.refreshComplete();
                    }
                },3000);
            }
        });
```
布局引用
```
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.dahai.pullrefreshlayout.PullToRefreshLayout
        android:id="@+id/mRefreshLayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <android.support.v7.widget.RecyclerView
            android:id="@+id/mRecyclerView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
    </com.dahai.pullrefreshlayout.PullToRefreshLayout>

</android.support.constraint.ConstraintLayout>
```
当然也可以自定义头，但是头部必须实现`PullToRefreshLayout.IHeaderView`
都有很好的注释，可以实现自己想要的刷新效果
```
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
```
效果预览
</br>
<img src="https://github.com/BigSea001/PullRefreshLayout/blob/master/art/GIF.gif" width = "300" alt="图片名称" align=center />

