package org.koitharu.kotatsu.parsers.site.madara.tr

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.ContentType
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("JIANGZAITOON", "JiangzaiToon", "tr", ContentType.HENTAI)
internal class Jiangzaitoon(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.JIANGZAITOON, "jiangzaitoon.cc") {
	override val postReq = true
	override val datePattern = "d MMMM yyyy"
}
