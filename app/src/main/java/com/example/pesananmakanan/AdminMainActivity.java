package com.example.pesananmakanan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminMainActivity extends AppCompatActivity {

    private RecyclerView rvProduk;
    private ProgressBar progressAdmin;
    private List<Produk> daftarProduk = new ArrayList<>();
    private ProdukAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        rvProduk = findViewById(R.id.rvProdukAdmin);
        progressAdmin = findViewById(R.id.progressAdmin);
        rvProduk.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProdukAdapter(this, daftarProduk, new ProdukAdapter.OnAksiListener() {
            @Override public void onEdit(Produk p) {
                Intent i = new Intent(AdminMainActivity.this, ProdukFormActivity.class);
                i.putExtra("is_edit", true); i.putExtra("id", p.getId());
                i.putExtra("nama_produk", p.getNamaProduk()); i.putExtra("kategori", p.getKategori());
                i.putExtra("harga", p.getHarga()); i.putExtra("stok", p.getStok());
                i.putExtra("gambar_url", p.getGambarUrl()); startActivity(i);
            }
            @Override public void onHapus(Produk p, int pos) {
                new AlertDialog.Builder(AdminMainActivity.this).setTitle("Hapus")
                        .setMessage("Yakin hapus?").setPositiveButton("Ya", (d, w) -> hapusProduk(p, pos)).show();
            }
        });
        rvProduk.setAdapter(adapter);

        findViewById(R.id.fabTambahProduk).setOnClickListener(v -> startActivity(new Intent(this, ProdukFormActivity.class)));
        findViewById(R.id.btnDashboard).setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        findViewById(R.id.btnLogoutAdmin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override protected void onResume() { super.onResume(); ambilProduk(); }

    private void ambilProduk() {
        progressAdmin.setVisibility(View.VISIBLE);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, ApiConfig.PRODUK_URL, null,
                res -> {
                    progressAdmin.setVisibility(View.GONE);
                    try {
                        daftarProduk.clear();
                        JSONArray data = res.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject o = data.getJSONObject(i);
                            daftarProduk.add(new Produk(o.getInt("id"), o.getString("nama_produk"),
                                    o.optString("kategori", ""), o.getDouble("harga"), o.getInt("stok"),
                                    o.optString("gambar_url", null)));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) { e.printStackTrace(); }
                }, err -> progressAdmin.setVisibility(View.GONE));
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void hapusProduk(Produk p, int pos) {
        com.android.volley.toolbox.StringRequest req = new com.android.volley.toolbox.StringRequest(Request.Method.POST, ApiConfig.PRODUK_DELETE_URL,
                res -> {
                    try {
                        JSONObject jsonRes = new JSONObject(res);
                        if (jsonRes.getString("status").equals("success")) {
                            daftarProduk.remove(pos);
                            adapter.notifyItemRemoved(pos);
                        } else {
                            Toast.makeText(this, "Gagal: " + jsonRes.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        // Menampilkan respon mentah jika gagal parse JSON untuk debugging
                        String msg = res.length() > 50 ? res.substring(0, 50) + "..." : res;
                        Toast.makeText(this, "Respon tidak valid: " + msg, Toast.LENGTH_LONG).show();
                    }
                },
                err -> Toast.makeText(this, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.HashMap<String, String> params = new java.util.HashMap<>();
                params.put("id", String.valueOf(p.getId()));
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    // --- INNER CLASS ADAPTER (KOMPRESI) ---
    public static class ProdukAdapter extends RecyclerView.Adapter<ProdukAdapter.ViewHolder> {
        public interface OnAksiListener { void onEdit(Produk p); void onHapus(Produk p, int pos); }
        private List<Produk> list; private OnAksiListener listener; private Context ctx;

        public ProdukAdapter(Context ctx, List<Produk> list, OnAksiListener listener) {
            this.ctx = ctx; this.list = list; this.listener = listener;
        }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_produk_admin, p, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            Produk p = list.get(pos);
            h.tvN.setText(p.getNamaProduk()); h.tvK.setText(p.getKategori());
            h.tvH.setText("Rp " + NumberFormat.getInstance(new Locale("in", "ID")).format(p.getHarga()));
            h.tvS.setText("Stok: " + p.getStok());
            if (p.getGambarUrl() != null) h.iv.setImageUrl(p.getGambarUrl(), VolleySingleton.getInstance(ctx).getImageLoader());
            h.btnE.setOnClickListener(v -> listener.onEdit(p));
            h.btnH.setOnClickListener(v -> listener.onHapus(p, h.getBindingAdapterPosition()));
        }

        @Override public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            NetworkImageView iv; TextView tvN, tvK, tvH, tvS; Button btnE, btnH;
            ViewHolder(View v) { super(v);
                iv = v.findViewById(R.id.ivGambarProduk); tvN = v.findViewById(R.id.tvNamaProdukAdmin);
                tvK = v.findViewById(R.id.tvKategoriAdmin); tvH = v.findViewById(R.id.tvHargaProdukAdmin);
                tvS = v.findViewById(R.id.tvStokProdukAdmin); btnE = v.findViewById(R.id.btnEditProduk);
                btnH = v.findViewById(R.id.btnHapusProduk);
            }
        }
    }
}