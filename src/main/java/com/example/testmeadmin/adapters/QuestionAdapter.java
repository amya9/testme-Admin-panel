package com.example.testmeadmin.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testmeadmin.AddQuestionActivity;
import com.example.testmeadmin.QuestionsActivity;
import com.example.testmeadmin.R;
import com.example.testmeadmin.models.QuestionModel;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.viewholder> {
    private static List<QuestionModel> list;
    private static String category;
    private static DeleteListener deleteListener;

    public QuestionAdapter(List<QuestionModel> list , String category , DeleteListener deleteListener) {
        QuestionAdapter.list = list;
        QuestionAdapter.category = category;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_item , parent , false);

        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        holder.setData(list.get(position).getQuestion() , list.get(position).getCorrectAnswer() , position);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class viewholder extends RecyclerView.ViewHolder {
        private TextView Questions , Answers;
        public viewholder(@NonNull final View itemView) {
            super(itemView);

            Questions = itemView.findViewById(R.id.question);
            Answers = itemView.findViewById(R.id.answer);


        }
        private  void setData(String questions , String answers , final int position){
            this.Questions.setText(position+1 +". " + questions);
            this.Answers.setText("Ans. " + answers);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editIntent = new Intent(itemView.getContext() , AddQuestionActivity.class);
                    editIntent.putExtra("categoryName", category);
                    editIntent.putExtra("setId" , list.get(position).getSet());
                    editIntent.putExtra("position" , position);
                    itemView.getContext().startActivity(editIntent);

                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deleteListener.onLongClick(position , list.get(position).getId());
                    return false;
                }
            });
        }
    }
    public interface DeleteListener{
        public void onLongClick(int position , String id);
    }
}
