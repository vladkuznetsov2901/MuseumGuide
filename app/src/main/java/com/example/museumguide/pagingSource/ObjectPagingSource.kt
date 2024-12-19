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

class ObjectPagingSource(
    private val museumId: Int,
) : PagingSource<Int, ObjectInfoModel>() {

    override fun getRefreshKey(state: PagingState<Int, ObjectInfoModel>): Int? = INITIAL_PAGE

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ObjectInfoModel> {
        val page = params.key ?: INITIAL_PAGE
        return try {
            val response = withContext(Dispatchers.IO) {
                MuseumsApi.retrofit.getObjectsFromMuseum(museumId).execute()
            }
            if (response.isSuccessful) {
                val objectIDs = response.body()?.objectIDs ?: emptyList()

                if (page > objectIDs.size) {
                    return LoadResult.Page(
                        data = emptyList(),
                        prevKey = if (page == INITIAL_PAGE) null else page - 1,
                        nextKey = null
                    )
                }

                var objectIndex = page - 1
                var objWithImages: ObjectInfoModel? = null

                while (objectIndex < objectIDs.size && objWithImages == null) {
                    val objectId = objectIDs[objectIndex]
                    val objResponse = withContext(Dispatchers.IO) {
                        MuseumsApi.retrofit.getInfoAboutObject(objectId).execute()
                    }

                    if (objResponse.isSuccessful) {
                        val obj = objResponse.body()
                        if (obj?.additionalImages?.isNotEmpty() == true && obj.objectName.isNotEmpty()) {
                            objWithImages = obj
                        }
                    } else {
                        Log.e(
                            "com.example.museumguide.pagingSource.ObjectPagingSource",
                            "Object response not successful: ${objResponse.code()} ${objResponse.message()}"
                        )
                    }
                    objectIndex++
                }

                objWithImages?.let { obj ->
                    LoadResult.Page(
                        data = listOf(obj),
                        prevKey = if (page == INITIAL_PAGE) null else page - 1,
                        nextKey = if (objectIndex < objectIDs.size) objectIndex + 1 else null
                    )
                } ?: LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (page == INITIAL_PAGE) null else page - 1,
                    nextKey = null
                )

            } else {
                Log.e(
                    "com.example.museumguide.pagingSource.ObjectPagingSource",
                    "Response not successful: ${response.code()} ${response.message()}"
                )
                LoadResult.Error(HttpException(response))
            }
        } catch (e: IOException) {
            Log.e("com.example.museumguide.pagingSource.ObjectPagingSource", "IOException", e)
            LoadResult.Error(e)
        } catch (e: HttpException) {
            Log.e("com.example.museumguide.pagingSource.ObjectPagingSource", "HttpException", e)
            LoadResult.Error(e)
        }
    }

    companion object {
        private const val INITIAL_PAGE = 1
    }
}
