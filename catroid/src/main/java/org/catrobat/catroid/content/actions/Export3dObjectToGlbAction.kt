package org.catrobat.catroid.content.actions

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Action
import org.catrobat.catroid.CatroidApplication
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.stage.StageActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import com.badlogic.gdx.math.Matrix4

class Export3dObjectToGlbAction : Action() {
    var scope: Scope? = null
    var objectId: Formula? = null
    var destFileName: Formula? = null

    private var started = false
    @Volatile private var finished = false

    override fun act(delta: Float): Boolean {
        if (!started) {
            started = true

            val idStr = objectId?.interpretString(scope) ?: ""
            var destStr = destFileName?.interpretString(scope) ?: "model.glb"
            val projectDir = scope?.project?.getFilesDir()

            if (projectDir == null || idStr.isEmpty()) {
                finished = true
                return true
            }

            if (!destStr.lowercase().endsWith(".glb")) {
                destStr += ".glb"
            }

            val finalDestFile = File(projectDir, destStr)
            finalDestFile.parentFile?.mkdirs()

            val context = CatroidApplication.getAppContext()
            val tempDir = File(context.cacheDir, "glb_temp_export_${System.currentTimeMillis()}")
            tempDir.mkdirs()
            val tempGltfFile = File(tempDir, "temp_model.gltf")

            Gdx.app.postRunnable {
                try {
                    val threeDManager = StageActivity.getActiveStageListener()?.threeDManager
                    val instance = threeDManager?.getModelInstance(idStr)

                    if (instance != null) {
                        ensurePbrMaterials(instance)

                        val originalTransforms = ArrayList<Matrix4>()
                        val nodesSize = instance.nodes.size

                        for (i in 0 until nodesSize) {
                            originalTransforms.add(Matrix4(instance.nodes.get(i).localTransform))
                        }

                        for (i in 0 until nodesSize) {
                            instance.nodes.get(i).localTransform.mulLeft(instance.transform)
                        }

                        val sceneToExport = net.mgsx.gltf.scene3d.scene.Scene(instance)
                        val fileHandle = Gdx.files.absolute(tempGltfFile.absolutePath)

                        net.mgsx.gltf.exporters.GLTFExporter().export(sceneToExport, fileHandle)

                        for (i in 0 until nodesSize) {
                            instance.nodes.get(i).localTransform.set(originalTransforms[i])
                        }

                        val success = GltfToGlbConverter.convert(tempGltfFile, finalDestFile, projectDir)

                        if (success) {
                            Gdx.app.log("GLB_Export", "Successfully packed and exported $idStr to ${finalDestFile.name}")
                        } else {
                            Gdx.app.error("GLB_Export", "Failed to pack files into GLB")
                        }
                    } else {
                        Gdx.app.error("GLB_Export", "Object $idStr not found in ThreeDManager")
                    }
                } catch (e: Exception) {
                    Gdx.app.error("GLB_Export", "Error exporting object $idStr", e)
                    e.printStackTrace()
                } finally {
                    tempDir.deleteRecursively()
                    finished = true
                }
            }
        }
        return finished
    }

    private fun ensurePbrMaterials(instance: com.badlogic.gdx.graphics.g3d.ModelInstance) {
        val materialsSize = instance.materials.size
        for (i in 0 until materialsSize) {
            val material = instance.materials.get(i)

            if (material.has(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.Diffuse) &&
                !material.has(net.mgsx.gltf.scene3d.attributes.PBRColorAttribute.BaseColorFactor)) {

                val diffuse = material.get(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.Diffuse) as com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
                material.set(net.mgsx.gltf.scene3d.attributes.PBRColorAttribute.createBaseColorFactor(diffuse.color))
            }

            if (material.has(com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute.Diffuse) &&
                !material.has(net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute.BaseColorTexture)) {

                val texAttr = material.get(com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute.Diffuse) as com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
                material.set(net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute.createBaseColorTexture(texAttr.textureDescription.texture))
            }

            if (!material.has(net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute.Metallic)) {
                material.set(net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute.createMetallic(0.0f))
            }
            if (!material.has(net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute.Roughness)) {
                material.set(net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute.createRoughness(1.0f))
            }
        }
    }

    override fun restart() {
        super.restart()
        started = false
        finished = false
    }

    override fun reset() {
        super.reset()
        started = false
        finished = false
        scope = null
        objectId = null
        destFileName = null
    }
}

