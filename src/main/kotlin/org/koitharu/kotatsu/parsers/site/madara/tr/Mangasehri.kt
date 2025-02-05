package org.koitharu.kotatsu.parsers.site.madara.tr

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGASEHRI", "MangaSehri", "tr")
internal class Mangasehri(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.MANGASEHRI, "manga-sehri.com", 18) {
	override val datePattern = "dd/MM/yyyy"
}
