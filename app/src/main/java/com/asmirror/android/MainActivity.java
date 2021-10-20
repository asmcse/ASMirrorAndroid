package com.asmirror.android;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.asmirror.android.auth.LoginActivity;
import com.asmirror.android.auth.ProfileImageActivity;
import com.asmirror.android.auth.TrainImageActivity;
import com.asmirror.android.mirror.RegisterMirrorActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private CircleImageView UserImageView;
    private TextView UserNameView, MirrorIdView;

    private FirebaseAuth mAuth;
    private FirebaseUser CurrentUser;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));

        mAuth = FirebaseAuth.getInstance();
        CurrentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        UserImageView = findViewById(R.id.main_user_image);
        UserNameView = findViewById(R.id.main_user_name);
        MirrorIdView = findViewById(R.id.main_user_mirror_id);
        CardView MirrorAddView = findViewById(R.id.main_card_mirror_add);
        CardView MirrorTurnOffView = findViewById(R.id.main_card_mirror_turn_off);
        CardView MirrorSettingsView = findViewById(R.id.main_card_mirror_setting);

        ValidateUser();

        MirrorAddView.setOnClickListener(v -> SendUserToAddMirrorActivity());
        MirrorSettingsView.setOnClickListener(v -> SendUserToProfileImageActivity());
        //MirrorTurnOffView.setOnClickListener(v -> TurnOffMirror());

        MirrorTurnOffView.setOnClickListener(v -> new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... integers) {
                try {
                    TurnOffMirror();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(1));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int MenuID = item.getItemId();
        if (MenuID == R.id.main_menu_notification) {
            //SendUserToNotificationActivity();
            return true;
        } if (MenuID == R.id.main_menu_setting) {
            //SendUserToSettingActivity();
            return true;
        } if (MenuID == R.id.main_menu_support) {
            //SendUserToSupportActivity();
            return true;
        } if (MenuID == R.id.main_menu_logout) {
            UserSignOut();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void ValidateUser() {
        if(CurrentUser == null) {
            SendUserToLoginActivity();
        } else if (CurrentUser.getPhotoUrl() == null){
            SendUserToProfileImageActivity();
        } else {
            RootRef.child("Users").child(CurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.child("profile_image").exists()) {
                        RootRef.child("Users").child(CurrentUser.getUid()).child("profile_image").setValue(CurrentUser.getPhotoUrl());
                    }
                    if (!snapshot.child("train_image").exists()) {
                        //SendUserToTrainImageActivity();
                    } else {
                        Picasso.get().load(CurrentUser.getPhotoUrl()).placeholder(R.drawable.user).into(UserImageView);
                        UserNameView.setText(CurrentUser.getDisplayName());

                        if (snapshot.child("mid").exists()) {
                            MirrorIdView.setText(Objects.requireNonNull(snapshot.child("mid").getValue()).toString());
                        } else {
                            MirrorIdView.setText("Connect a mirror first");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void TurnOffMirror() {
        RootRef.child("Users").child(CurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int port = 22;
                String command = "sudo reboot", mip = "", mpass = "", user = "pi";
                if (snapshot.child("mip").exists()) {
                    mip = Objects.requireNonNull(snapshot.child("mip").getValue()).toString();
                }
                if (snapshot.child("mpass").exists()) {
                    mpass = snapshot.child("mpass").getValue().toString();
                }

                try {
                    JSch jsch = new JSch();
                    Session session = jsch.getSession(user, mip, port);
                    session.setPassword(mpass);
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.setTimeout(60000);
                    session.connect();
                    ChannelExec channel = (ChannelExec) session.openChannel("exec");
                    channel.setCommand(command);
                    channel.connect();
                    Thread.sleep(1000);
                    channel.disconnect();
                }
                catch(JSchException | InterruptedException e){
                    //.....
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void UserSignOut() {
        mAuth.signOut();
        SendUserToLoginActivity();
    }

    private void SendUserToAddMirrorActivity() {
        Intent MirrorAddIntent = new Intent(this, RegisterMirrorActivity.class);
        startActivity(MirrorAddIntent);
    }

    private void SendUserToProfileImageActivity() {
        Intent ProfileImageIntent = new Intent(this, ProfileImageActivity.class);
        ProfileImageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(ProfileImageIntent);
    }

    private void SendUserToTrainImageActivity() {
        Intent TrainImageIntent = new Intent(this, TrainImageActivity.class);
        TrainImageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(TrainImageIntent);
    }

    private void SendUserToLoginActivity() {
        Intent LoginIntent = new Intent(this, LoginActivity.class);
        LoginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
    }

}