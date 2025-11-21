package com.example.rtspstreamer.stream

import android.content.Context
import android.content.Intent
import android.util.Log
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import com.pedro.rtplibrary.rtsp.RtspDisplay

class ScreenStreamer(
    context: Context,
    private val loggerTag: String = "ScreenStreamer",
    private val onStateChanged: (State, String?) -> Unit
) : ConnectCheckerRtsp {

    enum class State { IDLE, CONNECTING, STREAMING, ERROR }

    private val rtspDisplay: RtspDisplay = RtspDisplay(context, true, this)

    fun isStreaming(): Boolean = rtspDisplay.isStreaming

    fun start(endpoint: String, projectionData: Intent) {
        if (rtspDisplay.isStreaming) return
        val prepared = rtspDisplay.prepareAudio() && rtspDisplay.prepareVideo()
        if (!prepared) {
            onStateChanged(State.ERROR, "Не удалось инициализировать кодеки")
            return
        }
        onStateChanged(State.CONNECTING, null)
        rtspDisplay.startStream(endpoint)
    }

    fun stop() {
        if (rtspDisplay.isStreaming) {
            rtspDisplay.stopStream()
            onStateChanged(State.IDLE, null)
        }
    }

    override fun onAuthErrorRtsp() {
        onStateChanged(State.ERROR, "Ошибка авторизации")
    }

    override fun onAuthSuccessRtsp() {
        Log.d(loggerTag, "Auth success")
    }

    override fun onConnectionFailedRtsp(reason: String) {
        onStateChanged(State.ERROR, reason)
        rtspDisplay.stopStream()
    }

    override fun onConnectionStartedRtsp(rtspUrl: String) {
        Log.d(loggerTag, "Connecting to $rtspUrl")
    }

    override fun onConnectionSuccessRtsp() {
        onStateChanged(State.STREAMING, null)
    }

    override fun onDisconnectRtsp() {
        onStateChanged(State.IDLE, null)
    }

    override fun onNewBitrateRtsp(bitrate: Long) {
        Log.d(loggerTag, "Bitrate: $bitrate")
    }
}
