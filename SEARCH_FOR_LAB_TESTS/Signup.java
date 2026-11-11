import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.ArrayList;

// UC 03: Search for Lab Test (screen class is still called Signup so MainFrame keeps working)
public class Signup extends JFrame {

    private static final String ALL = "All";
    private static final String VALID_TEXT = "[A-Za-z0-9 .'()-]*";
    private static final Color GREEN = new Color(0, 128, 0);
    private static final Color RED = new Color(178, 34, 34);
    private static final Color ORANGE = new Color(200, 110, 0);
    private static final Color BLUE = new Color(25, 90, 160);

    // Column positions inside each test record
    private static final int ID = 0, NAME = 1, CATEGORY = 2, LAB = 3,
                             LOCATION = 4, PRICE = 5, PREP = 6, TURNAROUND = 7;

    // Demonstration data; replace with stored lab tests later.
    private static final String[][] TESTS = {
        {"LT-101", "CBC", "Hematology", "City Lab", "Lahore", "1500", "No special preparation", "Same day"},
        {"LT-102", "Blood Glucose", "Biochemistry", "Health Lab", "Lahore", "800", "Fasting for 8 hours", "Same day"},
        {"LT-103", "Urine Analysis", "Pathology", "Care Lab", "Karachi", "600", "Use a clean sample container", "Same day"},
        {"LT-104", "CBC", "Hematology", "Care Lab", "Karachi", "1400", "No special preparation", "Same day"},
        {"LT-105", "Lipid Profile", "Biochemistry", "City Lab", "Islamabad", "2200", "Fasting for 10 to 12 hours", "24 hours"},
        {"LT-106", "Liver Function Test", "Biochemistry", "Health Lab", "Islamabad", "1800", "Fasting for 8 hours recommended", "24 hours"},
        {"LT-107", "Thyroid Profile", "Endocrinology", "Prime Diagnostics", "Rawalpindi", "2500", "No fasting needed; morning sample preferred", "48 hours"},
        {"LT-108", "Hemoglobin", "Hematology", "Health Lab", "Islamabad", "500", "No special preparation", "Same day"},
        {"LT-109", "Blood Glucose", "Biochemistry", "City Lab", "Islamabad", "750", "Fasting for 8 hours", "Same day"},
        {"LT-110", "Stool Analysis", "Pathology", "Prime Diagnostics", "Lahore", "700", "Fresh sample in a sterile container", "24 hours"}
    };

    private final JComboBox<String> categoryBox = new JComboBox<>();
    private final JComboBox<String> testNameBox = new JComboBox<>();
    private final JComboBox<String> labBox = new JComboBox<>();
    private final JComboBox<String> locationBox = new JComboBox<>();

    private final JLabel statusLabel = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"Test ID", "Test Name", "Category", "Laboratory", "Location", "Price (Rs.)"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable resultsTable = new JTable(tableModel);

    private final JTextField idField = readOnlyField();
    private final JTextField nameField = readOnlyField();
    private final JTextField categoryField = readOnlyField();
    private final JTextField labField = readOnlyField();
    private final JTextField locationField = readOnlyField();
    private final JTextField priceField = readOnlyField();
    private final JTextField turnaroundField = readOnlyField();
    private final JTextArea prepArea = new JTextArea(3, 20);

    private final JRadioButton bookAppointmentOption = new JRadioButton("Book Appointment");
    private final JRadioButton viewPastRecordsOption = new JRadioButton("View Past Records");
    private final ButtonGroup bookingOptions = new ButtonGroup();
    private final JButton continueButton = new Theme.PulseButton("Continue", Theme.ACCENT);

    // Stops combo listeners from reacting while the program itself changes them
    private boolean updating = false;

