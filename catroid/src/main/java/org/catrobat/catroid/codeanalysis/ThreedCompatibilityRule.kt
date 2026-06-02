package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.content.bricks.*

class ThreedCompatibilityRule(private val context: Context) : AnalysisRule {
    override fun analyze(brick: Brick): AnalysisResult? {
        val sprite = org.catrobat.catroid.ProjectManager.getInstance().currentSprite ?: return null

        val allBricks = mutableListOf<Brick>()
        for (script in sprite.scriptList) {
            script.addToFlatList(allBricks)
        }

        val pbrBrickIndex = allBricks.indexOfFirst { it is EnablePbrRenderBrick }
        val isPbrEnabled = pbrBrickIndex != -1

        if (isPbrEnabled) {

            val isRender1Only = brick is SetObjectColorBrick ||
                    brick is SetObjectTextureBrick ||
                    brick is SetAmbientLightBrick ||
                    brick is SetDirectionalLightBrick ||
                    brick is SetShaderCodeBrick ||
                    brick is SetShaderUniformFloatBrick ||
                    brick is SetShaderUniformVec3Brick

            if (isRender1Only) {
                return AnalysisResult(
                    Severity.WARNING,
                    context.getString(R.string.analysis_3d_render_1_only)
                )
            }

            if (brick is CreateCubeBrick || brick is CreateSphereBrick || brick is ThreedCreateCylinderBrick) {
                val brickIndex = allBricks.indexOf(brick)
                if (brickIndex != -1 && brickIndex < pbrBrickIndex) {
                    return AnalysisResult(
                        Severity.ERROR,
                        context.getString(R.string.analysis_3d_primitive_before_pbr)
                    )
                }
            }

        } else {
            val isRender2Only = brick is SetMaterialBrick ||
                    brick is SetPostProcessingNewBrick ||
                    brick is SetPostProcessingBrick ||
                    brick is SetShadowQualityBrick ||
                    brick is SetSkyboxBrick ||
                    brick is SetPointLightBrick ||
                    brick is SetSpotLightBrick ||
                    brick is SetDirectionalLight2Brick ||
                    brick is SetBackgroundLightBrick ||
                    brick is RemovePbrLightBrick ||
                    brick is SetTextureTilingBrick ||
                    brick is SetAnisotropicFilterBrick ||
                    brick is CreateParticlesBrick ||
                    brick is SetParticleEmissionBrick ||
                    brick is DeleteParticlesBrick

            if (isRender2Only) {
                return AnalysisResult(
                    Severity.ERROR,
                    context.getString(R.string.analysis_3d_render_2_only)
                )
            }
        }

        return null
    }
}
