import java.io.File;
import java.io.FileFilter;

/**
 * Created by c13vzs on 2017-02-14.
 */
public interface CallbackFilter extends FileFilter {
    void fileFound(File f);
}
