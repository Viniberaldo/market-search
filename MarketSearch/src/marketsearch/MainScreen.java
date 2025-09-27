package marketsearch;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

/**
 * Interface Principal do Sistema
 */
public class MainScreen extends javax.swing.JFrame {

    /**
     * Classe que implementa a lógica de busca
     */
    private MarketSearch marketSearch;

    /**
     * Construtor da interface gráfica
     */
    public MainScreen() {
        initComponents();
        init();
    }

    /**
     * Ajustes de interface
     */
    private void init() {

        this.setTitle("Sistema de Comparação de Preços - E-commerce Brasil");
        this.setLocationRelativeTo(null); // Centralizar na tela

        // Placeholder text
        textCEP.setText("01234-567");
        textProduto.setText("Digite o nome do produto ou código de barras.");

        // Limpar campos quando clicados
        textCEP.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (textCEP.getText().equals("01234-567")) {
                    textCEP.setText("");
                }
            }
        });

        textProduto.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (textProduto.getText().equals("Digite o nome do produto ou"
                        + " código de barras.")) {
                    textProduto.setText("");
                }
            }
        });
    }

    /**
     * Action do botão Fechar.
     */
    private void buttonFecharActionPerformed(ActionEvent evt) {
        System.exit(0);
    }

    /**
     * Action do botão OK.
     */
    private void buttonOKActionPerformed(ActionEvent evt) {
        if (resultArea.getText() != null) {
            resultArea.setText("");
        }

        String cep = textCEP.getText().trim();
        String produto = textProduto.getText().trim();

        if (cep.isEmpty() || produto.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, preencha tanto o CEP quanto o nome do produto!",
                    "Campos obrigatórios",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (marketSearch == null) {
            String apiKey = JOptionPane.showInputDialog(this,
                    "Digite sua API Key do Google Gemini:",
                    "Configuração da API",
                    JOptionPane.PLAIN_MESSAGE);

            if (apiKey == null || apiKey.trim().isEmpty()) {
                return;
            }

            marketSearch = new MarketSearch(apiKey.trim());
        }

        // Executar pesquisa em thread separada para não travar a interface
        SwingWorker<List<MarketSearch.ProductResult>, Void> worker
                = new SwingWorker<>() {

            @Override
            protected List<MarketSearch.ProductResult> doInBackground()
                    throws Exception {
                return marketSearch.searchPrices(cep, produto);
            }

            @Override
            protected void done() {
                try {
                    List<MarketSearch.ProductResult> results = get();
                    displayResults(results, produto);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainScreen.this,
                            "Erro ao pesquisar preços: " + e.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                    buttonOK.setEnabled(true);
                }
            }
        };

        // Mostrar progresso
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        buttonOK.setEnabled(false);

        worker.execute();
    }

    private void displayResults(List<MarketSearch.ProductResult> results,
            String produto) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== COMPARAÇÃO DE PREÇOS - ")
                .append(produto.toUpperCase()).append(" ===\n\n");

        if (results.isEmpty()) {
            sb.append("❌ Nenhum resultado encontrado.\n");
            sb.append("Verifique se o nome do produto está correto.");
        } else {
            sb.append("📊 ").append(results.size())
                    .append(" resultados encontrados (por preço):\n\n");

            for (int i = 0; i < results.size(); i++) {
                MarketSearch.ProductResult result = results.get(i);
                sb.append("🏆 OPÇÃO ").append(i + 1);
                if (i == 0) {
                    sb.append(" - MELHOR PREÇO! 💰");
                }
                sb.append("\n");
                sb.append(result.toString()).append("\n");
            }

            // Resumo
            sb.append("═══════════════════════════════════════\n");
            sb.append("💡 RESUMO:\n");
            sb.append("• Melhor preço: ").append(results.get(0).getStoreName())
                    .append(" - R$ ").append(String.format("%.2f",
                    results.get(0).getPrice())).append("\n");

            if (results.size() > 1) {
                double saving = results.get(results.size() - 1)
                        .getPrice() - results.get(0).getPrice();
                sb.append("• Economia máxima: R$ ").append(
                        String.format("%.2f", saving)).append("\n");
            }
        }

        resultArea.setText(sb.toString());
        resultArea.setCaretPosition(0); // Scroll para o topo
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception ex) {
            System.out.println("Erro ao definir Look and Feel: "
                    + ex.getMessage());
        }

        SwingUtilities.invokeLater(() -> new MainScreen().setVisible(true));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        resultArea = new javax.swing.JTextArea();
        labelCEP = new javax.swing.JLabel();
        textCEP = new javax.swing.JTextField();
        labelProduto = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textProduto = new javax.swing.JTextArea();
        buttonOK = new javax.swing.JButton();
        buttonFechar = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        labelResultados = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        resultArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(650, 600));
        setMinimumSize(new java.awt.Dimension(650, 600));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        textCEP.setText("");
        textCEP.setToolTipText("Digite o CEP (ex: 01234-567)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(textCEP, gridBagConstraints);

        textProduto.setColumns(20);
        textProduto.setRows(3);
        textProduto.setToolTipText("Digite o nome do produto ou código de barras");
        textProduto.setMaximumSize(new java.awt.Dimension(300, 80));
        textProduto.setMinimumSize(new java.awt.Dimension(300, 80));
        textProduto.setPreferredSize(new java.awt.Dimension(300, 80));
        jScrollPane1.setViewportView(textProduto);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        labelCEP.setText("CEP:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        getContentPane().add(labelCEP, gridBagConstraints);

        labelProduto.setText("Produto:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        getContentPane().add(labelProduto, gridBagConstraints);

        buttonFechar.setText("Fechar");
        buttonFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonFecharActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 10);
        getContentPane().add(buttonFechar, gridBagConstraints);

        buttonOK.setText("Pesquisar Preços");
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        getContentPane().add(buttonOK, gridBagConstraints);

        resultArea.setColumns(20);
        resultArea.setRows(15);
        resultArea.setEditable(false);
        resultArea.setFont(new java.awt.Font("SansSerif", 0, 12));
        jScrollPane2.setViewportView(resultArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jScrollPane2, gridBagConstraints);

        labelResultados.setText("Resultados da Pesquisa:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
        getContentPane().add(labelResultados, gridBagConstraints);

        progressBar.setVisible(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        getContentPane().add(progressBar, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea resultArea;
    private javax.swing.JButton buttonFechar;
    private javax.swing.JButton buttonOK;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelCEP;
    private javax.swing.JLabel labelProduto;
    private javax.swing.JLabel labelResultados;
    private javax.swing.JTextField textCEP;
    private javax.swing.JTextArea textProduto;
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables
}
