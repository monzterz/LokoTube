package com.leplay.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LocalPlaylistsScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
) {
    val c = MaterialTheme.colorScheme
    val context = LocalContext.current
    var selectedId by remember { mutableStateOf<String?>(null) }
    var createDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<LocalPlaylist?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(LocalPlaylists.exportJson().toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, I18n.t("local_pl_exported", "Playlists exported"), Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, I18n.t("export_failed", "Export failed"), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            val n = if (text.isNullOrBlank()) 0 else LocalPlaylists.importJson(text)
            Toast.makeText(
                context,
                if (n > 0) I18n.t("local_pl_imported", "Imported {n} playlists").replace("{n}", n.toString())
                else I18n.t("local_pl_import_none", "Nothing to import"),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val selected = selectedId?.let { id -> LocalPlaylists.playlists.find { it.id == id } }

    Box(Modifier.fillMaxSize().background(c.surface)) {
        Column(Modifier.fillMaxSize()) {
            if (selected == null) {
                BackScreenHeader(onBack = onBack) {
                    Text(
                        I18n.t("local_playlists", "Local playlists"),
                        modifier = Modifier.weight(1f),
                        fontFamily = RoundedHeavy,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        letterSpacing = (-0.8).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconButton(onClick = { createDialog = true }) {
                        Icon(Icons.Rounded.Add, I18n.t("local_pl_new", "New playlist"), tint = c.onSurface)
                    }
                    IconButton(onClick = { exportLauncher.launch("lokotube-playlists.json") }) {
                        Icon(Icons.Rounded.Download, I18n.t("export_subscriptions", "Export"), tint = c.onSurface)
                    }
                    IconButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) {
                        Icon(Icons.Rounded.Upload, I18n.t("import_subscriptions", "Import"), tint = c.onSurface)
                    }
                }
                if (LocalPlaylists.playlists.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            I18n.t("local_pl_empty", "Belum ada playlist lokal. Ketuk + untuk membuat."),
                            color = c.onSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    }
                } else {
                    LazyColumn(contentPadding = contentPadding) {
                        items(LocalPlaylists.playlists, key = { it.id }) { pl ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .pressable(onClick = { selectedId = pl.id })
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(pl.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        I18n.t("local_pl_count", "{n} videos").replace("{n}", pl.videos.size.toString()),
                                        color = c.onSurfaceVariant,
                                        fontSize = 12.sp,
                                    )
                                }
                                IconButton(onClick = { renameTarget = pl }) {
                                    Icon(Icons.Rounded.ContentCopy, I18n.t("local_pl_rename", "Rename"), tint = c.onSurfaceVariant, modifier = Modifier.width(20.dp))
                                }
                                IconButton(onClick = { LocalPlaylists.delete(pl.id); if (selectedId == pl.id) selectedId = null }) {
                                    Icon(Icons.Rounded.Delete, I18n.t("local_pl_delete", "Delete"), tint = c.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            } else {
                BackScreenHeader(onBack = { selectedId = null }) {
                    Text(
                        selected.title,
                        modifier = Modifier.weight(1f),
                        fontFamily = RoundedHeavy,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.7).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (selected.videos.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            I18n.t("local_pl_empty_videos", "Belum ada video. Tekan lama video di beranda lalu pilih \u201cSimpan ke playlist\u201d."),
                            color = c.onSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    }
                } else {
                    LazyColumn(contentPadding = contentPadding) {
                        items(selected.videos, key = { it.id }) { v ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .pressable(onClick = { copyLink(context, v.id) })
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Thumbnail(v.thumbnail, Modifier.width(96.dp).aspectRatio(16f / 9f), radius = 10)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(v.title, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(v.channel, color = c.onSurfaceVariant, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(onClick = { LocalPlaylists.remove(selected.id, v.id) }) {
                                    Icon(Icons.Rounded.Delete, I18n.t("local_pl_remove", "Remove"), tint = c.onSurfaceVariant, modifier = Modifier.width(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (createDialog) {
        PlaylistNameDialog(
            title = I18n.t("local_pl_new_title", "Buat playlist"),
            initial = "",
            onConfirm = { name -> LocalPlaylists.create(name); createDialog = false },
            onDismiss = { createDialog = false },
        )
    }
    renameTarget?.let { target ->
        PlaylistNameDialog(
            title = I18n.t("local_pl_rename_title", "Ganti nama"),
            initial = target.title,
            onConfirm = { name -> LocalPlaylists.rename(target.id, name); renameTarget = null },
            onDismiss = { renameTarget = null },
        )
    }
}

@Composable
private fun PlaylistNameDialog(
    title: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                placeholder = { Text(I18n.t("local_pl_name_hint", "Nama playlist…")) },
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }, enabled = value.isNotBlank()) {
                Text(I18n.t("ok", "OK"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(I18n.t("download_cancel", "Batal")) }
        },
    )
}

private fun copyLink(context: Context, videoId: String) {
    val link = "https://youtu.be/$videoId"
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("LokoTube", link))
    Toast.makeText(context, I18n.t("copied", "Copied"), Toast.LENGTH_SHORT).show()
}