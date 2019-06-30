package win.morannz.m.notificationmanager.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import win.morannz.m.notificationmanager.fragments.SelectorsListFragment.OnListFragmentInteractionListener
import win.morannz.m.notificationmanager.R
import win.morannz.m.notificationmanager.C

import kotlinx.android.synthetic.main.fragment_selector_list_item.view.*
import win.morannz.m.notificationmanager.NotificationSelector
import win.morannz.m.notificationmanager.fragments.SelectorsListFragment

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class SelectorListRecyclerViewAdapter(
    private val mValues: List<Pair<Int, NotificationSelector>>,
    private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<SelectorListRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(C.NOTIFICATION_SELECTOR, v.tag)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_selector_list_item, parent, false)
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
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
