package win.morannz.m.notificationmanager.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_selector_edit.*
import kotlinx.serialization.json.Json
import win.morannz.m.notificationmanager.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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
    private var createNew: Boolean = false
    private var listener: OnFragmentInteractionListener? = null
    private var alertGroups: MutableMap<Int, AlertGroup> = mutableMapOf()
    private var alertGroupsNameLookup: MutableMap<String, Int> = mutableMapOf()
    private var notificationSelectors: MutableMap<Int, NotificationSelector> = mutableMapOf()
    private var packages: MutableList<String> = mutableListOf()

    fun EditText.saveAfterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
                notificationSelectors[nsId] = ns
                saveNotificationSelectors(activity!!.applicationContext, notificationSelectors)
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

        // load alert groups, create lookup map
        alertGroups = getAlertGroups(activity!!.applicationContext)
        for ((id, ag) in alertGroups) {
            alertGroupsNameLookup[ag.name] = id
        }
    }

    override fun onStart() {
        super.onStart()

        // populate existing data
        if (!createNew) {
            selector_edit_name.setText(ns.name)
            selector_edit_comment.setText(ns.comment)
            selector_edit_alert_group.setText(alertGroups[ns.alertGroupId]?.name)
            selector_edit_package_name.setText(ns.packageName)
            selector_edit_match_title.setText(ns.matchTitle)
            selector_edit_match_text.setText(ns.matchText)
            selector_edit_min_millisecs_between_alerts.setText(ns.minMillisecsBetweenAlerts.toString())
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
        selector_edit_name.saveAfterTextChanged { ns.name = it }
        selector_edit_comment.saveAfterTextChanged { ns.comment = it }
        selector_edit_alert_group.saveAfterTextChanged { ns.alertGroupId = alertGroupsNameLookup[it] }
        selector_edit_package_name.saveAfterTextChanged { ns.packageName = it }
        selector_edit_match_title.saveAfterTextChanged { ns.matchTitle = it }
        selector_edit_match_text.saveAfterTextChanged { ns.matchText = it }
        selector_edit_min_millisecs_between_alerts.saveAfterTextChanged { ns.minMillisecsBetweenAlerts = it.toInt() }
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
