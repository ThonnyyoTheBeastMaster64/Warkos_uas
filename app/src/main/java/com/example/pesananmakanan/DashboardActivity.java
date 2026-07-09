package com.example.pesananmakanan;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvTanggal, tvTotalOmzet, tvJumlahTransaksi;
    private RecyclerView rvProdukTerjual;
    private ProgressBar progressDashboard;
    private String tanggalDipilih;
    private ProdukTerjualAdapter adapter;
    private List<ProdukTerjualAdapter.ItemTerjual> daftarTerjual = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvTanggal = findViewById(R.id.tvTanggalDashboard);
        tvTotalOmzet = findViewById(R.id.tvTotalOmzet);
        tvJumlahTransaksi = findViewById(R.id.tvJumlahTransaksi);
        rvProdukTerjual = findViewById(R.id.rvProdukTerjual);
        progressDashboard = findViewById(R.id.progressDashboard);

        rvProdukTerjual.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProdukTerjualAdapter(daftarTerjual);
        rvProdukTerjual.setAdapter(adapter);

        Calendar today = Calendar.getInstance();
        tanggalDipilih = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH));

        findViewById(R.id.btnPilihTanggal).setOnClickListener(v -> tampilkanDatePicker());
        findViewById(R.id.btnBackDashboard).setOnClickListener(v -> finish());
        findViewById(R.id.btnGoToMenu).setOnClickListener(v -> {
            // Jika sudah ada AdminMainActivity di stack, finish() saja. 
            // Tapi buat jaga-jaga kita start activity-nya.
            finish(); 
        });
        ambilDashboard();
    }

    private void tampilkanDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            tanggalDipilih = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            ambilDashboard();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void ambilDashboard() {
        progressDashboard.setVisibility(View.VISIBLE);
        String url = ApiConfig.DASHBOARD_URL + "?tanggal=" + tanggalDipilih;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressDashboard.setVisibility(View.GONE);
                    try {
                        tvTanggal.setText("Rekap Tanggal: " + response.getString("tanggal"));
                        
                        NumberFormat formatter = NumberFormat.getInstance(new Locale("in", "ID"));
                        tvTotalOmzet.setText("Rp " + formatter.format(response.getDouble("total_omzet")));

                        tvJumlahTransaksi.setText(response.getInt("jumlah_transaksi") + " transaksi");

                        daftarTerjual.clear();
                        JSONArray data = response.getJSONArray("produk_terjual");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject o = data.getJSONObject(i);
                            daftarTerjual.add(new ProdukTerjualAdapter.ItemTerjual(
                                    o.getString("nama_produk"), o.getInt("total_qty"), o.getDouble("total_subtotal")));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Toast.makeText(this, "Gagal memproses data dashboard", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDashboard.setVisibility(View.GONE);
                    String message = "Gagal mengambil data dashboard";
                    if (error.networkResponse != null) {
                        message += " (Status: " + error.networkResponse.statusCode + ")";
                    } else if (error.getMessage() != null) {
                        message += " (" + error.getMessage() + ")";
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                });
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}