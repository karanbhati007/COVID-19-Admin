package com.ksb.covid_19admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    EditText etUsername, etPassword;
    Button btSignin;
    static FirebaseAuth mAuth;
    String username, password;
    View contextView;
    AlertDialog alertDialog;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    static FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        contextView = findViewById(android.R.id.content);
        alertDialog= new SpotsDialog.Builder().setContext(this).build();
        mAuth = FirebaseAuth.getInstance();

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btSignin = findViewById(R.id.bt_signin);

        btSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                /////////
                currentUser = mAuth.getCurrentUser();

                if(currentUser!=null)
                {
                    // TODO: 18-05-2020  
                   // java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
                   // String formatedDate = dateFormat.format(new Date(Long.valueOf("1589735313424")).getTime());
                    // Sign In
                    Toast.makeText(MainActivity.this, "Signed In", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    // Sign Out
                    Toast.makeText(MainActivity.this, "Not Signed In", Toast.LENGTH_SHORT).show();
                }
            }
        };

    }


    private void validate(){
        username=etUsername.getText().toString().trim();
        password=etPassword.getText().toString().trim();

        if(!username.equals("") && !password.equals("") && username.length()>0 && password.length()>=6){
            goToLogin();
        }
        else{

            if(password.length()<6)
            {
                Toast.makeText(this, "Password Length is too Short", Toast.LENGTH_SHORT).show();
            }

            Snackbar.make(contextView, "Invalid inputs", Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private void goToLogin() {
        alertDialog.setMessage("Signing in..");
        alertDialog.show();
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            alertDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            Snackbar.make(contextView, "Welcome Admin", Snackbar.LENGTH_SHORT)
                                    .show();
                        } else {
                            alertDialog.dismiss();
                            Snackbar.make(contextView, "Wrong Password, please try again", Snackbar.LENGTH_SHORT)
                                    .show();
                        }

                    }
                });

    }



    @Override
    public void onStart() {
        super.onStart();


        currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthStateListener);
        if(currentUser!=null){
            Log.i("CURRENT USER",currentUser.getEmail());
            updateUI(currentUser);

        }

    }

    @Override
    protected void onStop() {
            super.onStop();
            if(mAuthStateListener!= null)
            {
              mAuth.removeAuthStateListener(mAuthStateListener);
            }
    }

    private void updateUI(FirebaseUser currentUser) {
        Intent intent = new Intent(MainActivity.this,AdminActivity.class);
        intent.putExtra("currentUser",currentUser);
        startActivity(intent);
        finish();
    }
}
