package com.nobody.valurex.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nobody.valurex.R
import com.nobody.valurex.ui.components.ManageRow
import com.nobody.valurex.ui.components.PageTitle
import com.nobody.valurex.ui.components.RowPosition
import com.nobody.valurex.ui.components.SectionLabel
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.shapeLarge
import com.nobody.valurex.ui.theme.shapePill

@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val vc         = ValurexColors
    val vt         = ValurexTypography
    val uriHandler = LocalUriHandler.current

    Scaffold(
        containerColor      = vc.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp)
        ) {
            item {
                PageTitle(
                    text    = "About",
                    leading = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = vc.TextHigh)
                        }
                    }
                )
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 14.dp),
                    shape = shapeLarge,
                    color = vc.Surface
                ) {
                    Column(
                        modifier            = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter            = painterResource(R.drawable.avatar_haroon),
                            contentDescription = "Profile photo",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Muhammad Haroon", style = vt.profileName, color = vc.TextHigh)
                        Spacer(Modifier.height(4.dp))
                        Text("AI · Computer Vision · SZABIST", style = vt.rowSubtitle, color = vc.TextMedium)
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SocialButton(
                                icon     = Icons.Outlined.Work,
                                label    = "LinkedIn",
                                onClick  = { uriHandler.openUri("https://www.linkedin.com/in/haroon-ai1/") },
                                vc       = vc,
                                vt       = vt,
                                modifier = Modifier.weight(1f)
                            )
                            SocialButton(
                                icon     = Icons.Outlined.Code,
                                label    = "GitHub",
                                onClick  = { uriHandler.openUri("https://github.com/haroon-ai1") },
                                vc       = vc,
                                vt       = vt,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item { SectionLabel("Get in touch") }

            item {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    ManageRow(
                        icon         = Icons.Outlined.AlternateEmail,
                        iconBg       = vc.IconBgEmail,
                        iconFg       = vc.IconFgTransport,
                        title        = "muhammadharoon8124@gmail.com",
                        subtitle     = "Tap to email",
                        position     = RowPosition.TOP,
                        onClick      = { uriHandler.openUri("mailto:muhammadharoon8124@gmail.com") },
                        trailingIcon = Icons.Outlined.OpenInNew
                    )
                    ManageRow(
                        icon         = Icons.Outlined.Work,
                        iconBg       = vc.IconBgLinkedIn,
                        iconFg       = vc.IconFgTransport,
                        title        = "LinkedIn",
                        subtitle     = "linkedin.com/in/haroon-ai1",
                        position     = RowPosition.BOTTOM,
                        onClick      = { uriHandler.openUri("https://www.linkedin.com/in/haroon-ai1/") },
                        trailingIcon = Icons.Outlined.OpenInNew
                    )
                }
            }

            item { SectionLabel("About") }

            item {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    ManageRow(
                        icon         = Icons.Outlined.Info,
                        iconBg       = vc.IconBgInfo,
                        iconFg       = vc.TextLow,
                        title        = "Valurex",
                        subtitle     = "Version 1.0",
                        position     = RowPosition.SINGLE,
                        onClick      = null,
                        trailingIcon = null
                    )
                }
            }

            item {
                Box(
                    modifier         = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Made in Islamabad · 2026", style = vt.captionTiny, color = vc.TextLow)
                }
            }
        }
    }
}

@Composable
private fun SocialButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    vc: ValurexColors,
    vt: ValurexTypography,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(vc.Background, shapePill)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = vc.TextHigh, modifier = Modifier.size(14.dp))
            Text(label, style = vt.pillButton, color = vc.TextHigh)
        }
    }
}
