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

class MainActivity : AppCompatActivity(), LocationListener {
    // ActivityMainに対応するviewBindingを生成
    private lateinit var binding: ActivityMainBinding
    //　locationManager
    private lateinit var locationManager: LocationManager

    // 測定中かどうか
    private var measurement = false
    // Toastで表示するtext
    private var toastText = ""
    // messageに表示するtext
    private var messageText = ""
    // dataに表示するtext
    private var dataText = ""
    // unitに表示するtext
    private val unitText = "km/h"
    // 日時のフォーマット
    val df = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    // 開始日時
    var startData = ""
    // 終了日時
    var stopData = ""
    // 緯度のリスト
    var latitudeArr: RealmList<Double> = RealmList()
    // 経度のリスト
    var longitudeArr: RealmList<Double> = RealmList()
    // 時間のリスト
    var timeArr: RealmList<Long> = RealmList()
    // 速度のリスト
    var speedArr: RealmList<Double> = RealmList()
    // test用
    var testNS: RealmList<Double> = RealmList()
    // 今までの自然に漕いでいた時のリスト
    var nsList :RealmList<Double> = RealmList()
    // 自然に漕いでいるときの速度
    var naturalSpeed: Double = 0.0

    // 計算用class
    private val calculation = Calculation()
    // データベース用class
    private val database = Database()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Realmのインスタンスを取得
        database.create()


        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        // 開始ボタンが押された時
        binding.start.setOnClickListener {
            Log.d("start", "ボタンが押された")
            // 測定中の時
            if(measurement){
                //Toastの設定
                toastText = "測定中は無効です"
                var ts = Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT)
                ts.setGravity(Gravity.CENTER, 0, 800)
                ts.show()

            // 測定外の時
            }else{
                // 初期化
                initializeData()

                measurement = true
                messageText = "測定準備中"
                binding.message.setText(messageText)
                locationStart()

            }
        }

        // 終了ボタンが押された時
        binding.stop.setOnClickListener {
            Log.d("stop", "ボタンが押された")
            // 測定中の時
            if(measurement){
                measurement = false
                locationStop()

            // 測定外の時
            }else{
                // Toastの設定
                toastText = "開始ボタンを押してください"
                var ts = Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT)
                ts.setGravity(Gravity.CENTER, 0, 800)
                ts.show()

            }
        }

        binding.goDatabase.setOnClickListener {
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
    }

    // コピペ
    private fun locationStart() {

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("locationStart", "location manager Enabled")
        } else {
            // to prompt setting up GPS
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
            Log.d("locationStart", "not gpsEnable, startActivity")
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)

            Log.d("lcationStart", "checkSelfPermission false")
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
        Log.d("locationStop", "測定終了")
        stopData = df.format(timeArr.get(timeArr.size - 1))

        // 最頻値で自然に漕いでいる時の速度を求める
        naturalSpeed = calculation.calcMode(speedArr)

        // 最頻値がない場合（mode = 0）
        if(naturalSpeed == 0.0){
            // 中央値で自然に漕いでいる時の速度を求める
            naturalSpeed = calculation.calcMedian(speedArr)
            Log.d("locationStop", "中央値で計算")
        }else{
            Log.d("locationStop", "最頻値で計算")
        }
        Log.d("locationStop", "naturalSeed : " + naturalSpeed.toString())

        // データベースに保存
        database.setData(latitudeArr, longitudeArr, startData,
            stopData,timeArr,speedArr, naturalSpeed)

        // 今までの自然に漕いでいた時の速度を取得
        nsList.clear()
        var id = 1L
        var judge = true
        while(judge){
            var data = database.getData(id)
            var ns = data?.naturalSpeed
            Log.d("locationStop", "id:" + id + "ns:" + ns)
            if(ns == null){
                judge = false
            }else{
                nsList.add(ns)
                id++

            }
        }


        // この速度以下になったら通知する
        var alartSpeed = calculation.calcAlartSpeed(nsList, 1)
        Log.d("locationStop", "alartSpeed:" + alartSpeed)



        if(naturalSpeed >= alartSpeed){
            messageText = "タイヤの空気圧に\n問題なし！"
        }else{
            messageText = "タイヤに空気を\n入れたほうがいいよ！"
        }
        binding.message.setText(messageText)

        dataText = "自然に漕いでいた時の速度"
        binding.data.setText(dataText)

        binding.naturalSpeed.setText(naturalSpeed.toString())

        binding.unit.setText(unitText)

    }

    // 位置が変わると呼び出される
    override fun onLocationChanged(location: Location) {
        if(measurement){
            // 位置情報と時間を保存
            latitudeArr.add(location.latitude)
            longitudeArr.add(location.longitude)
            timeArr.add(location.time)
            Log.d("onLocationChanged", "latitude : " + latitudeArr.get(latitudeArr.size-1).toString())
            Log.d("onLocationChanged", "longitude : " + longitudeArr.get(longitudeArr.size-1).toString())
            Log.d("onLocationChanged", "time : " + timeArr.get(timeArr.size-1).toString())

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

                Log.d("onLocationChanged", "speed : " + speedArr.get(speedArr.size-1).toString())

                dataText = "緯度：" + latitudeArr.get(latitudeArr.size-1) + "°\n" +
                        "経度：" + longitudeArr.get(longitudeArr.size-1) + "°\n" +
                        "速度：" + speedArr.get(speedArr.size-1) + "km/h"

                binding.data.setText(dataText)

            }else{

                startData = df.format(timeArr.get(0))
                dataText = "緯度：" + latitudeArr.get(latitudeArr.size-1) + "°\n" +
                        "経度：" + longitudeArr.get(longitudeArr.size-1) + "°\n"

                binding.data.setText(dataText)

            }
            messageText = "速度を測定中"
            binding.message.setText(messageText)

        }
    }

    // データの初期化
    private fun initializeData(){
        latitudeArr.clear()
        longitudeArr.clear()
        timeArr.clear()
        speedArr.clear()
        naturalSpeed = 0.0
        binding.data.setText("")
        binding.naturalSpeed.setText("")
        binding.unit.setText("")

    }

    // アクティビティ終了処理
    override fun onDestroy() {
        super.onDestroy()
        database.close()

    }
}