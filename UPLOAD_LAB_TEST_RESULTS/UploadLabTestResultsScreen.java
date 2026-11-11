import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

// UC07: Upload Lab Test Results. Demo records stand in for database records.
public class UploadLabTestResultsScreen extends JFrame {
    private static final String TECHNICIAN_ID = "TECH-03";
    private static final String TECHNICIAN_LAB = "City Lab";

    private final JTextField patientIdField = new JTextField("P-1001", 18);
    private final JComboBox<String> recordBox = new JComboBox<>(new String[]{
        "LABREC-501 - CBC - City Lab",
        "LABREC-502 - Blood Glucose - City Lab",
        "LABREC-601 - Urine Analysis - Health Lab"
    });
    private final JLabel patientNameValue = new JLabel("Not verified");
    private final JLabel testDetailsValue = new JLabel("Not verified");
    private final JTextField reportDateField = new JTextField(18);
    private final JLabel fileNameLabel = new JLabel("No report file selected");
    private final JLabel statusLabel = new JLabel("Verify the patient and test record to begin.");
    private final JButton uploadButton = new Theme.PulseButton("Upload Result", Theme.ACCENT);
    private final List<SavedReport> savedReports = new ArrayList<>();
    private File selectedFile;
    private PatientRecord verifiedPatient;
    private TestRecord verifiedTest;

    public UploadLabTestResultsScreen() {
        setTitle("PULSE - Upload Lab Test Results");
        setSize(900, 640);
        setMinimumSize(new Dimension(800, 580));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Theme.BG);
        recordBox.setEditable(true); // allows the not-found test-record path to be exercised

        add(Theme.banner("Upload Lab Test Results", "Lab Technician Portal · "
            + TECHNICIAN_ID + " · " + TECHNICIAN_LAB), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildStatus(), BorderLayout.SOUTH);

