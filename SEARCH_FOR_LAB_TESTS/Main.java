import javax.swing.*;

// Entry point: starts the PULSE Patient Portal on the Swing thread.
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting PULSE...");
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("ERROR: This Java has no display (headless). "
                + "Run it on your own computer in IntelliJ / Eclipse / VS Code, not an online compiler or server.");
            return;
        }
        System.setProperty("swing.boldMetal", "false");
        SwingUtilities.invokeLater(() -> {
            try {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ignored) {
                    // Falls back to the default look and feel
                }
                MainFrame frame = new MainFrame();
                frame.setExtendedState(JFrame.NORMAL);
                frame.toFront();          // brings the window above the IDE
                frame.requestFocus();
                System.out.println("PULSE window opened.");
            } catch (Throwable t) {
                t.printStackTrace();
                JOptionPane.showMessageDialog(null, "PULSE failed to start:\n" + t,
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
