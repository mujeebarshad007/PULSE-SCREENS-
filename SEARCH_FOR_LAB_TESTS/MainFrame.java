import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

// Home screen of the PULSE Patient Portal.
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("PULSE Patient Portal");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(Theme.banner("PULSE Patient Portal",
            "Find lab tests, compare laboratories and book appointments"), BorderLayout.NORTH);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(Theme.BG);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(200, 215, 225)),
            new EmptyBorder(30, 50, 30, 50)));

        JLabel heading = new JLabel("Search for Lab Test");
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.PRIMARY);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel steps = new JLabel("<html><div style='text-align:center; width:430px'>"
            + "Choose a category or type a test name, compare laboratories and locations, "
            + "read the preparation instructions, then book an appointment "
            + "or view your past records.</div></html>");
        steps.setFont(Theme.BODY);
        steps.setAlignmentX(Component.CENTER_ALIGNMENT);

        Theme.PulseButton searchButton = new Theme.PulseButton("Search for Lab Test", Theme.PRIMARY);
        Theme.PulseButton loginButton = new Theme.PulseButton("Patient Login", Theme.ACCENT);
        Theme.PulseButton exitButton = new Theme.PulseButton("Exit", Theme.NEUTRAL);

        searchButton.addActionListener(e -> {
            new Signup();
            dispose();
        });
        loginButton.addActionListener(e -> {
            new Login();
            dispose();
        });
        exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?",
                "Exit PULSE", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) System.exit(0);
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);
        buttons.add(searchButton);
        buttons.add(loginButton);
        buttons.add(exitButton);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(heading);
        card.add(Box.createVerticalStrut(14));
        card.add(steps);
        card.add(Box.createVerticalStrut(26));
        card.add(buttons);

        background.add(card);
        add(background, BorderLayout.CENTER);
        setVisible(true);
    }
}
