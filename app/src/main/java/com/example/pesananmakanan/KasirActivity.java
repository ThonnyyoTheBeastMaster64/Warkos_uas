package com.example.pesananmakanan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KasirActivity extends AppCompatActivity {

    private RecyclerView rvKasir;
    private TextView tvTotalBelanja, tvTanggalSkrg;
    private EditText etUangBayar, etCariProduk;
    private Spinner spFilterKategori, spModeBayar;

    private List<Produk> daftarProdukSemua = new ArrayList<>();
    private List<Produk> daftarProdukTampil = new ArrayList<>();
    private List<String> daftarKategori = new ArrayList<>();

    private KasirAdapter adapter;
    private double totalBelanja = 0;
    private String kategoriFilter = "";
    private String[] modes = {"Cash"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kasir);

        // Binding UI
        rvKasir = findViewById(R.id.rvKasir);
        tvTotalBelanja = findViewById(R.id.tvTotalBelanja);
        tvTanggalSkrg = findViewById(R.id.tvTanggalSkrg);
        etUangBayar = findViewById(R.id.etUangBayar);
        etCariProduk = findViewById(R.id.etCariProduk);
        spFilterKategori = findViewById(R.id.spFilterKategori);
        spModeBayar = findViewById(R.id.spModeBayar); // Pastikan ada di XML

        tvTanggalSkrg.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("in", "ID")).format(new Date()));

        // Setup Mode Bayar Spinner
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modes);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spModeBayar.setAdapter(modeAdapter);

        // Format Ribuan Input
        etUangBayar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                etUangBayar.removeTextChangedListener(this);
                try {
                    String cleanString = s.toString().replaceAll("[.]", "");
                    if (!cleanString.isEmpty()) {
                        String formatted = NumberFormat.getInstance(new Locale("in", "ID")).format(Double.parseDouble(cleanString));
                        etUangBayar.setText(formatted);
                        etUangBayar.setSelection(formatted.length());
                    }
                } catch (Exception e) {}
                etUangBayar.addTextChangedListener(this);
            }
        });

        // Search Real-time
        etCariProduk.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterData(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Event Buttons
        findViewById(R.id.btnUangPas).setOnClickListener(v -> etUangBayar.setText(String.valueOf((int)totalBelanja)));
        findViewById(R.id.btnCash10k).setOnClickListener(v -> etUangBayar.setText("10000"));
        findViewById(R.id.btnCash20k).setOnClickListener(v -> etUangBayar.setText("20000"));
        findViewById(R.id.btnCash50k).setOnClickListener(v -> etUangBayar.setText("50000"));
        findViewById(R.id.btnCash100k).setOnClickListener(v -> etUangBayar.setText("100000"));
        findViewById(R.id.btnProsesBayar).setOnClickListener(v -> validasiDanProses());
        findViewById(R.id.btnLogoutKasir).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        rvKasir.setLayoutManager(new LinearLayoutManager(this));
        adapter = new KasirAdapter(daftarProdukTampil, this::hitungTotal);
        rvKasir.setAdapter(adapter);

        ambilDataServer();
    }

    private void filterData() {
        String query = etCariProduk.getText().toString().toLowerCase();
        daftarProdukTampil.clear();
        for (Produk p : daftarProdukSemua) {
            boolean mSearch = p.getNamaProduk().toLowerCase().contains(query);
            boolean mKat = kategoriFilter.isEmpty() || p.getKategori().equals(kategoriFilter);
            if (mSearch && mKat) daftarProdukTampil.add(p);
        }
        adapter.notifyDataSetChanged();
    }

    private void hitungTotal() {
        totalBelanja = 0;
        for (Produk p : daftarProdukSemua) totalBelanja += p.getHarga() * p.getQty();
        tvTotalBelanja.setText("Rp " + NumberFormat.getInstance(new Locale("in", "ID")).format(totalBelanja));
    }

    private void validasiDanProses() {
        String mode = spModeBayar.getSelectedItem().toString().toLowerCase();
        String uangStr = etUangBayar.getText().toString().replaceAll("[.]", "");

        if (totalBelanja <= 0) { Toast.makeText(this, "Pilih produk!", Toast.LENGTH_SHORT).show(); return; }

        if (mode.contains("cash")) {
            if (TextUtils.isEmpty(uangStr)) { etUangBayar.setError("Isi uang!"); return; }
            double bayar = Double.parseDouble(uangStr);
            if (bayar < totalBelanja) { Toast.makeText(this, "Uang kurang!", Toast.LENGTH_SHORT).show(); return; }
            prosesKirim(bayar, "cash", "");
        } else {
            // Mode Saldo atau Utang perlu Kode Anak Kos
            inputKodeUser(mode.contains("saldo") ? "saldo" : "utang");
        }
    }

    private void inputKodeUser(String tipe) {
        // Ambil list pengguna kos dari server
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, ApiConfig.AMBIL_PENGGUNA_URL, null,
                res -> {
                    try {
                        JSONArray data = res.getJSONArray("data");
                        String[] users = new String[data.length()];
                        for (int i = 0; i < data.length(); i++) {
                            users[i] = data.getJSONObject(i).getString("email") + " (" + data.getJSONObject(i).optString("no_kamar", "-") + ")";
                        }
                        
                        new AlertDialog.Builder(this).setTitle("Pilih Penghuni Kos")
                                .setItems(users, (dialog, which) -> {
                                    try {
                                        String selectedEmail = data.getJSONObject(which).getString("email");
                                        prosesKirim(tipe.equals("saldo") ? totalBelanja : 0, tipe, selectedEmail);
                                    } catch (Exception e) {}
                                }).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Gagal ambil data user", Toast.LENGTH_SHORT).show();
                    }
                }, err -> Toast.makeText(this, "Error Jaringan", Toast.LENGTH_SHORT).show());
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void prosesKirim(double bayar, String tipe, String userCode) {
        JSONObject body = new JSONObject();
        try {
            JSONArray items = new JSONArray();
            for (Produk p : daftarProdukSemua) {
                if (p.getQty() > 0) {
                    JSONObject item = new JSONObject();
                    item.put("produk_id", p.getId());
                    item.put("qty", p.getQty());
                    items.put(item);
                }
            }
            body.put("items", items);
            body.put("uang_dibayar", bayar);
            body.put("tipe_bayar", tipe);
            body.put("user_id", userCode); // PHP akan mencari ID berdasarkan ini
        } catch (Exception e) {}

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, ApiConfig.TRANSAKSI_URL, body,
                res -> {
                    try {
                        if (res.getString("status").equals("success")) {
                            tampilkanStruk(bayar, res.optDouble("kembalian", 0), tipe, userCode);
                            resetKasir();
                        } else { Toast.makeText(this, res.getString("message"), Toast.LENGTH_LONG).show(); }
                    } catch (Exception e) {}
                }, err -> Toast.makeText(this, "Error Jaringan", Toast.LENGTH_SHORT).show());
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void resetKasir() {
        etUangBayar.setText("");
        for(Produk p : daftarProdukSemua) p.setQty(0);
        ambilDataServer();
    }

    private void tampilkanStruk(double b, double k, String t, String u) {
        String msg = "Tipe: " + t.toUpperCase() + (u.isEmpty() ? "" : "\nUser: "+u) +
                "\nTotal: Rp " + NumberFormat.getInstance(new Locale("in", "ID")).format(totalBelanja) +
                "\nBayar: Rp " + NumberFormat.getInstance(new Locale("in", "ID")).format(b) +
                "\nKembali: Rp " + NumberFormat.getInstance(new Locale("in", "ID")).format(k);
        new AlertDialog.Builder(this).setTitle("WARKOSEAT - BERHASIL").setMessage(msg)
                .setPositiveButton("Selesai", null).show();
    }

    private void ambilDataServer() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ApiConfig.PRODUK_URL, null,
                res -> {
                    try {
                        daftarProdukSemua.clear();
                        JSONArray data = res.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject o = data.getJSONObject(i);
                            daftarProdukSemua.add(new Produk(o.getInt("id"), o.getString("nama_produk"),
                                    o.optString("kategori", "Lainnya"), o.getDouble("harga"), o.getInt("stok"),
                                    o.isNull("gambar_url") ? null : o.getString("gambar_url")));
                        }
                        filterData();
                    } catch (Exception e) {}
                }, null);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    // --- INNER ADAPTER ---
    public static class KasirAdapter extends RecyclerView.Adapter<KasirAdapter.VH> {
        private List<Produk> l; private OnQtyChangeListener n;
        public KasirAdapter(List<Produk> l, OnQtyChangeListener n) { this.l = l; this.n = n; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_produk_kasir, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int p) {
            Produk i = l.get(p);
            h.tvN.setText(i.getNamaProduk());
            h.tvH.setText("Rp " + NumberFormat.getInstance(new Locale("in", "ID")).format(i.getHarga()));
            h.tvQ.setText(String.valueOf(i.getQty()));

            if (i.getGambarUrl() != null && !i.getGambarUrl().isEmpty()) {
                h.iv.setImageUrl(i.getGambarUrl(), VolleySingleton.getInstance(h.itemView.getContext()).getImageLoader());
            } else {
                h.iv.setDefaultImageResId(android.R.drawable.ic_menu_gallery);
            }

            h.btnT.setOnClickListener(v -> { if(i.getQty() < i.getStok()){ i.setQty(i.getQty()+1); h.tvQ.setText(String.valueOf(i.getQty())); n.onQtyChange(); } });
            h.btnK.setOnClickListener(v -> { if(i.getQty() > 0){ i.setQty(i.getQty()-1); h.tvQ.setText(String.valueOf(i.getQty())); n.onQtyChange(); } });
        }
        @Override public int getItemCount() { return l.size(); }
        public static class VH extends RecyclerView.ViewHolder {
            TextView tvN, tvH, tvQ; Button btnT, btnK;
            NetworkImageView iv;
            public VH(View v) { super(v);
                tvN = v.findViewById(R.id.tvNamaProdukKasir); tvH = v.findViewById(R.id.tvHargaStokKasir);
                tvQ = v.findViewById(R.id.tvQty); btnT = v.findViewById(R.id.btnTambah); btnK = v.findViewById(R.id.btnKurang);
                iv = v.findViewById(R.id.ivGambarKasir);
            }
        }
        public interface OnQtyChangeListener { void onQtyChange(); }
    }
}