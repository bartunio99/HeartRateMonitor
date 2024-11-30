package com.mobileapp

object pulseSingleton{
    var pulseValue: Int = 0
    private val listeners = mutableListOf<(Int) -> Unit>()

    fun updatePulseValue(newPulse: Int){
        pulseValue = newPulse
        notifyListeners()
    }

    fun addListener(listener: (Int) -> Unit){
        listeners.add(listener)
    }

    private fun notifyListeners(){
        listeners.forEach({it(pulseValue)})
    }
}