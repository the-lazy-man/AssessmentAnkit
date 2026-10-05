package com.ankit.assessmentankit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ankit.assessmentankit.AssessmentApplication
import com.ankit.assessmentankit.ui.screens.cart.CartScreen
import com.ankit.assessmentankit.ui.screens.cart.CartViewModel
import com.ankit.assessmentankit.ui.screens.catalog.ProductCatalogScreen
import com.ankit.assessmentankit.ui.screens.catalog.ProductCatalogViewModel
import com.ankit.assessmentankit.ui.screens.detail.ProductDetailScreen
import com.ankit.assessmentankit.ui.screens.detail.ProductDetailViewModel

object Screen {
    const val Catalog = "catalog"
    const val Detail = "detail/{productId}"
    const val Cart = "cart"

    fun detailRoute(productId: Int) = "detail/$productId"
}

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as AssessmentApplication).appContainer

    NavHost(
        navController = navController,
        startDestination = Screen.Catalog
    ) {
        composable(Screen.Catalog) {
            val catalogViewModel: ProductCatalogViewModel = viewModel(
                factory = ProductCatalogViewModel.Factory(
                    productRepository = appContainer.productRepository,
                    cartRepository = appContainer.cartRepository,
                    networkMonitor = appContainer.networkMonitor
                )
            )
            ProductCatalogScreen(
                viewModel = catalogViewModel,
                onProductClick = { productId ->
                    navController.navigate(Screen.detailRoute(productId))
                },
                onCartClick = {
                    navController.navigate(Screen.Cart)
                }
            )
        }

        composable(
            route = Screen.Detail,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            val detailViewModel: ProductDetailViewModel = viewModel(
                factory = ProductDetailViewModel.Factory(
                    productId = productId,
                    productRepository = appContainer.productRepository,
                    cartRepository = appContainer.cartRepository,
                    networkMonitor = appContainer.networkMonitor
                )
            )
            ProductDetailScreen(
                viewModel = detailViewModel,
                onBackClick = { navController.popBackStack() },
                onCartClick = { navController.navigate(Screen.Cart) }
            )
        }

        composable(Screen.Cart) {
            val cartViewModel: CartViewModel = viewModel(
                factory = CartViewModel.Factory(
                    cartRepository = appContainer.cartRepository,
                    networkMonitor = appContainer.networkMonitor
                )
            )
            CartScreen(
                viewModel = cartViewModel,
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Screen.detailRoute(productId))
                },
                onBrowseCatalogClick = {
                    navController.navigate(Screen.Catalog) {
                        popUpTo(Screen.Catalog) { inclusive = true }
                    }
                }
            )
        }
    }
}
