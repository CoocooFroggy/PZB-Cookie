import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;

public final class HttpChannel implements SeekableByteChannel {

    private static final String COOKIE = System.getenv("COOKIE");

    private final URL url;
    private ReadableByteChannel ch;
    private long pos;
    private long length;

    public HttpChannel(URL url) {
        this.url = url;
    }

    @Override
    public long position() {
        return pos;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        if (newPosition == pos) {
            return this;
        } else if (ch != null) {
            ch.close();
            ch = null;
        }
        pos = newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        ensureOpen();
        return length;
    }

    @Override
    public SeekableByteChannel truncate(long size) {
        throw new UnsupportedOperationException("Truncate on HTTP is not supported.");
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        ensureOpen();
        int read = ch.read(buffer);
        if (read != -1)
            pos += read;
        return read;
    }

    @Override
    public int write(ByteBuffer buffer) {
        throw new UnsupportedOperationException("Write to HTTP is not supported.");
    }

    @Override
    public boolean isOpen() {
        return ch != null && ch.isOpen();
    }

    @Override
    public void close() throws IOException {
        ch.close();
    }

    private void ensureOpen() throws IOException {
        if (ch == null) {
            URLConnection connection = url.openConnection();
            connection.addRequestProperty("Host", "download.developer.apple.com");
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:101.0) Gecko/20100101 Firefox/101.0");
            connection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
            connection.addRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.addRequestProperty("Accept-Encoding", "gzip, deflate, br");
            connection.addRequestProperty("Referer", "https://developer.apple.com/");

            connection.addRequestProperty("Cookie", COOKIE);

            if (pos > 0)
                connection.addRequestProperty("Range", "bytes=" + pos + "-");
            ch = Channels.newChannel(connection.getInputStream());
            String resp = connection.getHeaderField("Content-Range");
            if (resp != null) {
                length = Long.parseLong(resp.split("/")[1]);
            } else {
                resp = connection.getHeaderField("Content-Length");
                length = Long.parseLong(resp);
            }
        }
    }
}