package com.google.mediapipe.examples.poselandmarker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()
    private var progressPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private val poseAnalyzer = PoseAnalyzer()
    private var lastLungeState = false  // Track the previous lunge state
    private var lungeCounter = 0
    private var previousCount = 0
    private var count: Int = 0
    private var wasAngleAboveThreshold = false

    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL

        progressPaint.color = Color.GREEN
        progressPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->
            for (landmark in poseLandmarkerResult.landmarks()) {
                for (normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                }

                PoseLandmarker.POSE_LANDMARKS.forEach {
                    canvas.drawLine(
                        poseLandmarkerResult.landmarks().get(0).get(it!!.start()).x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.start()).y() * imageHeight * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).y() * imageHeight * scaleFactor,
                        linePaint
                    )
                }
            }
            //write counted Excersize
//            pointPaint.color = Color.RED
//            pointPaint.textSize = 20f
//            pointPaint.isAntiAlias = true
//            val countText = "Lunge count: $previousCount"
//            canvas.drawText(countText, 20f, 50f, pointPaint)


            val landmarks = poseLandmarkerResult.landmarks().flatten()

            // Check if landmarks are detected
            if (landmarks.isEmpty()) {
                // No landmarks detected, handle accordingly (e.g., log a message)
                Log.e(PoseAnalyzer.TAG, "No landmarks detected")
                return
            }

            // Verify indices based on the landmarks data you receive
            val leftHipIndex = 23
            val leftKneeIndex = 25
            val leftAnkleIndex = 27

            // Ensure indices are within bounds
            if (leftHipIndex >= landmarks.size || leftKneeIndex >= landmarks.size || leftAnkleIndex >= landmarks.size) {
                Log.e(PoseAnalyzer.TAG, "Invalid landmark indices")
                return
            }

            // Calculate progress based on the angle
            val angle = poseAnalyzer.calculateAngle(
                landmarks,
                leftHipIndex, leftKneeIndex, leftAnkleIndex
            )

            //

            if (wasAngleAboveThreshold && angle < 160) {

                // Angle was above threshold and is now below, increment counter
                if (!lastLungeState) {
                    count =count +1
                }
            }
            // Update the lunge in progress state and count repetitions
            if (angle > 170.0) {
                wasAngleAboveThreshold = true
                count =count
            } else{
                wasAngleAboveThreshold = false
            }

//            pointPaint.color = Color.RED
//            pointPaint.textSize = 20f
//            pointPaint.isAntiAlias = true
//            var countText = "Lunge count: $count"
//            canvas.drawText(countText, 20f, 50f, pointPaint)


            val progress = calculateProgress(angle)
            drawProgressBar(canvas, progress)




            //



            // Verify indices based on the landmarks data you receive
            val RightHipIndex = 24
            val RightKneeIndex = 26
            val RightAnkleIndex = 28

            // Ensure indices are within bounds
            if (RightHipIndex >= landmarks.size || RightKneeIndex >= landmarks.size || RightAnkleIndex >= landmarks.size) {
                Log.e(PoseAnalyzer.TAG, "Invalid landmark indices")
                return
            }

            // Calculate progress based on the angle
            val angle_right = poseAnalyzer.calculateAngle(
                landmarks,
                leftHipIndex, leftKneeIndex, leftAnkleIndex
            )

            //

            if (wasAngleAboveThreshold && angle_right < 160) {

                // Angle was above threshold and is now below, increment counter
                if (!lastLungeState) {
                    count =count +1
                }
            }
            // Update the lunge in progress state and count repetitions
            if (angle_right > 170.0) {
                wasAngleAboveThreshold = true
                count =count
            } else{
                wasAngleAboveThreshold = false
            }


            val progress_ = calculateProgress(angle)
            drawProgressBar(canvas, progress_)
            pointPaint.color = Color.RED
            pointPaint.textSize = 20f
            pointPaint.isAntiAlias = true
            var countText = "Lunge count: $count"
            canvas.drawText(countText, 20f, 50f, pointPaint)
        }
    }

    private fun calculateProgress(angle: Double): Float {
        // The progress will be 100% when angle is below 160, and decreases as angle increases beyond 160.
        return when {
            angle < 150 -> 1f
            angle > 180 -> 0f
            else -> (1f - (angle - 150) / 20f).toFloat()
        }
    }

    private fun drawProgressBar(canvas: Canvas, progress: Float) {
        val progressBarHeight = 20f
        val progressBarWidth = width * progress

        val progressBarRect = RectF(0f, 0f, progressBarWidth, progressBarHeight)
        canvas.drawRect(progressBarRect, progressPaint)
    }




    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }

        // Analyze pose landmarks and count Forward Lunges
        poseAnalyzer.analyzePoseLandmarks(poseLandmarkerResults)
//        var count = poseAnalyzer.isLungeInProgress()
//        if (count != previousCount) {
////            Toast.makeText(context, "Lunge count: $count", Toast.LENGTH_SHORT).show()
//            previousCount = count
//
//        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}
