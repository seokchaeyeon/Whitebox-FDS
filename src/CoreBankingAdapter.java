public class CoreBankingAdapter {
    private final String coreIp;
    private final int port;
    private final DataPipeline pipeline;

    // 어댑터 생성 시 코어망 IP와 포트, 그리고 데이터를 밀어넣을 파이프라인을 연결
    public CoreBankingAdapter(String coreIp, int port, DataPipeline pipeline) {
        this.coreIp = coreIp;
        this.port = port;
        this.pipeline = pipeline;
    }

    /**
     * [데이터 수신]
     * 실제 환경에서는 Socket 통신이나 REST API(서버) 역할을 하여 대기하다가
     * 거래가 발생하면 데이터를 받아 파이프라인으로 넘깁니다.
     */
    public void receiveData(String rawData) {
        // 네트워크 딜레이나 수신 에러가 없다고 가정하고 파이프라인으로 즉시 전달 (ACK)
        pipeline.receiveData(rawData);
    }

    /**
     * [계좌 동결 명령 전송]
     * 룰 엔진이 이상거래를 적발했을 때, 원장망(코어뱅킹)에 해당 계좌 출금 정지 명령을 내림
     */
    public void sendBlockCommand(String accountNo) {
        System.out.println(">>> [🚨 코어뱅킹 긴급 네트워크 통신 🚨] 목적지: " + coreIp + ":" + port);
        System.out.println(">>> [명령어 전송] 계좌번호 [" + accountNo + "] 의 모든 출금 거래를 즉각 정지(Lock)합니다!");
        // 실제 실무에서는 여기서 Socket OutputStream을 열거나 HTTP POST 요청을 통해 은행 API를 호출합니다.
    }

    /**
     * [계좌 해제 명령 전송]
     * 관리자가 소명을 듣고 계좌를 다시 정상화할 때 사용
     */
    public void sendUnblockCommand(String accountNo) {
        System.out.println(">>> [코어뱅킹 네트워크 통신] 목적지: " + coreIp + ":" + port);
        System.out.println(">>> [명령어 전송] 계좌번호 [" + accountNo + "] 의 출금 정지(Lock)를 해제(Unlock)합니다.");
    }
}