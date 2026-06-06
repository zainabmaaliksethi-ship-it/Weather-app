package com.example.splashscreen;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity2 extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Match IDs exactly from your XML
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginBtn = findViewById(R.id.button);

        loginBtn.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity2.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity2.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity2.this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity2.this,
                                    "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Navigate to Sign Up page
        TextView goToSignUp = findViewById(R.id.goToSignUp);
        goToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
            startActivity(intent);
        });
    }
}