import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

// UC08: View Lab Test Results. Reports shown here are sample data and are read-only.
public class ViewLabTestResultsScreen extends JFrame {
    private static final String PATIENT_ACCOUNT_ID = "P-1001";
    private static final String DOCTOR_ACCOUNT_ID = "DR-77";

    private final JRadioButton patientOption = new JRadioButton("Patient");
    private final JRadioButton doctorOption = new JRadioButton("Authorized Doctor");
    private final ButtonGroup actorGroup = new ButtonGroup();
    private final JComboBox<String> patientBox = new JComboBox<>();
    private final JLabel statusLabel = new JLabel("Choose an actor and load the available results.");
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"Result ID", "Test Name", "Laboratory", "Report Date"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable resultsTable = new JTable(tableModel);
    private final JButton openButton = new Theme.PulseButton("Open Selected Result", Theme.ACCENT);

    private final List<LabResult> demoResults = List.of(
        new LabResult("RES-301", "P-1001", "CBC", "City Lab", LocalDate.of(2026, 10, 30),
            "Hemoglobin: 13.8 g/dL\nWhite blood cells: 6.2 x10^9/L\nPlatelets: 250 x10^9/L"),
        new LabResult("RES-302", "P-1001", "Blood Glucose", "Health Lab", LocalDate.of(2026, 11, 2),
            "Fasting glucose: 92 mg/dL\nStatus: Within the sample reference range"),
        new LabResult("RES-401", "P-2002", "Urine Analysis", "Care Lab", LocalDate.of(2026, 11, 4),
            "Appearance: Clear\nProtein: Negative\nGlucose: Negative")
    );

    public ViewLabTestResultsScreen() {
        setTitle("PULSE - View Lab Test Results");
        setSize(1000, 680);
        setMinimumSize(new Dimension(850, 580));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Theme.BG);

