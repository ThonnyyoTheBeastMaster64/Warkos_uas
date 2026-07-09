<?php
header('Content-Type: application/json');
require_once 'config/db.php';

// Ambil semua user kecuali Admin (untuk daftar orang yang bisa ngutang)
$stmt = $conn->prepare("SELECT email, no_kamar FROM users WHERE role != 'admin'");
$stmt->execute();
$result = $stmt->get_result();

$data = [];
while ($row = $result->fetch_assoc()) {
    $data[] = $row;
}

echo json_encode(["status" => "success", "data" => $data]);
?>