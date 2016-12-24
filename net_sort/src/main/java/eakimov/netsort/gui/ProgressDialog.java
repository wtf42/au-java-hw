package eakimov.netsort.gui;

import eakimov.netsort.ProgressHandler;

import javax.swing.*;
import java.awt.event.*;

public class ProgressDialog extends JDialog implements ProgressHandler {
    private JPanel contentPane;
    private JButton buttonCancel;
    private JProgressBar progressBar;
    private JTextPane progressTextPane;
    private final int begin, end;

    public ProgressDialog(int begin, int end) {
        setContentPane(contentPane);
        setModal(true);

        setTitle("progress");
        this.begin = begin;
        this.end = end;

        progressBar.setMinimum(begin);
        progressBar.setMaximum(end);
        setProgress(begin);

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public void setProgress(int value) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            String progressMessage = String.format("%.0f%% completed [from %d to %d: %d]",
                    100.0 * (value - begin) / (end - begin), begin, end, value);
            progressTextPane.setText(progressMessage);
        });
    }

    public void setCompleted() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progressBar.getMaximum());
            dispose();
        });
    }

    private void onCancel() {
        // TODO:???
        dispose();
    }
}
