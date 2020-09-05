package com.example.testmeadmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testmeadmin.adapters.CategoriesAdapter;
import com.example.testmeadmin.models.CategoriesModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoriesActivity extends AppCompatActivity {
    private Dialog loadingDialog , addCategoryDialog;
    private RecyclerView recyclerView ;
    private CircleImageView addImage ;
    private Button addCategoryBtn;
    private EditText addCategoryName;
    private  String downloadUrl;
    private Uri image;

    FirebaseDatabase database;
    DatabaseReference reference;
    public static List<CategoriesModel> list;
    private  CategoriesAdapter categoriesAdapter;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        Toolbar toolbar = findViewById(R.id.toolbar);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");
        //loading Dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corner));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT , LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        //loading category Dialog
          setCategoryDialog();
        recyclerView = findViewById(R.id.category_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        loadingDialog.show();
         list = new ArrayList<>();
         categoriesAdapter = new CategoriesAdapter(list, new CategoriesAdapter.DeleteListener() {
             @Override
             public void onDelete(final String key , final int position) {
                 new AlertDialog.Builder(CategoriesActivity.this , R.style.Theme_AppCompat_Light_Dialog_Alert)
                         .setTitle("Delete Category")
                         .setMessage("Are you sure, you want to delete this category?")
                         .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 loadingDialog.show();
                                 reference.child("categories").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                         if (task.isSuccessful()){
                                             for (String setIds :list.get(position).getSets()){
                                                 reference.child("SETS").child(setIds).removeValue();
                                             }
                                             list.remove(position);
                                             categoriesAdapter.notifyDataSetChanged();
                                             loadingDialog.dismiss();
                                         }else {
                                             Toast.makeText(CategoriesActivity.this , "Failed to delete" , Toast.LENGTH_LONG).show();
                                             loadingDialog.dismiss();
                                         }
                                         }
                                 });
                             }
                          })
                         .setNegativeButton("Cancel" , null)
                         .setIcon(android.R.drawable.ic_dialog_alert)
                         .show();
             }
         });
        recyclerView.setAdapter(categoriesAdapter);
        loadingDialog.show();
        reference.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                   List<String> sets = new ArrayList<>();
                   for (DataSnapshot dataSnapshot2 : dataSnapshot1.child("sets").getChildren()){
                       sets.add(dataSnapshot2.getKey());
                   }
                   list.add(new CategoriesModel(dataSnapshot1.child("name").getValue().toString(),
                           sets,
                           dataSnapshot1.child("url").getValue().toString() ,
                           dataSnapshot1.getKey()));
               }
               categoriesAdapter.notifyDataSetChanged();
               loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(CategoriesActivity.this , error.getMessage() , Toast.LENGTH_LONG).show();
                loadingDialog.dismiss();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu , menu);
        return super.onCreateOptionsMenu(menu);
    }
// function for menu items logout and add category
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.addItem){
            //dialog will show here
            addCategoryDialog.show();
        }
        if (item.getItemId() == R.id.logout){
            new AlertDialog.Builder(CategoriesActivity.this , R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle("Logout ")
                    .setMessage("Are you sure, you want to logout?")
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                       loadingDialog.show();
                            FirebaseAuth.getInstance().signOut();
                            Intent logoutIntent = new Intent(CategoriesActivity.this,MainActivity.class);
                            startActivity(logoutIntent);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel" , null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }
        return super.onOptionsItemSelected(item);
    }

    // method to set category dialog
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setCategoryDialog(){
        addCategoryDialog = new Dialog(this);
        addCategoryDialog.setContentView(R.layout.add_category_dialog);
        Objects.requireNonNull(addCategoryDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.rounded_box));
        addCategoryDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT , LinearLayout.LayoutParams.WRAP_CONTENT);
        addCategoryDialog.setCancelable(true);

        addImage = addCategoryDialog.findViewById(R.id.add_image);
        addCategoryName = addCategoryDialog.findViewById(R.id.add_category);
        addCategoryBtn = addCategoryDialog.findViewById(R.id.add_btn);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent , 101);
            }
        });

        addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addCategoryName.getText().toString().isEmpty() || addCategoryName.getText() == null){
                    addCategoryName.setError("Required");
                    return;
                }
                for (CategoriesModel model : list){
                    if (addCategoryName.getText().toString().equals(model.getName())){
                        addCategoryName.setError("category name already exists");
                        return;
                    }
                }
                if(image == null){
                    Toast.makeText(CategoriesActivity.this , "Please Select Your Image" , Toast.LENGTH_LONG).show();
                    return;
                }
                addCategoryDialog.dismiss();
                // else we upload data to firebase
                uploadData();
            }
        });
    }

    // over write activity result to set downloadable image url
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101){
            if (resultCode == RESULT_OK){
                 image = data.getData();
                addImage.setImageURI(image);
            }
        }
    }

    // method to upload upload dialog data into firebase database
    private  void uploadData(){
           loadingDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference imageReference =  storageReference.child("categories").child(image.getLastPathSegment());

       UploadTask uploadTask = imageReference.putFile(image);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                             downloadUrl = task.getResult().toString();
                             uploadCategoryName();
                        }else {
                            loadingDialog.dismiss();
                            Toast.makeText(CategoriesActivity.this , "Something Went Wrong" , Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    // Handle failures
                    loadingDialog.dismiss();
                    Toast.makeText(CategoriesActivity.this , "Something Went Wrong" , Toast.LENGTH_LONG).show();

                }
            }
        });
    }
  // method to upload  category name into firebase database
    private  void uploadCategoryName(){
        Map<String , Object> map = new HashMap<>();
        map.put("name" , addCategoryName.getText().toString());
        map.put("sets" , 0);
        map.put("url" , downloadUrl);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String id = UUID.randomUUID().toString();
        database.getReference().child("categories").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                     list.add(new CategoriesModel(addCategoryName.getText().toString() , new ArrayList<String>(), downloadUrl , id));
                     categoriesAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(CategoriesActivity.this , "Something Went Wrong" , Toast.LENGTH_LONG).show();
                }
                loadingDialog.dismiss();
            }
        });
    }
}