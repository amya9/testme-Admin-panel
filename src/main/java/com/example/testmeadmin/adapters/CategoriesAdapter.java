package com.example.testmeadmin.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.testmeadmin.R;
import com.example.testmeadmin.SetsActivity;
import com.example.testmeadmin.models.CategoriesModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {
    private DeleteListener deleteListener;
     private List<CategoriesModel> categoriesModelList;

    public CategoriesAdapter(List<CategoriesModel> categoriesModelList , DeleteListener deleteListener) {
        this.categoriesModelList = categoriesModelList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories_item , parent ,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(categoriesModelList.get(position).getUrl() , categoriesModelList.get(position).getName() , categoriesModelList.get(position).getKey() , position);

    }

    @Override
    public int getItemCount() {
        return categoriesModelList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView circleIV;
        private TextView titles;
        private ImageButton deleteBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            circleIV = itemView.findViewById(R.id.categories_iv);
            titles = itemView.findViewById(R.id.categories_title);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }
        private void setData(String url , final String title , final String key , final int position){
            Glide.with(itemView.getContext()).load(url).into(circleIV);
            titles.setText(title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent setsIntent = new Intent(itemView.getContext() , SetsActivity.class);
                    setsIntent.putExtra("title" , title);
                    setsIntent.putExtra("position" , position);
                    setsIntent.putExtra("key" , key);
                    itemView.getContext().startActivity(setsIntent);
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteListener.onDelete(key , position);
                }
            });
        }
    }

    public  interface DeleteListener{
        void onDelete(String key, int position);
    }
}
