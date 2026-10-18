package com.example.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminModule

private data class RailDestination(
    val route: String,
    val label: String,
    val module: AdminModule?,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun AdminNavigationRail(
    currentRoute: String,
    onNavigate: (String, AdminModule?) -> Unit,
    modifier: Modifier = Modifier
) {
    val destinations = listOf(
        RailDestination("dashboard", "Ana Sayfa", null, Icons.Default.Dashboard),
        RailDestination("appointments", "İşler", AdminModule.RANDEVULAR, Icons.Default.DateRange),
        RailDestination("customers", "Müşteriler", AdminModule.MUSTERILER, Icons.Default.People),
        RailDestination("finance", "Finans", AdminModule.FINANS, Icons.Default.AccountBalance),
        RailDestination("modules", "Modüller", null, Icons.Default.GridView)
    )
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        header = { Spacer(Modifier.height(8.dp)) }
    ) {
        destinations.forEach { item ->
            NavigationRailItem(
                selected = isPrimaryDestinationSelected(item.route, currentRoute),
                onClick = { onNavigate(item.route, item.module) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
                alwaysShowLabel = true
            )
        }
    }
}

internal fun isPrimaryDestinationSelected(destination: String, currentRoute: String): Boolean {
    if (destination == currentRoute) return true
    if (destination == "appointments" && currentRoute == "randevular") return true
    if (destination == "customers" && currentRoute == "musteriler") return true
    if (destination == "finance" && currentRoute in setOf("finans", "service_receipt", "makbuz")) return true
    return destination == "modules" && currentRoute in setOf(
        "quotes", "teklifler", "new_proposal", "proposal_detail",
        "messages", "mesajlar", "message_templates", "maintenance", "bakim", "reports", "raporlar",
        "ads", "google_ads", "googleads", "reklamlar", "quick_iban", "hizli_iban",
        "iban_gonder", "usta_iban", "catalog", "stock", "stok", "campaigns", "whatsapp_status"
    )
}
