import SwiftUI

struct ContentView: View {
    @StateObject private var locationManager = LocationPermissionManager()

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                Text("GPS Attendance iOS")
                    .font(.title2)
                    .bold()

                Text(locationManager.statusText)
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.secondary)

                Button("위치 권한 요청") {
                    locationManager.requestWhenInUsePermission()
                }
                .buttonStyle(.borderedProminent)

                Button("현재 위치 갱신") {
                    locationManager.requestSingleLocation()
                }
                .buttonStyle(.bordered)

                if let coordinate = locationManager.lastCoordinate {
                    Text("lat: \(coordinate.latitude), lng: \(coordinate.longitude)")
                        .font(.footnote)
                        .textSelection(.enabled)
                }
            }
            .padding(24)
            .navigationTitle("출퇴근")
        }
    }
}

#Preview {
    ContentView()
}
