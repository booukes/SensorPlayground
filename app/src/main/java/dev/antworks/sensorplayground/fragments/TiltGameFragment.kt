package dev.antworks.sensorplayground.fragments

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import dev.antworks.sensorplayground.R
import dev.antworks.sensorplayground.databinding.FragmentTiltGameBinding

class TiltGameFragment : Fragment(R.layout.fragment_tilt_game), SensorEventListener {

    private var _binding: FragmentTiltGameBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var xPos = 0f
    private var yPos = 0f

    private var xVel = 0f
    private var yVel = 0f

    private var xMax = 0f
    private var yMax = 0f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTiltGameBinding.bind(view)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        //calculating the size of the field
        binding.gameContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.gameContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)

                xMax = (binding.gameContainer.width - binding.ball.width).toFloat()
                yMax = (binding.gameContainer.height - binding.ball.height).toFloat()

                // Start is at the exact center
                xPos = xMax / 2
                yPos = yMax / 2
            }
        })
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            // SENSOR_DELAY_GAME is key for smoothness
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = -event.values[0]
            val ay = event.values[1]

            xVel += ax * 2f
            yVel += ay * 2f

            xVel *= 0.98f
            yVel *= 0.98f

            xPos += xVel
            yPos += yVel

            if (xPos < 0) { xPos = 0f; xVel = -xVel * 1 }
            if (xPos > xMax) { xPos = xMax; xVel = -xVel * 1}

            if (yPos < 0) { yPos = 0f; yVel = -yVel * 1 }
            if (yPos > yMax) { yPos = yMax; yVel = -yVel * 1 }


            binding.ball.x = xPos
            binding.ball.y = yPos

            binding.tvDebug.text = "X: %.2f\nY: %.2f".format(ax, ay)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}