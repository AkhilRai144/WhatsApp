package com.akhil.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.akhil.whatsapp.Models.Users;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth auth;
    GoogleSignInOptions googleSignInOptions;
    EditText etEmail,etPassword,etUserName;
    TextView tvAlreadyAccount;
    Button btnSignUp,btn_googleSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Objects.requireNonNull(getSupportActionBar()).hide();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("We are Creating your Account");
        etUserName = findViewById(R.id.etUserName);
        etEmail = findViewById(R.id.etEmail);
        tvAlreadyAccount = findViewById(R.id.tvAlreadyAccount);
        etPassword = findViewById(R.id.etPassword);
        btn_googleSignUp = findViewById(R.id.btn_googleSignUp);
        btnSignUp = findViewById(R.id.btn_signUp);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);


        btn_googleSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sigIn();
            }
        });

        tvAlreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUserName.getText().toString();
                String email = etEmail.getText().toString();
                String pass = etPassword.getText().toString();
                if (!email.isEmpty() && !pass.isEmpty()) {
                    progressDialog.show();
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Users users = new Users(username, email, pass);
                                String id = task.getResult().getUser().getUid();
                                database.getReference().child("User").child(id).setValue(users);
                                Toast.makeText(SignUpActivity.this, "Sign Up Successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                finish();

                            } else {
                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(SignUpActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
        private void sigIn(){
            Intent signIn = googleSignInClient.getSignInIntent();
            startActivityForResult(signIn,1000);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == 1000){
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d("TAG","firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch(ApiException e){
                    Log.w("TAG","Google Sign In Failed",e);
                }
            }
        }
        private void firebaseAuthWithGoogle(String idToken){
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
            auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Log.d("TAG","SigIn With Credential Successfully");
                        FirebaseUser user = auth.getCurrentUser();
                        Users users = new Users();
                        users.setUserId(user.getUid());
                        users.setUserName(user.getDisplayName());
                        users.setMail(user.getEmail());
                        users.setProfilepic(user.getPhotoUrl().toString());
                        database.getReference().child("Users").child(user.getUid()).setValue(users);

                        startActivity(new Intent(SignUpActivity.this,MainActivity.class));
                        finish();
                    }else{
                        Log.w("TAG","SigIn With Credential Failure", task.getException());
                    }
                }
            });

        }
    }