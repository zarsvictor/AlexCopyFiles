import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Victor on 2017-02-15.
 */
public class CopyThreadManager implements Runnable {

    private ArrayBlockingQueue<File> folders;
    private ArrayBlockingQueue<String> ignore;
    private Thread[] threads;
    private File sourceRoot;
    private String destinationRoot;
    private String destination;
    private boolean stop;

    public CopyThreadManager(int nrThreads, File sourceRoot, Collection<String> ignore, String destination) {
        threads = new Thread[50];
        for (int i = 0; i < nrThreads; i++) {
            threads[i] = new Thread(this);
        }
        stop = false;
        this.folders = new ArrayBlockingQueue<>(1000,true);
        this.ignore = new ArrayBlockingQueue<>(1000,false,ignore);
        this.destination = destination;
        this.sourceRoot = sourceRoot;
        this.destinationRoot = destination + '\\' + sourceRoot.getName();
        folders.add(sourceRoot);
    }

    public void cancel() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Thread is starting");
            File currFolder;
            while ((currFolder = folders.take()) != null) {
                String append = currFolder.getAbsolutePath().substring(currFolder.getAbsolutePath().indexOf(sourceRoot.getName()));
                String newDest = destination + '\\' + append;
                File file = new File(newDest);
                if(!ignore.contains(currFolder.getAbsolutePath())) {
                    try {
                        Files.copy(currFolder.toPath(),file.toPath());
                        for(File f : currFolder.listFiles()) {
                            if(f.isDirectory()) {
                                folders.put(f);
                            } else {
                                String fileDest = newDest + "\\" + f.getName();
                                Files.copy(f.toPath(),new File(fileDest).toPath());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (InterruptedException e) {
            return;
        }
    }
}
