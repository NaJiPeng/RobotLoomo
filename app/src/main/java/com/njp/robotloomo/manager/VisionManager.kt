package com.njp.robotloomo.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.Surface
import android.view.TextureView
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.segway.robot.algo.dts.DTSPerson
import com.segway.robot.algo.dts.PersonDetectListener
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.connectivity.BufferMessage
import com.segway.robot.sdk.connectivity.StringMessage
import com.segway.robot.sdk.locomotion.sbv.Base
import com.segway.robot.sdk.vision.DTS
import com.segway.robot.sdk.vision.Vision
import java.io.ByteArrayOutputStream

/**
 * 视觉
 */
@SuppressLint("StaticFieldLeak")
object VisionManager {

    private val mVision = Vision.getInstance()
    private val mDts = mVision.dts
    private var mIsBindSuccess = false
    private lateinit var mTextureView: TextureView
    private lateinit var mClassifier: ImageClassifier
    var isSend = false
    var isClassifier = false

    val options = FirebaseVisionObjectDetectorOptions.Builder()
            .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
            .enableClassification()  // Optional
            .build()
    val objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)

    private val width = 540
    private val height = 960

    private val mImageRunnable = Runnable {
        while (true) {
            if (isSend) {
                var bitmap: Bitmap? = mTextureView.getBitmap(width, height) ?: continue
                val m = Matrix()
                m.setRotate(-90f, (bitmap!!.width / 2).toFloat(), (bitmap.height / 2).toFloat())
                m.postScale(-1f, 1f)
                val bmp2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
                bitmap.recycle()
                bitmap = bmp2
                val outputStream = ByteArrayOutputStream(bitmap.byteCount)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                ConnectionManager.send(BufferMessage(outputStream.toByteArray()), null)
                bitmap.recycle()
                try {
                    Thread.sleep(40)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private val mDetectorRunnable = Runnable {
        while (true) {
            if (isClassifier) {
                val bitmap = mTextureView.getBitmap(width, height)
                val image = FirebaseVisionImage.fromBitmap(bitmap)
                objectDetector.processImage(image)
                        .addOnSuccessListener { detectedObjects ->
                            Log.i("mmmm","detector success")
                            if (detectedObjects.isNotEmpty()){
                                Log.i("mmmm",detectedObjects[0].boundingBox.flattenToString())
                            }
                            image.bitmap.recycle()
                        }
                        .addOnFailureListener { e ->
                            Log.i("mmmm", "objectDetector fail:" + e.message)
                            image.bitmap.recycle()
                        }
            }
            Thread.sleep(150)
        }
    }

    private val mClassifierRunnable = Runnable {
        while (true) {
            if (isClassifier) {
                val bitmap = mTextureView.getBitmap(ImageClassifier.DIM_IMG_SIZE_X, ImageClassifier.DIM_IMG_SIZE_Y)
                val textToShow = mClassifier.classifyFrame(bitmap)
                bitmap.recycle()
                Log.i("mmmm", textToShow)
                ConnectionManager.send(StringMessage("classify:$textToShow"))
            }
        }
    }

    private val mBindStateListener = object : ServiceBinder.BindStateListener {
        override fun onUnbind(reason: String?) {
            mIsBindSuccess = false
            mDts.stop()
        }

        override fun onBind() {
            mIsBindSuccess = true
            mDts.setVideoSource(DTS.VideoSource.CAMERA)
            mDts.setPreviewDisplay(Surface(mTextureView.surfaceTexture))
            mDts.start()
            Thread(mImageRunnable).start()
            Thread(mDetectorRunnable).start()
            Thread(mClassifierRunnable).start()
        }

    }
//    private val mPersonDetectListener = object : PersonDetectListener {
//        override fun onPersonDetected(person: Array<out DTSPerson>?) {
//        }
//
//        override fun onPersonDetectionError(errorCode: Int, message: String?) {
//        }
//
//        override fun onPersonDetectionResult(person: Array<out DTSPerson>?) {
//            if (!person.isNullOrEmpty()) {
//                BaseManager.setMode(Base.CONTROL_MODE_RAW)
//                BaseManager.setVelocity(0f, 0f)
//            } else {
//                BaseManager.setMode(Base.CONTROL_MODE_NAVIGATION)
//            }
//        }
//
//    }

//    fun startDetectingPerson() {
//        mDts?.startDetectingPerson(mPersonDetectListener)
//    }
//
//    fun stopDetectingPerson() {
//        try {
//            mDts?.stopDetectingPerson()
//        } catch (e: Exception) {
//            //Do nothing
//        }
//    }


    fun init(context: Activity, surface: TextureView) {
        mVision.bindService(context, mBindStateListener)
        mTextureView = surface
        mClassifier = ImageClassifier(context)
    }

    fun unbind() {
        if (mIsBindSuccess) {
            mVision.unbindService()
        }
    }

}