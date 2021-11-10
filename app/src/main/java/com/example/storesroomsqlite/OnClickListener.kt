package com.example.storesroomsqlite

interface OnClickListener {
    fun onclick (storeId: Long)
    fun onFavoriteStore (storeEntity: StoreEntity)
    fun onDeleteStore (storeEntity: StoreEntity)
}