package com.example.pesananmakanan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PenggunaActivity extends AppCompatActivity {

    private RecyclerView rvMenu;
    private TextView tvTotalHarga, tvTotalItem, tvWelcome, tvHargaKamar, tvTotalHutang, tvNoKamar;
    private EditText etCari;
    private List<Produk> daftarSemua = new ArrayList<>();
    private List<Produk> daftarTampil = new ArrayList<>();
    private KasirActivity.KasirAdapter adapter;
    private double totalBelanja = 0;
    private String emailUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengguna);

        emailUser = getIntent().getStringExtra("email");

        rvMenu = findViewById(R.id.rvMenuPengguna);
        tvTotalHarga = findViewById(R.id.tvTotalHargaPengguna);
        tvTotalItem = findViewById(R.id.tvTotalItem);
        tvWelcome = findViewById(R.id.tvWelcomeName);
        tvHargaKamar = findViewById(R.id.tvHargaKamar);
        tvTotalHutang = findViewById(R.id.tvTotalHutang);
        tvNoKamar = findViewById(R.id.tvNoKamar);
        etCari = findViewById(R.id.etCariMenu);

        if (emailUser != null) tvWelcome.setText(emailUser);

        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        adapter = new KasirActivity.KasirAdapter(daftarTampil, this::hitungTotal);
        rvMenu.setAdapter(adapter);

        etCari.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnLogoutPengguna).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.btnPesanSekarang).setOnClickListener(v -> prosesPesan());

        ambilData();
        ambilInfoKos();
    }

    private void filter(String query) {
        daftarTampil.clear();
        for (Produk p : daftarSemua) {
            if (p.getNamaProduk().toLowerCase().contains(query.toLowerCase())) {
                daftarTampil.add(p);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void hitungTotal() {
        totalBelanja = 0;
        int itemCounter = 0;
        for (Produk p : daftarSemua) {
            if (p.getQty() > 0) {
                totalBelanja += p.getHarga() * p.getQty();
                itemCounter += p.getQty();
            }
        }
        tvTotalHarga.setText("Rp " + formatRupiah(totalBelanja));
        tvTotalItem.setText(itemCounter + " Item terpilih");
    }

    private String formatRupiah(double nominal) {
        return NumberFormat.getInstance(new Locale("in", "ID")).format(nominal);
    }

    private void ambilInfoKos() {
        if (TextUtils.isEmpty(emailUser)) return;

        JSONObject body = new JSONObject();
        try { body.put("email", emailUser); } catch (Exception e) {}

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiConfig.INFO_PENGGUNA_URL, body,
                res -> {
                    try {
                        if (res.getString("status").equals("success")) {
                            tvNoKamar.setText(res.getString("no_kamar"));
                            tvHargaKamar.setText("Rp " + formatRupiah(res.getDouble("harga_kamar")));
                            tvTotalHutang.setText("Rp " + formatRupiah(res.getDouble("total_hutang")));
                        }
                    } catch (Exception e) {}
                }, null);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void prosesPesan() {
        if (totalBelanja <= 0) {
            Toast.makeText(this, "Pilih menu dulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Pesanan")
                .setMessage("Total: Rp " + formatRupiah(totalBelanja) + "\nLanjutkan pemesanan?")
                .setPositiveButton("Ya, Pesan", (d, w) -> {
                    Toast.makeText(this, "Pesanan dikirim! Silakan ke kasir untuk bayar.", Toast.LENGTH_LONG).show();
                    reset();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void reset() {
        for (Produk p : daftarSemua) p.setQty(0);
        hitungTotal();
        adapter.notifyDataSetChanged();
    }

    private void ambilData() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ApiConfig.PRODUK_URL, null,
                res -> {
                    try {
                        daftarSemua.clear();
                        JSONArray data = res.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject o = data.getJSONObject(i);
                            daftarSemua.add(new Produk(o.getInt("id"), o.getString("nama_produk"),
                                    o.optString("kategori", "Lainnya"), o.getDouble("harga"), o.getInt("stok"),
                                    o.isNull("gambar_url") ? null : o.getString("gambar_url")));
                        }
                        filter("");
                    } catch (Exception e) {}
                }, null);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}