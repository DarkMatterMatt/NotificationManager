package win.morannz.m.notificationmanager

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService.requestRebind
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import win.morannz.m.notificationmanager.fragments.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import kotlinx.serialization.json.Json


class MainActivity : AppCompatActivity(),
    SelectorsFragment.OnFragmentInteractionListener,
    SelectorsListFragment.OnListFragmentInteractionListener,
    SelectorEditFragment.OnFragmentInteractionListener,
    AlertsFragment.OnFragmentInteractionListener,
    AlertsListFragment.OnListFragmentInteractionListener,
    AlertEditFragment.OnFragmentInteractionListener,
    RecentsFragment.OnFragmentInteractionListener,
    RecentsListFragment.OnListFragmentInteractionListener {
    //RecentViewFragment.OnFragmentInteractionListener {

    private var localBroadcastManager : LocalBroadcastManager? = null

    private val broadcastReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == C.REFRESH_RECENTS_LIST_INTENT) {
                val rnString = intent.getStringExtra(C.RECENT_NOTIFICATION)
                val rn = Json.parse(RecentNotification.serializer(), rnString)
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment)?.childFragmentManager?.findFragmentById(R.id.fragment_recent_list)
                if (fragment !== null && fragment is RecentsListFragment) {
                    Log.d(C.TAG, "Refresh recents list")
                    fragment.refreshWithNewNotification(rn)
                }
            }
        }
    }

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
            showNotificationServiceAlertDialog()
        }

        restartNotificationService()

        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(C.REFRESH_RECENTS_LIST_INTENT)
        localBroadcastManager?.registerReceiver(broadcastReciever, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        localBroadcastManager?.unregisterReceiver(broadcastReciever);
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
            C.NEW_ALERT_GROUP -> {
                var maxIndex = getAlertGroupMaxIndex(this)
                saveAlertGroupMaxIndex(this, ++maxIndex)

                val b = Bundle()
                b.putBoolean(C.NEW_NOTIFICATION_SELECTOR, true)
                b.putInt(C.ALERT_GROUP, maxIndex)
                navigate(FragmentId.ALERT_EDIT, b)
            }
            C.ALERT_GROUP -> {
                val b = Bundle()
                b.putInt(C.ALERT_GROUP, data as Int)
                navigate(FragmentId.ALERT_EDIT, b)
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
            FragmentId.ALERT_EDIT -> AlertEditFragment()
            else -> return false
        }
        // get destination title
        val destTitle = when (destId) {
            FragmentId.RECENTS -> R.string.title_recents
            FragmentId.SELECTORS -> R.string.title_selectors
            FragmentId.SELECTOR_EDIT -> R.string.title_selector_edit
            FragmentId.ALERTS -> R.string.title_alerts
            FragmentId.ALERT_EDIT -> R.string.title_alert_edit
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

    private fun restartNotificationService() {
        // get component name
        val componentName = ComponentName(this, NotificationManagerService::class.java)

        // toggle disable/enable
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        // request rebind
        if (Build.VERSION.SDK_INT >= 24) {
            requestRebind(componentName)
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
    private fun showNotificationServiceAlertDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.notification_listener_service)
            .setMessage(R.string.notification_listener_service_explanation)
            .setPositiveButton(R.string.yes) { _, _ -> startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }
            .setNegativeButton(R.string.no) { dialog, id ->
                // If you choose to not enable the notification listener
                // the app. will not work as expected
            }
            .show()
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
