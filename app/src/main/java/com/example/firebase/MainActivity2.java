package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.firebase.databinding.ActivityMain2Binding;
import com.example.firebase.dtos.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity2 extends AppCompatActivity {

    FirebaseFirestore db;
    ActivityMain2Binding binding;
    ListenerRegistration snapshotListener;
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        binding.button.setOnClickListener(v -> {
            binding.button.setEnabled(false);
            String nombre = binding.textFieldNombre.getEditText().getText().toString();
            String apellido = binding.textFieldApellido.getEditText().getText().toString();
            String edadStr = binding.textFieldEdad.getEditText().getText().toString();
            String dniStr = binding.textFieldDni.getEditText().getText().toString();
            if(edadStr.equals("") && dniStr.equals("")){
                Toast.makeText(MainActivity2.this, "I use arch btw", Toast.LENGTH_SHORT).show();
            }else{
                int edad = Integer.parseInt(edadStr);
                Usuario usuario = new Usuario(nombre, apellido, edad);
                if(firebaseUser!=null){
                    String uid = firebaseUser.getUid();
                    db.collection("usuarios_por_auth").document(uid).collection("mis_usuarios").add(usuario).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(MainActivity2.this, "Usuario guardado en firebase :D", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity2.this, "Usuario no guardado en firebase :c", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Toast.makeText(MainActivity2.this, "I use arch btw", Toast.LENGTH_SHORT).show();
                }
            }
            binding.textFieldDni.getEditText().setText("");
            binding.textFieldEdad.getEditText().setText("");
            binding.textFieldApellido.getEditText().setText("");
            binding.textFieldNombre.getEditText().setText("");
            binding.button.setEnabled(true);
        });

        binding.btnTiempoReal.setOnClickListener(v -> {
            if(firebaseUser!=null){
                String uid = firebaseUser.getUid();
                snapshotListener = db.collection("usuarios_por_auth").document(uid).collection("mis_usuarios").
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
            }else{
                Toast.makeText(MainActivity2.this, "I use arch btw", Toast.LENGTH_SHORT).show();
            }
        });

        binding.floatingActionButton.setOnClickListener(v -> {
            finish();
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (snapshotListener != null)
            snapshotListener.remove();
    }
}