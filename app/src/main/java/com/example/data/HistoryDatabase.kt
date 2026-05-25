package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "document_history")
data class DocumentHistory(
    @PrimaryKey val uriString: String,
    val fileName: String,
    val fileSize: Long,
    val fileType: String,
    val lastOpenedTimestamp: Long = System.currentTimeMillis()
)

@Dao
interface DocumentHistoryDao {
    @Query("SELECT * FROM document_history ORDER BY lastOpenedTimestamp DESC")
    fun getRecentDocuments(): Flow<List<DocumentHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentHistory)

    @Query("DELETE FROM document_history WHERE uriString = :uriString")
    suspend fun deleteDocument(uriString: String)

    @Query("DELETE FROM document_history")
    suspend fun clearAllHistory()
}

@Database(entities = [DocumentHistory::class], version = 1, exportSchema = false)
abstract class FeathurDatabase : RoomDatabase() {
    abstract fun historyDao(): DocumentHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: FeathurDatabase? = null

        fun getDatabase(context: Context): FeathurDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FeathurDatabase::class.java,
                    "feathur_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class DocumentHistoryRepository(private val dao: DocumentHistoryDao) {
    val recentDocuments: Flow<List<DocumentHistory>> = dao.getRecentDocuments()

    suspend fun insert(document: DocumentHistory) {
        dao.insertDocument(document)
    }

    suspend fun delete(uriString: String) {
        dao.deleteDocument(uriString)
    }

    suspend fun clearAll() {
        dao.clearAllHistory()
    }
}
