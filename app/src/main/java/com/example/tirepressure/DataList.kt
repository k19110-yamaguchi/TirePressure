package com.example.tirepressure

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// モデルを準備
open class DataList : RealmObject(){
    @PrimaryKey
    var id: Long = 0
    // 測定開始日時
    var startDate: String = ""
    // 測定終了日時
    var stopDate: String = ""
    // 緯度のリスト
    var latitude: RealmList<Double> = RealmList()
    // 経度のリスト
    var longitude: RealmList<Double> = RealmList()
    // 時間のリスト
    var time: RealmList<Long> = RealmList()
    // 速度のリスト
    var speed: RealmList<Double> = RealmList()
    // 自然に漕いでいるときの速度
    var naturalSpeed: Double = 0.0
}