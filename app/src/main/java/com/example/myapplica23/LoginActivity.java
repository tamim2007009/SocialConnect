package com.example.myapplica23;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplica23.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        setupProgressDialog();

        binding.loginBtn.setOnClickListener(v -> validateAndLogin());
        binding.gotoSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
    }

    /**
     * DEBUG FIX: onStart now reloads the user state from Firebase to get the latest
     * isEmailVerified status before deciding to proceed.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Reload user data from Firebase to get latest verification status
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser reloadedUser = auth.getCurrentUser();
                    if (reloadedUser != null && reloadedUser.isEmailVerified()) {
                        // User is logged in and verified, go to MainActivity
                        goToMainActivity();
                    }
                    // If not verified, do nothing and wait for user to log in manually
                }
            });
        }
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Logging In");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private void validateAndLogin() {
        String email = binding.emailET.getText().toString().trim();
        String password = binding.passwordET.getText().toString().trim();

        if (isValid(email, password)) {
            progressDialog.show();
            loginUser(email, password);
        }
    }

    /**
     * DEBUG FIX: The logic here is now streamlined. It checks verification
     * as the ONLY condition for entry after a successful password check.
     */
    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                // THIS IS THE CRITICAL CHECK
                if (user != null && user.isEmailVerified()) {
                    // User's email is verified, proceed to the main activity.
                    goToMainActivity();
                } else {
                    // User's email is NOT verified. Inform them and sign them out.
                    auth.signOut();
                    showVerificationAlert();
                }
            } else {
                handleLoginFailure(task.getException());
            }
        });
    }

    private void handleLoginFailure(Exception exception) {
        try {
            throw exception;
        } catch (FirebaseAuthInvalidUserException e) {
            binding.emailET.setError("No account found with this email.");
            binding.emailET.requestFocus();
        } catch (FirebaseAuthInvalidCredentialsException e) {
            binding.passwordET.setError("Incorrect password. Please try again.");
            binding.passwordET.requestFocus();
        } catch (Exception e) {
            Log.e(TAG, "Login Failed: ", e);
            Toast.makeText(this, "Login Failed. Please check your connection.", Toast.LENGTH_LONG).show();
        }
    }

    private void goToMainActivity() {
        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showVerificationAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Email Not Verified")
                .setMessage("Please verify your email address to log in. A verification link was sent to your inbox.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean isValid(String email, String password) {
        binding.emailET.setError(null);
        binding.passwordET.setError(null);
        if (email.isEmpty()) {
            binding.emailET.setError("Email is required.");
            binding.emailET.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            binding.passwordET.setError("Password is required.");
            binding.passwordET.requestFocus();
            return false;
        }
        return true;
    }
}