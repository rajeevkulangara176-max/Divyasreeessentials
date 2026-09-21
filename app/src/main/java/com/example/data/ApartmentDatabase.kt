package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ApartmentDao
import com.example.data.model.FlatEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.ReminderLogEntity

@Database(
    entities = [FlatEntity::class, PaymentEntity::class, ReminderLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ApartmentDatabase : RoomDatabase() {
    abstract fun apartmentDao(): ApartmentDao

    companion object {
        @Volatile
        private var INSTANCE: ApartmentDatabase? = null

        fun getDatabase(context: Context): ApartmentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ApartmentDatabase::class.java,
                    "apartment_association_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