object GltfToGlbConverter {
    fun convert(gltfFile: File, glbFile: File, projectDir: File): Boolean {
        try {
            val tempDir = gltfFile.parentFile
            val jsonStr = gltfFile.readText(Charsets.UTF_8)
            val json = JSONObject(jsonStr)

            val binPayloads = mutableListOf<ByteArray>()
            var currentOffset = 0

            val buffers = json.optJSONArray("buffers")
            if (buffers != null && buffers.length() > 0) {
                val mainBuffer = buffers.getJSONObject(0)
                val uri = mainBuffer.optString("uri")
                if (uri.isNotEmpty()) {
                    val binFile = File(tempDir, uri)
                    if (binFile.exists()) {
                        val bytes = binFile.readBytes()
                        val paddedLen = padLength(bytes.size)
                        val paddedBytes = bytes.copyOf(paddedLen)

                        binPayloads.add(paddedBytes)

                        mainBuffer.remove("uri")
                        mainBuffer.put("byteLength", paddedLen)
                        currentOffset += paddedLen
                    }
                }
            }

            val images = json.optJSONArray("images")
            if (images != null) {
                val bufferViews = json.optJSONArray("bufferViews") ?: JSONArray().also { json.put("bufferViews", it) }

                for (i in 0 until images.length()) {
                    val img = images.getJSONObject(i)
                    val uri = img.optString("uri")
                    if (uri.isNotEmpty() && !uri.startsWith("data:")) {
                        val imgFile = if (uri.startsWith("/")) {
                            File(uri)
                        } else {
                            val fileInTemp = File(tempDir, uri)
                            if (fileInTemp.exists()) {
                                fileInTemp
                            } else {
                                File(projectDir, uri)
                            }
                        }

                        if (imgFile.exists()) {
                            val bytes = imgFile.readBytes()
                            val paddedLen = padLength(bytes.size)
                            val paddedBytes = bytes.copyOf(paddedLen)

                            binPayloads.add(paddedBytes)

                            val bvIndex = bufferViews.length()
                            val bv = JSONObject()
                            bv.put("buffer", 0)
                            bv.put("byteOffset", currentOffset)
                            bv.put("byteLength", bytes.size)
                            bufferViews.put(bv)

                            img.remove("uri")
                            img.put("bufferView", bvIndex)
                            img.put("mimeType", if (uri.endsWith(".png", true)) "image/png" else "image/jpeg")

                            currentOffset += paddedLen
                        } else {
                            Gdx.app.error("GLB_Export", "Texture file NOT found during packaging: ${imgFile.absolutePath}")
                        }
                    }
                }
            }

            if (buffers != null && buffers.length() > 0) {
                buffers.getJSONObject(0).put("byteLength", currentOffset)
            }

            val finalJsonStr = cleanJsonObject(json).toString()
            val jsonBytes = finalJsonStr.toByteArray(Charsets.UTF_8)
            val jsonPaddedLen = padLength(jsonBytes.size)
            val jsonPaddedBytes = ByteArray(jsonPaddedLen) { 0x20.toByte() }
            System.arraycopy(jsonBytes, 0, jsonPaddedBytes, 0, jsonBytes.size)

            val totalBinSize = currentOffset

            FileOutputStream(glbFile).use { out ->
                val header = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
                header.putInt(0x46546C67) // "glTF"
                header.putInt(2) // Version 2
                val totalLength = 12 + 8 + jsonPaddedLen + if (totalBinSize > 0) (8 + totalBinSize) else 0
                header.putInt(totalLength)
                out.write(header.array())

                val jsonChunkHeader = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
                jsonChunkHeader.putInt(jsonPaddedLen)
                jsonChunkHeader.putInt(0x4E4F534A) // "JSON"
                out.write(jsonChunkHeader.array())
                out.write(jsonPaddedBytes)

                if (totalBinSize > 0) {
                    val binChunkHeader = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
                    binChunkHeader.putInt(totalBinSize)
                    binChunkHeader.putInt(0x004E4942) // "BIN\0"
                    out.write(binChunkHeader.array())

                    for (payload in binPayloads) {
                        out.write(payload)
                    }
                }
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun padLength(len: Int): Int {
        return if (len % 4 == 0) len else len + (4 - (len % 4))
    }

    private fun cleanJsonObject(obj: JSONObject): JSONObject {
        val clean = JSONObject()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (key.startsWith("@")) continue

            when (val value = obj.get(key)) {
                is JSONObject -> clean.put(key, cleanJsonObject(value))
                is JSONArray -> clean.put(key, cleanJsonArray(value))
                else -> clean.put(key, value)
            }
        }
        return clean
    }

    private fun cleanJsonArray(array: JSONArray): JSONArray {
        val clean = JSONArray()
        for (i in 0 until array.length()) {
            when (val value = array.get(i)) {
                is JSONObject -> clean.put(cleanJsonObject(value))
                is JSONArray -> clean.put(cleanJsonArray(value))
                else -> clean.put(value)
            }
        }
        return clean
    }
}
