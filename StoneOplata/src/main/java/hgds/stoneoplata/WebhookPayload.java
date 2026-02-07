package hgds.stoneoplata;

import com.google.gson.annotations.SerializedName;

/**
 * Тело запроса вебхука от сайта после оплаты.
 */
public class WebhookPayload {

    @SerializedName("payment_id")
    private String paymentId;

    private String player;

    @SerializedName("product_type")
    private String productType;

    @SerializedName("product_id")
    private String productId;

    private String period;
    private Double amount;
    private Integer count;

    @SerializedName("case_type")
    private String caseType;

    private Boolean test;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getCaseType() {
        return caseType;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    public Boolean getTest() {
        return test;
    }

    public void setTest(Boolean test) {
        this.test = test;
    }
}
