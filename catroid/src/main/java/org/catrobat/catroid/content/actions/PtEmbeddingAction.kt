package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.ml.MLBridge

class PtEmbeddingAction : TemporalAction() {
    var scope: Scope? = null
    var layerNameFormula: Formula? = null
    var inputFormula: Formula? = null
    var outputFormula: Formula? = null
    var vocabSizeFormula: Formula? = null
    var embDimFormula: Formula? = null

    override fun update(percent: Float) {
        val layerName = layerNameFormula?.interpretString(scope) ?: "emb1"
        val input = inputFormula?.interpretString(scope) ?: "tokens"
        val output = outputFormula?.interpretString(scope) ?: "vectors"
        val vocab = vocabSizeFormula?.interpretInteger(scope) ?: 100
        val dim = embDimFormula?.interpretInteger(scope) ?: 16

        MLBridge.nativeLayerEmbedding(layerName, input, output, vocab, dim)
    }
}
