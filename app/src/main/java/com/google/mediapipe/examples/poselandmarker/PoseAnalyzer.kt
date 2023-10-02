package com.google.mediapipe.examples.poselandmarker

import android.util.Log
import android.widget.Toast
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseAnalyzer {

    private var lungeInProgress = false
    private var lastLungeState = false



    fun analyzePoseLandmarks(poseLandmarkerResult: PoseLandmarkerResult) {
        val landmarks = poseLandmarkerResult.landmarks().flatten()

        // Check if landmarks are detected
        if (landmarks.isEmpty()) {
            // No landmarks detected, handle accordingly (e.g., log a message)
            Log.e(TAG, "No landmarks detected")
            return
        }

        // Verify indices based on the landmarks data you receive
        val leftHipIndex = 23
        val leftKneeIndex = 25
        val leftAnkleIndex = 27

        // Ensure indices are within bounds
        if (leftHipIndex >= landmarks.size || leftKneeIndex >= landmarks.size || leftAnkleIndex >= landmarks.size) {
            Log.e(TAG, "Invalid landmark indices")
            return
        }


    }

    fun calculateAngle(
        landmarks: List<NormalizedLandmark>,
        hipIndex: Int,
        kneeIndex: Int,
        ankleIndex: Int
    ): Double {
        val hip = landmarks[hipIndex]
        val knee = landmarks[kneeIndex]
        val ankle = landmarks[ankleIndex]

        // Assuming your landmarks have x, y coordinates
        val hipX = hip.x()
        val hipY = hip.y()
        val kneeX = knee.x()
        val kneeY = knee.y()
        val ankleX = ankle.x()
        val ankleY = ankle.y()

        // Calculate the angle using the cosine rule
        val a = calculateDistance(hipX, hipY, kneeX, kneeY)
        val b = calculateDistance(kneeX, kneeY, ankleX, ankleY)
        val c = calculateDistance(hipX, hipY, ankleX, ankleY)

        return Math.toDegrees(Math.acos((a * a + b * b - c * c) / (2.0 * a * b)))
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        val dx = x1 - x2
        val dy = y1 - y2
        return Math.sqrt((dx * dx + dy * dy).toDouble())
    }

    fun isLungeInProgress(): Boolean {
        return true
    }

    companion object {
        const val TAG = "PoseAnalyzer"
    }
}
