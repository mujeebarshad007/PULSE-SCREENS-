import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// UC09: Manage User Accounts. All accounts are sample data held in memory.
public class ManageUserAccountsScreen extends JFrame {
    private static final String ADMIN_ID = "USR-1001";
    private static final String[] ROLES = {"Patient", "Doctor", "Lab Technician", "Administrator"};
    private final List<Account> accounts = new ArrayList<>();

    private final JTextField searchField = new JTextField(20);
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"User ID", "Name", "Email", "Role", "Account Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable userTable = new JTable(tableModel);
    private final JTextField idField = new JTextField(18);
    private final JTextField nameField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JComboBox<String> roleBox = new JComboBox<>(ROLES);
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"Active", "Deactivated"});
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel auditLabel = new JLabel("Select an account or click Create Account.");
    private final JLabel statusLabel = new JLabel("Administrator session active: " + ADMIN_ID);
    private final JButton saveButton = new Theme.PulseButton("Save", Theme.ACCENT);
    private final JButton deactivateButton = new Theme.PulseButton("Deactivate", Theme.DANGER);
    private final JButton reactivateButton = new Theme.PulseButton("Reactivate", Theme.PRIMARY);

    private Account selectedAccount;
    private boolean creating = false;
    private boolean administratorActive = true; // precondition: active administrator is signed in
    private int nextId = 2001;

    public ManageUserAccountsScreen() {
        addDemoAccounts();
        setTitle("PULSE - Manage User Accounts");
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(Theme.BG);

        add(Theme.banner("Manage User Accounts", "Administrator Portal · " + ADMIN_ID), BorderLayout.NORTH);
        add(buildMainPanel(), BorderLayout.CENTER);
        add(buildStatusPanel(), BorderLayout.SOUTH);

        refreshTable("");
        updateButtons();
        setVisible(true);
    }

    private JComponent buildMainPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(4, 12, 0, 12));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(new TitledBorder("Find an Account"));
        searchPanel.add(new JLabel("Search by ID, name, or email:"));
        searchPanel.add(searchField);
        JButton searchButton = new Theme.PulseButton("Search", Theme.PRIMARY);
        JButton createButton = new Theme.PulseButton("Create Account", Theme.ACCENT);
        JButton showAllButton = new Theme.PulseButton("Show All", Theme.NEUTRAL);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);
        searchPanel.add(createButton);
        searchButton.addActionListener(e -> searchAccounts());
        searchField.addActionListener(e -> searchAccounts());
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable("");
            setStatus("Showing all user accounts.", Theme.NEUTRAL);
        });
        createButton.addActionListener(e -> startCreateAccount());

        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(24);
        userTable.getTableHeader().setReorderingAllowed(false);
        userTable.getTableHeader().setBackground(Theme.PRIMARY);
        userTable.getTableHeader().setForeground(Color.WHITE);
        userTable.getTableHeader().setFont(Theme.BOLD);
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedAccount();
        });
        JScrollPane tableScroll = new JScrollPane(userTable);
        tableScroll.setBorder(new TitledBorder("User Accounts"));

        JPanel listPanel = new JPanel(new BorderLayout(0, 8));
        listPanel.add(searchPanel, BorderLayout.NORTH);
        listPanel.add(tableScroll, BorderLayout.CENTER);
        listPanel.setPreferredSize(new Dimension(540, 0));

        JPanel form = buildAccountForm();
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, form);
        split.setResizeWeight(0.55);
        split.setDividerLocation(600);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildAccountForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new TitledBorder("Account Details"));
        idField.setEditable(false);
        idField.setBackground(new Color(245, 245, 245));
        statusBox.setEnabled(false); // status changes use the confirmable Deactivate/Reactivate actions
        passwordField.setToolTipText("Required only when creating a new account; at least 8 characters.");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 9, 8, 9);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        addField(form, gbc, 0, "User ID:", idField);
        addField(form, gbc, 1, "Name:", nameField);
        addField(form, gbc, 2, "Email:", emailField);
        addField(form, gbc, 3, "Role:", roleBox);
        addField(form, gbc, 4, "Account Status:", statusBox);
        addField(form, gbc, 5, "Initial Password:", passwordField);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        auditLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        form.add(auditLabel, gbc);

        JPanel actions = new JPanel(new GridLayout(2, 2, 7, 7));
        actions.setOpaque(false);
        actions.add(saveButton);
        deactivateButton.setEnabled(false);
        reactivateButton.setEnabled(false);
        actions.add(deactivateButton);
        actions.add(reactivateButton);
        JButton cancelButton = new Theme.PulseButton("Cancel / Clear", Theme.NEUTRAL);
        actions.add(cancelButton);
        gbc.gridy = 7;
        form.add(actions, gbc);

        saveButton.addActionListener(e -> saveAccount());
        deactivateButton.addActionListener(e -> changeAccountStatus(false));
        reactivateButton.addActionListener(e -> changeAccountStatus(true));
        cancelButton.addActionListener(e -> clearForm());
        return form;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row,
                          String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
        gbc.weightx = 0;
    }

    private JComponent buildStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 16, 10, 16));
        statusLabel.setFont(Theme.BOLD);
        panel.add(statusLabel, BorderLayout.CENTER);
        return panel;
    }

    private void addDemoAccounts() {
        accounts.add(new Account("USR-1001", "Mujeeb Arshad", "mujeeb@pulse.test", "Administrator", "Active"));
        accounts.add(new Account("USR-1002", "Samira Iqbal", "samira@pulse.test", "Patient", "Active"));
        accounts.add(new Account("USR-1003", "Dr. Hamza Ali", "hamza@pulse.test", "Doctor", "Active"));
        accounts.add(new Account("USR-1004", "Ayesha Noor", "ayesha@pulse.test", "Lab Technician", "Deactivated"));
    }

    private void searchAccounts() {
        String query = searchField.getText().trim();
        if (!query.matches("[A-Za-z0-9@._' -]*")) {
            setStatus("Search accepts letters, numbers, spaces, @, dot, underscore, apostrophe and hyphen.", Theme.DANGER);
            return;
        }
        refreshTable(query);
        setStatus(tableModel.getRowCount() == 0 ? "No user accounts found." :
            tableModel.getRowCount() + " account(s) found. Select one to view or update details.",
            tableModel.getRowCount() == 0 ? Theme.NEUTRAL : Theme.ACCENT);
    }

    private void refreshTable(String query) {
        tableModel.setRowCount(0);
        String needle = query.toLowerCase(Locale.ROOT);
        for (Account account : accounts) {
            if (needle.isEmpty() || account.id.toLowerCase(Locale.ROOT).contains(needle)
                || account.name.toLowerCase(Locale.ROOT).contains(needle)
                || account.email.toLowerCase(Locale.ROOT).contains(needle)) {
                tableModel.addRow(new Object[]{account.id, account.name, account.email,
                    account.role, account.status});
            }
        }
        userTable.clearSelection();
    }

    private void loadSelectedAccount() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            selectedAccount = null;
            updateButtons();
            return;
        }
        selectedAccount = findAccount(tableModel.getValueAt(row, 0).toString());
        creating = false;
        if (selectedAccount == null) return;
        idField.setText(selectedAccount.id);
        nameField.setText(selectedAccount.name);
        emailField.setText(selectedAccount.email);
        roleBox.setSelectedItem(selectedAccount.role);
        statusBox.setSelectedItem(selectedAccount.status);
        passwordField.setText("");
        passwordField.setEnabled(false);
        updateAuditLabel(selectedAccount);
        updateButtons();
    }

    private void startCreateAccount() {
        if (!checkAdministratorPermission()) return;
        userTable.clearSelection();
        selectedAccount = null;
        creating = true;
        idField.setText("Generated on save");
        nameField.setText("");
        emailField.setText("");
        roleBox.setSelectedIndex(0);
        statusBox.setSelectedItem("Active");
        passwordField.setText("");
        passwordField.setEnabled(true);
        auditLabel.setText("New account · status will start as Active · changes recorded for " + ADMIN_ID);
        updateButtons();
        nameField.requestFocusInWindow();
        setStatus("Enter the new account details and initial password.", Theme.NEUTRAL);
    }

    private void saveAccount() {
        if (!checkAdministratorPermission()) return;
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String role = (String) roleBox.getSelectedItem();
        if (!name.matches("[\\p{L}][\\p{L} .'-]{1,59}")) {
            setStatus("Enter a valid name (at least 2 letters).", Theme.DANGER);
            return;
        }
        if (!email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")) {
            setStatus("Enter a valid email address, such as user@example.com.", Theme.DANGER);
            return;
        }
        if (role == null || !List.of(ROLES).contains(role)) {
            setStatus("Select a valid account role.", Theme.DANGER);
            return;
        }
        if (!creating && selectedAccount != null && selectedAccount.id.equals(ADMIN_ID)
            && !"Administrator".equals(role)) {
            setStatus("The active administrator cannot remove their own administrator role.", Theme.DANGER);
            return;
        }
        for (Account account : accounts) {
            if (account.email.equalsIgnoreCase(email)
                && (creating || selectedAccount == null || !account.id.equals(selectedAccount.id))) {
                setStatus("That email is already used by another account.", Theme.DANGER);
                return;
            }
        }

        if (creating) {
            char[] password = passwordField.getPassword();
            if (password.length < 8) {
                setStatus("Initial password must be at least 8 characters.", Theme.DANGER);
                return;
            }
            String id = String.format("USR-%04d", nextId++);
            Account account = new Account(id, name, email, role, "Active");
            account.passwordConfigured = true;
            recordChange(account);
            accounts.add(account);
            creating = false;
            selectedAccount = account;
            passwordField.setText("");
            passwordField.setEnabled(false);
            idField.setText(account.id);
            refreshTable(searchField.getText().trim());
            selectTableAccount(account.id);
            setStatus("User account created successfully. ID: " + id + ".", Theme.ACCENT);
        } else {
            if (selectedAccount == null) {
                setStatus("Select an account or click Create Account first.", Theme.DANGER);
                return;
            }
            selectedAccount.name = name;
            selectedAccount.email = email;
            selectedAccount.role = role;
            if (!"Active".equals(statusBox.getSelectedItem())) selectedAccount.sessionActive = false;
            recordChange(selectedAccount);
            Account updatedAccount = selectedAccount;
            String changedId = updatedAccount.id;
            refreshTable(searchField.getText().trim());
            selectTableAccount(changedId);
            setStatus("User account updated successfully. Changed by " + ADMIN_ID
                + " at " + updatedAccount.lastModified + ".", Theme.ACCENT);
        }
        updateButtons();
    }

    private void changeAccountStatus(boolean activate) {
        if (!checkAdministratorPermission()) return;
        if (selectedAccount == null) {
            setStatus("Select an account first.", Theme.DANGER);
            return;
        }
        if (selectedAccount.id.equals(ADMIN_ID) && !activate) {
            setStatus("The current administrator account cannot deactivate itself.", Theme.DANGER);
            return;
        }
        String action = activate ? "reactivate" : "deactivate";
        int answer = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to " + action + " " + selectedAccount.name + "?",
            "Confirm Account Status", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;

        selectedAccount.status = activate ? "Active" : "Deactivated";
        if (!activate) selectedAccount.sessionActive = false;
        recordChange(selectedAccount);
        String changedId = selectedAccount.id;
        refreshTable(searchField.getText().trim());
        selectTableAccount(changedId);
        setStatus("Account " + (activate ? "reactivated" : "deactivated")
            + " successfully. " + (activate ? "The user can log in again." :
                "Login is blocked and the existing session is ended.")
            + " Changed by " + ADMIN_ID + " at " + LocalDateTime.now().format(auditFormat()) + ".",
            Theme.ACCENT);
    }

    private boolean checkAdministratorPermission() {
        if (!administratorActive) {
            setStatus("Permission denied. An active administrator account is required.", Theme.DANGER);
            return false;
        }
        return true;
    }

    private void clearForm() {
        userTable.clearSelection();
        selectedAccount = null;
        creating = false;
        idField.setText("");
        nameField.setText("");
        emailField.setText("");
        roleBox.setSelectedIndex(0);
        statusBox.setSelectedItem("Active");
        passwordField.setText("");
        passwordField.setEnabled(false);
        auditLabel.setText("Select an account or click Create Account.");
        updateButtons();
        setStatus("Account form cleared.", Theme.NEUTRAL);
    }

    private void updateButtons() {
        boolean canSave = administratorActive && (creating || selectedAccount != null);
        saveButton.setEnabled(canSave);
        deactivateButton.setEnabled(administratorActive && selectedAccount != null
            && "Active".equals(selectedAccount.status) && !selectedAccount.id.equals(ADMIN_ID));
        reactivateButton.setEnabled(administratorActive && selectedAccount != null
            && "Deactivated".equals(selectedAccount.status));
        if (creating) statusBox.setSelectedItem("Active");
    }

    private void updateAuditLabel(Account account) {
        if (account.lastModified == null) {
            auditLabel.setText("No previous account changes recorded.");
        } else {
            auditLabel.setText("Last changed by " + account.changedBy + " at " + account.lastModified + ".");
        }
    }

    private void recordChange(Account account) {
        account.changedBy = ADMIN_ID;
        account.lastModified = LocalDateTime.now().format(auditFormat());
    }

    private DateTimeFormatter auditFormat() {
        return DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);
    }

    private Account findAccount(String id) {
        for (Account account : accounts) if (account.id.equals(id)) return account;
        return null;
    }

    private void selectTableAccount(String id) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (tableModel.getValueAt(row, 0).toString().equals(id)) {
                userTable.setRowSelectionInterval(row, row);
                return;
            }
        }
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    private static final class Account {
        final String id;
        String name, email, role, status, changedBy, lastModified;
        boolean passwordConfigured;
        boolean sessionActive = true;
        Account(String id, String name, String email, String role, String status) {
            this.id = id; this.name = name; this.email = email;
            this.role = role; this.status = status;
            this.sessionActive = "Active".equals(status);
        }
    }
}
