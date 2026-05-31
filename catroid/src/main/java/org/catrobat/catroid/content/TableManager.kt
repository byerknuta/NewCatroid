package org.catrobat.catroid.content

import com.danvexteam.lunoscript_annotations.LunoClass
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

@LunoClass
class TableManager {
    companion object {
        const val DEFAULT_VALUE = "0"

        private val tables: ConcurrentHashMap<String, TableData> = ConcurrentHashMap()

        class TableData {
            val cells = ConcurrentHashMap<Long, Any>()
            @Volatile var maxX: Int = 0
            @Volatile var maxY: Int = 0

            fun getKey(x: Int, y: Int): Long = (x.toLong() shl 32) or (y.toLong() and 0xFFFFFFFFL)

            @Synchronized
            fun insert(x: Int, y: Int, value: Any) {
                if (x <= 0 || y <= 0) return
                cells[getKey(x, y)] = value
                maxX = max(maxX, x)
                maxY = max(maxY, y)
            }

            fun get(x: Int, y: Int): Any = cells[getKey(x, y)] ?: DEFAULT_VALUE
        }

        fun createTable(name: String, x: Int, y: Int) {
            if (!tables.containsKey(name)) {
                val newTable = TableData()
                for (i in 1..x) {
                    for (j in 1..y) {
                        newTable.insert(i, j, DEFAULT_VALUE)
                    }
                }
                tables[name] = newTable
            }
        }

        fun insertElement(name: String, value: Any?, x: Int, y: Int) {
            if (value != null) tables[name]?.insert(x, y, value)
        }

        fun insertElement(name: String, value: Int, x: Int, y: Int) {
            tables[name]?.insert(x, y, value)
        }

        fun deleteTable(name: String) {
            tables.remove(name)
        }

        fun deleteAllTables() {
            tables.clear()
        }

        fun getTableXSize(name: String): Int = tables[name]?.maxX ?: 0
        fun getTableYSize(name: String): Int = tables[name]?.maxY ?: 0

        fun getElementValue(name: String, x: Int, y: Int): String? {
            return tables[name]?.get(x, y)?.toString()
        }

        fun stringToTable(name: String, data: String, xDelimiter: String, yDelimiter: String) {
            val table = tables[name] ?: return
            table.cells.clear()
            table.maxX = 0
            table.maxY = 0

            val rows = data.split(yDelimiter)
            rows.forEachIndexed { y, rowString ->
                val columns = rowString.split(xDelimiter)
                columns.forEachIndexed { x, value ->
                    val trimmed = value.trim()
                    val finalValue: Any = trimmed.toIntOrNull() ?: trimmed
                    table.insert(x + 1, y + 1, finalValue)
                }
            }
        }

        fun tableToString(name: String, xDelimiter: String, yDelimiter: String): String {
            val table = tables[name] ?: return ""
            val xSize = table.maxX
            val ySize = table.maxY

            if (xSize == 0 || ySize == 0) return ""

            val builder = StringBuilder()
            for (y in 1..ySize) {
                for (x in 1..xSize) {
                    builder.append(table.get(x, y))
                    if (x < xSize) builder.append(xDelimiter)
                }
                if (y < ySize) builder.append(yDelimiter)
            }
            return builder.toString()
        }

        fun getTable(name: String): Map<Pair<Int, Int>, Any?>? {
            val table = tables[name] ?: return null
            val legacyMap = mutableMapOf<Pair<Int, Int>, Any?>()
            table.cells.forEach { (key, value) ->
                val x = (key shr 32).toInt()
                val y = key.toInt()
                legacyMap[Pair(x, y)] = value
            }
            return legacyMap
        }
    }
}
