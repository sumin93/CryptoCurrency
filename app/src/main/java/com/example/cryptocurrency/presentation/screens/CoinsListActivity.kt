package com.example.cryptocurrency.presentation.screens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.cryptocurrency.R
import com.example.cryptocurrency.domain.ServiceOfLoadingData
import com.example.cryptocurrency.presentation.App.Companion.KEY_REFRESHING_PERIOD
import com.example.cryptocurrency.presentation.App.Companion.SHARED_PREFS_NAME
import com.example.cryptocurrency.presentation.adapters.PriceListAdapter
import com.example.cryptocurrency.presentation.adapters.PriceDiffUtilsCallback
import com.example.cryptocurrency.presentation.viewmodels.CoinsInfoViewModel
import com.example.cryptocurrency.utils.convertPeriodFromPercentToSeconds
import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.item_list.*

class CoinsListActivity : AppCompatActivity() {

    private var twoPane: Boolean = false

    private lateinit var adapter: PriceListAdapter
    private lateinit var coinsInfoViewModel: CoinsInfoViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var serviceLoadingIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)
        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        coinsInfoViewModel = ViewModelProviders.of(this).get(CoinsInfoViewModel::class.java)
        serviceLoadingIntent = Intent(this, ServiceOfLoadingData::class.java)
        if (item_detail_container != null) {
            twoPane = true
        }
        setupRecyclerView(item_list)
        setupSeekBar()
        coinsInfoViewModel.getPriceList().observe(this, Observer {
            adapter.submitList(it)
        })
        switchLoadingService(serviceLoadingIntent, true)
    }

    private fun switchLoadingService(serviceIntent: Intent, shouldTurnOn: Boolean) {
        if (shouldTurnOn) {
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            stopService(serviceIntent)
        }
    }

    private fun setupSeekBar() {
        seek_bar_time_of_refreshing.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val progress = convertPeriodFromPercentToSeconds(p1)
                sharedPreferences.edit().putInt(KEY_REFRESHING_PERIOD, p1).apply()
                text_view_period_of_refreshing_label.text = String.format(getString(R.string.period_of_refreshing_label), progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        seek_bar_time_of_refreshing.progress = if (sharedPreferences.contains(KEY_REFRESHING_PERIOD)) {
            (sharedPreferences.getInt(KEY_REFRESHING_PERIOD, 50) * 10) / 6
        } else {
            50 //default value of refreshing period (percent from minutes)
        }
        text_view_period_of_refreshing_label.text = String.format(getString(R.string.period_of_refreshing_label), convertPeriodFromPercentToSeconds(seek_bar_time_of_refreshing.progress))
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        adapter = PriceListAdapter(this, PriceDiffUtilsCallback())
        adapter.onItemClickListener = {
            if (twoPane) {
                val fragment = CoinDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(
                            CoinDetailFragment.ARG_ITEM_ID,
                            it.fromSymbol
                        )
                    }
                }

                supportFragmentManager.beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit()
            } else {
                val intent = Intent(this, CoinDetailActivity::class.java)
                intent.putExtra(CoinDetailFragment.ARG_ITEM_ID, it.fromSymbol)
                startActivity(intent)
            }
        }
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
    }

    override fun onDestroy() {
        switchLoadingService(serviceLoadingIntent, false)
        super.onDestroy()
    }
}