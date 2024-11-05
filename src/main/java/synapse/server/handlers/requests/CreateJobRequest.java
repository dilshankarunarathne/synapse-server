package synapse.server.handlers.requests;

public class CreateJobRequest {
    String type;
    String payload;
    String data;

    public CreateJobRequest(String type, String payload, String data) {
        this.type = type;
        this.payload = payload;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
