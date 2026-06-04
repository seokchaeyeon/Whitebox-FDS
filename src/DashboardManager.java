public class DashboardManager {
    private int normalCount = 0;
    private int blockedCount = 0;
    private int activeSessions = 0;

    // 관리자 로그인 시 세션 증가
    public void adminLogin() {
        activeSessions++;
        System.out.println("[대시보드] 관리자가 관제 시스템에 접속했습니다. (현재 활성 세션: " + activeSessions + "명)");
    }

    // 관리자 로그아웃 시 세션 감소
    public void adminLogout() {
        if (activeSessions > 0) activeSessions--;
        System.out.println("[대시보드] 관리자가 시스템에서 로그아웃했습니다.");
    }

    /**
     * [통계 그래프 업데이트]
     * 파이프라인과 룰 엔진에서 거래가 처리될 때마다 호출되어 실시간 비율을 갱신합니다.
     */
    public void updateStatistics(boolean isBlocked) {
        if (isBlocked) {
            blockedCount++;
        } else {
            normalCount++;
        }
        displayStatus(); // 수치가 바뀔 때마다 화면(콘솔) 갱신
    }

    /**
     * [긴급 푸시 알림 (WebSocket 대용)]
     * 룰 엔진이 스머핑 등 치명적 범죄를 적발하면, 즉시 관리자 화면에 경고 팝업을 띄웁니다.
     */
    public void sendAlertPush(WhiteBoxLog log) {
        System.out.println("\n🚨🚨 [대시보드 긴급 팝업 알림] 🚨🚨");
        System.out.println("치명적인 금융 범죄 패턴이 적발되었습니다! 즉각적인 확인이 필요합니다.");
        // 화면에 화이트박스 로그 리포트 렌더링
        System.out.println(log.createLogReport());
    }

    // 대시보드 상단 요약 현황판
    private void displayStatus() {
        System.out.printf("📊 [실시간 관제 현황] 정상 거래: %d건 | 차단 거래: %d건 | 모니터링 관리자: %d명\n",
                normalCount, blockedCount, activeSessions);
    }
}