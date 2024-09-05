package org.camunda.consulting.datagenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JsonTemplateProcessor {

  private static final Random random = new Random();

  @Value("${template.int.lower-bound}")
  private int intLowerBound;

  @Value("${template.int.upper-bound}")
  private int intUpperBound;

  @Value("${template.double.lower-bound}")
  private double doubleLowerBound;

  @Value("${template.double.upper-bound}")
  private double doubleUpperBound;

  @Value("${template.boolean.true-chance}")
  private double trueChance;

  public String processTemplate(File file) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.readTree(file);
    processNode(rootNode);
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
  }

  private void processNode(JsonNode node) {
    if (node.isObject()) {
      Iterator<String> fieldNames = node.fieldNames();
      while (fieldNames.hasNext()) {
        String fieldName = fieldNames.next();
        JsonNode fieldValue = node.get(fieldName);
        if (fieldValue.isTextual()) {
          String textValue = fieldValue.asText();
          replacePlaceholders((ObjectNode) node, fieldName, textValue);
        } else if (fieldValue.isArray()) {
          processArray((ArrayNode) fieldValue);
        } else if (fieldValue.isObject()) {
          processNode(fieldValue);
        }
      }
    }
  }

  private void replacePlaceholders(ObjectNode node, String fieldName, String textValue) {
    switch (textValue) {
      case "{{uuid}}":
        node.put(fieldName, generateUUID());
        break;
      case "{{double}}":
        node.put(fieldName, generateRandomDouble());
        break;
      case "{{boolean}}":
        node.put(fieldName, generateRandomBoolean());
        break;
      case "{{int}}":
        node.put(fieldName, generateRandomInt());
        break;
      default:
        if (isArrayString(textValue)) {
          node.put(fieldName, selectRandomArrayValue(textValue));
        }
        break;
    }
  }

  private int generateRandomInt() {
    return intLowerBound + random.nextInt(intUpperBound - intLowerBound + 1);
  }

  private String generateUUID() {
    return UUID.randomUUID().toString();
  }

  private double generateRandomDouble() {
    return doubleLowerBound + (random.nextDouble() * (doubleUpperBound - doubleLowerBound));
  }

  private boolean generateRandomBoolean() {
    return random.nextDouble() < trueChance;
  }

  private String selectRandomArrayValue(String arrayString) {
    String[] options = parseArrayString(arrayString);
    return options[random.nextInt(options.length)];
  }

  private void processArray(ArrayNode arrayNode) {
    if (arrayNode.size() > 0) {
      int randomIndex = random.nextInt(arrayNode.size());
      JsonNode randomElement = arrayNode.get(randomIndex);
      arrayNode.removeAll().add(randomElement);
    }
  }

  private boolean isArrayString(String textValue) {
    return textValue.startsWith("[") && textValue.endsWith("]");
  }

  private String[] parseArrayString(String textValue) {
    String trimmed = textValue.substring(1, textValue.length() - 1);
    return trimmed.split("\\s*,\\s*");
  }
}
