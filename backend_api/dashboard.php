<?php
header('Content-Type: application/json');
include 'koneksi.php'; // Pakai koneksi.php sesuai screenshot user

$tanggal = isset($_GET['tanggal']) ? $_GET['tanggal'] : date('Y-m-d');

// Total Omzet
$sqlOmzet = "SELECT SUM(total_harga) as total FROM transaksi WHERE DATE(tanggal) = '$tanggal'";
$resOmzet = $conn->query($sqlOmzet);
$totalOmzet = $resOmzet->fetch_assoc()['total'] ?? 0;

// Jumlah Transaksi
$sqlCount = "SELECT COUNT(*) as total FROM transaksi WHERE DATE(tanggal) = '$tanggal'";
$resCount = $conn->query($sqlCount);
$jumlahTransaksi = $resCount->fetch_assoc()['total'] ?? 0;

// Produk Terjual
$sqlProduk = "SELECT p.nama_produk, SUM(t.jumlah) as total_qty, SUM(t.total_harga) as total_subtotal
              FROM transaksi t
              JOIN produk p ON t.produk_id = p.id
              WHERE DATE(t.tanggal) = '$tanggal'
              GROUP BY t.produk_id";
$resProduk = $conn->query($sqlProduk);

$produkTerjual = [];
while($row = $resProduk->fetch_assoc()) {
    $produkTerjual[] = [
        "nama_produk" => $row['nama_produk'],
        "total_qty" => (int)$row['total_qty'],
        "total_subtotal" => (double)$row['total_subtotal']
    ];
}

echo json_encode([
    "status" => "success",
    "tanggal" => $tanggal,
    "total_omzet" => (double)$totalOmzet,
    "jumlah_transaksi" => (int)$jumlahTransaksi,
    "produk_terjual" => $produkTerjual
]);
?>