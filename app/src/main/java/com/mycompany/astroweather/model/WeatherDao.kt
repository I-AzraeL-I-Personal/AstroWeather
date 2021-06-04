package com.mycompany.astroweather.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather")
    fun getAll(): List<Weather>

    @Query("SELECT * FROM weather WHERE id IN (:weatherIds)")
    fun loadAllByIds(weatherIds: IntArray): List<Weather>

    @Query("SELECT * FROM weather WHERE city LIKE :city LIMIT 1")
    fun findByCity(city: String): Weather

    @Insert
    fun insertAll(vararg weathers: Weather)

    @Delete
    fun delete(weather: Weather)
}