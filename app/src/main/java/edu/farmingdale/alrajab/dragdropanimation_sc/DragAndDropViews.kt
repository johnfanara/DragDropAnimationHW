package edu.farmingdale.alrajab.dragdropanimation_sc

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Canvas
import android.graphics.Point
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import edu.farmingdale.alrajab.dragdropanimation_sc.databinding.ActivityDragAndDropViewsBinding
import android.graphics.drawable.AnimationDrawable
import android.media.Image
import android.view.animation.LinearInterpolator

class DragAndDropViews : AppCompatActivity() {
    lateinit var binding: ActivityDragAndDropViewsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDragAndDropViewsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        //sets OnDragListeners for placeholders
        binding.holder01.setOnDragListener(arrowDragListener)
        binding.holder02.setOnDragListener(arrowDragListener)
        binding.holder03.setOnDragListener(arrowDragListener)
        binding.holder04.setOnDragListener(arrowDragListener)
        binding.holder05.setOnDragListener(arrowDragListener)

        //sets OnLongClickListeners for arrows
        binding.upMoveBtn.setOnLongClickListener(onLongClickListener)
        binding.downMoveBtn.setOnLongClickListener(onLongClickListener)
        binding.forwardMoveBtn.setOnLongClickListener(onLongClickListener)
        binding.backMoveBtn.setOnLongClickListener(onLongClickListener)

        //Sets placeholder borders
        binding.holder01.setBackgroundResource(R.drawable.border)
        binding.holder02.setBackgroundResource(R.drawable.border)
        binding.holder03.setBackgroundResource(R.drawable.border)
        binding.holder04.setBackgroundResource(R.drawable.border)
        binding.holder05.setBackgroundResource(R.drawable.border)

        //creates and starts rocket animation
        val rocketAnimationView = findViewById<ImageView>(R.id.rocketAnimationView)
        val rocketAnim = rocketAnimationView.background as? AnimationDrawable
        rocketAnim?.start()

        //creates and listens for start button click to rotate rocket animation
        val startAnimationButton = findViewById<Button>(R.id.startAnimationButton)
        startAnimationButton.setOnClickListener {
            val directions = getDirectionsFromPlaceholders()
            startAnimation(directions, rocketAnimationView)
        }
    }

    override fun onPause() {
        super.onPause()
        (findViewById<ImageView>(R.id.rocketAnimationView).background as? AnimationDrawable)?.stop()
    }

    override fun onResume() {
        super.onResume()
        (findViewById<ImageView>(R.id.rocketAnimationView).background as? AnimationDrawable)?.start()
    }

    //gets directional arrows from placeholders
    fun getDirectionsFromPlaceholders(): List<String> {
        return listOf("UP", "DOWN", "FORWARD", "BACK")

    }

    fun startAnimation(directions: List<String>, rocketImageView: ImageView) {
        var currentRotation = rocketImageView.rotation

        //sets up directional map for animation to rotate
        val animations = directions.map { direction ->
            val rotationAngle = when (direction) {
                "UP" -> 0f
                "DOWN" -> 180f
                "FORWARD" -> 90f
                "BACK" -> -90f
                else -> 0f
            }
            //calculates next rotation
            val nextRotation = (currentRotation + rotationAngle) % 300
            val rotateAnimator = ObjectAnimator.ofFloat(rocketImageView, "rotation", currentRotation, nextRotation)
            currentRotation = nextRotation

            rotateAnimator.duration = 500
            rotateAnimator.interpolator = LinearInterpolator()
            rotateAnimator
        }

        AnimatorSet().apply {
            playSequentially(animations)
            start()
        }
    }



    private val onLongClickListener = View.OnLongClickListener { view: View ->
        (view as? Button)?.let {

            val item = ClipData.Item(it.tag as? CharSequence)

            val dragData = ClipData( it.tag as? CharSequence,
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
            val myShadow = ArrowDragShadowBuilder(it)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.startDragAndDrop(dragData, myShadow, null, 0)
            } else {
                it.startDrag(dragData, myShadow, null, 0)
            }

            true
        }
        false
    }




    private val arrowDragListener = View.OnDragListener { view, dragEvent ->
        (view as? ImageView)?.let {imageView ->
            when (dragEvent.action) {
                //highlights and unhighlights the border of placeholder
                DragEvent.ACTION_DRAG_ENTERED -> {
                    imageView.setBackgroundResource(R.drawable.highlight_border)
                    return@OnDragListener true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    imageView.setBackgroundResource(R.drawable.border)
                    return@OnDragListener true
                }
                //allows dropping arrow into placeholder
                DragEvent.ACTION_DROP -> {
                    val item: ClipData.Item = dragEvent.clipData.getItemAt(0)
                    val lbl = item.text.toString()

                    when (lbl) {
                        "UP" -> view.setImageResource(R.drawable.ic_baseline_arrow_upward_24)
                        "DOWN" -> view.setImageResource(R.drawable.ic_baseline_arrow_downward_24)
                        "FORWARD" -> view.setImageResource(R.drawable.ic_baseline_arrow_forward_24)
                        "BACK" -> view.setImageResource(R.drawable.ic_baseline_arrow_back_24)
                    }
                    imageView.setBackgroundResource(R.drawable.border)
                    return@OnDragListener true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    imageView.setBackgroundResource(R.drawable.border)
                    return@OnDragListener true
                }
                else -> return@OnDragListener true
            }
        }
        false
    }


    private class ArrowDragShadowBuilder(view: View) : View.DragShadowBuilder(view) {
        private val shadow = view.background
        override fun onProvideShadowMetrics(size: Point, touch: Point) {
            val width: Int = view.width
            val height: Int = view.height
            shadow?.setBounds(0, 0, width, height)
            size.set(width, height)
            touch.set(width / 2, height / 2)
        }
        override fun onDrawShadow(canvas: Canvas) {
            shadow?.draw(canvas)
        }
    }


}