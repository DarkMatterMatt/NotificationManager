package win.morannz.m.notificationmanager.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import win.morannz.m.notificationmanager.fragments.AlertsListFragment.OnListFragmentInteractionListener

import kotlinx.android.synthetic.main.fragment_alert_list_item.view.*
import win.morannz.m.notificationmanager.AlertGroup
import win.morannz.m.notificationmanager.R
import win.morannz.m.notificationmanager.C

class AlertListRecyclerViewAdapter(
    private val mValues: List<Pair<Int, AlertGroup>>,
    private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<AlertListRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            mListener?.onListFragmentInteraction(C.ALERT_GROUP, v.tag)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_alert_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (id, item) = mValues[position]
        holder.mIdView.text = "#ID: $id"
        holder.mContentView.text = "#NAME: ${item.name}"

        with (holder.mView) {
            tag = id
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.title
        val mContentView: TextView = mView.text

        override fun toString(): String {
            return "${super.toString()} '${mContentView.text}'"
        }
    }
}
