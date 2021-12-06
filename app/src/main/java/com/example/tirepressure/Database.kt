package com.example.tirepressure

import com.example.tirepressure.databinding.ActivityDatabaseBinding
import io.realm.Realm

class Database {
    private var _binding: ActivityDatabaseBinding? = null
    private val binding get() = _binding!!

    private lateinit var realm: Realm

    // データベースにデータを保存
    fun setDase(){

    }

    // データベースからデータを取得
    fun getDase(){

    }

    fun getDatabase(){
        realm = Realm.getDefaultInstance()

    }

    //override fun onDestroy(){
      //  super.onDestroy()
        //realm.close()
    //}

}