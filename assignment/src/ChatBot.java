import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ChatBot {

    // Replace with your actual API keys
    private static final String WEATHER_API_KEY = "b62ea93fe7d6995001a70369b9262d96";
    private static final String EXCHANGE_API_KEY = "f0c4df7498ad32d500b7bb07";  // Replace with your ExchangeRate-API key
    // Base URLs of the APIs
    private static final String WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String EXCHANGE_BASE_URL = "https://v6.exchangerate-api.com/v6/";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a city name: ");
        String city = scanner.nextLine();
        System.out.print("Enter a currency code (e.g., USD, EUR): ");
        String currencyCode = scanner.nextLine();
        scanner.close();

        try {
            String weatherResponse = getWeather(city);
            System.out.println("Weather info: " + weatherResponse);

            String exchangeResponse = getExchangeRate(currencyCode);
            System.out.println("Exchange rate info: " + exchangeResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static String getWeather(String city) throws Exception {
        // Encode the city name to ensure it's safe for URL
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());

        // Construct the full URL string with the base URL, API key, and city name
        String urlString = WEATHER_BASE_URL + "?q=" + encodedCity + "&appid=" + WEATHER_API_KEY + "&units=metric";

        // Print the URL to check its format
        System.out.println("Constructed URL: " + urlString);

        // Convert the URL string to a URI
        URI uri = new URI(urlString);

        // Convert the URI to a URL
        URL url = uri.toURL();

        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Get the response code from the server
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            // If response code is 200 (OK), read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Print the entire JSON response for debugging
            String response = content.toString();
            System.out.println("Weather API response: " + response);

            // Manually parse the JSON response to extract temperature information
            double temperature = parseJsonDouble(response, "\"temp\":");
            double feelsLike = parseJsonDouble(response, "\"feels_like\":");
            double tempMin = parseJsonDouble(response, "\"temp_min\":");
            double tempMax = parseJsonDouble(response, "\"temp_max\":");

            // Return the formatted temperature info
            return String.format("Current Temperature: %.2f°C\nFeels Like: %.2f°C\nMin Temperature: %.2f°C\nMax Temperature: %.2f°C",
                    temperature, feelsLike, tempMin, tempMax);
        } else {
            // If response code is not 200, throw an exception with the response message
            throw new Exception("Failed to get weather data. HTTP response code: " + responseCode);
        }
    }

    private static String getExchangeRate(String currencyCode) throws Exception {
        // Construct the full URL string with the base URL, API key, and base currency (USD)
        String urlString = EXCHANGE_BASE_URL + EXCHANGE_API_KEY + "/latest/USD";

        // Print the URL to check its format
        System.out.println("Constructed URL: " + urlString);

        // Convert the URL string to a URI
        URI uri = new URI(urlString);

        // Convert the URI to a URL
        URL url = uri.toURL();

        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Get the response code from the server
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            // If response code is 200 (OK), read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Print the entire JSON response for debugging
            String response = content.toString();
            System.out.println("Exchange Rate API response: " + response);

            // Manually parse the JSON response to extract exchange rate information
            String conversionRates = parseJsonObject(response, "\"conversion_rates\":");
            double exchangeRate = parseJsonDouble(conversionRates, "\"" + currencyCode + "\":");

            // Return the formatted exchange rate info
            return String.format("1 USD = %.2f %s", exchangeRate, currencyCode);
        } else {
            // If response code is not 200, throw an exception with the response message
            throw new Exception("Failed to get exchange rate data. HTTP response code: " + responseCode);
        }
    }

    // Helper method to extract double value from the JSON response
    private static double parseJsonDouble(String json, String key) {
        int start = json.indexOf(key) + key.length();
        // Find the first number after the key
        while (!Character.isDigit(json.charAt(start)) && json.charAt(start) != '-') {
            start++;
        }
        int end = start;
        while (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.') {
            end++;
        }
        return Double.parseDouble(json.substring(start, end).trim());
    }

    // Helper method to extract JSON object as a string
    private static String parseJsonObject(String json, String key) {
        int start = json.indexOf(key) + key.length();
        while (json.charAt(start) != '{') {
            start++;
        }
        int end = start;
        int braceCount = 0;
        do {
            if (json.charAt(end) == '{') {
                braceCount++;
            } else if (json.charAt(end) == '}') {
                braceCount--;
            }
            end++;
        } while (braceCount > 0);
        return json.substring(start, end);
    }
}
