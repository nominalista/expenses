package com.nominalista.expenses

import androidx.test.runner.AndroidJUnit4
import com.nominalista.expenses.util.extensions.toDate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat

@RunWith(AndroidJUnit4::class)
class LocalDateTest {

    @Test
    fun convertLocalDateToDate() {
        // Given
        val dateToParse = "2020-01-01"
        val datePattern = "yyyy-MM-dd"
        // When
        val date = LocalDate.parse(dateToParse, DateTimeFormatter.ofPattern(datePattern)).toDate()
        val formattedDate = SimpleDateFormat(datePattern).format(date)
        // Then
        assertEquals(dateToParse, formattedDate)
    }
}