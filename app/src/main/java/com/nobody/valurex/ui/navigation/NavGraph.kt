package com.nobody.valurex.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nobody.valurex.ui.screen.AccountScreen
import com.nobody.valurex.ui.screen.AddManualScreen
import com.nobody.valurex.ui.screen.BillSplitScreen
import com.nobody.valurex.ui.screen.CategoriesScreen
import com.nobody.valurex.ui.screen.DivideHelperScreen
import com.nobody.valurex.ui.screen.HomeScreen
import com.nobody.valurex.ui.screen.LoansScreen
import com.nobody.valurex.ui.screen.RecurringExpensesScreen
import com.nobody.valurex.ui.screen.RemindersScreen
import com.nobody.valurex.ui.screen.SettingsScreen
import com.nobody.valurex.ui.screen.StatsScreen
import com.nobody.valurex.ui.screen.WalletCheckinScreen
import com.nobody.valurex.ui.screen.WishlistScreen
import com.nobody.valurex.ui.theme.ValurexColors

private data class NavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val TABS = listOf(
    NavTab("home",     "Home",     Icons.Filled.Home,          Icons.Outlined.Home),
    NavTab("loans",    "Loans",    Icons.Filled.Group,         Icons.Outlined.Group),
    NavTab("stats",    "Stats",    Icons.Filled.BarChart,      Icons.Outlined.BarChart),
    NavTab("wishlist", "Wishlist", Icons.Filled.Star,          Icons.Outlined.Star),
    NavTab("account",  "Account",  Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)
)

@Composable
fun ValurexNavGraph(initialRoute: String = "home") {
    val nav      = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route    = backStack?.destination?.route
    val vc = ValurexColors

    LaunchedEffect(initialRoute) {
        if (initialRoute != "home") nav.navigate(initialRoute) {
            popUpTo("home") { inclusive = false }
            launchSingleTop = true
        }
    }

    Scaffold(
        containerColor      = vc.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar           = {
            if (route in TABS.map { it.route }) {
                NavigationBar(containerColor = vc.Surface) {
                    TABS.forEach { tab ->
                        val selected = route == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                nav.navigate(tab.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = {
                                Icon(
                                    imageVector        = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = null
                                )
                            },
                            label  = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = vc.TextHigh,
                                selectedTextColor   = vc.TextHigh,
                                unselectedIconColor = vc.TextLow,
                                unselectedTextColor = vc.TextLow,
                                indicatorColor      = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = nav,
            startDestination = "home",
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToCategories = { nav.navigate("categories") },
                    onNavigateToAddManual  = { nav.navigate("add_manual") },
                    onNavigateToAccount    = {
                        nav.navigate("account") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
            composable("loans")    { 
                LoansScreen(
                    onNavigateToDivide = { nav.navigate("divide_helper") },
                    onNavigateToSplit  = { nav.navigate("bill_split") }
                ) 
            }
            composable("stats")    { StatsScreen() }
            composable("wishlist") { WishlistScreen() }
            composable("account") {
                AccountScreen(
                    onNavigateToSettings    = { nav.navigate("settings") },
                    onNavigateToRecurring   = { nav.navigate("recurring") },
                    onNavigateToCategories  = { nav.navigate("categories") },
                    onNavigateToReminders   = { nav.navigate("reminders") }
                )
            }
            composable("settings") {
                SettingsScreen(onNavigateBack = { nav.popBackStack() })
            }
            composable("reminders") {
                RemindersScreen(onNavigateBack = { nav.popBackStack() })
            }
            composable("recurring") {
                RecurringExpensesScreen(onNavigateBack = { nav.popBackStack() })
            }
            composable("categories") {
                CategoriesScreen(onNavigateBack = { nav.popBackStack() })
            }
            composable("add_manual") {
                AddManualScreen(onNavigateBack = { nav.popBackStack() })
            }
            composable("wallet_checkin") {
                WalletCheckinScreen(onNavigateBack = { nav.popBackStack() })
            }
            composable("divide_helper") {
                DivideHelperScreen(
                    onNavigateBack = { nav.popBackStack() },
                    onNavigateToBillSplit = { total, people ->
                        nav.navigate("bill_split?total=$total&people=$people")
                    }
                )
            }
            composable(
                "bill_split?total={total}&people={people}",
                arguments = listOf(
                    navArgument("total") { type = NavType.IntType; defaultValue = 0 },
                    navArgument("people") { type = NavType.IntType; defaultValue = 0 }
                )
            ) { backStackEntry ->
                val total = backStackEntry.arguments?.getInt("total") ?: 0
                val people = backStackEntry.arguments?.getInt("people") ?: 0
                BillSplitScreen(
                    initialTotal   = total,
                    initialPeople  = people,
                    onNavigateBack = { nav.popBackStack() }
                )
            }
            composable("bill_split") {
                BillSplitScreen(onNavigateBack = { nav.popBackStack() })
            }
        }
    }
}
