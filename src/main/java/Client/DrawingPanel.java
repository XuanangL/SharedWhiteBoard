/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
package Client;
import Remote.RemoteWhiteBoardInterface;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.concurrent.CopyOnWriteArrayList;

class DrawingPanel extends JPanel {
    BufferedImage currentImage;
    Boolean isEditing= false;
    private CopyOnWriteArrayList<MyShape> myShapes = new CopyOnWriteArrayList<>();

    private JTextField textField;
    private Font textFont = new Font("SansSerif", Font.BOLD, 20);
    private MyShape currentMyShape;
    private String tool = "Pen"; // Default tool

    private Color currentColor = Color.BLACK;

    private int eraserSize = 10;

    private RemoteWhiteBoardInterface whiteBoard;
    public DrawingPanel(RemoteWhiteBoardInterface setWhiteBoard) {
        whiteBoard = setWhiteBoard;
        try {
            setBackground(Color.WHITE);
            // For handle mouse motion
            MouseAdapter mouseHandler = new MouseAdapter() {
                Point start;

                // This event captures the starting point of the shape that will be drawn
                @Override
                public void mousePressed(MouseEvent e) {
                    start = e.getPoint();
                    switch (tool) {
                        case "Pen":
                            currentMyShape = new FreeHand(currentColor, 1);
                            ((FreeHand) currentMyShape).addPoint(e.getX(), e.getY());
                            break;
                        case "Eraser":
                            currentMyShape = new FreeHand(getBackground(), eraserSize);
                            ((FreeHand) currentMyShape).addPoint(e.getX(), e.getY());
                            break;
                        case "Line":
                            currentMyShape = new Line(currentColor, start, start);
                            break;
                        case "Rectangle":
                            currentMyShape = new Rectangle(currentColor, start, 0, 0);
                            break;
                        case "Oval":
                            currentMyShape = new Oval(currentColor, start, 0, 0);
                            break;
                        case "Circle":
                            currentMyShape = new Circle(currentColor, start, 0); // Initialize with radius 0
                            break;
                        case "Text":
                            System.out.println("selected text");
                            // If the previous textfield is not saved due to focus lose, it adds current text to canvas
                            if (isEditing) {
                                System.out.println("unsaved");
                                try {
                                    addTextToCanvasWhenLoseFocus();
                                } catch (RemoteException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                createTextField(e.getPoint());
                            }
                            break;
                    }
                    // This ensures the user does not feel lagging.
                    // It first draws on client canvas
                    if (currentMyShape != null) {
                        myShapes.add(currentMyShape);
                    }

                }

                /* This event is fired as the mouse is dragged across the panel.
                    It is crucial for tools like the pen or drawing lines and shapes where the shape dimensions
                    or characteristics are updated in real-time as the user moves the mouse. */
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (currentMyShape instanceof FreeHand) {
                        ((FreeHand) currentMyShape).addPoint(e.getX(), e.getY());
                        // Send FreeHand drawing to server,
                        // this makes sure all freehand drawings are sychronised
                        try {
                            sendDrawingToServer(currentMyShape);
                        } catch (RemoteException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else if (currentMyShape instanceof Line) {
                        ((Line) currentMyShape).setEnd(e.getPoint());
                    } else if (currentMyShape instanceof Rectangle) {
                        Rectangle rect = (Rectangle) currentMyShape;
                        rect.setWidth(Math.abs(start.x - e.getX()));
                        rect.setHeight(Math.abs(start.y - e.getY()));
                        rect.setStart(new Point(Math.min(start.x, e.getX()), Math.min(start.y, e.getY())));
                    } else if (currentMyShape instanceof Oval) {
                        Oval oval = (Oval) currentMyShape;
                        oval.setWidth(Math.abs(start.x - e.getX()));
                        oval.setHeight(Math.abs(start.y - e.getY()));
                        oval.setStart(new Point(Math.min(start.x, e.getX()), Math.min(start.y, e.getY())));
                    } else if (currentMyShape instanceof Circle) {
                        Circle circle = (Circle) currentMyShape;
                        int dx = e.getX() - start.x;
                        int dy = e.getY() - start.y;
                        circle.setRadius((int) Math.sqrt(dx * dx + dy * dy)); // Update radius
                    }
                    // Only freehand do not need to repaint
                    // as it is updated in real-time as the user moves the mouse
                    if (!(currentMyShape instanceof FreeHand)) {
                        repaint();
//                        System.out.println("repaint");
                    }

                }

                // This event marks the end of a drawing action
                @Override
                public void mouseReleased(MouseEvent e) {
                    try {
                        sendDrawingToServer(currentMyShape);
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                    currentMyShape = null;
                    start = null;
                }
            };
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        } catch(Exception e) {
            System.out.println("runtime");
        }

    }
    private void sendDrawingToServer(MyShape myShape) throws RemoteException {
        // check if shape is null, this check is essential as this method will always  be
        // triggered by mouseReleased. When a user selects "Text" and click on the canvas,
        // an empty textfield will show up, this will be captured by mouseReleased.
        // The textField is empty means shape is null, so this checking avoid exception
        if (myShape == null) {
            return;
        }
        if (whiteBoard.newDrawing(myShape)) {
            myShapes = whiteBoard.getShapes();
            repaint();
        }
    }

    // set background image
    private void setBackgroundImage(Graphics g) {
        if (currentImage != null) {
            // Calculate the starting coordinates to center the image
            int x = (this.getWidth() - currentImage.getWidth()) / 2;
            int y = (this.getHeight() - currentImage.getHeight()) / 2;

            // Draw the image at its original size
            g.drawImage(currentImage, 0, 0, this);
        }
    }

    // This is called when repaint() is executed
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackgroundImage(g);
        for (MyShape myShape : myShapes) {
            myShape.draw(g);
        }
    }
    public void setTool(String tool) {
        this.tool = tool;
    }

    public void setCurrentColor(Color color) {
        currentColor = color;
    }

    // The method dynamically creates a JTextField when the user wants to insert text onto the canvas.
    // This text field is configured to appear where the user clicks,
    // allowing them to type text directly at that location.
    private void createTextField(Point point) {
        isEditing = true;
        textField = new JTextField(10);
        textField.setFont(textFont);
        textField.setLocation(point);
        textField.setSize(textField.getPreferredSize());
        textField.addActionListener(e -> {
            System.out.println("Enter");
            try {
                addTextToCanvas();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                System.out.println("lose");
                try {
                    addTextToCanvasWhenLoseFocus();
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        add(textField);
        textField.requestFocusInWindow();
    }

    // Add the text in textField to canvas, it is triggered when user taps Enter.
    private void addTextToCanvas() throws RemoteException {
        System.out.println("addtext to canvas");
        if (textField == null || textField.getText().isEmpty()) {
            System.out.println("empty text field");
        }

        if (textField.getText().length() > 0) {
            System.out.println("second check"+textField.getText());
            Text textShape = new Text(currentColor, textField.getText(), textField.getLocation(), textFont);
//            shapes.add(textShape);
            sendDrawingToServer(textShape);
        }

        remove(textField);
        textField = null;
        isEditing = false;
        repaint();
    }

    private void addTextToCanvasWhenLoseFocus() throws RemoteException {
        // if textfield is still being edited
        if(isEditing) {
            addTextToCanvas();
        }
    }
    public void setEraserSize(int setSize) {
        eraserSize = setSize;
    }

    public void updateShapes() throws RemoteException {
        myShapes = whiteBoard.getShapes();
        repaint();
    }

    public void displayImage(BufferedImage image) {
        currentImage = image;
        this.repaint();  // Repaint the panel to show the new image
    }


}