<?php
header('Content-Type: application/json');
require_once 'config/db.php';

// Ganti dengan kode rahasia kalian sendiri untuk role admin/kasir
define('SECRET_CODE_STAFF', 'BUKOS2026');

$json = file_get_contents('php://input');
$data = json_decode($json, true);

$email  = trim($data['email'] ?? '');
$role   = trim($data['role'] ?? 'pengguna_kos');
$secret = trim($data['secret_code'] ?? '');

$rolesValid = ['pengguna_kos', 'admin', 'kasir'];
if (!in_array($role, $rolesValid)) {
    $role = 'pengguna_kos';
}

if (empty($email)) {
    echo json_encode(["status" => "error", "message" => "Email wajib diisi"]);
    exit;
}

// Admin/kasir wajib kode rahasia yang benar
if ($role !== 'pengguna_kos' && $secret !== SECRET_CODE_STAFF) {
    echo json_encode(["status" => "error", "message" => "Kode otorisasi salah"]);
    exit;
}

// is_active = 0 dulu, baru aktif setelah verifikasi OTP
$stmt = $conn->prepare(
    "INSERT INTO users (email, role, is_active) VALUES (?, ?, 0)
     ON DUPLICATE KEY UPDATE role = VALUES(role)"
);
$stmt->bind_param("ss", $email, $role);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Terdaftar, silakan verifikasi OTP"]);
} else {
    echo json_encode(["status" => "error", "message" => "Gagal menyimpan ke database"]);
}
