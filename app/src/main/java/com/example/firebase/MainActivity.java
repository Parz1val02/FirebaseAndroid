package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.firebase.databinding.ActivityMainBinding;
import com.example.firebase.dtos.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db;
    ActivityMainBinding binding;
    ListenerRegistration snapshotListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        binding.button.setOnClickListener(v -> {
            binding.button.setEnabled(false);
            String nombre = binding.textFieldNombre.getEditText().getText().toString();
            String apellido = binding.textFieldApellido.getEditText().getText().toString();
            String edadStr = binding.textFieldEdad.getEditText().getText().toString();
            String dniStr = binding.textFieldDni.getEditText().getText().toString();
            if(edadStr.equals("")){
                Toast.makeText(MainActivity.this, "I use arch btw", Toast.LENGTH_SHORT).show();
            }else{
                int edad = Integer.parseInt(edadStr);
                Usuario usuario = new Usuario(nombre, apellido, edad);
                db.collection("usuarios").document(dniStr).set(usuario).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainActivity.this, "Usuario guardado en firebase :D", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Usuario no guardado en firebase :c", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            binding.button.setEnabled(true);
        });
        binding.btnListarUsuarios.setOnClickListener(v -> {
            binding.btnListarUsuarios.setEnabled(false);
            String dniStr = binding.textFieldDni.getEditText().getText().toString();
            db.collection("usuarios").document(dniStr).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot snapshot = task.getResult();
                                if(snapshot.exists()){
                                    Log.d("msg-test", "Document snapshot data: " + snapshot.getData());
                                    Usuario usuario = snapshot.toObject(Usuario.class);
                                    if(usuario!=null){
                                        Toast.makeText(MainActivity.this, "Usuario existe en firebase: " + usuario.getNombre() + " " + usuario.getApellido(), Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    Toast.makeText(MainActivity.this, "Usuario no existe en firebase :c", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
            binding.btnListarUsuarios.setEnabled(true);
        });
        binding.btnTiempoReal.setOnClickListener(v -> {
            snapshotListener = db.collection("usuarios").
                    addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.w("msg-test", "Listen failed.", error);
                                return;
                            }
                            Log.d("msg-test", "---- Datos en tiempo real ----");
                            for(QueryDocumentSnapshot doc: value){
                                Usuario usuario = doc.toObject(Usuario.class);
                                Log.d("msg-test", "Nombre: " + usuario.getNombre());
                                Log.d("msg-test", "Apellido: " + usuario.getApellido());
                            }
                        }
                    });
        });
        binding.fabNextActivity.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GatoActivity.class);
            startActivity(intent);
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (snapshotListener != null)
            snapshotListener.remove();
    }
}