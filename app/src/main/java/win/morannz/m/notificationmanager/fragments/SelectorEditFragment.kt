package win.morannz.m.notificationmanager.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_selector_edit.*
import kotlinx.serialization.json.Json
import win.morannz.m.notificationmanager.*

class SelectorEditFragment : Fragment() {
    private var mNsId: Int = -1
    private var mNs = NotificationSelector()
    private var mNsBackup = NotificationSelector()
    private var mListener: OnFragmentInteractionListener? = null
    private var mAlertGroups = mapOf<Int, AlertGroup>()
    private var mAlertGroupsNameLookup = mapOf<String, Int>()
    private var mNotificationSelectors = mutableMapOf<Int, NotificationSelector>()
    private var mPackages = listOf<String>()
    private var mAutoSaveEnabled = true

    companion object {
        private val TAG = SelectorEditFragment::class.java.simpleName
        private const val NOTIFICATION_SELECTOR_ID = "notificationSelectorId"

        fun newInstance(notificationSelectorId: Int) = SelectorEditFragment().apply {
            arguments = Bundle().apply {
                putInt(NOTIFICATION_SELECTOR_ID, notificationSelectorId)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mNsId = arguments!!.getInt(NOTIFICATION_SELECTOR_ID)

        // load list of notification selectors and packages
        mNotificationSelectors = getNotificationSelectors(context!!).toMutableMap()
        mNs = mNotificationSelectors[mNsId] ?: NotificationSelector()
        mPackages = getPackagesWithNotifications(context!!)

        // deep copy the notification selector as a backup (so we can revert if the user cancels)
        mNsBackup = Json.parse(
            NotificationSelector.serializer(),
            Json.stringify(NotificationSelector.serializer(), mNs)
        )

        // load alert groups, create lookup map
        mAlertGroups = getAlertGroups(context!!)
        mAlertGroupsNameLookup = mAlertGroups.map { (id, ag) -> ag.name to id }.toMap()

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_selector_edit, container, false)
    }

    override fun onStart() {
        super.onStart()

        // update the action bar title
        activity?.setTitle(R.string.title_selector_edit)

        // populate existing data (or blank template if creating a new alert group)
        populateFields(mNs)

        // add options for alertGroup dropdown
        val alertGroupAdapter = ArrayAdapter(
            context!!,
            android.R.layout.simple_dropdown_item_1line,
            mAlertGroups.values.map { x -> x.name }
        )
        selector_edit_alert_group.setAdapter(alertGroupAdapter)

        // add options for alertGroup dropdown
        val packageNameAdapter = ArrayAdapter(
            context!!,
            android.R.layout.simple_dropdown_item_1line,
            mPackages
        )
        selector_edit_package_name.setAdapter(packageNameAdapter)

        // add textWatchers
        registerWatchers()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.selector_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle item selection
        return when (item.itemId) {
            R.id.action_selector_edit_cancel_edit -> cancelEdit()
            R.id.action_selector_edit_delete -> deleteNotificationSelector()
            else -> super.onOptionsItemSelected(item)
        }
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(type: String, data: Any)
    }

    private fun EditText.saveAfterTextChanged(updateNsField: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            // auto-save when user types in the text fields
            override fun afterTextChanged(editable: Editable?) {
                if (mAutoSaveEnabled) {
                    // call function passed in by user, e.g. { mNs.name = it }
                    updateNsField.invoke(editable.toString())

                    // update & save notification selector
                    mNotificationSelectors[mNsId] = mNs
                    saveNotificationSelectors(context!!, mNotificationSelectors)
                }
            }
        })
    }

    private fun registerWatchers() {
        // auto-save when user types in the text fields
        selector_edit_name.saveAfterTextChanged { mNs.name = it }
        selector_edit_comment.saveAfterTextChanged { mNs.comment = it }
        selector_edit_alert_group.saveAfterTextChanged { mNs.alertGroupId = mAlertGroupsNameLookup[it] }
        selector_edit_package_name.saveAfterTextChanged { mNs.packageName = it }
        selector_edit_match_title.saveAfterTextChanged { mNs.matchTitle = it }
        selector_edit_match_text.saveAfterTextChanged { mNs.matchText = it }
        selector_edit_min_secs_between_alerts.saveAfterTextChanged {
            if (it != "") mNs.minSecsBetweenAlerts = it.toInt()
        }
    }

    private fun populateFields(ns: NotificationSelector) {
        // load existing stored data into the text fields
        mAutoSaveEnabled = false
        selector_edit_name.setText(ns.name)
        selector_edit_comment.setText(ns.comment)
        selector_edit_alert_group.setText(mAlertGroups[ns.alertGroupId]?.name)
        selector_edit_package_name.setText(ns.packageName)
        selector_edit_match_title.setText(ns.matchTitle)
        selector_edit_match_text.setText(ns.matchText)
        selector_edit_min_secs_between_alerts.setText(ns.minSecsBetweenAlerts.toString())
        mAutoSaveEnabled = true
    }

    private fun cancelEdit(): Boolean {
        // load notification selector backup
        populateFields(mNsBackup)
        mNs = Json.parse(
            NotificationSelector.serializer(),
            Json.stringify(NotificationSelector.serializer(), mNsBackup)
        )

        // save the reverted notification selector
        mNotificationSelectors[mNsId] = mNs
        saveNotificationSelectors(context!!, mNotificationSelectors)
        return true
    }

    private fun deleteNotificationSelector(): Boolean {
        // delete notification selector from list & then save
        mNotificationSelectors.remove(mNsId)
        saveNotificationSelectors(context!!, mNotificationSelectors)

        // leave edit fragment
        activity!!.onBackPressed()
        return true
    }
}
