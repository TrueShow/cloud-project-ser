public class FileRequest extends AbstractMessage {

    private static final long serialVersionUID = 4021238847063742848L;
    private final String filename;
    private final boolean delete;

    public String getFilename() {
        return filename;
    }

    public FileRequest(String filename, boolean delete) {
        this.filename = filename;
        this.delete = delete;
    }

    public boolean isDelete() {
        return delete;
    }
}
