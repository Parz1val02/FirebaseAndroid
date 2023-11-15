package com.example.firebase;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.firebase.databinding.ActivityLoginBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){
            if(currentUser.isEmailVerified()){
                Log.d("msg-test", "Firebase uid: " + currentUser.getUid());
                goToMainActivity();
            }
        }
        binding.loginBtn.setOnClickListener(v -> {
            binding.loginBtn.setEnabled(false);
            Intent intent = AuthUI.getInstance()
                  .createSignInIntentBuilder()
                    .setTheme(R.style.Base_Theme_Firebase)
                  .setAvailableProviders(Arrays.asList(
                          new AuthUI.IdpConfig.EmailBuilder().build(),
                          new AuthUI.IdpConfig.GoogleBuilder().build()
                  ))
                    .setIsSmartLockEnabled(false)
                    .setLogo(R.drawable.arch_linux_logo)
                  .build();
            signInLauncher.launch(intent);
        });
    }
    ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if(user != null){
                        Log.d("msg-test", "Firebase uid: " + user.getUid());
                        Log.d("msg-test", "Firebase display name: " + user.getDisplayName());
                        Log.d("msg-test", "Firebase email: " + user.getEmail());
                        user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(user.isEmailVerified()){
                                    goToMainActivity();
                                }else{
                                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(LoginActivity.this, "Se le ha enviado un correo para validar su cuenta", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }else{
                        Log.d("msg-test", "Canceló el Log-in");
                    }
                } else {
                    Log.d("msg-test", "Canceló el Log-in");
                }
                binding.loginBtn.setEnabled(false);
            }
    );

    public void goToMainActivity(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}