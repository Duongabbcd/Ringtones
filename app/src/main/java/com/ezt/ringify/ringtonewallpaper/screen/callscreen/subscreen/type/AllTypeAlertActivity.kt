package com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.type

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityAllTypeAlertBinding
import com.ezt.ringify.ringtonewallpaper.databinding.ItemTypeAlertBinding
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.FlashType
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.FlashVibrationManager
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.VibrationType
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.alert.CallScreenAlertActivity.Companion.flashTypeValue
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.alert.CallScreenAlertActivity.Companion.vibrationValue
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity

class AllTypeAlertActivity :
    BaseActivity<ActivityAllTypeAlertBinding>(ActivityAllTypeAlertBinding::inflate) {
    private val flashVibrationManager: FlashVibrationManager by lazy {
        FlashVibrationManager(this)
    }

    private val adapter: TypeAlertAdapter by lazy {
        TypeAlertAdapter { selected ->
            if (alertType == "Flash") {
                flashTypeValue = selected
                val flash = FlashType.fromLabel(flashTypeValue) ?: FlashType.DEFAULT
                flashVibrationManager.playFlashType(flash)
            } else {
                vibrationValue = selected
                val vibration = VibrationType.fromLabel(vibrationValue) ?: VibrationType.DEFAULT
                flashVibrationManager.playVibration(vibration)
            }
        }
    }

    private val alertType by lazy { intent.getStringExtra("alertType") ?: "Flash" }
    private val currentValue by lazy { intent.getStringExtra("currentValue") ?: "None" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadBanner(this)
        binding.backBtn.setOnClickListener {
            flashVibrationManager.stopFlashAndVibration()
            SearchRingtoneActivity.backToScreen(
                this@AllTypeAlertActivity,
                "INTER_CALLSCREEN"
            )
        }
        binding.allTypeAlerts.adapter = adapter

        if (alertType == "Flash") {
            adapter.submitList(
                FlashType.entries.filter { it != FlashType.None }.map { it.label },
                currentValue
            )
            binding.nameScreen.text = getString(R.string.flash_type)
        } else {
            adapter.submitList(VibrationType.entries.filter { it != VibrationType.None }
                .map { it.label }, currentValue)
            binding.nameScreen.text = getString(R.string.vibration_type)
        }
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@AllTypeAlertActivity, "INTER_CALLSCREEN")
    }
}



class TypeAlertAdapter(private val onClickListener: (String) -> Unit) :
    RecyclerView.Adapter<TypeAlertAdapter.TypeAlertViewHolder>() {
    private val allTypeAlerts: MutableList<String> = mutableListOf()
    private var selectedPosition: Int = RecyclerView.NO_POSITION
    private val TAG = TypeAlertAdapter::class.java.name
    private lateinit var context: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TypeAlertViewHolder {
        context = parent.context
        val binding =
            ItemTypeAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TypeAlertViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TypeAlertViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    fun submitList(list: List<String>, currentValue: String) {
        allTypeAlerts.clear()
        allTypeAlerts.addAll(list)
        selectedPosition = list.indexOf(currentValue)
        Log.d(TAG, "submitList: $list and $currentValue")
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = allTypeAlerts.size


    inner class TypeAlertViewHolder(private val binding: ItemTypeAlertBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                val item = allTypeAlerts[position]
                val default = context.getString(R.string.default_title)
                title.text = if (item.equals("None", false)) default else item
                // Change background or text color if selected
                if (position == selectedPosition) {
                    binding.selectButton.setImageResource(R.drawable.icon_select_circle)
                } else {
                    binding.selectButton.setImageResource(R.drawable.icon_unselect_circle)
                }


                root.setOnClickListener {
                    Log.d(TAG, "AllIconViewHolder: $item")
                    val previousPosition = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                    onClickListener(item)
                }
            }
        }
    }
}