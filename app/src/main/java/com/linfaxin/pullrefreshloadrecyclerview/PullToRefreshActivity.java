package com.linfaxin.pullrefreshloadrecyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.linfaxin.recyclerview.PullRefreshLoadRecyclerView;

/**
 * Created by linfaxin on 15/10/2.
 */
public class PullToRefreshActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PullRefreshLoadRecyclerView recyclerView = new PullRefreshLoadRecyclerView(this);
        setContentView(recyclerView);

        recyclerView.setLoadMoreView(null);//disable load more
        DemoAdapter adapter = new DemoAdapter();
        recyclerView.setAdapter(adapter);
        adapter.initData();
    }
}
