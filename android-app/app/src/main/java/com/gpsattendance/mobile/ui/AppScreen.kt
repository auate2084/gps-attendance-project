package com.gpsattendance.mobile.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gpsattendance.mobile.GpsAttendanceApp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelTextBuilder
import com.kakao.vectormap.label.LabelTextStyle

@Composable
fun AppScreen(
    sessionViewModel: SessionViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val mainState by mainViewModel.uiState.collectAsStateWithLifecycle()

    if (sessionState.isLoggedIn) {
        LaunchedEffect(Unit) {
            mainViewModel.refreshVisibleSessions()
        }

        MapHomeContent(
            userName = sessionState.userName,
            state = mainState,
            onLogout = sessionViewModel::logout,
            onRefresh = mainViewModel::refreshVisibleSessions,
            onUpdateLocation = mainViewModel::updateLocation,
            onMyLocationDetected = mainViewModel::setMyLocation
        )
    } else {
        LoginContent(
            state = sessionState,
            onLogin = sessionViewModel::login,
            onRegister = sessionViewModel::register,
            onRefreshTeams = sessionViewModel::loadTeams
        )
    }
}

@Composable
private fun LoginContent(
    state: SessionUiState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String, Long) -> Unit,
    onRefreshTeams: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var loginId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedTeamId by remember { mutableStateOf<Long?>(null) }
    var teamMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.registrationCompleted) {
        if (state.registrationCompleted) {
            isRegisterMode = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("GPS Attendance", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = loginId,
            onValueChange = { loginId = it },
            label = { Text("Login ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        if (isRegisterMode) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { teamMenuExpanded = true },
                    enabled = !state.isTeamsLoading && state.teams.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val teamName = state.teams.firstOrNull { it.id == selectedTeamId }?.name
                    Text(teamName ?: if (state.isTeamsLoading) "Loading teams..." else "Select team")
                }
                DropdownMenu(
                    expanded = teamMenuExpanded,
                    onDismissRequest = { teamMenuExpanded = false }
                ) {
                    state.teams.forEach { team ->
                        DropdownMenuItem(
                            text = { Text(team.name) },
                            onClick = {
                                selectedTeamId = team.id
                                teamMenuExpanded = false
                            }
                        )
                    }
                }
            }

            state.teamsError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRefreshTeams, enabled = !state.isTeamsLoading) {
                    Text("Retry team load")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (isRegisterMode) {
                    onRegister(
                        loginId.trim(),
                        email.trim(),
                        password,
                        name.trim(),
                        selectedTeamId ?: return@Button
                    )
                } else {
                    onLogin(loginId.trim(), password)
                }
            },
            enabled = !state.isLoading && loginId.isNotBlank() && password.isNotBlank() &&
                (!isRegisterMode || (name.isNotBlank() &&
                    email.isNotBlank() &&
                    password.length >= 8 &&
                    selectedTeamId != null &&
                    state.teams.isNotEmpty())),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegisterMode) "Sign up" else "Login")
        }

        TextButton(
            onClick = {
                isRegisterMode = !isRegisterMode
                if (isRegisterMode) onRefreshTeams()
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegisterMode) "Back to login" else "Create account")
        }

        state.infoMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        state.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun MapHomeContent(
    userName: String?,
    state: MainUiState,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onUpdateLocation: (Double, Double) -> Unit,
    onMyLocationDetected: (Double, Double) -> Unit
) {
    if (!GpsAttendanceApp.isKakaoMapAvailable) {
        UnsupportedMapContent(userName = userName, state = state, onLogout = onLogout, onRefresh = onRefresh)
        return
    }

    val context = LocalContext.current
    val mapView = rememberKakaoMapViewWithLifecycle()
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var mapError by remember { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.MAP) }
    val hasLocationPermission = hasFineLocationPermission(context)
    var requestedLocationPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchCurrentLocation(context) { lat, lng ->
                onMyLocationDetected(lat, lng)
                onUpdateLocation(lat, lng)
            }
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fetchCurrentLocation(context) { lat, lng ->
                onMyLocationDetected(lat, lng)
                onUpdateLocation(lat, lng)
            }
        } else if (!requestedLocationPermission) {
            requestedLocationPermission = true
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    DisposableEffect(mapView) {
        mapView.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() = Unit
                override fun onMapError(exception: Exception) {
                    mapError = exception.message ?: "Failed to initialize Kakao map"
                }
            },
            object : KakaoMapReadyCallback() {
                override fun onMapReady(map: KakaoMap) {
                    kakaoMap = map
                    mapError = null
                }

                override fun getPosition(): LatLng {
                    return LatLng.from(37.5665, 126.9780)
                }

                override fun getZoomLevel(): Int {
                    return 12
                }
            }
        )
        onDispose {
            mapView.finish()
            kakaoMap = null
        }
    }

    LaunchedEffect(state.myLatitude, state.myLongitude, kakaoMap) {
        val lat = state.myLatitude ?: return@LaunchedEffect
        val lng = state.myLongitude ?: return@LaunchedEffect
        kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(lat, lng), 15))
    }

    LaunchedEffect(kakaoMap, state.memberPins, state.myLatitude, state.myLongitude) {
        val map = kakaoMap ?: return@LaunchedEffect
        runCatching {
            renderMemberLabels(
                map = map,
                members = state.memberPins,
                myLatitude = state.myLatitude,
                myLongitude = state.myLongitude
            )
        }.onFailure {
            mapError = it.message ?: "Failed to render map labels"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == HomeTab.MAP) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { mapView }
                    )

                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Welcome, ${userName ?: "User"}", style = MaterialTheme.typography.titleMedium)
                            Text("Visible members: ${state.visibleMembers.size}")

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    if (hasFineLocationPermission(context)) {
                                        fetchCurrentLocation(context) { lat, lng ->
                                            onMyLocationDetected(lat, lng)
                                            onUpdateLocation(lat, lng)
                                        }
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                }) {
                                    Text("Update my location")
                                }

                                Button(onClick = onRefresh) {
                                    Text("Refresh")
                                }

                                Button(onClick = onLogout) {
                                    Text("Logout")
                                }
                            }

                            if (state.isLoading) {
                                CircularProgressIndicator()
                            }

                            state.trackingMessage?.let { Text(it) }
                            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            mapError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            } else {
                MemberListContent(
                    members = state.visibleMembers,
                    isLoading = state.isLoading
                )
            }
        }

        NavigationBar {
            NavigationBarItem(
                selected = selectedTab == HomeTab.MAP,
                onClick = { selectedTab = HomeTab.MAP },
                icon = { Text("지도") },
                label = { Text("Map") }
            )
            NavigationBarItem(
                selected = selectedTab == HomeTab.MEMBERS,
                onClick = { selectedTab = HomeTab.MEMBERS },
                icon = { Text("목록") },
                label = { Text("Members") }
            )
        }
    }
}

