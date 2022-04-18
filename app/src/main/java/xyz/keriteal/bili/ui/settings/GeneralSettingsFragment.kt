package xyz.keriteal.bili.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import xyz.keriteal.bili.R

class GeneralSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.general_preferences, rootKey)
    }
}