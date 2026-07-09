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

public class ProdukAdapter extends RecyclerView.Adapter<ProdukAdapter.ViewHolder> {

    public interface OnAksiListener {
        void onEdit(Produk produk);
        void onHapus(Produk produk, int position);
    }

    private final List<Produk> daftarProduk;
    private final OnAksiListener listener;
    private final Context context;

    public ProdukAdapter(Context context, List<Produk> daftarProduk, OnAksiListener listener) {
        this.context = context;
        this.daftarProduk = daftarProduk;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produk_admin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Produk p = daftarProduk.get(position);
        holder.tvNama.setText(p.getNamaProduk());
        holder.tvKategori.setText(p.getKategori());

        NumberFormat formatter = NumberFormat.getInstance(new Locale("in", "ID"));
        holder.tvHarga.setText("Rp " + formatter.format(p.getHarga()));

        holder.tvStok.setText("Stok: " + p.getStok());

        if (p.getGambarUrl() != null) {
            holder.ivGambar.setImageUrl(p.getGambarUrl(), VolleySingleton.getInstance(context).getImageLoader());
        } else {
            holder.ivGambar.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(p));
        holder.btnHapus.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onHapus(p, pos);
        });
    }

    @Override public int getItemCount() { return daftarProduk.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        NetworkImageView ivGambar;
        TextView tvNama, tvKategori, tvHarga, tvStok;
        Button btnEdit, btnHapus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGambar = itemView.findViewById(R.id.ivGambarProduk);
            tvNama = itemView.findViewById(R.id.tvNamaProdukAdmin);
            tvKategori = itemView.findViewById(R.id.tvKategoriAdmin);
            tvHarga = itemView.findViewById(R.id.tvHargaProdukAdmin);
            tvStok = itemView.findViewById(R.id.tvStokProdukAdmin);
            btnEdit = itemView.findViewById(R.id.btnEditProduk);
            btnHapus = itemView.findViewById(R.id.btnHapusProduk);
        }
    }
}