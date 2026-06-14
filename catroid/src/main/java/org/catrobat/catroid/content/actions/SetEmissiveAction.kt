package org.catrobat.catroid.content.actions

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class SetEmissiveAction : TemporalAction() {
    var scope: Scope? = null
    var objectName: Formula? = null

    var colorR: Formula? = null
    var colorG: Formula? = null
    var colorB: Formula? = null
    var colorA: Formula? = null
    var intensity: Formula? = null
    var texturePath: Formula? = null

    override fun update(percent: Float) {
        val name = objectName?.interpretString(scope)
        if (name.isNullOrEmpty()) return

        val sceneManager = StageActivity.getActiveStageListener()?.sceneManager ?: return
        val go = sceneManager.findObjectByName(name)

        val r = (colorR?.interpretFloat(scope) ?: 255f) / 255f
        val g = (colorG?.interpretFloat(scope) ?: 255f) / 255f
        val b = (colorB?.interpretFloat(scope) ?: 255f) / 255f
        val a = (colorA?.interpretFloat(scope) ?: 255f) / 255f
        val intent = intensity?.interpretFloat(scope) ?: 1f
        val tex = texturePath?.interpretString(scope) ?: ""

        val gdxColor = Color(r, g, b, a)

        if (go != null) {
            var material = go.getComponent(org.catrobat.catroid.raptor.MaterialComponent::class.java)
            if (material == null) {
                material = org.catrobat.catroid.raptor.MaterialComponent()
                go.addComponent(material)
            }
            material.emissiveColor.set(gdxColor)
            material.emissiveIntensity = intent
            material.emissiveTexturePath = tex.ifEmpty { null }

            sceneManager.setMaterialComponent(go, material)
        }

        StageActivity.getActiveStageListener()?.threeDManager?.setObjectEmissive(name, gdxColor, intent, tex)
    }
}
