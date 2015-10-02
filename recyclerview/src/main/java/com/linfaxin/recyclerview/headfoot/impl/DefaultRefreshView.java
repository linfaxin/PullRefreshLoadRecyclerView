package com.linfaxin.recyclerview.headfoot.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.linfaxin.recyclerview.headfoot.RefreshView;

/**
 * Created by linfaxin on 15/8/31.
 */
public class DefaultRefreshView extends RefreshView {

    private TextView textView;
    private ProgressBar progressBar;

    public DefaultRefreshView(Context context) {
        super(context);
        init();
    }

    public DefaultRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DefaultRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DefaultRefreshView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

        textView.setText("下拉刷新");
        progressBar.setVisibility(GONE);
    }

    @Override
    protected void onStateChange(int newState, int oldState) {
        switch (newState) {
            case STATE_NORMAL:
                textView.setText("下拉加载更多");
                progressBar.setVisibility(GONE);
                break;
            case STATE_READY:
                textView.setText("松开立即刷新");
                progressBar.setVisibility(GONE);
                break;
            case STATE_LOADING:
                textView.setText("加载中...");
                progressBar.setVisibility(VISIBLE);
                break;
        }
    }
}
