package win.morannz.m.notificationmanager

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService.requestRebind
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.json.Json
import win.morannz.m.notificationmanager.fragments.*
import android.view.Menu


class MainActivity : AppCompatActivity(),
    SelectorsFragment.OnFragmentInteractionListener,
    SelectorsListFragment.OnListFragmentInteractionListener,
    SelectorEditFragment.OnFragmentInteractionListener,
    AlertsFragment.OnFragmentInteractionListener,
    AlertsListFragment.OnListFragmentInteractionListener,
    //AlertEditFragment.OnFragmentInteractionListener,
    RecentsFragment.OnFragmentInteractionListener,
    RecentsListFragment.OnListFragmentInteractionListener {
    //RecentViewFragment.OnFragmentInteractionListener {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener {
        navigate(
            when (it.itemId) {
                R.id.navigation_recents -> FragmentId.RECENTS
                R.id.navigation_selectors -> FragmentId.SELECTORS
                R.id.navigation_alerts -> FragmentId.ALERTS
                else -> return@OnNavigationItemSelectedListener false
            }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            navigate(FragmentId.SETTINGS)
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigate(FragmentId.RECENTS)

        // prompt user to enable the notification listener service
        if (!isNotificationServiceEnabled()) {
            buildNotificationServiceAlertDialog().show()
        }

        // Alert Groups
        val alertGroups = mutableMapOf<Int, AlertGroup>() //getAlertGroups(this)
        var agMaxIndex = -1 //getAlertGroupMaxIndex(this)
        for (i in 0..19) {
            alertGroups.put(
                ++agMaxIndex, AlertGroup(
                    name = "Alert Group $i",
                    vibrationPattern = longArrayOf(
                        0,
                        i * 100L,
                        2000 - i * 100L,
                        i * 100L,
                        2000 - i * 100L
                    ).joinToString(",")
                )
            )
        }
        saveAlertGroupMaxIndex(this, agMaxIndex)
        saveAlertGroups(this, alertGroups)

        // Notification Selectors
        val notificationSelectors = mutableMapOf<Int, NotificationSelector>() //getNotificationSelectors(this)
        var nsMaxIndex = -1 //getNotificationSelectorMaxIndex(this)
        for (i in 0..19) {
            notificationSelectors.put(
                ++nsMaxIndex, NotificationSelector(
                    alertGroupId = i,
                    name = "Pushbullet NS $i",
                    packageName = "com.pushbullet.android",
                    matchText = """yeet $i"""
                )
            )
        }
        saveNotificationSelectorMaxIndex(this, nsMaxIndex)
        saveNotificationSelectors(this, notificationSelectors)

        restartNotificationService()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onListFragmentInteraction(type: String, item: Any) {
        // onFragmentInteraction handles everything
        onFragmentInteraction(type, item)
    }

    override fun onFragmentInteraction(type: String, data: Any) {
        when (type) {
            C.NEW_NOTIFICATION_SELECTOR -> {
                var maxIndex = getNotificationSelectorMaxIndex(this)
                saveNotificationSelectorMaxIndex(this, ++maxIndex)

                val b = Bundle()
                b.putBoolean(C.NEW_NOTIFICATION_SELECTOR, true)
                b.putInt(C.NOTIFICATION_SELECTOR, maxIndex)
                navigate(FragmentId.SELECTOR_EDIT, b)
            }
            C.NOTIFICATION_SELECTOR -> {
                val b = Bundle()
                b.putInt(C.NOTIFICATION_SELECTOR, data as Int)
                navigate(FragmentId.SELECTOR_EDIT, b)
            }
        }
    }

    fun navigate(destId: FragmentId, bundle: Bundle? = null): Boolean {
        // get destination fragment
        val destFragment = when (destId) {
            FragmentId.RECENTS -> RecentsFragment()
            FragmentId.SELECTORS -> SelectorsFragment()
            FragmentId.SELECTOR_EDIT -> SelectorEditFragment()
            FragmentId.ALERTS -> AlertsFragment()
            else -> return false
        }
        // get destination title
        val destTitle = when (destId) {
            FragmentId.RECENTS -> R.string.title_recents
            FragmentId.SELECTORS -> R.string.title_selectors
            FragmentId.SELECTOR_EDIT -> R.string.title_selector_edit
            FragmentId.ALERTS -> R.string.title_alerts
            else -> return false
        }
        // check whether to support the "back button"
        val addToBackStack = when (destId) {
            FragmentId.RECENTS,
            FragmentId.SELECTORS,
            FragmentId.ALERTS -> false
            else -> true
        }

        // add any data to send
        destFragment.arguments = bundle

        // perform fragment swap
        val t = supportFragmentManager.beginTransaction()
        if (addToBackStack) {
            t.addToBackStack(null)
        }
        t.replace(R.id.fragment, destFragment)
        t.commit()

        // change the title
        setTitle(destTitle)
        return true
    }

    fun restartNotificationService() {
        // get component name
        val cn = ComponentName(this, NotificationManagerService::class.java)

        // toggle disable/enable
        packageManager.setComponentEnabledSetting(
            cn,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
            cn,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        // request rebind
        if (Build.VERSION.SDK_INT >= 24) {
            requestRebind(cn)
        }
    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if enabled, false otherwise.
     */
    private fun isNotificationServiceEnabled(): Boolean {
        // loop through all packages that have enabled notification listeners and check if we're there
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && TextUtils.equals(pkgName, cn.packageName)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private fun buildNotificationServiceAlertDialog(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.notification_listener_service)
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation)
        alertDialogBuilder.setPositiveButton(R.string.yes) { dialog, id -> startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }
        alertDialogBuilder.setNegativeButton(R.string.no) { dialog, id ->
            // If you choose to not enable the notification listener
            // the app. will not work as expected
        }
        return alertDialogBuilder.create()
    }

    /*private fun notificationListenerServiceIsRunning(): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in am.getRunningServices(Integer.MAX_VALUE)) {
            if (NotificationManager3Service.name == service.service.className) {
                Log.i("NM3", "notificationListenerServiceIsRunning: " + true)
                return true
            }
        }
        Log.i("NM3", "notificationListenerServiceIsRunning: " + false)
        return false
    }*/
}
