package kr.ac.konkuk.recorder

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    //프로퍼티 정의 부분
    private val soundVisualizerView: SoundVisualizerView by lazy {
        findViewById(R.id.soundVisualizerView)
    }

    private val recordTimeTextView: CountUpView by lazy {
        findViewById(R.id.tv_recordTime)
    }

    private val resetButton: Button by lazy {
        findViewById(R.id.btn_reset)
    }

    private val recordButton: RecordButton by lazy {
        findViewById(R.id.btn_record)
    }
    private val requiredPermission = arrayOf(Manifest.permission.RECORD_AUDIO)

    //MediaRecorder Setup
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    //버튼의 변화된 상태값에 따라 아이콘의 변화를 주어야함. 이 구현은 state에 setter에서
    //value: 새로 할당된 값
    private var state = State.BEFORE_RECORDING
        set(value) {
            field = value //실제 프로퍼티에 새로할당된 값을 넣어줌

            //resetButton을 녹음이 끝난 후와 플레이상태에서만 활성화
            resetButton.isEnabled = (value == State.AFTER_RECORDING) || (value == State.ON_PLAYING)
            recordButton.updateIconWithState(value)
        }

    private val recordingFilePath: String by lazy {
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //앱을 시작하자마자 권한 요청
        requestAudioPermission()
        initView()
        bindViews()
        initVariables()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //해당 메소드가 뭔지 모르겠으면 컨트룰+좌클릭으로 정의를 봐라 다 알게된다.
        val audioRecordPermissionGranted =
            requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                    grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if(!audioRecordPermissionGranted) {
            //거절할 경우 앱을 종료시킴
           finish()
        }

    }

    private fun requestAudioPermission() {
        //마이크 권한 요청
        requestPermissions(requiredPermission, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun initView() {
        //현재 state를 전달
        recordButton.updateIconWithState(state)
    }

    private fun bindViews() {
        soundVisualizerView.onRequestCurrentAmplitude = {
            //maxAmplitude값 설정
            recorder?.maxAmplitude ?: 0
        }
        resetButton.setOnClickListener {
            stopPlaying()
            soundVisualizerView.clearVisualization()
            recordTimeTextView.clearCountTime()

            state = State.BEFORE_RECORDING
        }

        recordButton.setOnClickListener {
            when(state) {
                State.BEFORE_RECORDING -> {
                    startRecording()
                }
                State.ON_RECORDING -> {
                    stopRecording()
                }
                State.AFTER_RECORDING-> {
                    startPlaying()
                }
                State.ON_PLAYING-> {
                    stopPlaying()
                }
            }
        }
    }

    private fun initVariables() {
        state = State.BEFORE_RECORDING
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            //마이크에 접근
            setAudioSource(MediaRecorder.AudioSource.MIC)
            //OutputFile 지정
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            //Encorder 지정
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            //녹음된 오디오를 압축해서 저장
            //If internal storage doesn't provide enough space to store app-specific files, consider using external storage instead.
            //외부 캐시 디렉토리에 접근을 해서 임시적으로 녹음할 파일을 저장하되, 이 앱이 지워진다거나 안드로이드 디바이스 내에서
            //용량 확보가 필요할때는 날라가도록
            setOutputFile(recordingFilePath)
            //호출 준비
            prepare()
        }

        recorder?.start()

        soundVisualizerView.startVisualizing(false)
        //버튼 상태변화
        recordTimeTextView.startCountUp()
        state= State.ON_RECORDING
    }

    private fun stopRecording() {
        recorder?.run {
            stop()
            //메모리 해제
            release()
        }
        recorder = null
        soundVisualizerView.stopVisualizing()
        recordTimeTextView.stopCountUp()
        //버튼 상태변화
        state= State.AFTER_RECORDING

    }

    private fun startPlaying() {
        player = MediaPlayer()
            .apply {
                setDataSource(recordingFilePath)
                prepare()
                //재생을 할 수 있는 상태
            }

        //재생할 파일의 길이를 알아와서 완료처리
        //현재 전달된 파일을 재생 완료했을 때 발생하는 이벤트
        player?.setOnCompletionListener {
            stopPlaying()
            state = State.AFTER_RECORDING
        }

        player?.start()
        soundVisualizerView.startVisualizing(true)
        recordTimeTextView.startCountUp()
        //버튼 상태변화
        state= State.ON_PLAYING
    }

    private fun stopPlaying() {
        player?.release()
        player = null
        //버튼 상태변화
        soundVisualizerView.stopVisualizing()
        recordTimeTextView.stopCountUp()
        //버튼 상태변화
        state= State.AFTER_RECORDING
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201

    }
}