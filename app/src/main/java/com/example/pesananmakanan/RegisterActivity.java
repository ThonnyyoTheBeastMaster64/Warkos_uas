package com.example.pesananmakanan;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPass, etSecretCode;
    private Button btnDoRegister;
    private ProgressBar progressReg;
    private Spinner spinnerRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etRegEmail);
        etPass = findViewById(R.id.etRegPass);
        etSecretCode = findViewById(R.id.etSecretCode);
        btnDoRegister = findViewById(R.id.btnDoRegister);
        progressReg = findViewById(R.id.progressReg);
        spinnerRole = findViewById(R.id.spinnerRole);

        String[] roles = {"kasir", "admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        etSecretCode.setVisibility(View.VISIBLE);

        btnDoRegister.setOnClickListener(v -> prosesDaftar());
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());
    }

    private void prosesDaftar() {
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();
        String secret = etSecretCode.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Isi semua field bro!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!role.equals("pengguna_kos") && TextUtils.isEmpty(secret)) {
            Toast.makeText(this, "Admin/Kasir butuh kode otorisasi!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressReg.setVisibility(View.VISIBLE);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // 1. Daftar akun email/password ke Firebase (autentikasi password ditangani Firebase)
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 2. Simpan role ke MySQL (status non-aktif dulu, menunggu OTP)
                        simpanUserKeMySQL(email, role, secret, pass);
                    } else {
                        progressReg.setVisibility(View.GONE);
                        String msg = task.getException() != null ? task.getException().getMessage() : "Registrasi gagal";
                        Toast.makeText(this, "Gagal Firebase: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void simpanUserKeMySQL(String email, String role, String secret, String pass) {
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("role", role);
            body.put("secret_code", secret);
        } catch (Exception e) {}

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, ApiConfig.REGISTER_FIREBASE_URL, body,
                res -> {
                    String status = res.optString("status", "error");
                    if (status.equals("success")) {
                        // 3. Kirim Link Verifikasi Firebase (Email)
                        kirimEmailVerifikasi(email);
                    } else {
                        progressReg.setVisibility(View.GONE);
                        batalkanFirebaseUser();
                        Toast.makeText(this, res.optString("message", "Gagal simpan ke DB"), Toast.LENGTH_LONG).show();
                    }
                },
                err -> {
                    progressReg.setVisibility(View.GONE);
                    batalkanFirebaseUser();

                    String errorMsg = "Gagal simpan ke DB";
                    if (err.networkResponse != null && err.networkResponse.data != null) {
                        try {
                            JSONObject errorObj = new JSONObject(new String(err.networkResponse.data));
                            errorMsg = errorObj.optString("message", errorMsg);
                        } catch (Exception e) {}
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                });
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void kirimEmailVerifikasi(String email) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        progressReg.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Link verifikasi dikirim ke email", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, VerifikasiEmailActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Gagal kirim email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void batalkanFirebaseUser() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().getCurrentUser().delete();
        }
    }
}
