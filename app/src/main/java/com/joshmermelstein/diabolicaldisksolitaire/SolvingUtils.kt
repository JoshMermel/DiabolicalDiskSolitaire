package com.joshmermelstein.diabolicaldisksolitaire

import kotlinx.coroutines.yield
import java.util.*

data class BfsNode(
    val entries: List<CheapDisk>,
    val path: List<Move>
)

suspend fun solve(initial: MutableList<CheapDisk>, boardLogic: BoardLogic, winIdx : Int): List<Move> {
    if (boardLogic.isSolved(initial, winIdx)) {
        // TODO(jmerm): toast here?
        return listOf()
    }

    val seen = hashSetOf<List<CheapDisk>>()
    val q = LinkedList<BfsNode>()

    seen.add(initial)
    q.add(BfsNode(initial, listOf()))

    while (q.isNotEmpty()) {
        yield()
        val next = q.removeFirst()

        for (src in next.entries.indices) {
            val destinations = boardLogic.getValidDestinations(next.entries, src)
            for (dst in destinations) {
                val candidate = next.entries.toMutableList()  // Makes a copy
                candidate.swap(src, dst)
                if (boardLogic.isSolved(candidate, winIdx)) {
                    return next.path + Move(src, dst)
                }
                if (!seen.contains(candidate)) {
                    seen.add(candidate)
                    q.add(BfsNode(candidate, next.path + Move(src, dst)))
                }
            }
        }
    }

    // TODO(jmerm): toast here?
    return listOf()
}
