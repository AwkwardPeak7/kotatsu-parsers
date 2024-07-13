package org.koitharu.kotatsu.parsers.site.scan.it

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.scan.ScanParser

@MangaSourceParser("MANGAITALIA", "MangaItalia", "pt")
internal class MangaItalia(context: MangaLoaderContext) :
	ScanParser(context, MangaSource.MANGAITALIA, "manga-italia.com")
