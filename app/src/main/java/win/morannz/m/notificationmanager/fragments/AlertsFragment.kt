package win.morannz.m.notificationmanager.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_alerts.*
import win.morannz.m.notificationmanager.BuildConfig
import win.morannz.m.notificationmanager.R
import win.morannz.m.notificationmanager.getAlertGroupNewIndex

class AlertsFragment : Fragment() {
    companion object {
        fun newInstance() = AlertsFragment()
        private val TAG = AlertsFragment::class.java.simpleName

        private const val ME = "${BuildConfig.APPLICATION_ID}.AlertsFragment"
        const val NAVIGATE_ALERT_EDIT = "$ME.NAVIGATE_ALERT_EDIT"
    }

    private var mListener: OnFragmentInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alerts, container, false)
    }

    override fun onStart() {
        super.onStart()

        // update the action bar title
        activity?.setTitle(R.string.title_alerts)

        // 'new alert group' button
        btn_add_alert.setOnClickListener {
            val newIndex = getAlertGroupNewIndex(context!!)
            mListener?.onFragmentInteraction(NAVIGATE_ALERT_EDIT, newIndex)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(type: String, data: Any)
    }
}
