package org.koitharu.kotatsu.parsers.site.en

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaParser
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.config.ConfigKey
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.model.MangaListFilter
import org.koitharu.kotatsu.parsers.model.MangaPage
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.model.MangaTag
import org.koitharu.kotatsu.parsers.model.RATING_UNKNOWN
import org.koitharu.kotatsu.parsers.model.SortOrder
import org.koitharu.kotatsu.parsers.util.SuspendLazy
import org.koitharu.kotatsu.parsers.util.byte2HexFormatted
import org.koitharu.kotatsu.parsers.util.domain
import org.koitharu.kotatsu.parsers.util.generateUid
import org.koitharu.kotatsu.parsers.util.json.getBooleanOrDefault
import org.koitharu.kotatsu.parsers.util.json.getIntOrDefault
import org.koitharu.kotatsu.parsers.util.json.getLongOrDefault
import org.koitharu.kotatsu.parsers.util.json.getStringOrNull
import org.koitharu.kotatsu.parsers.util.json.mapJSONNotNull
import org.koitharu.kotatsu.parsers.util.parseJson
import org.koitharu.kotatsu.parsers.util.toAbsoluteUrl
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min

@MangaSourceParser("VIZPARSER", "VIZ", "en")
class VizParser(context: MangaLoaderContext) : MangaParser(context, MangaSource.VIZPARSER) {

	override val configKeyDomain = ConfigKey.Domain("viz.com")

	override val availableSortOrders: Set<SortOrder> = setOf(
		SortOrder.ALPHABETICAL,
		SortOrder.UPDATED
	)

	private val apiUrl get() = "https://api.$domain"

	override val headers = Headers.Builder()
		.add("User-Agent", System.getProperty("http.agent")!!)
		.add(context.decodeBase64(secretHeader).toString(Charsets.UTF_8), context.decodeBase64(secretHeaderVal).toString(Charsets.UTF_8))
		.build()

	private fun getExtraHeaders(vizType: String): Headers {
		return when (vizType) {
			"1" -> Headers.headersOf(
				"Referer", "com.vizmanga.android"
			)
			"3" -> Headers.headersOf(
				"Referer", "com.viz.wsj.android"
			)
			else -> Headers.headersOf()
		}
	}

	private val titleCache = SuspendLazy {
		coroutineScope {
			// vizmanga & shounenjump
			listOf("1", "3").map { serviceId ->
				async {
					webClient.httpGet("$apiUrl/manga/store_cached/$serviceId/4/9", getExtraHeaders(serviceId))
						.parseJson()
						.getJSONArray("data").mapJSONNotNull {
							val series = it.optJSONObject("manga_series")
								?: return@mapJSONNotNull null

							if (series.getBooleanOrDefault("show_chapter", false) && (series.getIntOrDefault("num_chapters_free", 0) > 0)) {
								series.put("kotatsu_vizType", serviceId)
							} else {
								null
							}
						}
				}
			}
		}.awaitAll().flatten()
	}

	private fun getServiceType(serviceId: String): String {
		return when (serviceId) {
			"1" -> "vizmanga"
			"3" -> "shonenjump"
			else -> {
				assert(false) { "unknown service passed to getServiceType" }
				"vizmanga"
			}
		}
	}

	override suspend fun getList(offset: Int, filter: MangaListFilter?): List<Manga> {
		var freeMangaSeries = titleCache.get()

		when (filter) {
			is MangaListFilter.Advanced -> {
				when (filter.sortOrder) {
					SortOrder.UPDATED -> {
						freeMangaSeries = freeMangaSeries.sortedByDescending {
							it.getIntOrDefault("chapter_latest_pub_date", 0)
						}
					}
					SortOrder.ALPHABETICAL -> {
						freeMangaSeries = freeMangaSeries.sortedBy { it.getString("title") }
					}
					else -> {}
				}
			}
			is MangaListFilter.Search -> {
				freeMangaSeries = freeMangaSeries.filter {
					it.getString("title").contains(filter.query, true) ||
							it.getString("title_sort").contains(filter.query, true)
				}
			}
			else -> {}
		}

		return freeMangaSeries
			.subList(offset, min(offset+24, freeMangaSeries.size))
			.map { manga ->
				val id = manga.getInt("id").toString()
				val serviceId = manga.getString("kotatsu_vizType")
				Manga(
					id = generateUid(id),
					url = "$id#$serviceId",
					publicUrl = ("/${getServiceType(serviceId)}/chapters/${manga.getString("vanityurl")}").toAbsoluteUrl("www.$domain"),
					title = manga.getString("title"),
					author = manga.getString("latest_author")
						.split(",")
						.joinToString { it.substringAfter("by").trim() },
					description = buildString {
						manga.getStringOrNull("tagline")
							?.let { append(it, "<br><br>") }

						manga.getString("synopsis").let(::append)
					},
					state = null,
					coverUrl = manga.getString("link_img_url"),
					isNsfw = false,
					rating = RATING_UNKNOWN,
					altTitle = null,
					source = source,
					tags = emptySet(),
				)
			}
	}

