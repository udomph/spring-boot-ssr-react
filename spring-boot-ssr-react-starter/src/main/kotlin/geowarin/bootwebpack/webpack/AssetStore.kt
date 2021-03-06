package geowarin.bootwebpack.webpack

import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8TypedArray
import com.eclipsesource.v8.V8Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets


@Component
open class AssetStore {
    var assets: MutableMap<String, Asset> = mutableMapOf<String, Asset>()

    fun store(assets: List<Asset>) {
//        this.assets.clear()
        assets.forEach { this.assets.put(it.name, it) }
    }

    fun getAsset(requestPath: String): Asset? {
        return assets[requestPath]
    }

    fun getCssNames(): List<String> {
        return assets.keys.filter { it.endsWith(".css") }
    }

    fun hasChunk(chunkName: String): Boolean {
        return getAssetByChunkName(chunkName) != null
    }

    fun getAssetAsResource(requestPath: String, modulePath: String?): Resource? {
        val asset = getAsset(requestPath)
        if (asset != null) {
            if (modulePath != null) {
                val prefix = modulePath + " = "
                val source = String(asset.source)
                return WebpackResource(asset.name, (prefix + source).toByteArray(), asset.lastModified)
            } else {
                return WebpackResource(asset.name, asset.source, asset.lastModified)
            }
        }
        return null
    }

    fun getAssetSourceByChunkName(chunkName: String): String? {
        val asset = getAssetByChunkName(chunkName)
        if (asset != null) {
            return String(asset.source)
        }
        return null
    }

    fun getAssetByChunkName(chunkName: String): Asset? {
        return assets.values.find { removeHash(it.name) == chunkName }
    }

    fun ensureAssetByChunkName(chunkName: String): Asset {
        return assets.values.find { removeHash(it.name) == chunkName }
                ?: throw IllegalStateException("Could not find chunk $chunkName")
    }

    private fun removeHash(name: String): String {
        return name.replace(Regex("__(?:\\w{8,})__"), "")
    }
}

class WebpackResource(val fileName: String, byteArray: ByteArray?, val lastModified:Long = 0) : ByteArrayResource(byteArray) {

    override fun getFilename(): String {
        return fileName
    }

    override fun lastModified(): Long {
        return lastModified
    }
}

class Asset(val name: String, val source: ByteArray, val lastModified: Long = 0) {
    constructor(obj: V8Object) : this(name = obj.getString("name"), source = getSourceAsByteArray(obj))
}

fun getSourceAsByteArray(obj: V8Object): ByteArray {
    val sourceObj = obj.get("source")
    try {
        when (sourceObj) {
            is V8TypedArray -> {
                return toByteArray(sourceObj.byteBuffer)
            }
            is String -> {
                return sourceObj.toByteArray(StandardCharsets.UTF_8)
            }
        }
    } finally {
        if (sourceObj is V8Value) {
            sourceObj.release()
        }
    }

    throw IllegalStateException("Unexpected source content")
}

fun toByteArray(buffer: ByteBuffer): ByteArray {
    val bytes: ByteArray = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return bytes
}