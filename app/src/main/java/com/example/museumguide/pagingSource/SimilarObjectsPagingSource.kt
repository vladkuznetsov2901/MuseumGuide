package com.example.museumguide.pagingSource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.museumguide.api.MuseumsApi
import com.example.museumguide.data.ObjectInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class SimilarObjectsPagingSource(
    private val similarObjectsId: List<Int>,
) : PagingSource<Int, ObjectInfoModel>() {

    override fun getRefreshKey(state: PagingState<Int, ObjectInfoModel>): Int? = INITIAL_PAGE

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ObjectInfoModel> {
        val page = params.key ?: INITIAL_PAGE
        return try {
            if (page > similarObjectsId.size) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (page == INITIAL_PAGE) null else page - 1,
                    nextKey = null
                )
            }

            var objectIndex = page - 1
            var objWithImages: ObjectInfoModel? = null

            while (objectIndex < similarObjectsId.size && objWithImages == null) {
                val objectId = similarObjectsId[objectIndex]
                val objResponse = withContext(Dispatchers.IO) {
                    MuseumsApi.retrofit.getInfoAboutObject(objectId).execute()
                }

                if (objResponse.isSuccessful) {
                    val obj = objResponse.body()
                    if (obj?.primaryImage?.isNotEmpty() == true) {
                        objWithImages = obj
                    }
                } else {
                    Log.e(
                        "com.example.museumguide.pagingSource.SimilarObjectsPagingSource",
                        "Object response not successful: ${objResponse.code()} ${objResponse.message()}"
                    )
                }
                objectIndex++
            }

            objWithImages?.let { obj ->
                LoadResult.Page(
                    data = listOf(obj),
                    prevKey = if (page == INITIAL_PAGE) null else page - 1,
                    nextKey = if (objectIndex < similarObjectsId.size) page + 1 else null
                )
            } ?: LoadResult.Page(
                data = emptyList(),
                prevKey = if (page == INITIAL_PAGE) null else page - 1,
                nextKey = null
            )

        } catch (e: HttpException) {
            Log.e("com.example.museumguide.pagingSource.SimilarObjectsPagingSource", "HttpException", e)
            LoadResult.Error(e)
        } catch (e: IOException) {
            Log.e("com.example.museumguide.pagingSource.SimilarObjectsPagingSource", "IOException", e)
            LoadResult.Error(e)
        }
    }

    companion object {
        private const val INITIAL_PAGE = 1
    }
}
