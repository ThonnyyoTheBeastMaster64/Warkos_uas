<?php
error_reporting(0); // Matikan error HTML agar tidak merusak JSON
header('Content-Type: application/json');
include 'koneksi.php';

$input = json_decode(file_get_contents('php://input'), true);
if (!$input) { die(json_encode(["status" => "error", "message" => "Input Kosong"])); }

$items = $input['items'];
$uang_bayar = (double)$input['uang_dibayar'];
$total_semua = 0;
$tanggal = date('Y-m-d H:i:s');

$conn->begin_transaction();
try {
    foreach ($items as $item) {
        $pid = $item['produk_id'];
        $qty = $item['qty'];
        $res = $conn->query("SELECT harga FROM produk WHERE id = $pid");
        $p = $res->fetch_assoc();
        $total_semua += ($p['harga'] * $qty);
    }

    $kembalian = $uang_bayar - $total_semua;
    $conn->query("INSERT INTO transaksi (total_harga, uang_bayar, kembalian, tanggal) VALUES ($total_semua, $uang_bayar, $kembalian, '$tanggal')");
    $trans_id = $conn->insert_id;

    foreach ($items as $item) {
        $pid = $item['produk_id'];
        $qty = $item['qty'];
        $res = $conn->query("SELECT harga FROM produk WHERE id = $pid");
        $p = $res->fetch_assoc();
        $sub = $p['harga'] * $qty;
        
        $conn->query("INSERT INTO transaksi_detail (transaksi_id, produk_id, jumlah, subtotal) VALUES ($trans_id, $pid, $qty, $sub)");
        $conn->query("UPDATE produk SET stok = stok - $qty WHERE id = $pid");
    }

    $conn->commit();
    echo json_encode(["status" => "success", "message" => "Berhasil", "kembalian" => $kembalian]);
} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["status" => "error", "message" => "Gagal: " . $e->getMessage()]);
}
?>