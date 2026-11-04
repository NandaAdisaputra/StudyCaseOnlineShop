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
 * ProductAdapter adalah Adapter RecyclerView yang menggunakan ListAdapter dan DiffUtil
 * untuk menampilkan daftar produk secara efisien dan dinamis.
 */
class ProductAdapter(private val onItemClick: (ProductItem) -> Unit) :
    ListAdapter<ProductItem, ProductAdapter.ProductViewHolder>(DIFF_CALLBACK) {

    /**
     * ViewHolder bertugas menyimpan referensi view dari item_product.xml
     * dan mengikat data produk (Binding) ke dalam komponen UI.
     */
    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductItem) {
            // Menampilkan teks judul dan harga produk
            binding.tvProductTitle.text = product.nama_product
            binding.tvProductPrice.text = "Rp ${product.harga}"

            // 1. Tampilkan ProgressBar terlebih dahulu sebelum gambar mulai diunduh dari server
            binding.progressBarImage.visibility = View.VISIBLE

            // 2. Memuat gambar menggunakan Glide dengan tambahan RequestListener
            // agar ProgressBar bisa disembunyikan saat gambar selesai dimuat.
            Glide.with(itemView.context)
                .load(product.gambar)
                .placeholder(android.R.drawable.ic_menu_gallery) // Gambar sementara saat proses loading
                .error(android.R.drawable.ic_menu_close_clear_cancel) // Gambar alternatif jika gagal dimuat
                .centerCrop() // Menyesuaikan potongan gambar agar rapi di ImageView
                .listener(object : RequestListener<Drawable> {

                    // Dipanggil jika terjadi kesalahan / gagal saat memuat gambar dari server
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Sembunyikan ProgressBar agar tidak berputar terus saat error
                        binding.progressBarImage.visibility = View.GONE
                        return false // Beritahu Glide untuk tetap memproses error placeholder
                    }

                    // Dipanggil ketika gambar berhasil diunduh dan siap ditampilkan
                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Sembunyikan ProgressBar karena gambar sudah sukses tampil
                        binding.progressBarImage.visibility = View.GONE
                        return false // Beritahu Glide untuk melanjutkan menampilkan resource ke ImageView
                    }
                })
                .into(binding.ivProductImage)

            // Event klik pada item produk (misal untuk menuju ke halaman detail)
            itemView.setOnClickListener {
                onItemClick(product)
            }
        }
    }

    /**
     * Dipanggil saat RecyclerView membutuhkan ViewHolder baru untuk layout item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    /**
     * Dipanggil untuk menghubungkan data dari posisi tertentu ke ViewHolder.
     */
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        /**
         * DIFF_CALLBACK digunakan oleh ListAdapter untuk membandingkan item lama dan item baru secara efisien,
         * sehingga RecyclerView hanya melakukan render ulang pada data yang benar-benar berubah.
         */
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ProductItem>() {
            // Memeriksa apakah item memiliki ID yang sama
            override fun areItemsTheSame(oldItem: ProductItem, newItem: ProductItem): Boolean {
                return oldItem.id == newItem.id
            }

            // Memeriksa apakah seluruh isi konten dari item sama
            override fun areContentsTheSame(oldItem: ProductItem, newItem: ProductItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}