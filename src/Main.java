import org.apache.commons.io.*;

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
    private ArrayList<String> ignores;
    private JButton copyButton;
    private File destinationPath;
    private File sourcePath;
    private JLabel filesCopied;
    private JTextArea log;
    private CopyThreadManager manager;

    public Main() {
        super("Backa upp din skit");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        mainPanel = new JPanel();
        log = new JTextArea(400,400);
        ignores = new ArrayList<>();
        filesCopied = new JLabel("0");
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
                        log.append("Selected: " + f.getAbsolutePath());
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
                    manager = new CopyThreadManager(50,sourcePath,ignores,destinationPath.getAbsolutePath());
                    manager.run();
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
    }

    private void copyFile(String source, String destination) throws IOException {
        File src = new File(source);
        File dest = new File(destination);
        if(src.lastModified() != dest.lastModified()) {
            Files.copy(src.toPath(), dest.toPath());
        }
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
