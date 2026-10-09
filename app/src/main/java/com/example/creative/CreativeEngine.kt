package com.example.creative

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.data.PinDraftEntity
import com.example.data.ProductItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CreativeEngine(private val context: Context) {

    /**
     * Creates SEO content and 1000x1500 vertical Pinterest pin image for a product
     */
    suspend fun generatePinForProduct(
        product: ProductItemEntity,
        targetBoard: String = "Kitchen Gadgets"
    ): PinDraftEntity = withContext(Dispatchers.IO) {
        val seoData = generateSeoMetadata(product)
        val imageFile = generatePinterestImage1000x1500(product)

        PinDraftEntity(
            id = "pin_${product.id}",
            taskId = product.taskId,
            productId = product.id,
            title = seoData.title,
            description = seoData.description,
            keywords = seoData.keywords,
            hashtags = seoData.hashtags,
            altText = seoData.altText,
            boardName = targetBoard,
            imageUrl = product.imageUrl,
            localImageUri = imageFile?.absolutePath,
            affiliateUrl = product.affiliateTrackingUrl,
            status = "PENDING_APPROVAL"
        )
    }

    data class SeoResult(
        val title: String,
        val description: String,
        val keywords: String,
        val hashtags: String,
        val altText: String
    )

    fun generateSeoMetadata(product: ProductItemEntity): SeoResult {
        val title = "Top Rated ${product.title.take(65)} | Must-Have Kitchen Hack"
        val savings = (((product.originalPrice - product.price) / product.originalPrice) * 100).toInt()
        val description = buildString {
            append("Upgrade your cooking routine with this viral ${product.title}! ")
            append("Rated ${product.rating}★ by ${product.reviewsCount}+ home chefs. ")
            append("Get it today for only $${String.format("%.2f", product.price)} (Save ${savings}%). ")
            append("Tap the link to grab this viral deal before stock runs out!")
        }
        val keywords = "${product.niche.lowercase()}, smart kitchen tools, kitchen gadgets, viral cooking hacks, amazon temu aliexpress kitchen finds"
        val hashtags = "#KitchenGadgets #KitchenHacks #HomeCooking #ViralFinds #SmartKitchen #CulinaryTools"
        val altText = "A high-quality showcase of ${product.title} with rating ${product.rating} stars and special discount pricing."

        return SeoResult(title, description, keywords, hashtags, altText)
    }

    /**
     * Generates a 1000 x 1500 vertical Pinterest marketing image asset.
     */
    fun generatePinterestImage1000x1500(product: ProductItemEntity): File? {
        return try {
            val width = 1000
            val height = 1500
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // 1. Sleek modern gradient background (Deep Slate to Indigo/Navy)
            val bgPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(Color.parseColor("#0F172A"), Color.parseColor("#1E293B"), Color.parseColor("#090D16")),
                    floatArrayOf(0f, 0.65f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // 2. Decorative glowing accent header bar
            val accentPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, width.toFloat(), 0f,
                    Color.parseColor("#0284C7"), Color.parseColor("#38BDF8"),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), 18f, accentPaint)

            // 3. Top Category Tag Pill
            val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E293B")
                style = Paint.Style.FILL
            }
            val pillBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#38BDF8")
                strokeWidth = 3f
                style = Paint.Style.STROKE
            }
            val pillRect = RectF(280f, 70f, 720f, 135f)
            canvas.drawRoundRect(pillRect, 32f, 32f, pillPaint)
            canvas.drawRoundRect(pillRect, 32f, 32f, pillBorder)

            val pillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#38BDF8")
                textSize = 30f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("VIRAL KITCHEN FIND 2026", 500f, 115f, pillTextPaint)

            // 4. Bold Catchy Title
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 54f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            // Word wrap title
            val words = product.title.split(" ")
            val line1 = words.take(4).joinToString(" ")
            val line2 = words.drop(4).take(5).joinToString(" ")
            canvas.drawText(line1, 500f, 230f, titlePaint)
            if (line2.isNotBlank()) {
                canvas.drawText(line2, 500f, 295f, titlePaint)
            }

            // 5. Hero Product Visual Frame (mock high-end card container)
            val cardBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E293B")
                style = Paint.Style.FILL
            }
            val cardRect = RectF(70f, 340f, 930f, 1080f)
            canvas.drawRoundRect(cardRect, 40f, 40f, cardBg)

            // Inner image backdrop
            val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 340f, 0f, 1080f,
                    Color.parseColor("#334155"), Color.parseColor("#0F172A"),
                    Shader.TileMode.CLAMP
                )
            }
            val innerRect = RectF(90f, 360f, 910f, 1060f)
            canvas.drawRoundRect(innerRect, 30f, 30f, innerPaint)

            // Robot verified sticker on the card
            val stickerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#10B981") // emerald
                style = Paint.Style.FILL
            }
            val stickerRect = RectF(120f, 390f, 450f, 450f)
            canvas.drawRoundRect(stickerRect, 20f, 20f, stickerPaint)

            val stickerText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 28f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("✓ AI VERIFIED DEAL", 285f, 432f, stickerText)

            // Rating & Reviews Ribbon
            val ratingText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#F59E0B") // amber
                textSize = 42f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("★ ${product.rating} / 5.0 (${product.reviewsCount} reviews)", 500f, 750f, ratingText)

            val platformText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#94A3B8")
                textSize = 34f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Discovered on ${product.sourcePlatform} Affiliate", 500f, 810f, platformText)

            // Price & Savings Display
            val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#34D399")
                textSize = 78f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("$${String.format("%.2f", product.price)}", 500f, 920f, pricePaint)

            val oldPricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#94A3B8")
                textSize = 38f
                isStrikeThruText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Was $${String.format("%.2f", product.originalPrice)}", 500f, 975f, oldPricePaint)

            // 6. High-Converting Bottom Action Button
            val ctaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    100f, 1140f, 900f, 1260f,
                    Color.parseColor("#E11D48"), Color.parseColor("#FB7185"),
                    Shader.TileMode.CLAMP
                )
            }
            val ctaRect = RectF(120f, 1140f, 880f, 1250f)
            canvas.drawRoundRect(ctaRect, 36f, 36f, ctaPaint)

            val ctaTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 42f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("👉 TAP TO SEE TODAY'S DEAL", 500f, 1210f, ctaTextPaint)

            // 7. Footer Pinterest Tagline
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#64748B")
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Save this pin • Link in bio for discount code", 500f, 1340f, footerPaint)

            // Save file
            val outputFile = File(context.cacheDir, "pin_${product.id}_1000x1500.jpg")
            val fos = FileOutputStream(outputFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos)
            fos.flush()
            fos.close()
            bitmap.recycle()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
