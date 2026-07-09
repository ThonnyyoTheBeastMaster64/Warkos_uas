<?php
header('Content-Type: application/json');
include 'koneksi.php';

$data = json_decode(file_get_contents('php://input'), true);
$id = $data['id'];

$stmt = $pdo->prepare("DELETE FROM pesanan WHERE id = ?");
if ($stmt->execute([$id])) {
    echo json_encode(["status" => "success", "message" => "Pesanan dihapus"]);
} else {
    echo json_encode(["status" => "error", "message" => "Gagal menghapus pesanan"]);
}
?>