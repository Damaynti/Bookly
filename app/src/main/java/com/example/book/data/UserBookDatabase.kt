package com.example.book.data

import android.content.Context
import androidx.room.Database
import androidx.room.*
import androidx.room.RoomDatabase

@Database(entities = [UserBook::class], version = 1)
abstract class UserBookDatabase : RoomDatabase() {
    abstract fun userBookDao(): UserBookDao

    companion object {
        @Volatile
        private var INSTANCE: UserBookDatabase? = null

        fun getInstance(context: Context): UserBookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserBookDatabase::class.java,
                    "user_books_db"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
