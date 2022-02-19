package com.joshmermelstein.diabolicaldisksolitaire

import java.util.*

data class BfsNode(
    val entries: List<CheapDisk>,
    val path: List<Move>
)

fun solve(initial: MutableList<CheapDisk>, boardLogic: BoardLogic, winIdx : Int): Move? {
    val seen = hashSetOf<List<CheapDisk>>()
    val q = LinkedList<BfsNode>()

    seen.add(initial)
    q.add(BfsNode(initial, listOf()))

    while (q.isNotEmpty()) {
        val next = q.removeFirst()

        for (src in next.entries.indices) {
            val destinations = boardLogic.getValidDestinations(next.entries, src)
            for (dst in destinations) {
                val candidate = next.entries.toMutableList()  // Makes a copy
                candidate.swap(src, dst)
                if (boardLogic.isSolved(candidate, winIdx)) {
                    return next.path.firstOrNull() ?: Move(src, dst)
                }
                if (!seen.contains(candidate)) {
                    seen.add(candidate)
                    q.add(BfsNode(candidate, next.path + Move(src, dst)))
                }
            }
        }
    }

    return null
}
