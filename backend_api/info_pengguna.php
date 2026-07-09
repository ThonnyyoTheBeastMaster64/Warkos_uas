<?php
header('Content-Type: application/json');
require_once 'config/db.php';

$json = file_get_contents('php://input');
$data = json_decode($json, true);
$email = $data['email'] ?? '';

if (empty($email)) {
    echo json_encode(['status' => 'error', 'message' => 'Email kosong']);
    exit;
}

// Ambil data user, no_kamar, harga_kamar, dan total_hutang
// Kita asumsikan ada kolom 'no_kamar', 'harga_kamar', dan 'total_hutang' di tabel users
// Jika belum ada, script ini akan mengembalikan nilai default untuk demo
$stmt = $conn->prepare("SELECT no_kamar, harga_kamar, total_hutang FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();

    // Fallback jika datanya null (buat demo)
    $no_kamar = $user['no_kamar'] ?? "01";
    $harga_kamar = $user['harga_kamar'] ?? 230000;
    $total_hutang = $user['total_hutang'] ?? 0;

    echo json_encode([
        "status" => "success",
        "no_kamar" => $no_kamar,
        "harga_kamar" => (int)$harga_kamar,
        "total_hutang" => (int)$total_hutang
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "User tidak ditemukan"]);
}
?>