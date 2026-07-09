package com.example.pesananmakanan;

public class ApiConfig {
    // Pakai 10.0.2.2 jika pakai Emulator
    public static final String BASE_URL = "http://10.0.2.2/backend_api/";

    public static final String REGISTER_URL = BASE_URL + "register.php";
    public static final String REGISTER_FIREBASE_URL = BASE_URL + "register_firebase.php";

    public static final String OTP_REQUEST_URL = BASE_URL + "otp_request.php";

    // FIX: nama file backend yang benar adalah "verify_otp.php" (sebelumnya
    // ditulis terbalik "otp_verify.php" sehingga selalu 404 / "Verifikasi Gagal (Jaringan)")
    public static final String OTP_VERIFY_URL = BASE_URL + "verify_otp.php";

    // Verifikasi email sekarang pakai Firebase (link email), endpoint ini cuma
    // buat aktifin flag is_active di MySQL setelah Firebase konfirmasi email verified
    public static final String VERIFY_FIREBASE_EMAIL_URL = BASE_URL + "verify_firebase_email.php";

    public static final String LOGIN_URL = BASE_URL + "login.php";
    public static final String PRODUK_URL = BASE_URL + "produk.php";
    public static final String PRODUK_SIMPAN_URL = BASE_URL + "produk_simpan.php";
    public static final String PRODUK_UPDATE_URL = BASE_URL + "produk_update.php";
    public static final String PRODUK_DELETE_URL = BASE_URL + "produk_delete.php";
    public static final String TRANSAKSI_URL = BASE_URL + "transaksi.php";
    public static final String DASHBOARD_URL = BASE_URL + "dashboard.php";
    public static final String INFO_PENGGUNA_URL = BASE_URL + "info_pengguna.php";
    public static final String AMBIL_PENGGUNA_URL = BASE_URL + "ambil_pengguna.php";
    public static final String UPDATE_PENGHUNI_URL = BASE_URL + "update_penghuni.php";
}
