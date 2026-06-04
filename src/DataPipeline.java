import java.util.ArrayList;
import java.util.List;

public class DataPipeline {
    private String rawData;
    private final List<Transaction> transactionArray;

    public DataPipeline() {
        this.transactionArray = new ArrayList<>();
    }

    // 외부 어댑터로부터 원시 데이터를 수신하는 메서드
    public void receiveData(String rawData) {
        this.rawData = rawData;
        parseToTransaction();
    }

    // 원시 문자열 파싱 (실제 환경에서는 JSON 파서 라이브러리를 사용하나, 기본 로직 구조 구현)
    private void parseToTransaction() {
        if (this.rawData == null || this.rawData.isEmpty()) return;

        // 예시용 파싱 로직: 컴마 등으로 구분된 데이터라 가정
        try {
            String[] tokens = this.rawData.split(",");
            String txId = tokens[0].trim();
            String sender = tokens[1].trim();
            String receiver = tokens[2].trim();
            double amount = Double.parseDouble(tokens[3].trim());
            long timestamp = Long.parseLong(tokens[4].trim());

            Transaction tx = new Transaction(txId, sender, receiver, amount, timestamp);
            insertData(tx);
        } catch (Exception e) {
            System.err.println("데이터 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * 핵심 알고리즘: 이진 탐색(Binary Search)을 이용해
     * 새로운 트랜잭션이 시간순으로 들어갈 최적의 인덱스를 반환합니다.
     */
    public int searchPositionByTime(long timestamp) {
        int low = 0;
        int high = transactionArray.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1; // 오버플로우 방지 비트 연산
            long midVal = transactionArray.get(mid).getTimestamp();

            if (midVal < timestamp) {
                low = mid + 1;
            } else if (midVal > timestamp) {
                high = mid - 1;
            } else {
                return mid; // 동일한 타임스탬프가 있다면 해당 위치 반환
            }
        }
        return low; // 존재하지 않는 경우, 정렬 상태를 유지하며 삽입될 최적의 위치
    }

    // 이진 탐색으로 찾은 인덱스 위치에 데이터를 동적 삽입
    public synchronized void insertData(Transaction tx) {
        int index = searchPositionByTime(tx.getTimestamp());
        transactionArray.add(index, tx);
    }

    // 룰 엔진이나 대시보드에서 안전하게 데이터를 읽어갈 수 있도록 얕은 복사본 반환
    public synchronized List<Transaction> getTransactionArray() {
        return new ArrayList<>(transactionArray);
    }
}