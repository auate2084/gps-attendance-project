import './globals.css'
import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'GPS 출퇴근 관리',
  description: 'GPS 기반 출퇴근 관리 시스템',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  )
}
