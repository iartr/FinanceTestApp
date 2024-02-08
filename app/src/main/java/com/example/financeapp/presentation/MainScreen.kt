package com.example.financeapp.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financeapp.R
import com.example.financeapp.ui.theme.BubbleGreen
import com.example.financeapp.ui.theme.BubbleRed
import com.example.financeapp.ui.theme.FinanceAppTheme
import com.example.financeapp.ui.theme.OnBubbleColor
import com.example.financeapp.ui.theme.TradingGreen
import com.example.financeapp.ui.theme.TradingRed

@Composable
fun MainScreen() {
    val viewModel: MainViewModel = viewModel<MainViewModel>(factory = MainViewModel.factory)
    val uiState: MainViewModel.UiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is MainViewModel.UiState.ListData -> {
            StockList(
                list = state.list,
                onItemClick = { viewModel.onTickerClick() })
        }
        MainViewModel.UiState.Error -> {
            ErrorState(Modifier.fillMaxSize()) { viewModel.onRetryClick() }
        }
        MainViewModel.UiState.Loading -> {
            InfiniteProgressFullScreen(Modifier.fillMaxSize())
        }
    }
}

@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit
) {
    Column(modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.error_ocurred),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = { onRetryClick() },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = stringResource(id = R.string.retry))
        }
    }
}

@Composable
fun InfiniteProgressFullScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.Center)
                .width(64.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Suppress("NonSkippableComposable")
@Composable
fun StockList(
    list: List<MainViewModel.UiState.Data>,
    onItemClick: (item: MainViewModel.UiState.Data) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        itemsIndexed(list, key = { _, item ->  item.title }) { index, item ->
            StockListItem(
                itemUi = item,
                modifier = Modifier
                    .clickable { onItemClick(item) }
                    .animateItemPlacement(),
            )
            Spacer(Modifier.height(8.dp))
            if (index < list.lastIndex) {
                Divider(modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
    }
}

@Composable
fun StockListItem(
    itemUi: MainViewModel.UiState.Data,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = itemUi.title,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            if (itemUi.showSubtitle) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = itemUi.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    text = itemUi.percentChangeFromLastClose,
                    color = if (itemUi.showBubble) OnBubbleColor else itemUi.percentChangeTypoColor,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .then(if (itemUi.showBubble) Modifier
                            .padding(2.dp)
                            .background(itemUi.strokeColor, RoundedCornerShape(4.dp))
                            .padding(2.dp) else Modifier
                        ),
                )

                Text(
                    text = itemUi.lastTradePriceWithChangePoints,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Image(
                imageVector = Icons.Default.KeyboardArrowRight,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(Color(android.graphics.Color.GRAY)),
                contentDescription = null
            )
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 100, heightDp = 100)
fun InfiniteProgressPreview() {
    FinanceAppTheme {
        InfiniteProgressFullScreen()
    }
}

@Composable
@Preview(showBackground = true)
fun ErrorPreview() {
    FinanceAppTheme {
        ErrorState() {  }
    }
}

@Composable
@Preview(showBackground = true)
fun StockListItemPreview(
    @PreviewParameter(ItemUiPreviewParameter::class) data: MainViewModel.UiState.Data
) {
    FinanceAppTheme {
        StockListItem(itemUi = data)
    }
}

class ItemUiPreviewParameter : PreviewParameterProvider<MainViewModel.UiState.Data> {
    override val values: Sequence<MainViewModel.UiState.Data> = sequenceOf(
        baseUiItem.copy(showSubtitle = false, percentChangeFromLastClose = "+2%", showBubble = true, strokeColor = BubbleGreen),
        baseUiItem.copy(percentChangeFromLastClose = "-2%", showBubble = true, strokeColor = BubbleRed),
        baseUiItem,
        baseUiItem.copy(showSubtitle = false),
        baseUiItem.copy(percentChangeFromLastClose = "-2%", percentChangeTypoColor = TradingRed),
    )

    companion object {
        val baseUiItem = MainViewModel.UiState.Data(
            icon = null,
            title = "AnyTitle",
            subtitle = "MCX | SBER",
            showSubtitle = true,
            percentChangeFromLastClose = "+2.5%",
            lastTradePriceWithChangePoints = "0.123 (1.123)",
            percentChangeTypoColor = TradingGreen,
            showBubble = false,
            strokeColor = androidx.compose.ui.graphics.Color.Black,
        )
    }
}