	override suspend fun getDetails(manga: Manga): Manga {
		val id = manga.url.substringBefore("#")
		val serviceId = manga.url.substringAfter("#")
		val response = webClient.httpGet("$apiUrl/manga/store/series/$id/$serviceId/4/9", getExtraHeaders(serviceId)).parseJson()
			.getJSONArray("data")
			.mapJSONNotNull {
				it.optJSONObject("manga")
			}

		val volume = response.mapNotNull {
			if (it.getIntOrDefault("volume", -1) != -1 && !it.getStringOrNull("thumburl").isNullOrEmpty()) {
				it
			} else {
				null
			}
		}.sortedBy { it.getInt("volume") }

		val currentEpoch = System.currentTimeMillis() / 1000

		return manga.copy(
			coverUrl = volume.lastOrNull()?.getString("thumburl") ?: manga.coverUrl,
			chapters = response.mapNotNull {
				val epochExp = it.getLongOrDefault("epoch_exp_date", -1L)
				val epochPub = it.getLongOrDefault("epoch_pub_date", -1L)
				val isFree = (epochExp == -1L || epochExp > currentEpoch) && (epochPub == -1L || epochPub < currentEpoch)
				val chapterNum = it.getStringOrNull("chapter")

				if (chapterNum != null && isFree) {
					val chapterNumber = chapterNum.replace(".0", "")
					val url = "${it.getInt("id")}#${it.getInt("numpages")}#$serviceId"
					MangaChapter(
						id = generateUid(url),
						url = url,
						name = "Chapter $chapterNumber",
						number = chapterNumber.toFloat().toInt(),
						branch = "English",
						scanlator = null,
						source = source,
						uploadDate = getEpoch(it.getString("publication_date"))
					)
				} else {
					null
				}
			}
		)
	}

	private fun getEpoch(date: String, defaultValue: Long = 0L): Long = runCatching {
		dateFormat.parse(
			date.replace(iso8601TimezoneRegex) {
				it.groupValues.subList(1, 4).joinToString("")
			},
		)?.time
	}.getOrNull() ?: defaultValue

	private val instanceId by lazy {
		val byteArray = ByteArray(8)
		SecureRandom().nextBytes(byteArray)
		byteArray.byte2HexFormatted()
	}

	private fun commonFormPayload(serviceId: String): MutableMap<String, String> = mutableMapOf(
		Pair("instance_id", instanceId),
		Pair("device_token", instanceId),
		Pair("device_id", "4"),
		Pair("version", "9"),
		Pair("viz_app_id", serviceId),
		Pair("android_app_version_code", "123")
	)

	override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
		val info = chapter.url.split("#")
		val id = info[0]
		val pages = info[1].toInt()
		val serviceId = info[2]

		val payload = commonFormPayload(serviceId)
		payload["manga_id"] = id

		val response = webClient.httpPost("$apiUrl/manga/auth".toHttpUrl(), payload, getExtraHeaders(serviceId)).parseJson()

		if (response.getJSONObject("archive_info").getInt("ok") == 0) {
			throw Exception("Can't read Premium chapter")
		}

		return (0..pages).map {
			MangaPage(
				id = generateUid(it.toString()),
				url = "$id#$it#$serviceId",
				preview = null,
				source = source
			)
		}
	}

	override suspend fun getPageUrl(page: MangaPage): String {
		val info = page.url.split("#")
		val id = info[0]
		val pageNum = info[1]
		val serviceId = info[2]

		val payload = commonFormPayload(serviceId)
		payload["manga_id"] = id
		payload["page"] = pageNum

		return webClient.httpPost("$apiUrl/manga/get_manga_url".toHttpUrl(), payload, getExtraHeaders(serviceId))
			.parseJson()
			.getString("data")
	}

	override suspend fun getAvailableTags(): Set<MangaTag> {
		return emptySet()
	}

	companion object {
		// some private app api headers in base64 so not easily searchable in the repo
		private const val secretHeader = "eC1kZXZpbC1mcnVpdA=="
		private const val secretHeaderVal = "MTIzIGZsYW1lLWZsYW1lIGZydWl0cw=="

		private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
		private val iso8601TimezoneRegex = Regex("""([+\-])(\d{2}):(\d{2})""")
	}
}
