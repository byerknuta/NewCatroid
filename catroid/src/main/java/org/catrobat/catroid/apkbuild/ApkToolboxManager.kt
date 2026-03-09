package org.catrobat.catroid.apkbuild

import android.content.Context
import android.util.Log
import com.android.apksig.ApkSigner
import com.reandroid.apk.ApkModule
import com.reandroid.archive.FileInputSource
import com.reandroid.arsc.chunk.xml.ResXmlElement
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.x509.X509V3CertificateGenerator
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Security
import java.security.cert.X509Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal

object ApkToolboxManager {

    private const val TAG = "ApkToolbox"

    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }




    data class ManifestConfig(
        val appName: String? = null,
        val packageName: String? = null,
        val versionCode: Int? = null,
        val versionName: String? = null,
        val minSdkVersion: Int? = null,
        val targetSdkVersion: Int? = null,
        val permissionsToAdd: List<String>? = null,
        val permissionsToRemove: List<String>? = null,
        val debuggable: Boolean? = null
    )


    fun updateManifest(apkPath: String, config: ManifestConfig): Boolean {
        return modifyApk(apkPath) { module ->
            val manifest = module.androidManifestBlock
            val oldPackage = manifest.packageName
            val newPackage = config.packageName ?: oldPackage


            if (config.packageName != null) {
                manifest.packageName = config.packageName

                fixManifestRecursive(manifest.manifestElement, oldPackage, newPackage)
            }
            if (config.versionCode != null) manifest.versionCode = config.versionCode
            if (config.versionName != null) manifest.versionName = config.versionName


            if (config.minSdkVersion != null) manifest.minSdkVersion = config.minSdkVersion
            if (config.targetSdkVersion != null) manifest.targetSdkVersion = config.targetSdkVersion


            if (config.appName != null) {
                val appElem = manifest.applicationElement

                val labelAttr = appElem.getOrCreateAndroidAttribute("label", 0x01010001)


                labelAttr.valueAsString = config.appName
            }


            if (config.debuggable != null) {
                val appElem = manifest.applicationElement

                val debugAttr = appElem.getOrCreateAndroidAttribute("debuggable", 0x0101000f)
                debugAttr.setValueAsBoolean(config.debuggable)
            }


            config.permissionsToAdd?.forEach { perm ->
                manifest.addUsesPermission(perm)
            }


            config.permissionsToRemove?.forEach { permToRemove ->
                val root = manifest.manifestElement
                val permissions = root.listElements("uses-permission")
                val toDelete = mutableListOf<ResXmlElement>()

                for (permElem in permissions) {
                    val nameAttr = permElem.searchAttributeByResourceId(0x01010003)
                    if (nameAttr?.valueAsString == permToRemove) {
                        toDelete.add(permElem)
                    }
                }
                toDelete.forEach { root.remove(it) }
            }
        }
    }

    fun addFileToApk(apkPath: String, sourceFile: File, pathInsideApk: String): Boolean {
        if (!sourceFile.exists() || sourceFile.isDirectory) return false
        return modifyApk(apkPath) { module ->

            module.zipEntryMap.remove(pathInsideApk)

            module.add(FileInputSource(sourceFile, pathInsideApk))
        }
    }

    fun addFolderToApk(apkPath: String, sourceFolder: File, destPathInApk: String): Boolean {
        if (!sourceFolder.exists() || !sourceFolder.isDirectory) return false

        return modifyApk(apkPath) { module ->

            sourceFolder.walk().forEach { file ->
                if (file.isFile) {

                    val relativePath = file.toRelativeString(sourceFolder)


                    val finalPath = if (destPathInApk.isEmpty()) {
                        relativePath
                    } else {

                        val cleanDest = destPathInApk.trimEnd('/')
                        "$cleanDest/$relativePath"
                    }


                    module.zipEntryMap.remove(finalPath)

                    module.add(FileInputSource(file, finalPath))
                }
            }
        }
    }

    fun deleteFromApk(apkPath: String, pathPattern: String): Boolean {
        return modifyApk(apkPath) { module ->
            val cleanPattern = pathPattern.replace("\\", "/")


            val toRemove = ArrayList<String>()


            for (entry in module.zipEntryMap.listInputSources()) {
                val entryName = entry.name



                val isDirectoryMatch = entryName.startsWith("$cleanPattern/")
                val isFileMatch = entryName == cleanPattern

                if (isFileMatch || isDirectoryMatch) {
                    toRemove.add(entryName)
                }
            }


            for (name in toRemove) {
                module.zipEntryMap.remove(name)
            }
        }
    }
    fun extractFileFromApk(apkPath: String, pathInsideApk: String, outputLocalPath: String): Boolean {
        var module: ApkModule? = null
        try {
            module = ApkModule.loadApkFile(File(apkPath))
            val entry = module.zipEntryMap.getInputSource(pathInsideApk)

            return if (entry != null) {
                File(outputLocalPath).parentFile?.mkdirs()

                entry.openStream().use { input ->
                    FileOutputStream(outputLocalPath).use { output ->
                        input.copyTo(output)
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            try { module?.close() } catch (e: Exception) {}
        }
    }





    fun generateKeyStore(outputPath: String, alias: String, pass: String, commonName: String): Boolean {
        return try {

            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            val keyPair = keyPairGenerator.generateKeyPair()

            val notBefore = Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24)
            val notAfter = Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 25)
            val serialNumber = BigInteger.valueOf(System.currentTimeMillis())
            val owner = X500Principal("CN=$commonName, OU=CatroidBuilder, O=NewCatroid, C=WW")


            val certBuilder = JcaX509v3CertificateBuilder(
                owner,
                serialNumber,
                notBefore,
                notAfter,
                owner,
                keyPair.public
            )



            val contentSigner = JcaContentSignerBuilder("SHA256WithRSA")
                .build(keyPair.private)


            val certHolder = certBuilder.build(contentSigner)




            val cert = JcaX509CertificateConverter()
                .getCertificate(certHolder)


            val ks = KeyStore.getInstance("PKCS12")
            ks.load(null, null)
            ks.setKeyEntry(alias, keyPair.private, pass.toCharArray(), arrayOf(cert))

            File(outputPath).parentFile?.mkdirs()
            FileOutputStream(outputPath).use { ks.store(it, pass.toCharArray()) }

            Log.d(TAG, "Successfully generated KeyStore: $outputPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Key generation failed", e)
            false
        }
    }

    fun signApk(
        context: Context,
        inputApkPath: String,
        outputApkPath: String,
        keyStorePath: String?,
        keyAlias: String?,
        keyPass: String?
    ): Boolean {
        return try {
            val input = File(inputApkPath)
            val output = File(outputApkPath)

            val signerBuilder: ApkSigner.Builder

            if (keyStorePath != null && File(keyStorePath).exists()) {
                val ks = KeyStore.getInstance("PKCS12")
                FileInputStream(keyStorePath).use { ks.load(it, keyPass?.toCharArray()) }

                val alias = keyAlias ?: ks.aliases().nextElement()
                val privateKey = ks.getKey(alias, keyPass?.toCharArray()) as PrivateKey
                val cert = ks.getCertificate(alias) as X509Certificate

                val config = com.android.apksig.ApkSigner.SignerConfig.Builder("CERT", privateKey, listOf(cert)).build()
                signerBuilder = ApkSigner.Builder(listOf(config))
            } else {
                val tempKey = File(context.cacheDir, "debug_auto.jks")
                if (!tempKey.exists()) {
                    generateKeyStore(tempKey.absolutePath, "debug", "android", "Debug User")
                }
                return signApk(context, inputApkPath, outputApkPath, tempKey.absolutePath, "debug", "android")
            }

            signerBuilder
                .setInputApk(input)
                .setOutputApk(output)
                .setV1SigningEnabled(true)
                .setV2SigningEnabled(true)
                .build()
                .sign()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    private fun fixManifestRecursive(element: ResXmlElement, oldPkg: String, newPkg: String) {
        element.attributes.forEach { attr ->
            val value = attr.valueAsString
            if (value != null && value.contains(oldPkg)) {
                attr.valueAsString = value.replace(oldPkg, newPkg)
            }
        }
        element.listElements().forEach { child ->
            fixManifestRecursive(child, oldPkg, newPkg)
        }
    }


    private fun modifyApk(apkPath: String, action: (ApkModule) -> Unit): Boolean {
        var module: ApkModule? = null
        val tempFile = File("$apkPath.tmp")
        val originalFile = File(apkPath)

        return try {
            if (!originalFile.exists()) return false

            module = ApkModule.loadApkFile(originalFile)
            module.setLoadDefaultFramework(false)

            if (module.hasTableBlock()) {
                module.tableBlock.stringPool.setFlagSorted(false)
                module.tableBlock.stringPool.styleArray.clear()
            }


            action(module)

            if (module.hasTableBlock()) {
                module.tableBlock.refresh()
            }
            module.androidManifestBlock.refresh()

            module.writeApk(tempFile)
            module.close()

            if (originalFile.delete()) {
                tempFile.renameTo(originalFile)
            } else {
                tempFile.copyTo(originalFile, overwrite = true)
                tempFile.delete()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            try { module?.close() } catch (ignored: Exception) {}
            tempFile.delete()
            false
        }
    }
}