import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListFileRequest extends AbstractMessage {
    private static final long serialVersionUID = -5914508040228238488L;

    private List<String> list;

    public ListFileRequest(Path path) {

        try {
//            list = Files.list(path)
//                    .map(Path::getFileName)
//                    .map(Path::toString)
//                    .collect(Collectors.toList());

            list = Files.list(path)
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getList() {
        return list;
    }

    public ListFileRequest() {

    }
}
