public class Transaction {
    private final String transactionId;
    private final String senderAccount;
    private final String receiverAccount;
    private final double amount;
    private final long timestamp; // Epoch Time (밀리초 단위)
    private String status; // PENDING, SAFE, BLOCKED

    public Transaction(String transactionId, String senderAccount, String receiverAccount, double amount, long timestamp) {
        this.transactionId = transactionId;
        this.senderAccount = senderAccount;
        this.receiverAccount = receiverAccount;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = "PENDING";
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getSenderAccount() { return senderAccount; }
    public String getReceiverAccount() { return receiverAccount; }
    public double getAmount() { return amount; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }

    // 상태 업데이트
    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }

    public String getTransactionInfo() {
        return String.format("[TxID: %s] %s -> %s | 금액: %,.0f원 | 시간: %d | 상태: %s",
                transactionId, senderAccount, receiverAccount, amount, timestamp, status);
    }
}