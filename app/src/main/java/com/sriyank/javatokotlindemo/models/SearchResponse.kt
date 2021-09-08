package com.sriyank.javatokotlindemo.models

import io.realm.RealmObject
import com.google.gson.annotations.SerializedName
import io.realm.RealmList

open class SearchResponse(

    @SerializedName("total_count") var totalCount: Int=0,
    var items: RealmList<Repository>?=null
) : RealmObject()
