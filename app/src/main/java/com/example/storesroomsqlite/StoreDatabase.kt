package com.example.storesroomsqlite

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(StoreEntity::class), version = 2)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDAO
}