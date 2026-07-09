<?php
header('Content-Type: application/json');
include 'koneksi.php';

$id = $_POST['id'] ?? 0;
$nama = $_POST['nama_produk'] ?? '';
$kategori = $_POST['kategori'] ?? '';
$harga = $_POST['harga'] ?? 0;
$stok = $_POST['stok'] ?? 0;

$sql_update = "UPDATE produk SET nama_produk='$nama', kategori='$kategori', harga='$harga', stok='$stok'";

// Jika ada gambar baru yang diupload
if(isset($_FILES['gambar']) && !empty($_FILES['gambar']['name'])) {
    $gambar = $_FILES['gambar']['name'];
    $tmp_name = $_FILES['gambar']['tmp_name'];
    $ext = pathinfo($gambar, PATHINFO_EXTENSION);
    $new_name = time() . "_" . uniqid() . "." . $ext;
    $path = "uploads/" . $new_name;

    if (move_uploaded_file($tmp_name, $path)) {
        $sql_update .= ", gambar='$new_name'";
    }
}

$sql_update .= " WHERE id=$id";

if ($conn->query($sql_update)) {
    echo json_encode(["status" => "success", "message" => "Produk berhasil diupdate"]);
} else {
    echo json_encode(["status" => "error", "message" => "Gagal update database: " . $conn->error]);
}
?>