package com.example.tirepressure

import android.util.Log
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.kotlin.createObject
import io.realm.kotlin.where

class Database {
    private lateinit var realm: Realm

    // データベースを作成
    fun create(){
        // Realmのインスタンスを取得
        realm = Realm.getDefaultInstance()

    }

    // データベースにデータを保存
    fun setData(lat: RealmList<Double>, lon: RealmList<Double>, startData: String,
        stopData: String, t: RealmList<Long>, s: RealmList<Double>, ns: Double){
        realm.executeTransaction{ db: Realm ->
            val maxId = db.where<DataList>().max("id")
            val nextId = (maxId?.toLong() ?: 0L) + 1L
            val dataList = db.createObject<DataList>(nextId)
            dataList.latitude = lat
            dataList.longitude = lon
            dataList.startDate = startData
            dataList.stopDate = stopData
            dataList.time = t
            dataList.speed = s
            dataList.naturalSpeed = ns

        }

    }

    // データベースを更新
    fun updateData(lat: RealmList<Double>, lon: RealmList<Double>, startData: String,
                   stopData: String, t: RealmList<Long>, s: RealmList<Double>, ns: Double, id: Long){

        realm.executeTransaction{ db: Realm ->
            val dataList = db.where<DataList>()
                .equalTo("id", id).findFirst()
            dataList?.latitude  = lat
            dataList?.longitude = lon
            dataList?.startDate = startData
            dataList?.stopDate = stopData
            dataList?.time = t
            dataList?.speed = s
            dataList?.naturalSpeed = ns

        }

    }

    // データベースから全てデータを取得
    fun getAllData(): RealmResults<DataList>? {
        // 全てのデータを取得
        var  dataList = realm.where<DataList>().findAll()
        return dataList

    }

    // データベースから特定のデータを取得
    fun getData(index: Long): DataList?{
        var data = realm.where<DataList>().equalTo("id", index).findFirst()
        return data

    }

    // データベースから特定のデータを削除
    fun delData(index: Long){

        if(index == -1L){
            val dl = getAllData()
            realm.executeTransaction{
                dl?.deleteAllFromRealm()

            }
        }else{
            var id = index
            while (true){
                var dl = getData(id)
                if(dl == null){
                    realm.executeTransaction{
                        dl = getData(id-1L)
                        dl?.deleteFromRealm()

                    }
                    break

                }
                updateData(dl!!.latitude, dl!!.longitude, dl!!.startDate,
                    dl!!.stopDate, dl!!.time, dl!!.speed, dl!!.naturalSpeed, id-1L)
                id++

            }
        }
    }

    fun getMaxId(): Long{
        var maxId = 0L
        realm.executeTransaction{db ->
            maxId = db.where<DataList>().max("id")!!.toLong()

        }

        return maxId

    }

    // データベースを閉じる
    fun close(){
        realm.close()

    }
}