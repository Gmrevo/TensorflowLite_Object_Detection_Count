package org.tensorflow.lite.examples.detection.convertion;

public class DetectedProduct  {

    int lastproductId;
    long timestamp;
    int totaldetections;
    public DetectedProduct(){}

    public DetectedProduct(int lastproductId,  int totaldetections,long timestamp) {
        this.lastproductId = lastproductId;
        this.timestamp = timestamp;
        this.totaldetections = totaldetections;
    }

    public int getLastproductId() {
        return lastproductId;
    }

    public void setLastproductId(int lastproductId) {
        this.lastproductId = lastproductId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTotaldetections() {
        return totaldetections;
    }

    public void setTotaldetections(int totaldetections) {
        this.totaldetections = totaldetections;
    }
}
