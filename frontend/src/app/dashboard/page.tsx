'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { attendanceApi, AttendanceStatusResponse } from '@/lib/attendance';
import { workplaceApi, WorkplaceResponse } from '@/lib/workplace';

export default function DashboardPage() {
  const router = useRouter();
  const [userName, setUserName] = useState('');
  const [status, setStatus] = useState<AttendanceStatusResponse | null>(null);
  const [workplace, setWorkplace] = useState<WorkplaceResponse | null>(null);
  const [currentLocation, setCurrentLocation] = useState<{ lat: number; lng: number } | null>(null);
  const [distance, setDistance] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const name = localStorage.getItem('userName');
    if (name) setUserName(name);
    
    loadData();
    startLocationTracking();
  }, []);

  const loadData = async () => {
    try {
      const [statusData, workplaceData] = await Promise.all([
        attendanceApi.getStatus(),
        workplaceApi.get().catch(() => null),
      ]);
      setStatus(statusData);
      setWorkplace(workplaceData);
    } catch (err: any) {
      console.error('데이터 로드 실패:', err);
    }
  };

  const startLocationTracking = () => {
    if ('geolocation' in navigator) {
      navigator.geolocation.watchPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          setCurrentLocation({ lat: latitude, lng: longitude });
        },
        (error) => {
          console.error('위치 추적 오류:', error);
          setError('위치 정보를 가져올 수 없습니다. 위치 권한을 허용해주세요.');
        },
        { enableHighAccuracy: true, maximumAge: 10000 }
      );
    }
  };

  useEffect(() => {
    if (currentLocation && workplace) {
      const dist = calculateDistance(
        currentLocation.lat,
        currentLocation.lng,
        workplace.latitude,
        workplace.longitude
      );
      setDistance(dist);
    }
  }, [currentLocation, workplace]);

  const calculateDistance = (lat1: number, lon1: number, lat2: number, lon2: number) => {
    const R = 6371000; // 지구 반지름 (미터)
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  };

  const handleCheckin = async () => {
    if (!currentLocation) {
      setError('위치 정보를 가져오는 중입니다...');
      return;
    }

    setLoading(true);
    setError('');

    try {
      await attendanceApi.checkin({
        latitude: currentLocation.lat,
        longitude: currentLocation.lng,
      });
      await loadData();
      alert('출근이 완료되었습니다!');
    } catch (err: any) {
      setError(err.response?.data?.message || '출근 처리에 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  const handleCheckout = async () => {
    if (!currentLocation) {
      setError('위치 정보를 가져오는 중입니다...');
      return;
    }

    setLoading(true);
    setError('');

    try {
      await attendanceApi.checkout({
        latitude: currentLocation.lat,
        longitude: currentLocation.lng,
      });
      await loadData();
      alert('퇴근이 완료되었습니다!');
    } catch (err: any) {
      setError(err.response?.data?.message || '퇴근 처리에 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userName');
    router.push('/login');
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-white shadow-sm">
        <div className="max-w-4xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">GPS 출퇴근 관리</h1>
          <div className="flex items-center gap-4">
            <span className="text-gray-600">{userName}님</span>
            <button
              onClick={() => router.push('/settings')}
              className="text-blue-500 hover:underline"
            >
              설정
            </button>
            <button
              onClick={() => router.push('/history')}
              className="text-blue-500 hover:underline"
            >
              기록
            </button>
            <button
              onClick={handleLogout}
              className="text-red-500 hover:underline"
            >
              로그아웃
            </button>
          </div>
        </div>
      </nav>

      <div className="max-w-4xl mx-auto px-4 py-8">
        {!workplace ? (
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-6 text-center">
            <p className="text-yellow-800 mb-4">회사 위치가 설정되지 않았습니다.</p>
            <button
              onClick={() => router.push('/settings')}
              className="bg-blue-500 text-white px-6 py-2 rounded-md hover:bg-blue-600"
            >
              회사 위치 설정하기
            </button>
          </div>
        ) : (
          <div className="space-y-6">
            {/* 현재 상태 */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-lg font-bold mb-4">현재 상태</h2>
              <div className="space-y-2">
                <p>
                  <span className="font-semibold">상태:</span>{' '}
                  <span className={status?.isCheckedIn ? 'text-green-600' : 'text-gray-600'}>
                    {status?.isCheckedIn ? '출근 중' : '퇴근'}
                  </span>
                </p>
                {status?.isCheckedIn && status.checkinTime && (
                  <p>
                    <span className="font-semibold">출근 시간:</span>{' '}
                    {new Date(status.checkinTime).toLocaleString('ko-KR')}
                  </p>
                )}
                <p>
                  <span className="font-semibold">회사:</span> {workplace.name}
                </p>
                {distance !== null && (
                  <p>
                    <span className="font-semibold">회사와의 거리:</span>{' '}
                    <span className={distance <= workplace.radius ? 'text-green-600' : 'text-red-600'}>
                      {Math.round(distance)}m
                    </span>
                    {' '}(반경: {workplace.radius}m)
                  </p>
                )}
              </div>
            </div>

            {/* 출퇴근 버튼 */}
            <div className="bg-white rounded-lg shadow p-6">
              {error && (
                <div className="mb-4 text-red-500 text-sm">
                  {error}
                </div>
              )}

              <div className="grid grid-cols-2 gap-4">
                <button
                  onClick={handleCheckin}
                  disabled={loading || status?.isCheckedIn || !currentLocation}
                  className="bg-green-500 text-white py-4 rounded-md hover:bg-green-600 disabled:bg-gray-400 font-semibold text-lg"
                >
                  {loading ? '처리 중...' : '출근'}
                </button>
                <button
                  onClick={handleCheckout}
                  disabled={loading || !status?.isCheckedIn || !currentLocation}
                  className="bg-red-500 text-white py-4 rounded-md hover:bg-red-600 disabled:bg-gray-400 font-semibold text-lg"
                >
                  {loading ? '처리 중...' : '퇴근'}
                </button>
              </div>

              {!currentLocation && (
                <p className="mt-4 text-sm text-gray-500 text-center">
                  위치 정보를 가져오는 중...
                </p>
              )}
            </div>

            {/* 위치 정보 */}
            {currentLocation && (
              <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-lg font-bold mb-4">현재 위치</h2>
                <div className="text-sm text-gray-600">
                  <p>위도: {currentLocation.lat.toFixed(6)}</p>
                  <p>경도: {currentLocation.lng.toFixed(6)}</p>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
