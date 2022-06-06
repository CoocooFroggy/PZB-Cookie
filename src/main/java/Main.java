import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> lines = FileUtils.readLines(new File("links.txt"), StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.equals("")) continue;
            if (!line.startsWith("https://")) continue;

            System.out.println(lines.get(i - 1));
            System.out.println(line);

            URL url = new URL(line);
            ZipFile ipsw = new ZipFile(new HttpChannel(url), "iPSW", StandardCharsets.UTF_8.name(), true, true);
            ZipArchiveEntry bmEntry = ipsw.getEntry("BuildManifest.plist");

            InputStream buildManifestInputStream = ipsw.getInputStream(bmEntry);
            File buildManifest = new File("files/" + lines.get(i - 1) + "/BuildManifest.plist");

            FileUtils.copyInputStreamToFile(buildManifestInputStream, buildManifest);

            ipsw.close();
        }
    }
}
