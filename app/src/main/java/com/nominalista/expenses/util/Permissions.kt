package com.nominalista.expenses.util

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat.checkSelfPermission

fun isPermissionGranted(context: Context, permission: String)
        = checkSelfPermission(context, permission) == PERMISSION_GRANTED

fun isGranted(grantResults: IntArray) = grantResults.all { it == PERMISSION_GRANTED }