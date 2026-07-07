package org.catrobat.catroid.content.actions

import com.badlogic.gdx.scenes.scene2d.Action
import com.android.tools.r8.D8
import com.android.tools.r8.D8Command
import com.android.tools.r8.OutputMode
import org.catrobat.catroid.CatroidApplication
import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.formulaeditor.Formula
import org.catrobat.catroid.formulaeditor.UserVariable
import org.catrobat.catroid.ide.AndroidECJ
import org.catrobat.catroid.ide.IdeSettings
import java.io.File
import kotlin.concurrent.thread

class CompileJavaToDexAction : Action() {
    var scope: Scope? = null
    var srcFolder: Formula? = null
    var destDexFile: Formula? = null

    private var started = false
    @Volatile private var finished = false

    override fun act(delta: Float): Boolean {
        if (!started) {
            started = true
            runAsyncCompile()
        }
        return finished
    }

    private fun runAsyncCompile() {
        thread(start = true) {
            val context = CatroidApplication.getAppContext()
            try {
                val projectDir = scope?.project?.filesDir ?: return@thread

                val srcPathStr = srcFolder?.interpretString(scope) ?: ""
                val destPathStr = destDexFile?.interpretString(scope) ?: "classes.dex"

                val destFile = File(projectDir, destPathStr)

                val cacheDir = context.codeCacheDir
                val tempSrcDir = File(cacheDir, "compile_temp_src_${System.currentTimeMillis()}").apply { mkdirs() }
                val tempClassesDir = File(cacheDir, "compile_temp_classes_${System.currentTimeMillis()}").apply { mkdirs() }
                val tempDexDir = File(cacheDir, "compile_temp_dex_${System.currentTimeMillis()}").apply { mkdirs() }

                val javaFilesToCopy = if (srcPathStr.trim().isNotEmpty()) {
                    srcPathStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.map { File(projectDir, it) }
                } else {
                    projectDir.listFiles()?.filter { it.isFile && it.extension == "java" } ?: emptyList()
                }

                if (javaFilesToCopy.isEmpty()) {
                    errorVariable?.value = "Request failed: No .java files found to compile"
                    tempSrcDir.deleteRecursively()
                    tempClassesDir.deleteRecursively()
                    tempDexDir.deleteRecursively()
                    finished = true
                    return@thread
                }

                for (file in javaFilesToCopy) {
                    if (file.exists() && file.extension == "java") {
                        file.copyTo(File(tempSrcDir, file.name), overwrite = true)
                    }
                }

                val targetApi = 34
                val androidJar = IdeSettings.getAndroidJar(context, targetApi)
                val lambdaStubs = File(context.filesDir, "core-lambda-stubs.jar")

                if (!lambdaStubs.exists()) {
                    context.assets.open("core-lambda-stubs.jar").use { input ->
                        lambdaStubs.outputStream().use { output -> input.copyTo(output) }
                    }
                }

                val libraries = mutableListOf(androidJar)
                val jarNames = listOf(
                    "app-classes.jar",
                    "gdx.jar",
                    "xstream.jar",
                    "koin-core.jar",
                    "fragment.jar",
                    "activity.jar",
                    "core.jar"
                )
                for (jarName in jarNames) {
                    val jarFile = File(context.filesDir, jarName)
                    if (jarFile.exists()) {
                        libraries.add(jarFile)
                    }
                }

                val projectLibsDir = File(projectDir, "libs")
                if (projectLibsDir.exists()) {
                    projectLibsDir.listFiles()?.filter { it.isFile && it.extension == "jar" }?.forEach {
                        libraries.add(it)
                    }
                }

                val compiledClasses = AndroidECJ.compileDirectory(
                    srcDir = tempSrcDir,
                    libraries = libraries,
                    bootJar = androidJar,
                    stubsJar = lambdaStubs
                )

                if (compiledClasses.isNotEmpty()) {
                    for ((relativePath, classBytes) in compiledClasses) {
                        val classFile = File(tempClassesDir, relativePath)
                        classFile.parentFile?.mkdirs()
                        classFile.writeBytes(classBytes)
                    }

                    val classFiles = tempClassesDir.walkTopDown().filter { it.isFile && it.extension == "class" }.toList()
                    val builder = D8Command.builder()
                        .setMode(com.android.tools.r8.CompilationMode.DEBUG)
                        .setMinApiLevel(21)
                        .setIntermediate(false)
                        .setOutput(tempDexDir.toPath(), OutputMode.DexIndexed)
                        .addLibraryFiles(androidJar.toPath())

                    classFiles.forEach { file -> builder.addProgramFiles(file.toPath()) }
                    D8.run(builder.build())

                    val generatedDex = File(tempDexDir, "classes.dex")
                    if (generatedDex.exists()) {
                        destFile.parentFile?.mkdirs()
                        generatedDex.copyTo(destFile, overwrite = true)
                    }
                }

                errorVariable?.value = ""

                tempSrcDir.deleteRecursively()
                tempClassesDir.deleteRecursively()
                tempDexDir.deleteRecursively()
            } catch (e: Throwable) {
                e.printStackTrace()
                errorVariable?.value = e.localizedMessage ?: "Unknown compilation error"
            } finally {
                finished = true
            }
        }
    }

    private var errorVariable: UserVariable? = null

    fun setErrorVariable(variable: UserVariable?) {
        this.errorVariable = variable
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
        srcFolder = null
        destDexFile = null
        errorVariable = null
    }
}
