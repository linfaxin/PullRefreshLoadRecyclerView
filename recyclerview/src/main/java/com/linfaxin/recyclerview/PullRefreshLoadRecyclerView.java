package com.linfaxin.recyclerview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.linfaxin.recyclerview.headfoot.LoadMoreView;
import com.linfaxin.recyclerview.headfoot.RefreshView;
import com.linfaxin.recyclerview.headfoot.impl.DefaultLoadMoreView;
import com.linfaxin.recyclerview.headfoot.impl.DefaultRefreshView;
import com.linfaxin.recyclerview.overscroll.OverScrollGridManager;
import com.linfaxin.recyclerview.overscroll.OverScrollImpl;
import com.linfaxin.recyclerview.overscroll.OverScrollLinearLayoutManager;

/**
 * Created by linfaxin on 15/8/31.
 * Pull To Refresh & Load RecyclerView
 */
public class PullRefreshLoadRecyclerView extends FrameLayout implements RefreshView.StateListener, LoadMoreView.StateListener {
    RecyclerView recyclerView;
    LoadMoreView loadMoreView;
    RefreshView refreshView;
    LoadRefreshListener loadRefreshListener;

    public PullRefreshLoadRecyclerView(Context context) {
        super(context);
        init();
    }

    public PullRefreshLoadRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullRefreshLoadRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PullRefreshLoadRecyclerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        recyclerView = new InnerRecyclerView(getContext());
        recyclerView.setLayoutManager(new OverScrollLinearLayoutManager(recyclerView));
        addView(recyclerView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        setLoadMoreView(new DefaultLoadMoreView(getContext()));
        setRefreshView(new DefaultRefreshView(getContext()));
    }

    public void setLoadMoreView(LoadMoreView loadMoreView) {
        if (this.loadMoreView != null) {
            this.loadMoreView.setStateListener(null);
            this.loadMoreView.unBind();
            removeView(this.loadMoreView);
        }
        this.loadMoreView = loadMoreView;

        if (loadMoreView != null) {
            addView(loadMoreView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));
            loadMoreView.bindWith(recyclerView);
            loadMoreView.setStateListener(this);
        }
    }

    public void setRefreshView(RefreshView refreshView) {
        if (this.refreshView != null) {
            this.refreshView.setStateListener(null);
            this.refreshView.unBind();
            removeView(this.refreshView);
        }
        this.refreshView = refreshView;
        if (refreshView != null) {
            addView(refreshView, new FrameLayout.LayoutParams(-1, -2, Gravity.TOP));
            refreshView.bindWith(recyclerView);
            refreshView.setStateListener(this);
        }
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public LoadMoreView getLoadMoreView() {
        return loadMoreView;
    }

    public RefreshView getRefreshView() {
        return refreshView;
    }

    public boolean isLoading() {
        return (loadMoreView != null && loadMoreView.getState() == LoadMoreView.STATE_LOADING) ||
                (refreshView != null && refreshView.getState() == RefreshView.STATE_LOADING);
    }

    @Override
    public boolean interceptStateChange(RefreshView refreshView, int state, int oldState) {
        if (state != RefreshView.STATE_NORMAL &&
                loadMoreView != null && loadMoreView.getState() == LoadMoreView.STATE_LOADING) {
            return true;
        }
        return false;
    }

    @Override
    public void onStateChange(RefreshView refreshView, int state) {
        if (loadRefreshListener != null && state == RefreshView.STATE_LOADING) {
            loadRefreshListener.onRefresh(this, refreshView);

            if (loadMoreView != null && loadMoreView.getState() == LoadMoreView.STATE_LOAD_FAIL) {
                loadMoreView.setState(LoadMoreView.STATE_NORMAL);
            }
        }
    }

    @Override
    public boolean interceptStateChange(LoadMoreView loadMoreView, int state, int oldState) {
        if (state == LoadMoreView.STATE_LOADING &&
                refreshView != null && refreshView.getState() == RefreshView.STATE_LOADING) {
            return true;
        }
        return false;
    }

    @Override
    public void onStateChange(LoadMoreView loadMoreView, int state) {
        if (loadRefreshListener != null && state == LoadMoreView.STATE_LOADING) {
            loadRefreshListener.onLoadMore(this, loadMoreView);
        }
    }

    public void setLoadRefreshListener(LoadRefreshListener loadRefreshListener) {
        this.loadRefreshListener = loadRefreshListener;
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        recyclerView.setAdapter(adapter);
    }

    public void setAdapter(LoadRefreshAdapter adapter) {
        recyclerView.setAdapter(adapter);
        setLoadRefreshListener(adapter);
    }

    public static abstract class LoadRefreshAdapter<T extends RecyclerView.ViewHolder>
            extends RecyclerView.Adapter<T> implements LoadRefreshListener {
    }

    public interface LoadRefreshListener {
        void onRefresh(PullRefreshLoadRecyclerView pullRefreshLoadRecyclerView, RefreshView refreshView);

        void onLoadMore(PullRefreshLoadRecyclerView pullRefreshLoadRecyclerView, LoadMoreView loadMoreView);
    }

    private static class InnerRecyclerView extends RecyclerView {
        public InnerRecyclerView(Context context) {
            super(context);
        }

        @Override
        public void setLayoutManager(LayoutManager layout) {
            if (layout != null && !(layout instanceof OverScrollImpl.OverScrollLayoutManager)) {
                if (layout.getClass().equals(GridLayoutManager.class)) {
                    GridLayoutManager grid = (GridLayoutManager) layout;
                    layout = new OverScrollGridManager(this, grid.getSpanCount(), grid.getOrientation(), grid.getReverseLayout());
                } else if (layout.getClass().equals(LinearLayoutManager.class)) {
                    LinearLayoutManager linear = (LinearLayoutManager) layout;
                    layout = new OverScrollLinearLayoutManager(this, linear.getOrientation(), linear.getReverseLayout());
                } else {
                    throw new IllegalArgumentException("LayoutManager " + layout +
                            " should be subclass of: " + OverScrollLinearLayoutManager.class.getName());
                }
            }
            super.setLayoutManager(layout);
        }

        @Override
        public int computeVerticalScrollOffset() {
            int overScrollDistance = 0;
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager instanceof OverScrollImpl.OverScrollLayoutManager) {
                overScrollDistance = ((OverScrollImpl.OverScrollLayoutManager) layoutManager).getOverScrollDistance();
            }
            return super.computeVerticalScrollOffset() - overScrollDistance;
        }
    }
}
