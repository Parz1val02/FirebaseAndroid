package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.firebase.databinding.ActivityGatoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class GatoActivity extends AppCompatActivity {

    ActivityGatoBinding binding;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGatoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        binding.button.setOnClickListener(v -> {
            binding.button.setEnabled(false);
            String nombre = binding.textFieldNombre.getEditText().getText().toString();
            String raza = binding.textFieldRaza.getEditText().getText().toString();
            HashMap<String, Object> gatoMap = new HashMap<>();
            gatoMap.put("nombre", nombre);
            gatoMap.put("raza", raza);
            gatoMap.put("timestamp", FieldValue.serverTimestamp());
            db.collection("gatos").add(gatoMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(GatoActivity.this, "Gato guardado en firebase :D", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GatoActivity.this, "Gato no guardado en firebase :c", Toast.LENGTH_SHORT).show();
                }
            });
            binding.button.setEnabled(true);
        });
        binding.btnListarGatos.setOnClickListener(v -> {
            binding.btnListarGatos.setEnabled(false);
            db.collection("gatos").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                QuerySnapshot gatosCollection = task.getResult();
                                for (QueryDocumentSnapshot document : gatosCollection) {
                                    String nombre = (String) document.get("nombre");
                                    String raza = document.getString("raza");
                                    String documentId = document.getId();
                                    if (document.contains("timestamp")) {
                                        Date date = document.getDate("timestamp");
                                        Log.d("msg-test", "documentId: " + documentId + " | nombre: " + nombre + " | raza: " + raza + " | fecha_server: " + date.toString());
                                    } else {
                                        Log.d("msg-test", "documentId: " + documentId + " | nombre: " + nombre + " | raza: " + raza);
                                    }

                                }
                            }
                        }
                    });
            binding.btnListarGatos.setEnabled(true);
        });
    }
}