package com.app.autismplay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.autismplay.activity.HomeActivity;
import com.app.autismplay.activity.RegisterActivity;
import com.app.autismplay.helper.ConfigurationFirebase;
import com.app.autismplay.helper.ConfigurationProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private TextView txtRegister;
    private EditText txtEmail,txtPassword;
    private ProgressDialog progressDialog;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        setContentView(R.layout.activity_main);

        progressDialog = ConfigurationProgressDialog.getProgressDialog(this);
        progressDialog.show();

        auth = ConfigurationFirebase.getReferenceAutentication();
        if(auth.getCurrentUser() !=null){
            progressDialog.dismiss();
            startActivity(new Intent(MainActivity.this,HomeActivity.class));
            finish();
        }
        progressDialog.dismiss();
        txtRegister = findViewById(R.id.TextGoToCadastro);
        txtEmail = findViewById(R.id.EmailTxt);
        txtPassword = findViewById(R.id.PassWordTxt);


        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                finish();
            }
        });

    }
    public void ValidAndSignin(View view){
        String email,password;
        email = txtEmail.getText().toString().trim();
        password = txtPassword.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            txtEmail.setError("Email Invalido");
            txtEmail. setFocusable(true);
        }else{
            if(!password.equals("") && password.length()>=5)
            loginUser(email, password);
        }
    }
    private void loginUser(String email,String password){
        progressDialog.show();
        auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            FirebaseUser user = auth.getCurrentUser();
                            Toast.makeText(MainActivity.this,"Usuario Logado !",Toast.LENGTH_LONG).show();
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        }else{
                            progressDialog.dismiss();
                        }
                    }
                });
    }
}