package com.example.museumguide.screens

import com.example.museumguide.pagingSource.ObjectPagingSource
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.museumguide.R
import com.example.museumguide.data.ObjectInfoModel
import kotlinx.coroutines.flow.MutableStateFlow


@Composable
fun ObjectsScreen(
    museumId: Int,
    museumTitle: String,
    navController: NavController,
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {

    LaunchedEffect(museumId) {
        viewModel.getObjects(museumId)
    }

    val lazyPagingItems = viewModel.objectsLoadInfo.collectAsLazyPagingItems()


    Column(
        modifier = Modifier
            .background(Color(207, 215, 231))
            .fillMaxSize()
    ) {
        SearchBarWithProfileObjects()
        MuseumTitleText(museumTitle)
        if (lazyPagingItems.loadState.refresh is androidx.paging.LoadState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            ObjectsList(objects = lazyPagingItems, museumId) { objectId, museumId ->
                navController.navigate("objectInfo/$objectId/$museumId")
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarWithProfileObjects() {
    var searchText by remember {
        mutableStateOf("")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        SearchBar(query = searchText, onQueryChange = { text ->
            searchText = text
        }, onSearch = {}, active = false, onActiveChange = {}, placeholder = {
            Text(
                text = stringResource(R.string.museum_search),
                fontFamily = FontFamily(Font(R.font.montserrat_light)),
                fontSize = 10.sp,
                maxLines = 1,
                modifier = Modifier.padding(0.dp)
            )
        }, leadingIcon = {
            Image(
                painter = painterResource(id = R.drawable.searchbar_icn),
                contentDescription = "searchbar_icn",
                modifier = Modifier.size(20.dp)
            )
        }, modifier = Modifier
            .width(251.dp)
            .height(47.dp)

        ) {

        }

        Image(
            painter = painterResource(id = R.drawable.account_img),
            contentDescription = "account_img",
            modifier = Modifier
                .size(72.dp)
                .padding(start = 16.dp, bottom = 18.dp)
        )

    }

}

@Composable
fun MuseumTitleText(museumTitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp)
    ) {
        Text(
            text = museumTitle,
            fontFamily = FontFamily(Font(R.font.montserrat_bold)),
            fontSize = 40.sp,
            lineHeight = 38.sp,
            color = Color(79, 90, 125)
        )
    }
}

@Composable
fun ObjectsList(objects: LazyPagingItems<ObjectInfoModel>, museumId: Int, onObjectClick: (Int, Int) -> Unit) {
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(23.dp),
        columns = GridCells.Fixed(2)
    ) {
        items(objects.itemCount) { index ->
            val objectFromMuseum = objects[index]
            Log.d("ObjectsList", "ObjectsList: $objectFromMuseum")
            if (objectFromMuseum != null) {
                ObjectCard(
                    objectName = objectFromMuseum.objectName ?: "Unknown Object",
                    objectImg = objectFromMuseum.additionalImages.firstOrNull() ?: "",
                    onClick = { onObjectClick(objectFromMuseum.objectID, museumId) }
                )
            }
        }
    }
}

@Composable
fun ObjectCard(objectName: String, objectImg: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .size(170.dp)
            .background(Color.Transparent)
            .padding(10.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val painter = rememberAsyncImagePainter(objectImg)
            Image(
                painter = painter,
                contentDescription = "object_test_img",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            when (painter.state) {
                is AsyncImagePainter.State.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is AsyncImagePainter.State.Error -> {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp),
                        color = Color.Red,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Red)
                                .align(Alignment.BottomCenter)
                                .width(144.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Failed to load image",
                                color = Color(23, 31, 56, 1),
                                style = TextStyle(fontSize = 12.sp)
                            )
                        }
                    }
                }

                else -> {}
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                color = Color(206, 215, 230).copy(alpha = 0.7f),
                shape = RoundedCornerShape(7.dp) // Закругленные углы
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(206, 215, 230).copy(alpha = 0.7f))
                        .align(Alignment.BottomCenter)
                        .width(144.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = objectName ?: "Unknown Object",
                        color = Color.Black, // Меняем цвет текста, чтобы он был виден на фоне
                        style = TextStyle(fontSize = 10.sp)
                    )
                }
            }
        }
    }
}

