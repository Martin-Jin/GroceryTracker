package com.martin.storage.ui.receipt

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.martin.storage.ui.theme.*
import java.io.File

/** Activity result extras key. */
const val EXTRA_RECEIPT_ITEMS = "receipt_items"

data class ScannedItem(
    val name: String,
    var selected: Boolean = true
)

class ReceiptScannerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VitalityFluxTheme {
                var scannedItems by remember { mutableStateOf<List<ScannedItem>?>(null) }

                if (scannedItems == null) {
                    ReceiptCameraScreen(
                        onCancel = { finish() },
                        onPhotoTaken = { bitmap ->
                            runOcr(bitmap) { lines ->
                                val items = parseReceiptLines(lines)
                                scannedItems = items
                            }
                        }
                    )
                } else {
                    ReceiptSelectionScreen(
                        items = scannedItems!!,
                        onConfirm = { selected ->
                            val json = Gson().toJson(selected.map { it.name })
                            val resultIntent = Intent().putExtra(EXTRA_RECEIPT_ITEMS, json)
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        },
                        onBack = { scannedItems = null }
                    )
                }
            }
        }
    }

    private fun runOcr(bitmap: Bitmap, onResult: (List<String>) -> Unit) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { result ->
                val lines = result.textBlocks
                    .flatMap { it.lines }
                    .sortedWith(compareBy({ it.boundingBox?.top ?: 0 }, { it.boundingBox?.left ?: 0 }))
                    .map { it.text }
                onResult(lines)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
}

// ── Camera screen ─────────────────────────────────────────────────────────────

@Composable
private fun ReceiptCameraScreen(onCancel: () -> Unit, onPhotoTaken: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isCapturing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            imageCapture = capture

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview, capture
                )
            } catch (_: Exception) {}
        }, ContextCompat.getMainExecutor(context))
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Scanning overlay guide
        Box(
            Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.55f)
                .align(Alignment.Center)
                .border(2.dp, PrimaryContainer, RoundedCornerShape(12.dp))
        )

        // Top bar
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, "Cancel", tint = Color.White)
            }
            Text(
                "Point at receipt",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.size(48.dp))
        }

        // Capture button
        Box(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 40.dp)
        ) {
            FilledIconButton(
                onClick = {
                    val capture = imageCapture ?: return@FilledIconButton
                    isCapturing = true
                    val file = File.createTempFile("receipt", ".jpg", context.cacheDir)
                    val options = ImageCapture.OutputFileOptions.Builder(file).build()
                    capture.takePicture(
                        options,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                                onPhotoTaken(bmp)
                                isCapturing = false
                            }
                            override fun onError(exc: ImageCaptureException) { isCapturing = false }
                        }
                    )
                },
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(50.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Primary)
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(Modifier.size(28.dp), color = OnPrimary, strokeWidth = 3.dp)
                } else {
                    Icon(Icons.Default.Camera, "Capture", Modifier.size(32.dp), tint = OnPrimary)
                }
            }
        }
    }
}

// ── Selection screen ──────────────────────────────────────────────────────────

@Composable
private fun ReceiptSelectionScreen(
    items: List<ScannedItem>,
    onConfirm: (List<ScannedItem>) -> Unit,
    onBack: () -> Unit
) {
    val mutableItems = remember(items) { items.map { it.copy() }.toMutableStateList() }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp, color = Surface) {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Primary) }
                    Text("Select Items to Add", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.weight(1f))
                    NutrientChip(
                        "${mutableItems.count { it.selected }} selected",
                        color = PrimaryContainer.copy(.25f),
                        textColor = Primary
                    )
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Surface) {
                Button(
                    onClick = { onConfirm(mutableItems.filter { it.selected }) },
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                    enabled = mutableItems.any { it.selected },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add ${mutableItems.count { it.selected }} Items")
                }
            }
        }
    ) { padding ->
        if (mutableItems.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No grocery items found", style = MaterialTheme.typography.titleMedium, color = OnSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Try rescanning with better lighting", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    Spacer(Modifier.height(20.dp))
                    OutlinedButton(onClick = onBack) { Text("Rescan") }
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    TextButton(
                        onClick = {
                            val allSelected = mutableItems.all { it.selected }
                            mutableItems.indices.forEach { mutableItems[it] = mutableItems[it].copy(selected = !allSelected) }
                        }
                    ) {
                        Text(if (mutableItems.all { it.selected }) "Deselect All" else "Select All", color = Primary)
                    }
                }
                items(mutableItems.indices.toList()) { idx ->
                    val item = mutableItems[idx]
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (item.selected) PrimaryContainer.copy(.15f) else SurfaceContainerLowest)
                            .clickable { mutableItems[idx] = item.copy(selected = !item.selected) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = item.selected,
                            onCheckedChange = { mutableItems[idx] = item.copy(selected = it) },
                            colors = CheckboxDefaults.colors(checkedColor = Primary)
                        )
                        Text(
                            item.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (item.selected) FontWeight.Medium else FontWeight.Normal,
                            color = if (item.selected) OnSurface else OnSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

// ── Simple receipt line parser (no ML, keyword-based) ────────────────────────

private val ignoredTokens = setOf(
    "total", "subtotal", "gst", "tax", "cash", "change", "visa", "eftpos",
    "eftpos", "mastercard", "paywave", "debit", "credit", "receipt", "invoice",
    "date", "time", "thank", "you", "store", "loyalty", "points", "discount",
    "savings", "amount", "qty", "description", "price", "barcode", "rounding"
)

private val knownIngredients = setOf(
    "apple", "banana", "orange", "mango", "avocado", "spinach", "broccoli",
    "carrot", "potato", "kumara", "tomato", "garlic", "onion", "lettuce",
    "chicken", "beef", "salmon", "tuna", "egg", "milk", "cheese", "yogurt",
    "butter", "bread", "oats", "rice", "pasta", "flour", "sugar", "honey",
    "olive oil", "soy sauce", "ketchup", "mustard", "mayo", "cream",
    "frozen", "peas", "berries", "blueberry", "strawberry", "lemon", "lime"
)

fun parseReceiptLines(lines: List<String>): List<ScannedItem> {
    val results = mutableListOf<ScannedItem>()
    for (line in lines) {
        val lower = line.lowercase().trim()
        if (lower.length < 3) continue
        if (ignoredTokens.any { lower.contains(it) }) continue
        // Keep lines that contain a known food keyword
        if (knownIngredients.any { lower.contains(it) }) {
            // Strip price-like tokens (e.g. $3.49, 2.99) and leading numbers
            val cleaned = line
                .replace(Regex("""\$?\d+[\.,]\d{2}\b"""), "")
                .replace(Regex("""^\d+\s*[xX]\s*"""), "")
                .trim()
            if (cleaned.length >= 3) results.add(ScannedItem(cleaned))
        }
    }
    return results.distinctBy { it.name.lowercase() }
}

// ── NutrientChip (re-declared here so this file compiles standalone) ──────────

@Composable
private fun NutrientChip(label: String, color: Color, textColor: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}