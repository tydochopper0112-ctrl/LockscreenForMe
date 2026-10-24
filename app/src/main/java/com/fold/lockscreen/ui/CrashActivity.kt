package com.fold.lockscreen.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fold.lockscreen.ui.theme.CyanAccent
import com.fold.lockscreen.ui.theme.DarkBackground
import com.fold.lockscreen.ui.theme.DarkSurface
import com.fold.lockscreen.ui.theme.LockscreenTheme
import com.fold.lockscreen.ui.theme.PureWhite

class CrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE) ?: "Không xác định được lỗi cụ thể."
        val stackTrace = intent.getStringExtra(EXTRA_STACK_TRACE) ?: "Không có nhật ký Stack Trace."

        setContent {
            LockscreenTheme {
                CrashScreen(
                    errorMessage = errorMessage,
                    stackTrace = stackTrace,
                    onCopyClicked = {
                        val fullReport = """
                            |=== LOCKSCREEN FOR ME CRASH REPORT ===
                            |Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})
                            |Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
                            |Error: $errorMessage
                            |
                            |=== STACK TRACE ===
                            |$stackTrace
                        """.trimMargin()

                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Crash Report", fullReport)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(this, "Đã sao chép nội dung lỗi vào bộ nhớ tạm!", Toast.LENGTH_SHORT).show()
                    },
                    onRestartClicked = {
                        val restartIntent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        startActivity(restartIntent)
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_ERROR_MESSAGE = "extra_error_message"
        const val EXTRA_STACK_TRACE = "extra_stack_trace"
    }
}

@Composable
fun CrashScreen(
    errorMessage: String,
    stackTrace: String,
    onCopyClicked: () -> Unit,
    onRestartClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            Surface(
                color = DarkSurface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopyClicked,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sao chép lỗi", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onRestartClicked,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Khởi động lại", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(verticalScroll)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFFF1744),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Ứng dụng gặp sự cố (Crash Handler)",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Samsung Galaxy Z Fold 7 • Android ${Build.VERSION.SDK_INT}",
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1214)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF1744))
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Thông điệp lỗi:",
                        color = Color(0xFFFF8A80),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage,
                        color = PureWhite,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Chi tiết Stack Trace:",
                color = CyanAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Scrollable Code Box for Stack Trace
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF14151A))
                    .border(1.dp, Color(0xFF333842), RoundedCornerShape(10.dp))
                    .padding(12.dp)
                    .horizontalScroll(horizontalScroll)
            ) {
                Text(
                    text = stackTrace,
                    color = Color(0xFFE0E0E0),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
