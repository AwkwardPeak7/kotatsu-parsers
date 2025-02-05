package org.koitharu.kotatsu.parsers.site.heancms.en

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.ContentType
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.heancms.HeanCms

@MangaSourceParser("OMEGASCANS", "OmegaScans", "en", ContentType.HENTAI)
internal class OmegaScans(context: MangaLoaderContext) :
	HeanCms(context, MangaSource.OMEGASCANS, "omegascans.org")
