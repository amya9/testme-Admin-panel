package com.example.testmeadmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.testmeadmin.adapters.setsGridAdapter;
import com.example.testmeadmin.models.QuestionModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.UUID;

public class SetsActivity extends AppCompatActivity {
    private GridView gridView;
    private Dialog loadingDialog;
    private setsGridAdapter gridAdapter;
    private DatabaseReference reference;
    private String categoryName;
    private List<String> sets;
//    private InterstitialAd mInterstitialAd;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //loading Dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corner));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT , LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);

        categoryName = getIntent().getStringExtra("title");

        getSupportActionBar().setTitle(categoryName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        reference = FirebaseDatabase.getInstance().getReference();

        gridView = findViewById(R.id.sets_gv);
        sets = CategoriesActivity.list.get(getIntent().getIntExtra("position", 0)).getSets();
//        setsGridAdapter gridAdapter = new setsGridAdapter(16);
                 gridAdapter = new setsGridAdapter( sets, getIntent().getStringExtra("title"), new setsGridAdapter.AddGridListener() {
            @Override
            public void addSets() {
                loadingDialog.show();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final String id  = UUID.randomUUID().toString();
                database.getReference().child("categories").child(getIntent().getStringExtra("key")).child("sets").child(id).setValue("setId").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                             sets.add(id);
                            gridAdapter.notifyDataSetChanged();
                        }else {
                         Toast.makeText(SetsActivity.this , "Something went  wrong" , Toast.LENGTH_LONG).show();
                        }
                        loadingDialog.dismiss();
                    }
                });
            }
            @Override
            public void onLongClick(final int position , final String setId) {
                new AlertDialog.Builder(SetsActivity.this , R.style.Theme_AppCompat_Light_Dialog_Alert)
                        .setTitle("Delete SET "+position)
                        .setMessage("Are you sure, you want to delete this Set?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingDialog.show();
                                         reference.child("SETS").child(setId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                                 if (task.isSuccessful()){
                                                     reference.child("categories").child( CategoriesActivity.list.get(getIntent().getIntExtra("position", 0)).getKey())
                                                             .child("sets").child(setId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                         @Override
                                                         public void onComplete(@NonNull Task<Void> task) {
                                                             if (task.isSuccessful()){
                                                                 gridAdapter.sets.remove(setId);
                                                                 gridAdapter.notifyDataSetChanged();
                                                             }else {
                                                                 Toast.makeText(SetsActivity.this , "Failed to delete" , Toast.LENGTH_LONG).show();
                                                             }
                                                         }
                                                     });
                                                 }else {
                                                     Toast.makeText(SetsActivity.this, "Something went wrong" , Toast.LENGTH_LONG).show();
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
        gridView.setAdapter(gridAdapter);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}