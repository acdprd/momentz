package com.bolaware.viewstimerstory

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.bolaware.viewstimerstory.utils.toPixel

class MyProgressBar : ProgressBar {
    var durationInSeconds: Int = 0
    private var index: Int = 0
    private var objectAnimator = ObjectAnimator.ofInt(this, "progress", this.progress, MAX_PROGRESS)
    private var hasStarted: Boolean = false
    private val timeWatcher: ProgressTimeWatcher
    private var mProgressDrawable : Int = R.drawable.green_lightgrey_drawable

    constructor(context: Context, index: Int, durationInSeconds: Int, timeWatcher: ProgressTimeWatcher,  @DrawableRes mProgressDrawable : Int = R.drawable.green_lightgrey_drawable ) : super(
        context,
        null,
        0,
        android.R.style.Widget_ProgressBar_Horizontal
    ) {
        this.durationInSeconds = durationInSeconds
        this.index = index
        this.timeWatcher = timeWatcher
        this.mProgressDrawable = mProgressDrawable
        initView()
    }

    private fun initView() {

        val params = LinearLayout.LayoutParams(
            /*LinearLayout.LayoutParams.MATCH_PARENT*/0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        )

        params.marginStart = 2f.toPixel(context)
        params.marginEnd = 2f.toPixel(context)

        //val progressBar = MyProgressBar(context, durationInSeconds * 1000)
        // textView.text = label.trim()

        this.max = MAX_PROGRESS

        this.progress = 0

        this.layoutParams = params

        this.progressDrawable = ContextCompat.getDrawable(context, mProgressDrawable)

    }

    fun startProgress() {
        objectAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                timeWatcher.onEnd(index)
            }

            override fun onAnimationCancel(animation: Animator?) {
                animation?.apply { removeAllListeners() }
            }

            override fun onAnimationRepeat(animation: Animator?) {

            }
        })
        objectAnimator.apply {
            duration = (durationInSeconds * 1000).toLong()
            start()
        }

        hasStarted = true
    }

    fun cancelProgress() {
        objectAnimator.apply {
            cancel()
            removeAllListeners()
        }
    }

    fun pauseProgress() {
        objectAnimator.apply {
            pause()
        }
    }

    fun resumeProgress() {
        if (hasStarted) {
            objectAnimator.apply {
                resume()
            }
        }
    }

    fun editDurationAndResume(newDurationInSeconds: Int){
        this.durationInSeconds = newDurationInSeconds
        cancelProgress()
        startProgress()
    }

    companion object {
        const val MAX_PROGRESS = 1000
    }
}