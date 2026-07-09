package com.example.pesananmakanan;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressLogin = findViewById(R.id.progressLogin);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Email dan password harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            loginKeServer(email, password);
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void loginKeServer(String email, String password) {
        progressLogin.setVisibility(View.VISIBLE);

        // 1. Cek email & password di Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 2. Status aktif/non-aktif (OTP) dan role dicek di database, bukan hardcode
                        cekRoleDiMySQL(email);
                    } else {
                        progressLogin.setVisibility(View.GONE);
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Email atau Password salah!";
                        Toast.makeText(this, "Login gagal: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void cekRoleDiMySQL(String email) {
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, ApiConfig.LOGIN_URL, body,
                res -> {
                    progressLogin.setVisibility(View.GONE);
                    try {
                        String status = res.getString("status");
                        if (status.equals("success")) {
                            String role = res.getString("role");

                            Intent intent;
                            if (role.equalsIgnoreCase("admin")) {
                                intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                            } else {
                                intent = new Intent(LoginActivity.this, KasirActivity.class);
                            }

                            startActivity(intent);
                            finish();
                        } else {
                            String message = res.optString("message", "Login gagal di database");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                            // Kalau belum aktif (belum verifikasi email), arahkan ke layar verifikasi
                            if (message.toLowerCase().contains("belum aktif")) {
                                Intent intent = new Intent(LoginActivity.this, VerifikasiEmailActivity.class);
                                intent.putExtra("email", email);
                                startActivity(intent);
                            } else {
                                mAuth.signOut();
                            }
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                err -> {
                    progressLogin.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal koneksi ke server database", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
}
