package win.morannz.m.notificationmanager.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_selector_edit.*
import kotlinx.serialization.json.Json
import win.morannz.m.notificationmanager.*
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter



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
    private var ns: NotificationSelector = NotificationSelector()
    private var createNew: Boolean = false
    private var listener: OnFragmentInteractionListener? = null
    private var alertGroups: Map<Int, AlertGroup> = mutableMapOf<Int, AlertGroup>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNew = arguments?.getBoolean(C.NEW_NOTIFICATION_SELECTOR) ?: false
        val nsString = arguments?.getString(C.NOTIFICATION_SELECTOR)
        if (nsString != null) {
            ns = Json.parse(NotificationSelector.serializer(), nsString)
        }
        alertGroups = getAlertGroups(activity!!.applicationContext)
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

        val adapter = ArrayAdapter<String>(
            activity!!.applicationContext,
            android.R.layout.simple_dropdown_item_1line,
            alertGroups.values.map{ x -> x.name }
        )
        selector_edit_alert_group.setAdapter(adapter)
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
