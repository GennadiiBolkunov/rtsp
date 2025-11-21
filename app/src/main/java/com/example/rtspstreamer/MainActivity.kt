package com.example.rtspstreamer

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.example.rtspstreamer.databinding.ActivityMainBinding
import com.example.rtspstreamer.stream.ScreenStreamer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var streamer: ScreenStreamer

    private val prefs by lazy { getSharedPreferences("rtsp_settings", Context.MODE_PRIVATE) }

    private val captureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val endpoint = currentEndpoint()
                if (endpoint != null) {
                    streamer.start(endpoint, result.data!!)
                }
            } else {
                setStatus(getString(R.string.status_error, "Разрешение не выдано"))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        streamer = ScreenStreamer(this) { state, message ->
            when (state) {
                ScreenStreamer.State.IDLE -> {
                    binding.streamButton.text = getString(R.string.start)
                    setStatus(getString(R.string.status_idle))
                }
                ScreenStreamer.State.CONNECTING -> {
                    binding.streamButton.text = getString(R.string.stop)
                    setStatus(getString(R.string.status_connecting))
                }
                ScreenStreamer.State.STREAMING -> setStatus(getString(R.string.status_streaming))
                ScreenStreamer.State.ERROR -> {
                    binding.streamButton.text = getString(R.string.start)
                    setStatus(getString(R.string.status_error, message ?: ""))
                }
            }
        }

        binding.streamButton.setOnClickListener {
            if (streamer.isStreaming()) {
                streamer.stop()
            } else {
                val endpoint = currentEndpoint()
                if (endpoint == null) {
                    Toast.makeText(this, R.string.change_endpoint, Toast.LENGTH_SHORT).show()
                    showEndpointDialog()
                } else {
                    requestScreenCapture()
                }
            }
        }

        updateEndpointText()
        setStatus(getString(R.string.status_idle))
    }

    override fun onDestroy() {
        super.onDestroy()
        streamer.stop()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_set_endpoint -> {
                showEndpointDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun requestScreenCapture() {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        captureLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    private fun currentEndpoint(): String? {
        val host = prefs.getString("host", null)
        val port = prefs.getInt("port", 0)
        return if (!host.isNullOrBlank() && port > 0) {
            "rtsp://$host:$port/live"
        } else null
    }

    private fun showEndpointDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((24 * resources.displayMetrics.density).toInt())
        }

        val hostInput = EditText(this).apply {
            hint = getString(R.string.host_hint)
            setText(prefs.getString("host", ""))
        }
        val portInput = EditText(this).apply {
            hint = getString(R.string.port_hint)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            val savedPort = prefs.getInt("port", 8554)
            setText(savedPort.takeIf { it > 0 }?.toString() ?: "")
        }

        container.addView(hostInput)
        container.addView(portInput)

        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title)
            .setMessage(R.string.dialog_message)
            .setView(container)
            .setPositiveButton(R.string.save) { _, _ ->
                val host = hostInput.text.toString().trim()
                val port = portInput.text.toString().toIntOrNull() ?: 0
                if (host.isNotEmpty() && port > 0) {
                    prefs.edit().putString("host", host).putInt("port", port).apply()
                    updateEndpointText()
                } else {
                    Toast.makeText(this, R.string.target_placeholder, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun updateEndpointText() {
        val endpoint = currentEndpoint()
        binding.targetText.text = endpoint?.let { "Target: $it" }
            ?: getString(R.string.target_placeholder)
    }

    private fun setStatus(text: String) {
        binding.statusText.text = text
    }
}
