package com.joshmermelstein.diabolicaldisksolitaire

import android.util.Log
import java.util.*

data class BfsNode(
    val entries: List<CheapDisk>,
    val path: List<Move>
)

fun solve(initial: MutableList<CheapDisk>, boardLogic: BoardLogic): Move? {
    val seen = hashSetOf<List<CheapDisk>>()
    val q = LinkedList<BfsNode>()

    seen.add(initial)
    q.add(BfsNode(initial, listOf()))

    /*
    for (i in initial.indices) {
        val dests = boardLogic.getValidDestinations(initial, i)
        Log.d("jmerm", "$i, $dests")
    }
    return null
     */

    while (q.isNotEmpty()) {
        val next = q.removeFirst()

        for (i in next.entries.indices) {
            val dests = boardLogic.getValidDestinations(next.entries, i)
            for (dest in dests) {
                val candidate = next.entries.toMutableList()
                val tmp = candidate[i]
                candidate[i] = candidate[dest]
                candidate[dest] = tmp
                if (boardLogic.isSolved(candidate)) {
                    Log.d("jmerm", "${q.size}, ${seen.size}")
                    return next.path.firstOrNull() ?: Move(i, dest)
                }
                if (!seen.contains(candidate)) {
                    seen.add(candidate)
                    q.add(BfsNode(candidate, next.path + Move(i, dest)))
                }
            }
        }
        if (q.size > 4000) {
            return null
        }
    }

    return null
}
