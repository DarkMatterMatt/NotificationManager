package win.morannz.m.notificationmanager.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_alerts.*
import win.morannz.m.notificationmanager.C
import win.morannz.m.notificationmanager.R
import win.morannz.m.notificationmanager.getAlertGroupMaxIndex
import win.morannz.m.notificationmanager.saveAlertGroupMaxIndex

class AlertsFragment : Fragment() {
    private var mListener: OnFragmentInteractionListener? = null

    companion object {
        fun newInstance() = AlertsFragment()
    }

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
        btn_add_alert.setOnClickListener {
            val newIndex = getAlertGroupMaxIndex(context!!) + 1
            saveAlertGroupMaxIndex(context!!, newIndex)
            mListener?.onFragmentInteraction(C.ALERT_GROUP, newIndex)
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
