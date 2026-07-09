<?php
header('Content-Type: application/json');
require_once 'config/db.php';

$input    = json_decode(file_get_contents('php://input'), true);
$email    = isset($input['email']) ? trim($input['email']) : '';
$password = isset($input['password']) ? trim($input['password']) : '';

if (empty($email) || empty($password)) {
    echo json_encode(["status" => "error", "message" => "Email dan password wajib diisi"]);
    exit;
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(["status" => "error", "message" => "Format email tidak valid"]);
    exit;
}

// Cek apakah email sudah ada
$stmt = $conn->prepare("SELECT id, is_active FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$check = $stmt->get_result();

$otp        = strval(rand(100000, 999999));
$hashedPass = password_hash($password, PASSWORD_DEFAULT);

if ($check->num_rows > 0) {
    $row = $check->fetch_assoc();
    if ($row['is_active'] == 1) {
        echo json_encode(["status" => "error", "message" => "Email sudah terdaftar dan aktif"]);
        exit;
    }
    // Email ada tapi belum aktif -> update password, OTP baru, DAN PAKSA ROLE JADI 'pengguna_kos'
    $update = $conn->prepare("UPDATE users SET password = ?, otp_code = ?, role = 'pengguna_kos' WHERE email = ?");
    $update->bind_param("sss", $hashedPass, $otp, $email);
    $update->execute();
} else {
    // Email baru -> Insert dengan role 'pengguna_kos'
    $insert = $conn->prepare(
        "INSERT INTO users (email, password, role, otp_code, is_active) VALUES (?, ?, 'pengguna_kos', ?, 0)"
    );
    $insert->bind_param("sss", $email, $hashedPass, $otp);
    $insert->execute();
}

// SIMPLE MECHANIC: Tidak kirim email asli, cukup simpan di DB
// User akan melihat kodenya langsung di database (phpMyAdmin)
echo json_encode([
    "status"    => "success",
    "message"   => "Registrasi berhasil! Silakan cek kode OTP di Database.",
    "otp_debug" => $otp // Gue tetep kasih ini biar lu gampang ngetest di Android
]);
?>