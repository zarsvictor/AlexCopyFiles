import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Copy files when visited using File.walkDirectory function
 * Created by Victor on 2017-10-09.
 */
public class CopyFileVisitor implements FileVisitor<Path> {

    public interface FileVisitFailed {
        public void onFileVisitFailed();
    }

    public interface CopyCondition {
        boolean shouldCopyFile(Path file, Path target, BasicFileAttributes attrs);
        boolean shouldCopyDir(Path dir, Path target, BasicFileAttributes attrs);
        void onSuccessfulFileCopy(Path file, Path target, BasicFileAttributes attrs);
        void onSuccessfulDirCopy(Path dir, Path target, BasicFileAttributes attrs);
    }

    private FileVisitFailed onFileVisitFailedCallback;
    private CopyCondition condition;
    private Path targetRoot;
    private Path sourceRoot;

    public CopyFileVisitor(Path sourceRoot, Path targetRoot, CopyCondition callback) {
        this.targetRoot = targetRoot;
        this.sourceRoot = sourceRoot;
        this.condition = callback;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path target = targetRoot.resolve(sourceRoot.relativize(dir));
        if(condition.shouldCopyDir(dir, target, attrs)) {
            try {
                System.out.println("Copying: " + dir.toString() + " to " + target.toString());
                Files.copy(dir, target);
                condition.onSuccessfulDirCopy(dir, target, attrs);
            } catch (FileAlreadyExistsException e) {
                if (!Files.isDirectory(target))
                    throw e;
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path target = targetRoot.resolve(sourceRoot.relativize(file));
        if(condition.shouldCopyFile(file, target, attrs)) {
            System.out.println("Copying: " + file.toString() + " to " + target.toString());
            Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
            condition.onSuccessfulFileCopy(file, target, attrs);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}
