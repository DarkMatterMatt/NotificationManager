package win.morannz.m.notificationmanager.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_selector_edit.*
import kotlinx.serialization.json.Json
import win.morannz.m.notificationmanager.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SelectorEdit.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SelectorEdit.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class SelectorEditFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var nsId: Int = -1
    private var ns: NotificationSelector = NotificationSelector()
    private var nsBackup: NotificationSelector = NotificationSelector()
    private var createNew: Boolean = false
    private var listener: OnFragmentInteractionListener? = null
    private var alertGroups: MutableMap<Int, AlertGroup> = mutableMapOf()
    private var alertGroupsNameLookup: MutableMap<String, Int> = mutableMapOf()
    private var notificationSelectors: MutableMap<Int, NotificationSelector> = mutableMapOf()
    private var packages: MutableList<String> = mutableListOf()
    private var textWatchersEnabled: Boolean = true

    private fun EditText.saveAfterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                if (textWatchersEnabled) {
                    afterTextChanged.invoke(editable.toString())
                    notificationSelectors[nsId] = ns
                    saveNotificationSelectors(activity!!.applicationContext, notificationSelectors)
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNew = arguments?.getBoolean(C.NEW_NOTIFICATION_SELECTOR) ?: false
        nsId = arguments?.getInt(C.NOTIFICATION_SELECTOR, -1) ?: -1

        // load list of notification selectors and packages
        notificationSelectors = getNotificationSelectors(activity!!.applicationContext)
        ns = notificationSelectors[nsId] ?: NotificationSelector()
        packages = getPackagesWithNotifications(activity!!.applicationContext)

        // deep copy the notificationSelector
        nsBackup = Json.parse(NotificationSelector.serializer(), Json.stringify(NotificationSelector.serializer(), ns))

        // load alert groups, create lookup map
        alertGroups = getAlertGroups(activity!!.applicationContext)
        for ((id, ag) in alertGroups) {
            alertGroupsNameLookup[ag.name] = id
        }

        setHasOptionsMenu(true)
    }

    private fun registerTextWatchers() {
        selector_edit_name.saveAfterTextChanged { ns.name = it }
        selector_edit_comment.saveAfterTextChanged { ns.comment = it }
        selector_edit_alert_group.saveAfterTextChanged { ns.alertGroupId = alertGroupsNameLookup[it] }
        selector_edit_package_name.saveAfterTextChanged { ns.packageName = it }
        selector_edit_match_title.saveAfterTextChanged { ns.matchTitle = it }
        selector_edit_match_text.saveAfterTextChanged { ns.matchText = it }
        selector_edit_min_millisecs_between_alerts.saveAfterTextChanged { ns.minMillisecsBetweenAlerts = it.toInt() }
    }

    private fun populateFields(ns: NotificationSelector) {
        textWatchersEnabled = false
        selector_edit_name.setText(ns.name)
        selector_edit_comment.setText(ns.comment)
        selector_edit_alert_group.setText(alertGroups[ns.alertGroupId]?.name)
        selector_edit_package_name.setText(ns.packageName)
        selector_edit_match_title.setText(ns.matchTitle)
        selector_edit_match_text.setText(ns.matchText)
        selector_edit_min_millisecs_between_alerts.setText(ns.minMillisecsBetweenAlerts.toString())
        textWatchersEnabled = true
    }

    override fun onStart() {
        super.onStart()

        // populate existing data
        if (!createNew) {
            populateFields(ns)
        }

        // add options for alertGroup dropdown
        val alertGroupAdapter = ArrayAdapter(
            activity!!.applicationContext,
            android.R.layout.simple_dropdown_item_1line,
            alertGroups.values.map { x -> x.name }
        )
        selector_edit_alert_group.setAdapter(alertGroupAdapter)

        // add options for alertGroup dropdown
        val packageNameAdapter = ArrayAdapter(
            activity!!.applicationContext,
            android.R.layout.simple_dropdown_item_1line,
            packages
        )
        selector_edit_package_name.setAdapter(packageNameAdapter)

        // add textWatchers
        registerTextWatchers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_selector_edit, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.selector_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle item selection
        when (item.itemId) {
            R.id.action_selector_edit_cancel_edit -> return cancelEdit()
            R.id.action_selector_edit_delete -> return deleteSelector()
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun cancelEdit(): Boolean {
        populateFields(nsBackup)
        ns = Json.parse(NotificationSelector.serializer(), Json.stringify(NotificationSelector.serializer(), nsBackup))
        notificationSelectors[nsId] = ns
        saveNotificationSelectors(activity!!.applicationContext, notificationSelectors)
        return true
    }

    private fun deleteSelector(): Boolean {
        notificationSelectors.remove(nsId)
        saveNotificationSelectors(activity!!.applicationContext, notificationSelectors)
        getActivity()?.onBackPressed()
        return true
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(type: String, data: Any)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SelectorEdit.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SelectorEditFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
