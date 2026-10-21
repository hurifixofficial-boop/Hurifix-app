package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CustomerEntity
import com.example.data.model.CustomerJobEntity
import com.example.data.model.ExpertCategoryEntity
import com.example.data.model.ExpertEntity
import com.example.data.model.TechnicianEntity

@Database(
    entities = [
        TechnicianEntity::class,
        CustomerEntity::class,
        ExpertEntity::class,
        CustomerJobEntity::class,
        ExpertCategoryEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun technicianDao(): TechnicianDao
    abstract fun customerDao(): CustomerDao
    abstract fun expertDao(): ExpertDao
    abstract fun customerJobDao(): CustomerJobDao
    abstract fun expertCategoryDao(): ExpertCategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val CENTRAL_DB_NAME = "hurifix_central_business.db"
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE customer_jobs ADD COLUMN customerMediaUrls TEXT")
                database.execSQL("ALTER TABLE customer_jobs ADD COLUMN technicianBeforePhotoUrl TEXT")
                database.execSQL("ALTER TABLE customer_jobs ADD COLUMN technicianAfterPhotoUrl TEXT")
            }
        }

        /**
         * Returns the single, centralized business database shared by all users (Admin & Staff).
         * Completely eliminates per-user isolated databases so that everyone sees and edits the same data.
         */
        fun getDatabase(context: Context, @Suppress("UNUSED_PARAMETER") userPhone: String = ""): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    CENTRAL_DB_NAME
                )
                    .addMigrations(MIGRATION_11_12)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
