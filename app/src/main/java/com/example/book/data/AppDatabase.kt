// AppDatabase.kt
package com.example.book.data

import android.content.Context
import android.util.Log
import androidx.room.*
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.book.dao.UserBookDao
import com.example.book.model.BookCollection
import com.example.book.model.StringListConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserBook::class, BookCollection::class, AppSettings::class],
    version = 3
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userBookDao(): UserBookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DATABASE_NAME = "user_books_db"
        private const val TAG = "DATABASE_DEBUG"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d(TAG, "База данных СОЗДАНА: $DATABASE_NAME")

                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.userBookDao()?.insertAppSettings(AppSettings())
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d(TAG, "База данных ОТКРЫТА: $DATABASE_NAME")
                        }
                    })
                    .build()

                val dbPath = context.getDatabasePath(DATABASE_NAME)
                Log.d(TAG, "Путь к базе данных: ${dbPath.absolutePath}")
                Log.d(TAG, "Файл существует: ${dbPath.exists()}")
                Log.d(TAG, "Размер файла: ${if (dbPath.exists()) dbPath.length() else 0} байт")

                INSTANCE = instance
                instance
            }
        }
    }
}