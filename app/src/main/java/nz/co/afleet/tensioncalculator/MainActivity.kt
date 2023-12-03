package nz.co.afleet.tensioncalculator

import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import nz.co.afleet.tensioncalculator.ui.theme.TensionCalculatorTheme
import kotlin.math.round

class MainActivity : ComponentActivity() {

    val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 123
    private lateinit var audioRecord: AudioRecord
    private lateinit var audioProcessor: AudioProcessor
    val frequency = MutableStateFlow(-1)
    @Composable
    fun FrequencyDisplay() {
        val freq by frequency.collectAsState()
        Text(text = "Freq: " + freq.toString() + " Tension: " + getTensionInPoundsRounded(freq) .toString() + " lb")
    }

    private fun getTensionInPoundsRounded(frequency: Int): Double {
        val ccPerCubicMeter = 1000000.0 // cm^3
        val pi = 3.14
        val metresPerInch = .0254
        val mmPerMeter = 1000
        val kgPerNewton = 0.102
        val poundsPerKg = 2.2

        val pGrmCm3 = 8.0 // g/cm^3
        val lMm = 590.0 // mm
        val dInch = .022 // inch

        val p = 8000 // kg per meter^3
        val D = dInch  * metresPerInch // metres
        val L = lMm / mmPerMeter // metres

        val tNewtons = p * pi * D*D /4 * frequency * frequency * L
        val tKilograms = tNewtons * kgPerNewton
        val tPounds = tNewtons/4.44822

        return round(tPounds * 10)/10

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TensionCalculatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FrequencyDisplay()
//                    Greeting("Android")
                }
            }
        }


        // Check and request the RECORD_AUDIO permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    RECORD_AUDIO_PERMISSION_REQUEST_CODE
                )
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    finish()
                } else {
                    // Permission already granted, start audio processing
                    val sampleRate = 44100
                    val bufferSize = AudioRecord.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )

                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                    )

                    audioProcessor = AudioProcessor(this, audioRecord)
                }
            } else {
                // Versions prior to Android 6.0 don't need runtime permission, start audio processing
                val sampleRate = 44100
                val bufferSize = AudioRecord.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                audioProcessor = AudioProcessor(this, audioRecord)
            }

        }

        /*
            @Deprecated("Deprecated in Java")
            override fun onRequestPermissionsResult(
                requestCode: Int,
                permissions: Array<String>,
                grantResults: IntArray
            ) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
                        // If request is cancelled, the result arrays are empty.
                        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                            // permission was granted, yay! Do the
                            // contacts-related task you need to do.
                            val audioRecord = AudioRecord.Builder().build()
                        } else {
                            // permission denied, boo! Disable the
                            // functionality that depends on this permission.
                        }
                        return
                    }
                }
            }

        */

    }

    override fun onStop() {
        super.onStop()
        audioProcessor.stopListening()
    }

}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TensionCalculatorTheme {
        Greeting("Android")
    }
}