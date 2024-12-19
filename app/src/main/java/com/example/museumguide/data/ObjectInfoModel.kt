package com.example.museumguide.data

data class ObjectInfoModel(
    val GalleryNumber: String,
    val accessionYear: String,
    val accessionNumber: String,
    val additionalImages: List<String>,
    val artistAlphaSort: String,
    val artistBeginDate: String,
    val artistDisplayBio: String,
    val artistDisplayName: String,
    val artistULAN_URL: String,
    val artistWikidata_URL: String,
    val city: String,
    val classification: String,
    val country: String,
    val county: String,
    val creditLine: String,
    val culture: String,
    val department: String,
    val dimensions: String,
    val dynasty: String,
    val objectID: Int,
    val objectName: String,
    val objectURL: String,
    val objectDate: String,
    val objectWikidata_URL: String,
    val period: String,
    val primaryImage: String,
    val primaryImageSmall: String,
    val tags: List<Tag>,
    val title: String,
)


data class Tag(
    val AAT_URL: String,
    val Wikidata_URL: String,
    val term: String,
)
