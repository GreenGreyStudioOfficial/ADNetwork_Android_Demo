package com.mobileadvsdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

        observe(advViewMadel.peekData()) {
            VASTParser(this).setListener(object : VASTParser.Listener {
                override fun onVASTParserError(error: Int) {

                }

                override fun onVASTParserFinished(model: VASTModel) {
                    pubnative_vast_player.load(model)
                    pubnative_vast_player.setListener(object : VASTPlayer.Listener {
                        override fun onVASTPlayerLoadFinish() {

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
                    pubnative_vast_player.play()
                }
            }).execute(it.seatbid[0].bid[0].adm)
        }
    }

    override fun onStart() {
        super.onStart()

        advViewMadel.onStart()
    }

}