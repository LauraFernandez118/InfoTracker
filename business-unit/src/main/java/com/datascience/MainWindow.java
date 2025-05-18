package com.datascience;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.InputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainWindow extends JFrame {

    private static final Color LIGHT_PINK = new Color(255, 209, 220);
    private static final Color LIGHT_VIOLET = new Color(204, 153, 255);
    private JLabel statusLabel;

    private final DataMart dataMart;
    private final EventLoader loader;

    public MainWindow(DataMart dataMart, EventLoader loader) {
        this.dataMart = dataMart;
        this.loader = loader;
        configureWindow();
        initComponents();
    }

    private void configureWindow() {
        setTitle("Infotracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(LIGHT_PINK);
        try {
            InputStream imgStream = Files.newInputStream(Paths.get("src/main/resources/logo.png"));
            if (imgStream != null) {
                Image logoImage = ImageIO.read(imgStream);
                setIconImage(logoImage);
            } else {
                System.err.println("Error: No se encontró el logo en /images/logo.png");
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el logo:");
            e.printStackTrace();
        }
    }
    public void updateStatus(String status) {
        statusLabel.setText(" " + status);
    }

    private void initComponents() {
        statusLabel = new JLabel(" Listo");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(LIGHT_PINK);
        tabbedPane.setForeground(new Color(94, 11, 51));


        SearchPanel searchPanel = new SearchPanel(dataMart, loader);
        ContentPanel historialPanel = new ContentPanel();


        tabbedPane.addTab("Búsqueda", createIcon("🔍"), searchPanel);
        tabbedPane.addTab("Historial", createIcon("📜"), historialPanel);


        searchPanel.setContentUpdateListener(historialPanel::refreshContent);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private Icon createIcon(String emoji) {
        JLabel label = new JLabel(emoji);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                label.setBounds(x, y, getIconWidth(), getIconHeight());
                label.paint(g);
            }
            public int getIconWidth() { return 25; }
            public int getIconHeight() { return 25; }
        };

    }

}
