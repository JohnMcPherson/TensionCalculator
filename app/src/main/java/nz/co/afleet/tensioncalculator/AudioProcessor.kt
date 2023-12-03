package nz.co.afleet.tensioncalculator

// AudioProcessor.kt
import android.media.AudioFormat
import android.media.AudioRecord
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.android.AndroidAudioInputStream
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm

class AudioProcessor (val activity: MainActivity, val audioRecord: AudioRecord){

    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val dispatcher: AudioDispatcher

    init {
        val audioFormat = TarsosDSPAudioFormat(sampleRate.toFloat(), 16, 1, true, false)
        val audioInputStream = AndroidAudioInputStream(audioRecord, audioFormat)
        dispatcher = AudioDispatcher(audioInputStream, bufferSize, bufferSize / 2)
        startListening()
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
        var lastPitch : Int
        return PitchProcessor(
            PitchEstimationAlgorithm.YIN,
            sampleRate.toFloat(),
            bufferSize,
            PitchDetectionHandler { result, audioEvent ->
                val rms = audioEvent.rms
                val pitchInHz = result.pitch.toInt()
                // Handle the pitch result (frequency) as needed
                //println("Detected pitch: $pitchInHz Hz")
                if (rms > .02 && pitchInHz != -1) {
                    lastPitch = pitchInHz
                    activity.frequency.value = lastPitch
                }
            }
        )
    }
}
