import java.util.List;

public class ListFileRequest extends AbstractMessage {
    private static final long serialVersionUID = -5914508040228238488L;

    List<String> list;

    public ListFileRequest(List<String> list) {
        this.list = list;
    }

    public List<String> getList() {
        return list;
    }

    public ListFileRequest() {

    }
}
