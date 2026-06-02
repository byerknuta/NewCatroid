package org.catrobat.catroid.utils

import org.catrobat.catroid.content.bricks.Brick

object BrickCollapseManager {
    private val collapsedBricks = HashSet<Int>()

    fun isCollapsed(brick: Brick): Boolean {
        return collapsedBricks.contains(brick.hashCode())
    }

    fun setCollapsed(brick: Brick, collapsed: Boolean) {
        if (collapsed) {
            collapsedBricks.add(brick.hashCode())
        } else {
            collapsedBricks.remove(brick.hashCode())
        }
    }

    fun toggleCollapsed(brick: Brick) {
        setCollapsed(brick, !isCollapsed(brick))
    }

    fun clear() {
        collapsedBricks.clear()
    }
}
