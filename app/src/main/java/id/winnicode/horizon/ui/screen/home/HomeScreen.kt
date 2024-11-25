package id.winnicode.horizon.ui.screen.home

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import id.winnicode.horizon.MainApplication
import id.winnicode.horizon.factory.ViewModelFactory
import id.winnicode.horizon.model.News
import id.winnicode.horizon.ui.common.UiState
import id.winnicode.horizon.ui.theme.GreyDark
import id.winnicode.horizon.ui.theme.WhiteSmoke

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory(MainApplication.injection)
    ),
    query: String,
    navigateToDetail: (Int) -> Unit,
) {
    val userSession = viewModel.userSession.collectAsState().value

    LaunchedEffect(userSession.token, query) {
        if (userSession.token.isNotEmpty() && query.isEmpty()) {
            viewModel.fetchNews(userSession.token)
        } else if (userSession.token.isNotEmpty() && query.isNotEmpty()) {
            viewModel.searchNews(userSession.token, query)
        }
    }

    viewModel.uiState.collectAsState(initial = UiState.Loading).value.let { uiState ->
        when (uiState) {
            is UiState.Loading -> {
                Box (modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center){
                    CircularProgressIndicator()
                }
            }

            is UiState.Success -> {
                Column {
                    Spacer(modifier = modifier.height(4.dp))
                    HomeContent(
                        news = uiState.data,
                        modifier = modifier,
                        navigateToDetail = navigateToDetail
                    )
                }

            }

            is UiState.Error -> {

            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeContent(
    news: List<News>,
    modifier: Modifier = Modifier,
    navigateToDetail: (Int) -> Unit,
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp),


        ) {
        itemsIndexed(news) { index, new ->
            if (index == 0) {
                Box(
                    modifier = modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(topStart = 27.dp, topEnd = 27.dp))
                        .background(WhiteSmoke)
                        .fillMaxSize()
                ) {
                    FirstArticleItem(
                        news = new,
                        context = context,
                        modifier = Modifier
                            .animateItemPlacement(tween(durationMillis = 100))
                            .clickable {
                                navigateToDetail(new.id)
                            })
                }
            } else {
                Box(
                    modifier = modifier
                        .background(WhiteSmoke)
                        .fillMaxSize(),
                ) {
                    RegularArticleItem(
                        news = new,
                        context = context,
                        modifier = Modifier
                            .animateItemPlacement(tween(durationMillis = 100))
                            .clickable {
                                navigateToDetail(new.id)
                            })
                }
            }
            if (index < news.size - 1) {
                Box(
                    modifier = modifier
                        .clip(RoundedCornerShape(topStart = 27.dp, topEnd = 27.dp))
                        .background(WhiteSmoke)
                        .fillMaxSize()
                ) {
                    Divider(
                        color = Color.LightGray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

        }
    }
}

@Composable
fun FirstArticleItem(
    news: News,
    context: Context,
    modifier: Modifier = Modifier
) {
    var isBookmarked by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }


    Column(
        modifier = modifier.padding(8.dp),
    ) {
        AsyncImage(
            model = news.imageUrl,
            contentDescription = null,
            modifier = modifier
                .clip(RoundedCornerShape(24.dp))
                .width(380.dp)
                .height(248.dp),
            contentScale = ContentScale.Crop
        )

        Column(Modifier.fillMaxWidth()) {
            Text(
                text = news.author,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Box(Modifier.fillMaxWidth()) {
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    maxLines = 3,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
                Row(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(top = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isBookmarked = !isBookmarked
                        },
                        modifier = modifier.padding(end = 6.dp)
                    ) {
                        if (isBookmarked) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Bookmark",
                                tint = Color.DarkGray
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = Color.DarkGray
                            )
                        }
                    }
                    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Bookmark",
                                tint = Color.DarkGray,
                            )
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Bagikan Berita") },
                                onClick = {
                                    val intent = Intent()
                                    intent.action = Intent.ACTION_SEND
                                    intent.putExtra(
                                        Intent.EXTRA_TEXT,
                                        "${news.title}\n${news.url}\nBaca berita terpecaya di Horizon"
                                    )
                                    intent.type = "text/plain"
                                    startActivity(
                                        context,
                                        Intent.createChooser(intent, "Share di:"),
                                        null
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Share,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun RegularArticleItem(
    news: News,
    context: Context,
    modifier: Modifier = Modifier
) {
    var isBookmarked by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(12.dp, 12.dp, 12.dp, 4.dp),

        ) {
        AsyncImage(
            model = news.imageUrl,
            contentDescription = null,
            modifier = modifier
                .width(116.dp)
                .height(78.dp)
                .clip(RoundedCornerShape(7.dp)),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(start = 8.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = news.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                textAlign = TextAlign.End,
                maxLines = 2,
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = news.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = GreyDark,
                    modifier = modifier.padding(end = 2.dp)
                )
                IconButton(
                    onClick = {
                        isBookmarked = !isBookmarked
                    },
                    modifier = modifier
                ) {
                    if (isBookmarked) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Bookmark",
                            tint = Color.DarkGray
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = Color.DarkGray
                        )
                    }
                }
                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Bookmark",
                            tint = Color.DarkGray,
                        )
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Bagikan Berita") },
                            onClick = {
                                val intent = Intent()
                                intent.action = Intent.ACTION_SEND
                                intent.putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${news.title}\n${news.url}\nBaca berita terpecaya di Horizon"
                                )
                                intent.type = "text/plain"
                                startActivity(
                                    context,
                                    Intent.createChooser(intent, "Share di:"),
                                    null
                                )
                            },
                            leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) }
                        )
                    }
                }
            }

        }
    }
}

