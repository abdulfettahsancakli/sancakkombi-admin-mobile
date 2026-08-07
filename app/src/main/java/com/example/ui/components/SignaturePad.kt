package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream

data class Line(
    val path: Path,
    val strokeWidth: Float = 5f
)

fun renderSignatureToBitmap(lines: List<Line>, size: IntSize): Bitmap? {
    if (lines.isEmpty() || size.width <= 0 || size.height <= 0) return null
    val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(AndroidColor.WHITE)
    val paint = AndroidPaint().apply {
        color = AndroidColor.rgb(15, 23, 42)
        style = AndroidPaint.Style.STROKE
        isAntiAlias = true
        strokeCap = AndroidPaint.Cap.ROUND
        strokeJoin = AndroidPaint.Join.ROUND
    }
    lines.forEach { line ->
        paint.strokeWidth = line.strokeWidth
        canvas.drawPath(line.path.asAndroidPath(), paint)
    }
    return bitmap
}

fun saveBitmapToCache(context: Context, bitmap: Bitmap, prefix: String): String {
    val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
    return file.absolutePath
}

@Composable
fun SignaturePad(
    title: String,
    lines: SnapshotStateList<Line>,
    onCanvasSizeChanged: (IntSize) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .testTag("signature_pad_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged(onCanvasSizeChanged)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset: Offset ->
                                val newPath = Path().apply { moveTo(offset.x, offset.y) }
                                lines.add(Line(newPath))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val currentPath = lines.lastOrNull()?.path
                                currentPath?.lineTo(change.position.x, change.position.y)
                            }
                        )
                    }
            ) {
                lines.forEach { line ->
                    drawPath(
                        path = line.path,
                        color = Color(0xFF0F172A),
                        style = Stroke(
                            width = line.strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            if (lines.isEmpty()) {
                Text(
                    text = "Buraya imzalayın",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = { lines.clear() },
                enabled = lines.isNotEmpty(),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Temizle", fontSize = 11.sp)
            }
        }
    }
}
