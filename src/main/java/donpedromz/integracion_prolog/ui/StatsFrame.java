package donpedromz.integracion_prolog.ui;

import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Vista simple para mostrar estadisticas (enfermedades y sintomas mas frecuentes).
 */
public class StatsFrame extends javax.swing.JFrame {

    public StatsFrame() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }

    public void loadData(List<Object[]> diseases, List<Object[]> symptoms) {
        boolean hasDiseases = diseases != null && !diseases.isEmpty();
        boolean hasSymptoms = symptoms != null && !symptoms.isEmpty();

        if (!hasDiseases && !hasSymptoms) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "No hay diagnosticos almacenados para generar estadisticas.",
                    "Sin datos",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE
            );
            diseasesChartPanel.removeAll();
            symptomsChartPanel.removeAll();
            diseasesChartPanel.revalidate();
            symptomsChartPanel.revalidate();
            return;
        }

        DefaultCategoryDataset diseaseDataset = new DefaultCategoryDataset();
        if (diseases != null) {
            for (Object[] row : diseases) {
                String label = row != null && row.length > 0 ? String.valueOf(row[0]) : "";
                long count = (row != null && row.length > 1 && row[1] instanceof Number) ? ((Number) row[1]).longValue() : 0L;
                diseaseDataset.addValue(count, "Enfermedades", label);
            }
        }

        DefaultCategoryDataset symptomDataset = new DefaultCategoryDataset();
        if (symptoms != null) {
            for (Object[] row : symptoms) {
                String label = row != null && row.length > 0 ? String.valueOf(row[0]) : "";
                long count = (row != null && row.length > 1 && row[1] instanceof Number) ? ((Number) row[1]).longValue() : 0L;
                symptomDataset.addValue(count, "Sintomas", label);
            }
        }

        JFreeChart diseasesChart = ChartFactory.createBarChart(
                "Enfermedades mas frecuentes",
                "Enfermedad",
                "Conteo",
                diseaseDataset
        );

        JFreeChart symptomsChart = ChartFactory.createBarChart(
                "Sintomas mas frecuentes",
                "Sintoma",
                "Conteo",
                symptomDataset
        );

        diseasesChartPanel.removeAll();
        diseasesChartPanel.add(new ChartPanel(diseasesChart), java.awt.BorderLayout.CENTER);
        diseasesChartPanel.revalidate();

        symptomsChartPanel.removeAll();
        symptomsChartPanel.add(new ChartPanel(symptomsChart), java.awt.BorderLayout.CENTER);
        symptomsChartPanel.revalidate();
    }

    public javax.swing.JPanel getCloseButton() {
        return closeButton;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        diseasesChartPanel = new javax.swing.JPanel();
        symptomsChartPanel = new javax.swing.JPanel();
        closeButton = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Estadisticas");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 10, 200, 30));

        diseasesChartPanel.setLayout(new java.awt.BorderLayout());
        jPanel1.add(diseasesChartPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 300, 300));

        symptomsChartPanel.setLayout(new java.awt.BorderLayout());
        jPanel1.add(symptomsChartPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 60, 300, 300));

        closeButton.setBackground(new java.awt.Color(204, 204, 204));
        closeButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeButtonMouseClicked(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Cerrar");

        javax.swing.GroupLayout closeButtonLayout = new javax.swing.GroupLayout(closeButton);
        closeButton.setLayout(closeButtonLayout);
        closeButtonLayout.setHorizontalGroup(
            closeButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(closeButtonLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                .addContainerGap())
        );
        closeButtonLayout.setVerticalGroup(
            closeButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(closeButtonLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.add(closeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 380, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeButtonMouseClicked
        dispose();
    }//GEN-LAST:event_closeButtonMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel closeButton;
    private javax.swing.JPanel diseasesChartPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel symptomsChartPanel;
    // End of variables declaration//GEN-END:variables
}