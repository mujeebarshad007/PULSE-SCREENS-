import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

// Patient login screen. Demo account: username "patient", password "pulse123".
public class Login extends JFrame {

    private static final String DEMO_USER = "patient";
    private static final String DEMO_PASS = "pulse123";
    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_SECONDS = 10;

    private final JPanel center = new JPanel(new GridBagLayout());
    private final JTextField userField = new JTextField(18);
    private final JPasswordField passField = new JPasswordField(18);
    private final JCheckBox showPassword = new JCheckBox("Show password");
    private final JLabel message = new JLabel(" ");
    private final Theme.PulseButton loginButton = new Theme.PulseButton("Login", Theme.PRIMARY);
    private int attempts = 0;
    private int lockLeft = 0;

    public Login() {
        setTitle("PULSE - Patient Login");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setJMenuBar(buildMenuBar());
        add(Theme.banner("Patient Login", "Sign in to access your PULSE account"), BorderLayout.NORTH);

        center.setBackground(Theme.BG);
        center.add(buildLoginCard());
        add(center, BorderLayout.CENTER);

        getRootPane().setDefaultButton(loginButton); // Enter key logs in
        setVisible(true);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem home = new JMenuItem("Home");
        JMenuItem exit = new JMenuItem("Exit");
        home.addActionListener(e -> goHome());
        exit.addActionListener(e -> System.exit(0));
        file.add(home);
        file.addSeparator();
        file.add(exit);

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "PULSE Patient Portal\nSearch lab tests, book appointments and view past records.",
            "About PULSE", JOptionPane.INFORMATION_MESSAGE));
        help.add(about);

        bar.add(file);
        bar.add(help);
        return bar;
    }

    private JPanel buildLoginCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(200, 215, 225)),
            new EmptyBorder(26, 40, 26, 40)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        JLabel heading = new JLabel("Welcome back", SwingConstants.CENTER);
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.PRIMARY);

        JLabel hint = new JLabel("Demo account:  patient / pulse123", SwingConstants.CENTER);
        hint.setFont(new Font("Arial", Font.ITALIC, 12));
        hint.setForeground(Theme.NEUTRAL);

        message.setFont(new Font("Arial", Font.BOLD, 12));
        message.setHorizontalAlignment(SwingConstants.CENTER);

        showPassword.setOpaque(false);
        char defaultEcho = passField.getEchoChar();
        showPassword.addActionListener(e ->
            passField.setEchoChar(showPassword.isSelected() ? (char) 0 : defaultEcho));

        loginButton.addActionListener(e -> attemptLogin());
        Theme.PulseButton backButton = new Theme.PulseButton("Back", Theme.NEUTRAL);
        backButton.addActionListener(e -> goHome());
        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        buttons.add(loginButton);
        buttons.add(backButton);

        int row = 0;
        gbc.gridy = row++; card.add(heading, gbc);
        gbc.gridy = row++; card.add(hint, gbc);
        gbc.gridy = row++; gbc.insets = new Insets(14, 0, 2, 0); card.add(new JLabel("Username"), gbc);
        gbc.gridy = row++; gbc.insets = new Insets(2, 0, 5, 0); card.add(userField, gbc);
        gbc.gridy = row++; gbc.insets = new Insets(8, 0, 2, 0); card.add(new JLabel("Password"), gbc);
        gbc.gridy = row++; gbc.insets = new Insets(2, 0, 5, 0); card.add(passField, gbc);
        gbc.gridy = row++; card.add(showPassword, gbc);
        gbc.gridy = row++; gbc.insets = new Insets(8, 0, 4, 0); card.add(message, gbc);
        gbc.gridy = row; gbc.insets = new Insets(6, 0, 0, 0); card.add(buttons, gbc);
        return card;
    }

    private void attemptLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            setMessage("Please enter both username and password.", Theme.DANGER);
            return;
        }
        if (user.equals(DEMO_USER) && pass.equals(DEMO_PASS)) {
            showLoggedIn(user);
            return;
        }
        attempts++;
        passField.setText("");
        passField.requestFocusInWindow();
        if (attempts >= MAX_ATTEMPTS) {
            startLock();
        } else {
            setMessage("Invalid username or password. Attempt " + attempts + " of " + MAX_ATTEMPTS + ".",
                Theme.DANGER);
        }
    }

    // Locks the form for a few seconds after too many wrong attempts
    private void startLock() {
        lockLeft = LOCK_SECONDS;
        loginButton.setEnabled(false);
        userField.setEnabled(false);
        passField.setEnabled(false);
        setMessage("Too many failed attempts. Try again in " + lockLeft + " seconds.", Theme.DANGER);
        Timer timer = new Timer(1000, null);
        timer.addActionListener(e -> {
            lockLeft--;
            if (lockLeft <= 0) {
                timer.stop();
                attempts = 0;
                loginButton.setEnabled(true);
                userField.setEnabled(true);
                passField.setEnabled(true);
                setMessage("You can try again now.", Theme.NEUTRAL);
            } else {
                setMessage("Too many failed attempts. Try again in " + lockLeft + " seconds.", Theme.DANGER);
            }
        });
        timer.start();
    }

    private void showLoggedIn(String user) {
        getRootPane().setDefaultButton(null);
        center.removeAll();

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(200, 215, 225)),
            new EmptyBorder(30, 50, 30, 50)));

        JLabel success = new JLabel("You are successfully logged in to the Application");
        success.setFont(Theme.HEADING);
        success.setForeground(Theme.ACCENT);
        success.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel welcome = new JLabel("Welcome, " + user + ". What would you like to do?");
        welcome.setFont(Theme.BODY);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        Theme.PulseButton search = new Theme.PulseButton("Search for Lab Test", Theme.PRIMARY);
        Theme.PulseButton logout = new Theme.PulseButton("Logout", Theme.NEUTRAL);
        search.addActionListener(e -> {
            new Signup();
            dispose();
        });
        logout.addActionListener(e -> goHome());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);
        buttons.add(search);
        buttons.add(logout);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(success);
        card.add(Box.createVerticalStrut(10));
        card.add(welcome);
        card.add(Box.createVerticalStrut(24));
        card.add(buttons);

        center.add(card);
        center.revalidate();
        center.repaint();
    }

    private void setMessage(String text, Color color) {
        message.setText(text);
        message.setForeground(color);
    }

    private void goHome() {
        new MainFrame();
        dispose();
    }
}
