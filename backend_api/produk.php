<?php
header('Content-Type: application/json');
include 'koneksi.php';

$sql = "SELECT * FROM produk ORDER BY id DESC";
$result = $conn->query($sql);
$data = [];

while($row = $result->fetch_assoc()) {
    $row['kategori'] = $row['kategori'] ?? 'Lainnya'; // Jika null, ganti jadi 'Lainnya'
    $row['gambar_url'] = "http://10.0.2.2/backend_api/uploads/" . $row['gambar'];
    $data[] = $row;
}
echo json_encode(["status" => "success", "data" => $data]);
?>