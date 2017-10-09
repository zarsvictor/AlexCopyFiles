import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stuff. May be outdated.
 * Created by Victor on 2017-02-15.
 */
class CopyThreadManager implements Runnable {

    private ArrayBlockingQueue<File> folders;
    private ArrayBlockingQueue<String> ignore;
    private File sourceRoot;
    private String destinationRoot;
    private String destination;
    private AtomicInteger currentlyWorking;
    private ExecutorService pool;
    private int nrThreads;

    CopyThreadManager(int nrThreads, File sourceRoot, Collection<String> ignore, String destination) {
        this.nrThreads = nrThreads;
        pool = Executors.newFixedThreadPool(this.nrThreads);
        this.folders = new ArrayBlockingQueue<>(1000,true);
        this.ignore = new ArrayBlockingQueue<>(1000,false,ignore);
        this.destination = destination;
        this.sourceRoot = sourceRoot;
        this.destinationRoot = destination + '\\' + sourceRoot.getName();
        this.currentlyWorking = new AtomicInteger(0);
        folders.add(sourceRoot);
    }

    public boolean awaitTermination() throws InterruptedException{
        return pool.awaitTermination(30, TimeUnit.SECONDS);
    }

    public void shutDown() {
        pool.shutdown();
    }

    public void start() {
        for (int i = 0; i < nrThreads; i++) {
            pool.execute(this);
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Thread is starting");
            File currFolder;
            do  {
                currFolder = folders.poll();
                if(currFolder == null) {
                    continue;
                }
                currentlyWorking.getAndIncrement();
                String append = currFolder.getAbsolutePath().substring(currFolder.getAbsolutePath().indexOf(sourceRoot.getName()));
                String newDest = destination + '\\' + append;
                File file = new File(newDest);
                if(!ignore.contains(currFolder.getAbsolutePath())) {
                    try {
                        //System.out.println("Copying folder " + currFolder.toString() + " to " + file.toString());
                        Files.copy(currFolder.toPath(),file.toPath());
                        for(File f : currFolder.listFiles()) {
                            if(f.isDirectory()) {
                                folders.put(f);
                            } else {
                                String fileDest = newDest + "\\" + f.getName();
                                Files.copy(f.toPath(),new File(fileDest).toPath());
                                //System.out.println("Copying file " + f.toString() + " to " + newDest);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                currentlyWorking.decrementAndGet();
            } while (!Thread.interrupted() && (currentlyWorking.get() > 0 || !folders.isEmpty()));
        } catch (InterruptedException e) {
            return;
        }
        System.out.println("Done copying files");
    }
}
