package com.example.weiverbook;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvBooks;
    private BookAdapter bookAdapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Terapkan Splash Screen API (Harus sebelum super.onCreate)
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // 2. Cek Status Login SEBELUM menampilkan layout
        if (!SessionManager.isLoggedIn()) {
            // Jika belum login, alihkan ke LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Tutup MainActivity agar tidak bisa kembali dengan tombol back
            return;   // Hentikan eksekusi onCreate lebih lanjut
        }

        // 3. Jika sudah login, baru lanjutkan untuk menampilkan layout dan data
        setContentView(R.layout.activity_main);

        // Inisialisasi DatabaseHelper dan Views
        dbHelper = new DatabaseHelper(this);
        rvBooks = findViewById(R.id.rv_books);
        FloatingActionButton fab = findViewById(R.id.fab_add_book);

        // Tambahkan data buku awal hanya jika database masih kosong
        addDummyBooksIfNeeded();

        // Atur listener untuk tombol tambah buku (Floating Action Button)
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddBookActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Jika pengguna tidak login (misalnya setelah logout dari halaman lain),
        // jangan lakukan apa-apa. Pemeriksaan di onCreate sudah cukup.
        if (!SessionManager.isLoggedIn()) {
            return;
        }
        // Muat ulang data setiap kali kembali ke activity ini
        // (misalnya setelah selesai menambah atau mengedit buku)
        loadBooksFromDatabase();
    }

    private void loadBooksFromDatabase() {
        // Ambil semua data buku dari database
        List<Book> bookList = dbHelper.getAllBooks();

        // Siapkan adapter dan tampilkan di RecyclerView
        bookAdapter = new BookAdapter(this, bookList);
        rvBooks.setLayoutManager(new GridLayoutManager(this, 2)); // Tampilan 2 kolom
        rvBooks.setAdapter(bookAdapter);
    }

    private void addDummyBooksIfNeeded() {
        // Cek jika tabel buku masih kosong, baru tambahkan data dummy
        if (dbHelper.getBooksCount() == 0) {
            dbHelper.addBook("Laskar Pelangi", "Andrea Hirata", "Kisah inspiratif anak-anak Belitung yang berjuang untuk pendidikan di tengah keterbatasan.", "laskar_pelangi");
            dbHelper.addBook("Bumi Manusia", "Pramoedya Ananta Toer", "Sebuah roman epik yang berlatar belakang zaman kolonial Belanda di Hindia Belanda.", "bumi_manusia");
            dbHelper.addBook("Pulang", "Tere Liye", "Sebuah kisah tentang perjalanan pulang seorang pembunuh bayaran yang mencari penebusan.", "pulang");
        }
    }
}