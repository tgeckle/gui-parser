
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.*;
import java.util.ArrayDeque;
import javax.swing.*;


/**
 * Filename: Parser.java
 Author: Theresa Geckle
 Date: Nov 14, 2016
 Purpose: Uses recursive descent parsing to parse a GUI and it's constituent 
 * parts including layouts, panels, and buttons from a particular grammar. In
 * the case of a syntax error, the invalid token and its location in the input 
 * file are printed to the standard output. 
 */
public class Parser {
    public Lexer lex;
    public Token currToken;
    public JFrame frame; 
    public JPanel curPanel; // keeps track of which panel is currently having components added to it
    
    public Parser(String fileName) throws FileNotFoundException, SyntaxError, IOException {
        lex = new Lexer(fileName);
        currToken = lex.getNextToken();
        frame = new JFrame();
        
        if (parseGUI()) {
        }
    }
    
    
    public boolean parseGUI() throws SyntaxError, IOException {
        String title;
        int width;
        int height;
        if (currToken == Token.WINDOW) {
            currToken = lex.getNextToken();
            if (currToken == Token.STRING) {
                title = lex.getLexeme();
                currToken = lex.getNextToken();
                if (currToken == Token.LEFT_PAREN) {
                    currToken = lex.getNextToken();
                    if (currToken == Token.NUMBER) {
                        width = (int) lex.getValue();
                        currToken = lex.getNextToken();
                        if (currToken == Token.COMMA) {
                            currToken = lex.getNextToken();
                            if (currToken == Token.NUMBER) {
                                height = (int)lex.getValue();
                                currToken = lex.getNextToken();
                                if (currToken == Token.RIGHT_PAREN) {
                                    currToken = lex.getNextToken();
                                    frame.setTitle(title);
                                    if (parseLayout()) {
                                        if (parseWidgets()) {
                                            if (currToken == Token.END) {
                                                currToken = lex.getNextToken();
                                                if (currToken == Token.PERIOD) { // Marks end of input
                                                    frame.setLocationRelativeTo(null);
                                                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                                    frame.pack();
                                                    frame.setVisible(true);
                                                    frame.setSize(width, height);
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    // Recursively parses 1+ widget(s)
    public boolean parseWidgets() throws SyntaxError, IOException {
        if (parseWidget()) {
            if (!parseWidgets()) {
            }
            
            return true;
        }
        return false;
    }
    
    // Parses input for a widget to be added to the GUI
    public boolean parseWidget() throws SyntaxError, IOException {
        if (null != currToken) {
            switch (currToken) {
                case BUTTON:
                    if (parseButton()) {
                        return true;
                    }
                    break;
                case GROUP:
                    if (parseGroup()) {
                        return true;
                    }
                    break;
                case LABEL:
                    if (parseLabel()) {
                        return true;
                    }
                    break;
                case PANEL:
                    if (parsePanel()) {
                        return true;
                    }
                    break;
                case TEXTFIELD:
                    if (parseTextfield()) {
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }

        return false;
    }

    // Parses input to add a panel to the GUI
    public boolean parsePanel() throws SyntaxError, IOException {
        // Keeps track of current panel prior to parsing of new panel input, so
        // that it can be reverted to once the parsing is done. 
        JPanel prevPanel = curPanel;
        

        if (currToken == Token.PANEL) {
            curPanel = new JPanel();
            currToken = lex.getNextToken();
            if (parseLayout()) {
                while (parseWidget()) {
                }
                if (currToken == Token.END) {
                    currToken = lex.getNextToken();
                    if (currToken == Token.SEMICOLON) {
                        if (prevPanel == null) {
                            frame.add(curPanel);
                            curPanel = null;
                        }
                        else {
                            prevPanel.add(curPanel);
                        }
                        
                        curPanel = prevPanel;
                        currToken = lex.getNextToken();
                        return true;
                    }
                }
            }

        }

        return false;
    }
    
    // Parses the input to define a Layout of type GridLayout or FlowLayout
    public boolean parseLayout() throws SyntaxError, IOException {
        if (currToken == Token.LAYOUT) {
            currToken = lex.getNextToken();
            if (parseFlow()) {
                return true;
            }
            
            else if (parseGrid()) {
                return true;
            }
        }
        
        return false;
    }
    
    // Parses the input to define a FlowLayout for the currently selected panel
    public boolean parseFlow() throws SyntaxError, IOException {
        if (currToken == Token.FLOW) {
            currToken = lex.getNextToken();
            if (currToken == Token.COLON) {
                if (curPanel == null) {
                    frame.setLayout(new FlowLayout());
                }
                else {
                    curPanel.setLayout(new FlowLayout());
                }
                currToken = lex.getNextToken();
                return true;
            }
        }

        return false;
    }
    
    // Parses the input to define a GridLayout for the currently selected panel
    public boolean parseGrid() throws SyntaxError, IOException {
        ArrayDeque<Integer> parameters;
        if (currToken == Token.GRID) {
            parameters = new ArrayDeque<>();
            currToken = lex.getNextToken();
            if (currToken == Token.LEFT_PAREN) {
                currToken = lex.getNextToken();
                if (currToken == Token.NUMBER) {
                    parameters.add(new Integer((int) lex.getValue()));
                    currToken = lex.getNextToken();
                    if (currToken == Token.COMMA) {
                        currToken = lex.getNextToken();
                        if (currToken == Token.NUMBER) {
                            parameters.add(new Integer((int) lex.getValue()));
                            currToken = lex.getNextToken();
                            if (currToken == Token.COMMA) {
                                currToken = lex.getNextToken();
                                if (currToken == Token.NUMBER) {
                                    parameters.add(new Integer((int) lex.getValue()));
                                    currToken = lex.getNextToken();
                                    if (currToken == Token.COMMA) {
                                        currToken = lex.getNextToken();
                                        if (currToken == Token.NUMBER) {
                                            parameters.add(new Integer((int) lex.getValue()));
                                            currToken = lex.getNextToken();
                                            if (currToken == Token.RIGHT_PAREN) {
                                                currToken = lex.getNextToken();
                                                if (currToken == Token.COLON) {
                                                    currToken = lex.getNextToken();
                                                    if (curPanel == null) {
                                                        frame.setLayout(new GridLayout(
                                                                parameters.removeFirst(),
                                                                parameters.removeFirst(),
                                                                parameters.removeFirst(),
                                                                parameters.removeFirst()));
                                                    } else {
                                                        curPanel.setLayout(new GridLayout(
                                                                parameters.removeFirst(),
                                                                parameters.removeFirst(),
                                                                parameters.removeFirst(),
                                                                parameters.removeFirst()));
                                                    }
                                                    
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                }
                            } 
                            else if (currToken == Token.RIGHT_PAREN) {
                                currToken = lex.getNextToken();
                                if (currToken == Token.COLON) {
                                    if (curPanel == null) {
                                        frame.setLayout(new GridLayout(
                                                parameters.removeFirst(),
                                                parameters.removeFirst()));
                                    } else {
                                        curPanel.setLayout(new GridLayout(
                                                parameters.removeFirst(),
                                                parameters.removeFirst()));
                                    }
                                    currToken = lex.getNextToken();
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    // Parses a line of input representing a GUI button with a given String as text. 
    public boolean parseButton() throws SyntaxError, IOException {
        String label;
        if (currToken == Token.BUTTON) {
            currToken = lex.getNextToken();
            if (currToken == Token.STRING) {
                label = lex.getLexeme();
                currToken = lex.getNextToken();
                if (currToken == Token.SEMICOLON) {
                    currToken = lex.getNextToken();
                    if (curPanel == null) {
                        frame.add(new JButton(label));
                    } 
                    else {
                        curPanel.add(new JButton(label));
                    }
                    return true;
                }
            }
        }
        
        return false;
    }
    
    //Recursively parses 1+ radio button(s)
    public boolean parseRadios(ArrayDeque<JRadioButton> queue) throws SyntaxError, IOException {
        if (parseRadio(queue)) {
            if (!parseRadios(queue)) {
            }
            return true;
        }
        return false;
    }
    
    // Parses a signle radio button and adds it to a queue that is kept track of inside
    // ParseGroup. 
    public boolean parseRadio(ArrayDeque<JRadioButton> queue) throws SyntaxError, IOException {
        if (currToken == Token.RADIO) {
            currToken = lex.getNextToken();
            if (currToken == Token.STRING) {
                queue.add(new JRadioButton(lex.getLexeme()));
                currToken = lex.getNextToken();
                if (currToken == Token.SEMICOLON) {
                    currToken = lex.getNextToken();
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // Parses a ButtonGroup - a group of radio buttons. Keeps track of the 
    // Individual buttons inside a queue
    public boolean parseGroup() throws SyntaxError, IOException {
        ArrayDeque<JRadioButton> queue = new ArrayDeque<>();
        ButtonGroup group;
        if (currToken == Token.GROUP) {
            group = new ButtonGroup();
            currToken = lex.getNextToken();

            if (parseRadios(queue)) {
                while (!queue.isEmpty()) {
                    JRadioButton radio = queue.removeFirst();
                    group.add(radio);

                    if (curPanel == null) {
                        frame.add(radio);
                    } 
                    else {
                        curPanel.add(radio);
                    }
                }

                if (currToken == Token.END) {
                    currToken = lex.getNextToken();
                    if (currToken == Token.SEMICOLON) {
                        currToken = lex.getNextToken();
                        return true;
                    }
                }
            }
        }

        return false;
    }
    
    // Parses a line of input representing a JLabel with a given String as text.
    public boolean parseLabel() throws SyntaxError, IOException {
        String text;
        
        if (currToken == Token.LABEL) {
            currToken = lex.getNextToken();
            if (currToken == Token.STRING) {
                text = lex.getLexeme();
                currToken = lex.getNextToken();
                if (currToken == Token.SEMICOLON) {
                    if (curPanel == null) {
                        frame.add(new JLabel(text));
                    }
                    else {
                        curPanel.add(new JLabel(text));
                    }
                    currToken = lex.getNextToken();
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // Parses a line of input representing a JTextField, with a given number 
    // representing the width fo the field.
    public boolean parseTextfield() throws SyntaxError, IOException {
        int number;
        
        if (currToken == Token.TEXTFIELD) {
            currToken = lex.getNextToken();
            if (currToken == Token.NUMBER) {
                number = (int)lex.getValue();
                currToken = lex.getNextToken();
                if (currToken == Token.SEMICOLON) {
                    if (curPanel != null) {
                        curPanel.add(new JTextField(number));
                    }
                    else {
                        frame.add(new JTextField(number));
                    }
                    currToken = lex.getNextToken();
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Parser parse = new Parser("testinput4.txt");
        }
        catch (FileNotFoundException exc) {
            System.out.println(exc);
        }
        catch (SyntaxError exc) {
            System.out.println(exc);
        }
        catch (IOException exc) {
            System.out.println(exc);
        }
    }

}
