package app.accrescent.client.data.appmanager

import android.os.Bundle

private const val KEY_APP_ID = "appId"
private const val KEY_SESSION_ID = "sessionId"
private const val KEY_INSTALL_TYPE = "installType"

// We don't implement Parcelable on this class directly because sending it across process boundaries
// (as is done in PackageInstaller.Session.commit()) can cause it to be stripped from Intent extras.
// On Android 12L and below, this behavior would cause our session commit broadcast receiver to fail
// to receive EXTRA_INSTALL_APP_REQUEST.
//
// See https://developer.android.com/guide/components/activities/parcelables-and-bundles#sdbp for
// more information.
data class InstallAppRequest(
    val appId: String,
    val sessionId: Int,
    val installType: InstallType,
) {
    companion object {
        fun fromBundle(bundle: Bundle): InstallAppRequest? {
            val appId = bundle.getString(KEY_APP_ID) ?: return null
            val sessionId = bundle.getInt(KEY_SESSION_ID).takeIf { it != 0 } ?: return null
            val installType = bundle
                .getString(KEY_INSTALL_TYPE)
                ?.let { InstallType.valueOf(it) }
                ?: return null

            return InstallAppRequest(appId = appId, sessionId = sessionId, installType = installType)
        }
    }

    fun toBundle(): Bundle = Bundle().apply {
        putString(KEY_APP_ID, appId)
        putInt(KEY_SESSION_ID, sessionId)
        putString(KEY_INSTALL_TYPE, installType.name)
    }
}
