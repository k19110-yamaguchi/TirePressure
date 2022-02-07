package com.example.tirepressure

import android.app.AlertDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import java.text.SimpleDateFormat
import java.util.*

// RecyclerViewにRealmデータを表示する
class DataListAdapter(data: OrderedRealmCollection<DataList>, context: Context) :
    RealmRecyclerViewAdapter<DataList, DataListAdapter.ViewHolder>(data, true){
    // データベース用class
    private val dbActivity = DatabaseActivity()
    private val ct = context
    // 日時のフォーマット
    val df = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

        init {
            setHasStableIds(true)
        }

    class ViewHolder(cell: View) : RecyclerView.ViewHolder(cell){
        val id: TextView = cell.findViewById(R.id.d_id)
        val priod: TextView = cell.findViewById(R.id.d_priod)
        val naturalSpeed: TextView = cell.findViewById(R.id.d_naturalSpeed)
        val latitude: TextView = cell.findViewById(R.id.d_latitude)
        val longitude: TextView = cell.findViewById(R.id.d_longitude)
        val speed: TextView = cell.findViewById(R.id.d_speed)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.datalayout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataList: DataList? = getItem(position)

        holder.id.text = "id: " + dataList?.id
        holder.priod.text = df.format(dataList?.startDate) + " 〜 " + df.format(dataList?.stopDate)
        holder.naturalSpeed.text = dataList?.naturalSpeed.toString() + "km/h"
        var text_lat = "緯度（°）\n"
        var text_lon = "経度（°）\n"
        var text_s = "速度（km/h）\n"

        Log.d("DataListAdapter", "開始！")
        for(i in 0 ..dataList?.latitude?.size!!-1){
            if(i == 0){
                text_lat += dataList?.latitude?.get(i).toString() + "\n"
                text_lon += dataList?.longitude?.get(i).toString() + "\n"
                text_s += "\n" + dataList?.time?.get(i).toString() + "\n"

            }else{
                text_lat += dataList?.latitude?.get(i).toString() + "\n"
                text_lon += dataList?.longitude?.get(i).toString() + "\n"
                text_s += dataList?.speed?.get(i-1).toString() + "\n" + dataList?.time?.get(i).toString() + "\n"

            }
        }
        Log.d("DataListAdapter", text_lat)
        holder.latitude.text = text_lat
        holder.longitude.text = text_lon
        holder.speed.text = text_s

        holder.itemView.setOnClickListener {

            dbActivity.alertDialog(dataList.id, ct)

        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: 0
    }
}