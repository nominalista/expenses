package com.nominalista.expenses.data.room.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nominalista.expenses.data.model.Rule
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "rules")
data class RuleEntity(
        @PrimaryKey(autoGenerate = true) val id: Long,
        @ColumnInfo(name = "name") val name: List<String>,
        @ColumnInfo(name = "firstSymbol") val firstSymbol: String,
        @ColumnInfo(name = "decimalSeparator") val decimalSeparator: String,
        @ColumnInfo(name = "groupSeparator") val groupSeparator: String
) : Parcelable {

    fun mapToRule() = Rule(id = id.toString(), keywords = name, firstSymbol = firstSymbol, decimalSeparator = decimalSeparator, groupSeparator = groupSeparator)

    companion object {

        fun prepareForInsertion(rule: Rule) =
                RuleEntity(id = 0,
                        name = rule.keywords,
                        firstSymbol = rule.firstSymbol,
                        decimalSeparator = rule.decimalSeparator,
                        groupSeparator = rule.groupSeparator)

        fun prepareForUpdate(rule: Rule) =
                RuleEntity(id = rule.id.toLong(),
                        name = rule.keywords,
                        firstSymbol = rule.firstSymbol,
                        decimalSeparator = rule.decimalSeparator,
                        groupSeparator = rule.groupSeparator)

        fun fromRule(rule: Rule) = RuleEntity(id = rule.id.toLong(), name = rule.keywords, firstSymbol = rule.firstSymbol, decimalSeparator = rule.decimalSeparator, groupSeparator = rule.groupSeparator)
    }
}