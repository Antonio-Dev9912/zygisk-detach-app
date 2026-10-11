package com.jhc.detach

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.jhc.detach.ui.theme.ZygiskdetachTheme
import com.topjohnwu.superuser.Shell
import java.util.concurrent.Executors

@Composable
fun AppsList(
    packageManager: PackageManager,
    renderedApps: MutableState<List<DetachedApp>>,
    uninstalledAppBitmap: ImageBitmap,
    onDetachedChanged: (DetachedApp, Boolean) -> Unit
) {
    val selectedIndicatorColor = if (isSystemInDarkTheme()) Color.White else Color.Black

    LazyColumn {
        items(renderedApps.value, key = { it.packageName }) { app ->
            val checkedState = remember(app.packageName) { mutableStateOf(app.detached) }
            val onToggle = {
                checkedState.value = !checkedState.value
                app.detached = checkedState.value
                onDetachedChanged(app, checkedState.value)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            ) {
                Image(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    bitmap =
                        if (app.installed)
                            try {
                                packageManager.getApplicationIcon(app.packageName)
                                    .toBitmap()
                                    .asImageBitmap()
                            } catch (_: Exception) {
                                uninstalledAppBitmap
                            } else uninstalledAppBitmap,
                    contentDescription = app.label
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        text = app.label,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .then(
                            if (checkedState.value) {
                                Modifier.background(selectedIndicatorColor, CircleShape)
                            } else {
                                Modifier.border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                            }
                        )
                )
            }
        }
    }
}


@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    val f = LocalFocusManager.current
    OutlinedTextField(
        leadingIcon = {
            Icon(
                Icons.Filled.Search, ""
            )
        },
        trailingIcon = {
            Icon(
                Icons.Filled.Close, "", modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = {
                        onQueryChange("")
                        f.clearFocus()
                    })
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp, start = 10.dp, end = 10.dp, top = 5.dp),
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search") },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun AppsFilterBar(selectedFilter: AppFilter, onFilterSelected: (AppFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val darkTheme = isSystemInDarkTheme()
        AppFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            val chipShape = RoundedCornerShape(20.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(chipShape)
                    .background(
                        color = when {
                            !isSelected -> Color.Transparent
                            darkTheme -> Color(0xFF2A2D39)
                            else -> Color(0xFFE2E2E9)
                        },
                        shape = chipShape
                    )
                    .clickable { onFilterSelected(filter) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    color = when {
                        isSelected && darkTheme -> Color.White
                        isSelected -> Color.Black
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

enum class AppFilter(val label: String) {
    ALL("All"),
    USER("User"),
    SYSTEM("System"),
    DETACHED("Detached")
}

private const val FILTER_PREFERENCES = "app_filters"
private const val SELECTED_FILTER_KEY = "selected_filter"

private fun filterApps(apps: List<DetachedApp>, query: String, filter: AppFilter) =
    apps.filter { app ->
        val matchesFilter = when (filter) {
            AppFilter.ALL -> true
            AppFilter.USER -> !app.isSystemApp
            AppFilter.SYSTEM -> app.isSystemApp
            AppFilter.DETACHED -> app.detached
        }
        val matchesQuery = query.isBlank() ||
            app.label.contains(query, ignoreCase = true) ||
            app.packageName.contains(query, ignoreCase = true)
        matchesFilter && matchesQuery
    }

data class DetachedApp(
    val packageName: String,
    val label: String,
    var detached: Boolean = false,
    val installed: Boolean = true,
    val isSystemApp: Boolean = false
)

class Toaster(private val context: Context) {
    fun toast(msg: String, delay: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, msg, delay).show()
    }
}

fun runShell(cmd: String, toaster: Toaster): String {
    val op = Shell.cmd(cmd).exec()
    if (op.code != 0) {
        toaster.toast("ERROR: " + op.err.joinToString("\n"), Toast.LENGTH_LONG)
        return ""
    } else {
        return op.out.joinToString("\n")
    }
}

fun String.splitn() =
    if (isEmpty()) emptyList()
    else this.split('\n')

class MainActivity : ComponentActivity() {
    private val shellExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toaster = Toaster(applicationContext)
        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_MOUNT_MASTER))
        if (!Shell.getShell().isRoot) {
            toaster.toast("Not root", Toast.LENGTH_SHORT)
            finishAndRemoveTask()
            return
        }
        if (Shell.cmd("test -f /data/adb/modules/zygisk-detach/detach").exec().code != 0) {
            toaster.toast("zygisk-detach is not installed", Toast.LENGTH_SHORT)
            finishAndRemoveTask()
            return
        }

        val filterPreferences = getSharedPreferences(FILTER_PREFERENCES, MODE_PRIVATE)
        val initialFilterName = filterPreferences.getString(SELECTED_FILTER_KEY, AppFilter.ALL.name)
        val initialFilter = AppFilter.entries.firstOrNull { it.name == initialFilterName }
            ?: AppFilter.ALL
        val apps = packageManager.getInstalledPackages(0).mapNotNull {
            it.applicationInfo?.let { info ->
                DetachedApp(
                    packageName = it.packageName,
                    label = packageManager.getApplicationLabel(info).toString(),
                    isSystemApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
        }.sortedBy { it.label }.toMutableList()
        val alDetach: List<String> = try {
            runShell("/data/adb/modules/zygisk-detach/detach list", toaster).splitn()
        } catch (_: IndexOutOfBoundsException) {
            runShell("/data/adb/modules/zygisk-detach/detach reset", toaster)
            listOf()
        }
        for (d in alDetach) {
            if (apps.find { it.packageName == d } == null) {
                apps.add(DetachedApp(d, d, detached = true, installed = false))
            }
        }
        apps.forEach { it.detached = alDetach.contains(it.packageName) }
        val appsFinal = apps.sortedBy { it.label }.sortedBy { !it.detached }.toList()
        setContent {
            val detachedApps = remember { mutableStateOf(appsFinal) }
            val searchQuery = remember { mutableStateOf("") }
            val selectedFilter = remember { mutableStateOf(initialFilter) }
            val renderedApps = remember {
                mutableStateOf(filterApps(appsFinal, searchQuery.value, selectedFilter.value))
            }
            fun updateRenderedApps() {
                renderedApps.value = filterApps(
                    detachedApps.value,
                    searchQuery.value,
                    selectedFilter.value
                )
            }
            ZygiskdetachTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                    ) {
                        SearchBar(
                            query = searchQuery.value,
                            onQueryChange = { query ->
                                searchQuery.value = query
                                updateRenderedApps()
                            }
                        )
                        AppsFilterBar(
                            selectedFilter = selectedFilter.value,
                            onFilterSelected = { filter ->
                                selectedFilter.value = filter
                                filterPreferences.edit()
                                    .putString(SELECTED_FILTER_KEY, filter.name)
                                    .apply()
                                updateRenderedApps()
                            }
                        )
                        AppsList(
                            packageManager = packageManager,
                            renderedApps = renderedApps,
                            uninstalledAppBitmap = getDrawable(R.mipmap.unistalled_app)!!
                                .toBitmap().asImageBitmap(),
                            onDetachedChanged = { app, detached ->
                                updateRenderedApps()
                                val action = if (detached) "detach" else "reattach"
                                shellExecutor.execute {
                                    val result = Shell.cmd(
                                        "/data/adb/modules/zygisk-detach/detach $action ${app.packageName}"
                                    ).exec()
                                    if (result.code != 0) {
                                        runOnUiThread {
                                            toaster.toast(
                                                "ERROR: " + result.err.joinToString("\n"),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        shellExecutor.shutdown()
        super.onDestroy()
    }
}
