package cn.edu.ecnu.stu.gateway.common.config;

public class FlowCtlConfig {

    private String path;

    private long permitPerSecond;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getPermitPerSecond() {
        return permitPerSecond;
    }

    public void setPermitPerSecond(long permitPerSecond) {
        this.permitPerSecond = permitPerSecond;
    }
}
