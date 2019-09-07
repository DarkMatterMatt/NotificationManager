package win.morannz.m.notificationmanager.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_selectors.*
import win.morannz.m.notificationmanager.BuildConfig
import win.morannz.m.notificationmanager.R
import win.morannz.m.notificationmanager.getNotificationSelectorNewIndex

class SelectorsFragment : Fragment() {
    companion object {
        fun newInstance() = SelectorsFragment()
        private val TAG = SelectorsFragment::class.java.simpleName

        private const val ME = "${BuildConfig.APPLICATION_ID}.SelectorsFragment"
        const val NAVIGATE_SELECTOR_EDIT = "$ME.NAVIGATE_ALERT_EDIT"
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
        return inflater.inflate(R.layout.fragment_selectors, container, false)
    }

    override fun onStart() {
        super.onStart()

        // update the action bar title
        activity?.setTitle(R.string.title_selectors)

        // 'new alert group' button
        btn_add_selector.setOnClickListener {
            val newIndex = getNotificationSelectorNewIndex(context!!)
            mListener?.onFragmentInteraction(NAVIGATE_SELECTOR_EDIT, newIndex)
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
