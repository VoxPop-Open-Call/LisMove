package it.lismove.app.android.awards.data

enum class AwardType(val value: Int)  {
    EURO(0),
    POINTS(1),
    TOWN_HALL(2),
    SHOP(3);


    companion object {
        fun fromInt(value: Int) = AwardType.values().first { it.value == value }
    }
}