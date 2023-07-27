package it.lismove.app.android.deviceConfiguration

object WheelUtils {
    val dimen = arrayOf("29\"", "28\"(700mm)", "27.5\"(650mm)",  "27\"", "26\"",
        "24\"(600mm)","22\"(550mm)", "20\"(500mm)", "18\"(450mm)",
        "16\"(400mm)","14\"(350mm)", "12\"",)

    val mm = arrayOf(736, 700, 650, 685, 660,
        600,550, 500, 450,
        400,350, 300,)

    fun getWheelInMM(textValue: String): Int{
        val index = dimen.indexOf(textValue)
        return mm[index]
    }
    fun getWheelString(value: Int): String{
        val index = mm.indexOf(value)
        return dimen[index]
    }
}