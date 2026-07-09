<?php
header('Content-Type: application/json');
include 'koneksi.php';

$input = json_decode(file_get_contents('php://input'), true);
$email = isset($input['email']) ? trim($input['email']) : '';
$otp   = isset($input['otp']) ? trim($input['otp']) : '';   // <-- key "otp", cocok dengan Android

if (empty($email) || empty($otp)) {
    echo json_encode(["status" => "error", "message" => "Email dan kode OTP wajib diisi"]);
    exit;
}

$stmt = $conn->prepare("SELECT id FROM users WHERE email = ? AND otp_code = ?");
$stmt->bind_param("ss", $email, $otp);
$stmt->execute();
$res = $stmt->get_result();

if ($res->num_rows > 0) {
    $update = $conn->prepare("UPDATE users SET is_active = 1, otp_code = NULL WHERE email = ?");
    $update->bind_param("s", $email);
    $update->execute();

    echo json_encode(["status" => "success", "message" => "Akun aktif, silakan login"]);
} else {
    echo json_encode(["status" => "error", "message" => "Kode OTP salah"]);
}
