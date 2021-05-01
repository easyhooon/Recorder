package kr.ac.konkuk.recorder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*

class SoundVisualizerView (
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs){

    var onRequestCurrentAmplitude: (()-> Int)? = null

    //곡선이 부드럽게 그려게 하는 FLAG
    val amplitudePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.purple_500)
        strokeWidth = LINE_WIDTH
        strokeCap = Paint.Cap.ROUND
    }
    private var drawingWidth: Int = 0
    private var drawingHeight: Int = 0
    private var drawingAmplitudes: List<Int> = emptyList()
    private var isReplaying: Boolean = false
    private var replayingPosition: Int = 0
//    var drawingAmplitudes: List<Unit> = (0..10).map{ Random.nextInt(Short.MAX_VALUE.toInt())}

    //시간경과에 따라서 bar가 쌓이면서 우측에서 좌측으로 이동하는 것을 구현
    private val visualizeRepeatAction: Runnable = object : Runnable {
        override fun run() {
            if(!isReplaying) {
                //현재 메인액티비티가 가지고있는 오디오의 maxAmplitude값을 가져올 수 있음
                val currentAmplitude = onRequestCurrentAmplitude?.invoke() ?: 0
                // Amplitude, Draw
                //현재의 amplitude가 제일 앞에 오도록
                //리스트의 제일 처음이 제일 마지막꺼 리스트의 제일 마지막이 제일 처음꺼
                drawingAmplitudes = listOf(currentAmplitude) + drawingAmplitudes
            } else {
                //drawing할 범위를 계속 늘려줌
                //실제 그리는 작업에선 오른쪽에서 왼쪽으로 밀리는게 아니라 그림을 갈아끼우면서 점점 세로바의 갯수를 늘리는 것
                replayingPosition++
            }
            invalidate()
            //이게 있어야 onDraw가 다시 호출 될 수 있음
            //리스트의 제일 마지막 부터 가져와서 replay (점점 확대)

            //내 자신을 20ms 뒤에 다시 실행
            //명시적으로 종료를 하기전까진 Amplitude를 가져와서 Drawing하는 동작을 반복적으로 실행하게됨
            handler?.postDelayed(this, ACTION_INTERVAL)

        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingWidth = w
        drawingHeight = h
    }

    //Canvas -> What to draw
    //Paint -> How to draw

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        val centerY = drawingHeight / 2f
        var offsetX = drawingWidth.toFloat()

        drawingAmplitudes
            .let { amplitudes ->
                if(isReplaying) {
                    amplitudes.takeLast(replayingPosition)
                } else {
                    amplitudes
                }
            }
            .forEach{ amplitude->
            //그리려는 높이 대비 몇 퍼센트를 그릴거라는 계산
                val lineLength = amplitude / MAX_AMPLITUDE * drawingHeight * 0.8F

                offsetX -= LINE_SPACE
                if(offsetX < 0) return@forEach

                canvas.drawLine(
                    offsetX,
                    centerY - lineLength / 2F,
                    offsetX,
                    centerY + lineLength / 2F,
                    amplitudePaint
                )
        }
    }

    fun startVisualizing(isReplaying: Boolean) {
        this.isReplaying = isReplaying
        handler?.post(visualizeRepeatAction)
    }

    fun stopVisualizing() {
        //리플레잉 포지션을 초기화
        replayingPosition = 0
        handler?.removeCallbacks(visualizeRepeatAction)
    }

    fun clearVisualization() {
        drawingAmplitudes = emptyList()
        invalidate()
    }

    companion object {
        private const val LINE_WIDTH = 10F
        private const val LINE_SPACE = 15F
        private const val MAX_AMPLITUDE = Short.MAX_VALUE.toFloat()
        private const val ACTION_INTERVAL = 20L
    }
}