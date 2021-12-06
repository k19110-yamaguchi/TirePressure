package com.example.tirepressure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tirepressure.databinding.ActivityDatabaseBinding
import io.realm.Realm
import io.realm.kotlin.where

class DatabaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatabaseBinding
    // Realmクラスのプロパティを用意
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Realmのインスタンスを取得
        realm = Realm.getDefaultInstance()

        binding = ActivityDatabaseBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intent = Intent(application, MainActivity::class.java)

        binding.goMain.setOnClickListener {
            startActivity(intent)
        }

        binding.list.layoutManager = LinearLayoutManager(this)
        val dataList = realm.where<DataList>().findAll()
        val adapter = DataListAdapter(dataList)
        binding.list.adapter = adapter

        binding.delete.setOnClickListener {
            realm.executeTransaction{
                dataList.deleteAllFromRealm()
            }
        }

    }

    // アクティビティ終了処理
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}