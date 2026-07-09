<?php
header('Content-Type: application/json');
include 'koneksi.php';

// Terima dari form-urlencoded ATAU JSON body, biar fleksibel
$email = $_POST['email'] ?? null;
if (empty($email)) {
    $input = json_decode(file_get_contents('php://input'), true);
    $email = $input['email'] ?? '';
}
$email = trim($email);

if (empty($email)) {
    echo json_encode(["status" => "error", "message" => "Email wajib diisi"]);
    exit;
}

// Pastikan emailnya memang terdaftar di users
$check = $conn->prepare("SELECT id FROM users WHERE email = ?");
$check->bind_param("s", $email);
$check->execute();
if ($check->get_result()->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "Email belum terdaftar"]);
    exit;
}

$otp = strval(rand(100000, 999999)); // 6 digit acak

$stmt = $conn->prepare("UPDATE users SET otp_code = ? WHERE email = ?");
$stmt->bind_param("ss", $otp, $email);

if ($stmt->execute()) {
    // otp_debug cuma buat tahap testing, hapus/kirim via email asli saat production
    echo json_encode(["status" => "success", "message" => "OTP terkirim", "otp_debug" => $otp]);
} else {
    echo json_encode(["status" => "error", "message" => "Gagal mengirim OTP"]);
}
