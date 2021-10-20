package com.asmirror.android.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.asmirror.android.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout InputNameField, InputEmailField, InputPasswordField;
    private EditText InputName, InputEmail, InputPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        InputNameField = findViewById(R.id.register_input_name_field);
        InputEmailField = findViewById(R.id.register_input_email_field);
        InputPasswordField = findViewById(R.id.register_input_password_field);
        InputName = findViewById(R.id.register_input_name);
        InputEmail = findViewById(R.id.register_input_email);
        InputPassword = findViewById(R.id.register_input_password);
        Button buttonRegister = findViewById(R.id.register_button);
        TextView alreadyRegistered = findViewById(R.id.register_text_already_registered);

        buttonRegister.setOnClickListener(v -> RegisterUser());
        alreadyRegistered.setOnClickListener(v -> SendUserToRegisterActivity());
    }

    private void SendUserToRegisterActivity() {
        Intent RegisterIntent = new Intent(this, RegisterActivity.class);
        startActivity(RegisterIntent);
    }

    private void RegisterUser() {
        String name = InputName.getText().toString();
        String email = InputEmail.getText().toString();
        String password = InputPassword.getText().toString();

        if (name.isEmpty()) {
            InputNameField.setError("Enter name");
        } if (email.isEmpty()) {
            InputEmailField.setError("Enter email");
        } if (password.isEmpty()) {
            InputPasswordField.setError("Enter password");
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUser User = mAuth.getCurrentUser();
                    if (User != null) {
                        String UserID = User.getUid();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                        User.updateProfile(profileUpdates);

                        Map<String, Object> UserMap = new HashMap<>();
                        UserMap.put("name", name);
                        UserMap.put("email", email);

                        RootRef.child("Users").child(UserID).updateChildren(UserMap).addOnCompleteListener(result -> {
                            if (!result.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, Objects.requireNonNull(result.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                SendUserToProfileImageActivity();
                            }
                        });
                    }
                }
            });
        }
    }

    private void SendUserToProfileImageActivity() {
        Intent ProfileImageIntent = new Intent(this, ProfileImageActivity.class);
        ProfileImageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(ProfileImageIntent);
    }

}