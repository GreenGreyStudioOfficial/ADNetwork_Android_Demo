package com.mobileadvsdk

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.android.synthetic.main.activity_adv.*
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import net.pubnative.player.model.VASTModel

import net.pubnative.player.VASTParser
import net.pubnative.player.VASTPlayer
import java.lang.Exception


private const val VAST = "<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?><VAST xmlns:xsi=\\\"https://www.w3.org/2001/XMLSchema-instance\\\" version=\\\"2.0\\\" xsi:noNamespaceSchemaLocation=\\\"vast.xsd\\\"><Ad id=\\\"1\\\"><InLine><AdSystem>Mobidriven</AdSystem><AdTitle>Ad</AdTitle><Impression id=\\\"mobidriven\\\"><![CDATA[https://sp-01.mobidriven.com/imp?data=I5w8QXb3xLeCToDEbOX0ua/4OoYPy+P3aBq0DovL/m+qBrS6VPnIHgzAfiJRRTux1btEcpdcgaxy1ZH7f2N5gsMeSHF9AuydE8hb/5p72CXXuQ/HvkyT0eoWTUoEq+VG8Tf0fzaxngEdorBkoQu+9g==]]></Impression><Impression><![CDATA[https://sp.mobidriven.com/sp.gif?imp1]]></Impression><Creatives><Creative id=\\\"1\\\"><Linear skipoffset=\\\"5\\\"><Duration>15</Duration><MediaFiles><MediaFile id=\\\"1\\\" delivery=\\\"progressive\\\" type=\\\"video/mp4\\\" bitrate=\\\"500\\\" width=\\\"640\\\" height=\\\"360\\\"><![CDATA[https://files.mobidriven.com/A45E6A.mp4]]></MediaFile></MediaFiles><TrackingEvents><Tracking event=\\\"start\\\"><![CDATA[https://sp-01.mobidriven.com/event?data=I5w8QXb3xLeCToDEbOX0ua/4OoYPy+P3aBq0DovL/m+qBrS6VPnIHgzAfiJRRTux1btEcpdcgaxy1ZH7f2N5gsMeSHF9AuydE8hb/5p72CXXuQ/HvkyT0eoWTUoEq+VG8Tf0fzaxngEdorBkoQu+9g==&name=start]]></Tracking><Tracking event=\\\"start\\\"><![CDATA[https://sp.mobidriven.com/sp.gif?start1]]></Tracking><Tracking event=\\\"firstQuartile\\\"><![CDATA[https://sp-01.mobidriven.com/event?data=I5w8QXb3xLeCToDEbOX0ua/4OoYPy+P3aBq0DovL/m+qBrS6VPnIHgzAfiJRRTux1btEcpdcgaxy1ZH7f2N5gsMeSHF9AuydE8hb/5p72CXXuQ/HvkyT0eoWTUoEq+VG8Tf0fzaxngEdorBkoQu+9g==&name=first_quartile]]></Tracking><Tracking event=\\\"midpoint\\\"><![CDATA[https://sp-01.mobidriven.com/event?data=I5w8QXb3xLeCToDEbOX0ua/4OoYPy+P3aBq0DovL/m+qBrS6VPnIHgzAfiJRRTux1btEcpdcgaxy1ZH7f2N5gsMeSHF9AuydE8hb/5p72CXXuQ/HvkyT0eoWTUoEq+VG8Tf0fzaxngEdorBkoQu+9g==&name=midpoint]]></Tracking><Tracking event=\\\"thirdQuartile\\\"><![CDATA[https://sp-01.mobidriven.com/event?data=I5w8QXb3xLeCToDEbOX0ua/4OoYPy+P3aBq0DovL/m+qBrS6VPnIHgzAfiJRRTux1btEcpdcgaxy1ZH7f2N5gsMeSHF9AuydE8hb/5p72CXXuQ/HvkyT0eoWTUoEq+VG8Tf0fzaxngEdorBkoQu+9g==&name=third_quartile]]></Tracking><Tracking event=\\\"complete\\\"><![CDATA[https://sp-01.mobidriven.com/event?data=I5w8QXb3xLeCToDEbOX0ua/4OoYPy+P3aBq0DovL/m+qBrS6VPnIHgzAfiJRRTux1btEcpdcgaxy1ZH7f2N5gsMeSHF9AuydE8hb/5p72CXXuQ/HvkyT0eoWTUoEq+VG8Tf0fzaxngEdorBkoQu+9g==&name=complete]]></Tracking><Tracking event=\\\"complete\\\"><![CDATA[https://sp.mobidriven.com/sp.gif?complete1]]></Tracking><Tracking event=\\\"skip\\\"><![CDATA[https://sp-01.mobidriven.com/event?data=I5w8QXb3xLeCToDEbOX0ua/4OoYPy+P3aBq0DovL/m+qBrS6VPnIHgzAfiJRRTux1btEcpdcgaxy1ZH7f2N5gsMeSHF9AuydE8hb/5p72CXXuQ/HvkyT0eoWTUoEq+VG8Tf0fzaxngEdorBkoQu+9g==&name=skip]]></Tracking><Tracking event=\\\"close\\\"><![CDATA[https://sp-01.mobidriven.com/event?data=I5w8QXb3xLeCToDEbOX0ua/4OoYPy+P3aBq0DovL/m+qBrS6VPnIHgzAfiJRRTux1btEcpdcgaxy1ZH7f2N5gsMeSHF9AuydE8hb/5p72CXXuQ/HvkyT0eoWTUoEq+VG8Tf0fzaxngEdorBkoQu+9g==&name=close]]></Tracking></TrackingEvents><VideoClicks><ClickThrough><![CDATA[http://ya.ru/1]]></ClickThrough><ClickTracking id=\\\"mobidriven\\\"><![CDATA[https://sp-01.mobidriven.com/click?data=I5w8QXb3xLeCToDEbOX0ua/4OoYPy+P3aBq0DovL/m+qBrS6VPnIHgzAfiJRRTux1btEcpdcgaxy1ZH7f2N5gsMeSHF9AuydE8hb/5p72CXXuQ/HvkyT0eoWTUoEq+VG8Tf0fzaxngEdorBkoQu+9g==]]></ClickTracking><ClickTracking><![CDATA[https://sp.mobidriven.com/sp.gif?click1]]></ClickTracking></VideoClicks><Extension type=\\\"cta\\\"><![CDATA[call to action]]></Extension></Linear></Creative></Creatives></InLine></Ad></VAST>"

