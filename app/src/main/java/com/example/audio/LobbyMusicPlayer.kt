package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class LobbyMusicPlayer(private val scope: CoroutineScope) {

    val tracks = listOf(
        "Zirve Okey Müzik",
        "Günbatımı Lounge",
        "VIP Marina Ritmi"
    )

    private var playbackJob: Job? = null
    private var audioTrack: AudioTrack? = null

    fun start(trackIndex: Int) {
        stop()
        playbackJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val chordProgression = when (trackIndex % tracks.size) {
                0 -> listOf(
                    doubleArrayOf(220.0, 277.18, 329.63, 415.30),
                    doubleArrayOf(196.0, 246.94, 293.66, 369.99),
                    doubleArrayOf(174.61, 220.0, 261.63, 329.63),
                    doubleArrayOf(164.81, 207.65, 246.94, 329.63)
                )
                1 -> listOf(
                    doubleArrayOf(261.63, 329.63, 392.0, 493.88),
                    doubleArrayOf(220.0, 261.63, 329.63, 392.0),
                    doubleArrayOf(174.61, 220.0, 261.63, 349.23),
                    doubleArrayOf(196.0, 246.94, 293.66, 392.0)
                )
                else -> listOf(
                    doubleArrayOf(293.66, 349.23, 440.0, 523.25),
                    doubleArrayOf(246.94, 293.66, 392.0, 493.88),
                    doubleArrayOf(220.0, 261.63, 329.63, 440.0),
                    doubleArrayOf(196.0, 246.94, 329.63, 392.0)
                )
            }

            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val track = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (_: Exception) {
                return@launch
            }

            audioTrack = track
            try {
                track.play()
                val chordDurationSec = 1.6
                val samplesPerChord = (sampleRate * chordDurationSec).toInt()
                val buffer = ShortArray(1024)
                var chordIdx = 0
                while (isActive) {
                    val chord = chordProgression[chordIdx % chordProgression.size]
                    var i = 0
                    while (i < samplesPerChord && isActive) {
                        val chunk = minOf(buffer.size, samplesPerChord - i)
                        for (j in 0 until chunk) {
                            val t = (i + j).toDouble() / sampleRate
                            val env = (1.0 - exp(-t * 10.0)) * exp(-t * 1.4)
                            var sum = 0.0
                            for (freq in chord) {
                                sum += sin(2.0 * PI * freq * t)
                            }
                            val sample = (sum / chord.size) * env * 0.14
                            buffer[j] = (sample * Short.MAX_VALUE).toInt()
                                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        }
                        track.write(buffer, 0, chunk)
                        i += chunk
                    }
                    chordIdx++
                }
            } catch (_: Exception) {
                // Ignore audio stop race
            } finally {
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {
                }
                if (audioTrack === track) audioTrack = null
            }
        }
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (_: Exception) {
        }
    }
}
