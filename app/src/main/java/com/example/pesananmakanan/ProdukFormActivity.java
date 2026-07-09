package com.example.pesananmakanan;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.NetworkImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class ProdukFormActivity extends AppCompatActivity {

    private EditText etNamaProduk, etHarga, etStok;
    private Spinner spKategori;
    private NetworkImageView ivPreviewLama;
    private ImageView ivPreviewBaru;
    private Button btnPilihGambar, btnSimpan;
    private ProgressBar progressForm;

    private String[] listKategori = {"Makanan", "Minuman", "Snack", "Lainnya"};

    private boolean isEdit = false;
    private int produkId;
    private File gambarTerpilih;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) prosesGambarDipilih(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produk_form);

        etNamaProduk = findViewById(R.id.etNamaProduk);
        spKategori = findViewById(R.id.spKategori);
        etHarga = findViewById(R.id.etHarga);
        etStok = findViewById(R.id.etStok);

        // Setup Spinner Adapter
        ArrayAdapter<String> adapterKategori = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listKategori);
        adapterKategori.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKategori.setAdapter(adapterKategori);
        ivPreviewLama = findViewById(R.id.ivPreviewLama);
        ivPreviewBaru = findViewById(R.id.ivPreviewBaru);
        btnPilihGambar = findViewById(R.id.btnPilihGambar);
        btnSimpan = findViewById(R.id.btnSimpanProduk);
        progressForm = findViewById(R.id.progressForm);

        // Tambahkan TextWatcher untuk format otomatis titik ribuan saat mengetik
        etHarga.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                etHarga.removeTextChangedListener(this);
                try {
                    String originalString = s.toString();

                    Long longval;
                    if (originalString.contains(".")) {
                        originalString = originalString.replaceAll("[.]", "");
                    }
                    longval = Long.parseLong(originalString);

                    NumberFormat formatter = NumberFormat.getInstance(new Locale("in", "ID"));
                    String formattedString = formatter.format(longval);

                    etHarga.setText(formattedString);
                    etHarga.setSelection(etHarga.getText().length());
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
                etHarga.addTextChangedListener(this);
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("is_edit", false)) {
            isEdit = true;
            produkId = extras.getInt("id");
            etNamaProduk.setText(extras.getString("nama_produk"));
            
            String kategoriLama = extras.getString("kategori");
            for (int i = 0; i < listKategori.length; i++) {
                if (listKategori[i].equalsIgnoreCase(kategoriLama)) {
                    spKategori.setSelection(i);
                    break;
                }
            }
            
            // Set harga awal dengan format titik jika sedang edit
            NumberFormat formatter = NumberFormat.getInstance(new Locale("in", "ID"));
            etHarga.setText(formatter.format((long) extras.getDouble("harga")));
            
            etStok.setText(String.valueOf(extras.getInt("stok")));
            String gambarUrl = extras.getString("gambar_url");
            if (gambarUrl != null) {
                ivPreviewLama.setImageUrl(gambarUrl, VolleySingleton.getInstance(this).getImageLoader());
            }
            btnSimpan.setText("Update Produk");
        }

        btnPilihGambar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnSimpan.setOnClickListener(v -> validasiDanSimpan());
    }

    private void prosesGambarDipilih(Uri uri) {
        String namaFile = getNamaFileDariUri(uri);
        String ext = (namaFile != null && namaFile.contains("."))
                ? namaFile.substring(namaFile.lastIndexOf('.') + 1).toLowerCase() : "";

        if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png")) {
            Toast.makeText(this, "Gambar harus format JPG atau PNG", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File cacheFile = new File(getCacheDir(), "upload_" + System.currentTimeMillis() + "." + ext);
            ContentResolver resolver = getContentResolver();
            InputStream in = resolver.openInputStream(uri);
            FileOutputStream out = new FileOutputStream(cacheFile);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
            in.close();
            out.close();

            if (cacheFile.length() > 2 * 1024 * 1024) {
                Toast.makeText(this, "Ukuran gambar maksimal 2MB", Toast.LENGTH_SHORT).show();
                return;
            }

            gambarTerpilih = cacheFile;
            ivPreviewBaru.setVisibility(View.VISIBLE);
            ivPreviewBaru.setImageURI(uri);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private String getNamaFileDariUri(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx != -1) result = cursor.getString(idx);
                }
            }
        }
        if (result == null) result = uri.getLastPathSegment();
        return result;
    }

    private void validasiDanSimpan() {
        String nama = etNamaProduk.getText().toString().trim();
        String kategori = spKategori.getSelectedItem().toString();
        String hargaStrRaw = etHarga.getText().toString().trim();
        String stokStr = etStok.getText().toString().trim();

        if (TextUtils.isEmpty(nama) || nama.length() < 3) {
            etNamaProduk.setError("Nama produk minimal 3 karakter"); return;
        }
        if (TextUtils.isEmpty(hargaStrRaw)) { etHarga.setError("Harga wajib diisi"); return; }
        if (TextUtils.isEmpty(stokStr)) { etStok.setError("Stok wajib diisi"); return; }

        // Hilangkan titik sebelum dikirim ke server/di-parse
        String hargaStr = hargaStrRaw.replaceAll("[.]", "");

        double harga; int stok;
        try {
            harga = Double.parseDouble(hargaStr);
            stok = Integer.parseInt(stokStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Format harga/stok tidak valid", Toast.LENGTH_SHORT).show(); return;
        }

        if (harga <= 0) { etHarga.setError("Harga harus lebih dari 0"); return; }
        if (stok < 0) { etStok.setError("Stok tidak boleh negatif"); return; }
        if (!isEdit && gambarTerpilih == null) {
            Toast.makeText(this, "Pilih gambar produk terlebih dahulu", Toast.LENGTH_SHORT).show(); return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("nama_produk", nama);
        params.put("kategori", kategori);
        params.put("harga", String.valueOf(harga));
        params.put("stok", String.valueOf(stok));

        String url;
        if (isEdit) { params.put("id", String.valueOf(produkId)); url = ApiConfig.PRODUK_UPDATE_URL; }
        else { url = ApiConfig.PRODUK_SIMPAN_URL; }

        progressForm.setVisibility(View.VISIBLE);
        btnSimpan.setEnabled(false);

        MultipartRequest request = new MultipartRequest(url, params, "gambar", gambarTerpilih,
                response -> {
                    progressForm.setVisibility(View.GONE);
                    btnSimpan.setEnabled(true);
                    try {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        if (response.getString("status").equals("success")) finish();
                    } catch (Exception e) {
                        Toast.makeText(this, "Terjadi kesalahan data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressForm.setVisibility(View.GONE);
                    btnSimpan.setEnabled(true);
                    String message = "Gagal terhubung ke server";
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