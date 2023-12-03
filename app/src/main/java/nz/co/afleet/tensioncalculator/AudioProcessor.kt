package nz.co.afleet.tensioncalculator

// AudioProcessor.kt
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.android.AndroidAudioInputStream
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm

class AudioProcessor {

    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize
    )

    private val dispatcher: AudioDispatcher

    init {
        val audioInputStream = AndroidAudioInputStream(audioRecord)
        val audioFormat = TarsosDSPAudioFormat(sampleRate.toFloat(), 16, 1, true, false)
        dispatcher = AudioDispatcher(audioInputStream, bufferSize, bufferSize / 2)
    }

    fun startListening() {
        audioRecord.startRecording()
        dispatcher.addAudioProcessor(createPitchProcessor())
        Thread(dispatcher, "Audio Dispatcher").start()
    }

    fun stopListening() {
        audioRecord.stop()
        audioRecord.release()
        dispatcher.stop()
    }

    private fun createPitchProcessor(): AudioProcessor {
        return PitchProcessor(
            PitchEstimationAlgorithm.YIN,
            sampleRate.toFloat(),
            bufferSize,
            PitchDetectionHandler { result, _ ->
                val pitchInHz = result.pitch
                // Handle the pitch result (frequency) as needed
                println("Detected pitch: $pitchInHz Hz")
            }
        )
    }
}
