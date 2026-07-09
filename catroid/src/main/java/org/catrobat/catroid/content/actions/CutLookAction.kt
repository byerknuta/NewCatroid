/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2024 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.catroid.content.actions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.CatroidApplication
import org.catrobat.catroid.common.LookData
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.utils.ErrorLog
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

class CutLookAction() : TemporalAction() {
    var scope: Scope? = null
    var x1: Formula? = null
    var y1: Formula? = null
    var x2: Formula? = null
    var y2: Formula? = null

    private var hasExecuted = false

    override fun update(percent: Float) {
        if (hasExecuted) return
        hasExecuted = true

        val s = scope ?: return
        val x1_i: Int = x1?.interpretInteger(s) ?: 0
        val y1_i: Int = y1?.interpretInteger(s) ?: 0
        val x2_i: Int = x2?.interpretInteger(s) ?: 0
        val y2_i: Int = y2?.interpretInteger(s) ?: 0
        val lookData: LookData = s.sprite?.look?.lookData ?: return

        val file: File = lookData.file
        try {
            val originalBitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return

            val bitmapWidth = originalBitmap.width
            val bitmapHeight = originalBitmap.height

            val safeX1 = x1_i.coerceIn(0, bitmapWidth)
            val safeY1 = y1_i.coerceIn(0, bitmapHeight)
            val safeX2 = x2_i.coerceIn(0, bitmapWidth)
            val safeY2 = y2_i.coerceIn(0, bitmapHeight)

            val cropX = minOf(safeX1, safeX2)
            val cropY = minOf(safeY1, safeY2)
            val width = abs(safeX2 - safeX1)
            val height = abs(safeY2 - safeY1)

            if (width > 0 && height > 0) {
                val croppedBitmap = Bitmap.createBitmap(originalBitmap, cropX, cropY, width, height)

                val context = CatroidApplication.getAppContext()
                val tempFile = File.createTempFile("cropped_look_", ".png", context?.cacheDir)
                FileOutputStream(tempFile).use { out ->
                    croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                val newLook = createlook(tempFile)
                setLook(newLook)

                croppedBitmap.recycle()
            }
            originalBitmap.recycle()

        } catch (e: Exception) {
            ErrorLog.log(e.message)
            e.printStackTrace()
        }
    }

    fun createlook(file: File): LookData {
        val look = LookData(file.name, file)
        look.collisionInformation.calculate()
        return look
    }

    fun setLook(look: LookData) {
        updateLookListIndex()
        scope?.sprite?.look?.lookData = look
        look.isWebRequest = false
    }

    private fun updateLookListIndex() {
        val currentLook = scope?.sprite?.look
        if (!(currentLook != null && currentLook.lookListIndexBeforeLookRequest > -1)) {
            scope?.sprite?.look?.lookListIndexBeforeLookRequest =
                scope?.sprite?.lookList?.indexOf(scope?.sprite?.look?.lookData) ?: -1
        }
    }
}
