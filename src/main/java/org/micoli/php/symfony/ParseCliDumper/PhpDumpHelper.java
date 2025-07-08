package org.micoli.php.symfony.ParseCliDumper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhpDumpHelper {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Parse l'output de cliDumper de Symfony et le transforme en JSON
     *
     * @param cliDumperOutput
     *            L'output du cliDumper
     * @return String JSON formaté
     */
    public static String parseCliDumperToJson(String cliDumperOutput) {
        String output = cliDumperOutput.trim();
        output = output.replace("\\n", "");

        JsonElement result = parseValue(output);

        return gson.toJson(result);
    }

    /**
     * Parse une valeur individuelle
     *
     * @param input
     *            La chaîne à parser
     * @return JsonElement correspondant
     */
    private static JsonElement parseValue(String input) {
        input = input.trim();

        // Cas d'un array
        Pattern arrayPattern = Pattern.compile("^array:\\d+\\s*\\[\\s*(.*)?\\s*\\]$", Pattern.DOTALL);
        Matcher arrayMatcher = arrayPattern.matcher(input);
        if (arrayMatcher.matches()) {
            String content = arrayMatcher.group(1);
            return parseArray(content != null ? content : "");
        }

        // Cas d'un objet avec nom de classe (App\Tests\TestDTO {#383)
        Pattern namedObjectPattern = Pattern.compile("^([A-Za-z_\\\\][A-Za-z0-9_\\\\]*?)\\s*\\{#\\d+\\s*(.*?)\\s*\\}$", Pattern.DOTALL);
        Matcher namedObjectMatcher = namedObjectPattern.matcher(input);
        if (namedObjectMatcher.matches()) {
            String className = namedObjectMatcher.group(1);
            String content = namedObjectMatcher.group(2);
            return parseObject(className, content);
        }

        // Cas d'un objet anonyme (class@anonymous {#382)
        Pattern anonymousObjectPattern = Pattern.compile("^class@anonymous\\s*\\{#\\d+\\s*(.*?)\\s*\\}$", Pattern.DOTALL);
        Matcher anonymousObjectMatcher = anonymousObjectPattern.matcher(input);
        if (anonymousObjectMatcher.matches()) {
            String content = anonymousObjectMatcher.group(1);
            return parseObject("@anonymous", content);
        }

        // Cas d'une chaîne entre guillemets
        Pattern stringPattern = Pattern.compile("^\"(.*)\"$");
        Matcher stringMatcher = stringPattern.matcher(input);
        if (stringMatcher.matches()) {
            return new JsonPrimitive(stringMatcher.group(1));
        }

        // Cas d'un nombre
        if (isNumeric(input)) {
            try {
                if (input.contains(".")) {
                    return new JsonPrimitive(Double.parseDouble(input));
                }
                else {
                    return new JsonPrimitive(Integer.parseInt(input));
                }
            } catch (NumberFormatException e) {
                return new JsonPrimitive(input);
            }
        }

        // Cas d'un booléen
        if ("true".equals(input))
            return new JsonPrimitive(true);
        if ("false".equals(input))
            return new JsonPrimitive(false);
        if ("null".equals(input))
            return new JsonPrimitive((String) null);

        // Par défaut, retourner comme chaîne
        return new JsonPrimitive(input);
    }

    /**
     * Parse un objet PHP
     *
     * @param className
     *            Le nom de la classe ou "@anonymous" pour les classes anonymes
     * @param content
     *            Le contenu de l'objet
     * @return JsonObject représentant l'objet
     */
    private static JsonObject parseObject(String className, String content) {
        JsonObject jsonObject = new JsonObject();

        // Ajouter des métadonnées sur l'objet
        // jsonObject.addProperty("__class", className);
        // jsonObject.addProperty("__type", "object");

        if (content == null || content.trim().isEmpty()) {
            return jsonObject;
        }

        List<String> properties = splitObjectProperties(content);

        for (String property : properties) {
            property = property.trim();

            // Pattern pour les propriétés avec visibilité (+name:, -name:, #name:)
            Pattern propertyPattern = Pattern.compile("^([+\\-#])([^:]+):\\s*(.+)$", Pattern.DOTALL);
            Matcher propertyMatcher = propertyPattern.matcher(property);

            if (propertyMatcher.matches()) {
                String visibility = propertyMatcher.group(1);
                String propertyName = propertyMatcher.group(2).trim();
                String propertyValue = propertyMatcher.group(3).trim();

                // Créer un objet pour stocker la propriété avec ses métadonnées
                // JsonObject propertyObj = new JsonObject();
                // propertyObj.addProperty("visibility", getVisibilityName(visibility));
                // propertyObj.add("value", parseValue(propertyValue));
                // jsonObject.add(propertyName, propertyObj);

                jsonObject.add(propertyName, parseValue(propertyValue));
            }
            else {
                // Si le pattern ne correspond pas, traiter comme une propriété simple
                // Chercher le pattern "key" => "value"
                Pattern keyValuePattern = Pattern.compile("^\"([^\"]*)\"\\s*=>\\s*(.+)$", Pattern.DOTALL);
                Matcher keyValueMatcher = keyValuePattern.matcher(property);
                if (keyValueMatcher.matches()) {
                    String key = keyValueMatcher.group(1);
                    String value = keyValueMatcher.group(2).trim();
                    jsonObject.add(key, parseValue(value));
                }
                else {
                    // Propriété sans clé explicite
                    jsonObject.add("property_" + jsonObject.size(), parseValue(property));
                }
            }
        }

        return jsonObject;
    }

    /**
     * Convertit le symbole de visibilité en nom
     *
     * @param visibility
     *            Le symbole de visibilité (+, -, #)
     * @return Le nom de la visibilité
     */
    private static String getVisibilityName(String visibility) {
        switch (visibility) {
        case "+":
            return "public";
        case "-":
            return "private";
        case "#":
            return "protected";
        default:
            return "unknown";
        }
    }

    /**
     * Divise le contenu d'un objet en propriétés individuelles
     *
     * @param content
     *            Le contenu à diviser
     * @return Liste des propriétés
     */
    private static List<String> splitObjectProperties(String content) {
        List<String> properties = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inString = false;
        boolean escapeNext = false;

        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);

            if (escapeNext) {
                current.append(ch);
                escapeNext = false;
                continue;
            }

            if (ch == '\\') {
                escapeNext = true;
                current.append(ch);
                continue;
            }

            if (ch == '"') {
                inString = !inString;
                current.append(ch);
                continue;
            }

            if (!inString) {
                if (ch == '{' || ch == '[') {
                    depth++;
                }
                else if (ch == '}' || ch == ']') {
                    depth--;
                }

                // Si on trouve une nouvelle ligne et qu'on est au niveau 0, c'est une nouvelle propriété
                if (ch == '\n' && depth == 0) {
                    String trimmed = current.toString().trim();
                    if (!trimmed.isEmpty()) {
                        properties.add(trimmed);
                    }
                    current = new StringBuilder();
                    continue;
                }
            }

            current.append(ch);
        }

        // Ajouter la dernière propriété
        String trimmed = current.toString().trim();
        if (!trimmed.isEmpty()) {
            properties.add(trimmed);
        }

        return properties;
    }

    /**
     * Parse un array
     *
     * @param content
     *            Le contenu de l'array
     * @return JsonElement (JsonObject ou JsonArray)
     */
    private static JsonElement parseArray(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new JsonArray();
        }

        List<String> items = splitArrayItems(content);
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        boolean isAssociative = false;
        boolean isIndexed = false;
        Map<Integer, JsonElement> indexedItems = new HashMap<>();

        for (String item : items) {
            item = item.trim();

            // Chercher le pattern "key" => "value"
            Pattern quotedKeyPattern = Pattern.compile("^\"([^\"]*)\"\\s*=>\\s*(.+)$", Pattern.DOTALL);
            Matcher quotedKeyMatcher = quotedKeyPattern.matcher(item);
            if (quotedKeyMatcher.matches()) {
                String key = quotedKeyMatcher.group(1);
                String value = quotedKeyMatcher.group(2).trim();
                jsonObject.add(key, parseValue(value));
                isAssociative = true;
                continue;
            }

            // Chercher le pattern key => value (sans guillemets pour la clé)
            Pattern keyPattern = Pattern.compile("^([^\\s=>]+)\\s*=>\\s*(.+)$", Pattern.DOTALL);
            Matcher keyMatcher = keyPattern.matcher(item);
            if (keyMatcher.matches()) {
                String key = keyMatcher.group(1);
                String value = keyMatcher.group(2).trim();

                // Vérifier si la clé est un index numérique
                if (isNumeric(key)) {
                    int index = Integer.parseInt(key);
                    indexedItems.put(index, parseValue(value));
                    isIndexed = true;
                }
                else {
                    jsonObject.add(key, parseValue(value));
                    isAssociative = true;
                }
                continue;
            }

            // Chercher le pattern index => value (pour les arrays indexés)
            Pattern indexPattern = Pattern.compile("^(\\d+)\\s*=>\\s*(.+)$", Pattern.DOTALL);
            Matcher indexMatcher = indexPattern.matcher(item);
            if (indexMatcher.matches()) {
                int index = Integer.parseInt(indexMatcher.group(1));
                String value = indexMatcher.group(2).trim();
                indexedItems.put(index, parseValue(value));
                isIndexed = true;
                continue;
            }

            // Valeur simple (pour les arrays sans clés explicites)
            jsonArray.add(parseValue(item));
            isIndexed = true;
        }

        // Retourner le bon type selon le contenu
        if (isAssociative && !isIndexed) {
            return jsonObject;
        }
        else if (isIndexed && !isAssociative) {
            // Vérifier si les indices sont consécutifs pour créer un tableau
            if (!indexedItems.isEmpty()) {
                return createArrayFromIndexedItems(indexedItems);
            }
            return jsonArray;
        }
        else if (isAssociative && isIndexed) {
            // Cas mixte, privilégier l'objet
            return jsonObject;
        }
        else {
            return new JsonArray();
        }
    }

    /**
     * Crée un JsonArray à partir d'items indexés si les indices sont consécutifs
     *
     * @param indexedItems
     *            Map des items indexés
     * @return JsonArray si les indices sont consécutifs, JsonObject sinon
     */
    private static JsonElement createArrayFromIndexedItems(Map<Integer, JsonElement> indexedItems) {
        if (indexedItems.isEmpty()) {
            return new JsonArray();
        }

        // Récupérer les indices et les trier
        List<Integer> indices = new ArrayList<>(indexedItems.keySet());
        Collections.sort(indices);

        // Vérifier si les indices sont consécutifs à partir de 0
        boolean isConsecutive = true;
        for (int i = 0; i < indices.size(); i++) {
            if (indices.get(i) != i) {
                isConsecutive = false;
                break;
            }
        }

        if (isConsecutive) {
            // Créer un JsonArray avec les éléments dans l'ordre
            JsonArray jsonArray = new JsonArray();
            for (int i = 0; i < indices.size(); i++) {
                jsonArray.add(indexedItems.get(i));
            }
            return jsonArray;
        }
        else {
            // Créer un JsonObject avec les indices comme clés
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<Integer, JsonElement> entry : indexedItems.entrySet()) {
                jsonObject.add(entry.getKey().toString(), entry.getValue());
            }
            return jsonObject;
        }
    }

    /**
     * Divise le contenu d'un array en items individuels
     *
     * @param content
     *            Le contenu à diviser
     * @return Liste des items
     */
    private static List<String> splitArrayItems(String content) {
        List<String> items = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inString = false;
        boolean escapeNext = false;

        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);

            if (escapeNext) {
                current.append(ch);
                escapeNext = false;
                continue;
            }

            if (ch == '\\') {
                escapeNext = true;
                current.append(ch);
                continue;
            }

            if (ch == '"') {
                inString = !inString;
                current.append(ch);
                continue;
            }

            if (!inString) {
                if (ch == '[' || ch == '{') {
                    depth++;
                }
                else if (ch == ']' || ch == '}') {
                    depth--;
                }

                // Si on trouve une nouvelle ligne et qu'on est au niveau 0, c'est un nouveau item
                if (ch == '\n' && depth == 0) {
                    String trimmed = current.toString().trim();
                    if (!trimmed.isEmpty()) {
                        items.add(trimmed);
                    }
                    current = new StringBuilder();
                    continue;
                }
            }

            current.append(ch);
        }

        // Ajouter le dernier item
        String trimmed = current.toString().trim();
        if (!trimmed.isEmpty()) {
            items.add(trimmed);
        }

        return items;
    }

    /**
     * Vérifie si une chaîne est numérique
     *
     * @param str
     *            La chaîne à vérifier
     * @return true si numérique, false sinon
     */
    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
