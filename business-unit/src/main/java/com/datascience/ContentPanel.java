package com.datascience;


import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;


public class ContentPanel extends JPanel {

    private static final Color LIGHT_PINK = new Color(255, 209, 220);
    private static final Color LIGHT_VIOLET = new Color(204, 153, 255);
    private static final Color DARK_VIOLET = new Color(178, 102, 255);
    private static final Color TEXT_COLOR = new Color(94, 11, 51);

    private DefaultTableModel tableModel;
    private JTable historialTable;

    public ContentPanel() {
        setBackground(LIGHT_PINK);
        initUI();
        refreshContent();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));


        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(LIGHT_PINK);

        JLabel titleLabel = new JLabel("Historial de Búsquedas");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);




        topPanel.add(titleLabel, BorderLayout.WEST);


        String[] columnNames = {"Fecha", "Tema", "Tipo", "Archivo"};
        tableModel = new DefaultTableModel(columnNames, 0);
        historialTable = new JTable(tableModel);
        styleTable(historialTable);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(historialTable), BorderLayout.CENTER);
    }

    public void refreshContent() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            loadHistorialData();
        });
    }

    private void loadHistorialData() {
        Path eventstore = Paths.get("eventstore");
        if (!Files.exists(eventstore)) return;

        try (Stream<Path> topics = Files.list(eventstore)) {
            topics.filter(Files::isDirectory)
                    .forEach(this::processTopic);
        } catch (IOException e) {
            showError("Error leyendo topics: " + e.getMessage());
        }
    }

    private void processTopic(Path topicPath) {
        String topicName = topicPath.getFileName().toString().replace("_", " ");

        try (Stream<Path> types = Files.list(topicPath)) {
            types.filter(Files::isDirectory)
                    .forEach(typePath -> processType(topicName, typePath));
        } catch (IOException e) {
            showError("Error leyendo tipos en " + topicName);
        }
    }

    private void processType(String topicName, Path typePath) {
        String type = typePath.getFileName().toString();

        try (Stream<Path> files = Files.list(typePath)) {
            files.filter(p -> p.toString().endsWith(".events"))
                    .sorted(Comparator.reverseOrder())
                    .forEach(file -> addToTable(topicName, type, file));
        } catch (IOException e) {
            showError("Error leyendo archivos en " + typePath);
        }
    }

    private void addToTable(String topic, String type, Path file) {
        try {
            String dateStr = file.getFileName().toString().substring(0, 8);
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
            String formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            tableModel.addRow(new Object[]{
                    formattedDate,
                    topic,
                    type.equals("news") ? "Noticias" : "Videos",
                    file.toString()
            });
        } catch (Exception e) {
            showError("Error procesando " + file.getFileName());
        }
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
    }

    private void styleTable(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                if (isSelected) {
                    c.setBackground(DARK_VIOLET);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setForeground(TEXT_COLOR);
                }
                return c;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setBackground(LIGHT_VIOLET);
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
