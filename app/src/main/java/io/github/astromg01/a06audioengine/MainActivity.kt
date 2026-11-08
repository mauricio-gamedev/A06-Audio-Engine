package io.github.astromg01.a06audioengine

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.AudioAttributes as Media3AudioAttributes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import io.github.astromg01.a06audioengine.audio.A06DspProcessor
import io.github.astromg01.a06audioengine.audio.A06RenderersFactory
import io.github.astromg01.a06audioengine.audio.AudioProfile

@UnstableApi
class MainActivity : Activity() {
    companion object {
        private const val REQUEST_AUDIO = 4106
    }

    private val dspProcessor = A06DspProcessor()
    private var player: ExoPlayer? = null
    private var selectedProfile = AudioProfile.DEFAULT
    private var intensity = 1f
    private var dspEnabled = true

    private lateinit var statusView: TextView
    private lateinit var profileDescription: TextView
    private lateinit var intensityLabel: TextView
    private lateinit var diagnosticsView: TextView
    private lateinit var toggleDspButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildInterface()
        initializePlayer()
        applyDspSettings()
        refreshDiagnostics()
    }

    private fun buildInterface() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(15, 20, 28))
            isFillViewport = true
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
        }
        scroll.addView(content)
        setContentView(scroll)

        content.addView(text(
            "A06 AUDIO ENGINE",
            25f,
            Color.rgb(120, 214, 176),
            bold = true
        ))
        content.addView(text(
            "Processamento PCM local · sem root · Galaxy A06",
            13f,
            Color.LTGRAY
        ))
        content.addView(spacer(14))

        content.addView(panelText(
            "O DSP é aplicado ao áudio reproduzido dentro deste player. " +
                "Android não permite que um app comum processe universalmente o áudio de YouTube, jogos ou outros apps."
        ))

        content.addView(sectionTitle("PERFIL SONORO"))
        val profileSpinner = Spinner(this)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            AudioProfile.entries.map { it.title }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        profileSpinner.adapter = adapter
        profileSpinner.setSelection(AudioProfile.entries.indexOf(selectedProfile))

        profileDescription = text(selectedProfile.description, 13f, Color.LTGRAY)
        content.addView(profileSpinner, matchWrap())
        content.addView(profileDescription)

        profileSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                selectedProfile = AudioProfile.entries[position]
                profileDescription.text = selectedProfile.description
                applyDspSettings()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }
        content.addView(spacer(8))

        intensityLabel = text("Intensidade do perfil: 100%", 14f, Color.WHITE, bold = true)
        content.addView(intensityLabel)
        val intensitySlider = SeekBar(this).apply {
            max = 100
            progress = 100
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    intensity = progress / 100f
                    intensityLabel.text = "Intensidade do perfil: $progress%"
                    applyDspSettings()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }
        content.addView(intensitySlider, matchWrap())

        toggleDspButton = button("DSP ativado") {
            dspEnabled = !dspEnabled
            applyDspSettings()
        }
        content.addView(toggleDspButton, matchWrap())

        content.addView(sectionTitle("REPRODUÇÃO"))
        content.addView(button("Selecionar áudio do dispositivo") { selectAudio() }, matchWrap())
        val playbackRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val playButton = button("Reproduzir / pausar") { togglePlayback() }
        val stopButton = button("Parar") { stopPlayback() }
        playbackRow.addView(playButton, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
            marginEnd = dp(6)
        })
        playbackRow.addView(stopButton, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
            marginStart = dp(6)
        })
        content.addView(playbackRow, matchWrap())

        statusView = panelText("Nenhum áudio selecionado.")
        content.addView(statusView)

        content.addView(sectionTitle("INTEGRAÇÃO DO SISTEMA"))
        content.addView(button("Abrir configurações de som do Android") {
            openSystemSoundSettings()
        }, matchWrap())
        content.addView(panelText(
            "As configurações da Samsung podem afetar o som do aparelho conforme os recursos " +
                "disponíveis na One UI. O Engine não altera configurações protegidas nem força efeitos globais."
        ))

        content.addView(sectionTitle("DIAGNÓSTICO"))
        content.addView(button("Atualizar diagnóstico") { refreshDiagnostics() }, matchWrap())
        diagnosticsView = panelText("Coletando informações...")
        content.addView(diagnosticsView)
    }

    private fun initializePlayer() {
        val renderersFactory = A06RenderersFactory(this, dspProcessor)
        player = ExoPlayer.Builder(this, renderersFactory)
            .build()
            .apply {
                setAudioAttributes(
                    Media3AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true
                )
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        statusView.text = if (isPlaying) {
                            "Reproduzindo com DSP: ${selectedProfile.title}"
                        } else {
                            "Reprodução pausada ou aguardando."
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            statusView.text = "Reprodução concluída."
                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        statusView.text = "Falha ao reproduzir: ${error.errorCodeName}"
                        Toast.makeText(
                            this@MainActivity,
                            "Não foi possível reproduzir este arquivo.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }
    }

    private fun selectAudio() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, REQUEST_AUDIO)
    }

    @Deprecated("Using the platform document picker keeps this initial build dependency-light.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_AUDIO || resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // The picker may return a temporary grant; the current playback can still proceed.
        }
        player?.setMediaItem(MediaItem.fromUri(uri))
        player?.prepare()
        player?.play()
        statusView.text = "Arquivo carregado: ${uri.lastPathSegment ?: "áudio local"}"
    }

    private fun togglePlayback() {
        val currentPlayer = player ?: return
        if (currentPlayer.isPlaying) {
            currentPlayer.pause()
            return
        }
        if (currentPlayer.currentMediaItem == null) {
            Toast.makeText(this, "Selecione um áudio primeiro.", Toast.LENGTH_SHORT).show()
            return
        }
        currentPlayer.play()
    }

    private fun stopPlayback() {
        player?.stop()
        player?.clearMediaItems()
        statusView.text = "Reprodução parada."
    }

    private fun applyDspSettings() {
        dspProcessor.update(selectedProfile, intensity, dspEnabled)
        if (::toggleDspButton.isInitialized) {
            toggleDspButton.text = if (dspEnabled) {
                "DSP ativado · limitador de segurança"
            } else {
                "DSP desativado · limitador de segurança ativo"
            }
        }
    }

    private fun openSystemSoundSettings() {
        try {
            startActivity(Intent(Settings.ACTION_SOUND_SETTINGS))
        } catch (_: Exception) {
            Toast.makeText(this, "Configurações de som indisponíveis neste firmware.", Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshDiagnostics() {
        if (::diagnosticsView.isInitialized) {
            diagnosticsView.text = DeviceDiagnostics.report(this)
        }
    }

    private fun text(
        value: String,
        sizeSp: Float,
        color: Int,
        bold: Boolean = false
    ): TextView = TextView(this).apply {
        text = value
        textSize = sizeSp
        setTextColor(color)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, dp(5), 0, dp(5))
    }

    private fun sectionTitle(value: String): TextView =
        text(value, 15f, Color.rgb(120, 214, 176), bold = true).apply {
            setPadding(0, dp(20), 0, dp(8))
        }

    private fun panelText(value: String): TextView =
        text(value, 13f, Color.rgb(222, 228, 235)).apply {
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.rgb(30, 38, 49))
        }

    private fun button(value: String, action: () -> Unit): Button =
        Button(this).apply {
            text = value
            isAllCaps = false
            setOnClickListener { action() }
        }

    private fun spacer(heightDp: Int): android.view.View =
        android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, dp(heightDp))
        }

    private fun matchWrap(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(6)
            bottomMargin = dp(4)
        }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}
