package com.example.cryptocurrency.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cryptocurrency.data.pojo.CoinInfo

@Dao
interface CoinInfoDao {
    @Query("SELECT * FROM coins")
    fun getAllCoins(): List<CoinInfo>

    @Query("SELECT name FROM coins")
    fun getAllCoinsNames(): List<String>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoins(coins: List<CoinInfo>)
}