package kr.ac.konkuk.recorder

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatImageButton

//Custom View Class

//AppCompatImageButton을 상속받아 context와 attrs를 전달해야함

class RecordButton (
    context: Context,
    attrs: AttributeSet
): AppCompatImageButton(context, attrs) {

    init {
        setBackgroundResource(R.drawable.shape_oval_button)
    }
    fun updateIconWithState(state: State){
        when(state){
            State.BEFORE_RECORDING-> {
                setImageResource(R.drawable.ic_record)
            }
            State.ON_RECORDING-> {
                setImageResource(R.drawable.ic_stop)
            }
            State.AFTER_RECORDING -> {
                setImageResource(R.drawable.ic_play)
            }
            State.ON_PLAYING -> {
                setImageResource(R.drawable.ic_stop)
            }
        }
    }
}

