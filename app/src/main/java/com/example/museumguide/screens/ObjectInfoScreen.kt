package com.example.museumguide.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.museumguide.R
import com.example.museumguide.data.ObjectInfoModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ObjectInfoScreen(
    museumId: Int,
    objectId: Int,
    context: Context,
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    var author by rememberSaveable { mutableStateOf(context.getString(R.string.not_information)) }
    var objectTitle by rememberSaveable { mutableStateOf(context.getString(R.string.not_information)) }
    var country by rememberSaveable { mutableStateOf(context.getString(R.string.not_information)) }
    var period by rememberSaveable { mutableStateOf(context.getString(R.string.not_information)) }
    var objectDate by rememberSaveable { mutableStateOf(context.getString(R.string.not_information)) }
    var dimensions by rememberSaveable { mutableStateOf(context.getString(R.string.not_information)) }
    var accessionNumber by rememberSaveable { mutableStateOf(context.getString(R.string.not_information)) }
    var classification by rememberSaveable { mutableStateOf(context.getString(R.string.not_information)) }
    var objectName by rememberSaveable { mutableStateOf(context.getString(R.string.not_information)) }
    var creditLine by rememberSaveable { mutableStateOf(context.getString(R.string.not_information)) }
    var objectImg by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getObjectInfo(objectId)
        viewModel.objectInfo.collect { objectInfoList ->
            val objectInfo = objectInfoList.firstOrNull()
            author = objectInfo?.artistDisplayName.takeIf { !it.isNullOrBlank() }
                ?: context.getString(R.string.not_information)
            objectTitle = objectInfo?.title.takeIf { !it.isNullOrBlank() }
                ?: context.getString(R.string.not_information)
            country = objectInfo?.country.takeIf { !it.isNullOrBlank() }
                ?: context.getString(R.string.not_information)
            period = objectInfo?.period.takeIf { !it.isNullOrBlank() }
                ?: context.getString(R.string.not_information)
            objectDate = objectInfo?.objectDate.takeIf { !it.isNullOrBlank() }
                ?: context.getString(R.string.not_information)
            dimensions = objectInfo?.dimensions.takeIf { !it.isNullOrBlank() } ?: context.getString(
                R.string.not_information
            )
            accessionNumber = objectInfo?.accessionNumber.takeIf { !it.isNullOrBlank() }
                ?: context.getString(R.string.not_information)
            classification = objectInfo?.classification.takeIf { !it.isNullOrBlank() }
                ?: context.getString(R.string.not_information)
            objectName = objectInfo?.objectName.takeIf { !it.isNullOrBlank() } ?: context.getString(
                R.string.not_information
            )
            creditLine = objectInfo?.creditLine.takeIf { !it.isNullOrBlank() } ?: context.getString(
                R.string.not_information
            )
            objectImg = objectInfo?.primaryImage ?: ""
        }

    }


    LaunchedEffect(museumId) {
        viewModel.getObjects(museumId)
    }

    val lazyPagingItems = viewModel.objectsLoadInfo.collectAsLazyPagingItems()

    SwipeableCardScreen(
        objectImg,
        author,
        objectTitle,
        country,
        period,
        dimensions,
        accessionNumber,
        classification,
        objectName,
        creditLine,
        objectDate,
        lazyPagingItems
    )


}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun SwipeableCardScreen(
    objectImg: String?,
    author: String,
    objectTitle: String,
    country: String,
    period: String,
    dimensions: String,
    accessionNumber: String,
    classification: String,
    objectName: String,
    creditLine: String,
    objectDate: String,
    similarObjects: LazyPagingItems<ObjectInfoModel>,
) {
    val swipeableState = rememberSwipeableState(initialValue = SwipeState.Expanded)
    val painter = rememberAsyncImagePainter(objectImg)
    val scope = rememberCoroutineScope()

    // Define the screen height and anchors
    val screenHeight = LocalDensity.current.run {
        LocalContext.current.resources.displayMetrics.heightPixels / density
    }
    val collapsedHeight = 300f // Height of the collapsed card
    val anchors =
        mapOf(0f to SwipeState.Expanded, screenHeight - collapsedHeight to SwipeState.Collapsed)

    var isFullscreen by rememberSaveable {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                orientation = Orientation.Vertical,
                enabled = true,
                reverseDirection = true
            )
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clickable { isFullscreen = !isFullscreen },
            contentScale = ContentScale.Crop
        )
        if (!isFullscreen) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((collapsedHeight + swipeableState.offset.value).dp)
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
                    .zIndex(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(206, 215, 230))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = objectTitle,
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.montserrat_semibold)),
                        color = Color(52, 65, 106),
                        lineHeight = 19.5.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        FirstTextColumn(author, objectTitle, country, period, objectDate)
                        SecondTextColumn(
                            accessionNumber,
                            classification,
                            objectName,
                            creditLine,
                            dimensions
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Похожие экспонаты",
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.montserrat_semibold)),
                        color = Color(57, 53, 102),
                        lineHeight = 19.5.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    SimilarObjects(similarObjects = similarObjects)
                }
            }
        }

    }
}

