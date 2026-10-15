package com.maven.codmbooster

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.BatteryManager
import android.os.Bundle
import android.os.StatFs
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var statsText: TextView
    private lateinit var statusText: TextView
    private lateinit var root: LinearLayout

    private val bg = Color.rgb(11, 15, 13)
    private val panel = Color.rgb(23, 30, 26)
    private val lime = Color.rgb(163, 230, 53)
    private val white = Color.rgb(245, 247, 244)
    private val muted = Color.rgb(174, 185, 177)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildDashboard()
        refreshStats()
    }

    private fun buildDashboard() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg); isFillViewport = true }
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
            setBackgroundColor(bg)
        }
        scroll.addView(root)
        setContentView(scroll)

        root.addView(text("MAVEN", 13f, lime, true))
        root.addView(text("CODM BOOSTER", 29f, white, true))
        root.addView(text("LIGHTWEIGHT GAMING COMPANION", 11f, muted, true).apply {
            letterSpacing = 0.08f
            setPadding(0, dp(3), 0, dp(20))
        })

        val hero = card()
        hero.addView(text("READY YOUR PHONE", 12f, lime, true))
        hero.addView(text("Less clutter. Smarter settings. More control.", 19f, white, true).apply {
            setPadding(0, dp(10), 0, dp(8))
        })
        hero.addView(text("Made for low-memory Android phones like the Galaxy A04e.", 14f, muted, false))
        hero.addView(actionButton("REFRESH DEVICE STATUS") { refreshStats() })

        root.addView(sectionTitle("DEVICE STATUS"))
        val stats = card()
        statsText = text("Reading Android system information…", 14f, white, false)
        stats.addView(statsText)

        root.addView(sectionTitle("CODM QUICK PREP"))
        val prep = card()
        prep.addView(text("1  Close apps you don't need", 16f, white, true))
        prep.addView(text("Use Android's recent-apps screen. Android may keep some apps in memory by design.", 13f, muted, false))
        prep.addView(separator())
        prep.addView(text("2  Keep graphics lightweight", 16f, white, true))
        prep.addView(text("Start with Low graphics and the highest frame-rate option that stays stable on your device.", 13f, muted, false))
        prep.addView(separator())
        prep.addView(text("3  Reduce heat before a match", 16f, white, true))
        prep.addView(text("Unplug while playing if practical, lower screen brightness, and avoid playing while the phone is hot.", 13f, muted, false))

        root.addView(sectionTitle("LAUNCH GAME"))
        val launchCard = card()
        statusText = text("CODM launch is available if the game is installed.", 13f, muted, false)
        launchCard.addView(statusText)
        launchCard.addView(actionButton("OPEN CALL OF DUTY: MOBILE") { launchCodm() })

        root.addView(text("HONEST BY DESIGN", 12f, lime, true).apply {
            setPadding(0, dp(20), 0, dp(6))
        })
        root.addView(text("This app does not claim to overclock your CPU/GPU, change touch sampling, or guarantee higher FPS or lower ping. Android restricts those controls.", 12f, muted, false))
    }

    private fun refreshStats() {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memory = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(memory)
        val totalRam = memory.totalMem / (1024L * 1024L)
        val availableRam = memory.availMem / (1024L * 1024L)
        val usedRam = (totalRam - availableRam).coerceAtLeast(0)
        val ramPercent = if (totalRam > 0) (usedRam * 100 / totalRam).toInt() else 0

        val storage = StatFs(filesDir.absolutePath)
        val freeStorage = storage.availableBytes / (1024.0 * 1024.0 * 1024.0)
        val totalStorage = storage.totalBytes / (1024.0 * 1024.0 * 1024.0)

        val battery = registerReceiver(null, IntentFilterCompat.batteryChanged())
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPercent = if (level >= 0 && scale > 0) level * 100 / scale else -1
        val tempTenths = battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val tempText = if (tempTenths >= 0) String.format(Locale.US, "%.1f°C", tempTenths / 10.0) else "Unavailable"
        val batteryText = if (batteryPercent >= 0) "$batteryPercent%" else "Unavailable"

        statsText.text = "RAM in use (system estimate): $usedRam / $totalRam MB ($ramPercent%)\n" +
            "RAM currently available: $availableRam MB\n" +
            "Free internal storage visible to app: " + String.format(Locale.US, "%.1f", freeStorage) + " GB\n" +
            "App-visible storage volume: " + String.format(Locale.US, "%.1f", totalStorage) + " GB\n" +
            "Battery: $batteryText\nBattery temperature: $tempText"
        statusText.text = "Status refreshed. System readings are estimates and may change while CODM is running."
    }

    private fun launchCodm() {
        val packages = listOf("com.activision.callofduty.shooter", "com.garena.game.codm")
        val intent = packages.firstNotNullOfOrNull { packageName ->
            try { packageManager.getLaunchIntentForPackage(packageName) } catch (_: Exception) { null }
        }
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            Toast.makeText(this, "CODM was not found. Open the version installed on your phone manually.", Toast.LENGTH_LONG).show()
        }
    }

    private fun sectionTitle(value: String) = text(value, 12f, lime, true).apply {
        setPadding(2, dp(22), 0, dp(10))
        letterSpacing = 0.06f
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        setBackgroundColor(panel)
        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = dp(4)
        root.addView(this, params)
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
        gravity = Gravity.START
        setPadding(0, dp(3), 0, dp(3))
        setLineSpacing(dp(3).toFloat(), 1.0f)
    }

    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        setTextColor(bg)
        textSize = 12f
        isAllCaps = false
        setBackgroundColor(lime)
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(14) }
    }

    private fun separator() = View(this).apply {
        setBackgroundColor(Color.rgb(49, 60, 52))
        layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply {
            topMargin = dp(12)
            bottomMargin = dp(12)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

private object IntentFilterCompat {
    fun batteryChanged(): android.content.IntentFilter =
        android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
}
