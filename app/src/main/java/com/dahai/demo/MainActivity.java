package com.dahai.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dahai.pullrefreshlayout.DefaultHeader;
import com.dahai.pullrefreshlayout.PullToRefreshLayout;

public class MainActivity extends AppCompatActivity {

    private PullToRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        mRefreshLayout.setHeaderView(new DefaultHeader(this));
        mRefreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.refreshComplete();
                    }
                },3000);
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new Adapter());
    }

    private void initView() {
        mRefreshLayout = (PullToRefreshLayout) findViewById(R.id.mRefreshLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
    }


    class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(MainActivity.this).inflate(R.layout.item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((Holder) holder).tv.setText("ITEM--->" + position);
        }

        @Override
        public int getItemCount() {
            return 25;
        }
    }

    class Holder extends RecyclerView.ViewHolder {

        TextView tv;

        public Holder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
        }
    }
}
