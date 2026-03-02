'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { workplaceApi, WorkplaceResponse } from '@/lib/workplace';

export default function SettingsPage() {
  const router = useRouter();
  const [name, setName] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [radius, setRadius] = useState('100');
  const [address, setAddress] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadWorkplace();
  }, []);

  const loadWorkplace = async () => {
    try {
      const data = await workplaceApi.get();
      setName(data.name);
      setLatitude(data.latitude.toString());
      setLongitude(data.longitude.toString());
      setRadius(data.radius.toString());
      setAddress(data.address || '');
    } catch (err) {
      console.log('회사 위치 없음');
    }
  };

  const handleGetCurrentLocation = () => {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLatitude(position.coords.latitude.toString());
          setLongitude(position.coords.longitude.toString());
          setSuccess('현재 위치를 가져왔습니다!');
        },
        (error) => {
          setError('위치 정보를 가져올 수 없습니다.');
        }
      );
    } else {
      setError('이 브라우저는 위치 서비스를 지원하지 않습니다.');
    }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      await workplaceApi.save({
        name,
        latitude: parseFloat(latitude),
        longitude: parseFloat(longitude),
        radius: parseInt(radius),
        address,
      });
      setSuccess('회사 위치가 저장되었습니다!');
      setTimeout(() => {
        router.push('/dashboard');
      }, 1500);
    } catch (err: any) {
      setError(err.response?.data?.message || '저장에 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-white shadow-sm">
        <div className="max-w-4xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">회사 위치 설정</h1>
          <button
            onClick={() => router.push('/dashboard')}
            className="text-blue-500 hover:underline"
          >
            돌아가기
          </button>
        </div>
      </nav>

      <div className="max-w-2xl mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow p-6">
          <form onSubmit={handleSave}>
            <div className="mb-4">
              <label className="block text-gray-700 text-sm font-bold mb-2">
                회사 이름
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="예: 본사"
                required
              />
            </div>

            <div className="mb-4">
              <label className="block text-gray-700 text-sm font-bold mb-2">
                위도
              </label>
              <input
                type="number"
                step="any"
                value={latitude}
                onChange={(e) => setLatitude(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="예: 37.5665"
                required
              />
            </div>

            <div className="mb-4">
              <label className="block text-gray-700 text-sm font-bold mb-2">
                경도
              </label>
              <input
                type="number"
                step="any"
                value={longitude}
                onChange={(e) => setLongitude(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="예: 126.9780"
                required
              />
            </div>

            <div className="mb-4">
              <button
                type="button"
                onClick={handleGetCurrentLocation}
                className="w-full bg-gray-500 text-white py-2 rounded-md hover:bg-gray-600"
              >
                현재 위치 가져오기
              </button>
            </div>

            <div className="mb-4">
              <label className="block text-gray-700 text-sm font-bold mb-2">
                반경 (미터)
              </label>
              <input
                type="number"
                value={radius}
                onChange={(e) => setRadius(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                min="50"
                max="1000"
                required
              />
              <p className="text-sm text-gray-500 mt-1">
                50m ~ 1000m 사이로 설정 가능합니다
              </p>
            </div>

            <div className="mb-6">
              <label className="block text-gray-700 text-sm font-bold mb-2">
                주소 (선택사항)
              </label>
              <input
                type="text"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="예: 서울시 강남구..."
              />
            </div>

            {error && (
              <div className="mb-4 text-red-500 text-sm text-center">
                {error}
              </div>
            )}

            {success && (
              <div className="mb-4 text-green-500 text-sm text-center">
                {success}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600 disabled:bg-gray-400"
            >
              {loading ? '저장 중...' : '저장'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
