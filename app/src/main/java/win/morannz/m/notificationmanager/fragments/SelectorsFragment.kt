package win.morannz.m.notificationmanager.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_selectors.*
import win.morannz.m.notificationmanager.C
import win.morannz.m.notificationmanager.R

class SelectorsFragment : Fragment() {
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
        btn_add_selector.setOnClickListener {
            mListener?.onFragmentInteraction(C.NEW_NOTIFICATION_SELECTOR, C.NO_DATA)
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
