package org.koitharu.kotatsu.parsers.site.mmrcms.fr

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.mmrcms.MmrcmsParser
import java.util.*

//the search doesn't work on the source.
@MangaSourceParser("JPSCANVF", "JpScan-vf", "fr")
internal class JpScanVf(context: MangaLoaderContext) :
	MmrcmsParser(context, MangaSource.JPSCANVF, "jpscan-vf.net") {
	override val sourceLocale: Locale = Locale.ENGLISH
}
