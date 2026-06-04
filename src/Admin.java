public class Admin {
    private final String adminId;
    private final String password; // 실제 환경에서는 해시(Hash) 암호화되어 저장되어야 함
    private final String department;
    private final String name;
    private boolean isLoggedIn;

    // 시스템 관제 대시보드와 연결하여 로그인 시 세션을 동기화
    private final DashboardManager dashboard;

    public Admin(String adminId, String password, String department, String name, DashboardManager dashboard) {
        this.adminId = adminId;
        this.password = password;
        this.department = department;
        this.name = name;
        this.dashboard = dashboard;
        this.isLoggedIn = false;
    }

    /**
     * [관리자 로그인]
     * 사번과 비밀번호를 검증하고, 성공 시 대시보드 세션을 활성화합니다.
     */
    public boolean login(String inputId, String inputPassword) {
        if (this.adminId.equals(inputId) && this.password.equals(inputPassword)) {
            this.isLoggedIn = true;
            System.out.println("\n✅ [보안 인증 성공] " + department + " " + name + " 관리자님, 환영합니다.");
            dashboard.adminLogin();
            return true;
        } else {
            System.err.println("\n❌ [보안 인증 실패] 사번 또는 비밀번호가 올바르지 않습니다.");
            return false;
        }
    }

    /**
     * [관리자 로그아웃]
     */
    public void logout() {
        if (this.isLoggedIn) {
            this.isLoggedIn = false;
            dashboard.adminLogout();
            System.out.println("🔒 [보안] " + name + " 관리자의 세션이 안전하게 종료되었습니다.");
        }
    }

    // 컨트롤러에서 권한을 검증할 때 사용할 상태 반환 메서드
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}
