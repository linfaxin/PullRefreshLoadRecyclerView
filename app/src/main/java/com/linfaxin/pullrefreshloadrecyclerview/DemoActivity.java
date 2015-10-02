package com.linfaxin.pullrefreshloadrecyclerview;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.linfaxin.recyclerview.PullRefreshLoadRecyclerView;
import com.linfaxin.recyclerview.overscroll.OverScrollLinearLayoutManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DemoActivity extends AppCompatActivity {
    static LinkedHashMap<String, Class<? extends Activity>> data = new LinkedHashMap<>();
    static{
        data.put("1.Pull to refresh", PullToRefreshActivity.class);
        data.put("2.Pull refresh and load", PullRefreshLoadActivity.class);
        data.put("3.Pull refresh and load grid", PullRefreshLoadGridActivity.class);
        data.put("", null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new OverScrollLinearLayoutManager(recyclerView));
        recyclerView.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ViewHolder();
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                String key = new ArrayList<>(data.keySet()).get(position);
                holder.button.setText(key);
                final Class<? extends Activity> value = data.get(key);
                if(value!=null){
                    holder.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(DemoActivity.this, value));
                        }
                    });
                }
            }


            @Override
            public int getItemCount() {
                return data.size();
            }
        });
    }

    class ViewHolder extends DemoViewHolder{
        public ViewHolder() {
            super(DemoActivity.this);
            button.setMinHeight(getResources().getDisplayMetrics().heightPixels / data.size());
        }
    }
}
