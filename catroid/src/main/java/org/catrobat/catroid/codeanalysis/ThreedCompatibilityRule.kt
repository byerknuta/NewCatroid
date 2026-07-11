package org.catrobat.catroid.codeanalysis

import android.content.Context
import org.catrobat.catroid.R
import org.catrobat.catroid.ProjectManager
import org.catrobat.catroid.content.bricks.*

class ThreedCompatibilityRule(private val context2: Context) : AnalysisRule {
    override fun analyze(brick: Brick, context: GlobalAnalysisContext): AnalysisResult? {
        val currentSprite = ProjectManager.getInstance().currentSprite ?: return null

        val isPbrEnabledInProject = context.isPbrEnabledInProject

        val currentSpriteBricks = mutableListOf<Brick>()
        for (script in currentSprite.scriptList) {
            script.addToFlatList(currentSpriteBricks)
        }
        val pbrBrickIndexInCurrent = currentSpriteBricks.indexOfFirst { it is EnablePbrRenderBrick && !CodeAnalyzer.isBrickCommented(it) }
        val isPbrEnabledInCurrent = pbrBrickIndexInCurrent != -1

        if (isPbrEnabledInProject) {
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
                    context2.getString(R.string.analysis_3d_render_1_only)
                )
            }

            if (brick is CreateCubeBrick || brick is CreateSphereBrick || brick is ThreedCreateCylinderBrick) {
                val brickIndex = currentSpriteBricks.indexOf(brick)
                if (isPbrEnabledInCurrent && brickIndex != -1 && brickIndex < pbrBrickIndexInCurrent) {
                    return AnalysisResult(
                        Severity.ERROR,
                        context2.getString(R.string.analysis_3d_primitive_before_pbr)
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
                    context2.getString(R.string.analysis_3d_render_2_only)
                )
            }
        }

        return null
    }
}
