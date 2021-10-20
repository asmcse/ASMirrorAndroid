package com.asmirror.android.mirror;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.asmirror.android.MainActivity;
import com.asmirror.android.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class RegisterMirrorActivity extends AppCompatActivity {
    private TextInputLayout InputMirrorIPView, InputMirrorIDView, InputMirrorPasswordView;
    private EditText InputMirrorIP, InputMirrorID, InputMirrorPassword;

    private DatabaseReference RootRef;
    private FirebaseUser CurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_mirror);

        Toolbar toolbar = findViewById(R.id.register_mirror_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.add_mirror));

        RootRef = FirebaseDatabase.getInstance().getReference();
        CurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        InputMirrorIPView = findViewById(R.id.register_mirror_input_ip_field);
        InputMirrorIDView = findViewById(R.id.register_mirror_input_mid_field);
        InputMirrorPasswordView = findViewById(R.id.register_mirror_input_password_field);
        InputMirrorIP = findViewById(R.id.register_mirror_input_ip);
        InputMirrorID = findViewById(R.id.register_mirror_input_mid);
        InputMirrorPassword = findViewById(R.id.register_mirror_input_password);
        Button RegisterMirrorButton = findViewById(R.id.register_mirror_button);
        TextView RegisterMirrorBack = findViewById(R.id.register_mirror_back_text);

        RegisterMirrorButton.setOnClickListener(v -> RegisterMirror());
        RegisterMirrorBack.setOnClickListener(v -> SendUserToMainActivity());
    }

    private void RegisterMirror() {
        String ip = InputMirrorIP.getText().toString();
        String mid = InputMirrorID.getText().toString();
        String password = InputMirrorPassword.getText().toString();

        if (ip.isEmpty()) {
            InputMirrorIPView.setError("Enter IP address");
        }
        if (mid.isEmpty()) {
            InputMirrorIDView.setError("Enter mirror ID");
        }
        if (password.isEmpty()) {
            InputMirrorPasswordView.setError("Enter password");
        } else {
            HashMap<String, Object> MirrorMap = new HashMap<>();
            MirrorMap.put("mip", ip);
            MirrorMap.put("mid", mid);
            MirrorMap.put("mpass", password);

            RootRef.child("Users").child(CurrentUser.getUid()).updateChildren(MirrorMap).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(RegisterMirrorActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    SendUserToMainActivity();
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(this, MainActivity.class);
        startActivity(MainIntent);
    }

}