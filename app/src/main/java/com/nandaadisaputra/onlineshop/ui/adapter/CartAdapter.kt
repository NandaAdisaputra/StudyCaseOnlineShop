package com.nandaadisaputra.onlineshop.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.nandaadisaputra.onlineshop.data.model.CartItem
import com.nandaadisaputra.onlineshop.databinding.ItemProductBinding
import java.text.NumberFormat
import java.util.Locale

/**
 * CartAdapter adalah kelas penghubung (Adapter) antara data daftar keranjang ([CartItem])
 * dengan tampilan daftar bergulir ([RecyclerView]) di CartFragment.
 *
 * Kelas ini mewarisi [RecyclerView.Adapter] yang bertugas mendaur ulang (recycle) tampilan item
 * yang keluar dari layar agar penggunaan memori tetap hemat dan guliran layar tetap mulus.
 *
 * @property onItemClick Fungsi callback (Higher-Order Function / Lambda) opsional yang akan dipanggil
 * ketika salah satu kartu produk di dalam daftar diklik oleh pengguna.
 */
class CartAdapter(
    private val onItemClick: ((CartItem) -> Unit)? = null
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // 1. Variabel penampung data daftar item keranjang di dalam memori Adapter.
    // Menggunakan ArrayList agar datanya mudah dibersihkan (clear) dan diperbarui (addAll).
    private val cartItems = ArrayList<CartItem>()

    /**
     * Fungsi untuk memperbarui seluruh daftar data di dalam Adapter ketika data baru
     * selesai diambil dari API melalui ViewModel.
     *
     * @param items Daftar item keranjang terbaru dari server[cite: 15].
     */
    fun submitList(items: List<CartItem>) {
        cartItems.clear()          // Hapus data lama agar tidak terjadi duplikasi item
        cartItems.addAll(items)    // Masukkan seluruh data baru ke dalam list
        notifyDataSetChanged()     // Beri tahu RecyclerView untuk menggambar ulang (refresh) layar
    }

    /**
     * Fungsi wajib 1: Dipanggil oleh RecyclerView ketika membutuhkan "wadah" tampilan baru (ViewHolder).
     * Di sinilah file layout XML (`item_product.xml`) diubah (di-inflate) menjadi objek ViewBinding.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false // false karena RecyclerView sendiri yang akan mengatur kapan view ini ditempelkan ke parent
        )
        return CartViewHolder(binding)
    }

    /**
     * Fungsi wajib 2: Dipanggil oleh RecyclerView untuk menampilkan data pada posisi baris tertentu.
     * Mengambil objek [CartItem] berdasarkan indeks `position` lalu mengirimkannya ke fungsi `bind()`.
     */
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    /**
     * Fungsi wajib 3: Memberi tahu RecyclerView berapa total jumlah baris item yang harus ditampilkan.
     */
    override fun getItemCount(): Int = cartItems.size

    /**
     * Kelas dalam (Inner Class) CartViewHolder bertugas memegang referensi komponen UI pada setiap baris item
     * melalui [ItemProductBinding] (`item_product.xml`) dan mengikat (bind) data ke komponen tersebut.
     */
    inner class CartViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Fungsi untuk memasukkan data dari objek [CartItem] ke dalam masing-masing komponen View (TextView & ImageView).
         */
        fun bind(item: CartItem) {
            // Menggunakan scope function 'apply' agar tidak perlu menulis 'binding.' berulang kali
            binding.apply {
                // 1. Menampilkan Nama Produk ke tv_product_title[cite: 15].
                // Elvis operator (?:) memberikan tanda "-" jika nama_product dari server bernilai null.
                tvProductTitle.text = item.nama_product ?: "-"

                // 2. Memformat angka harga (contoh: 450000 menjadi "450.000") menggunakan Locale Indonesia ("id", "ID")
                // agar sesuai dengan desain UI dan lebih mudah dibaca oleh pengguna[cite: 15, 16].
                val formattedPrice = NumberFormat.getNumberInstance(Locale("id", "ID"))
                    .format(item.harga ?: 0)
                tvProductPrice.text = formattedPrice

                // 3. Menampilkan ProgressBar kecil (progress_bar_image) tepat di tengah kotak gambar
                // sebagai indikator bahwa gambar sedang diunduh dari URL server.
                progressBarImage.visibility = View.VISIBLE

                // 4. Memuat gambar produk dari URL (item.gambar) menggunakan library Glide[cite: 15]
                Glide.with(itemView.context)
                    .load(item.gambar)
                    // Menambahkan RequestListener untuk memantau kapan proses unduh gambar selesai atau gagal
                    .listener(object : RequestListener<Drawable> {

                        // Dipanggil jika gambar GAGAL dimuat (misal: URL rusak atau internet terputus)
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            // Sembunyikan ProgressBar gambar karena proses muat sudah berakhir
                            progressBarImage.visibility = View.GONE
                            // Return false agar Glide tetap melanjutkan menampilkan gambar '.error(...)' di bawah
                            return false
                        }

                        // Dipanggil jika gambar BERHASIL diunduh dan siap ditampilkan
                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            // Sembunyikan ProgressBar gambar karena gambar sudah siap tampil
                            progressBarImage.visibility = View.GONE
                            // Return false agar Glide tetap menampilkan gambar hasil unduhan ke ivProductImage
                            return false
                        }
                    })
                    // Gambar cadangan (fallback) jika URL gambar gagal dimuat
                    .error(android.R.drawable.ic_menu_close_clear_cancel)
                    // Target ImageView tempat gambar ditampilkan (iv_product_image)
                    .into(ivProductImage)

                // 5. Menangani aksi klik pada keseluruhan kartu item (root CardView).
                // Memanggil fungsi lambda 'onItemClick' dan mengirimkan data 'item' yang sedang diklik ke CartFragment.
                root.setOnClickListener {
                    onItemClick?.invoke(item)
                }
            }
        }
    }
}