package geowarin.bootwebpack.template

import geowarin.bootwebpack.extensions.resource.readText
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.core.io.Resource
import org.springframework.web.util.HtmlUtils

val defaultHtml: String = """
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
  </head>
  <body>
  </body>
</html>
"""

/**
 * Allows html manipulations via Jsoup
 */
class HtmlTemplate(html: String = defaultHtml) {
    internal var document: Document

    init {
        document = parse(html)
    }

    companion object Factory {
        fun fromResource(resource: Resource): HtmlTemplate {
            return HtmlTemplate(resource.readText())
        }
    }

    fun setTitle(title: String): HtmlTemplate {
        document.title(title)
        return this
    }

    fun insertCssTag(cssUrl: String): HtmlTemplate {
        document.head().appendElement("link")
                .attr("rel", "stylesheet")
                .attr("type", "text/css")
                .attr("href", cssUrl)
        return this
    }

    fun insertScriptTag(scriptUrl: String): HtmlTemplate {
        document.body().appendElement("script").attr("type", "text/javascript").attr("src", scriptUrl)
        return this
    }

    fun insertScript(scriptText: String): HtmlTemplate {
        document.body().appendElement("script").attr("type", "text/javascript").text(scriptText)
        return this
    }

    fun replaceNodeContent(selector: String, content: String): HtmlTemplate {
        document.select(selector).html(content)
        return this
    }

    fun template(vararg values: Pair<String, String?>): HtmlTemplate {
        var html = document.html()
        values.forEach {
            html = html.replace("{${it.first}}", HtmlUtils.htmlEscape(it.second ?: ""))
        }
        document = parse(html)

        return this
    }

    override fun toString(): String {
        return document.toString()
    }

    private fun parse(html: String): Document {
        val document = Jsoup.parse(html)
        val outputSettings = Document.OutputSettings()
        // React does not like text nodes around rendered html...
        outputSettings.prettyPrint(false)
        document.outputSettings(outputSettings)
        return document
    }
}
