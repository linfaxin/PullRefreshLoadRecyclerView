package com.linfaxin.recyclerview.overscroll;

import android.support.v7.widget.RecyclerView;
import android.view.ViewTreeObserver;

/**
 * Created by linfaxin on 15/8/31.
 * OverScroll Impl.
 */
public class OverScrollImpl {
    public interface OverScrollLayoutManager {
        int superScrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state);

        int getOverScrollDistance();

        void setLockOverScrollTop(int topDistance);
    }

    private static final int overScrollDuration = 180;
    private int scrollWeight = 40;
    long startOverScrollTime;

    private int overScrollDistance = 0;
    private int overScrollTopDistanceLock = 0;
    RecyclerView recyclerView;

    public OverScrollImpl(final RecyclerView recyclerView) {
        this.recyclerView = recyclerView;

        scrollWeight = (int) (scrollWeight * recyclerView.getContext().getResources().getDisplayMetrics().density);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    resetOverScroll();
                }
            }
        });
    }

    private void resetOverScroll() {
        int scrollBy = overScrollDistance;
        if (scrollBy > 0) {
            scrollBy = Math.max(scrollBy - overScrollTopDistanceLock, 0);
        }
        recyclerView.smoothScrollBy(0, scrollBy);
        startOverScrollTime = 0;

    }

    public int scrollVerticallyBy(OverScrollLayoutManager layoutManager, int dy,
                                  RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getOverScrollDistance() > 0 && dy > 0) {
            int aim = getOverScrollDistance() - dy;
            if (aim < 0) aim = 0;
            setOverScrollDistance(aim);
            return dy;
        }
        if (getOverScrollDistance() < 0 && dy < 0) {
            int aim = getOverScrollDistance() - dy;
            if (aim > 0) aim = 0;
            setOverScrollDistance(aim);
            return dy;
        }

        int vResult = layoutManager.superScrollVerticallyBy(dy, recycler, state);
        int overScrollY = dy - vResult;
        if (overScrollY != 0) {
            if (overScrollY < 0 && overScrollDistance < overScrollTopDistanceLock) {
                appendOverScrollDistance(-overScrollY);
                return dy;
            }

            if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_SETTLING) {
                //TODO improve bounce.
                if (startOverScrollTime <= 0) startOverScrollTime = System.currentTimeMillis();

                float scale = (scrollWeight - Math.abs(getOverScrollDistance())) / scrollWeight;
                float timeScale = (1 - (System.currentTimeMillis() - startOverScrollTime) / overScrollDuration);
                scale = (scale + timeScale * 2) / 3;
                scale = scale * scale * scale;
                if (scale <= 0.05f) scale = 0;

                overScrollY *= scale;
                if (Math.abs(overScrollY) <= 0) {
                    resetOverScroll();

                } else {
                    appendOverScrollDistance(-overScrollY);
                }

            } else if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING) {
                appendOverScrollDistance(-overScrollY / 2);
            }
        } else {
            setOverScrollDistance(0);
        }
        return dy;
    }

    RecyclerView.Adapter lastRegAdapter;
    private RecyclerView.AdapterDataObserver syncOverScrollWhenDataChange = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            recyclerView.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            setOverScrollDistance(overScrollDistance);
                        }
                    });
        }
    };

    private void checkBindDataChange() {
        if (recyclerView == null) return;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter != lastRegAdapter) {
            if (lastRegAdapter != null)
                lastRegAdapter.unregisterAdapterDataObserver(syncOverScrollWhenDataChange);
            if (adapter != null) adapter.registerAdapterDataObserver(syncOverScrollWhenDataChange);
            lastRegAdapter = adapter;
        }
    }

    public int getOverScrollDistance() {
        return overScrollDistance;
    }

    public void setOverScrollDistance(int overScrollDistance) {
        this.overScrollDistance = overScrollDistance;
        if (recyclerView != null) {
            checkBindDataChange();
//      recyclerView.setTranslationY(overScrollDistance);
            for (int i = 0, count = recyclerView.getChildCount(); i < count; i++) {
                recyclerView.getChildAt(i).setTranslationY(overScrollDistance);
            }
        }
    }


    public void appendOverScrollDistance(int append) {
        setOverScrollDistance(overScrollDistance + append);
    }

    public void setLockOverScrollTop(int topDistance) {
        overScrollTopDistanceLock = topDistance;
        if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_DRAGGING) {
            resetOverScroll();
        }
    }
}
