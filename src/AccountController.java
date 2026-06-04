import java.util.List;

public class AccountController {
    private final CoreBankingAdapter adapter;
    private final DataPipeline pipeline;

    public AccountController(CoreBankingAdapter adapter, DataPipeline pipeline) {
        this.adapter = adapter;
        this.pipeline = pipeline;
    }

    // 변경점: String authCode 대신 Admin 객체를 파라미터로 받음
    public void executeManualLock(String targetAccount, Admin admin) {
        System.out.println("\n[컨트롤러] 수동 계좌 동결 프로세스 시작...");

        // 인증된 관리자인지(로그인 상태인지) 검증
        if (!admin.isLoggedIn()) {
            System.err.println("❌ [보안 에러] 유효한 로그인 세션이 없습니다. 동결 명령이 거부되었습니다.");
            return;
        }

        adapter.sendBlockCommand(targetAccount);

        List<Transaction> allTransactions = pipeline.getTransactionArray();
        int affectedCount = 0;

        for (Transaction tx : allTransactions) {
            if (tx.getSenderAccount().equals(targetAccount) || tx.getReceiverAccount().equals(targetAccount)) {
                tx.updateStatus("BLOCKED (MANUAL)");
                affectedCount++;
            }
        }

        System.out.println("✅ [성공] 계좌 [" + targetAccount + "] 수동 동결 완료. (영향받은 거래: " + affectedCount + "건)");
    }

    // 변경점: String authCode 대신 Admin 객체를 파라미터로 받음
    public void executeUnlock(String targetAccount, Admin admin) {
        System.out.println("\n[컨트롤러] 계좌 동결 해제(정상화) 프로세스 시작...");

        // 인증된 관리자인지 검증
        if (!admin.isLoggedIn()) {
            System.err.println("❌ [보안 에러] 유효한 로그인 세션이 없습니다. 해제 명령이 거부되었습니다.");
            return;
        }

        adapter.sendUnblockCommand(targetAccount);

        List<Transaction> allTransactions = pipeline.getTransactionArray();
        int restoredCount = 0;

        for (Transaction tx : allTransactions) {
            if (tx.getSenderAccount().equals(targetAccount) || tx.getReceiverAccount().equals(targetAccount)) {
                if (tx.getStatus().contains("BLOCKED")) {
                    tx.updateStatus("SAFE (RESTORED)");
                    restoredCount++;
                }
            }
        }

        System.out.println("✅ [성공] 계좌 [" + targetAccount + "] 차단 해제 완료. (복구된 거래: " + restoredCount + "건)");
    }
}