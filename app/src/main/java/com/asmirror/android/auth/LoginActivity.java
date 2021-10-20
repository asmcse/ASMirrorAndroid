package com.asmirror.android.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.asmirror.android.MainActivity;
import com.asmirror.android.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout InputEmailField, InputPasswordField;
    private EditText InputEmail, InputPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        InputEmailField = findViewById(R.id.login_input_email_field);
        InputPasswordField = findViewById(R.id.login_input_password_field);
        InputEmail = findViewById(R.id.login_input_email);
        InputPassword = findViewById(R.id.login_input_password);
        Button ButtonLogin = findViewById(R.id.login_button);
        TextView ForgetPassword = findViewById(R.id.login_text_forget_password);
        TextView CreateAccount = findViewById(R.id.login_text_create_account);

        ButtonLogin.setOnClickListener(v -> LoginUser());
        ForgetPassword.setOnClickListener(v -> SendUserToPasswordActivity());
        CreateAccount.setOnClickListener(v -> SendUserToRegisterActivity());
    }

    private void LoginUser() {
        String email = InputEmail.getText().toString();
        String password = InputPassword.getText().toString();

        if (email.isEmpty()) {
            InputEmailField.setError("Enter email");
        } if (password.isEmpty()) {
            InputPasswordField.setError("Enter password");
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    SendUserToMainActivity();
                }
            });
        }
    }

    private void SendUserToPasswordActivity() {
        Intent PasswordIntent = new Intent(this, ForgetPasswordActivity.class);
        startActivity(PasswordIntent);
    }

    private void SendUserToRegisterActivity() {
        Intent RegisterIntent = new Intent(this, RegisterActivity.class);
        startActivity(RegisterIntent);
    }

    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(this, MainActivity.class);
        MainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
    }

}