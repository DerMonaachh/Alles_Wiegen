package com.example.alleswiegen

class PlayerRank(
    val name: String,

    val time: Long,
    val number: Long,
    val averageError: Double,
    val bagelRate: Double,
    val bagels:Int,
    val pointsByErrorClassic: Int,
    val pointsByErrorNoExxer: Int,

)
{
    override fun toString(): String {
        return "Player(name='$name', number=${number.toInt()} points=${pointsByErrorClassic} )"
    }

}