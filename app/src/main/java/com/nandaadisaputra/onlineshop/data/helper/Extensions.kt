package com.nandaadisaputra.onlineshop.data.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nandaadisaputra.onlineshop.ui.auth.LoginActivity
import com.nandaadisaputra.onlineshop.ui.home.AdminDashboardActivity
import com.nandaadisaputra.onlineshop.ui.home.MainActivity
import java.io.File
import java.io.FileOutputStream

// Extension function agar pemanggilan Toast jauh lebih singkat di Activity/Fragment
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Extension function untuk menavigasikan Context/Activity ke LoginActivity
 * dan membersihkan tumpukan activity (activity stack).
 */
fun Context.navigateToLogin(finishCurrent: Boolean = true) {
    val intent = Intent(this, LoginActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    startActivity(intent)

    // Jika dipanggil dari Activity, kita bisa langsung menyelesaikan (finish) activity saat ini
    if (finishCurrent && this is Activity) {
        finish()
    }
}

/**
 * Generic Extension function untuk bernavigasi ke Activity mana saja.
 * Default finishCurrent = false karena berpindah halaman biasa tidak perlu menutup activity asal.
 */
inline fun <reified T : Activity> Context.navigateToActivity(
    finishCurrent: Boolean = false,
    noinline block: (Intent.() -> Unit)? = null
) {
    val intent = Intent(this, T::class.java).apply {
        block?.invoke(this)
    }
    startActivity(intent)
    if (finishCurrent && this is Activity) {
        finish()
    }
}

/**
 * Extension function untuk navigasi berbasis role setelah Login/Auth.
 * Otomatis menentukan tujuan Activity dan menutup activity saat ini (finish).
 */
fun Context.navigateToDashboard(role: String, finishCurrent: Boolean = true) {
    val destination = if (role.equals("admin", ignoreCase = true)) {
        AdminDashboardActivity::class.java
    } else {
        MainActivity::class.java
    }

    val intent = Intent(this, destination).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }

    startActivity(intent)

    if (finishCurrent && this is Activity) {
        finish()
    }
}

/**
 * Extension function untuk ProgressBar.
 * Otomatis mengatur visibility ProgressBar dan mengubah status enabled pada view-view lain (misal: Tombol, EditText).
 */
fun ProgressBar.setLoading(isLoading: Boolean, vararg viewsToDisable: View) {
    // Mengatur visibility ProgressBar
    visibility = if (isLoading) View.VISIBLE else View.GONE

    // Mengubah status enabled untuk semua view yang ikut dimasukkan
    viewsToDisable.forEach { view ->
        view.isEnabled = !isLoading
    }
}

/**
 * Extension function untuk ProgressBar pada halaman Register.
 * Mengatur visibilitas ProgressBar sekaligus menonaktifkan/mengaktifkan tombol Register (dan view lain jika ada).
 */
fun ProgressBar.setLoadingRegister(isLoading: Boolean, vararg viewsToDisable: View) {
    visibility = if (isLoading) View.VISIBLE else View.GONE
    viewsToDisable.forEach { view ->
        view.isEnabled = !isLoading
    }
}

/**
 * Extension function untuk melakukan Logout dan membersihkan seluruh Activity Stack.
 * Dapat dipanggil dari Context/Activity maupun Fragment.
 */
fun Context.navigateToLoginOnLogout() {
    val intent = Intent(this, LoginActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
    if (this is Activity) {
        finish()
    }
}

/**
 * Extension function untuk AppCompatActivity guna memuat/mengganti Fragment dengan mudah.
 */
fun AppCompatActivity.loadFragment(containerId: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .replace(containerId, fragment)
        .commit()
}

fun Uri.toFile(context: Context): File {
    // 1. Mendapatkan objek ContentResolver dari context untuk membaca data biner (stream) dari Uri
    val contentResolver = context.contentResolver

    // 2. Membuat file kosong sementara di dalam direktori cache internal aplikasi (context.cacheDir)
    // dengan awalan nama file "prod_upload_" dan berformat ekstensi ".jpg".
    val myFile = File.createTempFile("prod_upload_", ".jpg", context.cacheDir)

    // 3. Membuka InputStream dari Uri menggunakan safe call (?.) dan fungsi .use {}
    // agar aliran data (stream) otomatis tertutup setelah proses pembacaan selesai (mencegah kebocoran memori).
    contentResolver.openInputStream(this)?.use { inputStream ->

        // 4. Membuka FileOutputStream ke file sementara yang sudah dibuat sebelumnya (.use {} juga memastikan stream tertutup otomatis).
        FileOutputStream(myFile).use { outputStream ->

            // 5. Menyalin seluruh isi data biner dari InputStream (gambar asal) ke FileOutputStream (file sementara).
            inputStream.copyTo(outputStream)
        }
    }

    // 6. Mengembalikan objek File yang sekarang sudah berisi data gambar fisik dan siap dikirim ke server.
    return myFile
}