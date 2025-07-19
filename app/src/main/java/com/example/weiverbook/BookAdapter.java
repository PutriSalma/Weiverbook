package com.example.weiverbook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final Context context;
    private final List<Book> bookList;

    public BookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.tvBookTitle.setText(book.getTitle());
        holder.tvBookAuthor.setText(book.getAuthor());

        // =================================================================
        // == LOGIKA BARU: BISA MEMUAT GAMBAR DARI DUA SUMBER ==
        // =================================================================
        String imageIdentifier = book.getImageName();

        if (imageIdentifier != null && !imageIdentifier.isEmpty()) {
            // Cek apakah identifier adalah sebuah path file (mengandung '/')
            if (imageIdentifier.contains("/")) {
                // Ini adalah path file dari penyimpanan internal
                Glide.with(context)
                        .load(new File(imageIdentifier))
                        .placeholder(R.drawable.logo_weiverbook)
                        .error(R.drawable.logo_weiverbook)
                        .into(holder.ivBookCover);
            } else {
                // Ini adalah nama resource dari drawable (untuk data dummy)
                int imageResourceId = context.getResources().getIdentifier(
                        imageIdentifier, "drawable", context.getPackageName());
                Glide.with(context)
                        .load(imageResourceId)
                        .placeholder(R.drawable.logo_weiverbook)
                        .error(R.drawable.logo_weiverbook)
                        .into(holder.ivBookCover);
            }
        } else {
            // Jika tidak ada gambar sama sekali
            Glide.with(context).load(R.drawable.logo_weiverbook).into(holder.ivBookCover);
        }

        // ... sisa kode untuk rating dan onClickListener tetap sama ...
        float rating = book.getAverageRating();
        if (rating > 0) {
            holder.tvBookRating.setText(String.format("%.1f", rating));
            holder.tvBookRating.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            holder.tvBookRating.setTypeface(null, Typeface.NORMAL);
            holder.tvBookRating.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.btn_star_big_on, 0, 0, 0);
        } else {
            holder.tvBookRating.setText("Belum ada penilaian");
            holder.tvBookRating.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            holder.tvBookRating.setTypeface(null, Typeface.ITALIC);
            holder.tvBookRating.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_BOOK_ID, book.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover;
        TextView tvBookTitle;
        TextView tvBookAuthor;
        TextView tvBookRating;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.iv_book_cover);
            tvBookTitle = itemView.findViewById(R.id.tv_book_title);
            tvBookAuthor = itemView.findViewById(R.id.tv_book_author);
            tvBookRating = itemView.findViewById(R.id.tv_book_rating);
        }
    }
}