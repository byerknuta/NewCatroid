package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import com.badlogic.gdx.graphics.Color
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity

class ConfigureMaterialAction : TemporalAction() {
    var scope: Scope? = null
    var objectId: Formula? = null
    var value: Formula? = null
    var configTypeSelection: Int = 0

    override fun update(percent: Float) {
        val idStr = objectId?.interpretString(scope) ?: ""
        val valStr = value?.interpretString(scope) ?: ""

        if (idStr.isEmpty()) return

        val engine = StageActivity.getActiveStageListener()?.threeDManager ?: return

        try {
            val instance = engine.getModelInstance(idStr) ?: return

            for (material in instance.materials) {
                when (configTypeSelection) {
                    0 -> {
                        val rgba = valStr.split(",").map { it.trim().toFloatOrNull() ?: 1f }
                        if (rgba.size >= 3) {
                            val alpha = if (rgba.size >= 4) rgba[3] else 1.0f
                            val color = Color(rgba[0], rgba[1], rgba[2], alpha)
                            material.set(PBRColorAttribute.createBaseColorFactor(color))
                        }
                    }
                    1 -> {
                        val metallic = valStr.toFloatOrNull() ?: 0.0f
                        material.set(PBRFloatAttribute.createMetallic(metallic.coerceIn(0f, 1f)))
                    }
                    2 -> {
                        val roughness = valStr.toFloatOrNull() ?: 1.0f
                        material.set(PBRFloatAttribute.createRoughness(roughness.coerceIn(0f, 1f)))
                    }
                    3 -> {
                        val uv = valStr.split(",").map { it.trim().toFloatOrNull() ?: 1f }
                        if (uv.size >= 2) {
                            for (attr in material) {
                                if (attr is com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute) {
                                    attr.scaleU = uv[0]
                                    attr.scaleV = uv[1]
                                }
                            }
                        }
                    }
                    4 -> {
                        val rgb = valStr.split(",").map { it.trim().toFloatOrNull() ?: 0f }
                        if (rgb.size >= 3) {
                            val color = Color(rgb[0], rgb[1], rgb[2], 1.0f)
                            material.set(PBRColorAttribute.createEmissive(color))
                        }
                    }
                    5 -> {
                        val intensity = valStr.toFloatOrNull() ?: 1.0f
                        material.set(PBRFloatAttribute.createEmissiveIntensity(intensity))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
