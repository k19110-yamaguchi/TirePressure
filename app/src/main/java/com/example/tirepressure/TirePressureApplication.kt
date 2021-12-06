package com.example.tirepressure

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

// データベースの設定
class TirePressureApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        // Realmの初期化
        Realm.init(this)
        // Realmの初期設定
        val config = RealmConfiguration.Builder().allowWritesOnUiThread(true).build()
        Realm.setDefaultConfiguration(config)
        
    }
}