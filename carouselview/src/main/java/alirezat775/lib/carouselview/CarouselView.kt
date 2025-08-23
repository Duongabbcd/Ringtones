package alirezat775.lib.carouselview

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import androidx.annotation.IntDef
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Author:  Alireza Tizfahm Fard
 * Date:    12/7/17
 * Email:   alirezat775@gmail.com
 */

class CarouselView
/**
 * @param context  current context, will be used to access resources
 * @param attrs    attributeSet
 * @param defStyle defStyle
 */
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    RecyclerView(context, attrs, defStyle) {

    var listener: CarouselListener? = null

    private var velocityTracker: VelocityTracker? = null

    var currentPosition: Int = 0
    private var actionDown = true
    var isAutoScroll = false
    var isLoopMode: Boolean = false
    var delayMillis: Long = 5000
    private var reverseLoop = true
    private var scheduler: Scheduler? = null
    private var scrolling = true

    var anchor: Int = 0
        set(anchor) {
            if (this.anchor != anchor) {
                field = anchor
                manager.anchor = anchor
                requestLayout()
            }
        }

    /**
     * @return support RTL view
     */
    private val isRTL: Boolean
        get() = ViewCompat.getLayoutDirection(this@CarouselView) == ViewCompat.LAYOUT_DIRECTION_RTL

    /**
     * @return support RTL view
     */
    private val isTrustLayout: Boolean
        get() {
            if (isRTL && manager.reverseLayout) {
                return true
            } else if (!isRTL && manager.reverseLayout) {
                return false
            } else if (isRTL && !manager.reverseLayout) {
                return false
            } else if (!isRTL && !manager.reverseLayout) {
                return true
            }
            return false
        }

    /**
     * @return layoutManager
     */
    val manager: CarouselLayoutManager
        get() = layoutManager as CarouselLayoutManager

    /**
     * @param adapter the new adapter to set, or null to set no adapter
     */
    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        if ((adapter?.itemCount ?: 0) > 0)
            initSnap(initialPosition)  // use your desired starting index
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initSnap(initialPosition)  // use your desired starting index
    }

    var initialPosition: Int = 0

    /**
     * initialize
     */
    private fun initSnap(initialPosition: Int = 0) {
        clipToPadding = false
        overScrollMode = View.OVER_SCROLL_NEVER
        anchor = CENTER
        addOnItemTouchListener(onItemTouchListener())
        post {
            scrolling(initialPosition, true)
            if (isAutoScroll) getScheduler()
        }
    }

    /**
     * @return scheduler scroll item
     */
    private fun getScheduler(): Scheduler? {
        if (scheduler == null) {
            scheduler = Scheduler(delayMillis, 1)
        }
        return scheduler
    }

    /**
     * pause auto scroll
     */
    fun pauseAutoScroll() {
        getScheduler()?.cancel()
        scrolling = false
    }

    /**
     * resume auto scroll
     */
    fun resumeAutoScroll() {
        getScheduler()?.start()
        scrolling = true
    }

    /**
     * @return onItemTouchListener for calculate velocity and position fix view center
     */
    private var downX = 0f
    private var downY = 0f
    private val velocityThreshold = 700
    private val minFlingDistance = 30

    private fun onItemTouchListener(): OnItemTouchListener {
        return object : OnItemTouchListener {

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val action = e.actionMasked

                val manager = layoutManager as? CarouselLayoutManager ?: return false

                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = e.x
                        downY = e.y
                        if (velocityTracker == null) {
                            velocityTracker = VelocityTracker.obtain()
                        } else {
                            velocityTracker?.clear()
                        }
                        velocityTracker?.addMovement(e)
                        actionDown = true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        velocityTracker?.addMovement(e)
                        velocityTracker?.computeCurrentVelocity(1000)
                        actionDown = false
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                        if (action == MotionEvent.ACTION_UP) {
                            val child = rv.findChildViewUnder(e.x, e.y)

                            if (child == null) {
                                // Touch happened in empty space — consume it
                                return true
                            }

                            // Check if inside actual bounds of the child
                            val childLeft = child.left
                            val childTop = child.top
                            val childRight = child.right
                            val childBottom = child.bottom

                            if (e.x < childLeft || e.x > childRight || e.y < childTop || e.y > childBottom) {
                                return true // Also consume if touch is on invisible padding
                            }
                        }

                        velocityTracker?.addMovement(e)
                        velocityTracker?.computeCurrentVelocity(1000)

                        val deltaX = Math.abs(e.x - downX)
                        val deltaY = Math.abs(e.y - downY)

                        if (velocityTracker != null) {
                            val xVelocity = velocityTracker!!.xVelocity
                            val yVelocity = velocityTracker!!.yVelocity

                            when (manager.orientation) {
                                HORIZONTAL -> {
                                    if (deltaX > minFlingDistance && Math.abs(xVelocity) > velocityThreshold) {
                                        if (xVelocity <= 0) {
                                            if (!isTrustLayout) scrolling(-1) else scrolling(1)
                                        } else {
                                            if (!isTrustLayout) scrolling(1) else scrolling(-1)
                                        }
                                    }
                                }
                                VERTICAL -> {
                                    if (deltaY > minFlingDistance && Math.abs(yVelocity) > velocityThreshold) {
                                        if (yVelocity <= 0) {
                                            if (manager.getReverseLayout()) scrolling(-1) else scrolling(1)
                                        } else {
                                            if (manager.getReverseLayout()) scrolling(1) else scrolling(-1)
                                        }
                                    }
                                }
                            }
                            velocityTracker?.recycle()
                            velocityTracker = null
                        }
                        actionDown = true
                    }
                }
                return false
            }



            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        }
    }

    /**
     * @param dx delta x
     * @param dy delta y
     */
    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        if (listener != null)
            listener!!.onScroll(dx, dy)
    }

    /**
     * @param position scrolling to specific position
     */
    override fun scrollToPosition(position: Int) {
        post {
            try {
                scrollToPosition(position)
            } catch (e: IllegalStateException) {
                // Layout not ready, try again shortly
                postDelayed({
                    try {
                        scrollToPosition(position)
                    } catch (ignored: Exception) { }
                }, 50)
            }
        }
    }


    /**
     * @param state called when the scroll state of this RecyclerView changes
     */
    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == SCROLL_STATE_IDLE) {
            if (isAutoScroll) {
                getScheduler()?.start()
            }

            if (currentPosition == 0)
                reverseLoop = true
            else if (currentPosition == adapter!!.itemCount - 1)
                reverseLoop = false
        }
    }

    /**
     * @param positionPlus scroll to new position from previous position
     */
    fun scrolling(positionPlus: Int, immediately: Boolean = false) {
        val snapPosition = calculateSnapViewPosition()
        val adapterItemCount = adapter?.itemCount ?: return

        if (snapPosition > -1) {
            var targetPosition = snapPosition + positionPlus
            targetPosition = targetPosition.coerceIn(0, adapterItemCount - 1)

            if (immediately) {
                layoutManager?.scrollToPosition(targetPosition)
            } else {
                smoothScrollToPosition(targetPosition)
            }

            listener?.onPositionChange(targetPosition)
            currentPosition = targetPosition
        }
    }

    /**
     * @return position fit in screen for parent list
     */
    private val parentAnchor: Int
        get() = (if (manager.orientation == VERTICAL) height else width) / 2

    /**
     * @param view item view
     * @return position fit in screen specific view in parent
     */
    private fun getViewAnchor(view: View?): Int {
        return if (manager.orientation == VERTICAL) view?.top!! + view.height / 2
        else view?.left!! + view.width / 2
    }

    /**
     * @return calculate snapping position relation anchor
     */
    private fun calculateSnapViewPosition(): Int {
        val parentAnchor = parentAnchor
        val firstVisible = manager.findFirstVisibleItemPosition()
        val lastVisible = manager.findLastVisibleItemPosition()

        if (firstVisible == RecyclerView.NO_POSITION || lastVisible == RecyclerView.NO_POSITION) return -1

        var closestPos = firstVisible
        var minDistance = Int.MAX_VALUE

        for (i in firstVisible..lastVisible) {
            val view = manager.findViewByPosition(i) ?: continue
            val distance = kotlin.math.abs(parentAnchor - getViewAnchor(view))
            if (distance < minDistance) {
                minDistance = distance
                closestPos = i
            }
        }
        return closestPos
    }

    inner class Scheduler(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {

        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            if (isLoopMode) {
                if (reverseLoop)
                    scrolling(+1)
                else
                    scrolling(-1)

            } else {
                if (currentPosition >= adapter!!.itemCount - 1)
                    scrollToPosition(0)
            }
            cancel()
            if (scrolling) start()
        }
    }

    private val maxVelocityX = 2000 // Lower value = more control
    override fun fling(velocityX: Int, velocityY: Int): Boolean {

        val cappedVelocityX = when {
            velocityX > maxVelocityX -> maxVelocityX
            velocityX < -maxVelocityX -> -maxVelocityX
            else -> velocityX
        }
        return super.fling(cappedVelocityX , velocityY)
    }

    companion object {

        //carousel orientation
        const val HORIZONTAL = 0
        const val VERTICAL = 1

        //anchor default
        private val CENTER = 0

    }

    @IntDef(VERTICAL, HORIZONTAL)
    @Retention(AnnotationRetention.SOURCE)
    annotation class CarouselOrientation
}
