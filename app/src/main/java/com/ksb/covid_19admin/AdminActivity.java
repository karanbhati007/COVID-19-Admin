package com.ksb.covid_19admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ksb.covid_19admin.model.AdminMessage;

import dmax.dialog.SpotsDialog;

public class AdminActivity extends AppCompatActivity {
    EditText etDesc, etTitle;
    ImageView mImage;
    Button btPost, logout;
    boolean per = true;
  //  ImageCompression imageCompression;
    private DatabaseReference mPostDatabase;
    Bitmap myBitmap;
    String desc, title;
    View contextView;
    Uri downloadurl;
    private StorageReference mStorageRef;
    // String downloadUrl=null;
    String newPath = null;
    AlertDialog alertDialog;
    // private ProgressDialog mProgress;
    private Uri mImageUri;
    private static final int GALLERY_CODE = 1;


    final String TAG = "DOWNLOAD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        alertDialog = new SpotsDialog.Builder().setContext(this).build();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        etDesc = findViewById(R.id.description);
        etTitle = findViewById(R.id.et_title);
        mImage = findViewById(R.id.chooseImgAndSelected);
        btPost = findViewById(R.id.bt_post);
        contextView = findViewById(android.R.id.content);
        logout = findViewById(R.id.logout);
       // imageCompression = new ImageCompression(this);
        // mProgress = new ProgressDialog(this);

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.ksb.covid_19admin",MODE_PRIVATE);
        String postalCode = sharedPreferences.getString("postalCode",null);
        if(postalCode!=null)
            mPostDatabase = FirebaseDatabase.getInstance().getReference().child("Admin_Messages").child(postalCode);
        else
            mPostDatabase = FirebaseDatabase.getInstance().getReference().child("Admin_Messages").child("00000");

        Toast.makeText(this, "Postal Code :: "+postalCode, Toast.LENGTH_SHORT).show();

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Choose Image
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
            }
        });

        btPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mAuth.signOut();
                startActivity(new Intent(AdminActivity.this, MainActivity.class));
                finish();
            }
        });


    }


    private void startPosting() {

        alertDialog.setMessage("Posting ...");
        alertDialog.show();

        final String titleVal = etTitle.getText().toString().trim();
        final String descVal = etDesc.getText().toString().trim();

        if (!TextUtils.isEmpty(titleVal) && !TextUtils.isEmpty(descVal) && mImageUri != null) {

            final StorageReference filepath = mStorageRef.child("MAdminUsers_images").
                    child(String.valueOf(System.currentTimeMillis()));

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadurl = uri;
                            AdminMessage adminMessage = new AdminMessage(titleVal, descVal, downloadurl.toString(), MainActivity.currentUser.getEmail());
                            mPostDatabase.child(String.valueOf(System.currentTimeMillis())).setValue(adminMessage);
                        }
                    });


                    alertDialog.dismiss();
                    Snackbar.make(contextView, "Post Successful", Snackbar.LENGTH_SHORT).show();
                    mImage.setImageBitmap(null);
                    etTitle.setText(null);
                    etDesc.setText(null);
                    mImage.setBackgroundResource(R.drawable.add_btn);

                }

            });

        } else {
            alertDialog.dismiss();
            Snackbar.make(contextView, "Please fill all fields including Image", Snackbar.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            mImage.setBackgroundResource(android.R.color.white);
            mImage.setImageURI(mImageUri);  // mPostImage
        } else {
            Toast.makeText(this, "Image Not Provided", Toast.LENGTH_SHORT).show();
        }
    }
}
