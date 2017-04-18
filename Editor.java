package editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.scene.control.ScrollBar;
import javafx.geometry.Orientation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
* Double Linked List for text
* 
* ArrayList for dispalying text positions
* ArrayList of pointers to first Node elements of lines
*/


public class Editor extends Application {
    private static final int STARTING_FONT_SIZE = 12;
    private static final int STARTING_X = 5;
    private static final int STARTING_Y = 0;
    private static final int MARGIN = 19;
    private static final int STARTING_WIDTH = 500;
    private static final int STARTING_HEIGHT = 500;

    private boolean debug;
    private final int IMAGESIZE = 100;

    private ImageView bug1;
    private ImageView bug2;
    private ImageView bug3;

    private int bugsQuashed = 0;

    private double bug1x; private double bug1y;
    private double bug1opacity;
    private double bug2x; private double bug2y;
    private double bug2opacity;
    private double bug3x; private double bug3y;
    private double bug3opacity;


    private Group root;
    private Group textRoot;

    private final Rectangle cursorRectangle;

    private ScrollBar scrollBar;

    private UndoRedo undoredo;

    private LinkedList<Text> text;
    private ArrayList<LinkedList.Node> nodesAtLineStart;

    private int width;
    private int height;
    private int fontSize;
    private int lineSize;

    private String fileName;
    private File file;

    private String fontName = "Verdana";

    public Editor() {
        cursorRectangle = new Rectangle(1, STARTING_FONT_SIZE);
        // Creates double linked list of characters
        //text = new LinkedList<Text>();
        nodesAtLineStart = new ArrayList<>();

        width = STARTING_WIDTH;
        height = STARTING_HEIGHT;
        fontSize = STARTING_FONT_SIZE;
        lineSize = getLineSize();
    }

    /** An EventHandler to handle keys that get pressed. */
    private class KeyEventHandler implements EventHandler<KeyEvent> {

        //private Group root;

        public KeyEventHandler(Group root) {
            //this.root = root;
            updateDisplay();
        }

