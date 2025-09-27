package marketsearch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sistema de Comparação de Preços para E-commerce Brasileiro Integrado com
 * interface Swing
 */
public class MarketSearch {

    private static final String GEMINI_API_URL
            = "https://generativelanguage.googleapis.com/v1beta/models/"
            + "gemini-2.0-flash-exp:generateContent";
    private String apiKey;
    private String apiUrl;

    /**
     * Construtor principal
     */
    public MarketSearch(String apiKey, String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl != null ? apiUrl : GEMINI_API_URL;
    }

    /**
     * Construtor simplificado (usa Gemini 2.5 Flash por padrão)
     */
    public MarketSearch(String apiKey) {
        this(apiKey, null);
    }

    /**
     * Classe para representar um produto encontrado
     */
    public static class ProductResult {

        private String storeName;
        private double price;
        private String availability;
        private String link;

        public ProductResult(String storeName, double price,
                String availability, String link) {
            this.storeName = storeName;
            this.price = price;
            this.availability = availability;
            this.link = link;
        }

        // Getters
        public String getStoreName() {
            return storeName;
        }

        public double getPrice() {
            return price;
        }

        public String getAvailability() {
            return availability;
        }

        public String getLink() {
            return link;
        }

        @Override
        public String toString() {
            return String.format("🏪 %s\n💰 R$ %.2f\n📦 %s\n🔗 %s\n",
                    storeName, price, availability, link);
        }
    }

    /**
     * Método principal para pesquisar preços
     */
    public List<ProductResult> searchPrices(String cep, String produto) {
        try {
            String prompt = buildPrompt(cep, produto);
            String aiResponse = callAI(prompt);
            return parseResults(aiResponse);
        } catch (Exception e) {
            System.err.println("Erro ao pesquisar preços: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Constrói o prompt para a IA
     */
    private String buildPrompt(String cep, String produto) {
        return String.format(
                "Você é um especialista em e-commerce brasileiro. "
                + "Preciso que pesquise preços do produto: %s\n\n"
                + "Pesquise nos principais E-COMMERCE brasileiros "
                + "(Mercado Livre, Amazon, Magazine Luiza, Casas Bahia,"
                + " Submarino, Americanas, etc.).\n\n"
                + "Para cada site, retorne:\n"
                + "1. Nome da loja\n"
                + "2. Preço encontrado (em reais)\n"
                + "3. Disponibilidade\n"
                + "4. Link\n\n"
                + "IMPORTANTE:\n"
                + "- Use preços REALISTAS para o produto solicitado\n"
                + "- Considere frete para o CEP %s\n"
                + "- Seja específico e detalhado\n"
                + "- Retorne exatamente 5 opções\n\n"
                + "- Na linha de preço, retorne somente o valor numérico\n\n"
                + "Formato de resposta (OBRIGATÓRIO):\n"
                + "LOJA: [Nome]\n"
                + "PREÇO: R$ [valor]\n"
                + "STATUS: [disponível/indisponível]\n"
                + "LINK: [url exemplo]\n"
                + "---\n\n"
                + "Apresente as 5 melhores opções de compra baseada em preço e"
                + " frete para o CEP %s.",
                produto, cep, cep
        );
    }

    /**
     * Chama a API do Google Gemini
     */
    private String callAI(String prompt) throws Exception {
        String fullUrl = apiUrl + "?key=" + apiKey;
        URL url = new URL(fullUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String jsonPayload = String.format(
                "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}],"
                + "\"generationConfig\":{\"temperature\":0.7,"
                + "\"maxOutputTokens\":2000}}",
                prompt.replace("\"", "\\\"").replace("\n", "\\n")
                        .replace("\r", "")
        );

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Erro na API: Código " + responseCode + " - "
                    + connection.getResponseMessage());
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        String jsonResponse = response.toString();
        Pattern pattern = Pattern.compile("\"text\":\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(jsonResponse);

        if (matcher.find()) {
            String content = matcher.group(1);
            content = content.replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\r", "")
                    .replace("\\t", "\t");
            return content;
        }

        throw new Exception("Não foi possível extrair a resposta do Gemini."
                + " Resposta: " + jsonResponse);
    }

    /**
     * Interpreta a resposta da IA para extrair os produtos
     */
    private List<ProductResult> parseResults(String aiResponse) {
        List<ProductResult> results = new ArrayList<>();
        String[] blocks = aiResponse.split("---");

        for (String block : blocks) {
            try {
                ProductResult result = parseBlock(block.trim());
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                System.err.println("Erro ao parsear bloco: " + e.getMessage());
            }
        }

        Collections.sort(results, (a, b) -> Double.compare(a.getPrice(),
                b.getPrice()));

        return results.size() > 5 ? results.subList(0, 5) : results;
    }

    /**
     * Parseia um bloco individual de resultado
     */
    private ProductResult parseBlock(String block) {
        if (block.isEmpty()) {
            return null;
        }

        String storeName = extractValue(block, "LOJA:");
        String priceStr = extractValue(block, "PREÇO:");
        String availability = extractValue(block, "STATUS:");
        String link = extractValue(block, "LINK:");

        if (storeName != null && priceStr != null) {
            try {
                double price = extractPrice(priceStr);
                return new ProductResult(storeName, price,
                        availability != null ? availability : "disponível",
                        link != null ? link : "N/A");
            } catch (NumberFormatException e) {
                System.err.println("Erro ao converter preço: " + priceStr);
            }
        }

        return null;
    }

    /**
     * Extrai valor de uma linha do formato "CHAVE: valor"
     */
    private String extractValue(String block, String key) {
        String[] lines = block.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith(key)) {
                return line.substring(line.indexOf(key) + key.length()).trim();
            }
        }
        return null;
    }

    /**
     * Extrai preço numérico de string
     */
    private double extractPrice(String priceStr) {
        String cleanPrice = priceStr.replaceAll("[^0-9,.]", "");

        if (cleanPrice.contains(",") && !cleanPrice.contains(".")) {
            cleanPrice = cleanPrice.replace(",", ".");
        } else if (cleanPrice.contains(",") && cleanPrice.contains(".")) {
            cleanPrice = cleanPrice.replace(".", "").replace(",", ".");
        }

        return Double.parseDouble(cleanPrice);
    }
}
