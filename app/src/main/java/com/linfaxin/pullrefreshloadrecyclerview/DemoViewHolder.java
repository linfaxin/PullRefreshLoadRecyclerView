package com.linfaxin.pullrefreshloadrecyclerview;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by linfaxin on 15/10/2.
 */
public class DemoViewHolder extends RecyclerView.ViewHolder{
    Button button;
    public DemoViewHolder(Context context) {
        super(new Button(context));

        button = (Button) itemView;
        button.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));

    }

    public void setText(String text){
        button.setText(text);
    }
}
