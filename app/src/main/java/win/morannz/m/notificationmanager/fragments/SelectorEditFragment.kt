package win.morannz.m.notificationmanager.fragments

import android.os.Bundle
import android.content.SharedPreferences
import androidx.preference.PreferenceFragmentCompat
import win.morannz.m.notificationmanager.C
import win.morannz.m.notificationmanager.NotificationSelector
import win.morannz.m.notificationmanager.R
import win.morannz.m.notificationmanager.getNotificationSelectors

/**
 * shows the settings option for choosing the movie categories in ListPreference.
 */
class SelectorEditFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var ns: NotificationSelector? = null

    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.fragment_selector_edit, rootKey)

        val notificationSelectors = getNotificationSelectors(activity!!.applicationContext)
        val nsId = arguments?.getInt(C.NOTIFICATION_SELECTOR, -1)
        ns = if (nsId != -1) notificationSelectors[nsId] else NotificationSelector()
    }

    override fun onResume() {
        super.onResume()
        // register the preferenceChange listener
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
    }

    override fun onPause() {
        super.onPause()
        //unregister the preference change listener
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SelectorsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(nsId: Int) = SelectorsFragment().apply {
            arguments = Bundle().apply {
                putInt(C.NOTIFICATION_SELECTOR, nsId)
            }
        }
    }
}