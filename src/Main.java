import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * Created by c13vzs on 2017-02-13.
 */
public class Main extends JFrame {



    /*
    TODO:
    Backa upp skiten.
    Välj vilka mappar som ska backas upp.
    När hårddisk ansluts ska det automatiskt backa upp allt nytt som har hänt.
    Även om saker har ändrats.
    Backa upp allt nytt som har ändrats.
     */

    private JPanel mainPanel;
    private JFileChooser fileChooser;
    private JFileChooser destinationFileChooser;
    private JButton chooseFoldersButton;
    private JButton selectDestinationButton;
    private JButton copyButton;
    private File destinationPath;
    private ArrayBlockingQueue<File> directoriesToCopy;
    private SwingWorker<Void, Void> fileSearcher;
    private JLabel filesCopied;
    private JTextArea log;
    private int nrThreads;
    private int workingThreads;

    public Main() {
        super("Backa upp din skit");
        nrThreads = 10;
        workingThreads = 0;
        mainPanel = new JPanel();
        log = new JTextArea(400,400);
        filesCopied = new JLabel("0");
        directoriesToCopy = new ArrayBlockingQueue<>(1000);
        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        destinationFileChooser = new JFileChooser();
        destinationFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooseFoldersButton = new JButton("Välj mappar");
        chooseFoldersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int ret = fileChooser.showOpenDialog(mainPanel);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File[] files = fileChooser.getSelectedFiles();
                    for (File f : files) {
                        try {
                            directoriesToCopy.put(f);
                            log.append("Selected: " + f.getAbsolutePath());
                        } catch (InterruptedException e) {
                            JOptionPane.showMessageDialog(mainPanel, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        selectDestinationButton = new JButton("Välj var du vill spara din mappar!");
        selectDestinationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int ret = destinationFileChooser.showDialog(mainPanel, "Här vill jag spara min mappar");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    destinationPath = destinationFileChooser.getSelectedFile();
                }
            }
        });
        copyButton = new JButton("Kopiera");
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (destinationPath != null) {
                    while (!directoriesToCopy.isEmpty()) {
                        try {
                            CallbackFilter filter = new CallbackFilter() {
                                @Override
                                public boolean accept(File file) {
                                    return new Date(file.lastModified()).before(new Date());
                                }

                                @Override
                                public void fileFound(File f) {
                                    SwingUtilities.invokeLater(new TimerTask() {
                                        @Override
                                        public void run() {
                                            log.append("");
                                        }
                                    });
                                }
                            };
                            copyFile(directoriesToCopy.take(), destinationPath, filter);
                        } catch (IOException | InterruptedException e) {
                            JOptionPane.showMessageDialog(mainPanel, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "Du måste välja filer att kopiera...");
                }
            }
        });
        mainPanel.add(chooseFoldersButton);
        mainPanel.add(selectDestinationButton);
        mainPanel.add(copyButton);
        add(mainPanel);
        this.pack();
        this.setSize(new Dimension(400, 400));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);

    }

    private void copyFile(File source, File destination, FileFilter filter) throws IOException {
        File dest = new File(destination.getPath() + "//" + source.getName());
        FileUtils.copyDirectory(source,dest, filter, true);
        //FileUtils.copyDirectoryToDirectory();
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("FileChooserDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new Main());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                Main mainFrame = new Main();

            }
        });
    }

}
