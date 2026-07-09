package com.example.pesananmakanan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProdukTerjualAdapter extends RecyclerView.Adapter<ProdukTerjualAdapter.ViewHolder> {

    public static class ItemTerjual {
        public String namaProduk; public int totalQty; public double totalSubtotal;
        public ItemTerjual(String n, int q, double s) { namaProduk = n; totalQty = q; totalSubtotal = s; }
    }

    private final List<ItemTerjual> daftar;
    public ProdukTerjualAdapter(List<ItemTerjual> daftar) { this.daftar = daftar; }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produk_terjual, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemTerjual item = daftar.get(position);
        holder.tvNama.setText(item.namaProduk);
        holder.tvQty.setText(item.totalQty + " terjual");

        NumberFormat formatter = NumberFormat.getInstance(new Locale("in", "ID"));
        holder.tvSubtotal.setText("Rp " + formatter.format(item.totalSubtotal));
    }

    @Override public int getItemCount() { return daftar.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvQty, tvSubtotal;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNamaProdukTerjual);
            tvQty = itemView.findViewById(R.id.tvQtyProdukTerjual);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotalProdukTerjual);
        }
    }
}