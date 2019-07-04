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
import win.morannz.m.notificationmanager.getAlertGroupMaxIndex
import win.morannz.m.notificationmanager.saveAlertGroupMaxIndex

class AlertsFragment : Fragment() {
    companion object {
        fun newInstance() = AlertsFragment()
        private val TAG = this::class.java.simpleName

        private const val ME = "${BuildConfig.APPLICATION_ID}.AlertsFragment"
        const val INTERACTION = "$ME.INTERACTION"
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
        btn_add_alert.setOnClickListener {
            val newIndex = getAlertGroupMaxIndex(context!!) + 1
            saveAlertGroupMaxIndex(context!!, newIndex)
            mListener?.onFragmentInteraction(INTERACTION, newIndex)
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
