package com.example.weiverbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Weiverbook.db";
    private static final int DATABASE_VERSION = 2;

    // Tabel Users
    private static final String TABLE_USERS = "users";
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    // Tabel Books
    private static final String TABLE_BOOKS = "books";
    private static final String KEY_BOOK_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_SYNOPSIS = "synopsis";
    private static final String KEY_IMAGE_NAME = "image_name";

    // Tabel Reviews
    private static final String TABLE_REVIEWS = "reviews";
    private static final String KEY_REVIEW_ID = "id";
    private static final String KEY_BOOK_ID_FK = "book_id";
    private static final String KEY_USER_ID_FK = "user_id";
    private static final String KEY_REVIEW_TEXT = "review";
    private static final String KEY_RATING = "rating";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tambahkan UNIQUE pada kolom username untuk mencegah duplikat
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY,"
                + KEY_USERNAME + " TEXT UNIQUE,"
                + KEY_PASSWORD + " TEXT" + ")";

        String CREATE_BOOKS_TABLE = "CREATE TABLE " + TABLE_BOOKS + "("
                + KEY_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITLE + " TEXT,"
                + KEY_AUTHOR + " TEXT,"
                + KEY_SYNOPSIS + " TEXT,"
                + KEY_IMAGE_NAME + " TEXT" + ")";

        String CREATE_REVIEWS_TABLE = "CREATE TABLE " + TABLE_REVIEWS + "("
                + KEY_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_BOOK_ID_FK + " INTEGER,"
                + KEY_USER_ID_FK + " INTEGER,"
                + KEY_REVIEW_TEXT + " TEXT,"
                + KEY_RATING + " REAL,"
                + "FOREIGN KEY(" + KEY_BOOK_ID_FK + ") REFERENCES " + TABLE_BOOKS + "(" + KEY_BOOK_ID + "),"
                + "FOREIGN KEY(" + KEY_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")"
                + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_BOOKS_TABLE);
        db.execSQL(CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Metode ini akan menghapus semua data jika versi database berubah.
        // Hati-hati jika aplikasi sudah punya data pengguna asli.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // =================================================================================
    // FUNGSI UNTUK USER
    // =================================================================================

    /**
     * Menambahkan user baru ke database.
     * Menggunakan insertWithOnConflict untuk mencegah duplikasi username secara efisien.
     */
    public void addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, username);
        values.put(KEY_PASSWORD, password);

        // Menggunakan CONFLICT_IGNORE: jika username sudah ada, perintah insert akan diabaikan.
        db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    /**
     * Memeriksa apakah username dan password cocok untuk login.
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {KEY_USER_ID};
        String selection = KEY_USERNAME + " = ?" + " AND " + KEY_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

    /**
     * (METODE BARU) Memeriksa apakah sebuah username sudah ada di database.
     * Dibutuhkan untuk halaman registrasi.
     */
    public boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {KEY_USER_ID};
        String selection = KEY_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_USER_ID}, KEY_USERNAME + "=?", new String[]{username}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID));
            cursor.close();
            db.close();
            return userId;
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return -1; // Mengembalikan -1 jika user tidak ditemukan
    }

    // =================================================================================
    // FUNGSI UNTUK BUKU
    // =================================================================================

    public void addBook(String title, String author, String synopsis, String imageName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title);
        values.put(KEY_AUTHOR, author);
        values.put(KEY_SYNOPSIS, synopsis);
        values.put(KEY_IMAGE_NAME, imageName);
        db.insert(TABLE_BOOKS, null, values);
        db.close();
    }

    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<>();
        String selectQuery = "SELECT b." + KEY_BOOK_ID + ", b." + KEY_TITLE + ", b." + KEY_AUTHOR + ", b." + KEY_SYNOPSIS + ", b." + KEY_IMAGE_NAME + ", AVG(r." + KEY_RATING + ") as avg_rating " +
                "FROM " + TABLE_BOOKS + " b LEFT JOIN " + TABLE_REVIEWS + " r ON b." + KEY_BOOK_ID + " = r." + KEY_BOOK_ID_FK + " " +
                "GROUP BY b." + KEY_BOOK_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BOOK_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE));
                String author = cursor.getString(cursor.getColumnIndexOrThrow(KEY_AUTHOR));
                String synopsis = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SYNOPSIS));
                String imageName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_NAME));
                float avgRating = cursor.getFloat(cursor.getColumnIndexOrThrow("avg_rating"));

                Book book = new Book(id, title, author, synopsis, imageName, avgRating);
                bookList.add(book);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return bookList;
    }

    public int getBooksCount() {
        String countQuery = "SELECT * FROM " + TABLE_BOOKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public Book getBook(int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKS, null, KEY_BOOK_ID + "=?", new String[]{String.valueOf(bookId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BOOK_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(KEY_AUTHOR));
            String synopsis = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SYNOPSIS));
            String imageName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_NAME));
            cursor.close();
            db.close();
            // Mengembalikan rating 0 karena fungsi ini tidak menghitung rata-rata rating
            return new Book(id, title, author, synopsis, imageName, 0);
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return null;
    }

    public int updateBook(int id, String title, String author, String synopsis, String imageName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title);
        values.put(KEY_AUTHOR, author);
        values.put(KEY_SYNOPSIS, synopsis);
        values.put(KEY_IMAGE_NAME, imageName);
        int rowsAffected = db.update(TABLE_BOOKS, values, KEY_BOOK_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected;
    }

    public void deleteBook(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Hapus dulu review yang terkait dengan buku
        db.delete(TABLE_REVIEWS, KEY_BOOK_ID_FK + " = ?", new String[]{String.valueOf(id)});
        // Baru hapus bukunya
        db.delete(TABLE_BOOKS, KEY_BOOK_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // =================================================================================
    // FUNGSI UNTUK REVIEW
    // =================================================================================

    public void addOrUpdateReview(int bookId, int userId, String reviewText, float rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_REVIEWS, new String[]{KEY_REVIEW_ID}, KEY_BOOK_ID_FK + "=? AND " + KEY_USER_ID_FK + "=?", new String[]{String.valueOf(bookId), String.valueOf(userId)}, null, null, null);

        ContentValues values = new ContentValues();
        values.put(KEY_BOOK_ID_FK, bookId);
        values.put(KEY_USER_ID_FK, userId);
        values.put(KEY_REVIEW_TEXT, reviewText);
        values.put(KEY_RATING, rating);

        if (cursor != null && cursor.moveToFirst()) {
            int reviewId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_REVIEW_ID));
            db.update(TABLE_REVIEWS, values, KEY_REVIEW_ID + "=?", new String[]{String.valueOf(reviewId)});
            Log.d("DatabaseHelper", "Review updated");
        } else {
            db.insert(TABLE_REVIEWS, null, values);
            Log.d("DatabaseHelper", "New review added");
        }

        if(cursor != null) {
            cursor.close();
        }
        db.close();
    }

    public List<Review> getReviewsForBook(int bookId) {
        List<Review> reviewList = new ArrayList<>();
        String selectQuery = "SELECT r." + KEY_REVIEW_ID + ", r." + KEY_REVIEW_TEXT + ", r." + KEY_RATING + ", u." + KEY_USERNAME +
                " FROM " + TABLE_REVIEWS + " r, " + TABLE_USERS + " u " +
                " WHERE r." + KEY_USER_ID_FK + " = u." + KEY_USER_ID +
                " AND r." + KEY_BOOK_ID_FK + " = " + bookId;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_REVIEW_ID));
                String reviewText = cursor.getString(cursor.getColumnIndexOrThrow(KEY_REVIEW_TEXT));
                float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_RATING));
                String username = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USERNAME));

                Review review = new Review(id, reviewText, rating);
                review.setUsername(username);
                reviewList.add(review);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return reviewList;
    }
}