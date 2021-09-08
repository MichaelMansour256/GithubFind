package com.sriyank.javatokotlindemo.models

import io.realm.RealmObject

open class Owner(var id: Int=0, var login: String?=null) : RealmObject()
