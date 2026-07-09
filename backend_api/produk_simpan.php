<?php
header('Content-Type: application/json');
include 'koneksi.php';

// Buat folder uploads jika belum ada
if (!file_exists('uploads')) { mkdir('uploads', 0777, true); }

$nama = $_POST['nama_produk'] ?? '';
$kategori = $_POST['kategori'] ?? '';
$harga = $_POST['harga'] ?? 0;
$stok = $_POST['stok'] ?? 0;

if(isset($_FILES['gambar'])) {
    $gambar = $_FILES['gambar']['name'];
    $tmp_name = $_FILES['gambar']['tmp_name'];

    $ext = pathinfo($gambar, PATHINFO_EXTENSION);
    $new_name = time() . "_" . uniqid() . "." . $ext;
    $path = "uploads/" . $new_name;

    if (move_uploaded_file($tmp_name, $path)) {
        $sql = "INSERT INTO produk (nama_produk, kategori, harga, stok, gambar) VALUES ('$nama', '$kategori', '$harga', '$stok', '$new_name')";
        if ($conn->query($sql)) {
            echo json_encode(["status" => "success", "message" => "Produk berhasil disimpan"]);
        } else {
            echo json_encode(["status" => "error", "message" => "Gagal simpan ke database: " . $conn->error]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "Gagal upload gambar"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Gambar tidak ditemukan"]);
}
?>