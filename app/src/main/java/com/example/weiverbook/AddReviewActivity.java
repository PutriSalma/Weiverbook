package com.example.weiverbook;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddReviewActivity extends AppCompatActivity {
    public static final String EXTRA_BOOK_ID_FOR_REVIEW = "extra_book_id_for_review";
    private DatabaseHelper dbHelper;
    private int bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);
        setTitle("Tambah/Edit Review");

        dbHelper = new DatabaseHelper(this);
        RatingBar ratingBar = findViewById(R.id.rb_book_rating);
        EditText etReview = findViewById(R.id.et_review_text);
        Button btnSave = findViewById(R.id.btn_save_review);

        bookId = getIntent().getIntExtra(EXTRA_BOOK_ID_FOR_REVIEW, -1);
        if (bookId == -1) {
            Toast.makeText(this, "Error: ID Buku tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSave.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String reviewText = etReview.getText().toString().trim();
            int userId = SessionManager.getLoggedInUserId();

            if (rating == 0) {
                Toast.makeText(this, "Rating tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!SessionManager.isLoggedIn()) {
                Toast.makeText(this, "Error: Sesi login tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.addOrUpdateReview(bookId, userId, reviewText, rating);
            Toast.makeText(this, "Review berhasil disimpan", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}