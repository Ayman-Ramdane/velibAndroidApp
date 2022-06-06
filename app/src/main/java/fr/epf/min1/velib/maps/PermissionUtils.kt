package fr.epf.min1.velib.maps

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

object PermissionUtils {

    @JvmStatic
    fun requestPermission(
        activity: AppCompatActivity, requestId: Int,
        permission: String) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
            requestId
        )

    }

    @JvmStatic
    fun isPermissionGranted(
        grantPermissions: Array<String>, grantResults: IntArray,
        permission: String
    ): Boolean {
        for (i in grantPermissions.indices) {
            if (permission == grantPermissions[i]) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }
}