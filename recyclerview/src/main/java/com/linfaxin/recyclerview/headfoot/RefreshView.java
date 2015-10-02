package com.linfaxin.recyclerview.headfoot;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.linfaxin.recyclerview.overscroll.OverScrollImpl;

/**
 * Created by linfaxin on 15/8/28.
 * Load More View
 */
public abstract class RefreshView extends LinearLayout {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_READY = 2;

    public interface StateListener {
        boolean interceptStateChange(RefreshView refreshView, int state, int oldState);

        void onStateChange(RefreshView refreshView, int state);
    }

    private RecyclerView recyclerView;
    private int state;
    private int scrollState;
    private StateListener stateListener;

    public RefreshView(Context context) {
        super(context);
        init();
    }

    public RefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RefreshView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

    }

    public void setState(int state) {
        int oldState = this.state;
        if (oldState == state) return;
        if (stateListener != null && stateListener.interceptStateChange(this, state, oldState)) {
            return;
        }
        this.state = state;

        OverScrollImpl.OverScrollLayoutManager overScrollManager =
                (OverScrollImpl.OverScrollLayoutManager) recyclerView.getLayoutManager();
        if (overScrollManager != null) {
            int lockTop = (state == STATE_LOADING || state == STATE_READY ? getMeasuredHeight() : 0);
            overScrollManager.setLockOverScrollTop(lockTop);
        }
        onStateChange(state, oldState);
        if (stateListener != null) stateListener.onStateChange(this, state);
    }

    protected abstract void onStateChange(int newState, int oldState);

    protected void dispatchOverScroll(int overScrollDistance, int scrollState) {
        boolean isInDrag = RecyclerView.SCROLL_STATE_DRAGGING == scrollState;
        if (state != STATE_LOADING) {
            if (isInDrag && overScrollDistance > getHeight()) {
                setState(STATE_READY);
            }
            if (overScrollDistance < getHeight()) {
                setState(STATE_NORMAL);
            }
            if (scrollState != this.scrollState) {
                if (this.scrollState == RecyclerView.SCROLL_STATE_DRAGGING
                        && overScrollDistance > getHeight()) {
                    setState(STATE_LOADING);
                }
                this.scrollState = scrollState;
            }
        }
        checkRegObserver();
        onOverScroll(overScrollDistance, isInDrag);
    }

    protected void onOverScroll(int overScrollDistance, boolean isInDrag) {
    }

    public void setStateListener(StateListener stateListener) {
        this.stateListener = stateListener;
    }

    public int getState() {
        return state;
    }

    public void bindWith(final RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        recyclerView.removeOnScrollListener(computePositionListener);
        recyclerView.addOnScrollListener(computePositionListener);

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        setTranslationY(-getMeasuredHeight());
                    }
                });
    }

    public void unBind() {
        if (recyclerView != null) {
            recyclerView.removeOnScrollListener(computePositionListener);
        }
    }

    private RecyclerView.OnScrollListener computePositionListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (recyclerView.getAdapter() == null) return;
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
            if (viewHolder != null && viewHolder.itemView != null) {
                int currentScroll = recyclerView.computeVerticalScrollOffset();
                setTranslationY(-currentScroll - getHeight());

                dispatchOverScroll(-currentScroll, recyclerView.getScrollState());
            }
        }
    };

    private void checkRegObserver() {
        try {
            if (recyclerView != null && recyclerView.getAdapter() != null) {
                recyclerView.getAdapter().registerAdapterDataObserver(observer);
            }
        } catch (Exception ignore) {
        }
    }

    private RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            checkItemCountChange();
        }

        private void checkItemCountChange() {
            if (recyclerView != null && recyclerView.getAdapter() != null) {
                int itemCount = recyclerView.getAdapter().getItemCount();
                if (itemCount == 0) {//when reload empty, make sure position invisible
                    setTranslationY(-getMeasuredHeight());
                }
            }
        }
    };
}
