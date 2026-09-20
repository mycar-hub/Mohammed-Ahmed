package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.UserProfile
import com.example.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "maitre_user_preferences")

enum class ThemeMode(val labelAr: String) {
  LIGHT("المظهر الفاتح"),
  DARK("المظهر الداكن"),
  SYSTEM("حسب مظهر النظام")
}

data class AppPreferences(
  val themeMode: ThemeMode = ThemeMode.LIGHT,
  val notificationsEnabled: Boolean = true,
  val biometricAuthEnabled: Boolean = false,
  val soundAlertsEnabled: Boolean = true
)

class UserPreferencesRepository private constructor(private val dataStore: DataStore<Preferences>) {

  private object PreferencesKeys {
    val USER_ID = stringPreferencesKey("user_id")
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val USER_PHONE = stringPreferencesKey("user_phone")
    val USER_ROLE = stringPreferencesKey("user_role")
    val USER_BALANCE = doublePreferencesKey("user_balance")
    val LICENSE_NUMBER = stringPreferencesKey("license_number")
    val IS_VERIFIED = booleanPreferencesKey("is_verified")
    val PENDING_VERIFICATION = booleanPreferencesKey("pending_verification")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val NATIONAL_ID_OR_CR = stringPreferencesKey("national_id_or_cr")
    val COMPANY_NAME = stringPreferencesKey("company_name")
    val NAFATH_VERIFIED = booleanPreferencesKey("nafath_verified")
    val OFFICE_ADDRESS = stringPreferencesKey("office_address")

    // Theme and App Settings
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val BIOMETRIC_AUTH_ENABLED = booleanPreferencesKey("biometric_auth_enabled")
    val SOUND_ALERTS_ENABLED = booleanPreferencesKey("sound_alerts_enabled")
  }

  /**
   * Flow of UserProfile observed from DataStore preferences.
   */
  val userProfileFlow: Flow<UserProfile> = dataStore.data
    .catch { exception ->
      if (exception is IOException) {
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }
    .map { preferences ->
      mapUserProfile(preferences)
    }

  /**
   * Flow of App Preferences (Theme mode, notifications, etc.)
   */
  val appPreferencesFlow: Flow<AppPreferences> = dataStore.data
    .catch { exception ->
      if (exception is IOException) {
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }
    .map { preferences ->
      val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.LIGHT.name
      val themeMode = try {
        ThemeMode.valueOf(themeModeStr)
      } catch (e: Exception) {
        ThemeMode.LIGHT
      }
      val notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
      val biometricAuthEnabled = preferences[PreferencesKeys.BIOMETRIC_AUTH_ENABLED] ?: false
      val soundAlertsEnabled = preferences[PreferencesKeys.SOUND_ALERTS_ENABLED] ?: true

      AppPreferences(
        themeMode = themeMode,
        notificationsEnabled = notificationsEnabled,
        biometricAuthEnabled = biometricAuthEnabled,
        soundAlertsEnabled = soundAlertsEnabled
      )
    }

  private fun mapUserProfile(preferences: Preferences): UserProfile {
    val id = preferences[PreferencesKeys.USER_ID] ?: "user_client_1"
    val name = preferences[PreferencesKeys.USER_NAME] ?: "م. شريف عبد الفتاح التميمي"
    val email = preferences[PreferencesKeys.USER_EMAIL] ?: "sherif.tamimi@example.com"
    val phone = preferences[PreferencesKeys.USER_PHONE] ?: "+20 100 123 4567"
    val roleStr = preferences[PreferencesKeys.USER_ROLE] ?: UserRole.CLIENT.name
    val role = try {
      UserRole.valueOf(roleStr)
    } catch (e: Exception) {
      UserRole.CLIENT
    }
    val balance = preferences[PreferencesKeys.USER_BALANCE] ?: 4500.00
    val licenseNumber = preferences[PreferencesKeys.LICENSE_NUMBER]
    val isVerified = preferences[PreferencesKeys.IS_VERIFIED] ?: true
    val pendingVerification = preferences[PreferencesKeys.PENDING_VERIFICATION] ?: false
    val isLoggedIn = preferences[PreferencesKeys.IS_LOGGED_IN] ?: true
    val nationalIdOrCr = preferences[PreferencesKeys.NATIONAL_ID_OR_CR] ?: "29408151203948"
    val companyName = preferences[PreferencesKeys.COMPANY_NAME] ?: "شركة النيل للحلول الرقمية"
    val nafathVerified = preferences[PreferencesKeys.NAFATH_VERIFIED] ?: true
    val officeAddress = preferences[PreferencesKeys.OFFICE_ADDRESS] ?: "القاهرة - التجمع الخامس - شارع التسعين الشمالي"

    return UserProfile(
      id = id,
      name = name,
      email = email,
      phone = phone,
      role = role,
      balance = balance,
      licenseNumber = licenseNumber,
      isVerified = isVerified,
      pendingVerification = pendingVerification,
      isLoggedIn = isLoggedIn,
      nationalIdOrCr = nationalIdOrCr,
      companyName = companyName,
      nafathVerified = nafathVerified,
      officeAddress = officeAddress
    )
  }

  /**
   * Saves or updates the entire User Profile and session.
   */
  suspend fun saveUserSession(user: UserProfile) {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.USER_ID] = user.id
      preferences[PreferencesKeys.USER_NAME] = user.name
      preferences[PreferencesKeys.USER_EMAIL] = user.email
      preferences[PreferencesKeys.USER_PHONE] = user.phone
      preferences[PreferencesKeys.USER_ROLE] = user.role.name
      preferences[PreferencesKeys.USER_BALANCE] = user.balance
      if (user.licenseNumber != null) {
        preferences[PreferencesKeys.LICENSE_NUMBER] = user.licenseNumber
      } else {
        preferences.remove(PreferencesKeys.LICENSE_NUMBER)
      }
      preferences[PreferencesKeys.IS_VERIFIED] = user.isVerified
      preferences[PreferencesKeys.PENDING_VERIFICATION] = user.pendingVerification
      preferences[PreferencesKeys.IS_LOGGED_IN] = user.isLoggedIn
      preferences[PreferencesKeys.NATIONAL_ID_OR_CR] = user.nationalIdOrCr
      if (user.companyName != null) {
        preferences[PreferencesKeys.COMPANY_NAME] = user.companyName
      } else {
        preferences.remove(PreferencesKeys.COMPANY_NAME)
      }
      preferences[PreferencesKeys.NAFATH_VERIFIED] = user.nafathVerified
      if (user.officeAddress != null) {
        preferences[PreferencesKeys.OFFICE_ADDRESS] = user.officeAddress
      } else {
        preferences.remove(PreferencesKeys.OFFICE_ADDRESS)
      }
    }
  }

