package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PinDraftEntity
import com.example.data.ProductItemEntity
import com.example.ui.RobotViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RobotAmberWarning
import com.example.ui.theme.RobotBackgroundDark
import com.example.ui.theme.RobotCardBorder
import com.example.ui.theme.RobotCyanPrimary
import com.example.ui.theme.RobotEmeraldSuccess
import com.example.ui.theme.RobotSurfaceDark
import com.example.ui.theme.RobotSurfaceVariantDark
import java.io.File

@Composable
fun MarketingScreen(viewModel: RobotViewModel) {
    val context = LocalContext.current
    val products by viewModel.allProducts.collectAsState()
    val pinDrafts by viewModel.allPinDrafts.collectAsState()
    val pendingPins by viewModel.pendingPins.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Pinterest Pins (${pinDrafts.size})", "Researched Products (${products.size})")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RobotBackgroundDark)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = RobotSurfaceDark,
            contentColor = RobotCyanPrimary,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = RobotCyanPrimary,
                    height = 3.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (selectedTab == index) RobotCyanPrimary else Color(0xFF94A3B8)
                        )
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // Pinterest Pins List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (pendingPins.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF14243A)),
                            border = BorderStroke(1.dp, RobotCyanPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${pendingPins.size} Pins Awaiting Approval",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Target Board: Kitchen Gadgets",
                                        color = RobotCyanPrimary,
                                        fontSize = 11.sp
                                    )
                                }
                                Button(
                                    onClick = { viewModel.approveAndResumeCurrentTask() },
                                    colors = ButtonDefaults.buttonColors(containerColor = RobotEmeraldSuccess),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Approve All", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                if (pinDrafts.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Pinterest Pins Yet",
                            description = "Run the 10 Kitchen Gadgets workflow from the Assistant tab to generate vertical 1000x1500 pins and SEO copy."
                        )
                    }
                } else {
                    items(pinDrafts, key = { it.id }) { pin ->
                        PinDraftCard(pin = pin, viewModel = viewModel, context = context)
                    }
                }
            }
        } else {
            // Researched Products
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (products.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Products Researched Yet",
                            description = "Ask My AI Robot to research Temu and AliExpress products in any niche."
                        )
                    }
                } else {
                    items(products, key = { it.id }) { product ->
                        ProductItemCard(product = product, context = context)
                    }
                }
            }
        }
    }
}

@Composable
fun PinDraftCard(
    pin: PinDraftEntity,
    viewModel: RobotViewModel,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RobotSurfaceDark),
        border = BorderStroke(1.dp, RobotCardBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Status & Board
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE11D48).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "📌 ${pin.boardName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFB7185)
                        )
                    }
                }
                StatusBadge(status = pin.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Thumbnail Preview + Details
            Row(modifier = Modifier.fillMaxWidth()) {
                // 1000x1500 Vertical Thumbnail (Aspect Ratio 2:3)
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(RobotSurfaceVariantDark)
                        .border(1.dp, RobotCardBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val file = pin.localImageUri?.let { File(it) }
                    if (file != null && file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = pin.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = pin.imageUrl,
                            contentDescription = pin.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Format badge overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("1000×1500", fontSize = 8.sp, color = RobotCyanPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pin.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color.White,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pin.description.take(120) + "...",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = pin.hashtags,
                        fontSize = 10.5.sp,
                        color = RobotCyanPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Verified Affiliate Tracking URL Box
            Surface(
                color = Color(0xFF09121F),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, RobotEmeraldSuccess.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = RobotEmeraldSuccess, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pin.affiliateUrl,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFA7F3D0),
                            maxLines = 1
                        )
                    }
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Affiliate Link", pin.affiliateUrl))
                            Toast.makeText(context, "Affiliate link copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = RobotCyanPrimary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Live Pin Link (if published)
            pin.livePinUrl?.let { liveUrl ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = RobotCyanPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Live Pin: $liveUrl",
                        fontSize = 11.sp,
                        color = RobotCyanPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Actions
            if (pin.status == "PENDING_APPROVAL") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { viewModel.approveSinglePin(pin.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = RobotEmeraldSuccess),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Approve", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(product: ProductItemEntity, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RobotSurfaceDark),
        border = BorderStroke(1.dp, RobotCardBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Platform Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (product.sourcePlatform == "Temu") Color(0xFFF97316).copy(alpha = 0.2f)
                            else Color(0xFFEF4444).copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = product.sourcePlatform + " Affiliate",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.sourcePlatform == "Temu") Color(0xFFFB923C) else Color(0xFFF87171)
                    )
                }

                // Suitability Score
                Text(
                    text = "Score: ${product.suitabilityScore}/100",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RobotEmeraldSuccess
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = RobotEmeraldSuccess
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$${String.format("%.2f", product.originalPrice)}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = RobotAmberWarning, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${product.rating} (${product.reviewsCount})",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }

                Text(
                    text = "Est. ${(product.commissionRate * 100).toInt()}% Comm.",
                    fontSize = 11.sp,
                    color = RobotCyanPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Verified Link Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(RobotSurfaceVariantDark)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.affiliateTrackingUrl,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = RobotCyanPrimary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Tracking Link", product.affiliateTrackingUrl))
                        Toast.makeText(context, "Tracking link copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RobotSurfaceDark),
        border = BorderStroke(1.dp, RobotCardBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = Color(0xFF94A3B8), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
