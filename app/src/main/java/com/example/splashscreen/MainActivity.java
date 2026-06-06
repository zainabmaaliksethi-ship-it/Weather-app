package com.example.splashscreen;

import android.content.Intent;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText usernameField, emailField, passwordField, reenterPasswordField;
    private Button signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Match IDs exactly from your XML
        usernameField = findViewById(R.id.username);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        reenterPasswordField = findViewById(R.id.reenter_password);
        signUpBtn = findViewById(R.id.button); // your XML uses "button", not "signupBtn"

        signUpBtn.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String reenterPassword = reenterPasswordField.getText().toString().trim();

            // Validate all fields
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || reenterPassword.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check passwords match
            if (!password.equals(reenterPassword)) {
                Toast.makeText(MainActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase sign up
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Account created!", Toast.LENGTH_SHORT).show();
                            // Replace HomeActivity.class with whatever your home screen is
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Sign up failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Navigate to Login page
        TextView goToLogin = findViewById(R.id.goToLogin);
        goToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
        });
    }
}