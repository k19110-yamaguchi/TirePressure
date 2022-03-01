package com.example.tirepressure

import android.util.Log
import com.example.tirepressure.model.AlertSpeed
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.time.LocalDate
import java.util.*

class Database {
    private lateinit var realm: Realm


    // データベースを作成
    fun create(){
        // Realmのインスタンスを取得
        realm = Realm.getDefaultInstance()

    }


    // DataListにデータを保存
    fun setData(lat: RealmList<Double>, lon: RealmList<Double>, startData: Date,
        stopData: Date, t: RealmList<Long>, s: RealmList<Double>, ns: Double){
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

    // DataListから全てデータを取得
    fun getAllData(): RealmResults<DataList>? {
        // 全てのデータを取得
        var  dataList = realm.where<DataList>().findAll()
        return dataList

    }

    // DataListから特定のデータを取得
    fun getData(index: Long): DataList?{
        var data = realm.where<DataList>().equalTo("id", index).findFirst()
        return data

    }

    // DataListから特定のデータを削除
    fun delData(index: Long){

        // 全てのデータを削除
        if(index == -1L){
            val dl = getAllData()
            realm.executeTransaction{
                dl?.deleteAllFromRealm()

            }
        }else{
            realm.executeTransaction{
                var dl = getData(index)
                dl?.deleteFromRealm()

            }
        }
    }

    // DataListの保存数を取得
    fun getMaxId(): Long{
        var maxId = 0L
        realm.executeTransaction{db ->
            maxId = db.where<DataList>().max("id")!!.toLong()

        }
        return maxId

    }

    // AlertSpeed関係
    // 通知速度を保存
    fun saveALS(als : Double?, dateInf: Date?, dateInfAfter: Date?){
        realm.executeTransaction{ db: Realm ->
            var dataList = realm.where<AlertSpeed>().equalTo("id", 1L).findFirst()
            if(dataList == null){
                dataList = db.createObject<AlertSpeed>(1L)
            }
            if(als != null){
                dataList.als = als
            }
            if(dateInf != null){
                dataList.dateInf = dateInf
            }
            if(dateInfAfter != null){
                dataList.dateInfAfter = dateInfAfter
            }
        }
    }

    // 通知速度を取得
    fun getALS(): Double{
        create()
        var data = realm.where<AlertSpeed>().equalTo("id", 1L).findFirst()
        return data!!.als

    }

    // 通知速度関係のデータベースを削除
    fun delALS(){
        var  als = realm.where<AlertSpeed>().findAll()
        realm.executeTransaction{
            als?.deleteAllFromRealm()

        }
    }

    // 空気を入れた日を取得
    fun getDateInf(): Date? {
        var data = realm.where<AlertSpeed>().equalTo("id", 1L).findFirst()
        if(data != null) {
            return data.dateInf
        }else{
           return null
        }

    }
    // 測定終了日を取得
    fun getDateInfAfter(): Date? {
        var data = realm.where<AlertSpeed>().equalTo("id", 1L).findFirst()
        if(data != null) {
            return data.dateInfAfter
        }else{
            return null
        }

    }

    // データベースを閉じる
    fun close(){
        realm.close()

    }
}