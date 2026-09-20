package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatWorkspaceScreen(
  request: ServiceRequest,
  messages: List<ChatMessage>,
  currentUser: UserProfile,
  typingUsers: List<String> = emptyList(),
  isCloudSyncActive: Boolean = false,
  onTypingChanged: (Boolean) -> Unit = {},
  onBackClick: () -> Unit,
  onSendMessage: (text: String, attachment: String?) -> Unit,
  onReleaseEscrow: () -> Unit,
  onOpenDispute: () -> Unit
) {
  var inputMessage by remember { mutableStateOf("") }
  var attachedFileName by remember { mutableStateOf<String?>(null) }
  var searchQuery by remember { mutableStateOf("") }
  var isSearchActive by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  val takePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
    if (bitmap != null) {
      attachedFileName = "صورة_مستند_كاميرا.jpg"
    }
  }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      takePhotoLauncher.launch(null)
    }
  }

  // Handle typing debounce to Firestore presence
  LaunchedEffect(inputMessage) {
    if (inputMessage.isNotBlank()) {
      onTypingChanged(true)
      delay(2500)
      onTypingChanged(false)
    } else {
      onTypingChanged(false)
    }
  }

  val filteredMessages = remember(messages, searchQuery) {
    if (searchQuery.isBlank()) messages
    else messages.filter { it.text.contains(searchQuery, ignoreCase = true) || it.senderName.contains(searchQuery, ignoreCase = true) }
  }

  LaunchedEffect(filteredMessages.size) {
    if (filteredMessages.isNotEmpty()) {
      listState.animateScrollToItem(filteredMessages.size - 1)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          if (isSearchActive) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text("بحث في رسائل القضية...", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f)) },
              modifier = Modifier.fillMaxWidth().testTag("chat_search_field"),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = GoldSecondary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
              )
            )
          } else {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "مساحة العمل القانونية",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Surface(
                  color = if (isCloudSyncActive) EmeraldSuccess else GoldDark,
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                  ) {
                    Icon(
                      imageVector = if (isCloudSyncActive) Icons.Default.CloudDone else Icons.Default.Bolt,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(10.dp)
                    )
                    Text(
                      text = if (isCloudSyncActive) "Firestore Live" else "مباشر",
                      fontSize = 8.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }
                }
              }
              Text(
                text = request.title,
                fontSize = 11.sp,
                color = GoldLight,
                maxLines = 1
              )
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = {
            if (isSearchActive) {
              isSearchActive = false
              searchQuery = ""
            } else {
              onBackClick()
            }
          }) {
            Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Color.White)
          }
        },
        actions = {
          IconButton(onClick = { isSearchActive = !isSearchActive }) {
            Icon(
              imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
              contentDescription = "بحث",
              tint = Color.White
            )
          }
          if (request.status == RequestStatus.IN_PROGRESS && currentUser.role == UserRole.CLIENT) {
            IconButton(onClick = onReleaseEscrow) {
              Icon(Icons.Default.CheckCircle, contentDescription = "تحرير الأتعاب", tint = EmeraldSuccess)
            }
          }
          IconButton(onClick = onOpenDispute) {
            Icon(Icons.Default.ReportProblem, contentDescription = "نزاع", tint = CrimsonError)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
      )
    },
    containerColor = MaterialTheme.adaptiveBackground,
    bottomBar = {
      // Input Bar
      Surface(
        color = MaterialTheme.adaptiveSurface,
        shadowElevation = 8.dp,
        modifier = Modifier
          .fillMaxWidth()
          .imePadding()
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          // Active typing indicator
          AnimatedVisibility(
            visible = typingUsers.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp, start = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(13.dp))
              Text(
                text = "${typingUsers.joinToString("، ")} يكتب الآن...",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldSuccess
              )
            }
          }

          if (attachedFileName != null) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
                .background(GoldContainer, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.AttachFile, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
                Text(text = attachedFileName!!, fontSize = 12.sp, color = GoldOnContainer, fontWeight = FontWeight.Bold)
              }
              IconButton(onClick = { attachedFileName = null }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Close, contentDescription = "إلغاء المرفق", tint = GoldDark, modifier = Modifier.size(14.dp))
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            IconButton(
              onClick = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
              },
              modifier = Modifier.testTag("chat_camera_button")
            ) {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "تصوير مستند بالكاميرا",
                tint = NavyPrimary
              )
            }

            IconButton(
              onClick = {
                attachedFileName = if (attachedFileName == null) "مستند_قضائي_إضافي.pdf" else null
              },
              modifier = Modifier.testTag("chat_attach_button")
            ) {
              Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = "إرفاق مستند",
                tint = if (attachedFileName != null) GoldDark else MaterialTheme.adaptiveTextSecondary
              )
            }

            IconButton(
              onClick = {
                attachedFileName = if (attachedFileName == null) "تسجيل_صوتي_استشارة.m4a" else null
              },
              modifier = Modifier.testTag("chat_audio_button")
            ) {
              Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "تسجيل ملاحظة صوتية",
                tint = if (attachedFileName?.endsWith(".m4a") == true) CrimsonError else MaterialTheme.adaptiveTextSecondary
              )
            }

            OutlinedTextField(
              value = inputMessage,
              onValueChange = { inputMessage = it },
              placeholder = { Text("اكتب رسالتك في مساحة العمل...", fontSize = 13.sp) },
              modifier = Modifier.weight(1f).testTag("chat_message_input"),
              shape = RoundedCornerShape(20.dp),
              maxLines = 3,
              colors = maitreTextFieldColors()
            )

            IconButton(
              onClick = {
                if (inputMessage.isNotBlank() || attachedFileName != null) {
                  onSendMessage(inputMessage, attachedFileName)
                  inputMessage = ""
                  attachedFileName = null
                  onTypingChanged(false)
                }
              },
              colors = IconButtonDefaults.iconButtonColors(
                containerColor = NavyPrimary,
                contentColor = Color.White
              ),
              modifier = Modifier.size(44.dp).testTag("chat_send_button")
            ) {
              Icon(Icons.Default.Send, contentDescription = "إرسال", modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      // Escrow Protection & Firestore Status Header Banner
      Surface(
        color = NavyContainer,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
            Text(
              text = "محفظة الضمان المالي مفعلة لحماية أتعاب الطرفين",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Surface(
            color = EmeraldSuccess,
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = "100% مضمون",
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
          }
        }
      }

      // Quick Consultation Suggestions
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(CreamSurfaceVariant)
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        SuggestionChip(
          onClick = { inputMessage = "هل يمكن تزويدي بالصيغة المحدثة مع إضافة التعديلات؟" },
          label = { Text("طلب التعديلات", fontSize = 10.sp) }
        )
        SuggestionChip(
          onClick = { inputMessage = "ما هي الخطوة القانونية القادمة أمام المحكمة المختصة؟" },
          label = { Text("الخطوة التالية", fontSize = 10.sp) }
        )
        SuggestionChip(
          onClick = { inputMessage = "اطلعت على المسودة وهي معتمدة وممتازة." },
          label = { Text("اعتماد المسودة", fontSize = 10.sp) }
        )
      }

      // Messages List
      LazyColumn(
        state = listState,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .testTag("chat_messages_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(filteredMessages, key = { it.id }) { msg ->
          MessageBubble(message = msg, currentUserId = currentUser.id)
        }
      }
    }
  }
}

