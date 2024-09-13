/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
package Client;

import Remote.RemoteWhiteBoardInterface;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


public class WhiteboardGUI extends JFrame {
    private BufferedImage currentImage;
    JList<String> userList;
    private String currentFilePath = null;
    CopyOnWriteArrayList<String> chats;
    private String username;
    private Color currentColor = Color.BLACK;
    private DrawingPanel drawingPanel;
    private RemoteWhiteBoardInterface server;
    JTextArea chatArea;
    private boolean isModified = false;

    private boolean isAdmin = false;
    private BufferedImage backgroundImage;


    public WhiteboardGUI(RemoteWhiteBoardInterface setServer, String setUsername) {
        userList = new JList<>();
        username = setUsername;
        server = setServer;
        try {
            isAdmin = server.getAdmin().equals(username);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        if (isAdmin) {
            setTitle("Drawing Application User: " + username + " (Admin)");
        } else {
            setTitle("Drawing Application User: " + username + " (Guest)");
        }

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitProcedure();
            }
        });

        initializeUI();
    }

    private void initializeUI()  {
            // Drawing area setup
            drawingPanel = new DrawingPanel(server);
            drawingPanel.setPreferredSize(new Dimension(600, 400));
            drawingPanel.setBackground(Color.WHITE);
            drawingPanel.setCurrentColor(currentColor);

            // Toolbar with drawing tools
            JToolBar toolBar = new JToolBar();
            JButton penButton = new JButton("Pen");
            JButton lineButton = new JButton("Line");
            JButton circleButton = new JButton("Circle");
            JButton ovalButton = new JButton("Oval");
            JButton rectangleButton = new JButton("Rectangle");
            JButton colorButton = new JButton("Color");
            JButton textButton = new JButton("Text");

            // Handle different eraser
            String[] eraserSizes = {"Small Eraser", "Medium Eraser", "Large Eraser"};
            JComboBox<String> eraserSizeBox = new JComboBox<>(eraserSizes);
            eraserSizeBox.setMaximumSize(new Dimension(100, 20));
            eraserSizeBox.addActionListener(e -> {
                String selectedSize = (String) eraserSizeBox.getSelectedItem();
                switch (selectedSize) {
                    case "Small Eraser":
                        drawingPanel.setEraserSize(5);  // Small size
                        break;
                    case "Medium Eraser":
                        drawingPanel.setEraserSize(10);  // Medium size
                        break;
                    case "Large Eraser":
                        drawingPanel.setEraserSize(20);  // Large size
                        break;
                }
            });

            colorButton.addActionListener(e -> {
                currentColor = JColorChooser.showDialog(this, "Choose a color", currentColor);
                drawingPanel.setCurrentColor(currentColor);
            });
            toolBar.add(penButton);
            toolBar.add(lineButton);
            toolBar.add(rectangleButton);
            toolBar.add(circleButton);
            toolBar.add(ovalButton);
            toolBar.add(textButton);
            toolBar.add(eraserSizeBox);
            toolBar.add(colorButton);

            // Listener of different drawing tools
            penButton.addActionListener(e -> drawingPanel.setTool("Pen"));
            lineButton.addActionListener(e -> drawingPanel.setTool("Line"));
            circleButton.addActionListener(e -> drawingPanel.setTool("Circle"));
            ovalButton.addActionListener(e -> drawingPanel.setTool("Oval"));
            rectangleButton.addActionListener(e -> drawingPanel.setTool("Rectangle"));
            eraserSizeBox.addActionListener(e -> drawingPanel.setTool("Eraser"));
            textButton.addActionListener(e -> drawingPanel.setTool("Text"));

            // Fetch latest user list from server when initialing
            updateUsersDisplay();

            // Display user list
            JScrollPane userScrollPane = new JScrollPane(userList);
            userScrollPane.setPreferredSize(new Dimension(150, 400));
            JButton kickButton = new JButton("Kick");
            JPanel userPanel = new JPanel(new BorderLayout());
            // Kick logic
            if (isAdmin) {
                kickButton.setEnabled(true);

            } else {
                kickButton.setEnabled(false);
            }
            kickButton.addActionListener(e -> {
                String selectedUser = userList.getSelectedValue();
                // Prevent admin from kicking themselves
                if (selectedUser != null && !selectedUser.equals(username)) {
                    try {
                        server.removeUser(selectedUser);
                        JOptionPane.showMessageDialog(this, "User kicked: " + selectedUser);
                    } catch (RemoteException ex) {
                        JOptionPane.showMessageDialog(this, "Failed to kick user: " + ex.getMessage(),
                                "Communication Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "No user selected or invalid selection",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            userPanel.add(userScrollPane, BorderLayout.CENTER);
            userPanel.add(kickButton, BorderLayout.SOUTH);

            /* Chat area on the right */
            chatArea = new JTextArea();
            // make sure users can't directly edit the chat display
            chatArea.setEditable(false);
            JTextField chatField = new JTextField();
            JButton sendButton = new JButton("Send");
            sendButton.addActionListener(e -> {
                String message = chatField.getText().trim();
                if (!message.isEmpty()) {
                    try {
                        // Send message to server
                        server.sendMessage(username + ": " + message);
                    } catch (RemoteException ex) {
                        JOptionPane.showMessageDialog(this, "Failed to send message: " + ex.getMessage(),
                                "Communication Error", JOptionPane.ERROR_MESSAGE);
                    }
                    chatField.setText(""); // clear the input field
                }
            });

            JPanel chatInputPanel = new JPanel(new BorderLayout());
            chatInputPanel.add(chatField, BorderLayout.CENTER);
            chatInputPanel.add(sendButton, BorderLayout.EAST);

            JPanel chatPanel = new JPanel(new BorderLayout());
            chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
            chatPanel.add(chatInputPanel, BorderLayout.SOUTH);
            chatPanel.setPreferredSize(new Dimension(200, 400)); // Adjust width here

            // Menu bar for file operations
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");

            if (isAdmin) {  // Only allow manager to access these options
                JMenuItem newItem = new JMenuItem("New");
                JMenuItem openItem = new JMenuItem("Open");
                JMenuItem saveItem = new JMenuItem("Save");
                JMenuItem saveAsItem = new JMenuItem("Save As");
                newItem.addActionListener(e -> {
                    try {
                        newCanvas();
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                openItem.addActionListener(e -> {open();});
                saveItem.addActionListener(e -> {save();});
                saveAsItem.addActionListener(e -> {saveAs();});

                fileMenu.add(newItem);
                fileMenu.add(openItem);
                fileMenu.add(saveItem);
                fileMenu.add(saveAsItem);
                }

            JMenuItem closeItem = new JMenuItem("Close");
            closeItem.addActionListener(e -> {
                exitProcedure();
            });
            fileMenu.add(closeItem);

            menuBar.add(fileMenu);
            setJMenuBar(menuBar);

            // Layout setup
            add(toolBar, BorderLayout.NORTH);
            add(drawingPanel, BorderLayout.CENTER);
            add(userPanel, BorderLayout.WEST);
            add(chatPanel, BorderLayout.EAST);

            // Show the frame
            pack();
            setVisible(true);

    }

    // Update GUI chats
    public void guiUpdateChats() {
        try {
            // Fetch chats from server
            chats = server.getChats();
            // Clear existing content
            chatArea.setText("");
            // Append each message with a new line
            for (String message : chats) {
                chatArea.append(message + "\n");
            }
            // Scroll to the bottom
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error updating chats: " + e.getMessage(),
                    "Communication Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Update gui drawings
    public void guiUpdateShapes() throws RemoteException {
        drawingPanel.updateShapes();
        isModified = true;
    }

    // For File -> New operation
    private void newCanvas() throws RemoteException {
        if (isModified) {
            int result = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes. Do you want to save them before starting a new canvas?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                // Perform save operation
                if(save()) {
                    // Clear the canvas after saving
                    clearCanvas();
                }

            } else if (result == JOptionPane.NO_OPTION) {
                // Clear the canvas without saving
                clearCanvas();
            }
            // If the result is CANCEL_OPTION, do nothing
        } else {
            // If there are no changes, simply clear the canvas
            clearCanvas();
        }
    }

    // Clear canvas
    private void clearCanvas() throws RemoteException {
        server.resetWhiteBoard();
        currentFilePath = null;  // Reset the file path
        isModified = false;
    }

    // For FILE -> saveAs operation
    private boolean saveAs() {
        // Show an informational message before proceeding with the file save
        int confirm = JOptionPane.showConfirmDialog(this,
                "Please EXPAND this whiteboard application GUI so that all the drawings can be saved. Do you want to continue?",
                "Confirm Save",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Ask user specify a file to save current whiteboard content
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Files (*.png)", "png");
            fileChooser.setFileFilter(filter);

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".png")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
                }
                // update current file path
                currentFilePath = fileToSave.getAbsolutePath();
                try {
                    // save file as PNG
                    saveWhiteboardAsPNG(currentFilePath);
                    JOptionPane.showMessageDialog(this, "Saved as file: " + currentFilePath);
                    isModified = false;
                    return true;
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(),
                            "Save Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            return false;
        } else {
            JOptionPane.showMessageDialog(this, "Save operation canceled.",
                    "Operation Canceled", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
    }


    private void saveWhiteboardAsPNG(String filePath) throws IOException {
        // Create a BufferedImage object of the type TYPE_INT_RGB
        BufferedImage image = new BufferedImage(drawingPanel.getWidth(), drawingPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        // Get graphics context from the BufferedImage
        Graphics2D g2d = image.createGraphics();

        // Paint the drawing panel to the BufferedImage
        drawingPanel.paint(g2d);
        g2d.dispose();

        // Write the BufferedImage as a PNG file
        File file = new File(filePath);
        ImageIO.write(image, "PNG", file);
    }

    // For FILE -> OPEN operation
    private void open() {
        // Check for unsaved changes
        if (isModified) {
            int result = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes. Do you want to save them before opening another file?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                // Save before opening new
                if(save()){
                    showFileChooserForOpen();  // Continue to open
                }
            } else if (result == JOptionPane.NO_OPTION) {
                 showFileChooserForOpen();  // Open without saving

            }
            // If cancel, do nothing
        } else {
            showFileChooserForOpen();  // Directly show file chooser
        }
    }

    // File chooser when admin clicks Open
    private void showFileChooserForOpen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open File");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Files (*.png)", "png");
        fileChooser.setFileFilter(filter);
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            try {
                openImage(fileToOpen);  // Method to load and display the image
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if(userSelection == JFileChooser.CANCEL_OPTION) {
            System.out.println("cancelled22");
        }
    }

    // Display image to whiteboard and reset whiteboard
    private void openImage(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        // set new image to drawing panel
        drawingPanel.displayImage(image);
        currentFilePath = file.getAbsolutePath();
        isModified = false;
        // reset whiteboard (clear shapes and image)
        server.resetWhiteBoard();
        currentImage = image;
        // After set admin's background, set others.
        sendImageToServer(image);
    }

    // Convert image to byte array
    public byte[] imageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    // Send image to server
    public void sendImageToServer(BufferedImage image) {
        try {
            // image need to be converted to byte
            // as RMI only supports serializable object or primitive data type
            byte[] imageData = imageToByteArray(image);
            server.uploadImage(imageData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // GUI receive image from server
    public void guiReceiveImage(byte[] imageData) {
        if (imageData == null) {
            drawingPanel.displayImage(null);  // Clear the display or handle the null appropriately
            return;  // Exit the method to avoid processing null data
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            BufferedImage image = ImageIO.read(bais);
            drawingPanel.displayImage(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // File -> Save operation
    private boolean save() {
        if (currentFilePath == null) {
            return saveAs();  // If there's no file path, use Save As functionality
        } else {
            // Display a warning message before proceeding with the save operation
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Please ENLARGE the GUI of the whiteboard application to ensure that all drawings can be saved successfully. Do you want to continue?",
                    "WARNING",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    saveWhiteboardAsPNG(currentFilePath);  // Save to the current file path
                    JOptionPane.showMessageDialog(this, "Whiteboard saved successfully.");
                    isModified = false;
                    return true;
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error saving whiteboard: " + e.getMessage(),
                            "Save Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Save operation canceled.",
                        "Operation Canceled", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
            return false;
        }
    }

    // Update user list
    public void updateUsersDisplay() {
        try {
            ArrayList<String> users = server.getUsers(); // Fetch the list of users from the server
            DefaultListModel<String> userListModel = new DefaultListModel<>();
            for (String user : users) {
                userListModel.addElement(user);
            }
            userList.setModel(userListModel);
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error fetching user list: " + e.getMessage(),
                    "Communication Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Ask admin if client can join
    public boolean notifyJoinRequest(String username) throws RemoteException {
        // This needs to be an atomic reference to handle the value inside and outside the EDT
        final AtomicBoolean approved = new AtomicBoolean(false);

        try {
            SwingUtilities.invokeAndWait(() -> {
                int response = JOptionPane.showConfirmDialog(null,
                        username + " wants to join. Approve?",
                        "Approve Join Request",
                        JOptionPane.YES_NO_OPTION);
                approved.set(response == JOptionPane.YES_OPTION);
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // re-interrupt the thread
            System.err.println("Thread interrupted during execution of dialog.");
        } catch (InvocationTargetException e) {
            System.err.println("Error in executing the update on the AWT event dispatching thread.");
        }

        return approved.get();
    }

    // Close application
    public void shutdown() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "The manager has terminated the session. The application will now close.", "Session Ended", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);  // Close the client application
        });
    }

    // It is called when user tries to exit
    private void exitProcedure() {
        if (isAdmin) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "You are the manager. Closing will end the session for all users. Continue?",
                    "Confirm Shutdown",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    server.notifyShutdown();  // Notify all clients about the shutdown
                    System.exit(0);  // Exit the application
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(this, "Error while shutting down: " + ex.getMessage(),
                            "Shutdown Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    server.userQuit(username);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                System.exit(0);
            }
        }
    }

    // Reset drawings
    public void guiResetShapes() throws RemoteException {
        drawingPanel.updateShapes();
        isModified = false;
    }
}

