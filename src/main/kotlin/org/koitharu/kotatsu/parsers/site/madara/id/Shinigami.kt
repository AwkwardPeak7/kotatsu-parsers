package org.koitharu.kotatsu.parsers.site.madara.id

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser
import java.util.Locale

@MangaSourceParser("SHINIGAMI", "Shinigami", "id")
internal class Shinigami(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.SHINIGAMI, "shinigami.moe", 10) {
	override val tagPrefix = "genre/"
	override val listUrl = "series/"
	override val sourceLocale: Locale = Locale.ENGLISH
}
