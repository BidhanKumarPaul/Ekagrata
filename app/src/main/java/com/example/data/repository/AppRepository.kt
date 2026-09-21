package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.example.data.local.AppPreferenceDao
import com.example.data.local.AppPreferenceEntity
import com.example.model.AppCategory
import com.example.model.AppInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val preferenceDao: AppPreferenceDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val applicationScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val packageManager: PackageManager = context.packageManager
    private val myPackageName: String = context.packageName

    // Raw cached list of installed applications from PackageManager
    private val _rawInstalledApps = MutableStateFlow<List<RawAppItem>>(emptyList())

    // Combined Flow: Installed apps + Room preferences
    val appsFlow: StateFlow<List<AppInfo>> = combine(
        _rawInstalledApps,
        preferenceDao.getAllPreferences()
    ) { rawApps, preferences ->
        val prefMap = preferences.associateBy { it.packageName }

        rawApps.map { raw ->
            val pref = prefMap[raw.packageName]
            val defaultCategory = guessCategory(raw.packageName, raw.label)
            val defaultEssential = defaultCategory == AppCategory.ESSENTIAL
            val defaultAllowed = defaultCategory == AppCategory.ESSENTIAL || defaultCategory == AppCategory.STUDY

            AppInfo(
                packageName = raw.packageName,
                activityName = raw.activityName,
                label = raw.label,
                isFavorite = pref?.isFavorite ?: false,
                isAllowedInFocus = pref?.isAllowedInFocus ?: defaultAllowed,
                isEssential = pref?.isEssential ?: defaultEssential,
                category = pref?.customCategory?.let { runCatching { AppCategory.valueOf(it) }.getOrNull() } ?: defaultCategory,
                installTime = raw.installTime
            )
        }.sortedWith(compareBy({ !it.isFavorite }, { it.label.lowercase() }))
    }.stateIn(
        scope = applicationScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    init {
        refreshInstalledApps()
    }

    /**
     * Queries PackageManager on IO dispatcher and updates the in-memory cache.
     */
    fun refreshInstalledApps() {
        applicationScope.launch(ioDispatcher) {
            val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfoList: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    launcherIntent,
                    PackageManager.ResolveInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(launcherIntent, 0)
            }

            val apps = resolveInfoList
                .filter { it.activityInfo.packageName != myPackageName }
                .map { resolveInfo ->
                    val pkgName = resolveInfo.activityInfo.packageName
                    val actName = resolveInfo.activityInfo.name
                    val label = resolveInfo.loadLabel(packageManager)?.toString()?.trim() ?: pkgName
                    val installTime = runCatching {
                        packageManager.getPackageInfo(pkgName, 0).firstInstallTime
                    }.getOrDefault(0L)

                    RawAppItem(
                        packageName = pkgName,
                        activityName = actName,
                        label = label,
                        installTime = installTime
                    )
                }
                .distinctBy { it.packageName }
                .sortedBy { it.label.lowercase() }

            _rawInstalledApps.value = apps
        }
    }

    /**
     * Launches the application safely. Returns true if launch succeeded.
     */
    fun launchApp(packageName: String, activityName: String? = null): Boolean {
        return try {
            val intent = if (!activityName.isNullOrBlank()) {
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setClassName(packageName, activityName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                }
            } else {
                packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                }
            }

            if (intent != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleFavorite(packageName: String) = withContext(ioDispatcher) {
        val current = preferenceDao.getPreference(packageName)
        val newFav = !(current?.isFavorite ?: false)
        if (current == null) {
            preferenceDao.upsertPreference(AppPreferenceEntity(packageName = packageName, isFavorite = newFav))
        } else {
            preferenceDao.updateFavorite(packageName, newFav)
        }
    }

    suspend fun toggleAllowedInFocus(packageName: String) = withContext(ioDispatcher) {
        val current = preferenceDao.getPreference(packageName)
        val defaultCategory = guessCategory(packageName, "")
        val defaultAllowed = defaultCategory == AppCategory.ESSENTIAL || defaultCategory == AppCategory.STUDY
        val currentAllowed = current?.isAllowedInFocus ?: defaultAllowed
        val newAllowed = !currentAllowed

        if (current == null) {
            preferenceDao.upsertPreference(AppPreferenceEntity(packageName = packageName, isAllowedInFocus = newAllowed))
        } else {
            preferenceDao.updateAllowedInFocus(packageName, newAllowed)
        }
    }

    suspend fun toggleEssential(packageName: String) = withContext(ioDispatcher) {
        val current = preferenceDao.getPreference(packageName)
        val defaultCategory = guessCategory(packageName, "")
        val defaultEssential = defaultCategory == AppCategory.ESSENTIAL
        val currentEssential = current?.isEssential ?: defaultEssential
        val newEssential = !currentEssential

        if (current == null) {
            preferenceDao.upsertPreference(AppPreferenceEntity(packageName = packageName, isEssential = newEssential))
        } else {
            preferenceDao.updateEssential(packageName, newEssential)
        }
    }

    private fun guessCategory(packageName: String, label: String): AppCategory {
        val p = packageName.lowercase()
        val l = label.lowercase()

        return when {
            p.contains("calc") || l.contains("calc") ||
            p.contains("clock") || l.contains("clock") ||
            p.contains("calendar") || l.contains("calendar") ||
            p.contains("notes") || l.contains("notes") ||
            p.contains("keep") || l.contains("keep") ||
            p.contains("contact") || p.contains("dialer") || p.contains("phone") ||
            p.contains("deskclock") || p.contains("settings") -> AppCategory.ESSENTIAL

            p.contains("drive") || p.contains("doc") || p.contains("sheet") ||
            p.contains("slide") || p.contains("pdf") || p.contains("reader") ||
            p.contains("anki") || p.contains("notion") || p.contains("obsidian") ||
            p.contains("coursera") || p.contains("duolingo") || p.contains("math") ||
            p.contains("physics") || p.contains("chem") || p.contains("study") ||
            p.contains("wiki") -> AppCategory.STUDY

            p.contains("gmail") || p.contains("mail") || p.contains("slack") ||
            p.contains("teams") || p.contains("zoom") || p.contains("trello") ||
            p.contains("github") || p.contains("termux") -> AppCategory.WORK

            p.contains("message") || p.contains("sms") || p.contains("whatsapp") ||
            p.contains("telegram") || p.contains("signal") || p.contains("discord") -> AppCategory.COMMUNICATION

            p.contains("instagram") || p.contains("facebook") || p.contains("tiktok") ||
            p.contains("twitter") || p.contains("reddit") || p.contains("snapchat") ||
            p.contains("pinterest") || p.contains("threads") -> AppCategory.SOCIAL

            p.contains("youtube") || p.contains("spotify") || p.contains("netflix") ||
            p.contains("prime") || p.contains("music") || p.contains("video") ||
            p.contains("twitch") || p.contains("podcast") -> AppCategory.ENTERTAINMENT

            p.contains("game") || p.contains("arcade") || p.contains("pubg") ||
            p.contains("chess") || p.contains("roblox") || p.contains("clash") -> AppCategory.GAMES

            else -> AppCategory.OTHER
        }
    }

    private data class RawAppItem(
        val packageName: String,
        val activityName: String,
        val label: String,
        val installTime: Long
    )
}
