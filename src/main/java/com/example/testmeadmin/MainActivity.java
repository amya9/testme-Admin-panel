package com.example.testmeadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText email ,password ;
    private Button loginBtn;
    private ProgressBar progressBar;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email_et);
        password = findViewById(R.id.password_et);
        loginBtn = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        final Intent addIntent = new Intent(this , CategoriesActivity.class);
        if(firebaseAuth.getCurrentUser()  != null){
            //Category Intent
            startActivity(addIntent);
            finish();
            return;
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.getText().toString().isEmpty()){
                       email.setError("required");
                       return;
                }else {
                      email.setError(null);
                }
                if (password.getText().toString().isEmpty()){
                     password.setError("required");
                     return;
                }else {
                      password.setError(null);
                }
                    progressBar.setVisibility(View.VISIBLE);

                firebaseAuth.signInWithEmailAndPassword(email.getText().toString() , password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                      if (task.isSuccessful()){
                          startActivity(addIntent);
                          finish();
                      }else {
                          Toast.makeText(MainActivity.this , "Failled" , Toast.LENGTH_LONG).show();
                      }
                      progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }
}