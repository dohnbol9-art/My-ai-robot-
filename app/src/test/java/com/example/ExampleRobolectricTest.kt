package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.affiliate.AffiliateEngine
import com.example.security.SecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("My AI Robot", appName)
    }

    @Test
    fun `test Temu affiliate link generation and verification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val securityManager = SecurityManager(context)
        val affiliateEngine = AffiliateEngine(securityManager)

        val origUrl = "https://www.temu.com/goods-wireless-garlic-chopper-60109923812.html"
        val affiliateLink = affiliateEngine.generateTemuAffiliateLink(origUrl, "act73919")

        assertTrue(affiliateLink.contains("temu.to/m/act73919"))
        val verification = affiliateEngine.verifyAffiliateTrackingLink(affiliateLink, "Temu")
        assertTrue(verification.isValid)
    }

    @Test
    fun `test AliExpress affiliate link generation and verification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val securityManager = SecurityManager(context)
        val affiliateEngine = AffiliateEngine(securityManager)

        val origUrl = "https://www.aliexpress.com/item/1005004892182710.html"
        val affiliateLink = affiliateEngine.generateAliExpressAffiliateLink(origUrl, "ali_pro_partner_99")

        assertTrue(affiliateLink.contains("s.click.aliexpress.com"))
        val verification = affiliateEngine.verifyAffiliateTrackingLink(affiliateLink, "AliExpress")
        assertTrue(verification.isValid)
    }
}
