<?php
header('Content-Type: application/json');

// Gunakan path absolut biar gak error "No such file or directory"
$db_path = __DIR__ . '/config/db.php';
if (file_exists($db_path)) {
    require_once $db_path;
} else {
    echo json_encode(['status' => 'error', 'message' => 'File config/db.php tidak ditemukan di ' . $db_path]);
    exit;
}

$json = file_get_contents('php://input');
$data = json_decode($json, true);

// Untuk Firebase, kita cuma butuh Email buat cek Role
$email = $data['email'] ?? '';

if (empty($email)) {
    echo json_encode(['status' => 'error', 'message' => 'Email kosong!']);
    exit;
}

$stmt = $conn->prepare("SELECT role, is_active FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();

    // Cek apakah akun sudah aktif (di-set 1 pas verifikasi Firebase)
    if ($user['is_active'] == 0) {
        echo json_encode(['status' => 'error', 'message' => 'Akun belum aktif di database']);
        exit;
    }

    echo json_encode([
        "status" => "success",
        "message" => "Role ditemukan",
        "role" => $user['role']
    ]);
} else {
    // Jika di Firebase ada tapi di MySQL belum ada, kita buatkan otomatis
    $role = 'pengguna_kos';
    $active = 1;
    $insert = $conn->prepare("INSERT INTO users (email, role, is_active) VALUES (?, ?, ?)");
    $insert->bind_param("ssi", $email, $role, $active);

    if ($insert->execute()) {
        echo json_encode([
            "status" => "success",
            "message" => "User baru didaftarkan",
            "role" => $role
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Gagal membuat user baru di database"]);
    }
}
?>