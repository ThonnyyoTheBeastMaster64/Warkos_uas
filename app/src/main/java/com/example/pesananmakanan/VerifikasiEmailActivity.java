package com.example.pesananmakanan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

/**
 * Menggantikan OTPActivity untuk alur registrasi.
 * Verifikasi email sekarang ditangani Firebase (link verifikasi),
 * bukan kode OTP manual lagi.
 */
public class VerifikasiEmailActivity extends AppCompatActivity {

    private TextView tvInfoVerifikasi;
    private Button btnCekVerifikasi, btnKirimUlang;
    private ProgressBar progressVerifikasi;
    private String emailUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifikasi_email);

        tvInfoVerifikasi = findViewById(R.id.tvInfoVerifikasi);
        btnCekVerifikasi = findViewById(R.id.btnCekVerifikasi);
        btnKirimUlang = findViewById(R.id.btnKirimUlang);
        progressVerifikasi = findViewById(R.id.progressVerifikasi);

        emailUser = getIntent().getStringExtra("email");
        tvInfoVerifikasi.setText("Kami sudah kirim link verifikasi ke:\n" + emailUser +
                "\n\nBuka email kamu, klik link verifikasinya, lalu tekan tombol di bawah ini.");

        btnCekVerifikasi.setOnClickListener(v -> cekStatusVerifikasi());
        btnKirimUlang.setOnClickListener(v -> kirimUlangEmail());
    }

    private void cekStatusVerifikasi() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sesi habis, silakan login ulang", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        progressVerifikasi.setVisibility(View.VISIBLE);
        btnCekVerifikasi.setEnabled(false);

        // reload() ambil data terbaru dari server Firebase, biar isEmailVerified() akurat
        user.reload().addOnCompleteListener(task -> {
            btnCekVerifikasi.setEnabled(true);

            if (!task.isSuccessful()) {
                progressVerifikasi.setVisibility(View.GONE);
                Toast.makeText(this, "Gagal cek status, coba lagi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user.isEmailVerified()) {
                aktivasiAkunDiServer();
            } else {
                progressVerifikasi.setVisibility(View.GONE);
                Toast.makeText(this, "Email belum diverifikasi. Cek inbox/spam, klik link-nya dulu ya", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void aktivasiAkunDiServer() {
        JSONObject body = new JSONObject();
        try {
            body.put("email", emailUser);
        } catch (Exception e) {}

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, ApiConfig.VERIFY_FIREBASE_EMAIL_URL, body,
                res -> {
                    progressVerifikasi.setVisibility(View.GONE);
                    String status = res.optString("status", "error");
                    if (status.equals("success")) {
                        Toast.makeText(this, "Verifikasi berhasil, silakan login!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, res.optString("message", "Gagal mengaktifkan akun"), Toast.LENGTH_SHORT).show();
                    }
                },
                err -> {
                    progressVerifikasi.setVisibility(View.GONE);
                    String msg = "Gagal terhubung ke server";
                    if (err.networkResponse != null) {
                        msg += " (Error: " + err.networkResponse.statusCode + ")";
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void kirimUlangEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        progressVerifikasi.setVisibility(View.VISIBLE);
        user.sendEmailVerification().addOnCompleteListener(task -> {
            progressVerifikasi.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Email verifikasi dikirim ulang", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Gagal kirim ulang, coba beberapa saat lagi", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
