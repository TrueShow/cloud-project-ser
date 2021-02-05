import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileMessage extends AbstractMessage {
    private static final long serialVersionUID = -4998843746053293367L;

    private String fileName;
    private byte[] data;

    public FileMessage(String path) {
        try {
            Path file = Paths.get(path);
            fileName = file.getFileName().toString();
            data = Files.readAllBytes(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileMessage(Path path) throws IOException {
        fileName = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }
}
