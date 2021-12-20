package com.example.tirepressure

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tirepressure.databinding.ActivityDatabaseBinding

class DatabaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatabaseBinding

    // class
    private val database = Database()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Realmのインスタンスを取得
        database.create()

        binding = ActivityDatabaseBinding.inflate(layoutInflater)
        val view = binding.root
        view.context
        setContentView(view)

        val intent = Intent(application, MainActivity::class.java)

        binding.goMain.setOnClickListener {
            startActivity(intent)

        }

        binding.list.layoutManager = LinearLayoutManager(this)
        val dl = database.getAllData()
        val adapter = DataListAdapter(dl!!, this)
        binding.list.adapter = adapter


        binding.delete.setOnClickListener {
            AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                .setTitle("削除確認")
                .setMessage("全てのデータを削除しますか？")
                .setPositiveButton("OK", { dialog, which ->
                    // TODO:Yesが押された時の挙動
                    database.delData(-1)

                })
                .setNegativeButton("No", { dialog, which ->
                    // TODO:Noが押された時の挙動

                })
                .show()

        }

    }

    // アクティビティ終了処理
    override fun onDestroy() {
        super.onDestroy()
        database.close()
    }

    fun alertDialog(id: Long, context: Context){

        AlertDialog.Builder(context) // FragmentではActivityを取得して生成
            .setTitle("削除確認")
            .setMessage("id:" + id + "のデータを削除しますか？")
            .setPositiveButton("OK", { dialog, which ->
                // TODO:Yesが押された時の挙動
                database.create()
                database.delData(id)
                database.close()

            })
            .setNegativeButton("No", { dialog, which ->
                // TODO:Noが押された時の挙動

            })
            .show()


    }
}