<?php
header('Content-Type: application/json');
include 'koneksi.php';

$search = isset($_GET['search']) ? $_GET['search'] : '';
$kategori = isset($_GET['kategori']) ? $_GET['kategori'] : '';

$sql = "SELECT * FROM produk WHERE 1=1";
if (!empty($search)) {
    $sql .= " AND nama_produk LIKE '%$search%'";
}
if (!empty($kategori)) {
    $sql .= " AND kategori = '$kategori'";
}
$sql .= " ORDER BY id DESC";

$result = $conn->query($sql);
$data = [];

while($row = $result->fetch_assoc()) {
    $row['kategori'] = $row['kategori'] ?? 'Lainnya';
    $row['gambar_url'] = "http://10.0.2.2/backend_api/uploads/" . $row['gambar'];
    $data[] = $row;
}

// Ambil daftar kategori unik untuk filter di aplikasi
$resKat = $conn->query("SELECT DISTINCT kategori FROM produk WHERE kategori IS NOT NULL");
$listKategori = [];
while($rk = $resKat->fetch_assoc()) { $listKategori[] = $rk['kategori']; }

echo json_encode([
    "status" => "success",
    "data" => $data,
    "list_kategori" => $listKategori
]);
?>