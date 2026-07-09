<?php
header('Content-Type: application/json');
require_once 'config/db.php';

$json = file_get_contents('php://input');
$data = json_decode($json, true);

$email = $data['email'] ?? '';

if (empty($email)) {
    echo json_encode(['status' => 'error', 'message' => 'Email harus diisi']);
    exit;
}

// Kita cuma cari Role berdasarkan Email, karena Password sudah divalidasi Firebase di HP
$stmt = $conn->prepare("SELECT role, is_active FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();
    
    // Pastikan akun aktif
    if ($user['is_active'] == 0) {
        echo json_encode(['status' => 'error', 'message' => 'Akun belum aktif']);
        exit;
    }

    echo json_encode([
        "status" => "success",
        "role" => $user['role']
    ]);
} else {
    // Kalau email belum ada di MySQL tapi sudah ada di Firebase, kita masukin default sebagai pengguna_kos
    $insert = $conn->prepare("INSERT INTO users (email, role, is_active) VALUES (?, 'pengguna_kos', 1)");
    $insert->bind_param("s", $email);
    $insert->execute();
    
    echo json_encode([
        "status" => "success",
        "role" => "pengguna_kos"
    ]);
}
?>