import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RuleEngine {
    private final DataPipeline pipeline;
    private CoreBankingAdapter adapter; // 추가된 부분
    private DashboardManager dashboard;

    // 실시간 탐지를 위한 룰셋 임계치 (핫스왑 업데이트 가능)
    private double highAmountThreshold = 10000000; // 1천만원 이상 단일 이체 시 적발
    private long smurfingTimeWindow = 1000 * 60 * 60; // 탐색 시간 단위: 1시간 (밀리초)
    private int smurfingCountThreshold = 3; // 1시간 내 동일 송금인이 3회 이상 이체 시 의심

    // 파이프라인과 연결
    public RuleEngine(DataPipeline pipeline) {
        this.pipeline = pipeline;
    }

    // 1. 단일 고액 거래 탐지
    public WhiteBoxLog detectHighAmount(Transaction tx) {
        if (tx.getAmount() >= highAmountThreshold) {
            tx.updateStatus("BLOCKED"); // 거래 차단
            return groupEvidence(tx, "단일_고액_거래_위반", List.of(tx));
        }
        return null; // 정상 거래
    }

    // 2. 스머핑(분할 송금) 탐지 알고리즘
    public WhiteBoxLog detectSmurfing(Transaction currentTx) {
        // 파이프라인에서 시간순으로 정렬된 과거 데이터 복사본 가져오기
        List<Transaction> history = pipeline.getTransactionArray();
        List<Transaction> evidence = new ArrayList<>();
        evidence.add(currentTx); // 현재 거래를 증거 1호로 추가

        long startTime = currentTx.getTimestamp() - smurfingTimeWindow;

        // 배열의 끝(최신)부터 역순으로 탐색하여 성능 최적화
        for (int i = history.size() - 1; i >= 0; i--) {
            Transaction pastTx = history.get(i);

            // 현재 평가 중인 거래 자기 자신은 건너뜀
            if (pastTx.getTransactionId().equals(currentTx.getTransactionId())) continue;

            // 탐색 범위(예: 1시간 전)를 벗어나면 즉시 반복문 종료 (정렬되어 있기 때문에 가능)
            if (pastTx.getTimestamp() < startTime) break;

            // 송금인이 동일한 과거 거래를 발견하면 증거 배열에 추가
            if (pastTx.getSenderAccount().equals(currentTx.getSenderAccount())) {
                evidence.add(pastTx);
            }
        }

        // 증거가 임계치(예: 3건) 이상 모였다면 스머핑으로 간주
        if (evidence.size() >= smurfingCountThreshold) {
            // 적발된 모든 연관 거래의 상태를 BLOCKED로 강제 변경
            for (Transaction e : evidence) {
                e.updateStatus("BLOCKED");
            }
            if (adapter != null) {
                adapter.sendBlockCommand(currentTx.getSenderAccount());
            }

            WhiteBoxLog generatedLog = groupEvidence(currentTx, "고빈도_분할송금_스머핑", evidence);
            if (dashboard != null) {
                dashboard.updateStatistics(true); // 차단 통계 1 증가
                dashboard.sendAlertPush(generatedLog); // 화면에 경고 팝업!
            }
            return generatedLog;

        }

        return null; // 정상 거래
    }

    // 3. 증거를 묶어 투명한 화이트박스 로그(WhiteBoxLog) 생성
    private WhiteBoxLog groupEvidence(Transaction triggerTx, String ruleName, List<Transaction> evidence) {
        String logId = "LOG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new WhiteBoxLog(logId, ruleName, triggerTx.getTimestamp(), evidence);
    }

    // 4. 시스템 무중단 룰셋 업데이트 (런타임 임계치 조정)
    public void updateRuleSettings(double newHighAmount, long newTimeWindow, int newCount) {
        this.highAmountThreshold = newHighAmount;
        this.smurfingTimeWindow = newTimeWindow;
        this.smurfingCountThreshold = newCount;
        System.out.println("[시스템] 룰 임계치가 성공적으로 업데이트되었습니다.");
    }

    public void setAdapter(CoreBankingAdapter adapter){
        this.adapter = adapter;
    }

    public void setDashboard(DashboardManager dashboard) {
        this.dashboard = dashboard;
    }
}

