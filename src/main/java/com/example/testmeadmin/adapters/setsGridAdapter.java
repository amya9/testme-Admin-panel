package com.example.testmeadmin.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.example.testmeadmin.QuestionsActivity;
import com.example.testmeadmin.R;

import java.util.List;

public class setsGridAdapter extends BaseAdapter {
    public List<String> sets;
    private String category;
    private AddGridListener addGridListener;

    public setsGridAdapter(List<String> sets , String  category , AddGridListener addGridListener) {
        this.sets = sets;
        this.category = category;
        this.addGridListener = addGridListener;
    }

    @Override
    public int getCount() {
        return sets.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final View view ;
        if(convertView == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sets_item , parent ,false);
        }else {
            view = convertView;
        }

        if (position == 0){
            ((TextView) view.findViewById(R.id.sets_number)).setText("+");
        }else {
            ((TextView) view.findViewById(R.id.sets_number)).setText(String.valueOf(position));
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0){
                    //add sets
                    addGridListener.addSets();
                }else {
                Intent questionIntent = new Intent(v.getContext() , QuestionsActivity.class);
                questionIntent.putExtra("category" , category);
                questionIntent.putExtra("setId" , sets.get(position-1));
                v.getContext().startActivity(questionIntent);
                }
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (position !=0) {
                    addGridListener.onLongClick(position , sets.get(position-1));
                }
                return false;
            }
        });

        return view;
    }
    public interface AddGridListener{
        void addSets();
        void onLongClick(int position , String setId);
    }
}
