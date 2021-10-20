package com.asmirror.android.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.asmirror.android.MainActivity;
import com.asmirror.android.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

public class ProfileImageActivity extends AppCompatActivity {
    private ImageView ProfileImageView;
    private Uri ProfileImageUri;

    private FirebaseUser CurrentUser;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image);

        CurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        ProfileImageView = findViewById(R.id.profile_image_view);
        Button ProfileImageSelectButton = findViewById(R.id.profile_image_select_button);
        Button ProfileImageUpdateButton = findViewById(R.id.profile_image_update_button);

        ProfileImageSelectButton.setOnClickListener(v -> CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(this));
        ProfileImageUpdateButton.setOnClickListener(v -> UploadImageToStorage());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    ProfileImageUri = result.getUri();
                    ProfileImageView.setImageURI(ProfileImageUri);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = Objects.requireNonNull(result).getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void UploadImageToStorage() {
        if (ProfileImageUri == null) {
            Toast.makeText(this, "Select image first", Toast.LENGTH_SHORT).show();
        } else {
            StorageReference StorageRef = FirebaseStorage.getInstance().getReference("ProfileImages/" + CurrentUser.getUid() + ".jpg");
            StorageRef.putFile(ProfileImageUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
                return StorageRef.getDownloadUrl();
            }).addOnCompleteListener(downloadTask -> {
                if (downloadTask.isSuccessful()) {
                    Uri DownloadImageUri = downloadTask.getResult();
                    if (DownloadImageUri != null) {
                        String DownloadImageUrl = DownloadImageUri.toString();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(DownloadImageUri).build();
                        CurrentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Toast.makeText(ProfileImageActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                RootRef.child("Users").child(CurrentUser.getUid()).child("profile_image").setValue(DownloadImageUrl).addOnCompleteListener(task1 -> {
                                    if (!task1.isSuccessful()) {
                                        Toast.makeText(ProfileImageActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        SendUserToMainActivity();
                                    }
                                });
                            }
                        });
                    }
                } else {
                    Toast.makeText(ProfileImageActivity.this, Objects.requireNonNull(downloadTask.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(this, MainActivity.class);
        startActivity(MainIntent);
    }

}