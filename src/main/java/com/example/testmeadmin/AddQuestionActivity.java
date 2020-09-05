package com.example.testmeadmin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.testmeadmin.adapters.QuestionAdapter;
import com.example.testmeadmin.models.CategoriesModel;
import com.example.testmeadmin.models.QuestionModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AddQuestionActivity extends AppCompatActivity {
    private EditText addQuestions ;
    private LinearLayout optionLayout;
    private Button upload;
    private RadioGroup optionBtn;
    private String categoryName;
    private int   position;
    private Dialog loadingDialog;
    private QuestionModel questionModel;
    private QuestionAdapter questionAdapter;
    private String id , setId;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Question");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //loading Dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corner));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT , LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);


        addQuestions = findViewById(R.id.addQuestions_et);
        optionLayout = findViewById(R.id.answer_layout);
        optionBtn = findViewById(R.id.options_rg);
        upload = findViewById(R.id.upload_btn);

        categoryName = getIntent().getStringExtra("categoryName");
        setId = getIntent().getStringExtra("setId");
        position = getIntent().getIntExtra("position" , -1);


        if (setId == null){
            finish();
            return;
        }
        if (position != -1){
           questionModel  = QuestionsActivity.list.get(position);
            setData();
        }
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addQuestions.getText().toString().isEmpty()){
                    addQuestions.setError("Required");
                    return;
                }
                uploadQuestion();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setData(){
             addQuestions.setText(questionModel.getQuestion());
        ((EditText)optionLayout.getChildAt(0)).setText(questionModel.getOptionA());
        ((EditText)optionLayout.getChildAt(1)).setText(questionModel.getOptionB());
        ((EditText)optionLayout.getChildAt(2)).setText(questionModel.getOptionC());
        ((EditText)optionLayout.getChildAt(3)).setText(questionModel.getOptionD());

             for (int i =0 ;i<optionLayout.getChildCount() ; i++){
                 if (((EditText)optionLayout.getChildAt(i)).getText().toString().equals(questionModel.getCorrectAnswer())){
                    RadioButton radioButton = (RadioButton)optionBtn.getChildAt(i);
                    radioButton.toggle();
                    break;
                 }
             }
    }
    private void uploadQuestion(){
        int correctIndex = -1;
        for(int i =0 ;i < optionBtn.getChildCount() ; i++){
            EditText answer = (EditText)optionLayout.getChildAt(i);
            if(answer.getText().toString().isEmpty()){
                answer.setError("Required");
            }


            RadioButton radioButton = (RadioButton)optionBtn.getChildAt(i);
            if (radioButton.isChecked()){
                correctIndex = i;
                break;
            }
        }
        if (correctIndex == -1){
            Toast.makeText(this , "Please mark your answer" , Toast.LENGTH_LONG).show();
        }

        final HashMap<String  , Object> map = new HashMap<>();
        map.put("correctAnswer" , ((EditText)optionLayout.getChildAt(correctIndex)).getText().toString() );
        map.put("optionA" , ((EditText)optionLayout.getChildAt(0)).getText().toString());
        map.put("optionB" , ((EditText)optionLayout.getChildAt(1)).getText().toString());
        map.put("optionC" ,((EditText)optionLayout.getChildAt(2)).getText().toString() );
        map.put("optionD" ,((EditText)optionLayout.getChildAt(3)).getText().toString() );
        map.put("question" , addQuestions.getText().toString());
        map.put("setId" , setId);


        if (position != -1){
            id = questionModel.getId();
        }else {
            id = UUID.randomUUID().toString();
        }
        loadingDialog.show();
        FirebaseDatabase.getInstance().getReference()
                .child("SETS").child(setId).child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    QuestionModel questionModel = new QuestionModel(id , Objects.requireNonNull(map.get("question")).toString() ,
                            Objects.requireNonNull(map.get("optionA")).toString() , Objects.requireNonNull(map.get("optionB")).toString(), Objects.requireNonNull(map.get("optionC")).toString() ,
                            Objects.requireNonNull(map.get("optionD")).toString(), Objects.requireNonNull(map.get("correctAnswer")).toString(), map.get("setId").toString());

                    if (position != -1){

                        QuestionsActivity.list.set(position,questionModel);
                    }else {
                        QuestionsActivity.list.add(questionModel);
//                        questionAdapter.notifyDataSetChanged();
                    }
                    finish();
                }else {
                       Toast.makeText(AddQuestionActivity.this, "Something went wrong" , Toast.LENGTH_LONG).show();
                }
                loadingDialog.dismiss();

            }
        });
    }
}