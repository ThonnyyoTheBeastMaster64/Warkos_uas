<?php
header('Content-Type: application/json');
include 'koneksi.php';

$data = json_decode(file_get_contents('php://input'), true);
$id = $data['id'];
$nama_pemesan = $data['nama_pemesan'];
$jumlah = $data['jumlah'];
$alamat = $data['alamat'];
$catatan = $data['catatan'] ?? '';

// Hitung ulang total harga
$s = $pdo->prepare("SELECT m.harga FROM pesanan p JOIN menu m ON p.menu_id = m.id WHERE p.id = ?");
$s->execute([$id]);
$m = $s->fetch();
$total_harga = $m['harga'] * $jumlah;

$sql = "UPDATE pesanan SET nama_pemesan=?, jumlah=?, alamat=?, catatan=?, total_harga=? WHERE id=?";
$stmt = $pdo->prepare($sql);
if ($stmt->execute([$nama_pemesan, $jumlah, $alamat, $catatan, $total_harga, $id])) {
    echo json_encode(["status" => "success", "message" => "Pesanan berhasil diupdate"]);
} else {
    echo json_encode(["status" => "error", "message" => "Gagal update pesanan"]);
}
?>
