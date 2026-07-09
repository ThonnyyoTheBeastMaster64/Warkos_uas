package com.example.pesananmakanan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.NetworkImageView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class KasirAdapter extends RecyclerView.Adapter<KasirAdapter.ViewHolder> {

    public interface OnQtyChangeListener { void onQtyChange(); }

    private final List<Produk> daftarProduk;
    private final OnQtyChangeListener listener;
    private final Context context;

    public KasirAdapter(Context context, List<Produk> daftarProduk, OnQtyChangeListener listener) {
        this.context = context; this.daftarProduk = daftarProduk; this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produk_kasir, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Produk p = daftarProduk.get(position);
        holder.tvNama.setText(p.getNamaProduk());
        holder.tvKategori.setText(p.getKategori());

        NumberFormat formatter = NumberFormat.getInstance(new Locale("in", "ID"));
        holder.tvHargaStok.setText("Rp " + formatter.format(p.getHarga()) + " - Stok: " + p.getStok());

        holder.tvQty.setText(String.valueOf(p.getQty()));

        if (p.getGambarUrl() != null) {
            holder.ivGambar.setImageUrl(p.getGambarUrl(), VolleySingleton.getInstance(context).getImageLoader());
        } else {
            holder.ivGambar.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.btnTambah.setOnClickListener(v -> {
            if (p.getQty() < p.getStok()) {
                p.setQty(p.getQty() + 1);
                holder.tvQty.setText(String.valueOf(p.getQty()));
                listener.onQtyChange();
            } else {
                android.widget.Toast.makeText(context, "Stok tidak cukup", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnKurang.setOnClickListener(v -> {
            if (p.getQty() > 0) {
                p.setQty(p.getQty() - 1);
                holder.tvQty.setText(String.valueOf(p.getQty()));
                listener.onQtyChange();
            }
        });
    }

    @Override public int getItemCount() { return daftarProduk.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        NetworkImageView ivGambar;
        TextView tvNama, tvKategori, tvHargaStok, tvQty;
        Button btnTambah, btnKurang;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGambar = itemView.findViewById(R.id.ivGambarKasir);
            tvNama = itemView.findViewById(R.id.tvNamaProdukKasir);
            tvKategori = itemView.findViewById(R.id.tvKategoriKasir);
            tvHargaStok = itemView.findViewById(R.id.tvHargaStokKasir);
            tvQty = itemView.findViewById(R.id.tvQty);
            btnTambah = itemView.findViewById(R.id.btnTambah);
            btnKurang = itemView.findViewById(R.id.btnKurang);
        }
    }
}