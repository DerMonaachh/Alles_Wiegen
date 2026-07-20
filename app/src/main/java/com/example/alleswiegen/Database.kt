package com.example.alleswiegen

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "player_scores")
data class PlayerScoreEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Wichtig für die Datenbank

    val name: String = "",
    val playerCount: Int = 0,
    val averageError: Double = 0.0,
    val sipCount: Int = 0,
    val sipCountWithoutExer: Int = 0,
    val totalErrorClassic: Int= 0,
    val classicPosition : Int=0,
    val totalErrorWithoutExer: Int= 0,
    val withoutExerPosition: Int=0,
    val bagelCount: Int = 0,



    // Besserer Typ: Long speichert Millisekunden.
    // Lässt sich super sortieren und später einfach formatieren.
    val timestamp: Long =0//System.currentTimeMillis() / 1000) - 1704067200L).toInt()
)
@Entity(
    tableName = "events",
    indices = [Index(value = ["name"], unique = true)] // This makes the name unique
)
data class EventEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startTimestamp: Long, // Dein gewohntes Format
    val endTimestamp: Long
)
{
    override fun toString(): String {
        return "$name: $startTimestamp - $endTimestamp"
    }
}
@Dao
interface PlayerScoreDao {
    @Insert
    suspend fun insertScore(score: PlayerScoreEntry)

    @Query("SELECT * FROM player_scores ORDER BY timestamp DESC")
    fun getAllScores(): Flow<List<PlayerScoreEntry>>

    @Query("DELETE FROM player_scores")
    suspend fun deleteAll()

    @Query("SELECT * FROM player_scores WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getScoresByTimeRange(start: Long, end: Long): Flow<List<PlayerScoreEntry>>

    @Upsert
    suspend fun upsertEvent(event: EventEntry)

    @Query("SELECT * FROM events ORDER BY startTimestamp ASC")
    fun getAllEvents(): Flow<List<EventEntry>>

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Long)
}

// 1. Version auf 10 erhöhen
@Database(entities = [PlayerScoreEntry::class, EventEntry::class], version = 11)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerScoreDao(): PlayerScoreDao

    companion object {
        // 2. Die Migrations-Logik definieren

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "alles_wiegen_database"
                )
                    // 3. Migration hinzufügen

                    // 4. WICHTIG: Wenn die Migration fehlschlägt, löscht dies weiterhin alles.
                    // Wenn du das NICHT willst, entferne .fallbackToDestructiveMigration(true)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


class PlayerScoreRepository(private val dao: PlayerScoreDao) {
    val allScores: Flow<List<PlayerScoreEntry>> = dao.getAllScores()
    //val allEvents: Flow<List<EventEntry>> = dao.getAllEvents()
    suspend fun upsertEvent(event: EventEntry) = dao.upsertEvent(event)


    suspend fun insert(score: PlayerScoreEntry) {
        dao.insertScore(score)
    }

    suspend fun clearAll() {
        dao.deleteAll()
    }
     fun  getAllEvents() = dao.getAllEvents()
    fun getScoresForRange(start: Long, end: Long) = dao.getScoresByTimeRange(start, end)
}
/*
class ScoreViewModel(private val repository: PlayerScoreRepository) : ViewModel() {

    // Wandelt den Flow aus der DB in einen StateFlow für die UI um
    val allScores: StateFlow<List<PlayerScoreEntry>> = repository.allScores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val allEvents: StateFlow<List<EventEntry>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEvent(name: String, start: Int, end: Int) {
        viewModelScope.launch {
            repository.upsertEvent(
                EventEntry(
                    name = name,
                    startTimestamp = start,
                    endTimestamp = end
                )
            )
        }
    }


    fun saveScore(score: PlayerScoreEntry) {
        viewModelScope.launch {
            repository.insert(score)
        }
    }

    fun deleteHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}*/