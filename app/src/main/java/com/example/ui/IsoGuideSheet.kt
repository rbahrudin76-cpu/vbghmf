package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.AshMuted
import com.example.ui.theme.BasaltSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ElevatedBasalt
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.ParchmentText
import com.example.ui.theme.WarmGoldBright

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IsoGuideSheet(
    onDismiss: () -> Unit,
    onPickIsoClick: () -> Unit
) {
    val context = LocalContext.current
    val targetTitle = stringResource(id = R.string.target_iso_title)
    val targetUrl = stringResource(id = R.string.target_iso_url)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BasaltSurface,
        contentColor = ParchmentText
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Panduan Menyiapkan File ISO",
                style = MaterialTheme.typography.titleLarge,
                color = ParchmentText
            )

            Text(
                text = "Aplikasi ini tidak menyertakan file game di dalam APK awal agar ukurannya tetap ringan (10–40 MB). Anda cukup mengunduh cadangan file ISO sendiri lalu mengimpornya 1 kali ke dalam vault internal aplikasi.",
                style = MaterialTheme.typography.bodyMedium,
                color = AshMuted
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ElevatedBasalt,
                border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Rekomendasi Rilis PSP",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmGoldBright
                    )
                    Text(
                        text = targetTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = ParchmentText
                    )
                    Text(
                        text = "Format hasil ekstrak: .iso atau .cso (~500–600 MB)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AshMuted
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                runCatching { context.startActivity(intent) }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("open_iso_url_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = null,
                                tint = WarmGoldBright,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka Tautan", color = WarmGoldBright)
                        }

                        OutlinedButton(
                            onClick = {
                                val query = Uri.encode("Assassins Creed Bloodlines Europe PSP ISO")
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/search?q=$query")
                                )
                                runCatching { context.startActivity(intent) }
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("search_iso_web_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = ParchmentText,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cari Web", color = ParchmentText)
                        }
                    }
                }
            }

            StepItem(
                number = "1",
                title = "Unduh & Ekstrak Arsip",
                detail = "Jika file yang diunduh berbentuk .zip atau .7z, ekstrak terlebih dahulu memakai pengelola file hingga muncul file berakhiran .iso atau .cso."
            )
            StepItem(
                number = "2",
                title = "Import Langsung ke Aplikasi",
                detail = "Ketuk tombol Import di bawah dan pilih file .iso tersebut. Aplikasi akan menyalinnya ke penyimpanan privat internal (ukuran aplikasi akan menjadi ~500–600 MB)."
            )
            StepItem(
                number = "3",
                title = "Siap Main Otomatis Landscape",
                detail = "File ISO tersimpan permanen di aplikasi—Anda tidak perlu memilih folder lagi. Atur Anti-Lag lalu ketuk Main Sekarang."
            )

            Button(
                onClick = {
                    onDismiss()
                    onPickIsoClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("guide_import_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
            ) {
                Icon(
                    imageVector = Icons.Rounded.UploadFile,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pilih File .ISO / .CSO Sekarang", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StepItem(number: String, title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = GoldContainer,
            border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.5f)),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelLarge,
                    color = WarmGoldBright
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ParchmentText
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = AshMuted
            )
        }
    }
}
