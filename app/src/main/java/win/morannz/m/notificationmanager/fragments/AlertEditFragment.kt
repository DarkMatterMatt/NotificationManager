package win.morannz.m.notificationmanager.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_alert_edit.*
import kotlinx.serialization.json.Json
import win.morannz.m.notificationmanager.*

class AlertEditFragment : Fragment() {
    private var mAgId = -1
    private var mAg = AlertGroup()
    private var mAgBackup = AlertGroup()
    private var mListener: OnFragmentInteractionListener? = null
    private var mAlertGroups = mutableMapOf<Int, AlertGroup>()
    private var mAutoSaveEnabled = true
    private var mRelativeVolumeStreams = mapOf<String, Int>()

    companion object {
        private val TAG = AlertEditFragment::class.java.simpleName
        private const val ALERT_GROUP_ID = "alertGroupId"

        fun newInstance(alertGroupId: Int) = AlertEditFragment().apply {
            arguments = Bundle().apply {
                putInt(ALERT_GROUP_ID, alertGroupId)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAgId = arguments!!.getInt(ALERT_GROUP_ID)
        mAlertGroups = getAlertGroups(context!!).toMutableMap()
        mAg = mAlertGroups[mAgId] ?: AlertGroup()

        // deep copy the AlertGroup
        mAgBackup = Json.parse(AlertGroup.serializer(), Json.stringify(AlertGroup.serializer(), mAg))

        /* Note -
         *   on Mi 8SE the following streams are used:
         *      STREAM_MUSIC, STREAM_ACCESSIBILITY - these are both changed together
         *      STREAM_NOTIFICATION, STREAM_DTMF, STREAM_RING, STREAM_SYSTEM - these are all changed together
         *      STREAM_ALARM
         *   other streams available:
         *      STREAM_VOICE_CALL - most likely can only be changed during phone call, probably irrelevant to this app
         */
        mRelativeVolumeStreams = mutableMapOf(
            getString(R.string.alert_edit_relative_volume_stream_notification) to AudioManager.STREAM_NOTIFICATION,
            getString(R.string.alert_edit_relative_volume_stream_music) to AudioManager.STREAM_MUSIC,
            getString(R.string.alert_edit_relative_volume_stream_alarm) to AudioManager.STREAM_ALARM
        )

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alert_edit, container, false)
    }

    override fun onStart() {
        super.onStart()

        // update the action bar title
        activity?.setTitle(R.string.title_alert_edit)

        // populate existing data (or blank template if creating a new alert group)
        populateFields(mAg)

        // add options for alertGroup dropdown
        val relativeVolumeStreamAdapter = ArrayAdapter(
            context!!,
            android.R.layout.simple_dropdown_item_1line,
            mRelativeVolumeStreams.keys.toList()
        )
        alert_edit_relative_volume_stream.setAdapter(relativeVolumeStreamAdapter)

        // add textWatchers
        registerWatchers()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.alert_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle item selection
        return when (item.itemId) {
            R.id.action_alert_edit_cancel_edit -> cancelEdit()
            R.id.action_alert_edit_delete -> deleteAlertGroup()
            else -> super.onOptionsItemSelected(item)
        }
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(type: String, data: Any)
    }

    private fun Boolean.toInt() = if (this) 1 else 0

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            // auto-save when user types in the text fields
            override fun afterTextChanged(editable: Editable?) {
                autoSave { afterTextChanged(editable.toString()) }
            }
        })
    }

    private fun autoSave(updateAgField: () -> Unit) {
        // auto-save when user interacts with an input
        if (mAutoSaveEnabled) {
            updateAgField()
            mAlertGroups[mAgId] = mAg
            saveAlertGroups(context!!, mAlertGroups)
        }
    }

    private fun getRelativeVolumeStreamString(stream: Int): String {
        // get the name of the volume stream from it'd Android integer ID
        for ((k, v) in mRelativeVolumeStreams) {
            if (v == stream) {
                return k
            }
        }
        return null.toString()
    }

    private fun registerWatchers() {
        // auto-save when user types in the input fields
        alert_edit_name.afterTextChanged { mAg.name = it }
        alert_edit_comment.afterTextChanged { mAg.comment = it }
        alert_edit_min_secs_between_alerts.afterTextChanged { if (it != "") mAg.minSecsBetweenAlerts = it.toInt() }
        alert_edit_sound_uri.setOnClickListener { selectSoundUri() }
        alert_edit_vibration_pattern.afterTextChanged { mAg.vibrationPatternTimings = it }
        alert_edit_alert_when_screen_on.setOnCheckedChangeListener { _, isChecked ->
            autoSave { mAg.alertWhenScreenOn = isChecked }
        }
        alert_edit_absolute_volume.setOnCheckedChangeListener { _, isChecked ->
            autoSave { mAg.absoluteVolume = isChecked }
        }
        alert_edit_volume_percent.afterTextChanged { if (it != "") mAg.volumePercent = it.toInt() }
        alert_edit_sound_ringer_modes_group.addOnButtonCheckedListener { _, _, _ ->
            autoSave {
                // 3 bits stored as a single int
                mAg.soundRingerModes = (0
                    + alert_edit_sound_ringer_mode_silent.isChecked.toInt() * RingerMode.SILENT
                    + alert_edit_sound_ringer_mode_vibrate.isChecked.toInt() * RingerMode.VIBRATE
                    + alert_edit_sound_ringer_mode_normal.isChecked.toInt() * RingerMode.NORMAL)
            }
        }
        alert_edit_vibration_ringer_modes_group.addOnButtonCheckedListener { _, _, _ ->
            autoSave {
                // 3 bits stored as a single int
                mAg.vibrationRingerModes = (0
                    + alert_edit_vibration_ringer_mode_silent.isChecked.toInt() * RingerMode.SILENT
                    + alert_edit_vibration_ringer_mode_vibrate.isChecked.toInt() * RingerMode.VIBRATE
                    + alert_edit_vibration_ringer_mode_normal.isChecked.toInt() * RingerMode.NORMAL)
            }
        }
        alert_edit_relative_volume_stream.afterTextChanged {
            mAg.relativeVolumeStream = mRelativeVolumeStreams[it] ?: AudioManager.STREAM_NOTIFICATION
        }
    }

    private fun populateFields(ag: AlertGroup) {
        // load existing stored data into the text fields
        mAutoSaveEnabled = false
        alert_edit_name.setText(ag.name)
        alert_edit_comment.setText(ag.comment)
        alert_edit_min_secs_between_alerts.setText(ag.minSecsBetweenAlerts.toString())
        alert_edit_sound_uri.setText(ag.soundUri)
        alert_edit_vibration_pattern.setText(ag.vibrationPatternTimings)
        alert_edit_alert_when_screen_on.isChecked = ag.alertWhenScreenOn
        alert_edit_absolute_volume.isChecked = ag.absoluteVolume
        alert_edit_volume_percent.setText(ag.volumePercent.toString())
        alert_edit_sound_ringer_mode_silent.isChecked = (ag.soundRingerModes and RingerMode.SILENT) != 0
        alert_edit_sound_ringer_mode_vibrate.isChecked = (ag.soundRingerModes and RingerMode.VIBRATE) != 0
        alert_edit_sound_ringer_mode_normal.isChecked = (ag.soundRingerModes and RingerMode.NORMAL) != 0
        alert_edit_vibration_ringer_mode_silent.isChecked = (ag.vibrationRingerModes and RingerMode.SILENT) != 0
        alert_edit_vibration_ringer_mode_vibrate.isChecked = (ag.vibrationRingerModes and RingerMode.VIBRATE) != 0
        alert_edit_vibration_ringer_mode_normal.isChecked = (ag.vibrationRingerModes and RingerMode.NORMAL) != 0
        alert_edit_relative_volume_stream.setText(getRelativeVolumeStreamString(ag.relativeVolumeStream))
        mAutoSaveEnabled = true
    }

    private fun selectSoundUri() {
        // use RingtoneManager to select a notification tone
        val i = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            //.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select ringtone for notifications:")
            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            //.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        startActivityForResult(i, RequestCode.SOUND_URI)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // recieving selected notification tone
        if (requestCode == RequestCode.SOUND_URI) {
            // user selected a ringtone successfully
            if (resultCode == Activity.RESULT_OK) {
                // load ringtone from intent
                val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                mAg.soundUri = uri?.toString()

                // display selected ringtone to user & save change
                alert_edit_sound_uri.setText(mAg.soundUri)
                mAlertGroups[mAgId] = mAg
                saveAlertGroups(context!!, mAlertGroups)
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                // there's no result
            }
        }
    }

    private fun cancelEdit(): Boolean {
        // load alert group backup
        populateFields(mAgBackup)
        mAg = Json.parse(AlertGroup.serializer(), Json.stringify(AlertGroup.serializer(), mAgBackup))

        // save the reverted alert group
        mAlertGroups[mAgId] = mAg
        saveAlertGroups(context!!, mAlertGroups)
        return true
    }

    private fun deleteAlertGroup(): Boolean {
        // delete alert group from list & then save
        mAlertGroups.remove(mAgId)
        saveAlertGroups(context!!, mAlertGroups)

        // leave edit fragment
        activity!!.onBackPressed()
        return true
    }
}
