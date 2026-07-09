<?php
header('Content-Type: application/json');
include 'koneksi.php';

$method = $_SERVER['REQUEST_METHOD'];

if ($method == 'GET') {
    // READ: Ambil riwayat pesanan (Join dengan tabel menu untuk ambil nama makanan)
    $stmt = $pdo->query("SELECT p.*, m.nama_menu FROM pesanan p JOIN menu m ON p.menu_id = m.id ORDER BY p.id DESC");
    $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode(["status" => "success", "data" => $data]);

} elseif ($method == 'POST') {
    // CREATE: Tambah pesanan baru
    $data = json_decode(file_get_contents('php://input'), true);
    $menu_id = $data['menu_id'];
    $nama_pemesan = $data['nama_pemesan'];
    $jumlah = $data['jumlah'];
    $alamat = $data['alamat'];
    $catatan = $data['catatan'] ?? '';

    // Ambil harga menu untuk hitung total_harga otomatis
    $s = $pdo->prepare("SELECT harga FROM menu WHERE id = ?");
    $s->execute([$menu_id]);
    $m = $s->fetch();
    $total_harga = $m['harga'] * $jumlah;

    $sql = "INSERT INTO pesanan (menu_id, nama_pemesan, jumlah, alamat, catatan, total_harga) VALUES (?, ?, ?, ?, ?, ?)";
    $stmt = $pdo->prepare($sql);
    if ($stmt->execute([$menu_id, $nama_pemesan, $jumlah, $alamat, $catatan, $total_harga])) {
        echo json_encode(["status" => "success", "message" => "Pesanan berhasil dibuat"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Gagal membuat pesanan"]);
    }
}
?>