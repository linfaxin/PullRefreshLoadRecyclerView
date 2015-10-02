package com.linfaxin.pullrefreshloadrecyclerview;

import android.view.ViewGroup;

import com.linfaxin.recyclerview.PullRefreshLoadRecyclerView;
import com.linfaxin.recyclerview.headfoot.LoadMoreView;
import com.linfaxin.recyclerview.headfoot.RefreshView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by linfaxin on 15/10/2.
 */
public class DemoAdapter extends PullRefreshLoadRecyclerView.LoadRefreshAdapter<DemoViewHolder>{
    private ArrayList<String> data = new ArrayList<>();
    Random r = new Random();
    boolean failFlag = r.nextBoolean();

    public void initData(){
        data.clear();
        for(int i=0; i<20; i++){
            data.add(r.nextInt(100)+"");
        }
        notifyDataSetChanged();
    }
    private void loadMoreData(){
        for(int i=0; i<20; i++){
            data.add(r.nextInt(100)+"");
        }
        notifyDataSetChanged();
    }

    @Override
    public DemoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DemoViewHolder(parent.getContext());
    }

    @Override
    public void onBindViewHolder(DemoViewHolder holder, int position) {
        holder.setText("Item"+position+"_"+data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onRefresh(PullRefreshLoadRecyclerView pullRefreshLoadRecyclerView, final RefreshView refreshView) {
        refreshView.postDelayed(new Runnable() {
            @Override
            public void run() {
                initData();
                refreshView.setState(RefreshView.STATE_NORMAL);
            }
        }, 1000);
    }

    @Override
    public void onLoadMore(PullRefreshLoadRecyclerView pullRefreshLoadRecyclerView, final LoadMoreView loadMoreView) {
        loadMoreView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(failFlag=!failFlag){
                    loadMoreData();
                    loadMoreView.setState(LoadMoreView.STATE_NORMAL);
                }else {
                    loadMoreView.setState(LoadMoreView.STATE_LOAD_FAIL);
                }
            }
        }, 1000);
    }
}
