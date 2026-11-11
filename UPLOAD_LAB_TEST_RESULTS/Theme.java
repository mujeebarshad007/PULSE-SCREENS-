import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Shared colours, fonts and styled components so every PULSE screen looks the same.
public final class Theme {

    public static final Color PRIMARY = new Color(13, 110, 140);
    public static final Color ACCENT = new Color(0, 140, 105);
    public static final Color DANGER = new Color(178, 34, 34);
    public static final Color NEUTRAL = new Color(90, 105, 115);
    public static final Color BG = new Color(240, 247, 250);
    public static final Color INFO_BG = new Color(232, 243, 255);
    public static final Color INFO_BORDER = new Color(150, 190, 230);

    public static final Font TITLE = new Font("Arial", Font.BOLD, 26);
    public static final Font HEADING = new Font("Arial", Font.BOLD, 20);
    public static final Font BODY = new Font("Arial", Font.PLAIN, 14);
    public static final Font BOLD = new Font("Arial", Font.BOLD, 14);

    private Theme() { }

    // Coloured header strip shown at the top of every screen
    public static JPanel banner(String title, String subtitle) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 2));
        panel.setBackground(PRIMARY);
        panel.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(TITLE);
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel(subtitle, SwingConstants.CENTER);
        s.setFont(new Font("Arial", Font.PLAIN, 13));
        s.setForeground(new Color(205, 232, 242));
        panel.add(t);
        panel.add(s);
        return panel;
    }

    // Rounded button with hover, pressed and disabled looks
    public static class PulseButton extends JButton {
        private final Color base;
        private boolean hover;

        public PulseButton(String text, Color base) {
            super(text);
            this.base = base;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(BOLD);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 18, 8, 18));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c;
            if (!isEnabled()) c = new Color(214, 220, 224);
            else if (getModel().isPressed()) c = base.darker().darker();
            else if (hover) c = base.darker();
            else c = base;
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
