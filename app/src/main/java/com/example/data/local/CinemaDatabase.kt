package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.CategoryBouquetEntity
import com.example.model.StreamItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreamsChunk(items: List<StreamItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBouquets(bouquets: List<CategoryBouquetEntity>)

    @Query("SELECT COUNT(*) FROM stream_items")
    fun observeTotalStreamCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM stream_items")
    suspend fun getTotalStreamCountOnce(): Int

    @Query("SELECT COUNT(*) FROM stream_items WHERE categoryType = 'MOVIE'")
    suspend fun getMovieCountOnce(): Int

    @Query("SELECT * FROM stream_items WHERE categoryType = 'MOVIE' ORDER BY addedTimestamp DESC, id DESC LIMIT 25")
    fun observeRecentlyAddedMovies(): Flow<List<StreamItemEntity>>

    @Query("SELECT * FROM category_bouquets WHERE categoryType = :categoryType ORDER BY itemCount DESC, categoryName ASC")
    fun observeBouquetsByType(categoryType: String): Flow<List<CategoryBouquetEntity>>

    @Query("SELECT * FROM category_bouquets ORDER BY itemCount DESC, categoryName ASC")
    fun observeAllBouquets(): Flow<List<CategoryBouquetEntity>>

    @Query(
        """
        SELECT * FROM stream_items 
        WHERE (:categoryType = '' OR categoryType = :categoryType)
          AND (:categoryId = '' OR categoryId = :categoryId)
          AND (:searchQuery = '' OR title LIKE '%' || :searchQuery || '%' OR categoryName LIKE '%' || :searchQuery || '%')
        ORDER BY isFavorite DESC, addedTimestamp DESC, title ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getWindowedStreams(
        categoryType: String,
        categoryId: String,
        searchQuery: String,
        limit: Int,
        offset: Int
    ): List<StreamItemEntity>

    @Query(
        """
        SELECT COUNT(*) FROM stream_items 
        WHERE (:categoryType = '' OR categoryType = :categoryType)
          AND (:categoryId = '' OR categoryId = :categoryId)
          AND (:searchQuery = '' OR title LIKE '%' || :searchQuery || '%' OR categoryName LIKE '%' || :searchQuery || '%')
        """
    )
    suspend fun countMatchingStreams(
        categoryType: String,
        categoryId: String,
        searchQuery: String
    ): Int

    @Query(
        """
        SELECT * FROM stream_items
        WHERE categoryType = 'LIVE'
          AND (
            title LIKE '%beIN%' OR 
            title LIKE '%SSC%' OR 
            title LIKE '%DAZN%' OR 
            title LIKE '%Canal%' OR 
            title LIKE '%Arryadia%' OR 
            title LIKE '%Sky Sport%' OR 
            title LIKE '%Sport%' OR 
            title LIKE '%ESPN%' OR 
            title LIKE '%TNT%' OR 
            title LIKE '%Euro%' OR 
            categoryName LIKE '%Sport%' OR
            title LIKE '%' || :keyword1 || '%' OR
            title LIKE '%' || :keyword2 || '%'
          )
        ORDER BY 
          CASE 
            WHEN title LIKE '%4K%' OR title LIKE '%UHD%' THEN 0
            WHEN title LIKE '%FHD%' OR title LIKE '%1080%' THEN 1
            ELSE 2
          END,
          title ASC
        LIMIT 40
        """
    )
    suspend fun findSmartSportsChannels(keyword1: String, keyword2: String): List<StreamItemEntity>

    @Query("UPDATE stream_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM stream_items")
    suspend fun clearAllStreams()

    @Query("DELETE FROM category_bouquets")
    suspend fun clearAllBouquets()
}

@Database(
    entities = [StreamItemEntity::class, CategoryBouquetEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CinemaDatabase : RoomDatabase() {
    abstract fun streamDao(): StreamDao

    companion object {
        @Volatile
        private var INSTANCE: CinemaDatabase? = null

        fun getInstance(context: Context): CinemaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CinemaDatabase::class.java,
                    "nevada_cine_plus_cache.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
