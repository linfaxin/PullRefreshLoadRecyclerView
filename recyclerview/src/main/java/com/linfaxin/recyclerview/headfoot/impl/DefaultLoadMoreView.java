package com.linfaxin.recyclerview.headfoot.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.linfaxin.recyclerview.headfoot.LoadMoreView;

/**
 * Created by linfaxin on 15/8/31.
 */
public class DefaultLoadMoreView extends LoadMoreView {
    private TextView textView;
    private ProgressBar progressBar;

    public DefaultLoadMoreView(Context context) {
        super(context);
        init();
    }

    public DefaultLoadMoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DefaultLoadMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DefaultLoadMoreView(Context context, AttributeSet attrs, int defStyleAttr,
                               int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setMinimumHeight((int) (48 * getContext().getResources().getDisplayMetrics().density));

        textView = new TextView(getContext());
        progressBar = new ProgressBar(getContext());
        addView(progressBar);
        addView(textView);
        textView.setText("查看更多");
        textView.setGravity(Gravity.CENTER);
        progressBar.setVisibility(GONE);
    }

    @Override
    protected void onStateChange(int state, int oldState) {
        switch (state) {
            case STATE_NORMAL:
                textView.setText("查看更多");
                progressBar.setVisibility(GONE);
                break;
            case STATE_LOADING:
                textView.setText("加载中...");
                progressBar.setVisibility(VISIBLE);
                break;
            case STATE_READY:
                textView.setText("松开加载更多");
                progressBar.setVisibility(GONE);
                break;
            case STATE_NO_MORE:
                textView.setText("加载完毕");
                progressBar.setVisibility(GONE);
                break;
            case STATE_LOAD_FAIL:
                textView.setText("重新加载");
                progressBar.setVisibility(GONE);
                break;
            case STATE_EMPTY_RELOAD:
                textView.setText("无数据,点击重试");
                progressBar.setVisibility(GONE);
                break;
        }
    }

    public void setTextShow(String text) {
        textView.setText(text);
    }
}
