package com.example.museumguide.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.museumguide.R
import com.example.museumguide.data.Department
import com.example.museumguide.data.MuseumModel


@Composable
fun MuseumsScreen(navController: NavController, viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var museums by remember { mutableStateOf<List<Department>>(emptyList()) }

    LaunchedEffect(Unit) {
        if (viewModel.museums.value.isEmpty()) {
            viewModel.getMuseums()
            viewModel.museums.collect { museum ->
                museums = museum
            }
        } else museums = viewModel.museums.value

    }

    Column(
        modifier = Modifier
            .background(Color(207, 215, 231))
            .fillMaxSize()
    ) {
        SearchBarWithProfile()
        WelcomeToMuseumGuideText()
        MuseumsList(museums) { museumId, museumTitle ->
            navController.navigate("objectsList/$museumId/$museumTitle")
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarWithProfile() {
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
fun WelcomeToMuseumGuideText() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 23.dp)
    ) {
        Text(
            text = "Добро пожаловать в\n" + "Museum guide",
            fontFamily = FontFamily(Font(R.font.montserrat_bold)),
            fontSize = 30.sp,
            lineHeight = 35.sp,
            color = Color(79, 90, 125)
        )
        Text(
            text = "Выберите музей",
            fontFamily = FontFamily(Font(R.font.montserrat_semibold)),
            fontSize = 20.sp,
            color = Color(79, 90, 125),
            modifier = Modifier.padding(start = 23.dp, top = 24.dp)
        )
    }
}

@Composable
fun MuseumsList(museums: List<Department>, onMuseumClick: (Int, String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(museums) { department ->
            MuseumCard(
                museum = department,
                onClick = { onMuseumClick(department.departmentId, department.displayName) })
        }
    }
}

@Composable
fun MuseumCard(museum: Department, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clickable { onClick() }, contentAlignment = Alignment.Center

    ) {
        Card(
            modifier = Modifier
                .width(335.dp)
                .height(100.dp), shape = RoundedCornerShape(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.museum_img),
                    contentDescription = "museum_img",
                    modifier = Modifier
                        .size(90.dp)
                        .padding(top = 8.dp)
                )
                Column(modifier = Modifier.fillMaxHeight()) {
                    Text(
                        text = museum.displayName,
                        fontFamily = FontFamily(Font(R.font.montserrat_semibold)),
                        fontSize = 18.sp,
                        color = Color(79, 90, 125),
                        modifier = Modifier.padding(start = 10.dp, top = 5.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.arrow_img),
                            contentDescription = "arrow_img",
                            modifier = Modifier
                                .size(45.dp)
                                .padding(end = 15.dp)
                        )
                    }

                }


            }
        }
    }

}



