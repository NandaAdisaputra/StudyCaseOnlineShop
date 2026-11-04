package com.nandaadisaputra.onlineshop.data.session

import android.content.Context
import android.content.SharedPreferences

/**
 * UserPreference adalah kelas pengelola penyimpanan lokal (Local Storage) menggunakan Android [SharedPreferences].
 * Kelas ini berfungsi sebagai tempat penyimpanan data sesi pengguna di dalam memori internal perangkat
 * agar pengguna tetap berada dalam status login (tidak perlu login ulang) meskipun aplikasi ditutup atau dibuka kembali[cite: 10].
 *
 * @property context Konteks aplikasi atau aktivitas yang dibutuhkan untuk mengakses SharedPreferences.
 */
class UserPreference(context: Context) {

    // Inisialisasi SharedPreferences dengan nama file penyimpanan "user_session_pref"
    // dan mode ACCESS_PRIVATE (hanya aplikasi ini sendiri yang bisa membaca data di dalamnya).
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Companion Object berfungsi untuk mendeklarasikan konstanta kunci (Key) yang bersifat statis
     * agar nama variabel kunci tidak salah ketik (typo) saat dipanggil di dalam fungsi-fungsi di bawah.
     */
    companion object {
        private const val PREF_NAME = "user_session_pref"
        private const val KEY_TOKEN =
            "token"           // Kunci untuk menyimpan Token Autentikasi (Bearer Token)
        private const val KEY_USERNAME =
            "username"     // Kunci untuk menyimpan Nama / Username pengguna[cite: 10]
        private const val KEY_ROLE =
            "role"             // Kunci untuk menyimpan Hak Akses (admin / user)
        private const val KEY_IS_LOGGED_IN =
            "is_logged_in" // Kunci untuk status penanda apakah user sudah login atau belum
    }

    /**
     * Fungsi untuk menyimpan data sesi utama ke SharedPreferences setelah pengguna berhasil Login atau Register[cite: 10].
     *
     * @param token Token akses dari server[cite: 10]
     * @param username Nama akun pengguna[cite: 10]
     * @param role Hak akses pengguna (misal: "admin" atau "user")[cite: 10]
     */
    fun saveSession(token: String, username: String, role: String) {
        // prefs.edit() digunakan untuk membuka mode penyuntingan data SharedPreferences.
        // apply() digunakan untuk menyimpan perubahan secara asinkron (di background thread) agar tidak membuat UI macet.
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USERNAME, username)
            putString(KEY_ROLE, role)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    /**
     * Fungsi untuk mengambil Token Autentikasi yang tersimpan di perangkat[cite: 10].
     * @return Mengembalikan String Token atau `null` jika token belum ada/kosong[cite: 10].
     */
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    /**
     * Fungsi untuk mengambil Username pengguna yang sedang aktif saat ini[cite: 10].
     * @return Mengembalikan String Username atau `null` jika data belum tersimpan[cite: 10].
     */
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    /**
     * Fungsi untuk mengambil data Hak Akses (Role) pengguna[cite: 10].
     * @return Mengembalikan String Role, dengan nilai default "user" jika data kosong[cite: 10].
     */
    fun getRole(): String? = prefs.getString(KEY_ROLE, "user")

    /**
     * Fungsi untuk memeriksa apakah pengguna sedang dalam status login[cite: 10].
     * @return Mengembalikan `true` jika sudah login, atau `false` jika belum[cite: 10].
     */
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    /**
     * Fungsi untuk menghapus seluruh data sesi di dalam SharedPreferences (proses Logout)[cite: 10].
     * Perintah `.clear()` akan menghapus semua kunci dan nilai yang tersimpan, sehingga aplikasi
     * kembali ke kondisi bersih seperti saat pertama kali diinstal.
     */
    fun logout() {
        prefs.edit().clear().apply()
    }

    /**
     * Fungsi khusus untuk memperbarui (update) nilai username saja secara parsial di SharedPreferences.
     * Digunakan setelah pengguna berhasil mengubah profil mereka di halaman EditProfileActivity,
     * agar perubahan nama langsung tersimpan secara lokal tanpa harus menghapus token atau status login[cite: 10].
     *
     * @param newUsername Username baru yang berhasil diperbarui dari server.
     */
    fun updateUsername(newUsername: String) {
        prefs.edit().putString(KEY_USERNAME, newUsername).apply()
    }
}