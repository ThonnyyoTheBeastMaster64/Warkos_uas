<?php
header('Content-Type: application/json');
include 'koneksi.php';

// Buat folder uploads jika belum ada
if (!file_exists('uploads')) { mkdir('uploads', 0777, true); }

$nama = $_POST['nama_produk'] ?? '';
$harga = $_POST['harga'] ?? 0;
$stok = $_POST['stok'] ?? 0;

if(isset($_FILES['gambar'])) {
    $ext = pathinfo($_FILES['gambar']['name'], PATHINFO_EXTENSION);
    $nama_gambar = time() . "_" . uniqid() . "." . $ext;
    $target = "uploads/" . $nama_gambar;

    if (move_uploaded_file($_FILES['gambar']['tmp_name'], $target)) {
        $sql = "INSERT INTO produk (nama_produk, harga, stok, gambar) VALUES ('$nama', '$harga', '$stok', '$nama_gambar')";
        if ($conn->query($sql)) {
            echo json_encode(["status" => "success", "message" => "Produk disimpan"]);
        } else {
            echo json_encode(["status" => "error", "message" => "Gagal simpan ke DB: " . $conn->error]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "Gagal upload file"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Gambar tidak ada"]);
}
?>
