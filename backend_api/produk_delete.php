<?php
header('Content-Type: application/json');
include 'koneksi.php';

$input = json_decode(file_get_contents('php://input'), true);
$id = $input['id'];

// Opsional: Hapus file gambar dari folder uploads
$res = $conn->query("SELECT gambar FROM produk WHERE id=$id");
$data = $res->fetch_assoc();
if ($data && file_exists("uploads/".$data['gambar'])) {
    unlink("uploads/".$data['gambar']);
}

$sql = "DELETE FROM produk WHERE id = $id";

if ($conn->query($sql)) {
    echo json_encode(["status" => "success", "message" => "Produk berhasil dihapus"]);
} else {
    echo json_encode(["status" => "error", "message" => "Gagal menghapus: " . $conn->error]);
}
?>