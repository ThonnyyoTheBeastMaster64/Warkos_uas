<?php
header('Content-Type: application/json');
require_once 'config/db.php';

// KODE RAHASIA
$ADMIN_SECRET = "ADMIN123";
$KASIR_SECRET = "KASIR123";

$json = file_get_contents('php://input');
$data = json_decode($json, true);

$email = $data['email'] ?? '';
$role = $data['role'] ?? '';
$secret_code = $data['secret_code'] ?? '';

if (empty($email) || empty($role)) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Email dan Role wajib diisi']);
    exit;
}

// Validasi Role (Hanya Admin dan Kasir)
if ($role !== 'admin' && $role !== 'kasir') {
    http_response_code(403);
    echo json_encode(['status' => 'error', 'message' => 'Role tidak valid! Hanya Admin dan Kasir yang diperbolehkan.']);
    exit;
}

// Validasi Kode Rahasia
if ($role === 'admin') {
    if ($secret_code !== $ADMIN_SECRET) {
        http_response_code(403);
        echo json_encode(['status' => 'error', 'message' => 'Kode rahasia ADMIN salah!']);
        exit;
    }
} else if ($role === 'kasir') {
    if ($secret_code !== $KASIR_SECRET) {
        http_response_code(403);
        echo json_encode(['status' => 'error', 'message' => 'Kode rahasia KASIR salah!']);
        exit;
    }
}

// Simpan ke database
$stmt = $conn->prepare("INSERT INTO users (email, role, is_active) VALUES (?, ?, 1) ON DUPLICATE KEY UPDATE role = ?, is_active = 1");
$stmt->bind_param("sss", $email, $role, $role);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Akun " . $role . " berhasil disimpan"]);
} else {
    http_response_code(500);
    echo json_encode(["status" => "error", "message" => "Gagal simpan ke database: " . $conn->error]);
}
?>