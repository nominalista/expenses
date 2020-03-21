package com.nominalista.expenses.data.model

import android.os.Parcelable
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Rule(val id: String, val name: String, val firstSymbol: String, val lastSymbol: String, val decimalSeparator: String, val groupSeparator: String) : Parcelable {
    constructor(doc: QueryDocumentSnapshot) : this(
            id = "",
            name = doc.getString("name")!!,
            firstSymbol = doc.getString("firstSymbol")!!,
            lastSymbol = doc.getString("lastSymbol")!!,
            decimalSeparator = doc.getString("decimalSeparator")!!,
            groupSeparator = doc.getString("groupSeparator")!!
    )
}