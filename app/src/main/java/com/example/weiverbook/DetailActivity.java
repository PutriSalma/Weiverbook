package com.example.weiverbook;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "extra_book_id";
    private DatabaseHelper dbHelper;
    private Book currentBook;
    private int bookId = -1;

    private ImageView ivBookCover;
    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvSynopsis;
    private LinearLayout llReviewsContainer;

    // Gunakan ExecutorService untuk operasi database di background
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Inisialisasi semua view
        ivBookCover = findViewById(R.id.iv_detail_book_cover);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvAuthor = findViewById(R.id.tv_detail_author);
        tvSynopsis = findViewById(R.id.tv_detail_synopsis);
        llReviewsContainer = findViewById(R.id.ll_reviews_container);
        Button btnEdit = findViewById(R.id.btn_edit_book);
        Button btnDelete = findViewById(R.id.btn_delete_book);
        Button btnAddReview = findViewById(R.id.btn_add_review);

        dbHelper = new DatabaseHelper(this);

        // Ambil ID buku dari Intent
        bookId = getIntent().getIntExtra(EXTRA_BOOK_ID, -1);
        if (bookId == -1) {
            Toast.makeText(this, "Error: Buku tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup listener untuk tombol-tombol
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, AddBookActivity.class);
            intent.putExtra(AddBookActivity.EXTRA_BOOK_ID_TO_EDIT, bookId);
            startActivity(intent);
        });

        btnAddReview.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, AddReviewActivity.class);
            intent.putExtra(AddReviewActivity.EXTRA_BOOK_ID_FOR_REVIEW, bookId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Muat ulang detail buku setiap kali kembali ke halaman ini
        loadBookDetails();
    }

    private void loadBookDetails() {
        if (bookId != -1) {
            // Jalankan query database di background thread
            executor.execute(() -> {
                currentBook = dbHelper.getBook(bookId);
                List<Review> reviews = dbHelper.getReviewsForBook(bookId);

                // Kembali ke UI thread untuk memperbarui tampilan
                runOnUiThread(() -> {
                    if (currentBook != null) {
                        populateBookData(currentBook);
                        populateReviews(reviews);
                    } else {
                        Toast.makeText(this, "Gagal memuat detail buku", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    private void populateBookData(Book book) {
        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor());
        tvSynopsis.setText(book.getSynopsis());

        // =================================================================
        // == LOGIKA BARU: BISA MEMUAT GAMBAR DARI DUA SUMBER ==
        // =================================================================
        String imageIdentifier = book.getImageName();

        if (imageIdentifier != null && !imageIdentifier.isEmpty()) {
            // Cek apakah identifier adalah sebuah path file (mengandung '/')
            if (imageIdentifier.contains("/")) {
                // Ini adalah path file dari penyimpanan internal
                Glide.with(this)
                        .load(new File(imageIdentifier))
                        .placeholder(R.drawable.logo_weiverbook)
                        .error(R.drawable.logo_weiverbook)
                        .into(ivBookCover);
            } else {
                // Ini adalah nama resource dari drawable (untuk data dummy)
                int imageResourceId = this.getResources().getIdentifier(
                        imageIdentifier, "drawable", this.getPackageName());
                Glide.with(this)
                        .load(imageResourceId)
                        .placeholder(R.drawable.logo_weiverbook)
                        .error(R.drawable.logo_weiverbook)
                        .into(ivBookCover);
            }
        } else {
            // Jika tidak ada gambar sama sekali
            ivBookCover.setImageResource(R.drawable.logo_weiverbook);
        }
    }

    private void populateReviews(List<Review> reviews) {
        llReviewsContainer.removeAllViews();
        if (reviews.isEmpty()) {
            TextView noReviewText = new TextView(this);
            noReviewText.setText("Belum ada review untuk buku ini.");
            llReviewsContainer.addView(noReviewText);
        } else {
            LayoutInflater inflater = LayoutInflater.from(this);
            for (Review review : reviews) {
                View reviewView = inflater.inflate(R.layout.item_review, llReviewsContainer, false);

                TextView tvUsername = reviewView.findViewById(R.id.tv_review_username);
                RatingBar rbRating = reviewView.findViewById(R.id.rb_review_rating);
                TextView tvReviewText = reviewView.findViewById(R.id.tv_review_text);

                tvUsername.setText(review.getUsername());
                rbRating.setRating(review.getRating());

                if (review.getReviewText() != null && !review.getReviewText().isEmpty()) {
                    tvReviewText.setText(review.getReviewText());
                    tvReviewText.setVisibility(View.VISIBLE);
                } else {
                    tvReviewText.setVisibility(View.GONE);
                }
                llReviewsContainer.addView(reviewView);
            }
        }
    }

    private void showDeleteDialog() {
        if (currentBook == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Hapus Buku")
                .setMessage("Apakah Anda yakin ingin menghapus buku '" + currentBook.getTitle() + "'?")
                .setPositiveButton("Ya, Hapus", (dialog, which) -> {
                    executor.execute(() -> {
                        dbHelper.deleteBook(currentBook.getId());
                        runOnUiThread(() -> {
                            Toast.makeText(DetailActivity.this, "Buku berhasil dihapus", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .setNegativeButton("Tidak", null)
                .show();
    }
}