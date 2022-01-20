package com.mobileadvsdk.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobileadvsdk.AdNetworkSDK
import com.mobileadvsdk.di.KodeinHolder
import com.mobileadvsdk.R
import com.mobileadvsdk.datasource.domain.model.AdvData
import com.mobileadvsdk.observe
import kotlinx.android.synthetic.main.activity_adv.*
import net.pubnative.player.VASTParser
import net.pubnative.player.VASTPlayer
import net.pubnative.player.model.VASTModel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import org.kodein.di.subKodein


class AdvActivity : AppCompatActivity(), KodeinAware {

    override val kodein: Kodein = subKodein(KodeinHolder.kodein) {}

    private val advViewMadel: AdvViewModel?  = AdNetworkSDK.provider

    private lateinit var advData: AdvData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adv)
        advViewMadel?.let {
            observe(it.advDataLive) {
                advData = it
                parseAdvData(it)
            }
        }
        initPlayerListener()
    }

    private fun parseAdvData(it: AdvData) {
        VASTParser(this).setListener(object : VASTParser.Listener {
            override fun onVASTParserError(error: Int) {

            }

            override fun onVASTParserFinished(model: VASTModel) {
                vastPlayer.load(model)
            }
        }).execute(it.seatbid[0].bid[0].adm)
    }

    private fun initPlayerListener(){
        vastPlayer.setListener(object : VASTPlayer.Listener {
            override fun onVASTPlayerLoadFinish() {
                advViewMadel?.getUrl(advData.seatbid[0].bid[0].nurl?:"")
                vastPlayer.play()
            }

            override fun onVASTPlayerFail(exception: Exception?) {

            }

            override fun onVASTPlayerPlaybackStart() {

            }

            override fun onVASTPlayerPlaybackFinish() {

            }

            override fun onVASTPlayerOpenOffer() {

            }
        })
    }

    override fun onPause() {
        super.onPause()
        vastPlayer.pause()
    }

    override fun onStop() {
        super.onStop()
        vastPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        vastPlayer.destroy()
    }
}