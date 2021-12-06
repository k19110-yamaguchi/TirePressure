package com.example.tirepressure

import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import java.text.SimpleDateFormat
import java.util.*

// RecyclerViewにRealmデータを表示する
class DataListAdapter(data: OrderedRealmCollection<DataList>) :
    RealmRecyclerViewAdapter<DataList, DataListAdapter.ViewHolder>(data, true){

        init {
            setHasStableIds(true)
        }

    class ViewHolder(cell: View) : RecyclerView.ViewHolder(cell){
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

        holder.priod.text = dataList?.startDate + " 〜 " + dataList?.stopDate
        holder.naturalSpeed.text = dataList?.naturalSpeed.toString() + "km/h"
        var text_lat = "緯度（°）\n"
        var text_lon = "経度（°）\n"
        var text_s = "速度（km/h）\n"

        Log.d("DataListAdapter", "開始！")
        for(i in 0 ..dataList?.latitude?.size!!-1){
            if(i == 0){
                text_lat += dataList?.latitude?.get(i).toString() + "\n"
                text_lon += dataList?.longitude?.get(i).toString() + "\n"
                text_s += "\n"

            }else{
                text_lat += dataList?.latitude?.get(i).toString() + "\n"
                text_lon += dataList?.longitude?.get(i).toString() + "\n"
                text_s += dataList?.speed?.get(i-1).toString() +"\n"

            }
        }
        Log.d("DataListAdapter", text_lat)
        holder.latitude.text = text_lat
        holder.longitude.text = text_lon
        holder.speed.text = text_s
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: 0
    }
}