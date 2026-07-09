<?php
header('Content-Type: application/json');
require_once 'config/db.php';

$json = file_get_contents('php://input');
$data = json_decode($json, true);

$email = $data['email'] ?? '';
$no_kamar = $data['no_kamar'] ?? '';
$harga_kamar = $data['harga_kamar'] ?? 0;
$total_hutang = $data['total_hutang'] ?? 0;

if (empty($email)) {
    echo json_encode(['status' => 'error', 'message' => 'Email wajib diisi']);
    exit;
}

$stmt = $conn->prepare("UPDATE users SET no_kamar = ?, harga_kamar = ?, total_hutang = ? WHERE email = ?");
$stmt->bind_param("sdis", $no_kamar, $harga_kamar, $total_hutang, $email);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Data berhasil diupdate"]);
} else {
    echo json_encode(["status" => "error", "message" => "Gagal update data"]);
}
?>