package win.morannz.m.notificationmanager.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import win.morannz.m.notificationmanager.C
import win.morannz.m.notificationmanager.R
import win.morannz.m.notificationmanager.RecentNotification
import win.morannz.m.notificationmanager.adapters.RecentsListRecyclerViewAdapter
import win.morannz.m.notificationmanager.getRecentNotifications

class RecentsListFragment : Fragment() {
    companion object {
        private val TAG = RecentsListFragment::class.java.simpleName
    }

    private var mListener: OnListFragmentInteractionListener? = null
    private var mListAdapter: RecentsListRecyclerViewAdapter? = null
    private var mRecentNotifications = mutableListOf<RecentNotification>()

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
        val view = inflater.inflate(R.layout.fragment_recents_list, container, false)
        mRecentNotifications = getRecentNotifications(context!!).toMutableList()
        mListAdapter = RecentsListRecyclerViewAdapter(mRecentNotifications, mListener)

        // set the adapter
        if (view is RecyclerView) {
            with (view) {
                layoutManager = LinearLayoutManager(context)
                adapter = mListAdapter
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

    fun updateList(rn: RecentNotification) {
        // insert recent notification at the start of the list
        mRecentNotifications.add(0, rn)

        // trim list length
        mRecentNotifications = mRecentNotifications.take(C.MAX_NUMBER_OF_RECENT_NOTIFICATIONS).toMutableList()

        // refresh list
        mListAdapter?.notifyDataSetChanged()
    }
}