        add(Theme.banner("View Lab Test Results", "Open your uploaded lab reports"), BorderLayout.NORTH);
        add(buildMainPanel(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        actorGroup.add(patientOption);
        actorGroup.add(doctorOption);
        patientOption.setSelected(true);
        patientOption.addActionListener(e -> updateActorMode());
        doctorOption.addActionListener(e -> updateActorMode());
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) openButton.setEnabled(resultsTable.getSelectedRow() >= 0);
        });
        openButton.setEnabled(false);
        updateActorMode();
        loadResults();
        setVisible(true);
    }

    private JComponent buildMainPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(4, 14, 0, 14));

        JPanel controls = new JPanel(new GridBagLayout());
        controls.setBackground(Color.WHITE);
        controls.setBorder(new TitledBorder("Select Patient Record"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        controls.add(new JLabel("I am viewing as:"), gbc);
        gbc.gridx = 1;
        controls.add(patientOption, gbc);
        gbc.gridx = 2;
        controls.add(doctorOption, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        controls.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        controls.add(patientBox, gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JButton loadButton = new Theme.PulseButton("Load Results", Theme.PRIMARY);
        controls.add(loadButton, gbc);
        loadButton.addActionListener(e -> loadResults());

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new TitledBorder("Uploaded Lab Test Results"));
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setRowHeight(26);
        resultsTable.getTableHeader().setReorderingAllowed(false);
        resultsTable.getTableHeader().setBackground(Theme.PRIMARY);
        resultsTable.getTableHeader().setForeground(Color.WHITE);
        resultsTable.getTableHeader().setFont(Theme.BOLD);
        tablePanel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        root.add(controls, BorderLayout.NORTH);
        root.add(tablePanel, BorderLayout.CENTER);
        return root;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 18, 12, 18));
        statusLabel.setFont(Theme.BOLD);
        footer.add(statusLabel, BorderLayout.CENTER);
        footer.add(openButton, BorderLayout.EAST);
        openButton.addActionListener(e -> openSelectedResult());
        return footer;
    }

    private void updateActorMode() {
        DefaultComboBoxModel<String> model;
        if (doctorOption.isSelected()) {
            model = new DefaultComboBoxModel<>(new String[]{
                "P-1001 - Samira Iqbal (authorized)",
                "P-2002 - Ayesha Noor (not authorized)",
                "P-3003 - Omar Ilyas (authorized; no results)"
            });
            statusLabel.setText("Signed in as doctor " + DOCTOR_ACCOUNT_ID + ". Choose a patient you are allowed to access.");
        } else {
            model = new DefaultComboBoxModel<>(new String[]{"P-1001 - Samira Iqbal (your account)"});
            statusLabel.setText("Signed in as patient " + PATIENT_ACCOUNT_ID + ". Your patient ID is selected.");
        }
        patientBox.setModel(model);
        tableModel.setRowCount(0);
        openButton.setEnabled(false);
    }

    private void loadResults() {
        tableModel.setRowCount(0);
        openButton.setEnabled(false);
        String patientId = selectedPatientId();
        if (doctorOption.isSelected() && !hasDoctorPermission(patientId)) {
            setStatus("Access denied. This doctor is not authorized to view the selected patient's results.", Theme.DANGER);
            return;
        }
        for (LabResult result : demoResults) {
            if (result.patientId.equals(patientId)) {
                tableModel.addRow(new Object[]{result.id, result.testName,
                    result.labName, result.reportDate.toString()});
            }
        }
        if (tableModel.getRowCount() == 0) {
            setStatus("No lab test results available.", Theme.NEUTRAL);
        } else {
            setStatus(tableModel.getRowCount() + " result(s) available. Select a row to open a report.", Theme.ACCENT);
        }
    }

    private void openSelectedResult() {
        int row = resultsTable.getSelectedRow();
        if (row < 0) {
            setStatus("Select a lab test result first.", Theme.DANGER);
            return;
        }
        String resultId = tableModel.getValueAt(row, 0).toString();
        LabResult result = findResult(resultId);
        if (result == null) {
            setStatus("Selected lab test result is no longer available.", Theme.DANGER);
            return;
        }
        String patientId = selectedPatientId();
        boolean canAccess = doctorOption.isSelected()
            ? hasDoctorPermission(patientId)
            : patientId.equals(PATIENT_ACCOUNT_ID);
        if (!canAccess || !result.patientId.equals(patientId)) {
            setStatus("Access denied. The result does not belong to the logged-in patient or is not authorized.", Theme.DANGER);
            return;
        }

        String report = "Test: " + result.testName + "\nLaboratory: " + result.labName
            + "\nReport date: " + result.reportDate + "\nResult ID: " + result.id
            + "\n\n" + result.reportText;
        JTextArea reportArea = new JTextArea(report, 10, 44);
        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(reportArea),
            "Lab Test Result", JOptionPane.INFORMATION_MESSAGE);
        setStatus("Report opened read-only. The saved result was not changed.", Theme.ACCENT);
    }

    private String selectedPatientId() {
        Object selected = patientBox.getSelectedItem();
        if (selected == null) return "";
        String value = selected.toString();
        int separator = value.indexOf(" - ");
        return separator < 0 ? value : value.substring(0, separator);
    }

    private boolean hasDoctorPermission(String patientId) {
        // Demo access list: doctor DR-77 is authorized for P-1001 and P-3003.
        return DOCTOR_ACCOUNT_ID.equals("DR-77")
            && (patientId.equals("P-1001") || patientId.equals("P-3003"));
    }

    private LabResult findResult(String id) {
        for (LabResult result : demoResults) if (result.id.equals(id)) return result;
        return null;
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    private static final class LabResult {
        final String id, patientId, testName, labName, reportText;
        final LocalDate reportDate;
        LabResult(String id, String patientId, String testName, String labName,
                  LocalDate reportDate, String reportText) {
            this.id = id; this.patientId = patientId; this.testName = testName;
            this.labName = labName; this.reportDate = reportDate; this.reportText = reportText;
        }
    }
}
