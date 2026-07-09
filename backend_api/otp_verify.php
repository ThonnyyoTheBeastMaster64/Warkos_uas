<?php
header('Content-Type: application/json');
require_once 'config/db.php';

$json = file_get_contents('php://input');
$data = json_decode($json, true);

$email = $data['email'] ?? '';
$otp = $data['kode_otp'] ?? '';

if (empty($email) || empty($otp)) {
    echo json_encode(['status' => 'error', 'message' => 'Email dan OTP wajib diisi']);
    exit;
}

// Cek OTP di database
$stmt = $conn->prepare("SELECT id FROM users WHERE email = ? AND otp_code = ?");
$stmt->bind_param("ss", $email, $otp);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    // Update status jadi active (sesuai field di register.php lu)
    $update = $conn->prepare("UPDATE users SET is_active = 1, otp_code = NULL WHERE email = ?");
    $update->bind_param("s", $email);

    if ($update->execute()) {
        echo json_encode(['status' => 'success', 'message' => 'Verifikasi berhasil! Silakan login.']);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Gagal update status aktivasi']);
    }
} else {
    echo json_encode(['status' => 'error', 'message' => 'Kode OTP salah atau tidak ditemukan']);
}
?>