package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ReviewItemEntity::class], version = 2, exportSchema = false)
abstract class CleanerDatabase : RoomDatabase() {
    abstract fun reviewDao(): ReviewDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE review_items ADD COLUMN category TEXT NOT NULL DEFAULT 'all_media'")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_items_category ON review_items(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_items_decision ON review_items(decision)")
            }
        }

        @Volatile
        private var INSTANCE: CleanerDatabase? = null

        fun getInstance(context: Context): CleanerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CleanerDatabase::class.java,
                    "storage_cleaner_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
