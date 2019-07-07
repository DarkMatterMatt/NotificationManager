package win.morannz.m.notificationmanager

import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService.requestRebind
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.json.Json
import win.morannz.m.notificationmanager.fragments.*


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
    companion object {
        private val TAG = this::class.java.simpleName
    }

    private lateinit var mLocalBroadcastManager: LocalBroadcastManager
    private lateinit var mCurrentFragment: Fragment

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == RecentsFragment.INTENT_UPDATE) {
                val rnString = intent.getStringExtra(RecentsFragment.UPDATE_DATA)
                val rn = Json.parse(RecentNotification.serializer(), rnString)
                val fragment = mCurrentFragment
                if (fragment is RecentsFragment) {
                    val recentsListFragment = fragment.childFragmentManager.findFragmentById(R.id.fragment_recents_list)
                    if (recentsListFragment is RecentsListFragment) {
                        recentsListFragment.updateList(rn)
                    }
                }
            }
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener {
        when (it.itemId) {
            R.id.navigation_recents -> navigate(RecentsFragment.newInstance(), R.string.title_recents)
            R.id.navigation_selectors -> navigate(SelectorsFragment.newInstance(), R.string.title_selectors)
            R.id.navigation_alerts -> navigate(AlertsFragment.newInstance(), R.string.title_alerts)
            else -> return@OnNavigationItemSelectedListener false
        }
        true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            //navigate(FragmentId.SETTINGS)
            true
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
        navigate(RecentsFragment.newInstance(), R.string.title_notification_manager)

        // prompt user to enable the notification listener service
        if (!isNotificationServiceEnabled()) {
            showNotificationServiceAlertDialog()
        }

        restartNotificationService()

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, IntentFilter().apply {
            addAction(RecentsFragment.INTENT_UPDATE)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver)
    }

    override fun onBackPressed() {
        if (mCurrentFragment is AlertEditFragment) {
            navigate(AlertsFragment.newInstance(), R.string.title_alerts)
        } else if (mCurrentFragment is SelectorEditFragment) {
            navigate(SelectorsFragment.newInstance(), R.string.title_selectors)
        } else {
            super.onBackPressed()
        }
    }

    override fun onListFragmentInteraction(type: String, item: Any) {
        // onFragmentInteraction handles everything
        onFragmentInteraction(type, item)
    }

    override fun onFragmentInteraction(type: String, data: Any) {
        when (type) {
            SelectorsFragment.INTERACTION -> {
                val destFragment = SelectorEditFragment.newInstance(data as Int)
                navigate(destFragment, R.string.title_selector_edit)
            }
            AlertsFragment.INTERACTION -> {
                val destFragment = AlertEditFragment.newInstance(data as Int)
                navigate(destFragment, R.string.title_alert_edit)
            }
        }
    }

    private fun navigate(destFragment: Fragment, destTitle: Int? = null) {
        val addToBackStack = false

        // save the new fragment
        mCurrentFragment = destFragment

        // perform fragment swap
        val t = supportFragmentManager.beginTransaction()
        if (addToBackStack) {
            t.addToBackStack(null)
        }
        t.replace(R.id.fragment, destFragment, destFragment::class.java.canonicalName)
        t.commit()

        // change the title
        if (destTitle !== null) setTitle(destTitle)
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
        MaterialAlertDialogBuilder(this).apply {
            setTitle(R.string.notification_listener_service)
            setMessage(R.string.notification_listener_service_explanation)
            setPositiveButton(R.string.yes) { _, _ -> startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }
            setNegativeButton(R.string.no) { dialog, id ->
                // If you choose to not enable the notification listener
                // the app. will not work as expected
            }
            show()
        }
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
