package com.sriyank.javatokotlindemo.models

import io.realm.RealmObject
import com.google.gson.annotations.SerializedName
import io.realm.RealmList

data class SearchResponse(

    @SerializedName("total_count") var totalCount: Int,
    var items: List<Repository>?
)
