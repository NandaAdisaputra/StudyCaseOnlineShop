package com.nandaadisaputra.onlineshop.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.nandaadisaputra.onlineshop.data.model.ProductItem
import com.nandaadisaputra.onlineshop.databinding.ItemProductBinding

/**
 * ProductAdapter adalah kelas penghubung (Adapter) RecyclerView yang menggunakan [ListAdapter]
 * dan [DiffUtil] untuk menampilkan daftar produk secara efisien, dinamis, dan berperforma tinggi.
 *
 * @property onItemClick Fungsi Higher-Order (Lambda) yang akan dipanggil ketika salah satu item produk diklik.
 */
class ProductAdapter(private val onItemClick: (ProductItem) -> Unit) :
    ListAdapter<ProductItem, ProductAdapter.ProductViewHolder>(DIFF_CALLBACK) {

    /**
     * Kelas dalam (Inner Class) ProductViewHolder bertugas memegang referensi komponen UI
     * dari `item_product.xml` melalui ViewBinding dan mengikat (bind) data produk ke tampilan.
     */
    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Fungsi untuk memasukkan data dari objek [ProductItem] ke dalam komponen UI (TextView & ImageView).
         */
        fun bind(product: ProductItem) {
            // Menggunakan scope function 'apply' agar penulisan kode lebih bersih tanpa menulis 'binding.' berulang kali
            binding.apply {
                // 1. Menampilkan teks nama produk dan harga ke komponen TextView
                tvProductTitle.text = product.nama_product
                tvProductPrice.text = "Rp ${product.harga}"

                // 2. Menampilkan ProgressBar kecil (progress_bar_image) sebelum proses unduh gambar dimulai
                progressBarImage.visibility = View.VISIBLE

                // 3. Memuat gambar produk dari URL server menggunakan library Glide
                Glide.with(itemView.context)
                    .load(product.gambar)
                    .placeholder(android.R.drawable.ic_menu_gallery)             // Gambar sementara saat proses unduh berlangsung
                    .error(android.R.drawable.ic_menu_close_clear_cancel)         // Gambar alternatif jika URL rusak/gagal dimuat
                    .centerCrop()                                                 // Menyesuaikan potongan gambar agar proporsional di ImageView
                    .listener(object : RequestListener<Drawable> {

                        // Dipanggil jika terjadi kesalahan / gagal saat mengunduh gambar dari server
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            // Sembunyikan ProgressBar agar tidak berputar terus menerus saat terjadi error
                            progressBarImage.visibility = View.GONE
                            return false // Kembalikan false agar Glide tetap memproses error placeholder di atas
                        }

                        // Dipanggil ketika gambar berhasil diunduh sepenuhnya dan siap dirender ke layar
                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            // Sembunyikan ProgressBar karena gambar sudah sukses tampil
                            progressBarImage.visibility = View.GONE
                            return false // Kembalikan false agar Glide melanjutkan menampilkan gambar ke ivProductImage
                        }
                    })
                    .into(ivProductImage)

                // 4. Menangani aksi klik pada kartu item produk (mengirim data produk ke Fragment melalui lambda)
                itemView.setOnClickListener {
                    onItemClick(product)
                }
            }
        }
    }

    /**
     * Fungsi wajib RecyclerView 1: Dipanggil saat RecyclerView membutuhkan wadah tampilan baru (ViewHolder).
     * Di sinilah file layout XML (`item_product.xml`) diubah (inflate) menjadi objek ViewBinding.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    /**
     * Fungsi wajib RecyclerView 2: Dipanggil untuk menghubungkan data dari posisi baris tertentu
     * ke dalam ViewHolder.
     */
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        /**
         * DIFF_CALLBACK adalah algoritma pembanding yang digunakan oleh [ListAdapter] untuk mengevaluasi
         * perbedaan antara daftar item lama dan daftar item baru secara otomatis di background thread.
         * Dengan DiffUtil, RecyclerView tidak merender ulang seluruh daftar, melainkan hanya memperbarui
         * item yang benar-benar mengalami perubahan (sangat efisien untuk performa aplikasi).
         */
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ProductItem>() {
            // Memeriksa apakah dua item memiliki ID unik yang sama
            override fun areItemsTheSame(oldItem: ProductItem, newItem: ProductItem): Boolean {
                return oldItem.id == newItem.id
            }

            // Memeriksa apakah seluruh isi konten/properti dari item tersebut sama persis
            override fun areContentsTheSame(oldItem: ProductItem, newItem: ProductItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}