class AdvActivity : AppCompatActivity() {

    private val player: ExoPlayer by lazy {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSource.Factory(this)

        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
            .setAdsLoaderProvider { adsLoader }
            .setAdViewProvider(pvPlayer)

        ExoPlayer.Builder(this).setMediaSourceFactory(mediaSourceFactory).build()
    }
    private val adsLoader: ImaAdsLoader by lazy {
        ImaAdsLoader.Builder(this).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adv)

        VASTParser(this).setListener(object : VASTParser.Listener {
            override fun onVASTParserError(error: Int) {

            }

            override fun onVASTParserFinished(model: VASTModel) {
                pubnative_vast_player.load(model)
                pubnative_vast_player.setListener(object : VASTPlayer.Listener{
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
        }).execute(assets.open("VAST").bufferedReader().use { it.readText() })


    }

    private fun startPlay(){
//        // Create the MediaItem to play, specifying the content URI and ad tag URI.
//        // Create the MediaItem to play, specifying the content URI and ad tag URI.
        val contentUri: Uri = Uri.parse("https://files.mobidriven.com/A45E6A.mp4")
        val adTagUri: Uri = Uri.parse(VAST)
        val mediaItem: MediaItem = MediaItem.Builder().setUri(contentUri)
            .setAdTagUri(adTagUri).build()

        // Prepare the content and ad to be played with the SimpleExoPlayer.
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

        player.addListener(object: Player.Listener {

        })
    }

    private fun initPlayer(){
        pvPlayer.player = player
        adsLoader.setPlayer(player)
    }

    override fun onStart() {
        super.onStart()
//        initPlayer()
//        startPlay()
    }

    private fun releasePlayer() {
        adsLoader.release()
        pvPlayer.player?.release()
        player.release()
    }

    override fun onResume() {
        super.onResume()
        pvPlayer.onResume()
    }

    override fun onPause() {
        super.onPause()
        pvPlayer.onPause()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        adsLoader.release()
    }
}