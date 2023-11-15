package com.example.firebase;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.example.firebase.databinding.ActivityStorageBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class StorageActivity extends AppCompatActivity {

    FirebaseStorage db;
    StorageReference storageReference;
    ActivityStorageBinding binding;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStorageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        storageReference = db.getReference();
        listarArchivos();
        binding.btnUploadFile.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
        binding.btnDownloadSave.setOnClickListener(v -> {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                descargarYguardar();
            }else{
                String permission =android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
                requestReadPermissionLauncher.launch(permission);
            }
        });
        binding.btnDownloadShow.setOnClickListener(v -> {
           descargarYmostrarPeroNoGuardarGlide();
        });
        binding.floatingActionButton.setOnClickListener(v -> {
            finish();
        });
    }
    public void descargarYguardar(){
        if(firebaseUser!=null){
           String uid = firebaseUser.getUid();
            String selectedItem = (String) binding.spinner.getSelectedItem();
            File directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File localFile = new File(directorio, selectedItem);

            StorageReference docRef = storageReference.child(uid).child(selectedItem);

            docRef.getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d("msg", "archivo descargado");
                        Toast.makeText(StorageActivity.this,"Archivo descargado",Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Log.d("msg", "error", e.getCause()))
                    .addOnProgressListener(snapshot -> {
                        long bytesTransferred = snapshot.getBytesTransferred();
                        long totalByteCount = snapshot.getTotalByteCount();

                        binding.textViewDownload.setText(Math.round((bytesTransferred * 1.0f / totalByteCount) * 100) + "%");
                        //
                    });
        }else{
            Toast.makeText(StorageActivity.this, "I use arch btw", Toast.LENGTH_SHORT).show();
        }
    }
    public void descargarYmostrarPeroNoGuardarGlide(){
        if(firebaseUser != null){
            String uid = firebaseUser.getUid();
            String selectedItem = (String) binding.spinner.getSelectedItem();
            StorageReference docRef = storageReference.child(uid).child(selectedItem);
            Glide.with(StorageActivity.this).load(docRef).into(binding.imageView2);
        }else{
            Toast.makeText(StorageActivity.this, "I use arch btw", Toast.LENGTH_SHORT).show();
        }
    }
    public void listarArchivos(){
        if(firebaseUser != null){
            String uid = firebaseUser.getUid();
            StorageReference listRef = storageReference.child(uid);

            listRef.listAll()
                    .addOnSuccessListener(listResult -> {
                        String[] items = new String[listResult.getItems().size()];
                        int i = 0;

                        for (StorageReference item : listResult.getItems()) {
                            // All the items under listRef.
                            Log.d("msg-test","item.getName(): " + item.getName());
                            Log.d("msg-test","item.getPath(): " + item.getPath());
                            items[i++] = item.getName();
                        }


                        ArrayAdapter<String> adapter = new ArrayAdapter<>(StorageActivity.this, android.R.layout.simple_spinner_dropdown_item,items);
                        binding.spinner.setAdapter(adapter);

                    })
                    .addOnFailureListener(e -> e.printStackTrace());
        }else{
            Toast.makeText(StorageActivity.this, "I use arch btw", Toast.LENGTH_SHORT).show();
        }
    }
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    Log.d("msg-test", "Selected URI: " + uri);
                    Log.d("msg-test", "uri.getLastPathSegment(): " + uri.getLastPathSegment());
                    String mimeType = getContentResolver().getType(uri);
                    String[] parts = mimeType.split("/");
                    String part2 = parts[1];
                    Log.d("msg-test", "MimeType: " + mimeType);
                    if(firebaseUser != null){
                        String uid = firebaseUser.getUid();
                        StorageReference imagesRef = storageReference.child(uid).child(uri.getLastPathSegment() + "." + part2); //.child("images").child(uri.getLastPathSegment());

                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setCustomMetadata("distro", "arch")
                                .setCustomMetadata("based", "obvi")
                                .build();

                        UploadTask uploadTask = imagesRef.putFile(uri, metadata);

                        uploadTask.addOnFailureListener(exception -> {
                            exception.printStackTrace();
                        }).addOnSuccessListener(taskSnapshot -> {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            Toast.makeText(StorageActivity.this, "Archivo subido correctamente", Toast.LENGTH_SHORT).show();
                        }).addOnProgressListener(snapshot -> {
                            long bytesTransferred = snapshot.getBytesTransferred();
                            long totalByteCount = snapshot.getTotalByteCount();
                            double porcentajeSubida = Math.round((bytesTransferred * 1.0f / totalByteCount) * 100);
                            binding.textViewPorc.setText(porcentajeSubida + "%");
                        });
                    }else{
                        Toast.makeText(StorageActivity.this, "I use arch btw", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("msg-test", "No media selected");
                }
            });
    ActivityResultLauncher<String> requestReadPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    descargarYguardar();
                }
            }
    );
}