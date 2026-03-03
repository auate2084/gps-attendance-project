import CoreLocation
import Foundation

final class LocationPermissionManager: NSObject, ObservableObject {
    @Published var statusText: String = "아직 위치 권한을 요청하지 않았습니다."
    @Published var lastCoordinate: CLLocationCoordinate2D?

    private let manager = CLLocationManager()

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
        syncStatusText()
    }

    func requestWhenInUsePermission() {
        manager.requestWhenInUseAuthorization()
    }

    func requestSingleLocation() {
        manager.requestLocation()
    }

    private func syncStatusText() {
        switch manager.authorizationStatus {
        case .notDetermined:
            statusText = "권한 미요청"
        case .restricted:
            statusText = "제한됨"
        case .denied:
            statusText = "거부됨"
        case .authorizedWhenInUse:
            statusText = "앱 사용 중 허용"
        case .authorizedAlways:
            statusText = "항상 허용"
        @unknown default:
            statusText = "알 수 없음"
        }
    }
}

extension LocationPermissionManager: CLLocationManagerDelegate {
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        syncStatusText()
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        lastCoordinate = locations.last?.coordinate
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        statusText = "위치 가져오기 실패: \(error.localizedDescription)"
    }
}
