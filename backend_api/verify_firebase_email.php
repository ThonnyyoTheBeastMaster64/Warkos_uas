<?php
include 'koneksi.php';
header('Content-Type: application/json');
$json = file_get_contents('php://input');
$data = json_decode($json, true);
$email = $data['email'] ?? '';
// Query untuk mengaktifkan akun di database
$sql = "UPDATE users SET is_active = 1 WHERE email = '$email'";
if (mysqli_query($conn, $sql)) {
    echo json_encode(["status" => "success"]);
} else {
    echo json_encode(["status" => "error", "message" => mysqli_error($conn)]);
}
?>