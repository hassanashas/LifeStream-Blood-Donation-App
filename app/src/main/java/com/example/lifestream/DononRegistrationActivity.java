package com.example.lifestream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lifestream.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class DononRegistrationActivity extends AppCompatActivity {

    TextInputLayout name, phone, email, password, confirm_password;
    CircleImageView profile_pic;
    String lat, lng;
    AppCompatSpinner blood_group, user_type;
    Button register_submit_button;
    Uri uri;

    ProgressDialog loading_dialog;
    FirebaseAuth mAuth;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        lat = "null";

        setContentView(R.layout.activity_donon_registration);
        name = findViewById(R.id.register_full_name);
        phone = findViewById(R.id.register_phone_number);
        email = findViewById(R.id.register_email_address);
        password = findViewById(R.id.register_password);
        confirm_password = findViewById(R.id.register_confirm_password);
        register_submit_button = findViewById(R.id.register_submit_button);
        profile_pic = findViewById(R.id.profile_image);
        blood_group = findViewById(R.id.register_blood_group);
        user_type = findViewById(R.id.register_user_type);
        loading_dialog = new ProgressDialog(DononRegistrationActivity.this);
        mAuth = FirebaseAuth.getInstance();


        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 100);
            }
        });

        register_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!emailInput() | !phoneInput() | !passwordInput() | !confirmPasswordInput() | !nameInput() | !locationInput())
                {
                    Toast.makeText(DononRegistrationActivity.this, "Unable to Register", Toast.LENGTH_SHORT).show();
                    return;
                }

                String emailText, passwordText, confirm_passwordText, bloodGroup, userType, nameText, phoneText;
                emailText = email.getEditText().getText().toString().trim();
                nameText = name.getEditText().getText().toString().trim();
                passwordText = password.getEditText().getText().toString().trim();
                confirm_passwordText = confirm_password.getEditText().getText().toString().trim();
                bloodGroup = blood_group.getSelectedItem().toString();
                userType = user_type.getSelectedItem().toString();
                phoneText = phone.getEditText().getText().toString().trim();

                if (bloodGroup.equals("Select your Blood Group"))
                {
                    Toast.makeText(DononRegistrationActivity.this, "Please Select Blood Group", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (userType.equals("Select User Type"))
                {
                    Toast.makeText(DononRegistrationActivity.this, "Please Select User Type", Toast.LENGTH_SHORT).show();
                    return;
                }

                loading_dialog.setMessage("Please wait while we register your account...");
                loading_dialog.setCanceledOnTouchOutside(false);
                loading_dialog.show();

                mAuth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful())
                        {
                            String err = task.getException().toString();
                            Toast.makeText(DononRegistrationActivity.this, "Error: " + err, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            String currentID = mAuth.getCurrentUser().getUid();
                            myRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentID);

                            HashMap user = new HashMap();
                            user.put("name", nameText);
                            user.put("email", emailText);
                            user.put("phone", phoneText);
                            user.put("bloodgroup", bloodGroup);
                            user.put("usertype", userType);
                            user.put("latitude", lat);
                            user.put("longitude", lng);

                            myRef.updateChildren(user).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (!task.isSuccessful())
                                        Toast.makeText(DononRegistrationActivity.this, "Error: " + task.getException().toString(),
                                                Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(DononRegistrationActivity.this, "Registration has been successful!",
                                                Toast.LENGTH_SHORT).show();
                                    finish();
                                    // loading_dialog.dismiss();
                                }
                            });

                            if (uri != null)
                            {
                                StorageReference filePath = FirebaseStorage.getInstance().getReference().child("Profile Images").
                                        child(currentID);
                                Bitmap bitmap = null;

                                try
                                {
                                    bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), uri);
                                }catch(IOException e)
                                {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                                byte[] data = byteArrayOutputStream.toByteArray();
                                UploadTask uploadTask = filePath.putBytes(data);

                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(DononRegistrationActivity.this, "Failed to Upload Image",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        if (taskSnapshot.getMetadata() != null && taskSnapshot.getMetadata().getReference() != null)
                                        {
                                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String imageUrl = uri.toString();
                                                    Map newMap = new HashMap();
                                                    newMap.put("profilepic", imageUrl);

                                                    myRef.updateChildren(newMap).addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            if (task.isSuccessful())
                                                                Toast.makeText(DononRegistrationActivity.this, "Successful!",
                                                                        Toast.LENGTH_SHORT).show();
                                                            else
                                                                Toast.makeText(DononRegistrationActivity.this,
                                                                        task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                                    finish();

                                                }
                                            });
                                        }
                                    }
                                });

                                Intent intent = new Intent(DononRegistrationActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                loading_dialog.dismiss();

                            }


                        }
                    }
                });



            }
        });

        confirm_password.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                confirmPasswordInput();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        phone.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                phoneInput();
            }

            @Override
            public void afterTextChanged(Editable editable) {

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

        name.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                nameInput();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null)
        {
            uri = data.getData();
            profile_pic.setImageURI(uri);
        }

        if (requestCode == 101 && resultCode == RESULT_OK && data != null)
        {
            lat = data.getStringExtra("lat");
            lng = data.getStringExtra("lng");
            Toast.makeText(DononRegistrationActivity.this, "Location Selected", Toast.LENGTH_SHORT).show();
        }
    }


    boolean locationInput() {
        if (lat.equals("null"))
        {
            Toast.makeText(DononRegistrationActivity.this, "You Must Select a Location", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    boolean nameInput()
    {
        if (name.getEditText().getText().toString().trim().isEmpty()) {
            name.setError("Name can't be empty");
            return false;
        }
        name.setError(null);
        return true;
    }

    boolean emailInput() {
        String emailText = email.getEditText().getText().toString().trim();

        if (emailText.isEmpty())
        {
            email.setError("Email can't be empty");
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

    boolean phoneInput()
    {
        String phoneText = phone.getEditText().getText().toString().trim();

        if (phoneText.length() != 11)
        {
            phone.setError("Phone Number must have 11 digits.");
            return false;
        }
        phone.setError(null);
        return true;
    }

    boolean passwordInput()
    {
        String passwordText = password.getEditText().getText().toString().trim();
        if (passwordText.isEmpty())
        {
            password.setError("Password can't be empty");
            return false;
        }
        password.setError(null);
        return true;
    }

    boolean confirmPasswordInput()
    {
        String confirmPasswordText = confirm_password.getEditText().getText().toString().trim();
        if (!confirmPasswordText.equals(password.getEditText().getText().toString().trim()))
        {
            confirm_password.setError("Entered passwords do not match");
            return false;
        }
        confirm_password.setError(null);
        return true;
    }


    public void Go_To_Login_Page(View view) {
        Intent intent = new Intent(DononRegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void get_user_location(View view) {
        Intent intent = new Intent(DononRegistrationActivity.this, com.example.lifestream.Map.class);
        startActivityForResult(intent, 101);
    }
}