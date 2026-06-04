import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LogRepository {
    // 실제 MySQL 환경에 맞게 추후 수정해야 할 접속 정보
    private final String dbUrl = "jdbc:mysql://localhost:3306/fds_system";
    private final String dbUser = "root";
    private final String dbPassword = "password";

    // DB 서버 다운 등의 네트워크 장애 시 로그 유실을 막기 위한 로컬 메모리 캐시 (설계서 반영)
    private final List<WhiteBoxLog> localCache = new ArrayList<>();

    public LogRepository() {
        // MySQL JDBC 드라이버 로드 확인
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[경고] MySQL JDBC 드라이버가 없습니다. (추후 라이브러리 추가 필요)");
        }
    }

    // 화이트박스 로그를 DB에 INSERT
    public void saveLog(WhiteBoxLog log) {
        String sql = "INSERT INTO fds_logs (log_id, rule_name, detected_time, evidence_tx_ids) VALUES (?, ?, ?, ?)";

        // try-with-resources 문법으로 안전한 Connection 자원 해제
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, log.getLogId());
            pstmt.setString(2, log.getRuleName());
            pstmt.setLong(3, log.getDetectedTime());

            // 증거 트랜잭션들의 ID만 뽑아서 콤마(,)로 연결해 저장 (조회 시 용이함)
            StringBuilder evidenceIds = new StringBuilder();
            for (Transaction tx : log.getEvidenceTransactions()) {
                evidenceIds.append(tx.getTransactionId()).append(",");
            }
            pstmt.setString(4, evidenceIds.toString());

            pstmt.executeUpdate();
            System.out.println(">>> [DB] 화이트박스 로그 영구 저장 완료! (Log ID: " + log.getLogId() + ")");

            // 만약 캐시에 쌓인 게 있다면 여기서 한꺼번에 밀어 넣는(Flush) 로직을 추가할 수 있습니다.

        } catch (SQLException e) {
            // DB 연결 실패 시 시스템이 뻗지 않고 캐시에 담아두어 생존력(Resilience)을 높임
            System.err.println(">>> [DB 장애] 연결 실패. 로컬 캐시에 로그를 임시 안전 보관합니다. 사유: " + e.getMessage());
            localCache.add(log);
        }
    }
}