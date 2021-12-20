package com.example.tirepressure

import android.util.Log
import io.realm.RealmList
import kotlin.collections.ArrayList

class Calculation {
    private val latDistance = 91.2877855
    private val lonDistance = 110.9964837
    private val truncateSpeed = 2.0

    fun calcSpeed(lat1: Double, lon1: Double, t1: Long,
                  lat2: Double, lon2: Double, t2: Long): Double {
        // 緯度の差
        var latDiff = Math.abs(lat1 - lat2)
        latDiff = Math.round(latDiff * Math.pow(10.0, 8.0)) / Math.pow(10.0, 8.0)

        // 経度の差
        var lonDiff = Math.abs(lon1 - lon2)
        lonDiff = Math.round(lonDiff * Math.pow(10.0, 8.0)) / Math.pow(10.0, 8.0)

        // 距離
        var dis = Math.sqrt(Math.pow(latDistance*latDiff, 2.0) +
                    Math.pow(lonDistance*lonDiff, 2.0))
        dis = Math.round(dis * Math.pow(10.0, 8.0))/ Math.pow(10.0, 8.0)

        // 時間
        val tDiff = Math.abs(t1-t2)/ 1000

        // 速度
        val speed = dis/tDiff * 3600;

        val roundedSpeed = Math.round(speed * 10.0).toDouble() / 10

        return roundedSpeed

    }

    // 最頻値を計算
    fun calcMode(s: RealmList<Double>): Double{

        // 速度を昇順でsort
        s.sort()

        // 最頻値
        var mode = 0.0
        // 前と同じだった回数
        var count = 0
        // 最大回数
        var maxCount = 0

        for(i in 1..s.size-1){
            // truncateSpeedの値以上で求める
            if(s.get(i)!! >= truncateSpeed) {
                // 1つ前の値と同じ時
                if(s.get(i) == s.get(i-1)){
                    count += 1

                // 1つ前の速度と違う時
                }else{
                    // 今まで出た回数が多い場合
                    if(count >= maxCount){
                        maxCount = count
                        mode = s.get(i-1)!!
                        count = 0
                    }
                }
            }
        }

        return mode
    }

    // 中央値を求める
    fun calcMedian(s: RealmList<Double>): Double{
        s.sort()
        val index: Int = s.size/2
        val median = s.get(index)

        return median!!
    }

    // 平均を求める
    fun calcAve(list: RealmList<Double>): Double{
        var sum = 0.0
        for(i in 1..list.size-1){
            sum += list[i]!!

        }
        var ave = sum / list.size
        return ave

    }

    // 標準偏差を求める
    fun calcSd(ave: Double, list: RealmList<Double>): Double{
        var sum = 0.0
        for(i in 1..list.size-1){
            sum += Math.pow((list[i]!! - ave), 2.0)

        }
        val sd = Math.sqrt(sum/list.size)

        return sd
    }

    // この速度以下になったら通知する速度を求める
    fun calcAlartSpeed(ns: RealmList<Double>, n: Int): Double {
        var test = false
        var als = 18.0

        if (!test) {
            val ave = calcAve(ns)
            val sd = calcSd(ave, ns)
            als = ave - (n * sd)
            als = Math.round(als * 10.0).toDouble() / 10
            var msg = n.toString() + "σ: " + (n * sd)
            Log.d("calcAlartSpeed", msg)
        }

        return als
    }

    fun LongtoString(s_date: String): Long{
        var l_date = 0L
        return l_date
    }

}

