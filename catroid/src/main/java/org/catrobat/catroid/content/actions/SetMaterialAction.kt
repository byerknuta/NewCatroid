package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.raptor.MaterialComponent
import org.catrobat.catroid.stage.StageActivity

class SetMaterialAction : TemporalAction() {
    var scope: Scope? = null
    var objectName: Formula? = null

    var colorR: Formula? = null
    var colorG: Formula? = null
    var colorB: Formula? = null
    var colorA: Formula? = null
    var metallic: Formula? = null
    var roughness: Formula? = null

    var baseColorTexture: Formula? = null
    var normalTexture: Formula? = null
    var metallicRoughnessTexture: Formula? = null

    override fun update(percent: Float) {
        val name = objectName?.interpretString(scope)
        if (name.isNullOrEmpty()) return

        val sceneManager = StageActivity.getActiveStageListener()?.sceneManager ?: return
        val go = sceneManager.findObjectByName(name)

        var material = go?.getComponent(MaterialComponent::class.java)
        if (material == null) {
            material = MaterialComponent()
        }

        val r = colorR?.interpretFloat(scope)
        val g = colorG?.interpretFloat(scope)
        val b = colorB?.interpretFloat(scope)
        val a = colorA?.interpretFloat(scope)
        if (r != null && g != null && b != null && a != null) {
            material.baseColor.set(r / 255f, g / 255f, b / 255f, a / 255f)
        }

        metallic?.interpretFloat(scope)?.let { material.metallic = it / 100f }
        roughness?.interpretFloat(scope)?.let { material.roughness = it / 100f }

        baseColorTexture?.interpretString(scope)?.let { material.baseColorTexturePath = it }
        normalTexture?.interpretString(scope)?.let { material.normalTexturePath = it }
        metallicRoughnessTexture?.interpretString(scope)?.let { material.metallicRoughnessTexturePath = it }

        if (go == null) {
            StageActivity.getActiveStageListener().threeDManager.applyPBRMaterial(name, material)
        } else {
            sceneManager.setMaterialComponent(go, material)
        }
    }
}