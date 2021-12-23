package com.example.tirepressure.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.time.LocalDate
import java.util.*

open class AlertSpeed : RealmObject(){
    @PrimaryKey
    var id: Long = 0
    var als: Double = 0.0
    var dateInf : Date = Date()

}