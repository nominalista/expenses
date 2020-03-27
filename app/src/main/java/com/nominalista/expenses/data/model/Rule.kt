package com.nominalista.expenses.data.model

import android.os.Parcelable
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Rule(val id: String, val keywords: List<String>, val firstSymbol: String, val decimalSeparator: String, val groupSeparator: String) : Parcelable {
    constructor(doc: QueryDocumentSnapshot) : this(
            id = "",
            keywords = doc.getString("name")!!.split("\n"),
            firstSymbol = doc.getString("firstSymbol")!!,
            decimalSeparator = doc.getString("decimalSeparator")!!,
            groupSeparator = doc.getString("groupSeparator")!!
    )
}

data class Format(val hint: String, val groupSeparator: String, val decimalSeparator: String)