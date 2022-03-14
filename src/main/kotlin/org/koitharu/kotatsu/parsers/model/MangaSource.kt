package org.koitharu.kotatsu.parsers.model

@Suppress("SpellCheckingInspection")
enum class MangaSource(
	val title: String,
	val locale: String?,
) {
	LOCAL("Local", null),
	READMANGA_RU("ReadManga", "ru"),
	MINTMANGA("MintManga", "ru"),
	SELFMANGA("SelfManga", "ru"),
	MANGACHAN("Манга-тян", "ru"),
	DESUME("Desu.me", "ru"),
	HENCHAN("Хентай-тян", "ru"),
	YAOICHAN("Яой-тян", "ru"),
	MANGATOWN("MangaTown", "en"),
	MANGALIB("MangaLib", "ru"),
	NUDEMOON("Nude-Moon", "ru"),
	MANGAREAD("MangaRead", "en"),
	REMANGA("Remanga", "ru"),
	HENTAILIB("HentaiLib", "ru"),
	ANIBEL("Anibel", "be"),
	NINEMANGA_EN("NineManga English", "en"),
	NINEMANGA_ES("NineManga Español", "es"),
	NINEMANGA_RU("NineManga Русский", "ru"),
	NINEMANGA_DE("NineManga Deutsch", "de"),
	NINEMANGA_IT("NineManga Italiano", "it"),
	NINEMANGA_BR("NineManga Brasil", "pt"),
	NINEMANGA_FR("NineManga Français", "fr"),
	EXHENTAI("ExHentai", null),
	MANGAOWL("MangaOwl", "en"),
	MANGADEX("MangaDex", null),
	BATOTO("Bato.To", null),
	COMICK_FUN("ComicK", null),
	;
}