package com.example.museumguide.screens

import com.example.museumguide.pagingSource.ObjectPagingSource
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.museumguide.api.MuseumsApi
import com.example.museumguide.data.Department
import com.example.museumguide.data.ObjectInfoModel
import com.example.museumguide.data.SearchResponse
import com.example.museumguide.pagingSource.SimilarObjectsPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    var currentObjectId = MutableStateFlow<Int?>(null)
    private val _museums = MutableStateFlow<List<Department>>(emptyList())
    val museums = _museums.asStateFlow()

    private val _objects = MutableStateFlow<List<PagingData<ObjectInfoModel>>>(emptyList())
    val objects = _objects.asStateFlow()

    private val _objectsLoadInfo = MutableStateFlow<PagingData<ObjectInfoModel>>(PagingData.empty())
    val objectsLoadInfo = _objectsLoadInfo.asStateFlow()

    private val _objectInfo = MutableStateFlow<List<ObjectInfoModel?>>(emptyList())
    val objectInfo = _objectInfo.asStateFlow()

    private val _similarObjects = MutableStateFlow<PagingData<ObjectInfoModel>>(PagingData.empty())
    val similarObjects = _similarObjects.asStateFlow()

    suspend fun getMuseums() {
        withContext(Dispatchers.IO) {
            try {
                val response = MuseumsApi.retrofit.getMuseums().execute()
                if (response.isSuccessful) {
                    _museums.value = response.body()!!.departments
                } else {
                    Log.e("getMuseums", "Request failed with code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("getMuseums", "Request failed with exception: ${e.message}")
            }
        }
    }

    fun getObjects(museumId: Int) {
        viewModelScope.launch {
            val pager = Pager(PagingConfig(pageSize = 20)) {
                ObjectPagingSource(museumId)
            }.flow.cachedIn(viewModelScope)

            pager.collectLatest {
                _objectsLoadInfo.value = it
            }
        }
    }

    suspend fun getObjectInfo(objectID: Int) {
        withContext(Dispatchers.IO) {
            try {
                val response = MuseumsApi.retrofit.getInfoAboutObject(objectID).execute()
                if (response.isSuccessful) {
                    _objectInfo.value = listOf(response.body())
                } else {
                    Log.e("getObjectInfo", "Request failed with code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("getObjectInfo", "Request failed with exception: ${e.message}")
            }
        }
    }

    suspend fun getSimilarObjects(objectInfoModel: ObjectInfoModel) {
        val similarObjectsList = findSimilarObjects(objectInfoModel)
        viewModelScope.launch {
            val pager = Pager(PagingConfig(pageSize = 20)) {
                SimilarObjectsPagingSource(similarObjectsList)
            }.flow.cachedIn(viewModelScope)

            pager.collectLatest {
                _similarObjects.value = it
                Log.d("getSimilarObjects", "Similar Objects: $it")
            }
        }
    }

    suspend fun findSimilarObjects(objectInfo: ObjectInfoModel): List<Int> {
        val responses = mutableListOf<SearchResponse>()

        try {
            // Search by artist
            val artistResponse = withContext(Dispatchers.IO) {
                MuseumsApi.retrofit.getSimilarObjects(
                    query = objectInfo.artistDisplayName,
                    artistOrCulture = true
                ).execute()
            }
            if (artistResponse.isSuccessful) {
                artistResponse.body()?.let { responses.add(it) }
            } else {
                Log.e(
                    "findSimilarObjects",
                    "Artist search failed with code: ${artistResponse.code()}"
                )
            }

            // Search by culture
            val cultureResponse = withContext(Dispatchers.IO) {
                MuseumsApi.retrofit.getSimilarObjects(
                    query = objectInfo.culture,
                    artistOrCulture = true
                ).execute()
            }
            if (cultureResponse.isSuccessful) {
                cultureResponse.body()?.let { responses.add(it) }
            } else {
                Log.e(
                    "findSimilarObjects",
                    "Culture search failed with code: ${cultureResponse.code()}"
                )
            }

            // Search by medium
            if (objectInfo.classification.isNotEmpty()) {
                val mediumResponse = withContext(Dispatchers.IO) {
                    MuseumsApi.retrofit.getSimilarObjects(
                        query = objectInfo.classification,
                        medium = objectInfo.classification
                    ).execute()
                }
                if (mediumResponse.isSuccessful) {
                    mediumResponse.body()?.let { responses.add(it) }
                } else {
                    Log.e(
                        "findSimilarObjects",
                        "Medium search failed with code: ${mediumResponse.code()}"
                    )
                }
            }

        } catch (e: Exception) {
            Log.e("findSimilarObjects", "Request failed with exception: ${e}")
        }

        // Combine and deduplicate the object IDs
        val objectIDs = responses.flatMap { it.objectIDs ?: emptyList() }.distinct()
        Log.e("findSimilarObjects", "findSimilarObjects: ${objectIDs.size}")
        return objectIDs
    }

}
