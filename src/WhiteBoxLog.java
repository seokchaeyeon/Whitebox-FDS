import java.util.ArrayList;
import java.util.List;

public class WhiteBoxLog {
    private final String logId;
    private final String ruleName;
    private final long detectedTime;
    private final List<Transaction> evidenceTransactions;

    public WhiteBoxLog(String logId, String ruleName, long detectedTime, List<Transaction> evidence) {
        this.logId = logId;
        this.ruleName = ruleName;
        this.detectedTime = detectedTime;
        // 데이터 오염 방지를 위해 깊은 복사(Deep Copy) 형태로 증거 리스트 저장
        this.evidenceTransactions = new ArrayList<>(evidence);
    }

    public String getLogId() { return logId; }
    public String getRuleName() { return ruleName; }
    public long getDetectedTime() { return detectedTime; }
    public List<Transaction> getEvidenceTransactions() { return new ArrayList<>(evidenceTransactions); }

    // 관리자가 즉각 소명 자료로 쓸 수 있는 포맷팅 리포트 생성
    public String createLogReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append(String.format("[화이트박스 탐지 로그] 로그 ID: %s\n", logId));
        sb.append(String.format("위반 규칙명: %s\n", ruleName));
        sb.append(String.format("탐지 시각: %d\n", detectedTime));
        sb.append("--------------------------------------------------\n");
        sb.append("   [이상거래 증거 내역 (Evidence Transactions)]\n");
        for (Transaction tx : evidenceTransactions) {
            sb.append("   -> ").append(tx.getTransactionInfo()).append("\n");
        }
        sb.append("==================================================");
        return sb.toString();
    }
}