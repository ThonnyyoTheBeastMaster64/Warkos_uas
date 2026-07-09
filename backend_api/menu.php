<?php
header('Content-Type: application/json');
include 'koneksi.php';

$stmt = $pdo->query("SELECT * FROM menu");
$menu = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo json_encode(["status" => "success", "data" => $menu]);
?>