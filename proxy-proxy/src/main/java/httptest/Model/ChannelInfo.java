package httptest.Model;

import java.net.InetSocketAddress;

public class ChannelInfo {
    private String url;
    private long writtenBytes;
    private long readBytes;
    private InetSocketAddress clientIp;

    public ChannelInfo(String url, long writtenBytes, long readBytes, InetSocketAddress clientIp) {
        super();
        this.url = url;
        this.writtenBytes = writtenBytes;
        this.readBytes = readBytes;
        this.clientIp = clientIp;
    }

    public String getUrl() {
        return url;
    }

    public long getWrittenBytes() {
        return writtenBytes;
    }

    public long getReadBytes() {
        return readBytes;
    }

    public InetSocketAddress getClientIp() {
        return clientIp;
    }
}