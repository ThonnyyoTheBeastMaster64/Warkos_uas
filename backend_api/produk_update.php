<?php
header('Content-Type: application/json');
require_once 'config/db.php';

$id = $_POST['id'];
$nama = $_POST['nama_produk'];
$harga = $_POST['harga'];
$stok = $_POST['stok'];
$kategori = $_POST['kategori'];

$stmt = $conn->prepare("UPDATE produk SET nama_produk=?, harga=?, stok=?, kategori=? WHERE id=?");
$stmt->bind_param("sdisi", $nama, $harga, $stok, $kategori, $id);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Berhasil diupdate"]);
} else {
    echo json_encode(["status" => "error", "message" => "Gagal update"]);
}
?>