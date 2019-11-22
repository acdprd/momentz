package com.bolaware.viewstimerstory

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.constraint.ConstraintLayout
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.VideoView
import com.bolaware.viewstimerstory.MyProgressBar.Companion.MAX_PROGRESS
import com.bolaware.viewstimerstory.utils.Utils
import kotlinx.android.synthetic.main.progress_story_view.view.*
import kotlin.math.max
import kotlin.math.min


open class Momentz : ConstraintLayout {
    private var currentlyShownIndex = 0
    private var currentView: View? = null
    private var momentzViewList: List<MomentzView>
    private var libSliderViewList = mutableListOf<MyProgressBar>()
    private var momentzCallback: MomentzCallback
    private lateinit var view: View
    private val passedInContainerView: ViewGroup
    private var mProgressDrawable: Int = R.drawable.green_lightgrey_drawable
    private var pausedState: Boolean = false
    var touchListener: OnTouchListener? = null
    var gestureDetector: GestureDetector? = null
    var ifFirst: (() -> Unit)? = null

    constructor(
            context: Context,
            momentzViewList: List<MomentzView>,
            passedInContainerView: ViewGroup,
            momentzCallback: MomentzCallback,
            @DrawableRes mProgressDrawable: Int = R.drawable.green_lightgrey_drawable
    ) : super(context) {
        this.momentzViewList = momentzViewList
        this.momentzCallback = momentzCallback
        this.passedInContainerView = passedInContainerView
        this.mProgressDrawable = mProgressDrawable
    }

    open fun init(post: () -> Unit = {}) {
        Utils.fixRootTopPadding(view.linearProgressIndicatorLay)
        momentzViewList.forEachIndexed { index, sliderView ->
            val myProgressBar = MyProgressBar(
                    context,
                    index,
                    sliderView.durationInSeconds,
                    object : ProgressTimeWatcher {
                        override fun onEnd(indexFinished: Int) {
                            currentlyShownIndex = indexFinished
                            next()
                        }
                    },
                    mProgressDrawable
            )
            libSliderViewList.add(myProgressBar)
            view.linearProgressIndicatorLay.addView(myProgressBar)
        }
        //start()
        view.post(post)
    }

    open fun callPause(pause: Boolean) {
        try {
            if (pause) {
                if (!pausedState) {
                    this.pausedState = !pausedState
                    pause(false)
                }
            } else {
                if (pausedState) {
                    this.pausedState = !pausedState
                    resume()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun make(post: () -> Unit = {}): Momentz {
        initView()
        init(post)
        return this
    }

    open fun initView() {
        view = View.inflate(context, R.layout.progress_story_view, this)
        val params = FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )
        view.isLongClickable = true
        touchListener ?: let {
            if (gestureDetector == null) {
                gestureDetector = GestureDetector(context, SingleTapConfirm())
            }
            touchListener = object : OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (gestureDetector!!.onTouchEvent(event)) {
                        // single tap
                        if (x > resources.displayMetrics.widthPixels / 2) next()
                        else prev()
                        return true
                    } else {
                        // your code for move and drag
                        when (event?.action) {
                            MotionEvent.ACTION_DOWN -> {
                                callPause(true)
                                return true
                            }

                            MotionEvent.ACTION_UP -> {
                                callPause(false)
                                return true
                            }
                            else -> return false
                        }
                    }
                }
            }
        }
        view.setOnTouchListener(touchListener)

        this.layoutParams = params
        passedInContainerView.addView(this)
    }

    open fun cancelProgress() {
        view.loaderProgressbar.visibility = View.GONE
        if (currentlyShownIndex != 0) {
            for (i in 0..min(libSliderViewList.size - 1, max(0, currentlyShownIndex - 1))) {
                libSliderViewList[i].progress = MAX_PROGRESS
                libSliderViewList[i].cancelProgress()
            }
        }

        if (currentlyShownIndex != libSliderViewList.size - 1) {
            for (i in (currentlyShownIndex + 1) until libSliderViewList.size) {
                libSliderViewList[i].progress = 0
                libSliderViewList[i].cancelProgress()
            }
        }
    }

    open fun show() {
        cancelProgress()
        val nextView = momentzViewList[currentlyShownIndex].view
        val sameViewType = currentView === nextView
        if (!sameViewType) {
            currentView = nextView
            view.currentlyDisplayedView.removeAllViews()
            view.currentlyDisplayedView.addView(currentView)
            val params = FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            )
            //params.gravity = Gravity.CENTER_VERTICAL
            if (currentView is ImageView) {
                (currentView as ImageView).scaleType = ImageView.ScaleType.CENTER_CROP
//            (currentView as ImageView).adjustViewBounds = true
            }
            currentView?.layoutParams = params
        }
        libSliderViewList[currentlyShownIndex].startProgress()

        momentzCallback.onNextCalled(currentView!!, this, currentlyShownIndex)
    }

    open fun start() {
//            Handler().postDelayed({
//                show()
//            }, 2000)
        show()
    }

    open fun editDurationAndResume(index: Int, newDurationInSeconds: Int) {
        view.loaderProgressbar.visibility = View.GONE
        libSliderViewList[index].editDurationAndResume(newDurationInSeconds)
    }

    @JvmOverloads
    open fun pause(withLoader: Boolean = false) {
        if (withLoader) {
            view.loaderProgressbar.visibility = View.VISIBLE
        }
        libSliderViewList[currentlyShownIndex].pauseProgress()
        if (momentzViewList[currentlyShownIndex].view is VideoView) {
            (momentzViewList[currentlyShownIndex].view as VideoView).pause()
        }
    }

    open fun resume() {
        view.loaderProgressbar.visibility = View.GONE
        libSliderViewList[currentlyShownIndex].resumeProgress()
        if (momentzViewList[currentlyShownIndex].view is VideoView) {
            (momentzViewList[currentlyShownIndex].view as VideoView).start()
        }
    }

    open fun next() {
        try {
            currentlyShownIndex++
            if (momentzViewList.size <= currentlyShownIndex) {
                finish()
                return
            }
            show()
        } catch (e: IndexOutOfBoundsException) {
            finish()
        }
    }

    open fun finish() {
        momentzCallback.done()
        for (progressBar in libSliderViewList) {
            progressBar.cancelProgress()
            progressBar.progress = MAX_PROGRESS
        }
    }

    open fun prev() {
        if (currentlyShownIndex == 0 && ifFirst != null) {
            cancelProgress()
            ifFirst?.invoke()
        } else
            try {
                currentlyShownIndex = max(currentlyShownIndex - (if (currentlyShownIndex in momentzViewList.indices) 1 else 2), 0)
//            if (0 > currentlyShownIndex) {
//                currentlyShownIndex = 0
//            }
//            if (currentView == momentzViewList[currentlyShownIndex].view) {
//                currentlyShownIndex--
//                if (0 > currentlyShownIndex) {
//                    currentlyShownIndex = 0
//                }
//            }
            } catch (e: IndexOutOfBoundsException) {
                currentlyShownIndex -= 2
            } finally {
                cancelProgress()
                show()
            }
    }

    private inner class SingleTapConfirm : SimpleOnGestureListener() {

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            return true
        }
    }


}