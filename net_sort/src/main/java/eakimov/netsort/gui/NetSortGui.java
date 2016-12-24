package eakimov.netsort.gui;

import eakimov.netsort.NetSortRunner;
import eakimov.netsort.Results;
import eakimov.netsort.settings.RunSettings;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;

public class NetSortGui extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox archTypeCBox;
    private JRadioButton nRadioButton;
    private JRadioButton mRadioButton;
    private JRadioButton dRadioButton;
    private JSpinner nStepSpinner;
    private JSpinner mStepSpinner;
    private JSpinner dStepSpinner;
    private JSpinner xSpinner;
    private JSpinner nStartSpinner;
    private JSpinner nEndSpinner;
    private JSpinner mStartSpinner;
    private JSpinner mEndSpinner;
    private JSpinner dStartSpinner;
    private JSpinner dEndSpinner;

    public NetSortGui() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        setTitle("NetSort");

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

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

        final SelChangeListener selChangeListenerInstance = new SelChangeListener();
        nRadioButton.addChangeListener(selChangeListenerInstance);
        mRadioButton.addChangeListener(selChangeListenerInstance);
        dRadioButton.addChangeListener(selChangeListenerInstance);

        xSpinner.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        nStartSpinner.setModel(new SpinnerNumberModel(1000000, 1, 1000000000, 1));
        nEndSpinner.setModel(new SpinnerNumberModel(100000000, 1, 1000000000, 1));
        nStepSpinner.setModel(new SpinnerNumberModel(1000000, 1, 1000000000, 1));
        mStartSpinner.setModel(new SpinnerNumberModel(10, 1, 1000, 1));
        mEndSpinner.setModel(new SpinnerNumberModel(100, 1, 1000, 1));
        mStepSpinner.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        dStartSpinner.setModel(new SpinnerNumberModel(0, 0, 1000000000, 1));
        dEndSpinner.setModel(new SpinnerNumberModel(100, 1, 1000000000, 1));
        dStepSpinner.setModel(new SpinnerNumberModel(10, 1, 1000000000, 1));
    }

    private class SelChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            nEndSpinner.setEnabled(nRadioButton.isSelected());
            nStepSpinner.setEnabled(nRadioButton.isSelected());
            mEndSpinner.setEnabled(mRadioButton.isSelected());
            mStepSpinner.setEnabled(mRadioButton.isSelected());
            dEndSpinner.setEnabled(dRadioButton.isSelected());
            dStepSpinner.setEnabled(dRadioButton.isSelected());
        }
    }

    private void onOK() {
        final RunSettings settings = new RunSettings(
                archTypeCBox.getSelectedIndex(),
                nRadioButton.isSelected(),
                mRadioButton.isSelected(),
                dRadioButton.isSelected(),
                (Integer) xSpinner.getValue(),
                (Integer) nStartSpinner.getValue(),
                (Integer) nEndSpinner.getValue(),
                (Integer) nStepSpinner.getValue(),
                (Integer) mStartSpinner.getValue(),
                (Integer) mEndSpinner.getValue(),
                (Integer) mStepSpinner.getValue(),
                (Integer) dStartSpinner.getValue(),
                (Integer) dEndSpinner.getValue(),
                (Integer) dStepSpinner.getValue()
        );

        ProgressDialog progressDialog =
                new ProgressDialog(settings.getProgressStart(), settings.getProgressEnd());
        progressDialog.pack();

        NetSortRunner netSortRunner = new NetSortRunner(settings, progressDialog);
        Thread sortThread = new Thread(netSortRunner);
        sortThread.start();

        progressDialog.setVisible(true);

        try {
            sortThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JOptionPane.showMessageDialog(this,
                "completed!\nplease select file to save results!");

        Results results = netSortRunner.getResults();
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            results.exportToCSV(chooser.getSelectedFile());
        }
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        NetSortGui dialog = new NetSortGui();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