        @Override
        public void handle(KeyEvent keyEvent) {

            if (debug) {
                boolean flag = false;
                bug1opacity -= .01;
                bug2opacity -= .01;
                bug3opacity -= .01;
                if (bug1opacity < 0) {
                    flag = true;
                    bug1opacity = .99;
                    bug1x = Math.random(); bug1y = Math.random();
                    bug1.setX((width - IMAGESIZE) * bug1x);
                    bug1.setY((height - IMAGESIZE) * bug1y);
                }
                if (bug2opacity < 0) {
                    flag = true;
                    bug2opacity = .99;
                    bug2x = Math.random(); bug2y = Math.random();
                    bug2.setX((width - IMAGESIZE) * bug2x);
                    bug2.setY((height - IMAGESIZE) * bug2y);
                }
                if (bug3opacity < 0) {
                    flag = true;
                    bug3opacity = .99;
                    bug3x = Math.random(); bug3y = Math.random();
                    bug3.setX((width - IMAGESIZE) * bug3x);
                    bug3.setY((height - IMAGESIZE) * bug3y);
                }
                if (flag) {
                    bugsQuashed += 1;
                    System.out.println("Bugs quashed: " + bugsQuashed);
                    if (bugsQuashed % 5 == 0) {
                        System.out.println("Your GPA increased from quashing so many bugs!");
                        System.out.println("GPA: " + (3 + bugsQuashed*.002));
                        if (Math.abs(bugsQuashed*.002 - .3) < .00001) {
                            System.out.println("You qualify for the CS major!!!");
                            System.out.println("You can keep debugging if you want to.");
                        }
                    }
                }
                bug1.setOpacity(Math.sqrt(bug1opacity));
                bug2.setOpacity(Math.sqrt(bug2opacity));
                bug3.setOpacity(Math.sqrt(bug3opacity));
            }

            boolean adjustScrollBarHeight = false;
            boolean adjustScrollBarPosition = false;
            boolean checkScrollBarExceedsMax = false;

            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                // the KEY_TYPED event, javafx handles the "Shift" key and associated
                // capitalization.
                String characterTyped = keyEvent.getCharacter();
                if (!keyEvent.isShortcutDown()) {
                    if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8
                        && characterTyped.charAt(0) != 13 && characterTyped.charAt(0) != 127) {
                        // Ignore control keys, which have non-zero length, as well as the backspace key,
                        // which represented as a character of value = 8 on Windows.
                        Text t = new Text(0, 0, characterTyped);
                        t.setTextOrigin(VPos.TOP);
                        t.setFont(Font.font(fontName, fontSize));
                        textRoot.getChildren().add(t);
                        t.toFront();
                        text.add(t);

                        undoredo.add(text.cursor.prev, UndoRedo.INSERT, text.cursor);

                        keyEvent.consume();
                        updateDisplay();

                        adjustScrollBarHeight = true;
                        adjustScrollBarPosition = true;
                    }
                }

            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                if (keyEvent.isShortcutDown()) {
                    if (keyEvent.getCode() == KeyCode.PLUS || keyEvent.getCode() == KeyCode.EQUALS) {
                        fontSize += 4;
                        for (Text t : text) {
                            t.setFont(Font.font(fontName, fontSize));
                        }
                        updateDisplay();
                        adjustScrollBarHeight = true;

                    } else if (keyEvent.getCode() == KeyCode.MINUS) {
                        if (fontSize > 4) {
                            fontSize -= 4;
                            for (Text t : text) {
                                t.setFont(Font.font(fontName, fontSize));
                            }
                            updateDisplay();
                            adjustScrollBarHeight = true;
                            checkScrollBarExceedsMax = true;
                        }
                    } else if (keyEvent.getCode() == KeyCode.P) {
                        printCursorLocation();
                    } else if (keyEvent.getCode() == KeyCode.S) {
                        saveFile();

                    } else if (keyEvent.getCode() == KeyCode.Z) {
                        Text temp = undoredo.undo();
                        if (temp != null) {
                            temp.setFont(Font.font(fontName, fontSize));
                        }
                        updateDisplay();
                        adjustScrollBarHeight = true;
                        adjustScrollBarPosition = true;
                    } else if (keyEvent.getCode() == KeyCode.Y) {
                        Text temp = undoredo.redo();
                        if (temp != null) {
                            temp.setFont(Font.font(fontName, fontSize));
                        }
                        updateDisplay();
                        adjustScrollBarHeight = true;
                        adjustScrollBarPosition = true;
                    }
                } else {
                    if (code == KeyCode.BACK_SPACE) {
                        LinkedList.Node tempNode = text.cursor.prev;
                        Text temp = text.remove();

                        if (temp != null) {
                            textRoot.getChildren().remove(temp);

                            undoredo.add(tempNode, UndoRedo.REMOVE, text.cursor);

                            updateDisplay();
                            adjustScrollBarHeight = true;
                            adjustScrollBarPosition = true;
                            checkScrollBarExceedsMax = true;
                        }
                    }
                    if (code == KeyCode.LEFT) {
                        text.moveCursorLeft();
                        updateCursor();
                        adjustScrollBarHeight = true;
                        adjustScrollBarPosition = true;
                    }
                    if (code == KeyCode.RIGHT) {
                        text.moveCursorRight();
                        updateCursor();
                        adjustScrollBarHeight = true;
                        adjustScrollBarPosition = true;
                    }
                    if (code == KeyCode.DOWN) {
                        int lineNum = ((int) cursorRectangle.getY()) / lineSize;
                        setCursorNode(cursorRectangle.getX(), lineNum + 1);
                        adjustScrollBarHeight = true;
                        adjustScrollBarPosition = true;
                    }
                    if (code == KeyCode.UP) {
                        int lineNum = ((int) cursorRectangle.getY()) / lineSize;
                        if (lineNum > 0) {
                            setCursorNode(cursorRectangle.getX(), lineNum - 1);
                        }
                        adjustScrollBarHeight = true;
                        adjustScrollBarPosition = true;
                    }
                    if (code == KeyCode.ENTER) {
                        Text t = new Text(0, 0, "\n");
                        text.add(t);
                        updateDisplay();
                        adjustScrollBarHeight = true;
                        adjustScrollBarPosition = true;
                    }
                }
            }
            if (adjustScrollBarHeight) {
                scrollBar.setMax(Math.max(getTextHeight() - height, 0));
            }
            if (adjustScrollBarPosition) {
                if (cursorRectangle.getY() - scrollBar.getValue() < 0) {
                    scrollBar.setValue(cursorRectangle.getY());
                } else if (cursorRectangle.getY() + lineSize - scrollBar.getValue() > height) {
                    scrollBar.setValue(cursorRectangle.getY() + lineSize - height);
                }
            }
            if (checkScrollBarExceedsMax) {
                if (scrollBar.getValue() > scrollBar.getMax()) {
                    scrollBar.setValue(scrollBar.getMax());
                }
            }
        }
    }

    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            double xVal = mouseEvent.getX();
            int lineNumber = (int) ((mouseEvent.getY() + scrollBar.getValue()) / lineSize);
            setCursorNode(xVal, lineNumber);
        }
    }

    private void saveFile() {
        try {
            FileWriter writer = new FileWriter(file);
            Iterator<Text> textObjects = text.iterator();
            Text currentText = textObjects.next();

            while (currentText != null) {
                String character = currentText.getText();
                if (character.equals("\n")) {
                    writer.write('\r');
                }
                writer.write(character.charAt(0));
                currentText = textObjects.next();
            }
            writer.close();
            System.out.println("Saved file: " + fileName);
        } catch (IOException ioException) {
            System.out.println("Error when saving: " + ioException);
        }
    }

    private void setCursorNode(double xVal, int lineNumber) {
        if (nodesAtLineStart.size() > 0) {
            if (lineNumber < nodesAtLineStart.size()) {
                findCursorPosInLine(xVal, lineNumber);
            } else {
                if (text.getBack().getText().equals("\n")) {
                    text.moveCursorToBack();
                    cursorRectangle.setX(STARTING_X);
                    cursorRectangle.setY(text.getBack().getY() + lineSize);
                } else {
                    findCursorPosInLine(xVal, nodesAtLineStart.size() - 1);
                }
            }
        }
    }

    private void findCursorPosInLine(double xVal, int lineNumber) {
        text.setCursorNode(nodesAtLineStart.get(lineNumber));
        boolean cont = true;
        while (cont) {
            Text a = text.getCurrentItem();
            text.moveCursorRight();
            Text b = text.getCurrentItem();
            if (b == null) {
                cont = false;
                if (xVal < (a.getX() + (a.getLayoutBounds().getWidth() / 2)) ||
                    a.getText().equals("\n") || a.getText().equals(" ")) {
                    text.moveCursorLeft();
                    updateCursor();
                } else {
                    cursorRectangle.setX((int) (a.getX() + a.getLayoutBounds().getWidth() + .5));
                    cursorRectangle.setY(a.getY());
                }
            } else if (b.getY() / lineSize > lineNumber) {
                cont = false;
                if (xVal < (a.getX() + (a.getLayoutBounds().getWidth() / 2)) ||
                    a.getText().equals("\n") || a.getText().equals(" ")) {
                    text.moveCursorLeft();
                    updateCursor();
                } else {
                    cursorRectangle.setX((int) (a.getX() + a.getLayoutBounds().getWidth() + .5));
                    cursorRectangle.setY(a.getY());
                }
            }
            else {
                double ax = a.getX();
                double bx = b.getX();
                if (Math.abs(ax - xVal) < Math.abs(bx - xVal)) {
                    cont = false;
                    text.moveCursorLeft();
                    updateCursor();
                }
            }
        }
    }

    private int getLineSize() {
        Text temp = new Text(0, 0, " ");
        temp.setFont(Font.font(fontName, fontSize));
        return (int) (temp.getLayoutBounds().getHeight() + .5);
    }

    private int getTextHeight() {
        int textHeight = nodesAtLineStart.size() * lineSize;
        if (text.getBack() != null && ((Text) text.getBack()).getText().equals("\n")) {
            textHeight += lineSize;
        }
        return textHeight;
    }

    private void printCursorLocation() {
        int x = (int) cursorRectangle.getX(), y = (int) cursorRectangle.getY();
        System.out.println(x + ", " + y);
    }

    private void updateCursor() {
        Text cursorTextObject = text.cursor.item;
        if (cursorTextObject != null) {
            cursorRectangle.setX(cursorTextObject.getX());
            cursorRectangle.setY(cursorTextObject.getY());
        } else {
            Text temp = text.getBack();
            if (temp != null) {
                if (temp.getText().equals("\n")) {
                    cursorRectangle.setX(STARTING_X);
                    cursorRectangle.setY(((int) temp.getY()) + lineSize);
                } else {
                    int x = (int) (temp.getX() + temp.getLayoutBounds().getWidth() + .5);
                    x = Math.min(x, width - MARGIN);
                    cursorRectangle.setX(x);
                    cursorRectangle.setY(temp.getY());
                }
            }
            else {
                cursorRectangle.setX(STARTING_X);
                cursorRectangle.setY(STARTING_Y);
            }
        }
    }

    private void updateDisplay() {
        lineSize = getLineSize();
        Text cursorTextObject = new Text();
        int currentX = STARTING_X;
        int currentY = STARTING_Y;
        boolean drawCursorAtEnd = true;
        boolean currentWordStartsLine = true;
        nodesAtLineStart = new ArrayList<>();

        Iterator<Text> textObjects = text.iterator();
        Text currentText = textObjects.next();

        LinkedList.Node currentWordStartNode = ((LinkedList.LinkedListIterator) textObjects).currentNode();
        ArrayList<Text> currentWordTextBoxes = new ArrayList<>();
        ArrayList<Integer> currentWordXDisplacements = new ArrayList<>();

        while (currentText != null) {
            currentText.setX(currentX);
            currentText.setY(currentY);

            if (currentX == STARTING_X) {
                nodesAtLineStart.add(((LinkedList.LinkedListIterator) textObjects).currentNode());
            }

            int characterWidth = (int) (currentText.getLayoutBounds().getWidth() + .5);
            currentX += characterWidth;

            String currLetter = currentText.getText();
            if (currLetter.equals(" ") || currLetter.equals("\n")) {

                currentWordStartNode = null;
                currentWordTextBoxes = new ArrayList<>();
                currentWordXDisplacements = new ArrayList<>();

                if (currLetter.equals(" ")) {
                    currentWordStartsLine = false;
                } else {
                    currentWordStartsLine = true;
                    currentX = STARTING_X;
                    currentY += lineSize;
                }

            } else {
                if (currentWordStartsLine) {
                    if (currentX > width - MARGIN) {
                        currentX = STARTING_X;
                        currentY += lineSize;
                        currentText.setX(currentX);
                        currentText.setY(currentY);
                        nodesAtLineStart.add(((LinkedList.LinkedListIterator) textObjects).currentNode());
                        currentX += (int) (currentText.getLayoutBounds().getWidth() + .5);
                    }
                } else {
                    if (currentWordTextBoxes.size() == 0) {
                        currentWordStartNode = ((LinkedList.LinkedListIterator) textObjects).currentNode();
                    }
                    currentWordTextBoxes.add(currentText);
                    currentWordXDisplacements.add(characterWidth);
                    if (currentX > width - MARGIN) {
                        currentY += lineSize;
                        currentX = STARTING_X;
                        nodesAtLineStart.add(currentWordStartNode);
                        currentWordStartsLine = true;
                        for (int i = 0; i < currentWordTextBoxes.size(); i++) {
                            currentWordTextBoxes.get(i).setX(currentX);
                            currentWordTextBoxes.get(i).setY(currentY);
                            currentX += currentWordXDisplacements.get(i);
                        }
                    }
                }
            }
            if (((LinkedList.LinkedListIterator) textObjects).isNextCursorNode()) {
                drawCursorAtEnd = false;
                cursorTextObject = currentText;
            }

            currentText = textObjects.next();
        }

        if (drawCursorAtEnd) {
            cursorRectangle.setX(Math.min(currentX, width - MARGIN));
            cursorRectangle.setY(currentY);
            cursorRectangle.setHeight(lineSize);
        } else {
            int xVal = (int) cursorTextObject.getX();
            xVal = Math.min(xVal, width - MARGIN);
            cursorRectangle.setX(xVal);
            cursorRectangle.setY(cursorTextObject.getY());
            cursorRectangle.setHeight(lineSize);
        }
    }

    private class CursorBlinkEventHandler implements EventHandler<ActionEvent> {
        private int visible = 1;

        private void CursorBlinkEventHandler() {
            changeVisible();
        }

        private void changeVisible() {
            if (visible == 1) {
                cursorRectangle.setFill(Color.BLACK);
            } else {
                cursorRectangle.setFill(null);
            }
            visible = 1 - visible;
        }

        @Override public void handle(ActionEvent event) {
            changeVisible();
        }
    }

    private void makeCursorBlink() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        CursorBlinkEventHandler cursorChange = new CursorBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    @Override
    public void start(Stage primaryStage) {
        // Create a Node that will be the parent of all things displayed on the screen.
        root = new Group();
        textRoot = new Group();

        text = new LinkedList<Text>();
        List<String> commandLineArgs = getParameters().getRaw();
        if (commandLineArgs.size() == 0) {
            System.out.println("Error: No filename provided");
            System.exit(1);
        }
        fileName = commandLineArgs.get(0);

        try {
            file = new File(fileName);

            if (file.exists()) {
                if (file.isDirectory()) {
                    System.out.println("Error: Unable to open file: " + fileName);
                    System.exit(1);
                }
                System.out.println("Attempting to open file: " + fileName);
                FileReader reader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(reader);
                int intRead = -1;

                while ((intRead = bufferedReader.read()) != -1) {
                    String charRead = Character.toString((char) intRead);
                    if (!charRead.equals("\r")) {
                        Text t = new Text(0, 0, charRead);
                        t.setTextOrigin(VPos.TOP);
                        t.setFont(Font.font(fontName, fontSize));
                        textRoot.getChildren().add(t);
                        t.toFront();
                        text.add(t);
                    }
                }
                bufferedReader.close();
            }
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File not found " + fileNotFoundException);
        } catch (IOException ioException) {
            System.out.println("oops " + ioException);
        }

        debug = false;

        if (commandLineArgs.size() >= 2) {
            if (commandLineArgs.get(1).equals("debug")) {
                debug = true;
                System.out.println("TYPE THINGS TO DEBUG");
                System.out.println("GPA: 3.00");
            }
        }

        undoredo = new UndoRedo(text, textRoot);

        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.
        int windowWidth = STARTING_WIDTH;
        int windowHeight = STARTING_HEIGHT;
        Scene scene = new Scene(root, windowWidth, windowHeight, Color.WHITE);

        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root);
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        scene.setOnMouseClicked(new MouseClickEventHandler());

        // Adds the cursor to the screen
        textRoot.getChildren().add(cursorRectangle);

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenWidth,
                    Number newScreenWidth) {
                width = newScreenWidth.intValue();
                scrollBar.setLayoutX((int) (width - scrollBar.getLayoutBounds().getWidth() + .5));
                if (debug) {
                    bug1.setX((width - IMAGESIZE) * bug1x);
                    bug2.setX((width - IMAGESIZE) * bug2x);
                    bug3.setX((width - IMAGESIZE) * bug3x);
                }
                updateDisplay();
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenHeight,
                    Number newScreenHeight) {
                height = newScreenHeight.intValue();
                scrollBar.setPrefHeight(height);
                if (debug) {
                    bug1.setY((height - IMAGESIZE) * bug1y);
                    bug2.setY((height - IMAGESIZE) * bug2y);
                    bug3.setY((height - IMAGESIZE) * bug3y);
                }
                updateDisplay();
            }
        });

        scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setPrefHeight(STARTING_HEIGHT);
        scrollBar.setMin(0);
        scrollBar.setMax(Math.max(getTextHeight() - STARTING_HEIGHT, 0));

        scrollBar.setLayoutX((int) (STARTING_WIDTH - 15));

        root.getChildren().add(scrollBar);


        scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                ObservableValue<? extends Number> observableValue,
                Number oldValue,
                Number newValue) {
                textRoot.setLayoutY(-newValue.doubleValue());
            }
        });

        makeCursorBlink();

        if (debug) {
            bug1 = new ImageView(new Image("editor/bug1.png"));
            bug2 = new ImageView(new Image("editor/bug2.png"));
            bug3 = new ImageView(new Image("editor/bug3.png"));
            bug1.setFitHeight(IMAGESIZE);
            bug1.setFitWidth(IMAGESIZE);
            bug2.setFitHeight(IMAGESIZE);
            bug2.setFitWidth(IMAGESIZE);
            bug3.setFitHeight(IMAGESIZE);
            bug3.setFitWidth(IMAGESIZE);
            bug1x = Math.random(); bug1y = Math.random();
            bug2x = Math.random(); bug2y = Math.random();
            bug3x = Math.random(); bug3y = Math.random();
            bug1opacity = .99;
            bug2opacity = .66;
            bug3opacity = .33;
            bug1.setX((width - IMAGESIZE) * bug1x);
            bug1.setY((height - IMAGESIZE) * bug1y);
            bug2.setX((width - IMAGESIZE) * bug2x);
            bug2.setY((height - IMAGESIZE) * bug2y);
            bug3.setX((width - IMAGESIZE) * bug3x);
            bug3.setY((height - IMAGESIZE) * bug3y);
            bug1.setOpacity(bug1opacity);
            bug2.setOpacity(bug2opacity);
            bug3.setOpacity(bug3opacity);
            root.getChildren().add(bug1);
            root.getChildren().add(bug2);
            root.getChildren().add(bug3);
        }

        root.getChildren().add(textRoot);

        // All new Nodes need to be added to the root in order to be displayed.

        primaryStage.setTitle("Editor");

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();

        updateDisplay();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
