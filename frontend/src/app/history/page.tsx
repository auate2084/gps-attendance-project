'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { attendanceApi, AttendanceResponse } from '@/lib/attendance';

export default function HistoryPage() {
  const router = useRouter();
  const [history, setHistory] = useState<AttendanceResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadHistory();
  }, []);

  const loadHistory = async () => {
    try {
      const data = await attendanceApi.getHistory();
      setHistory(data);
    } catch (err) {
      console.error('기록 로드 실패:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString('ko-KR');
  };

  const calculateWorkTime = (checkinTime: string, checkoutTime?: string) => {
    if (!checkoutTime) return '-';
    const diff = new Date(checkoutTime).getTime() - new Date(checkinTime).getTime();
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    return `${hours}시간 ${minutes}분`;
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-white shadow-sm">
        <div className="max-w-4xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">출퇴근 기록</h1>
          <button
            onClick={() => router.push('/dashboard')}
            className="text-blue-500 hover:underline"
          >
            돌아가기
          </button>
        </div>
      </nav>

      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow">
          {loading ? (
            <div className="p-8 text-center text-gray-500">로딩 중...</div>
          ) : history.length === 0 ? (
            <div className="p-8 text-center text-gray-500">출퇴근 기록이 없습니다</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                      회사
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                      출근 시간
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                      퇴근 시간
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                      근무 시간
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                      상태
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {history.map((record) => (
                    <tr key={record.id} className="hover:bg-gray-50">
                      <td className="px-4 py-3 text-sm text-gray-900">
                        {record.workplaceName}
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600">
                        {formatDateTime(record.checkinTime)}
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600">
                        {record.checkoutTime ? formatDateTime(record.checkoutTime) : '-'}
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600">
                        {calculateWorkTime(record.checkinTime, record.checkoutTime)}
                      </td>
                      <td className="px-4 py-3 text-sm">
                        <span
                          className={`px-2 py-1 rounded-full text-xs font-semibold ${
                            record.status === 'CHECKED_IN'
                              ? 'bg-green-100 text-green-800'
                              : 'bg-gray-100 text-gray-800'
                          }`}
                        >
                          {record.status === 'CHECKED_IN' ? '출근 중' : '퇴근'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
