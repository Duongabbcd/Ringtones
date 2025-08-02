package com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.type

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.applovin.impl.a7
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityAllTypeAlertBinding
import com.ezt.ringify.ringtonewallpaper.databinding.ItemTypeAlertBinding
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.FlashType
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.VibrationType
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.alert.CallScreenAlertActivity
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.alert.CallScreenAlertActivity.Companion.flashTypeValue
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.alert.CallScreenAlertActivity.Companion.vibrationValue

class AllTypeAlertActivity :
    BaseActivity<ActivityAllTypeAlertBinding>(ActivityAllTypeAlertBinding::inflate) {

    private val adapter: TypeAlertAdapter by lazy {
        TypeAlertAdapter { selected ->
            if (alertType == "Flash") {
                flashTypeValue = selected
            } else {
                vibrationValue = selected
            }
            finish()
        }
    }

    private val alertType by lazy { intent.getStringExtra("alertType") ?: "Flash" }
    private val currentValue by lazy { intent.getStringExtra("currentValue") ?: "None" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.backBtn.setOnClickListener { finish() }
        binding.allTypeAlerts.adapter = adapter

        if (alertType == "Flash") {
            adapter.submitList(FlashType.entries.map { it.label }, currentValue)
            binding.nameScreen.text = getString(R.string.flash_type)
        } else {
            adapter.submitList(VibrationType.entries.map { it.label }, currentValue)
            binding.nameScreen.text = getString(R.string.vibration_type)
        }
    }
}



class TypeAlertAdapter(private val onClickListener: (String) -> Unit) :
    RecyclerView.Adapter<TypeAlertAdapter.TypeAlertViewHolder>() {
    private val allTypeAlerts: MutableList<String> = mutableListOf()
    private var selectedPosition: Int = RecyclerView.NO_POSITION

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
        println("submitList: $list and $currentValue")
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
                    println("AllIconViewHolder: $item")
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