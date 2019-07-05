package win.morannz.m.notificationmanager.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import win.morannz.m.notificationmanager.R
import win.morannz.m.notificationmanager.adapters.AlertsListRecyclerViewAdapter
import win.morannz.m.notificationmanager.getAlertGroups

class AlertsListFragment : Fragment() {
    companion object {
        private val TAG = this::class.java.simpleName
    }

    private var mListener: OnListFragmentInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alerts_list, container, false)
        val alertGroups = getAlertGroups(context!!)

        // set the adapter
        if (view is RecyclerView) {
            with (view) {
                layoutManager = LinearLayoutManager(context)
                adapter = AlertsListRecyclerViewAdapter(alertGroups.toList(), mListener)
            }
        }
        return view
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(type: String, item: Any)
    }
}
