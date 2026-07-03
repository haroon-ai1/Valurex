package com.nobody.valurex.data.db.dao

import androidx.room.*
import com.nobody.valurex.data.db.entity.WalletLog

@Dao
interface WalletLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(walletLog: WalletLog)

    @Query("SELECT * FROM WalletLog ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): WalletLog?
}
