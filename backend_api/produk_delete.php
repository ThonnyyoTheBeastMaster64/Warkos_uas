<?php
header('Content-Type: application/json');
require_once 'config/db.php';

$id = $_POST['id'];

$stmt = $conn->prepare("DELETE FROM produk WHERE id = ?");
$stmt->bind_param("i", $id);

if ($stmt->execute()) {
    echo json_encode(["status" => "success"]);
} else {
    echo json_encode(["status" => "error"]);
}
?>