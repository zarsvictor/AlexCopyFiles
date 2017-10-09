import org.apache.commons.io.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
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
    private JButton selectIgnoreFolders;
    private JButton copyButton;
    private JTextArea chooseFolderLabel;
    private JTextArea selectDestinationLabel;
    private JLabel filesCopied;
    private JTextArea log;

    private ArrayList<String> ignores;

    private File destinationPath;
    private File sourcePath;

    private int nrFilesCopied;

    public Main() {
        super("Backa upp din skit");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setLayout(new BorderLayout());
        nrFilesCopied = 0;
        mainPanel = new JPanel();
        log = new JTextArea();
        log.setEditable(false);
        log.setLineWrap(true);
        log.setEnabled(false);
        ignores = new ArrayList<>();
        filesCopied = new JLabel("Files copied: " + Integer.toString(nrFilesCopied));
        chooseFolderLabel = new JTextArea("Från");
        chooseFolderLabel.setEditable(false);
        chooseFolderLabel.setLineWrap(true);
        chooseFolderLabel.setEnabled(false);
        selectDestinationLabel = new JTextArea("Till");
        selectDestinationLabel.setEditable(false);
        selectDestinationLabel.setLineWrap(true);
        selectDestinationLabel.setEnabled(false);
        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        destinationFileChooser = new JFileChooser();
        destinationFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooseFoldersButton = new JButton("Välj mapp");
        chooseFoldersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int ret = fileChooser.showOpenDialog(mainPanel);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File[] files = fileChooser.getSelectedFiles();
                    for (File f : files) {
                        sourcePath = f;
                        chooseFolderLabel.setText(sourcePath.toString());
                        logging("Selected source: " + f.toString());
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
                    selectDestinationLabel.setText(destinationPath.toString());
                    logging("Selected destination: " + destinationPath.toString());
                }
            }
        });
        copyButton = new JButton("Kopiera");
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                /*File[] paths;
                FileSystemView fsv = FileSystemView.getFileSystemView();

                // returns pathnames for files and directory
                paths = File.listRoots();

                // for each pathname in pathname array
                for (File path : paths) {
                    // prints file and directory paths
                    System.out.println("Drive Name: " + path);
                    System.out.println("Description: " + fsv.getSystemTypeDescription(path));
                    System.out.println();
                }*/
                if (destinationPath != null) {
                    //int nrThread = 5;
                    resetNrFilesCopied();
                    //manager = new CopyThreadManager(nrThread, sourcePath,ignores,destinationPath.getAbsolutePath());
                    CopyFileVisitor visitor = new CopyFileVisitor(sourcePath.toPath(), destinationPath.toPath(), new CopyFileVisitor.CopyCondition() {
                        @Override
                        public boolean shouldCopyFile(Path file, Path target, BasicFileAttributes attrs) {
                            boolean copy = shouldCopy(file, target, attrs);
                            return copy;
                        }

                        @Override
                        public boolean shouldCopyDir(Path dir, Path target, BasicFileAttributes attrs) {
                            boolean copy = shouldCopy(dir, target, attrs);
                            return copy;
                        }

                        @Override
                        public void onSuccessfulFileCopy(Path file, Path target, BasicFileAttributes attrs) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    incrementNrFilesCopied();
                                    logging("File copied: " + file.toString() + "\nto\n" + target.toString());
                                }
                            });
                        }

                        @Override
                        public void onSuccessfulDirCopy(Path dir, Path target, BasicFileAttributes attrs) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    incrementNrFilesCopied();
                                    logging("Directory copied: " + dir.toString() + "\nto\n" + target.toString());
                                }
                            });
                        }
                    });
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Files.walkFileTree(sourcePath.toPath(), visitor);
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(mainPanel, "Alla filer är kopierade nu");
                                    }
                                });
                            } catch (IOException e) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(mainPanel, "Something went wrong");
                                    }
                                });
                                e.printStackTrace();
                            }
                        }

                    });
                    thread.start();
                    }else{
                        JOptionPane.showMessageDialog(mainPanel, "Du måste välja filer att kopiera...");
                    }
                }
            }

            );
            mainPanel.setLayout(new

            BorderLayout()

            );

            JPanel chooseFoldersPanel = new JPanel();
            chooseFoldersPanel.setLayout(new

            BorderLayout()

            );
            //chooseFoldersPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            chooseFoldersPanel.add(chooseFoldersButton,BorderLayout.PAGE_START);
            chooseFoldersPanel.add(chooseFolderLabel,BorderLayout.PAGE_END);

            JPanel selectDestinationPanel = new JPanel();
            selectDestinationPanel.setLayout(new

            BorderLayout()

            );
            //selectDestinationPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            selectDestinationPanel.add(selectDestinationButton,BorderLayout.PAGE_START);
            selectDestinationPanel.add(selectDestinationLabel,BorderLayout.PAGE_END);

            JPanel logPanel = new JPanel();
            logPanel.setLayout(new

            BoxLayout(logPanel, BoxLayout.Y_AXIS)

            );
            //logPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            logPanel.add(filesCopied);
            logPanel.add(log);

            JPanel buttons = new JPanel();
            buttons.add(chooseFoldersPanel);
            buttons.add(selectDestinationPanel);
            buttons.add(copyButton);

            mainPanel.add(buttons,BorderLayout.PAGE_START);

            mainPanel.add(logPanel,BorderLayout.CENTER);

            add(mainPanel);
            //mainPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
            this.

            pack();

            this.

            setSize(new Dimension(400, 400)

            );
            this.

            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            this.

            setVisible(true);

        }

    private void logging(String text) {
        log.append(text + "\n\n");
    }

    private void incrementNrFilesCopied() {
        nrFilesCopied++;
        filesCopied.setText("Files copied: " + Integer.toString(nrFilesCopied));
    }

    private void resetNrFilesCopied() {
        nrFilesCopied = 0;
        filesCopied.setText("Files copied: " + Integer.toString(nrFilesCopied));
    }

    private boolean shouldCopy(Path file, Path target, BasicFileAttributes attrs) {
        try {
            if (!Files.exists(target)) {
                return true;
            }
            FileTime targetLastModify = Files.readAttributes(target, BasicFileAttributes.class).lastModifiedTime();
            FileTime sourceLastModify = attrs.lastModifiedTime();
            int compare = targetLastModify.compareTo(sourceLastModify);
            if (compare < 0) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("FileChooserDemo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

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
