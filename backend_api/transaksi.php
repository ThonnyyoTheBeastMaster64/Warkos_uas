<?php
header('Content-Type: application/json');
require_once 'config/db.php';

$input = json_decode(file_get_contents('php://input'), true);
if (!$input) { die(json_encode(["status" => "error", "message" => "Input Kosong"])); }

$items = $input['items'];
$uang_bayar = (double)$input['uang_dibayar'];
$tipe_bayar = $input['tipe_bayar'] ?? 'cash'; // 'cash', 'saldo', 'utang'
$user_id = $input['user_id'] ?? ''; // Email atau ID user kos
$total_semua = 0;
$tanggal = date('Y-m-d H:i:s');

$conn->begin_transaction();
try {
    // 1. Hitung Total & Cek Stok
    foreach ($items as $item) {
        $pid = $item['produk_id'];
        $qty = $item['qty'];
        $stmt = $conn->prepare("SELECT harga, stok FROM produk WHERE id = ?");
        $stmt->bind_param("i", $pid);
        $stmt->execute();
        $p = $stmt->get_result()->fetch_assoc();

        if ($p['stok'] < $qty) { throw new Exception("Stok tidak cukup untuk produk ID $pid"); }
        $total_semua += ($p['harga'] * $qty);
    }

    $kembalian = ($tipe_bayar == 'cash') ? ($uang_bayar - $total_semua) : 0;
    if ($tipe_bayar == 'cash' && $kembalian < 0) { throw new Exception("Uang tidak cukup"); }

    // 2. Handle Utang
    if ($tipe_bayar == 'utang' || $tipe_bayar == 'utang (anak kos)') {
        if (empty($user_id)) { throw new Exception("User ID wajib diisi untuk utang"); }
        // Update hutang di tabel users
        $stmt_u = $conn->prepare("UPDATE users SET total_hutang = total_hutang + ? WHERE email = ?");
        $stmt_u->bind_param("ds", $total_semua, $user_id);
        $stmt_u->execute();
        if ($conn->affected_rows == 0) { throw new Exception("User tidak ditemukan atau gagal update hutang"); }
    }

    // 3. Simpan Transaksi Master
    $stmt_t = $conn->prepare("INSERT INTO transaksi (total_harga, uang_bayar, kembalian, tanggal) VALUES (?, ?, ?, ?)");
    $stmt_t->bind_param("ddds", $total_semua, $uang_bayar, $kembalian, $tanggal);
    $stmt_t->execute();
    $trans_id = $conn->insert_id;

    // 4. Simpan Detail & Potong Stok
    foreach ($items as $item) {
        $pid = $item['produk_id'];
        $qty = $item['qty'];

        $stmt_p = $conn->prepare("SELECT harga FROM produk WHERE id = ?");
        $stmt_p->bind_param("i", $pid);
        $stmt_p->execute();
        $p = $stmt_p->get_result()->fetch_assoc();
        $sub = $p['harga'] * $qty;

        $stmt_d = $conn->prepare("INSERT INTO transaksi_detail (transaksi_id, produk_id, jumlah, subtotal) VALUES (?, ?, ?, ?)");
        $stmt_d->bind_param("iiid", $trans_id, $pid, $qty, $sub);
        $stmt_d->execute();

        $stmt_s = $conn->prepare("UPDATE produk SET stok = stok - ? WHERE id = ?");
        $stmt_s->bind_param("ii", $qty, $pid);
        $stmt_s->execute();
    }

    $conn->commit();
    echo json_encode(["status" => "success", "message" => "Transaksi Berhasil", "kembalian" => $kembalian]);
} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
?>