@Composable
fun MessageBubble(message: ChatMessage, currentUserId: String) {
  if (message.isSystemMessage) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      contentAlignment = Alignment.Center
    ) {
      Surface(
        color = GoldContainer,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
          Text(
            text = message.text,
            color = GoldOnContainer,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
    return
  }

  val isMe = message.senderId == currentUserId

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
      Icon(
        imageVector = when (message.senderRole) {
          UserRole.CLIENT -> Icons.Default.Person
          UserRole.LAWYER -> Icons.Default.Gavel
          UserRole.ADMIN -> Icons.Default.Shield
        },
        contentDescription = null,
        tint = when (message.senderRole) {
          UserRole.CLIENT -> GoldDark
          UserRole.LAWYER -> EmeraldSuccess
          UserRole.ADMIN -> CrimsonError
        },
        modifier = Modifier.size(12.dp)
      )
      Text(
        text = "${message.senderName} (${message.senderRole.labelAr})",
        color = TextMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
      )
    }

    Surface(
      color = if (isMe) NavyPrimary else CreamSurface,
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isMe) 16.dp else 2.dp,
        bottomEnd = if (isMe) 2.dp else 16.dp
      ),
      border = if (isMe) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
      shadowElevation = 1.dp,
      modifier = Modifier.widthIn(max = 290.dp)
    ) {
      Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (message.text.isNotBlank()) {
          Text(
            text = message.text,
            color = if (isMe) Color.White else TextPrimary,
            fontSize = 13.sp,
            lineHeight = 18.sp
          )
        }

        if (message.attachmentName != null) {
          val isAudio = message.attachmentName.endsWith(".m4a") || message.attachmentName.endsWith(".mp3")
          Surface(
            color = if (isMe) NavyContainer else GoldContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = if (isAudio) Icons.Default.AudioFile else Icons.Default.PictureAsPdf,
                contentDescription = "مستند",
                tint = if (isMe) GoldLight else if (isAudio) EmeraldSuccess else CrimsonError,
                modifier = Modifier.size(22.dp)
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = message.attachmentName,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isMe) Color.White else TextPrimary,
                  maxLines = 1
                )
                Text(
                  text = if (isAudio) "تسجيل صوتي استشاري • مشفر" else "مستند قانوني معتمد • PDF",
                  fontSize = 9.sp,
                  color = if (isMe) Color.White.copy(alpha = 0.7f) else TextMuted
                )
              }
              Icon(
                imageVector = if (isAudio) Icons.Default.PlayArrow else Icons.Default.Download,
                contentDescription = "فتح",
                tint = if (isMe) GoldLight else GoldDark,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        Row(
          modifier = Modifier.align(Alignment.End),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = message.timestamp,
            color = if (isMe) Color.White.copy(alpha = 0.65f) else TextMuted,
            fontSize = 9.sp
          )
          if (isMe) {
            Icon(
              imageVector = Icons.Default.DoneAll,
              contentDescription = "تم التسليم",
              tint = GoldLight,
              modifier = Modifier.size(13.dp)
            )
          }
        }
      }
    }
  }
}