    public Signup() {
        setTitle("PULSE - Search for Lab Tests");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 8));
        getContentPane().setBackground(Color.WHITE);

        add(buildHeader(), BorderLayout.NORTH);

        JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildFilterPanel(), buildResultsPanel());
        center.setDividerLocation(300);
        center.setBorder(new EmptyBorder(0, 10, 0, 10));
        add(center, BorderLayout.CENTER);

        add(buildBottomPanel(), BorderLayout.SOUTH);

        fillDropdowns();
        runSearch(); // Show every available test when the screen opens
        updateActionState();
        setVisible(true);
    }

    // ---------------------------------------------------------------- UI building

    private JComponent buildHeader() {
        return Theme.banner("Search for Lab Tests",
            "Find a test, compare laboratories and continue to booking");
    }

    private JComponent buildFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Search Filters"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(6, 10, 0, 10);

        testNameBox.setEditable(true);
        testNameBox.setToolTipText("Type a test name (e.g. CBC) or pick one from the list");

        int row = 0;
        row = addFilter(panel, gbc, row, "Category:", categoryBox);
        row = addFilter(panel, gbc, row, "Test Name (type or pick):", testNameBox);
        row = addFilter(panel, gbc, row, "Laboratory Name:", labBox);
        row = addFilter(panel, gbc, row, "Location:", locationBox);

        JButton searchButton = new Theme.PulseButton("Search", Theme.PRIMARY);
        JButton clearButton = new Theme.PulseButton("Clear Filters", Theme.NEUTRAL);
        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.add(searchButton);
        buttons.add(clearButton);
        gbc.gridy = row++;
        gbc.insets = new Insets(14, 10, 0, 10);
        panel.add(buttons, gbc);

        gbc.gridy = row;
        gbc.weighty = 1; // pushes everything to the top
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createGlue(), gbc);

        // Live search: any change to a filter refreshes the results
        categoryBox.addActionListener(e -> {
            if (updating) return;
            refreshTestNames(typedName());
            runSearch();
        });
        labBox.addActionListener(e -> { if (!updating) runSearch(); });
        locationBox.addActionListener(e -> { if (!updating) runSearch(); });
        testNameBox.addActionListener(e -> { if (!updating) onTestNameChosen(); });
        ((JTextField) testNameBox.getEditor().getEditorComponent()).getDocument()
            .addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { scheduleTestNameSearch(); }
                public void removeUpdate(DocumentEvent e) { scheduleTestNameSearch(); }
                public void changedUpdate(DocumentEvent e) { }
            });

        searchButton.addActionListener(e -> runSearch());
        clearButton.addActionListener(e -> clearFilters());
        return panel;
    }

    private int addFilter(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row++;
        gbc.insets = new Insets(10, 10, 0, 10);
        panel.add(new JLabel(label), gbc);
        gbc.gridy = row++;
        gbc.insets = new Insets(3, 10, 0, 10);
        panel.add(field, gbc);
        return row;
    }

    private JComponent buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));

        statusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        statusLabel.setBorder(new EmptyBorder(2, 6, 2, 6));
        panel.add(statusLabel, BorderLayout.NORTH);

        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setRowHeight(24);
        resultsTable.getTableHeader().setReorderingAllowed(false);
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(70);
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            showDetails(selectedTest());
            updateActionState();
        });
        resultsTable.setGridColor(new Color(225, 232, 238));
        resultsTable.setSelectionBackground(new Color(190, 225, 240));
        resultsTable.setSelectionForeground(Color.BLACK);
        resultsTable.getTableHeader().setBackground(Theme.PRIMARY);
        resultsTable.getTableHeader().setForeground(Color.WHITE);
        resultsTable.getTableHeader().setFont(Theme.BOLD);
        resultsTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 250, 252));
                return c;
            }
        });
        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(new TitledBorder("Matching Tests (click a row)"));
        tableScroll.setPreferredSize(new Dimension(0, 200));
        panel.add(tableScroll, BorderLayout.CENTER);

        panel.add(buildDetailsPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Test Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addPair(panel, gbc, 0, 0, "Test ID:", idField);
        addPair(panel, gbc, 0, 2, "Test Name:", nameField);
        addPair(panel, gbc, 1, 0, "Category:", categoryField);
        addPair(panel, gbc, 1, 2, "Laboratory:", labField);
        addPair(panel, gbc, 2, 0, "Location:", locationField);
        addPair(panel, gbc, 2, 2, "Price:", priceField);
        addPair(panel, gbc, 3, 0, "Report Time:", turnaroundField);

        prepArea.setEditable(false);
        prepArea.setLineWrap(true);
        prepArea.setWrapStyleWord(true);
        prepArea.setBackground(panel.getBackground());
        prepArea.setBorder(new EmptyBorder(2, 4, 2, 4));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Preparation:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        panel.add(new JScrollPane(prepArea), gbc);
        return panel;
    }

    private void addPair(JPanel panel, GridBagConstraints gbc, int row, int col,
                         String label, JTextField field) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = col;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = col + 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private JComponent buildBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(Theme.BG);
        panel.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            new EmptyBorder(0, 6, 4, 6)));

        JLabel next = new JLabel("After viewing details:");
        next.setFont(new Font("Arial", Font.BOLD, 13));
        bookingOptions.add(bookAppointmentOption);
        bookingOptions.add(viewPastRecordsOption);
        bookAppointmentOption.setSelected(true); // default choice required by the screen design
        bookAppointmentOption.addActionListener(e -> updateActionState());
        viewPastRecordsOption.addActionListener(e -> updateActionState());

        continueButton.addActionListener(e -> continueWithSelectedOption());
        JButton backButton = new Theme.PulseButton("Back to Home", Theme.NEUTRAL);
        backButton.addActionListener(e -> {
            new MainFrame();
            dispose();
        });

        panel.add(next);
        panel.add(bookAppointmentOption);
        panel.add(viewPastRecordsOption);
        panel.add(continueButton);
        panel.add(Box.createHorizontalStrut(40));
        panel.add(backButton);
        return panel;
    }

    private static JTextField readOnlyField() {
        JTextField field = new JTextField(12);
        field.setEditable(false);
        field.setBackground(new Color(245, 245, 245));
        return field;
    }

    // ---------------------------------------------------------------- dropdown data

    private void fillDropdowns() {
        updating = true;
        categoryBox.setModel(new DefaultComboBoxModel<>(withAll(distinct(CATEGORY))));
        labBox.setModel(new DefaultComboBoxModel<>(withAll(distinct(LAB))));
        locationBox.setModel(new DefaultComboBoxModel<>(withAll(distinct(LOCATION))));
        updating = false;
        refreshTestNames("");
    }

    // Rebuilds the Test Name suggestions for the chosen category and keeps what was typed
    private void refreshTestNames(String keepText) {
        String category = selected(categoryBox);
        List<String> items = new ArrayList<>();
        items.add("");
        items.addAll(distinct(NAME, category.equals(ALL) ? null : category));
        updating = true;
        testNameBox.setModel(new DefaultComboBoxModel<>(items.toArray(new String[0])));
        testNameBox.setSelectedItem(keepText);
        updating = false;
    }

    private List<String> distinct(int column) {
        return distinct(column, null);
    }

    private List<String> distinct(int column, String categoryFilter) {
        TreeSet<String> values = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String[] test : TESTS) {
            if (categoryFilter == null || test[CATEGORY].equals(categoryFilter)) values.add(test[column]);
        }
        return new ArrayList<>(values);
    }

    private String[] withAll(List<String> values) {
        List<String> list = new ArrayList<>();
        list.add(ALL);
        list.addAll(values);
        return list.toArray(new String[0]);
    }

    private String selected(JComboBox<String> box) {
        Object item = box.getSelectedItem();
        return item == null ? ALL : item.toString();
    }

    private String typedName() {
        Object item = testNameBox.getEditor().getItem();
        return item == null ? "" : item.toString().trim();
    }

    // ---------------------------------------------------------------- behaviour

    // When the user presses Enter or picks a test name, also fill the category automatically
    private void onTestNameChosen() {
        syncCategoryForTestName();
        runSearch();
    }

    // If the test name identifies one category, fill that filter as well.
    private void syncCategoryForTestName() {
        String name = typedName();
        if (name.isEmpty()) return;
        String category = null;
        boolean sameCategory = true;
        for (String[] test : TESTS) {
            if (test[NAME].equalsIgnoreCase(name)) {
                if (category == null) category = test[CATEGORY];
                else if (!category.equals(test[CATEGORY])) sameCategory = false;
            }
        }
        if (category != null && sameCategory && !category.equals(selected(categoryBox))) {
            updating = true;
            categoryBox.setSelectedItem(category);
            updating = false;
            refreshTestNames(name);
        }
    }

    // Defer model updates until Swing finishes notifying the text document.
    private void scheduleTestNameSearch() {
        if (updating) return;
        SwingUtilities.invokeLater(() -> {
            if (updating) return;
            syncCategoryForTestName();
            runSearch();
        });
    }

    private void runSearch() {
        String name = typedName();
        tableModel.setRowCount(0);

        if (!name.matches(VALID_TEXT)) {
            showDetails(null);
            setStatus("Test Name has invalid characters. Use letters, numbers, spaces and . ' ( ) - only.", RED);
            updateActionState();
            return;
        }

        String category = selected(categoryBox);
        String lab = selected(labBox);
        String location = selected(locationBox);

        for (String[] test : TESTS) {
            boolean nameOk = name.isEmpty() || test[NAME].toLowerCase().contains(name.toLowerCase());
            boolean categoryOk = category.equals(ALL) || test[CATEGORY].equals(category);
            boolean labOk = lab.equals(ALL) || test[LAB].equals(lab);
            boolean locationOk = location.equals(ALL) || test[LOCATION].equals(location);
            if (nameOk && categoryOk && labOk && locationOk) {
                tableModel.addRow(new Object[]{test[ID], test[NAME], test[CATEGORY],
                    test[LAB], test[LOCATION], test[PRICE]});
            }
        }

        int count = tableModel.getRowCount();
        if (count > 0) {
            setStatus(count + (count == 1 ? " lab test found." : " lab tests found.")
                + " Click a row to see its details.", GREEN);
            resultsTable.setRowSelectionInterval(0, 0); // fills the Test Details section
        } else {
            showDetails(null);
            setStatus("No lab tests found matching your criteria.", ORANGE);
            List<String> alternatives = distinct(NAME, category.equals(ALL) ? null : category);
            prepArea.setText("No match. Try one of these tests: " + String.join(", ", alternatives)
                + ".\nOr click Clear Filters to see all available tests.");
        }
        updateActionState();
    }

    private void showDetails(String[] test) {
        if (test == null) {
            for (JTextField f : new JTextField[]{idField, nameField, categoryField, labField,
                                                  locationField, priceField, turnaroundField}) {
                f.setText("");
            }
            prepArea.setText("Select a test from the table to see its full details.");
            return;
        }
        idField.setText(test[ID]);
        nameField.setText(test[NAME]);
        categoryField.setText(test[CATEGORY]);
        labField.setText(test[LAB]);
        locationField.setText(test[LOCATION]);
        priceField.setText("Rs. " + test[PRICE]);
        turnaroundField.setText(test[TURNAROUND]);
        prepArea.setText(test[PREP]);
        prepArea.setCaretPosition(0);
    }

    private String[] selectedTest() {
        int row = resultsTable.getSelectedRow();
        if (row < 0) return null;
        return findTest(tableModel.getValueAt(row, 0).toString());
    }

    private String[] findTest(String id) {
        for (String[] test : TESTS) {
            if (test[ID].equalsIgnoreCase(id)) return test;
        }
        return null;
    }

    // Booking options only work once a test is selected; Continue needs an option too
    private void updateActionState() {
        boolean hasTest = selectedTest() != null;
        bookAppointmentOption.setEnabled(hasTest);
        viewPastRecordsOption.setEnabled(hasTest);
        if (!bookAppointmentOption.isSelected() && !viewPastRecordsOption.isSelected()) {
            bookAppointmentOption.setSelected(true);
        }
        continueButton.setEnabled(hasTest
            && (bookAppointmentOption.isSelected() || viewPastRecordsOption.isSelected()));
    }

    private void continueWithSelectedOption() {
        String[] test = selectedTest();
        if (test == null) {
            setStatus("Select a test from the table first.", RED);
            return;
        }
        if (bookAppointmentOption.isSelected()) {
            bookAppointment(test);
        } else if (viewPastRecordsOption.isSelected()) {
            viewPastRecords(test);
        } else {
            setStatus("Select Book Appointment or View Past Records first.", RED);
        }
    }

    private void bookAppointment(String[] test) {
        DateTimeFormatter shown = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH);
        JComboBox<String> dateBox = new JComboBox<>();
        for (int i = 1; i <= 7; i++) dateBox.addItem(LocalDate.now().plusDays(i).format(shown));
        JComboBox<String> timeBox = new JComboBox<>(
            new String[]{"09:00 AM", "11:00 AM", "02:00 PM", "04:00 PM"});

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Test:"));
        form.add(new JLabel(test[NAME] + " (" + test[ID] + ")"));
        form.add(new JLabel("Laboratory:"));
        form.add(new JLabel(test[LAB] + ", " + test[LOCATION]));
        form.add(new JLabel("Price:"));
        form.add(new JLabel("Rs. " + test[PRICE]));
        form.add(new JLabel("Preferred Date:"));
        form.add(dateBox);
        form.add(new JLabel("Preferred Time:"));
        form.add(timeBox);

        int choice = JOptionPane.showConfirmDialog(this, form, "Book Appointment",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            setStatus("Booking cancelled. You can choose another option.", ORANGE);
            return;
        }
        String reference = "APT-" + test[ID].substring(3) + "-"
            + LocalDate.now().plusDays(dateBox.getSelectedIndex() + 1)
                .format(DateTimeFormatter.ofPattern("ddMMyy"));
        JOptionPane.showMessageDialog(this,
            "Appointment requested.\n\nReference: " + reference
                + "\nTest: " + test[NAME] + " at " + test[LAB] + ", " + test[LOCATION]
                + "\nDate: " + dateBox.getSelectedItem() + " at " + timeBox.getSelectedItem()
                + "\n\nPreparation: " + test[PREP],
            "Appointment Requested", JOptionPane.INFORMATION_MESSAGE);
        setStatus("Appointment requested for " + test[NAME] + " (" + reference + ").", GREEN);
    }

    private void viewPastRecords(String[] test) {
        DateTimeFormatter shown = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
        String message = "Past records for " + test[NAME] + " (sample data):\n\n"
            + "- " + LocalDate.now().minusDays(45).format(shown) + " | " + test[LAB]
            + " | Report available\n"
            + "- " + LocalDate.now().minusDays(200).format(shown) + " | " + test[LAB]
            + " | Report available";
        JOptionPane.showMessageDialog(this, message, "Past Records", JOptionPane.INFORMATION_MESSAGE);
        setStatus("Showing past records for " + test[NAME] + ".", BLUE);
    }

    private void clearFilters() {
        updating = true;
        categoryBox.setSelectedItem(ALL);
        labBox.setSelectedItem(ALL);
        locationBox.setSelectedItem(ALL);
        updating = false;
        refreshTestNames("");
        bookingOptions.clearSelection();
        runSearch();
        setStatus("Filters cleared. Showing all " + tableModel.getRowCount() + " available tests.", BLUE);
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }
}
