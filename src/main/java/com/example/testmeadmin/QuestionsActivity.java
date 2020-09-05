package com.example.testmeadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testmeadmin.adapters.QuestionAdapter;
import com.example.testmeadmin.models.QuestionModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class QuestionsActivity extends AppCompatActivity {
    private Button addExcelBtn;
    private QuestionAdapter questionAdapter;
     public static   List<QuestionModel> list;
    private Dialog loadingDialog;
    private DatabaseReference reference;
    private String categoryName;
    private String setId;
    private TextView loadingText;
    private static final int CELL_NUM = 6;
    private static final int QUESTION_CELL_INDEX = 0;
    private static final int OPTION_A_CELL_INDEX = 1;
    private static final int OPTION_B_CELL_INDEX = 2;
    private static final int OPTION_C_CELL_INDEX = 3;
    private static final int OPTION_D_CELL_INDEX = 4;
    private static final int CORRECT_ANS_CELL_INDEX = 5;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
         categoryName = getIntent().getStringExtra("category");
         setId = getIntent().getStringExtra("setId");
        Objects.requireNonNull(getSupportActionBar()).setTitle(categoryName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        reference = FirebaseDatabase.getInstance().getReference();

        //loading Dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.rounded_corner));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT , LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingText = loadingDialog.findViewById(R.id.loadingText);

        Button addSingleBtn = findViewById(R.id.addSingle_btn);
        addExcelBtn = findViewById(R.id.addExcel_btn);
        RecyclerView recyclerView = findViewById(R.id.addBtn_rv);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
         list = new ArrayList<>();
        questionAdapter = new QuestionAdapter(list , categoryName , new QuestionAdapter.DeleteListener() {
            @Override
            public void onLongClick(final int position, final String id) {

                new AlertDialog.Builder(QuestionsActivity.this , R.style.Theme_AppCompat_Light_Dialog_Alert)
                        .setTitle("Delete Question")
                        .setMessage("Are you sure, you want to delete this Question?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingDialog.show();
                                reference.child("SETS").child(setId).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            list.remove(position);
                                            questionAdapter.notifyItemRemoved(position);
                                        }else {
                                            Toast.makeText(QuestionsActivity.this , "Failed to delete" , Toast.LENGTH_LONG).show();
                                        }
                                        loadingDialog.dismiss();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel" , null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        recyclerView.setAdapter(questionAdapter);
        getData(categoryName , setId);
        addSingleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addQuestionIntent = new Intent(QuestionsActivity.this , AddQuestionActivity.class);
                addQuestionIntent.putExtra("categoryName" , categoryName);
                addQuestionIntent.putExtra("setId" , setId);
                startActivity(addQuestionIntent);
            }
        });

        addExcelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(QuestionsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                    selectFile();

                }else {

                    ActivityCompat.requestPermissions(QuestionsActivity.this , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE} , 101);
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectFile();
            }else {
                Toast.makeText(this , "Please Grant Permission" , Toast.LENGTH_LONG).show();
            }
        }
    }

    private void selectFile(){
        Intent selectIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        selectIntent.setType("*/*");
        selectIntent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(selectIntent , "Select File") , 102);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102){
            if (resultCode == RESULT_OK){
                assert data != null;
                String filePath = Objects.requireNonNull(data.getData()).getPath();
                assert filePath != null;
                if (filePath.endsWith(".xlsx")){
//                    Toast.makeText(this , "file selected" , Toast.LENGTH_LONG).show();

                    readFile(data.getData());
                }else {
                    Toast.makeText(this , "select the correct excel file" , Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void readFile(final Uri fileUri){
        loadingText.setText("Scanning Questions...");
        loadingDialog.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {


        final HashMap<String , Object> pMap = new HashMap<>();
        final List<QuestionModel>tempList = new ArrayList<>();
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

            int rowNum = sheet.getPhysicalNumberOfRows();
            if (rowNum >0){
                for (int r = 0;r<rowNum ;r++){
                    Row row = sheet.getRow(r);
                    if(row.getPhysicalNumberOfCells() == CELL_NUM) {
                        String question = getCellData(row, QUESTION_CELL_INDEX, formulaEvaluator);
                        String optionA = getCellData(row, OPTION_A_CELL_INDEX, formulaEvaluator);
                        String optionB = getCellData(row, OPTION_B_CELL_INDEX, formulaEvaluator);
                        String optionC = getCellData(row, OPTION_C_CELL_INDEX, formulaEvaluator);
                        String optionD = getCellData(row, OPTION_D_CELL_INDEX, formulaEvaluator);
                        String correctAns = getCellData(row, CORRECT_ANS_CELL_INDEX, formulaEvaluator);

                        if (correctAns.equals(optionA) || correctAns.equals(optionB) || correctAns.equals(optionC) || correctAns.equals(optionD) ){
                            HashMap<String , Object>questionMap = new HashMap<>();
                            questionMap.put("correctAnswer" , correctAns);
                            questionMap.put("optionA" , optionA);
                            questionMap.put("optionB" , optionB);
                            questionMap.put("optionC" , optionC);
                            questionMap.put("optionD" , optionD);
                            questionMap.put("question" , question);
                            questionMap.put("setId" , setId);

                            String id = UUID.randomUUID().toString();
                            pMap.put(id , questionMap);
                            tempList.add(new QuestionModel(id , correctAns , optionA , optionB , optionC , optionD , question , setId));

                        }else {
                            final int finalR1 = r;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingText.setText("Loading...");
                                    loadingDialog.dismiss();
                                    Toast.makeText(QuestionsActivity.this , "Row No " +(finalR1 + 1) +" has no correct answer" , Toast.LENGTH_LONG).show();

                                }
                            });
                            return;
                        }
                    }else {
                        final int finalR = r;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingText.setText("Loading...");
                                loadingDialog.dismiss();
                                Toast.makeText(QuestionsActivity.this , "Row No "+(finalR +1)+ "has incorrect cell data" , Toast.LENGTH_LONG).show();
                            }
                        });
                      return;

                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingText.setText("Uploading...");

                        FirebaseDatabase.getInstance().getReference()
                                .child("SETS").child(setId).updateChildren(pMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){

                                    list.addAll(tempList);
                                    questionAdapter.notifyDataSetChanged();
                                }else {
                                    loadingText.setText("Loading...");
                                    Toast.makeText(QuestionsActivity.this , "Something went wrong" , Toast.LENGTH_LONG).show();
                                }
                                loadingDialog.dismiss();
                            }
                        });

                    }
                });

            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingText.setText("Loading...");
                        loadingDialog.dismiss();
                        Toast.makeText(QuestionsActivity.this , "File is Empty" , Toast.LENGTH_LONG).show();

                    }
                });
             }
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingText.setText("Loading...");
                    loadingDialog.dismiss();
                    Toast.makeText(QuestionsActivity.this , e.getMessage() , Toast.LENGTH_LONG).show();
                }
            });
        } catch (final IOException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingText.setText("Loading...");
                    loadingDialog.dismiss();
                    Toast.makeText(QuestionsActivity.this , e.getMessage() , Toast.LENGTH_LONG).show();
                }
            });
        }
            }
        });
    }
    private String getCellData(Row row , int cellPosition , FormulaEvaluator formulaEvaluator){
        String value  = "";
        Cell cell = row.getCell(cellPosition);

        switch (cell.getCellType()){
            case Cell.CELL_TYPE_BOOLEAN:
                return value + cell.getBooleanCellValue();
                case Cell.CELL_TYPE_NUMERIC:
                    return value + cell.getNumericCellValue();
                    case Cell.CELL_TYPE_STRING:
                        return value + cell.getStringCellValue();
            default:
                return value;
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();

        }
        return super.onOptionsItemSelected(item);
    }
    private void getData(String  categoryName , final String setId){
        loadingDialog.show();
                reference.child("SETS").child(setId).addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                    String id = dataSnapshot1.getKey();
                    String question = dataSnapshot1.child("question").getValue().toString();
                    String optionA = dataSnapshot1.child("optionA").getValue().toString();
                    String optionB = dataSnapshot1.child("optionB").getValue().toString();
                    String optionC = dataSnapshot1.child("optionC").getValue().toString();
                    String optionD = dataSnapshot1.child("optionD").getValue().toString();
                    String correctAns = dataSnapshot1.child("correctAnswer").getValue().toString();

                    list.add(new QuestionModel(id , correctAns ,optionA , optionB ,optionC , optionD , question , setId));
                }
                loadingDialog.dismiss();
                questionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(QuestionsActivity.this , "Something went wrong" , Toast.LENGTH_LONG).show();

                loadingDialog.dismiss();
                finish();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        questionAdapter.notifyDataSetChanged();
    }

}