@Composable
fun FirstTextColumn(
    author: String,
    objectTitle: String,
    country: String,
    period: String,
    objectDate: String,
) {
    Column(
        modifier = Modifier
            .padding(start = 20.dp)
            .width(134.dp)
            .height(322.dp)
    ) {
        Text(
            text = "Автор", fontFamily = FontFamily(Font(R.font.montserrat_bold)), fontSize = 14.sp
        )
        Text(
            text = author, fontFamily = FontFamily(Font(R.font.montserrat_light)), fontSize = 14.sp
        )

        Text(
            text = "Название",
            fontFamily = FontFamily(Font(R.font.montserrat_bold)),
            fontSize = 14.sp
        )
        Text(
            text = objectTitle,
            fontFamily = FontFamily(Font(R.font.montserrat_light)),
            fontSize = 14.sp
        )

        Text(
            text = "Место создания",
            fontFamily = FontFamily(Font(R.font.montserrat_bold)),
            fontSize = 14.sp
        )
        Text(
            text = country, fontFamily = FontFamily(Font(R.font.montserrat_light)), fontSize = 14.sp
        )

        Text(
            text = "Период", fontFamily = FontFamily(Font(R.font.montserrat_bold)), fontSize = 14.sp
        )
        Text(
            text = period, fontFamily = FontFamily(Font(R.font.montserrat_light)), fontSize = 14.sp
        )

        Text(
            text = "Год", fontFamily = FontFamily(Font(R.font.montserrat_bold)), fontSize = 14.sp
        )
        Text(
            text = objectDate,
            fontFamily = FontFamily(Font(R.font.montserrat_light)),
            fontSize = 14.sp
        )

    }
}

@Composable
fun SecondTextColumn(
    accessionNumber: String,
    classification: String,
    objectName: String,
    creditLine: String,
    dimensions: String,
) {
    Column(
        modifier = Modifier
            .padding(start = 20.dp)
            .width(187.dp)
            .height(322.dp)
    ) {
        Text(
            text = "Инвентарный номер",
            fontFamily = FontFamily(Font(R.font.montserrat_bold)),
            fontSize = 14.sp
        )
        Text(
            text = accessionNumber,
            fontFamily = FontFamily(Font(R.font.montserrat_light)),
            fontSize = 14.sp
        )

        Text(
            text = "Категория",
            fontFamily = FontFamily(Font(R.font.montserrat_bold)),
            fontSize = 14.sp
        )
        Text(
            text = classification,
            fontFamily = FontFamily(Font(R.font.montserrat_light)),
            fontSize = 14.sp
        )

        Text(
            text = "Отдел/сектор",
            fontFamily = FontFamily(Font(R.font.montserrat_bold)),
            fontSize = 14.sp
        )
        Text(
            text = objectName,
            fontFamily = FontFamily(Font(R.font.montserrat_light)),
            fontSize = 14.sp
        )

        Text(
            text = "Коллекция",
            fontFamily = FontFamily(Font(R.font.montserrat_bold)),
            fontSize = 14.sp
        )
        Text(
            text = creditLine,
            fontFamily = FontFamily(Font(R.font.montserrat_light)),
            fontSize = 14.sp
        )

        Text(
            text = "Размер", fontFamily = FontFamily(Font(R.font.montserrat_bold)), fontSize = 14.sp
        )
        Text(
            text = dimensions,
            fontFamily = FontFamily(Font(R.font.montserrat_light)),
            fontSize = 14.sp
        )

    }
}

@Composable
fun SimilarObjects(similarObjects: LazyPagingItems<ObjectInfoModel>) {
    LazyRow(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)) {
        items(similarObjects.itemCount) { index ->
            val similarObject = similarObjects[index]
            if (similarObject != null) {
                SimilarObjectCard(
                    objectName = similarObject.objectName,
                    objectImg = similarObject.primaryImage
                )
            }
        }
    }
}

@Composable
fun SimilarObjectCard(objectName: String, objectImg: String?) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(200.dp)
            .background(Color.Transparent)
            .padding(10.dp),
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


enum class SwipeState {
    Collapsed, Expanded
}



