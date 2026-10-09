package com.example.ui.screens

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.RobotViewModel
import com.example.ui.theme.RobotBackgroundDark
import com.example.ui.theme.RobotCardBorder
import com.example.ui.theme.RobotCyanPrimary
import com.example.ui.theme.RobotEmeraldSuccess
import com.example.ui.theme.RobotSurfaceDark
import com.example.ui.theme.RobotSurfaceVariantDark

@Composable
fun BrowserScreen(viewModel: RobotViewModel) {
    val browserEngine = viewModel.browserEngine
    val currentUrl by browserEngine.currentUrl.collectAsState()
    val pageTitle by browserEngine.pageTitle.collectAsState()
    val isLoading by browserEngine.isLoading.collectAsState()
    val lastScrape by browserEngine.lastScrapedData.collectAsState()

    var urlInput by remember { mutableStateOf(currentUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RobotBackgroundDark)
    ) {
        // Address & Navigation Bar
        Surface(
            color = RobotSurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure",
                        tint = RobotEmeraldSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("browser_url_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RobotCyanPrimary,
                            unfocusedBorderColor = RobotCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { browserEngine.navigateTo(urlInput) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = RobotCyanPrimary)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Go", tint = RobotCyanPrimary)
                        }
                    }

                    IconButton(
                        onClick = { browserEngine.webViewInstance?.reload() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color(0xFF94A3B8))
                    }
                }

                // Quick Site Portals
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        PortalChip(label = "Temu Affiliate", url = "https://affiliate.temu.com") {
                            urlInput = it
                            browserEngine.navigateTo(it)
                        }
                    }
                    item {
                        PortalChip(label = "AliExpress Portals", url = "https://portals.aliexpress.com") {
                            urlInput = it
                            browserEngine.navigateTo(it)
                        }
                    }
                    item {
                        PortalChip(label = "Pinterest", url = "https://www.pinterest.com") {
                            urlInput = it
                            browserEngine.navigateTo(it)
                        }
                    }
                    item {
                        PortalChip(label = "Google Search", url = "https://www.google.com") {
                            urlInput = it
                            browserEngine.navigateTo(it)
                        }
                    }
                }
            }
        }

        // Automation Action Strip
        Surface(
            color = RobotSurfaceVariantDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = pageTitle.take(30),
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { browserEngine.extractPageProductDom() },
                    colors = ButtonDefaults.buttonColors(containerColor = RobotCyanPrimary),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("scrape_dom_button")
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scrape DOM", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live Scraped DOM Data Banner (if available)
        lastScrape?.let { scrape ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
                border = BorderStroke(1.dp, RobotEmeraldSuccess),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Extracted DOM Product",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RobotEmeraldSuccess
                        )
                        Text(
                            text = scrape.priceText.ifBlank { "$0.00" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = scrape.title.ifBlank { "Sample Product" },
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Embedded Android WebView
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        browserEngine.attachWebView(this)
                        loadUrl(currentUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun PortalChip(
    label: String,
    url: String,
    onClick: (String) -> Unit
) {
    Surface(
        color = RobotSurfaceVariantDark,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, RobotCardBorder),
        modifier = Modifier.clickable { onClick(url) }
    ) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            color = Color(0xFFCBD5E1),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
