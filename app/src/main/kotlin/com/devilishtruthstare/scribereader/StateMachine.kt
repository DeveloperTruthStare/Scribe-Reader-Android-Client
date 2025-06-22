package com.devilishtruthstare.scribereader

class StateMachine<T> (private var state: T) {
    private var stateMap: MutableMap<T, (() -> Pair<T, Boolean>)> = mutableMapOf()
    fun nextState() {
        var res = stateMap[state]!!.invoke()
        state = res.first
        while (res.second) {
            res = stateMap[state]!!.invoke()
            state = res.first
        }
    }
    fun setState(s: T, f: (() -> Pair<T, Boolean>)) {
        stateMap[s] = f
    }
}