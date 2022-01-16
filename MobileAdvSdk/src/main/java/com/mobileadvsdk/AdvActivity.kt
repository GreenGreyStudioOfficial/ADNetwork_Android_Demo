package com.mobileadvsdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobileadvsdk.datasource.domain.model.AdvData
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

    private val advViewMadel: AdvViewModel by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adv)

        observe(advViewMadel.advDataLive) {
            parseAdvData(it)
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