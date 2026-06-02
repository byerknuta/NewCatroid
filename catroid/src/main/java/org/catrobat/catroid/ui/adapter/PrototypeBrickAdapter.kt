package org.catrobat.catroid.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.BaseAdapter
import org.catrobat.catroid.content.bricks.Brick

class PrototypeBrickAdapter(private var brickList: List<Brick>) : BaseAdapter() {

    private val viewCache = HashMap<Int, View>()
    private val itemsToAnimate = HashMap<Brick, Long>()

    override fun getCount(): Int = brickList.size

    override fun getItem(position: Int): Brick = brickList[position]

    override fun getItemId(position: Int): Long = brickList[position].hashCode().toLong()

    override fun hasStableIds(): Boolean = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val brick = brickList[position]
        val cacheKey = brick.hashCode()

        var cachedView = viewCache[cacheKey]
        if (cachedView == null) {
            cachedView = brick.getPrototypeView(parent?.context)
            if (cachedView != null) {
                viewCache[cacheKey] = cachedView
            }
        } else {
            (cachedView.parent as? ViewGroup)?.removeView(cachedView)
        }

        if (cachedView != null) {
            if (itemsToAnimate.containsKey(brick)) {
                val delay = itemsToAnimate.remove(brick) ?: 0L

                cachedView.alpha = 0f
                cachedView.scaleX = 0.93f
                cachedView.scaleY = 0.93f

                cachedView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(delay)
                    .setDuration(250)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            } else {
                cachedView.animate().cancel()
                cachedView.alpha = 1f
                cachedView.scaleX = 1f
                cachedView.scaleY = 1f
                cachedView.translationX = 0f
                cachedView.translationY = 0f
            }
        }

        return cachedView
    }

    fun replaceList(newList: List<Brick>) {
        itemsToAnimate.clear()
        var newItemsCount = 0

        for (brick in newList) {
            if (!brickList.contains(brick)) {
                itemsToAnimate[brick] = newItemsCount * 40L
                newItemsCount++
            }
        }

        brickList = newList
        notifyDataSetChanged()
    }

    fun clearCache() {
        viewCache.clear()
        itemsToAnimate.clear()
    }
}
