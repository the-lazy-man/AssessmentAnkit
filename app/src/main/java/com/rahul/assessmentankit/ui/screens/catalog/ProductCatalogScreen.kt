package com.rahul.assessmentankit.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rahul.assessmentankit.ui.components.CartBadgeIconButton
import com.rahul.assessmentankit.ui.components.CategoryChips
import com.rahul.assessmentankit.ui.components.ConnectionStateBanner
import com.rahul.assessmentankit.ui.components.EmptyView
import com.rahul.assessmentankit.ui.components.ErrorView
import com.rahul.assessmentankit.ui.components.LoadingView
import com.rahul.assessmentankit.ui.components.ProductCard
import com.rahul.assessmentankit.ui.components.SearchBarView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCatalogScreen(
    viewModel: ProductCatalogViewModel,
    onProductClick: (Int) -> Unit,
    onCartClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userNotification) {
        state.userNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "DummyJSON Market",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    actions = {
                        CartBadgeIconButton(
                            itemCount = state.cartItemCount,
                            onClick = onCartClick
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
                ConnectionStateBanner(isOffline = state.isOffline)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            SearchBarView(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onSearch = { viewModel.loadProducts() }
            )

            // Category Chips (Only show when categories loaded)
            if (state.categories.isNotEmpty()) {
                CategoryChips(
                    categories = state.categories,
                    selectedCategorySlug = state.selectedCategorySlug,
                    onCategorySelected = viewModel::onCategorySelected
                )
            }

            // Main Content Area based on CatalogUiState
            when (val uiState = state.uiState) {
                is CatalogUiState.Loading -> {
                    LoadingView(message = "Fetching catalog from DummyJSON...")
                }
                is CatalogUiState.Empty -> {
                    EmptyView(
                        title = "No Products Found",
                        description = if (state.searchQuery.isNotBlank())
                            "No results for '${state.searchQuery}'. Try another query."
                        else
                            "No products available in this category.",
                        actionLabel = "Clear Filters",
                        onAction = {
                            viewModel.onCategorySelected(null)
                            viewModel.onSearchQueryChanged("")
                        }
                    )
                }
                is CatalogUiState.Error -> {
                    ErrorView(
                        message = uiState.message,
                        isNetworkError = uiState.isNetworkError,
                        onRetry = { viewModel.loadProducts() }
                    )
                }
                is CatalogUiState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = onProductClick,
                                onAddToCartClick = { viewModel.addToCart(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
