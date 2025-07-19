package com.example.weiverbook;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddBookActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID_TO_EDIT = "extra_book_id_to_edit";

    private EditText etTitle, etAuthor, etSynopsis;
    private ImageView ivCoverPreview;
    private Button btnSelectImage, btnSaveBook;
    private DatabaseHelper dbHelper;
    private Uri selectedImageUri = null;
    private String currentImagePath = null;
    private int bookIdToEdit = -1;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Menggunakan Photo Picker modern dari Android
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).into(ivCoverPreview);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        // Inisialisasi Views dan DatabaseHelper
        dbHelper = new DatabaseHelper(this);
        etTitle = findViewById(R.id.et_add_title);
        etAuthor = findViewById(R.id.et_add_author);
        etSynopsis = findViewById(R.id.et_add_synopsis);
        ivCoverPreview = findViewById(R.id.iv_add_book_cover_preview);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnSaveBook = findViewById(R.id.btn_save_book);

        // Cek apakah ini mode edit atau tambah baru
        if (getIntent().hasExtra(EXTRA_BOOK_ID_TO_EDIT)) {
            bookIdToEdit = getIntent().getIntExtra(EXTRA_BOOK_ID_TO_EDIT, -1);
            setTitle("Edit Buku");
            loadBookData();
        } else {
            setTitle("Tambah Buku Baru");
        }

        btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSaveBook.setOnClickListener(v -> saveBook());
    }

    private void loadBookData() {
        if (bookIdToEdit != -1) {
            // Operasi database di background
            executor.execute(() -> {
                Book book = dbHelper.getBook(bookIdToEdit);
                if (book != null) {
                    // Update UI di thread utama
                    runOnUiThread(() -> {
                        etTitle.setText(book.getTitle());
                        etAuthor.setText(book.getAuthor());
                        etSynopsis.setText(book.getSynopsis());
                        currentImagePath = book.getImageName();

                        if (currentImagePath != null && !currentImagePath.isEmpty()) {
                            // Cek apakah gambar dari drawable atau file path
                            if (currentImagePath.contains("/")) {
                                Glide.with(AddBookActivity.this).load(new File(currentImagePath)).into(ivCoverPreview);
                            } else {
                                int resId = getResources().getIdentifier(currentImagePath, "drawable", getPackageName());
                                Glide.with(AddBookActivity.this).load(resId).into(ivCoverPreview);
                            }
                        }
                    });
                }
            });
        }
    }

    private void saveBook() {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String synopsis = etSynopsis.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty() || synopsis.isEmpty()) {
            Toast.makeText(this, "Semua kolom teks harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Jalankan semua operasi file dan database di background
        executor.execute(() -> {
            String finalImagePath = currentImagePath;

            // Jika user memilih gambar baru, salin file-nya
            if (selectedImageUri != null) {
                finalImagePath = copyImageToInternalStorage(selectedImageUri);
            }

            // Validasi gambar
            if (finalImagePath == null && bookIdToEdit == -1) {
                runOnUiThread(() -> Toast.makeText(this, "Silakan pilih gambar sampul", Toast.LENGTH_SHORT).show());
                return;
            }

            // Simpan ke database
            if (bookIdToEdit != -1) {
                dbHelper.updateBook(bookIdToEdit, title, author, synopsis, finalImagePath);
                runOnUiThread(() -> Toast.makeText(this, "Buku berhasil diperbarui", Toast.LENGTH_SHORT).show());
            } else {
                dbHelper.addBook(title, author, synopsis, finalImagePath);
                runOnUiThread(() -> Toast.makeText(this, "Buku berhasil ditambahkan", Toast.LENGTH_SHORT).show());
            }

            // Selesai dan kembali ke halaman sebelumnya
            finish();
        });
    }

    // CARA BARU YANG LEBIH AMAN UNTUK MENYIMPAN GAMBAR
    private String copyImageToInternalStorage(Uri uri) {
        // Buat nama file unik (bisa pakai timestamp atau UUID)
        String fileName = "cover_" + System.currentTimeMillis() + ".jpg";
        File directory = getDir("images", Context.MODE_PRIVATE);
        File file = new File(directory, fileName);

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(file)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            // Tampilkan pesan error di UI thread
            runOnUiThread(() -> Toast.makeText(this, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show());
            return null;
        }
    }
}