  /**
   * Switches user role and persists immediately.
   */
  suspend fun updateUserRole(role: UserRole) {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.USER_ROLE] = role.name
    }
  }

  /**
   * Updates user balance in persistent storage.
   */
  suspend fun updateBalance(balance: Double) {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.USER_BALANCE] = balance
    }
  }

  /**
   * Updates lawyer verification status and license number.
   */
  suspend fun updateVerification(isVerified: Boolean, isPending: Boolean, licenseNumber: String?) {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.IS_VERIFIED] = isVerified
      preferences[PreferencesKeys.PENDING_VERIFICATION] = isPending
      if (licenseNumber != null) {
        preferences[PreferencesKeys.LICENSE_NUMBER] = licenseNumber
      }
    }
  }

  /**
   * Updates Theme Mode (LIGHT, DARK, SYSTEM).
   */
  suspend fun setThemeMode(mode: ThemeMode) {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.THEME_MODE] = mode.name
    }
  }

  /**
   * Updates Notifications Toggle.
   */
  suspend fun setNotificationsEnabled(enabled: Boolean) {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
    }
  }

  /**
   * Updates Biometric Auth Toggle.
   */
  suspend fun setBiometricAuthEnabled(enabled: Boolean) {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.BIOMETRIC_AUTH_ENABLED] = enabled
    }
  }

  /**
   * Updates Sound Alerts Toggle.
   */
  suspend fun setSoundAlertsEnabled(enabled: Boolean) {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.SOUND_ALERTS_ENABLED] = enabled
    }
  }

  /**
   * Logs out user and resets session state.
   */
  suspend fun logout() {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.IS_LOGGED_IN] = false
    }
  }

  /**
   * Restores initial default session for demo/quick test.
   */
  suspend fun restoreSession() {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.IS_LOGGED_IN] = true
    }
  }

  companion object {
    @Volatile
    private var INSTANCE: UserPreferencesRepository? = null

    fun getInstance(context: Context): UserPreferencesRepository {
      return INSTANCE ?: synchronized(this) {
        val instance = UserPreferencesRepository(context.applicationContext.userPreferencesDataStore)
        INSTANCE = instance
        instance
      }
    }
  }
}
