package com.example.tirepressure

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tirepressure.databinding.ActivityMainBinding
import io.realm.RealmList
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), LocationListener {
    // ActivityMainに対応するviewBindingを生成
    private lateinit var binding: ActivityMainBinding
    //　locationManagerを生成
    private lateinit var locationManager: LocationManager

    // 現在測定中かどうか
    private var measurement = false
    // 現在通知速度を求めているか
    private var calcAS = true

    // Toastで表示するtext
    private var toastText = ""
    // t_resultに表示するtext
    private var resultText = ""
    // t_dataに表示するtext
    private var dataText = ""
    // t_naturalSpeedに表示するtext
    private var naturalSpeedText = ""
    // t_unitに表示するtext
    private val unitText = "km/h"
    // t_statusに表示するtext
    private var statusText = ""
    // t_dateOfInflatedに表示するtext
    private var dateOfInflatedText = "空気を入れてボタンを押してください"

    // 日時のフォーマット
    private val fullSdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    // 日時のフォーマット
    private val aboutSdf = SimpleDateFormat("yyyy/MM/dd")

    // 開始日時
    var startData = Date()
    // 終了日時
    var stopData = Date()
    // 緯度のリスト
    var latitudeArr: RealmList<Double> = RealmList()
    // 経度のリスト
    var longitudeArr: RealmList<Double> = RealmList()
    // 時間のリスト
    var timeArr: RealmList<Long> = RealmList()
    // 速度のリスト
    var speedArr: RealmList<Double> = RealmList()
    // 今までの自然に漕いでいた時のリスト
    var nsList :RealmList<Double> = RealmList()
    // 自然に漕いでいるときの速度
    var naturalSpeed: Double = 0.0
    // 通知速度
    var alertSpeed: Double = 0.0
    // 空気を入れた日時
    var dateInf : Date? = null
    // 空気を入れたから何日後に判定するのか
    val amount = 1
    // 空気を入れた日からある程度経過した時の日付
    var dateInfAfter : Date? = null

    // 計算用class
    private val calculation = Calculation()
    // データベース用class
    private val database = Database()

    fun print(tag: String, mes: String){
        Log.d(tag, mes)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Realmのインスタンスを取得
        database.create()

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        // 空気を入れた日付を取得
        dateInf = database.getDateInf()
        // もし空気を入れた日の記録があった場合
        if(dateInf != null){
            alertSpeed = database.getAS()
            // 測定を終了する日を取得
            dateInfAfter = database.getDateInfAfter()
            // t_dateOfInflatedに表示するテキストを代入
            dateOfInflatedText = "最後に空気を入れた日：" + aboutSdf.format(dateInf)
            binding.tDateOfInflated.setText(dateOfInflatedText)
            // 状態を表示
            setStatus(binding)
        }else{
            binding.tDateOfInflated.setText("空気を入れてボタンを押してください")

        }

        // 開始ボタンが押された時
        binding.bStart.setOnClickListener {
            // タイヤに空気を入れた日常を記録していない場合
            if(dateInf == null){
                toastText = "空気を入れてボタンを押してください"
                var ts = Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT)
                ts.setGravity(Gravity.CENTER, 0, 800)
                ts.show()

            }else{
                // 測定中の時
                if(measurement){
                    //Toastの設定
                    toastText = "測定中は無効です"
                    var ts = Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT)
                    ts.setGravity(Gravity.CENTER, 0, 800)
                    ts.show()

                // 測定外の時
                }else{
                    measurement = true
                    // 初期化
                    initializeData()

                    locationStart()

                }
            }
        }

        // 終了ボタンが押された時
        binding.bStop.setOnClickListener {
            // 測定中の時
            if(measurement){
                // 終了ボタンを押すのが早い場合
                if(speedArr.size < 2){
                    // Toastの設定
                    toastText = "測定データが不足しています\n" +
                            "少しお待ちください"
                    var ts = Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT)
                    ts.setGravity(Gravity.CENTER, 0, 800)
                    ts.show()

                }else{
                    measurement = false
                    locationStop()

                }

            // 測定外の時
            }else{
                // Toastの設定
                toastText = "開始ボタンを押してください"
                var ts = Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT)
                ts.setGravity(Gravity.CENTER, 0, 800)
                ts.show()

            }
        }

        // データベースボタンが押された時
        binding.bGoDatabase.setOnClickListener {
            if(measurement){
                //Toastの設定
                toastText = "測定中は無効です"
                var ts = Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT)
                ts.setGravity(Gravity.CENTER, 0, 800)
                ts.show()

            }else{
                val intent = Intent(application, DatabaseActivity::class.java)
                startActivity(intent)

            }
        }

        // 空気を入れたボタンを押した時
        binding.bInflated.setOnClickListener {
            if(dateInf != null){
                // データベースを削除
                database.delALS()
            }
            // 空気を入れた時の日付を取得
            dateInf = Date()
            // 空気を入れたから１週間後の日時
            dateInfAfter = calculation.calcDaysLater(dateInf!!, amount)

            dateOfInflatedText = "最後に空気を入れた日："
            dateOfInflatedText += aboutSdf.format(dateInf)
            binding.tDateOfInflated.setText(dateOfInflatedText)


            // データベースに空気を入れた日付を保存
            database.saveDateInf(dateInf!!, dateInfAfter!!)
            // データベースに保存されている
            setStatus(binding)

        }
    }


    // t_statusに表示するテキストを取得
    private fun setStatus(binding: ActivityMainBinding){
        // 現在の日付
        val dateNow = Date()
        statusText = "状態："

        // 空気を入れてから1週間経っていない時
        if(dateNow.before(dateInfAfter)){
            calcAS = true
            var s_dateInf = aboutSdf.format(dateInf)
            var s_dateInfAfWeek = aboutSdf.format(dateInfAfter)
            statusText += s_dateInf + "〜" + s_dateInfAfWeek + "まで通知速度測定中"

        // 空気を入れてから1週間経っている時
        }else{
            calcAS = false
            statusText += "通知速度（" + alertSpeed.toString() + "km/h）と自然速度を比較中"

        }
        binding.tStatus.setText(statusText)

    }

    // コピペ
    private fun locationStart() {

        // Instances of LocationManager class must be obtained using Context.getSystemService(Class)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        } else {
            // to prompt setting up GPS
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
        }

        // 許可がなかった場合
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)

            measurement = false
            return
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            0f,
            this)
    }
    // ここまで

    private fun locationStop(){
        stopData = Date()

        // thread{計算する関数}
        // 最頻値で自然に漕いでいる時の速度を求める
        for(s in speedArr){
            print("自然速度計算前", s.toString())
        }
        naturalSpeed = calculation.calcMode(speedArr)

        // 最頻値がない場合（mode = 0）
        if(naturalSpeed == 0.0){
            // 中央値で自然に漕いでいる時の速度を求める
            naturalSpeed = calculation.calcMedian(speedArr)
        }else{
        }

        for(s in speedArr){
            print("自然速度計算後", s.toString())
        }

        // データベースに保存
        database.setData(latitudeArr, longitudeArr, startData,
            stopData,timeArr,speedArr, naturalSpeed)

        //
        if(calcAS){
            resultText = "現在計測期間中"

        }else{
            alertSpeed = database.getAS()
            if(alertSpeed == null){
                // 今までの自然に漕いでいた時の速度を取得
                nsList.clear()
                var sta_id = 0L
                var end_id = 0L
                val max_id = database.getMaxId()
                for(id in 1L..max_id){
                    var data = database.getData(id)
                    if(data != null){
                        if(data.startDate.before(dateInf) && data.startDate.after(dateInfAfter)){
                            if(sta_id == 0L){
                                sta_id = data.id
                                end_id = data.id

                            }else{
                                end_id = data.id
                            }
                        }
                    }
                }
                for(id in 1L .. end_id){
                    var data = database.getData(id)
                    var ns = data?.naturalSpeed
                    if(ns != null){
                        nsList.add(ns)
                    }
                }
                // この速度以下になったら通知する
                alertSpeed = calculation.calcAlertSpeed(nsList, 1)
                print("alertSpeed", alertSpeed.toString())
                database.saveAS(alertSpeed)

            }

            if(naturalSpeed >= alertSpeed){
                resultText = "タイヤの空気圧に\n問題なし！"
            }else{
                resultText = "タイヤに空気を\n入れたほうがいいよ！"
            }
        }

        binding.tResult.setText(resultText)

        dataText = "自然に漕いでいた時の速度"
        binding.tData.setText(dataText)

        binding.tNaturalSpeed.setText(naturalSpeed.toString())

        binding.tUnit.setText(unitText)

    }

    // 位置が変わると呼び出される
    override fun onLocationChanged(location: Location) {
        if(measurement){
            // 位置情報と時間を保存
            latitudeArr.add(location.latitude)
            longitudeArr.add(location.longitude)
            timeArr.add(location.time)

            // 計測データが2つ以上ある時
            if(latitudeArr.size >= 2){
                // 速度を計算
                val speed = calculation.calcSpeed(
                    latitudeArr.get(latitudeArr.size-1)!!,
                    longitudeArr.get(longitudeArr.size-1)!!,
                    timeArr.get(timeArr.size-1)!!,
                    latitudeArr.get(latitudeArr.size-2)!!,
                    longitudeArr.get(longitudeArr.size-2)!!,
                    timeArr.get(timeArr.size-2)!!)

                // 速度を保存
                speedArr.add(speed)
                print("speed", speed.toString())

                dataText = "緯度：" + latitudeArr.get(latitudeArr.size-1) + "°\n" +
                        "経度：" + longitudeArr.get(longitudeArr.size-1) + "°\n" +
                        "速度：" + speedArr.get(speedArr.size-1) + "km/h"

                binding.tData.setText(dataText)

            }else{

                startData = Date()
                dataText = "緯度：" + latitudeArr.get(latitudeArr.size-1) + "°\n" +
                        "経度：" + longitudeArr.get(longitudeArr.size-1) + "°\n"

                binding.tData.setText(dataText)

            }
            resultText = "速度を測定中"
            binding.tResult.setText(resultText)

        }
    }

    // データの初期化
    private fun initializeData(){
        latitudeArr.clear()
        longitudeArr.clear()
        timeArr.clear()
        speedArr.clear()
        naturalSpeed = 0.0
        binding.tData.setText("")
        binding.tNaturalSpeed.setText("")
        binding.tUnit.setText("")

    }

    // アクティビティ終了処理
    override fun onDestroy() {
        super.onDestroy()
        database.close()

    }
}