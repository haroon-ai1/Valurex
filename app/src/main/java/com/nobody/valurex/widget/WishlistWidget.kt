package com.nobody.valurex.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.nobody.valurex.MainActivity
import com.nobody.valurex.R
import com.nobody.valurex.data.db.ValurexDatabase
import com.nobody.valurex.data.db.entity.WishlistItem
import kotlinx.coroutines.flow.first

class WishlistWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db          = ValurexDatabase.getInstance(context)
        val wishlistDao = db.wishlistDao()

        val count = wishlistDao.getCount().first()

        val item: WishlistItem? = if (count > 0) {
            val cycleIndex = ((System.currentTimeMillis() / (30 * 60 * 1000L)) % count).toInt()
            wishlistDao.getNthItem(cycleIndex)
        } else null

        val bitmap: Bitmap? = item?.imageUri?.let { uriString ->
            try {
                val request = ImageRequest.Builder(context).data(Uri.parse(uriString)).build()
                val result  = ImageLoader(context).execute(request)
                (result as? SuccessResult)?.drawable?.toBitmap()
            } catch (_: Exception) { null }
        }

        val launchAction = actionStartActivity(Intent(context, MainActivity::class.java))

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF141414))
                    .cornerRadius(24.dp)
                    .clickable(launchAction)
                    .padding(20.dp),
                contentAlignment = Alignment.TopStart
            ) {
                if (item == null) {
                    EmptyWishlistContent()
                } else {
                    WishlistItemContent(item, count, bitmap)
                }
            }
        }
    }
}

@Composable
private fun EmptyWishlistContent() {
    Column(
        modifier            = GlanceModifier.fillMaxSize(),
        verticalAlignment   = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Image(
            provider           = ImageProvider(R.drawable.ic_star),
            contentDescription = null,
            modifier           = GlanceModifier.size(32.dp),
            colorFilter        = ColorFilter.tint(ColorProvider(Color(0xFF5A5A5A)))
        )
        Spacer(GlanceModifier.height(8.dp))
        Text(
            "Add your first wish",
            style = TextStyle(
                color      = ColorProvider(Color(0xFFFFFFFF)),
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.height(6.dp))
        Row(
            modifier          = GlanceModifier.background(Color(0x268C52FF)).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Image(
                provider           = ImageProvider(R.drawable.ic_plus),
                contentDescription = null,
                modifier           = GlanceModifier.size(14.dp),
                colorFilter        = ColorFilter.tint(ColorProvider(Color(0xFFFFFFFF)))
            )
            Spacer(GlanceModifier.width(6.dp))
            Text("Add", style = TextStyle(color = ColorProvider(Color(0xFFFFFFFF)), fontSize = 12.sp))
        }
    }
}

@Composable
private fun WishlistItemContent(
    item: WishlistItem,
    count: Int,
    bitmap: Bitmap?
) {
    val itemIndex = ((System.currentTimeMillis() / (30 * 60 * 1000L)) % count).toInt() + 1
    val showBars  = count <= 3

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                "Wishlist",
                modifier = GlanceModifier.defaultWeight(),
                style    = TextStyle(color = ColorProvider(Color(0xFFFFFFFF)), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            )
            Text(
                "$count items",
                style = TextStyle(color = ColorProvider(Color(0xFF8A8A8A)), fontSize = 12.sp)
            )
        }

        Spacer(GlanceModifier.height(16.dp))

        Row(
            modifier          = GlanceModifier.fillMaxWidth().defaultWeight(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Box(
                modifier         = GlanceModifier.size(64.dp).background(Color(0xFF2A2A2A)).cornerRadius(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        provider           = ImageProvider(bitmap),
                        contentDescription = null,
                        modifier           = GlanceModifier.fillMaxSize().cornerRadius(12.dp),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    Image(
                        provider           = ImageProvider(R.drawable.ic_camera),
                        contentDescription = null,
                        modifier           = GlanceModifier.size(24.dp),
                        colorFilter        = ColorFilter.tint(ColorProvider(Color(0xFF8A8A8A)))
                    )
                }
            }

            Spacer(GlanceModifier.width(16.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    item.name,
                    maxLines = 1,
                    style    = TextStyle(
                        color      = ColorProvider(Color(0xFFFFFFFF)),
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(4.dp))
                if (item.price != null) {
                    Text(
                        "Rs %,d".format(item.price),
                        style = TextStyle(
                            color      = ColorProvider(Color(0xFF8C52FF)),
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(GlanceModifier.width(8.dp))

            Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
                Text(
                    "$itemIndex / $count",
                    style = TextStyle(color = ColorProvider(Color(0xFF8A8A8A)), fontSize = 11.sp)
                )
            }
        }
    }
}
