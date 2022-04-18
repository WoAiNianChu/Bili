package xyz.keriteal.bili.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import xyz.keriteal.bili.R

class PlayerSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.player_preferences, rootKey)
    }
}