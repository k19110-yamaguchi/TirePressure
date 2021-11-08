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
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tirepressure.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), LocationListener {
    // ActivityMainに対応するviewBindingを生成
    private lateinit var binding: ActivityMainBinding
    //　locationManager
    private lateinit var locationManager: LocationManager

    private lateinit var sb: SpannableStringBuilder

    // 測定中かどうか
    private var measurement = false
    // この速度以下になったら通知する
    private val alartSpeed = 18.0
    // Toastで表示するtext
    private var toastText = ""
    // messageに表示するtext
    private var messageText = ""
    // dataに表示するtext
    private var dataText = ""
    // unitに表示するtext
    private val unitText = "km/h"

    // class
    private val dataList = DataList()
    private val calculation = Calculation()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                dataList.latitudeArr.clear()
                dataList.longitudeArr.clear()
                dataList.timeArr.clear()
                dataList.speedArr.clear()
                dataList.naturalSpeed = 0.0
                binding.data.setText("")
                binding.naturalSpeed.setText("")
                binding.unit.setText("")


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
                locationStop()
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
        // 最頻値で自然に漕いでいる時の速度を求める

        dataList.naturalSpeed = calculation.calcMode(dataList.speedArr)

        // 最頻値がない場合（mode = 0）
        if(dataList.naturalSpeed == 0.0){
            // 中央値で自然に漕いでいる時の速度を求める
            dataList.naturalSpeed = calculation.calcMedian(dataList.speedArr)
            Log.d("locationStop", "中央値で計算")
        }else{
            Log.d("locationStop", "最頻値で計算")
        }
        Log.d("locationStop", "naturalSeed : " + dataList.naturalSpeed.toString())

        /*
        // なんか使えない
        sb.append("自然に漕いでいた時の速度 ")
        val start = sb.length
        sb.append(dataList.naturalSpeed.toString())
        val end = sb.length
        sb.append("km/h")
        sb.setSpan(RelativeSizeSpan(1.5f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.data.setText(sb)
        */

        if(dataList.naturalSpeed >= alartSpeed){
            messageText = "問題なし"
        }else{
            messageText = "タイヤの空気圧低下"
        }
        binding.message.setText(messageText)

        dataText = "自然に漕いでいた時の速度"
        binding.data.setText(dataText)

        binding.naturalSpeed.setText(dataList.naturalSpeed.toString())

        binding.unit.setText(unitText)

        // データの保存


    }

    // 位置が変わると呼び出される
    override fun onLocationChanged(location: Location) {
        if(measurement){
            // 位置情報と時間を保存
            dataList.latitudeArr.add(location.latitude)
            dataList.longitudeArr.add(location.longitude)
            dataList.timeArr.add(location.time)
            Log.d("onLocationChanged", "latitude : " + dataList.latitudeArr.get(dataList.latitudeArr.size-1).toString())
            Log.d("onLocationChanged", "longitude : " + dataList.longitudeArr.get(dataList.longitudeArr.size-1).toString())
            Log.d("onLocationChanged", "time : " + dataList.timeArr.get(dataList.timeArr.size-1).toString())

            // 計測データが2つ以上ある時
            if(dataList.latitudeArr.size >= 2){
                // 速度を計算
                val speed = calculation.calcSpeed(
                    dataList.latitudeArr.get(dataList.latitudeArr.size-1),
                    dataList.longitudeArr.get(dataList.longitudeArr.size-1),
                    dataList.timeArr.get(dataList.timeArr.size-1),
                    dataList.latitudeArr.get(dataList.latitudeArr.size-2),
                    dataList.longitudeArr.get(dataList.longitudeArr.size-2),
                    dataList.timeArr.get(dataList.timeArr.size-2))

                // 速度を保存
                dataList.speedArr.add(speed)

                Log.d("onLocationChanged", "speed : " + dataList.speedArr.get(dataList.speedArr.size-1).toString())

                dataText = "緯度：" + dataList.latitudeArr.get(dataList.latitudeArr.size-1) + "°\n" +
                        "経度：" + dataList.longitudeArr.get(dataList.longitudeArr.size-1) + "°\n" +
                        "速度：" + dataList.speedArr.get(dataList.speedArr.size-1) + "km/h"

                binding.data.setText(dataText)

            }else{

                dataText = "緯度：" + dataList.latitudeArr.get(dataList.latitudeArr.size-1) + "°\n" +
                        "経度：" + dataList.longitudeArr.get(dataList.longitudeArr.size-1) + "°\n"

                binding.data.setText(dataText)

            }

            messageText = "測定中"
            binding.message.setText(messageText)

        }
    }
}