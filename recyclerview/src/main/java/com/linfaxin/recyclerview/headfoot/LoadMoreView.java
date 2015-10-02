package com.linfaxin.recyclerview.headfoot;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

/**
 * Created by linfaxin on 15/8/28.
 * Load More View
 */
public abstract class LoadMoreView extends LinearLayout {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_READY = 2;
    public static final int STATE_LOAD_FAIL = 3;
    public static final int STATE_NO_MORE = 4;
    public static final int STATE_EMPTY_RELOAD = 5;

    public interface StateListener {
        boolean interceptStateChange(LoadMoreView loadMoreView, int state, int oldState);

        void onStateChange(LoadMoreView loadMoreView, int state);
    }

    private int state;
    private int scrollState;
    private StateListener stateListener;
    private boolean isAutoLoadWhenScroll = true;
    private boolean shouldAutoLoadThisTimeFlag = true;
    protected RecyclerView recyclerView;

    public LoadMoreView(Context context) {
        super(context);
        init();
    }

    public LoadMoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LoadMoreView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == STATE_NORMAL || state == STATE_LOAD_FAIL || state == STATE_EMPTY_RELOAD) {
                    setState(STATE_LOADING);
                }
            }
        });
    }

    public void setState(int state) {
        int oldState = this.state;
        if (oldState == state) return;
        if (stateListener != null && stateListener.interceptStateChange(this, state, oldState)) {
            return;
        }
        this.state = state;
        shouldAutoLoadThisTimeFlag = true;
        onStateChange(state, oldState);
        if (stateListener != null) stateListener.onStateChange(this, state);
    }

    public int getState() {
        return state;
    }

    protected abstract void onStateChange(int state, int oldState);

    public void setStateListener(StateListener stateListener) {
        this.stateListener = stateListener;
    }

    public boolean isAutoLoadWhenScroll() {
        return isAutoLoadWhenScroll;
    }

    public void setIsAutoLoadWhenScroll(boolean isAutoLoadWhenScroll) {
        this.isAutoLoadWhenScroll = isAutoLoadWhenScroll;
        shouldAutoLoadThisTimeFlag = true;
    }

    private void computeOffsetWith(RecyclerView recyclerView) {
        if (recyclerView.getAdapter() == null) {
            setTranslationY(getMeasuredHeight());
            return;
        }
        int lastPosition = recyclerView.getAdapter().getItemCount() - 1;
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(lastPosition);
        if (viewHolder != null && viewHolder.itemView != null && viewHolder.itemView.isShown()) {
            int scrollRange = recyclerView.computeVerticalScrollRange();
            int currentScroll = recyclerView.computeVerticalScrollOffset();
            int rHeight = recyclerView.getHeight();
            int rPadTop = recyclerView.getPaddingTop();
            int offset = scrollRange - currentScroll - rHeight + getHeight() + rPadTop;

            checkAutoLoad();
            setTranslationY(offset);
            if (offset < 0) {
                dispatchOverScroll(offset, recyclerView.getScrollState());
            }
        } else if (recyclerView.getAdapter().getItemCount() == 0) {//no item
            setTranslationY(-recyclerView.getHeight() / 2 + getMeasuredHeight() / 2);
            checkAutoLoad();

        } else {
            setTranslationY(getMeasuredHeight());
        }
    }

    private void checkAutoLoad() {
        if (recyclerView == null || recyclerView.getAdapter() == null) return;
        if (isAutoLoadWhenScroll) {
            try {
                recyclerView.getAdapter().registerAdapterDataObserver(observer);
            } catch (Exception ignore) {
            }
            if (state == STATE_NORMAL && shouldAutoLoadThisTimeFlag) {
                shouldAutoLoadThisTimeFlag = false;
                setState(STATE_LOADING);
                //may in layout when change state, force layout again
                post(new Runnable() {
                    @Override
                    public void run() {
                        requestLayout();
                    }
                });
            }
        }

    }

    private RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
        private int lastItemCount = -1;

        @Override
        public void onChanged() {
            super.onChanged();
            checkItemCountChange();
        }

        private void checkItemCountChange() {
            if (recyclerView != null && recyclerView.getAdapter() != null) {
                int itemCount = recyclerView.getAdapter().getItemCount();
                if (itemCount != lastItemCount) {
                    lastItemCount = itemCount;
                    shouldAutoLoadThisTimeFlag = true;//reset auto load flag
                }
            }
        }
    };

    protected void dispatchOverScroll(int overScrollDistance, int scrollState) {
        boolean isInDrag = RecyclerView.SCROLL_STATE_DRAGGING == scrollState;
        if (state != STATE_LOADING && state != STATE_NO_MORE) {
            if (isInDrag && overScrollDistance < -getHeight()) {
                setState(STATE_READY);
            }
            if (overScrollDistance > -getHeight()) {
                if (state != STATE_LOAD_FAIL) setState(STATE_NORMAL);
            }
            if (scrollState != this.scrollState) {
                if (this.scrollState == RecyclerView.SCROLL_STATE_DRAGGING && overScrollDistance < -getHeight()) {
                    setState(STATE_LOADING);
                }
                this.scrollState = scrollState;
            }
        }
        onOverScroll(overScrollDistance, isInDrag);
    }

    protected void onOverScroll(int overScrollDistance, boolean isInDrag) {
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (recyclerView != null) computeOffsetWith(recyclerView);
        else setTranslationY(getMeasuredHeight());
    }

    RecyclerView.OnScrollListener computePositionListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            computeOffsetWith(recyclerView);
        }
    };

    public void bindWith(final RecyclerView recyclerView) {
        this.recyclerView = recyclerView;

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        if (getWindowToken() == null) return;
                        recyclerView.setPadding(recyclerView.getPaddingLeft(), recyclerView.getPaddingTop(),
                                recyclerView.getPaddingRight(), getMeasuredHeight());
                        recyclerView.setClipToPadding(false);
                        recyclerView.removeOnScrollListener(computePositionListener);
                        recyclerView.addOnScrollListener(computePositionListener);
                    }
                });
    }

    public void unBind() {
        if (recyclerView != null) {
            recyclerView.removeOnScrollListener(computePositionListener);
        }
    }
}
