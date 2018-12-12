package com.example.ami.coinz


//Making a Coin Data class so I can store easily
//LThe coordinates, the id, the currency and the value of the coin on Firebase or for other variables

data class Coin (var id: String, var coord: List<Double>, var curr: String, var value : String){
}