        uploadButton.setEnabled(false);
        setVisible(true);
    }

    private JComponent buildForm() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new TitledBorder("Report Upload Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(9, 10, 9, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        addRow(form, gbc, 0, "Patient ID:", patientIdField);
        addRow(form, gbc, 1, "Lab Test Record:", recordBox);

        JButton verifyButton = new Theme.PulseButton("Verify Patient and Test", Theme.PRIMARY);
        gbc.gridx = 1;
        gbc.gridy = 2;
        form.add(verifyButton, gbc);

        addRow(form, gbc, 3, "Patient Name:", patientNameValue);
        addRow(form, gbc, 4, "Test and Test Date:", testDetailsValue);
        addRow(form, gbc, 5, "Report Date (YYYY-MM-DD):", reportDateField);

        JPanel filePanel = new JPanel(new BorderLayout(8, 0));
        filePanel.setOpaque(false);
        JButton chooseFileButton = new Theme.PulseButton("Choose Report File", Theme.NEUTRAL);
        filePanel.add(chooseFileButton, BorderLayout.WEST);
        filePanel.add(fileNameLabel, BorderLayout.CENTER);
        addRow(form, gbc, 6, "Report File (PDF / JPG / PNG):", filePanel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton clearButton = new Theme.PulseButton("Clear", Theme.NEUTRAL);
        buttons.add(clearButton);
        buttons.add(uploadButton);
        gbc.gridx = 1;
        gbc.gridy = 7;
        form.add(buttons, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        outer.add(form, gbc);

        verifyButton.addActionListener(e -> verifySelection());
        chooseFileButton.addActionListener(e -> chooseReportFile());
        uploadButton.addActionListener(e -> uploadReport());
        clearButton.addActionListener(e -> clearForm());
        return outer;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row,
                        String label, JComponent input) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(input, gbc);
    }

    private JComponent buildStatus() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0, 18, 12, 18));
        panel.setOpaque(false);
        statusLabel.setFont(Theme.BOLD);
        panel.add(statusLabel, BorderLayout.CENTER);
        return panel;
    }

    private void verifySelection() {
        verifiedPatient = null;
        verifiedTest = null;
        patientNameValue.setText("Not verified");
        testDetailsValue.setText("Not verified");
        uploadButton.setEnabled(false);
        String patientId = patientIdField.getText().trim();
        String recordId = selectedRecordId();
        if (patientId.isEmpty() || recordId.isEmpty()) {
            fail("Enter a patient ID and select a lab test record.");
            return;
        }

        verifiedPatient = findPatient(patientId);
        verifiedTest = findTest(recordId);
        if (verifiedPatient == null || verifiedTest == null
            || !verifiedTest.patientId.equals(patientId)) {
            verifiedPatient = null;
            verifiedTest = null;
            patientNameValue.setText("Not found");
            testDetailsValue.setText("Not found");
            uploadButton.setEnabled(false);
            fail("Patient or test record not found. Check the ID and select the correct record.");
            return;
        }
        if (!TECHNICIAN_LAB.equals(verifiedTest.labName)) {
            verifiedPatient = null;
            verifiedTest = null;
            patientNameValue.setText("Not authorized");
            testDetailsValue.setText("Not authorized");
            uploadButton.setEnabled(false);
            fail("You are not allowed to upload results for " + verifiedTest.labName + ".");
            return;
        }

        patientNameValue.setText(verifiedPatient.name);
        testDetailsValue.setText(verifiedTest.testName + " · " + verifiedTest.testDate
            + " · " + verifiedTest.labName);
        uploadButton.setEnabled(true);
        setStatus("Patient and test record verified. Check the details, then enter the report date and file.", Theme.ACCENT);
    }

    private void chooseReportFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Lab Test Report");
        chooser.setFileFilter(new FileNameExtensionFilter(
            "Supported reports (PDF, JPG, JPEG, PNG)", "pdf", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            fileNameLabel.setText(selectedFile.getName());
            fileNameLabel.setToolTipText(selectedFile.getAbsolutePath());
        }
    }

    private void uploadReport() {
        if (verifiedPatient == null || verifiedTest == null
            || !verifiedPatient.id.equals(patientIdField.getText().trim())
            || !verifiedTest.id.equals(selectedRecordId())) {
            uploadButton.setEnabled(false);
            fail("Verify the patient and test record again before uploading.");
            return;
        }

        LocalDate reportDate;
        try {
            reportDate = LocalDate.parse(reportDateField.getText().trim());
        } catch (DateTimeParseException ex) {
            fail("Enter a valid report date in YYYY-MM-DD format.");
            return;
        }
        if (reportDate.isAfter(LocalDate.now())) {
            fail("Report date cannot be in the future.");
            return;
        }
        if (reportDate.isBefore(LocalDate.parse(verifiedTest.testDate))) {
            fail("Report date cannot be earlier than the selected test date.");
            return;
        }
        if (selectedFile == null || !selectedFile.isFile()) {
            fail("Select an existing report file before uploading.");
            return;
        }
        if (!isSupportedFile(selectedFile)) {
            fail("Unsupported file type. Select a PDF, JPG, JPEG, or PNG file.");
            return;
        }

        SavedReport saved = new SavedReport(verifiedPatient.id, verifiedTest.id,
            TECHNICIAN_ID, reportDate, selectedFile.getName());
        savedReports.add(saved);
        JOptionPane.showMessageDialog(this,
            "Lab test result uploaded successfully.\nPatient: " + verifiedPatient.name
                + "\nTest: " + verifiedTest.testName + " (" + verifiedTest.id + ")"
                + "\nReport date: " + reportDate + "\nFile: " + selectedFile.getName(),
            "Upload Complete", JOptionPane.INFORMATION_MESSAGE);
        setStatus("Saved for patient " + saved.patientId + " and test " + saved.testId
            + " by " + saved.technicianId + " on " + LocalDate.now() + ".", Theme.ACCENT);
        uploadButton.setEnabled(false);
    }

    private boolean isSupportedFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".pdf") || name.endsWith(".jpg")
            || name.endsWith(".jpeg") || name.endsWith(".png");
    }

    private String selectedRecordId() {
        Object selected = recordBox.getSelectedItem();
        if (selected == null) return "";
        String value = selected.toString().trim();
        int separator = value.indexOf(" - ");
        return separator < 0 ? value : value.substring(0, separator).trim();
    }

    private PatientRecord findPatient(String id) {
        PatientRecord[] patients = {
            new PatientRecord("P-1001", "Samira Iqbal"),
            new PatientRecord("P-2002", "Ayesha Noor")
        };
        for (PatientRecord patient : patients) if (patient.id.equalsIgnoreCase(id)) return patient;
        return null;
    }

    private TestRecord findTest(String id) {
        TestRecord[] tests = {
            new TestRecord("LABREC-501", "P-1001", "CBC", "2026-10-29", "City Lab"),
            new TestRecord("LABREC-502", "P-1001", "Blood Glucose", "2026-11-02", "City Lab"),
            new TestRecord("LABREC-601", "P-2002", "Urine Analysis", "2026-11-04", "Health Lab")
        };
        for (TestRecord test : tests) if (test.id.equalsIgnoreCase(id)) return test;
        return null;
    }

    private void clearForm() {
        patientIdField.setText("");
        recordBox.setSelectedIndex(0);
        reportDateField.setText("");
        selectedFile = null;
        fileNameLabel.setText("No report file selected");
        patientNameValue.setText("Not verified");
        testDetailsValue.setText("Not verified");
        verifiedPatient = null;
        verifiedTest = null;
        uploadButton.setEnabled(false);
        setStatus("Form cleared. Enter a patient ID and select a test record.", Theme.NEUTRAL);
    }

    private void fail(String message) {
        setStatus(message, Theme.DANGER);
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private static final class PatientRecord {
        final String id;
        final String name;
        PatientRecord(String id, String name) { this.id = id; this.name = name; }
    }

    private static final class TestRecord {
        final String id, patientId, testName, testDate, labName;
        TestRecord(String id, String patientId, String testName, String testDate, String labName) {
            this.id = id; this.patientId = patientId; this.testName = testName;
            this.testDate = testDate; this.labName = labName;
        }
    }

    private static final class SavedReport {
        final String patientId, testId, technicianId, fileName;
        final LocalDate reportDate;
        SavedReport(String patientId, String testId, String technicianId,
                    LocalDate reportDate, String fileName) {
            this.patientId = patientId; this.testId = testId;
            this.technicianId = technicianId; this.reportDate = reportDate; this.fileName = fileName;
        }
    }
}
