package com.electric.glp.Adapters

import android.annotation.SuppressLint
import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.electric.glp.R
import com.electric.glp.databinding.ItemWifiNetworkBinding

class WifiNetworkAdapter(
    private var wifiNetworks: List<ScanResult>,
    private val listener: OnWifiItemClickListener
) : RecyclerView.Adapter<WifiNetworkAdapter.WifiViewHolder>() {

    inner class WifiViewHolder(private val binding: ItemWifiNetworkBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val scanResult = wifiNetworks[position]
                    listener.onWifiItemClicked(scanResult.SSID)
                }
            }
        }

        fun bind(scanResult: ScanResult) {
            val ssid = scanResult.SSID
            val level = scanResult.level
            binding.wifiSsid.text = ssid
            binding.wifiSignalIcon.setImageResource(getSignalIcon(level))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newWifiNetworks: List<ScanResult>) {
        wifiNetworks = newWifiNetworks.filterNot { it.SSID == "GLP" || it.SSID == "SSID" }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWifiNetworkBinding.inflate(inflater, parent, false)
        return WifiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WifiViewHolder, position: Int) {
        holder.bind(wifiNetworks[position])
    }

    override fun getItemCount() = wifiNetworks.size

    private fun getSignalIcon(signalStrength: Int): Int {
        return when {
            signalStrength > -50 -> R.drawable.ic_wifi_strong
            signalStrength > -70 -> R.drawable.ic_wifi_medium
            else -> R.drawable.ic_wifi_weak
        }
    }

    interface OnWifiItemClickListener {
        fun onWifiItemClicked(ssid: String)
    }
}