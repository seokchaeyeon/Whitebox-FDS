import java.util.List;

public class AccountController {
    private final CoreBankingAdapter adapter;
    private final DataPipeline pipeline;

    // 관리자 권한 인증을 위한 임시 마스터 키 (실제 환경에서는 DB나 세션 토큰으로 검증)
    private static final String MASTER_AUTH_CODE = "ADMIN-2026";

    public AccountController(CoreBankingAdapter adapter, DataPipeline pipeline) {
        this.adapter = adapter;
        this.pipeline = pipeline;
    }

    /**
     * [수동 동결 지시]
     * 관리자가 특정 계좌의 위험성을 인지하고 수동으로 차단할 때 호출
     */
    public void executeManualLock(String targetAccount, String authCode) {
        System.out.println("\n[컨트롤러] 수동 계좌 동결 프로세스 시작...");

        if (!MASTER_AUTH_CODE.equals(authCode)) {
            System.err.println("❌ [보안 에러] 권한이 없습니다. 동결 명령이 거부되었습니다.");
            return;
        }

        // 1. 코어뱅킹 망에 즉각 차단 명령 전송
        adapter.sendBlockCommand(targetAccount);

        // 2. 파이프라인(메모리)에 떠 있는 해당 계좌의 거래 내역 상태를 모두 강제 변경
        List<Transaction> allTransactions = pipeline.getTransactionArray();
        int affectedCount = 0;

        for (Transaction tx : allTransactions) {
            // 송금인이나 수취인 중 하나라도 해당 계좌라면 상태 변경
            if (tx.getSenderAccount().equals(targetAccount) || tx.getReceiverAccount().equals(targetAccount)) {
                tx.updateStatus("BLOCKED (MANUAL)");
                affectedCount++;
            }
        }

        System.out.println("✅ [성공] 계좌 [" + targetAccount + "] 수동 동결 완료. (영향받은 거래: " + affectedCount + "건)");
    }

    /**
     * [수동 동결 해제 지시]
     * 고객 소명 완료 후, 관리자가 계좌를 다시 정상화할 때 호출
     */
    public void executeUnlock(String targetAccount, String authCode) {
        System.out.println("\n[컨트롤러] 계좌 동결 해제(정상화) 프로세스 시작...");

        if (!MASTER_AUTH_CODE.equals(authCode)) {
            System.err.println("❌ [보안 에러] 권한이 없습니다. 해제 명령이 거부되었습니다.");
            return;
        }

        // 1. 코어뱅킹 망에 잠금 해제 명령 전송
        adapter.sendUnblockCommand(targetAccount);

        // 2. 파이프라인(메모리)에 떠 있던 차단 상태를 정상으로 복구
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