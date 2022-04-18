package xyz.keriteal.bili.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.setFragmentResult
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import xyz.keriteal.bili.R

class NetworkSettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var sp: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.network_preferences, rootKey)
        sp = PreferenceManager.getDefaultSharedPreferences(this.requireContext())
        sp.registerOnSharedPreferenceChangeListener(this)
        val proxyUsernamePreference = findPreference<EditTextPreference>("pref_proxy_auth_username")
        val proxyPasswordPreference = findPreference<EditTextPreference>("pref_proxy_auth_password")
    }

    override fun onPause() {
        super.onPause()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

    }
}