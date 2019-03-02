package win.morannz.m.notificationmanager

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.provider.Settings
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_main.*
import win.morannz.m.notificationmanager.fragments.*
import android.content.pm.PackageManager
import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService.requestRebind


class MainActivity : AppCompatActivity(),
    SelectorsListFragment.OnListFragmentInteractionListener,
    AlertsListFragment.OnListFragmentInteractionListener,
    RecentsListFragment.OnListFragmentInteractionListener,
    SelectorsFragment.OnFragmentInteractionListener,
    AlertsFragment.OnFragmentInteractionListener,
    RecentsFragment.OnFragmentInteractionListener {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { navigate(it.itemId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigate(R.id.navigation_recents)

        // prompt user to enable the notification listener service
        if (!isNotificationServiceEnabled()) {
            buildNotificationServiceAlertDialog().show()
        }

        // TODO: Remove this
        // Alert Groups
        val alertGroups = mutableMapOf<Int, AlertGroup>() //getAlertGroups(this)
        var agMaxIndex = -1 //getAlertGroupMaxIndex(this)
        for (i in 0..19) {
            alertGroups.put(++agMaxIndex, AlertGroup(
                name = "Alert Group " + i,
                vibrationPattern = longArrayOf(0, i*100L, 2000 - i*100L, i*100L, 2000 - i*100L).joinToString(",")
            ))
        }
        saveAlertGroupMaxIndex(this, agMaxIndex)
        saveAlertGroups(this, alertGroups)

        // Notification Selectors
        val notificationSelectors = mutableMapOf<Int, NotificationSelector>() //getNotificationSelectors(this)
        var nsMaxIndex = -1 //getNotificationSelectorMaxIndex(this)
        for (i in 0..19) {
            notificationSelectors.put(++nsMaxIndex, NotificationSelector(
                alertGroupId = i,
                name = "Pushbullet NS " + i,
                packageName = "com.pushbullet.android",
                matchText = """yeet """ + i
            ))
        }
        saveNotificationSelectorMaxIndex(this, nsMaxIndex)
        saveNotificationSelectors(this, notificationSelectors)

        restartNotificationService()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onListFragmentInteraction(type: String, item: Any) {

    }

    override fun onFragmentInteraction(type: String, data: Any) {

    }

    fun navigate(destId: Int, bundle: Bundle? = null) : Boolean {
        // get destination fragment
        val destFragment = when (destId) {
            R.id.navigation_recents -> RecentsFragment()
            R.id.navigation_selectors -> SelectorsFragment()
            R.id.navigation_alerts -> AlertsFragment()
            else -> return false
        }
        // get destination title
        val destTitle = when (destId) {
            R.id.navigation_recents -> R.string.title_recents
            R.id.navigation_selectors -> R.string.title_selectors
            R.id.navigation_alerts -> R.string.title_alerts
            else -> return false
        }
        // check whether to support the "back button"
        val addToBackStack = when (destId) {
            R.id.navigation_recents,
            R.id.navigation_selectors,
            R.id.navigation_alerts -> false
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
        toolbar.setTitle(destTitle)
        return true
    }

    fun restartNotificationService() {
        // get component name
        val cn = ComponentName(this, NotificationManager3Service::class.java)

        // toggle disable/enable
        packageManager.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        packageManager.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)

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
    private fun isNotificationServiceEnabled() : Boolean {
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
