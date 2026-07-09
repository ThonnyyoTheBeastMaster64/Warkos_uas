package com.example.pesananmakanan;

public class Produk {
    private int id;
    private String namaProduk;
    private String kategori;
    private double harga;
    private int stok;
    private String gambarUrl;
    private int qty = 0;

    public Produk(int id, String namaProduk, String kategori, double harga, int stok, String gambarUrl) {
        this.id = id;
        this.namaProduk = namaProduk;
        this.kategori = kategori;
        this.harga = harga;
        this.stok = stok;
        this.gambarUrl = gambarUrl;
    }

    public int getId() { return id; }
    public String getNamaProduk() { return namaProduk; }
    public String getKategori() { return kategori; }
    public double getHarga() { return harga; }
    public int getStok() { return stok; }
    public String getGambarUrl() { return gambarUrl; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
}