private enum class HomeTab {
    MAP,
    MEMBERS
}

@Composable
private fun MemberListContent(
    members: List<VisibleMember>,
    isLoading: Boolean
) {
    if (isLoading && members.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (members.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("표시 가능한 팀원 데이터가 없습니다.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(members, key = { it.userId }) { member ->
            val inRange = member.isInRange
            val cardColor = if (inRange) Color(0xFFE8F5E9) else Color(0xFFE0E0E0)
            val statusText = when {
                member.isCheckedIn && inRange -> "근무중 (반경 내)"
                member.isCheckedIn -> "근무중 (반경 밖)"
                else -> "퇴근"
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(member.userName, style = MaterialTheme.typography.titleMedium)
                    Text(statusText)
                    Text("Session status: ${member.status}", style = MaterialTheme.typography.bodySmall)
                    if (member.latitude != null && member.longitude != null) {
                        Text(
                            "위치: %.5f, %.5f".format(member.latitude, member.longitude),
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text("위치 정보 없음", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun UnsupportedMapContent(
    userName: String?,
    state: MainUiState,
    onLogout: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Welcome, ${userName ?: "User"}", style = MaterialTheme.typography.titleMedium)
        Text(
            "현재 에뮬레이터 ABI(x86/x86_64)는 Kakao Map SDK를 지원하지 않아 지도가 비활성화되었습니다.",
            color = MaterialTheme.colorScheme.error
        )
        Text("ARM64 에뮬레이터 또는 실제 안드로이드 기기에서 실행해 주세요.")
        Text("Visible members: ${state.visibleMembers.size}")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRefresh) { Text("Refresh team") }
            Button(onClick = onLogout) { Text("Logout") }
        }

        if (state.isLoading) {
            CircularProgressIndicator()
        }

        state.trackingMessage?.let { Text(it) }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        MemberListContent(members = state.visibleMembers, isLoading = state.isLoading)
    }
}

private fun renderMemberLabels(
    map: KakaoMap,
    members: List<TeamMemberPin>,
    myLatitude: Double?,
    myLongitude: Double?
) {
    val layer = map.labelManager?.layer ?: return
    layer.removeAll()

    members.forEach { member ->
        val labelColor = if (member.isInRange) AndroidColor.parseColor("#2E7D32") else AndroidColor.parseColor("#616161")
        val text = if (member.isInRange) "${member.userName} ●" else "${member.userName} ○"
        val options = LabelOptions.from(LatLng.from(member.latitude, member.longitude))
            .setStyles(
                LabelStyle.from(
                    LabelTextStyle.from(28, labelColor, 4, AndroidColor.WHITE)
                )
            )
            .setTexts(LabelTextBuilder().setTexts(text))
        layer.addLabel(options)
    }

    if (myLatitude != null && myLongitude != null) {
        val myOptions = LabelOptions.from(LatLng.from(myLatitude, myLongitude))
            .setStyles(
                LabelStyle.from(
                    LabelTextStyle.from(30, AndroidColor.parseColor("#1565C0"), 4, AndroidColor.WHITE)
                )
            )
            .setTexts(LabelTextBuilder().setTexts("나"))
        layer.addLabel(myOptions)
    }
}

private fun hasFineLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
private fun fetchCurrentLocation(context: Context, onLocation: (Double, Double) -> Unit) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) onLocation(location.latitude, location.longitude)
        }
}

@Composable
private fun rememberKakaoMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                mapView.resume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.pause()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return mapView
}
