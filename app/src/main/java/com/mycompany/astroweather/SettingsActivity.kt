package com.mycompany.astroweather

import android.os.Bundle
import android.text.InputType.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val inputFlags = TYPE_CLASS_NUMBER or TYPE_NUMBER_FLAG_DECIMAL or TYPE_NUMBER_FLAG_SIGNED

            mapOf("latitude" to 90, "longitude" to 180).forEach { (key, value) ->
                findPreference<EditTextPreference>(key)?.apply {
                    setOnBindEditTextListener { it.inputType = inputFlags }
                    setOnPreferenceChangeListener { preference, newValue ->
                        val input = newValue.toString().toDouble()
                        if (input >= -value && input <= value) {
                            preference.summary = input.toString()
                            true
                        } else {
                            Toast.makeText(activity, "$key must be between ${-value} and $value",
                                Toast.LENGTH_LONG).show()
                            false
                        }
                    }
                }
            }
        }
    }
}
