import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

public class ClientGuiView {
    private final ClientGuiController controller;

    private JFrame frame = new JFrame("С подключением!))");
    private JTextField textField = new JTextField(50);
    private JTextArea messages = new JTextArea(10, 50);
    private JTextArea users = new JTextArea(10, 13);

    public ClientGuiView(ClientGuiController controller) {
        this.controller = controller;
        initView();
    }

    private void initView() {
        textField.setEditable(false);
        messages.setEditable(true);
        users.setEditable(false);

        frame.getContentPane().add(textField, BorderLayout.NORTH);
        JScrollPane jScroller = new JScrollPane(messages);
        SmartScroller smartScroller = new SmartScroller(jScroller);
        frame.getContentPane().add(jScroller, BorderLayout.WEST);
        frame.getContentPane().add(new JScrollPane(users), BorderLayout.EAST);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setSize(600,400);
        

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.sendTextMessage(textField.getText());
                textField.setText("");
            }
        });
    }

    public String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter server's address:",
                "Ещё не подключены...",
                JOptionPane.QUESTION_MESSAGE);
    }

    public int getServerPort() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frame,
                    "Please enter server's port:",
                    "Ещё не подключены...",
                    JOptionPane.QUESTION_MESSAGE);
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        frame,
                        "A incorrect server's port was entered. Please try again.",
                        "Ещё не подключены...",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public String getUserName() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter your nickname:",
                "Ещё не подключены...",
                JOptionPane.QUESTION_MESSAGE);
    }

    public void notifyConnectionStatusChanged(boolean clientConnected) {
        textField.setEditable(clientConnected);
        if (clientConnected) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Соединение с сервером установлено",
                    "С подключением!)",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "С подключением!)",
                    "С подключением!))0)",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    public void refreshMessages() {
        messages.append(controller.getModel().getNewMessage() + "\n");
    }

    public void refreshUsers() {
        ClientGuiModel model = controller.getModel();
        StringBuilder sb = new StringBuilder();
        for (String userName : model.getAllUserNames()) {
            sb.append(userName).append("\n");
        }
        users.setText(sb.toString());
    }
}
class SmartScroller implements AdjustmentListener
{
    public final static int HORIZONTAL = 0;
    public final static int VERTICAL = 1;

    public final static int START = 0;
    public final static int END = 1;

    private int viewportPosition;

    private JScrollBar scrollBar;
    private boolean adjustScrollBar = true;

    private int previousValue = -1;
    private int previousMaximum = -1;

    /**
     * Convenience constructor.
     * Scroll direction is VERTICAL and viewport position is at the END.
     *
     * @param scrollPane the scroll pane to monitor
     */
    public SmartScroller (JScrollPane scrollPane)
    {
        this(scrollPane, VERTICAL, END);
    }

    /**
     * Convenience constructor.
     * Scroll direction is VERTICAL.
     *
     * @param scrollPane the scroll pane to monitor
     * @param viewportPosition valid values are START and END
     */
    public SmartScroller (JScrollPane scrollPane, int viewportPosition)
    {
        this(scrollPane, VERTICAL, viewportPosition);
    }

    /**
     * Specify how the SmartScroller will function.
     *
     * @param scrollPane the scroll pane to monitor
     * @param scrollDirection indicates which JScrollBar to monitor.
     *        Valid values are HORIZONTAL and VERTICAL.
     * @param viewportPosition indicates where the viewport will normally be
     *        positioned as data is added.
     *        Valid values are START and END
     */
    public SmartScroller (JScrollPane scrollPane, int scrollDirection, int viewportPosition)
    {
        if (scrollDirection != HORIZONTAL && scrollDirection != VERTICAL)
        {
            throw new IllegalArgumentException("invalid scroll direction specified");
        }

        if (viewportPosition != START && viewportPosition != END)
        {
            throw new IllegalArgumentException("invalid viewport position specified");
        }

        this.viewportPosition = viewportPosition;

        if (scrollDirection == HORIZONTAL)
        {
            scrollBar = scrollPane.getHorizontalScrollBar();
        }
        else
        {
            scrollBar = scrollPane.getVerticalScrollBar();
        }

        scrollBar.addAdjustmentListener(this);

        // Turn off automatic scrolling for text components

        Component view = scrollPane.getViewport().getView();

        if (view instanceof JTextComponent)
        {
            JTextComponent textComponent = (JTextComponent)view;
            DefaultCaret caret = (DefaultCaret)textComponent.getCaret();
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }

    @Override
    public void adjustmentValueChanged (final AdjustmentEvent e)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run ()
            {
                checkScrollBar(e);
            }
        });
    }

    /*
     * Analyze every adjustment event to determine when the viewport
     * needs to be repositioned.
     */
    private void checkScrollBar (AdjustmentEvent e)
    {
        // The scroll bar listModel contains information needed to determine
        // whether the viewport should be repositioned or not.

        JScrollBar scrollBar = (JScrollBar)e.getSource();
        BoundedRangeModel listModel = scrollBar.getModel();
        int value = listModel.getValue();
        int extent = listModel.getExtent();
        int maximum = listModel.getMaximum();

        boolean valueChanged = previousValue != value;
        boolean maximumChanged = previousMaximum != maximum;

        // Check if the user has manually repositioned the scrollbar

        if (valueChanged && !maximumChanged)
        {
            if (viewportPosition == START)
            {
                adjustScrollBar = value != 0;
            }
            else
            {
                adjustScrollBar = value + extent >= maximum;
            }
        }

        // Reset the "value" so we can reposition the viewport and
        // distinguish between a user scroll and a program scroll.
        // (ie. valueChanged will be false on a program scroll)

        if (adjustScrollBar && viewportPosition == END)
        {
            // Scroll the viewport to the end.
            scrollBar.removeAdjustmentListener(this);
            value = maximum - extent;
            scrollBar.setValue(value);
            scrollBar.addAdjustmentListener(this);
        }

        if (adjustScrollBar && viewportPosition == START)
        {
            // Keep the viewport at the same relative viewportPosition
            scrollBar.removeAdjustmentListener(this);
            value = value + maximum - previousMaximum;
            scrollBar.setValue(value);
            scrollBar.addAdjustmentListener(this);
        }

        previousValue = value;
        previousMaximum = maximum;
    }
}









