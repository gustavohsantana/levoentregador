package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pending_stop_actions")
data class PendingStopActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val token: String,
    val stopId: String,
    val outcome: String, // "DELIVERED" or "FAILED"
    val reason: String? = null,
    val deliveryCode: String? = null,
    val occurredAt: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val attemptCount: Int = 0
)

@Entity(tableName = "cached_route")
data class CachedRouteEntity(
    @PrimaryKey val token: String,
    val routeId: String,
    val establishmentName: String,
    val courierName: String,
    val exigeCodigo: Boolean,
    val routeJson: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Dao
interface DriverDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingAction(action: PendingStopActionEntity): Long

    @Query("SELECT * FROM pending_stop_actions ORDER BY id ASC")
    fun observePendingActions(): Flow<List<PendingStopActionEntity>>

    @Query("SELECT * FROM pending_stop_actions ORDER BY id ASC")
    suspend fun getAllPendingActions(): List<PendingStopActionEntity>

    @Query("DELETE FROM pending_stop_actions WHERE id = :id")
    suspend fun deletePendingAction(id: Long)

    @Query("SELECT COUNT(*) FROM pending_stop_actions")
    fun observePendingActionsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCachedRoute(route: CachedRouteEntity)

    @Query("SELECT * FROM cached_route WHERE token = :token LIMIT 1")
    suspend fun getCachedRoute(token: String): CachedRouteEntity?

    @Query("DELETE FROM cached_route WHERE token = :token")
    suspend fun clearCachedRoute(token: String)
}

@Database(
    entities = [PendingStopActionEntity::class, CachedRouteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DriverDatabase : RoomDatabase() {
    abstract fun driverDao(): DriverDao

    companion object {
        @Volatile
        private var INSTANCE: DriverDatabase? = null

        fun getDatabase(context: Context): DriverDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DriverDatabase::class.java,
                    "levo_driver.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
