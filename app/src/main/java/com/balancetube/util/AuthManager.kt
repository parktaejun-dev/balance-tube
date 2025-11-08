package com.balancetube.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/youtube.readonly"))
            .requestServerAuthCode(getWebClientId())
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    private fun getWebClientId(): String {
        // This will be replaced with actual client ID from google-services.json
        return context.getString(
            context.resources.getIdentifier(
                "default_web_client_id",
                "string",
                context.packageName
            )
        )
    }

    fun getSignInClient(): GoogleSignInClient = googleSignInClient

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    suspend fun getAccessToken(): String? {
        val account = getLastSignedInAccount() ?: return null
        return try {
            // Get fresh token
            val token = GoogleSignIn
                .getAccountForExtension(context, GoogleSignInOptions.DEFAULT_SIGN_IN)
                .getRequestedScopes()

            // In production, you would use GoogleAuthUtil.getToken() here
            // For now, we'll use the server auth code approach
            account.serverAuthCode
        } catch (e: Exception) {
            null
        }
    }

    fun saveUserEmail(email: String) {
        val hashedEmail = hashEmail(email)
        encryptedPrefs.edit()
            .putString(KEY_USER_EMAIL_HASH, hashedEmail)
            .apply()
    }

    fun getUserEmailHash(): String? {
        return encryptedPrefs.getString(KEY_USER_EMAIL_HASH, null)
    }

    private fun hashEmail(email: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(email.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
            encryptedPrefs.edit().clear().apply()
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun isSignedIn(): Boolean {
        return getLastSignedInAccount() != null
    }

    companion object {
        private const val KEY_USER_EMAIL_HASH = "user_email_hash"
    }
}
