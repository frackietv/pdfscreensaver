package tv.frackie.pdfscreensaver;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PDFScreenSaverSettings extends JPanel implements
		ListSelectionListener, WindowListener, ActionListener, ChangeListener {

	private static final long serialVersionUID = 1L;

	private JList<File> list;
	private DefaultListModel<File> listModel;
	private JButton removeButton;
	private JButton applyButton;
	private SpinnerModel delayModel;

	public void show() {
		JDialog frame = new JDialog((Window) null, "PDF ScreenSaver Settings",
				Dialog.ModalityType.TOOLKIT_MODAL);
		frame.setAlwaysOnTop(true);
		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setOpaque(true);
		frame.addWindowListener(this);
		frame.setContentPane(this);
		frame.pack();
		frame.setVisible(true);
	}

	public PDFScreenSaverSettings() {
		super(new BorderLayout());

		listModel = new DefaultListModel<>();
		list = new JList<>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);
		list.addListSelectionListener(this);
		list.setVisibleRowCount(10);
		JScrollPane listScrollPane = new JScrollPane(list);
		JButton addButton = new JButton("Add PDF");
		addButton.setActionCommand("Add PDF");
		addButton.addActionListener(this);

		removeButton = new JButton("Remove PDF");
		removeButton.setActionCommand("Remove PDF");
		removeButton.addActionListener(this);
		removeButton.setEnabled(false);

		delayModel = new SpinnerNumberModel(2500, 1000, 60000, 100);
		JLabel delaySpinnerLabel = new JLabel("Delay (ms)");
		JLabel delaySpinnerLabel2 = new JLabel("ms");
		JSpinner delaySpinner = new JSpinner(delayModel);
		delaySpinnerLabel.setLabelFor(delaySpinner);
		delaySpinnerLabel2.setLabelFor(delaySpinner);
		delaySpinner.addChangeListener(this);

		JPanel buttonPane = new JPanel();
		FlowLayout buttonLayout = new FlowLayout();
		buttonLayout.setAlignment(FlowLayout.RIGHT);
		buttonPane.setLayout(buttonLayout);
		buttonPane.add(removeButton);
		buttonPane.add(addButton);

		JPanel delayPane = new JPanel();
		FlowLayout delayLayout = new FlowLayout();
		delayLayout.setAlignment(FlowLayout.LEFT);
		delayPane.setLayout(delayLayout);
		delayPane.add(delaySpinnerLabel);
		delayPane.add(delaySpinner);
		delayPane.add(delaySpinnerLabel2);

		JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.add(delayPane);
		center.add(listScrollPane);
		center.add(buttonPane);
		center.add(Box.createVerticalStrut(5));
		center.add(new JSeparator(SwingConstants.HORIZONTAL));
		center.add(Box.createVerticalStrut(5));

		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);

		applyButton = new JButton("Apply");
		applyButton.setActionCommand("Apply");
		applyButton.addActionListener(this);
		applyButton.setEnabled(false);

		JPanel dialogButtons = new JPanel();
		FlowLayout dialogLayout = new FlowLayout();
		dialogLayout.setAlignment(FlowLayout.RIGHT);
		dialogButtons.setLayout(dialogLayout);
		dialogButtons.add(okButton);
		dialogButtons.add(cancelButton);
		dialogButtons.add(applyButton);

		add(center, BorderLayout.CENTER);
		add(dialogButtons, BorderLayout.PAGE_END);

		Preferences prefs = Preferences.userNodeForPackage(ScreenSaver.class);
		List<String> filenames = new ArrayList<>(Arrays.asList((prefs.get(
				"pdfFiles", "").split("\\|"))));
		if (filenames.get(0).isEmpty()) {
			filenames.clear();
		}
		for (String filename : filenames) {
			listModel.addElement(new File(filename));
		}
		delayModel.setValue(prefs.getInt("delay", 2500));

	}

	private void setDirty() {
		applyButton.setEnabled(true);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() instanceof JList) {
			@SuppressWarnings("unchecked")
			JList<File> l = (JList<File>) e.getSource();
			if (l.getSelectedValuesList().isEmpty()) {
				removeButton.setEnabled(false);
			} else {
				removeButton.setEnabled(true);
			}
		}
		System.out.println(e.getSource().toString());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		switch (command) {
		case "Add PDF":
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setMultiSelectionEnabled(true);
			fc.setFileFilter(new FileNameExtensionFilter("PDF Document", "pdf"));
			if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this)) {
				for (File file : fc.getSelectedFiles()) {
					if (!listModel.contains(file)) {
						listModel.addElement(file);
						setDirty();
					}
				}
			}
			break;
		case "Remove PDF":
			for (File file : list.getSelectedValuesList()) {
				if (listModel.removeElement(file)) {
					setDirty();
				}
			}
			break;
		case "OK":
			save();
			close();
			break;
		case "Cancel":
			close();
			break;
		case "Apply":
			save();
			break;
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		setDirty();
	}

	private void close() {
		this.setVisible(false);
		System.exit(0);
	}

	private void save() {
		Preferences prefs = Preferences.userNodeForPackage(ScreenSaver.class);
		List<String> filenames = new ArrayList<>();
		for (Object obj : listModel.toArray()) {
			filenames.add(((File) obj).getAbsolutePath());
		}
		if (filenames.isEmpty()) {
			prefs.put("pdfFiles", "");
		} else {
			prefs.put("pdfFiles", String.join("|", filenames));
		}
		prefs.putInt("delay", (int) delayModel.getValue());
	}
}
