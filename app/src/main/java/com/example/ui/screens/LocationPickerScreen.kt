package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.GeoLocation
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
  initialLocation: GeoLocation? = null,
  onBackClick: () -> Unit,
  onLocationSelected: (GeoLocation) -> Unit
) {
  val context = LocalContext.current

  // Egyptian Governorates & Key Cities with GPS Coordinates
  val egyptianGovernorates = listOf(
    "القاهرة" to Pair(30.0444, 31.2357),
    "الجيزة" to Pair(30.0131, 31.2089),
    "الإسكندرية" to Pair(31.2001, 29.9187),
    "المنصورة (الدقهلية)" to Pair(31.0409, 31.3785),
    "طنطا (الغربية)" to Pair(30.7865, 31.0004),
    "بورسعيد" to Pair(31.2653, 32.3019),
    "أسيوط" to Pair(27.1783, 31.1859),
    "سوهاج" to Pair(26.5569, 31.6948)
  )

  // Specialized Egyptian Courts Jurisdictions
  val courtJurisdictions = listOf(
    "محكمة القاهرة الاقتصادية",
    "محكمة استئناف القاهرة",
    "مجلس الدولة (محكمة القضاء الإداري)",
    "محكمة جنوب القاهرة الابتدائية (زينهم)",
    "محكمة شمال القاهرة الابتدائية (العباسية)",
    "محكمة الإسكندرية الاقتصادية",
    "محكمة الأسرة والتركات",
    "محكمة الجيزة الابتدائية"
  )

  // Famous Districts per selected City
  val districtsByGovernorate = mapOf(
    "القاهرة" to listOf("مدينة نصر", "مصر الجديدة", "المعادي", "التجمع الخامس (القاهرة الجديدة)", "وسط البلد", "الزمالك", "شبرا"),
    "الجيزة" to listOf("الدقي", "المهندسين", "الشيخ زايد", "مدينة 6 أكتوبر", "الهرم", "فيصل", "العجوزة"),
    "الإسكندرية" to listOf("سموحة", "محطة الرمل", "سيدي جابر", "ميامي", "لوران", "المنتزه"),
    "المنصورة (الدقهلية)" to listOf("حي المشاية", "حي الجامعة", "توريل", "المنصورة الجديدة"),
    "طنطا (الغربية)" to listOf("شارع البحر", "شارع النحاس", "سبرباي"),
    "بورسعيد" to listOf("حي الشرق", "حي المناخ", "حي العرب", "بورفؤاد"),
    "أسيوط" to listOf("حي غرب أسيوط", "حي شرق", "شارع النميس"),
    "سوهاج" to listOf("شارع الجمهورية", "مدينة سوهاج الجديدة", "حي الكوثر")
  )

  var selectedCity by remember { mutableStateOf(initialLocation?.city ?: "القاهرة") }
  var selectedCourt by remember { mutableStateOf(initialLocation?.courtJurisdiction ?: "محكمة القاهرة الاقتصادية") }
  var selectedDistrict by remember { mutableStateOf(initialLocation?.district ?: "مدينة نصر") }
  var latitude by remember { mutableStateOf(initialLocation?.latitude ?: 30.0444) }
  var longitude by remember { mutableStateOf(initialLocation?.longitude ?: 31.2357) }

  var isGpsActive by remember { mutableStateOf(false) }
  var gpsStatusMessage by remember { mutableStateOf("اضغط لتفعيل تحديد موقعك المباشر في مصر عبر GPS") }
  var permissionGranted by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    )
  }

  // Permission Launcher for Location
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
    val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
    permissionGranted = fineGranted || coarseGranted

    if (permissionGranted) {
      isGpsActive = true
      fetchDeviceLocation(context) { loc ->
        if (loc != null) {
          latitude = loc.latitude
          longitude = loc.longitude
          gpsStatusMessage = "تم التقاط إحداثياتك المباشرة بدقة ✓"

          // Auto-detect nearest city if in Egypt bounds
          if (loc.latitude in 29.5..30.5 && loc.longitude in 31.0..31.6) {
            selectedCity = "القاهرة"
          } else if (loc.latitude in 31.0..31.5 && loc.longitude in 29.7..30.2) {
            selectedCity = "الإسكندرية"
          } else if (loc.latitude in 29.8..30.2 && loc.longitude in 30.7..31.2) {
            selectedCity = "الجيزة"
          }
        } else {
          gpsStatusMessage = "تم تفعيل GPS بنجاح (الموقع التقريبي في جمهورية مصر العربية)"
        }
      }
    } else {
      gpsStatusMessage = "لم يتم منح إذن الموقع - يمكنك الاختيار يدوياً من القائمة"
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
  val pulseRadius by infiniteTransition.animateFloat(
    initialValue = 20f,
    targetValue = 95f,
    animationSpec = infiniteRepeatable(
      animation = tween(2000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radius"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(2000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "alpha"
  )

  val scrollState = rememberScrollState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              "تحديد الموقع والاختصاص القضائي",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              "جمهورية مصر العربية • المحاكم والدوائر القانونية",
              fontSize = 10.sp,
              color = GoldLight
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
      )
    },
    containerColor = CreamBackground
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(scrollState)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

      // 1. Interactive Radar / Map Canvas
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldSecondary.copy(alpha = 0.4f)),
        modifier = Modifier
          .fillMaxWidth()
          .height(230.dp)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          // Canvas rendering simulated radar & Egyptian legal map coordinates
          Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Grid lines
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            for (i in 1..4) {
              val y = size.height * (i / 5f)
              drawLine(
                color = Color(0xFF1E3253).copy(alpha = 0.4f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                pathEffect = dashEffect
              )
            }
            for (i in 1..5) {
              val x = size.width * (i / 6f)
              drawLine(
                color = Color(0xFF1E3253).copy(alpha = 0.4f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                pathEffect = dashEffect
              )
            }

            // Concentric range circles
            drawCircle(
              color = GoldSecondary.copy(alpha = 0.15f),
              radius = 50f,
              center = center,
              style = Stroke(width = 1.5f)
            )
            drawCircle(
              color = GoldSecondary.copy(alpha = 0.25f),
              radius = 110f,
              center = center,
              style = Stroke(width = 1.5f)
            )

            // Animated pulse
            drawCircle(
              color = if (isGpsActive) EmeraldSuccess.copy(alpha = pulseAlpha) else GoldSecondary.copy(alpha = pulseAlpha),
              radius = pulseRadius,
              center = center,
              style = Stroke(width = 2f)
            )

            // Pin marker center dot
            drawCircle(
              color = if (isGpsActive) EmeraldSuccess else GoldSecondary,
              radius = 7f,
              center = center
            )
          }

          // Top coordinates badge
          Surface(
            color = NavyPrimary.copy(alpha = 0.9f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                Icons.Default.GpsFixed,
                contentDescription = null,
                tint = if (isGpsActive) EmeraldSuccess else GoldSecondary,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "${String.format("%.4f", latitude)}° N, ${String.format("%.4f", longitude)}° E",
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Center Pin Icon Overlay
          Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = if (isGpsActive) EmeraldSuccess else CrimsonError,
              modifier = Modifier.size(36.dp)
            )
            Surface(
              color = NavyDark,
              shape = RoundedCornerShape(6.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary)
            ) {
              Text(
                "$selectedCity - $selectedCourt",
                fontSize = 10.sp,
                color = GoldLight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          // GPS Auto-Locate Button inside map
          FloatingActionButton(
            onClick = {
              if (permissionGranted) {
                isGpsActive = true
                fetchDeviceLocation(context) { loc ->
                  if (loc != null) {
                    latitude = loc.latitude
                    longitude = loc.longitude
                    gpsStatusMessage = "تم تحديد موقعك بدقة عبر الأقمار الصناعية"
                  } else {
                    latitude = 30.0444
                    longitude = 31.2357
                    gpsStatusMessage = "تم اعتماد موقع القاهرة (GPS نشط)"
                  }
                }
              } else {
                locationPermissionLauncher.launch(
                  arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                  )
                )
              }
            },
            containerColor = NavySurface,
            contentColor = if (isGpsActive) EmeraldSuccess else GoldSecondary,
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(12.dp)
              .size(46.dp),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.MyLocation, contentDescription = "تفعيل الموقع المباشر GPS")
          }
        }
      }

      // GPS Status & Activation Banner
      Surface(
        color = if (isGpsActive) EmeraldContainer else NavyContainer.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isGpsActive) EmeraldSuccess.copy(alpha = 0.4f) else NavyPrimary.copy(alpha = 0.2f)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            if (!permissionGranted) {
              locationPermissionLauncher.launch(
                arrayOf(
                  Manifest.permission.ACCESS_FINE_LOCATION,
                  Manifest.permission.ACCESS_COARSE_LOCATION
                )
              )
            } else {
              isGpsActive = true
              fetchDeviceLocation(context) { loc ->
                if (loc != null) {
                  latitude = loc.latitude
                  longitude = loc.longitude
                }
              }
            }
          }
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = if (isGpsActive) Icons.Default.GpsFixed else Icons.Default.AddLocationAlt,
            contentDescription = null,
            tint = if (isGpsActive) EmeraldSuccess else NavyPrimary,
            modifier = Modifier.size(22.dp)
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isGpsActive) "الموقع الجغرافي نشط ومفعل ✓" else "تفعيل الموقع الجغرافي المباشر (GPS)",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = if (isGpsActive) Color(0xFF065F46) else TextPrimary
            )
            Text(
              text = gpsStatusMessage,
              fontSize = 11.sp,
              color = if (isGpsActive) Color(0xFF047857) else TextSecondary
            )
          }
          Button(
            onClick = {
              locationPermissionLauncher.launch(
                arrayOf(
                  Manifest.permission.ACCESS_FINE_LOCATION,
                  Manifest.permission.ACCESS_COARSE_LOCATION
                )
              )
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isGpsActive) EmeraldSuccess else NavyPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(
              if (isGpsActive) "تحديث" else "تفعيل الآن",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }

      // City / Governorate Selection
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("المحافظة / المدينة في مصر:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
            Surface(color = GoldContainer, shape = RoundedCornerShape(6.dp)) {
              Text("جمهورية مصر العربية", fontSize = 10.sp, color = GoldDark, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
          }

          // Row 1: Cairo, Giza, Alex
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            egyptianGovernorates.take(3).forEach { (cityName, coords) ->
              val isSelected = selectedCity == cityName
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .clickable {
                    selectedCity = cityName
                    latitude = coords.first
                    longitude = coords.second
                    val defaultDistrict = districtsByGovernorate[cityName]?.firstOrNull() ?: "وسط البلد"
                    selectedDistrict = defaultDistrict
                  }
                  .border(
                    1.dp,
                    if (isSelected) NavyPrimary else BorderSubtle,
                    RoundedCornerShape(10.dp)
                  ),
                color = if (isSelected) NavyPrimary else CreamSurfaceVariant
              ) {
                Text(
                  cityName,
                  modifier = Modifier.padding(vertical = 10.dp),
                  textAlign = TextAlign.Center,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else TextPrimary
                )
              }
            }
          }

          // Row 2: Mansoura, Tanta, Port Said
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            egyptianGovernorates.drop(3).take(3).forEach { (cityName, coords) ->
              val isSelected = selectedCity == cityName
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .clickable {
                    selectedCity = cityName
                    latitude = coords.first
                    longitude = coords.second
                    val defaultDistrict = districtsByGovernorate[cityName]?.firstOrNull() ?: "وسط المدينة"
                    selectedDistrict = defaultDistrict
                  }
                  .border(
                    1.dp,
                    if (isSelected) NavyPrimary else BorderSubtle,
                    RoundedCornerShape(10.dp)
                  ),
                color = if (isSelected) NavyPrimary else CreamSurfaceVariant
              ) {
                Text(
                  cityName,
                  modifier = Modifier.padding(vertical = 10.dp),
                  textAlign = TextAlign.Center,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else TextPrimary
                )
              }
            }
          }

          // Row 3: Assiut, Sohag
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            egyptianGovernorates.drop(6).forEach { (cityName, coords) ->
              val isSelected = selectedCity == cityName
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .clickable {
                    selectedCity = cityName
                    latitude = coords.first
                    longitude = coords.second
                    val defaultDistrict = districtsByGovernorate[cityName]?.firstOrNull() ?: "المدينة"
                    selectedDistrict = defaultDistrict
                  }
                  .border(
                    1.dp,
                    if (isSelected) NavyPrimary else BorderSubtle,
                    RoundedCornerShape(10.dp)
                  ),
                color = if (isSelected) NavyPrimary else CreamSurfaceVariant
              ) {
                Text(
                  cityName,
                  modifier = Modifier.padding(vertical = 10.dp),
                  textAlign = TextAlign.Center,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else TextPrimary
                )
              }
            }
          }
        }
      }

      // Court Jurisdiction Selection (Egyptian Judiciary)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text("المحكمة ذات الاختصاص المكاني والنوعي في مصر:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)

          courtJurisdictions.forEach { court ->
            val isSelected = selectedCourt == court
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { selectedCourt = court }
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) GoldDark else BorderSubtle,
                  shape = RoundedCornerShape(10.dp)
                ),
              color = if (isSelected) GoldContainer.copy(alpha = 0.5f) else CreamSurfaceVariant
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Gavel,
                  contentDescription = null,
                  tint = if (isSelected) GoldDark else TextMuted,
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = court,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) TextPrimary else TextSecondary,
                  modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                }
              }
            }
          }
        }
      }

      // District Selection (Egyptian Districts)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text("المنطقة / الحي / الشارع:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)

          // Quick District chips for chosen governorate
          val availableDistricts = districtsByGovernorate[selectedCity] ?: listOf("وسط البلد", "الحي الرئيسي")
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            availableDistricts.take(3).forEach { dist ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (selectedDistrict == dist) NavyPrimary else CreamSurfaceVariant,
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { selectedDistrict = dist }
              ) {
                Text(
                  text = dist,
                  fontSize = 11.sp,
                  textAlign = TextAlign.Center,
                  color = if (selectedDistrict == dist) Color.White else TextPrimary,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                )
              }
            }
          }

          OutlinedTextField(
            value = selectedDistrict,
            onValueChange = { selectedDistrict = it },
            label = { Text("اسم الحي أو مقر الشركة/المكتب بالتفصيل") },
            leadingIcon = { Icon(Icons.Default.HomeWork, contentDescription = null, tint = GoldDark) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
          )
        }
      }

      // Selected Location Preview Summary
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = NavyContainer.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(NavyPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Place, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(20.dp))
          }

          Column(modifier = Modifier.weight(1f)) {
            Text("نطاق الاختصاص القضائي المعتمد:", fontSize = 11.sp, color = TextMuted)
            Text(
              "$selectedCity - $selectedDistrict (جمهورية مصر العربية)",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = TextPrimary
            )
            Text(
              "المحكمة المختصة: $selectedCourt",
              fontSize = 11.sp,
              color = NavyPrimary,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      // Confirm Location Button
      Button(
        onClick = {
          val loc = GeoLocation(
            city = selectedCity,
            district = selectedDistrict,
            courtJurisdiction = selectedCourt,
            latitude = latitude,
            longitude = longitude,
            fullAddress = "$selectedCity - $selectedDistrict - نطاق $selectedCourt (مصر)"
          )
          onLocationSelected(loc)
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
      ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          "تأكيد واعتماد الموقع والاختصاص القضائي",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = Color.White
        )
      }
    }
  }
}

// Helper to fetch device coordinates safely
private fun fetchDeviceLocation(context: Context, onResult: (Location?) -> Unit) {
  val finePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
  val coarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

  if (finePermission != PackageManager.PERMISSION_GRANTED && coarsePermission != PackageManager.PERMISSION_GRANTED) {
    onResult(null)
    return
  }

  try {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (locationManager == null) {
      onResult(null)
      return
    }

    val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    val passiveLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

    val bestLocation = gpsLocation ?: networkLocation ?: passiveLocation
    onResult(bestLocation)
  } catch (e: Exception) {
    onResult(null)
  }
}
