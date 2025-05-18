package com.datascience;


import java.awt.font.TextAttribute;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import java.util.Map;


public class SearchPanel extends JPanel {

    private static final Color LIGHT_PINK = new Color(255, 209, 220);
    private static final Color LIGHT_VIOLET = new Color(188, 141, 246);
    private static final Color DARK_VIOLET = new Color(178, 102, 255);

    private DefaultTableModel tableModel;
    private JToggleButton newsToggle, videosToggle;
    private Runnable contentUpdateListener;

    public SearchPanel(DataMart dataMart, EventLoader loader) {
        setBackground(LIGHT_PINK);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));


        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(LIGHT_PINK);


        JTextField searchField = new JTextField(25);
        newsToggle = createToggle("Noticias", true);
        videosToggle = createToggle("Videos", true);
        JButton searchButton = createButton("Buscar", DARK_VIOLET);

        searchButton.addActionListener(e -> performSearch(searchField.getText().trim()));
        searchField.addActionListener(e -> performSearch(searchField.getText().trim()));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Tema:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        controlPanel.add(searchField, gbc);

        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        controlPanel.add(searchButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        controlPanel.add(new JLabel("Filtrar:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        togglePanel.setBackground(LIGHT_PINK);
        togglePanel.add(newsToggle);
        togglePanel.add(videosToggle);
        controlPanel.add(togglePanel, gbc);

        tableModel = new DefaultTableModel(new String[]{"Tipo", "Fecha", "Título", "Fuente/Canal", "URL"}, 0);
        JTable resultsTable = new JTable(tableModel);
        styleTable(resultsTable);

        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultsTable), BorderLayout.CENTER);
    }

    private JToggleButton createToggle(String text, boolean selected) {
        JToggleButton toggle = new JToggleButton(text, selected);
        toggle.setBackground(selected ? DARK_VIOLET : LIGHT_VIOLET);
        toggle.setForeground(Color.WHITE);
        toggle.addItemListener(e -> toggle.setBackground(
                e.getStateChange() == ItemEvent.SELECTED ? DARK_VIOLET : LIGHT_VIOLET));
        return toggle;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBorderPainted(false);
        return button;
    }

    private void styleTable(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                if (column == 4) {
                    c.setForeground(Color.BLUE.darker());
                    Font font = c.getFont();
                    Map<TextAttribute, Object> attributes = (Map<TextAttribute, Object>) font.getAttributes();
                    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                    c.setFont(font.deriveFont(attributes));

                    if (isSelected) {
                        c.setForeground(Color.WHITE);
                    }
                }

                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                if (isSelected) {
                    c.setBackground(DARK_VIOLET);
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (col == 4 && row >= 0) { // Si se hace clic en la columna de URL
                    String url = (String) table.getValueAt(row, col);
                    if (url != null && !url.isEmpty()) {
                        try {
                            Desktop.getDesktop().browse(new URI(url));
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(SearchPanel.this,
                                    "No se pudo abrir el enlace: " + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
    }

    private void performSearch(String topic) {
        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un tema de búsqueda", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);

        if (newsToggle.isSelected()) {
            Main.fetchNews(topic, this::addResult, this::searchComplete);
        }
        if (videosToggle.isSelected()) {
            Main.fetchYouTubeVideos(topic, this::addResult, this::searchComplete);
        }
    }
    private void addResult(String result) {
        SwingUtilities.invokeLater(() -> {
            String[] parts = result.split("\\|");
            if (parts.length >= 5) {
                tableModel.addRow(new Object[]{
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        parts[4].trim() // URL
                });
            }
        });
    }

    private void searchComplete() {
        if (contentUpdateListener != null) {
            contentUpdateListener.run();
        }
    }

    public void setContentUpdateListener(Runnable listener) {
        this.contentUpdateListener = listener;
    }

}
