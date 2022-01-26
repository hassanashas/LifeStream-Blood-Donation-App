package com.example.lifestream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout email, password;
    Button submit_button;
    TextView forgot_password;
    ProgressDialog loading_dialog;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.login_email_address);
        password = findViewById(R.id.login_password);
        submit_button = findViewById(R.id.login_submit_button);
        loading_dialog = new ProgressDialog(LoginActivity.this);
        mAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null)
                {
                    Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, UserMainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };


        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!emailInput() | !passwordInput())
                {
                    Toast.makeText(LoginActivity.this, "Error while logging in ", Toast.LENGTH_SHORT).show();
                    return;
                }
                loading_dialog.setMessage("Please Wait. We are logging you in...");
                loading_dialog.setCanceledOnTouchOutside(false);
                loading_dialog.show();

                mAuth.signInWithEmailAndPassword(email.getEditText().getText().toString().trim(),
                        password.getEditText().getText().toString().trim()).
                        addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(LoginActivity.this, "Successfully Logged in", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, UserMainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else
                                    Toast.makeText(LoginActivity.this, "Error: "+task.getException().toString(),
                                            Toast.LENGTH_SHORT).show();
                                loading_dialog.dismiss();
                            }
                        });
            }
        });


        password.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordInput();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        email.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emailInput();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });



    }

    public void Go_To_Registration_Page(View view) {
        Intent intent = new Intent(LoginActivity.this, DononRegistrationActivity.class);
        startActivity(intent);
    }


    boolean emailInput() {
        String emailText = email.getEditText().getText().toString().trim();

        if (emailText.isEmpty())
        {
            email.setError("Email is Required");
            return false;
        }
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        if (!pattern.matcher(emailText).matches())
        {
            email.setError("Please enter Correct Email Address");
            return false;
        }
        else
        {
            email.setError(null);
            return true;
        }
    }

    boolean passwordInput()
    {
        String passwordText = password.getEditText().getText().toString().trim();
        if (passwordText.isEmpty())
        {
            password.setError("Password is Required");
            return false;
        }
        password.setError(null);
        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }
}