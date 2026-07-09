<?php
error_reporting(0); // SANGAT PENTING: Mencegah teks error merusak JSON
header('Content-Type: application/json');
include 'koneksi.php';

$tanggal = $_GET['tanggal'] ?? date('Y-m-d');

// Omzet
$resO = $conn->query("SELECT SUM(total_harga) as total FROM transaksi WHERE DATE(tanggal) = '$tanggal'");
$omzet = (double)($resO->fetch_assoc()['total'] ?? 0);

// Jumlah Transaksi
$resC = $conn->query("SELECT COUNT(*) as total FROM transaksi WHERE DATE(tanggal) = '$tanggal'");
$count = (int)($resC->fetch_assoc()['total'] ?? 0);

// Detail Produk Terjual
$resP = $conn->query("SELECT p.nama_produk, SUM(td.jumlah) as total_qty, SUM(td.subtotal) as sub 
                      FROM transaksi_detail td 
                      JOIN transaksi t ON td.transaksi_id = t.id 
                      JOIN produk p ON td.produk_id = p.id 
                      WHERE DATE(t.tanggal) = '$tanggal' 
                      GROUP BY td.produk_id");
$terjual = [];
while($row = $resP->fetch_assoc()){
    $terjual[] = ["nama_produk" => $row['nama_produk'], "total_qty" => (int)$row['total_qty'], "total_subtotal" => (double)$row['sub']];
}

echo json_encode([
    "status" => "success",
    "tanggal" => $tanggal,
    "total_omzet" => $omzet,
    "jumlah_transaksi" => $count,
    "produk_terjual" => $terjual
]);
?>