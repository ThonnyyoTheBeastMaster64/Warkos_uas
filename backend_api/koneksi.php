<?php
$conn = new mysqli("localhost", "root", "", "db_pesanan_makanan");
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Koneksi Gagal"]));
}
?>