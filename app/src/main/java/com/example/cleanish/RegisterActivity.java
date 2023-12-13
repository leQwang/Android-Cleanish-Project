package com.example.cleanish;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cleanish.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText registerEmail, registerPassword;
    private Button toLoginButton, registerButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        toLoginButton = findViewById(R.id.toLoginButton);
        registerButton = findViewById(R.id.registerAccountButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = registerEmail.getText().toString().trim();
                String password = registerPassword.getText().toString().trim();

                if(email.isEmpty()){
                    registerEmail.setError("Email cannot be empty");
//                    Toast.makeText(RegisterActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                }else if (!email.contains("@")){
                    registerEmail.setError("Email invalid");
                }

                if(password.isEmpty()){
                    registerPassword.setError("Password cannot be empty");
//                    Toast.makeText(RegisterActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (password.toString().length() < 8) {
                    registerPassword.setError("Password must be at least 8 characters long");
//                    Toast.makeText(RegisterActivity.this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();

                } else {
//                    String email = "hello3@gmail.com";
//                    String password = "123456789";

                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful()) {

                                    FirebaseUser user = task.getResult().getUser();
                                    String uid = user.getUid();

                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    DocumentReference userRef = db.collection("Users").document(uid); // Reference using UID

                                    // Set user data
                                    userRef.set(new User(email, password, "Volunteer"))
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                                Toast.makeText(RegisterActivity.this, "Registered Successful!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w(TAG, "Error writing document", e);
                                                Toast.makeText(RegisterActivity.this, "Firestore registered failed!", Toast.LENGTH_SHORT).show();
                                            });

                                } else {

                                    Toast.makeText(RegisterActivity.this, "Firebase Authenticate registered failed!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        toLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }
}