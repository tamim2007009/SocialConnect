package com.example.myapplica23;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplica23.Model.User;
import com.example.myapplica23.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;
    private static final String TAG = "SignUpActivity";
    // Define the required email domain
    private static final String ALLOWED_DOMAIN = "@stud.kuet.ac.bd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        setupProgressDialog();

        binding.signupBtn.setOnClickListener(v -> validateAndRegisterUser());
        binding.gotoLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("We're setting things up for you...");
        progressDialog.setCancelable(false);
    }

    private void validateAndRegisterUser() {
        String name = binding.nameET.getText().toString().trim();
        String profession = binding.professionET.getText().toString().trim();
        String email = binding.emailET.getText().toString().trim();
        String password = binding.passwordET.getText().toString().trim();

        if (isValid(name, profession, email, password)) {
            progressDialog.show();
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser != null) {
                        sendVerificationEmail(firebaseUser, name, profession, email);
                    }
                } else {
                    progressDialog.dismiss();
                    handleRegistrationFailure(task.getException());
                }
            });
        }
    }

    private void sendVerificationEmail(FirebaseUser firebaseUser, String name, String profession, String email) {
        firebaseUser.sendEmailVerification().addOnCompleteListener(verificationTask -> {
            if (verificationTask.isSuccessful()) {
                Log.d(TAG, "Verification email sent successfully.");
                User user = new User(name, profession, email, "");
                String id = firebaseUser.getUid();
                database.getReference().child("Users").child(id).setValue(user)
                        .addOnCompleteListener(dbTask -> {
                            progressDialog.dismiss();
                            auth.signOut();
                            showVerificationAlert(email);
                        });
            } else {
                progressDialog.dismiss();
                Log.e(TAG, "Failed to send verification email.", verificationTask.getException());
                Toast.makeText(SignUpActivity.this, "Failed to send verification email.", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * KEY CHANGE: This method now checks if the email ends with the allowed domain.
     */
    private boolean isValid(String name, String profession, String email, String password) {
        // Clear previous errors
        binding.nameET.setError(null);
        binding.professionET.setError(null);
        binding.emailET.setError(null);
        binding.passwordET.setError(null);

        if (name.isEmpty()) {
            binding.nameET.setError("Name is required.");
            binding.nameET.requestFocus();
            return false;
        }
        if (profession.isEmpty()) {
            binding.professionET.setError("Profession is required.");
            binding.professionET.requestFocus();
            return false;
        }
        if (email.isEmpty()) {
            binding.emailET.setError("Email is required.");
            binding.emailET.requestFocus();
            return false;
        }
        // General email format check
        if (!Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$").matcher(email).matches()) {
            binding.emailET.setError("Please enter a valid email address.");
            binding.emailET.requestFocus();
            return false;
        }
        // ✨ KUET domain restriction check
        if (!email.toLowerCase().endsWith(ALLOWED_DOMAIN)) {
            binding.emailET.setError("Only KUET student emails (" + ALLOWED_DOMAIN + ") are allowed.");
            binding.emailET.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            binding.passwordET.setError("Password is required.");
            binding.passwordET.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            binding.passwordET.setError("Password must be at least 6 characters long.");
            binding.passwordET.requestFocus();
            return false;
        }

        return true;
    }

    private void handleRegistrationFailure(Exception exception) {
        try {
            throw exception;
        } catch (FirebaseAuthWeakPasswordException e) {
            binding.passwordET.setError("Password is too weak. Please use at least 6 characters.");
            binding.passwordET.requestFocus();
        } catch (FirebaseAuthUserCollisionException e) {
            binding.emailET.setError("This email is already registered.");
            binding.emailET.requestFocus();
        } catch (Exception e) {
            Log.e(TAG, "Registration Failed: ", e);
            Toast.makeText(this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showVerificationAlert(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Registration Almost Complete")
                .setMessage("A verification link has been sent to " + email + ".\n\nPlease check your email inbox (and spam folder!) to complete your registration.")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}