package kr.ac.konkuk.recorder


//버튼의 모양을 바꿔야 하기에 ENUM Class를 선언해줌
enum class State {
    BEFORE_RECORDING,
    ON_RECORDING,
    AFTER_RECORDING,
    ON_PLAYING
}