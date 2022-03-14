package com.joshmermelstein.diabolicaldisksolitaire


// A BoardLogic represents the rules for what moves are possible, independent
// of the current state of the board or the way it is drawn. This is key to
// decoupling game logic and UI logic.
// TODO(jmerm): consider adding a kind of "circular lane" logic handling
class BoardLogic(lanes: List<List<Int>>) {
    private val numEntries: Int = 1 + lanes.maxOf { it.maxOrNull() ?: 0 }
    private val rays = computeRays(lanes)
    private fun computeRays(lanes: List<List<Int>>): List<List<List<Int>>> {
        val ret = MutableList<MutableList<List<Int>>>(numEntries) { mutableListOf() }
        for (lane in lanes) {
            for (i in lane.indices) {
                val tmp = lane.slice((i + 1 until lane.size))
                if (tmp.isNotEmpty()) {
                    ret[lane[i]].add(tmp)
                }
                val reverseTmp = lane.asReversed().slice(i + 1 until lane.size)
                if (reverseTmp.isNotEmpty()) {
                    ret[lane[lane.size - i - 1]].add(reverseTmp)
                }
            }
        }
        return ret
    }

    private fun getDestinationAlongRay(entries: List<CheapDisk>, idx: Int, ray: List<Int>): Int? {
        if (entries[idx].size == 0 || entries[idx].isVoid || entries[idx].isFixed) {
            // nothing to move
            return null
        }
        if (ray.isEmpty() || entries[ray[0]].size == 0 || entries[ray[0]].isVoid) {
            // nothing to jump over!
            return null
        }
        for (dst in ray.drop(1)) {
            if (entries[dst].size == 0) {
                return if (isValidMove(idx, dst, entries) && !entries[dst].isVoid) {
                    dst
                } else {
                    null
                }
            }
        }
        return null
    }

    // Returns whether a move from |srcIdx| to |dstIdx| is allowed.
    private fun isValidMove(srcIdx : Int, dstIdx: Int, entries: List<CheapDisk>): Boolean {
        val maxSize = 4 - entries[srcIdx].size
        return rays[dstIdx].map { ray -> ray[0] }.filter { neighbor -> neighbor != srcIdx }
            .map { neighbor -> entries[neighbor].size <= maxSize }.all { it }
    }

    fun getValidDestinations(entries: List<CheapDisk>, idx: Int): List<Int> =
        rays[idx].mapNotNull { getDestinationAlongRay(entries, idx, it) }

    fun isSolved(entries: List<CheapDisk>, winIdx: Int): Boolean = entries[winIdx].isWin
}