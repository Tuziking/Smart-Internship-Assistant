package com.example.smart_internship_assistant.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_internship_assistant.R;
import com.amap.api.services.help.Tip;

import java.util.ArrayList;
import java.util.List;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> implements View.OnClickListener {
    private final ArrayList<Tip> list;
    private final Context context;
    private final RecyclerView rv;
    private OnItemClickListener mOnItemClickListener;

    public RvAdapter(ArrayList<Tip> list, Context context, RecyclerView rv) {
        this.list = list;
        this.context = context;
        this.rv = rv;
    }

    @Override
    public void onClick(View view) {
        int position = rv.getChildAdapterPosition(view);

        //程序执行到此，会去执行具体实现的onItemClick()方法
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(rv, view, position, list.get(position));
            Log.i("TAG", "onClick: " + position);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView recyclerView, View view, int position, Tip data);
    }

    public void setmOnItemClickListener(OnItemClickListener clickListener) {
        this.mOnItemClickListener = clickListener;
    }

    public void setData(List<Tip> list) {
        if (list != null) {
            this.list.clear();
            this.list.addAll(list);
            notifyDataSetChanged();
            Log.i("TAG", "setData: " + list);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View view = LayoutInflater.from(context).inflate(R.layout.search_item, parent ,false);
        View view = View.inflate(context, R.layout.view_item, null);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tip tip = list.get(position);
        holder.textView.setText(tip.getName());
        Log.i("TAG", "onBindViewHolder:  getName= " + tip.getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.search_adapter_text);
        }
    }
}
