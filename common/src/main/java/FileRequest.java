public class FileRequest extends AbstractMessage {

    private static final long serialVersionUID = 4021238847063742848L;
    private String filename;

    public String getFilename() {
        return filename;
    }

    public FileRequest(String filename) {
        this.filename = filename;
    }
}
