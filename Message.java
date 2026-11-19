import java.util.Random;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

    private String messageID;
    private int messageNumber;
    private String recipient;
    private String messageText;
    private String sender;

    private static String currentSender = "Developer";

    // PART 3: Five required arrays
    private static Message[] sentMessagesArray = new Message[100];
    private static Message[] disregardedMessagesArray = new Message[100];
    private static Message[] storedMessagesArray = new Message[100];

    private static String[] messageHashesArray = new String[100];
    private static String[] messageIDsArray = new String[100];

    // Array counters
    private static int sentCount = 0;
    private static int disregardedCount = 0;
    private static int storedCount = 0;
    private static int messageCount = 0;

    private static final String FILE_NAME = "storedMessages.json";

    // Constructor
    public Message(int messageNumber, String recipient, String messageText) {
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.sender = currentSender;
        this.messageID = generateMessageID();
    }

    // Constructor for unit testing with a predetermined ID
    public Message(String messageID, int messageNumber,
                   String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.sender = currentSender;
    }

    // Set the name of the current sender
    public static void setCurrentSender(String sender) {
        currentSender = sender;
    }

    // Generate a ten-digit message ID
    private String generateMessageID() {
        Random random = new Random();
        long number = Math.abs(random.nextLong()) % 10_000_000_000L;
        return String.format("%010d", number);
    }

    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getSender() {
        return sender;
    }

    // Validate recipient cellphone number
    public String checkRecipientCell() {
        if (recipient != null &&
            recipient.matches("^\\+\\d{1,3}\\d{1,10}$")) {
            return "Cell phone number successfully captured.";
        }

        return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    // Generate message hash
    public String createMessageHash() {
        if (messageText == null || messageText.trim().isEmpty()) {
            return messageID.substring(0, 2) + ":" +
                   messageNumber + ":";
        }

        String[] words = messageText.trim().split("\\s+");

        String firstWord = words[0].replaceAll("[^A-Za-z0-9]", "");
        String lastWord = words[words.length - 1]
                          .replaceAll("[^A-Za-z0-9]", "");

        return (messageID.substring(0, 2) + ":" +
                messageNumber + ":" +
                firstWord + lastWord).toUpperCase();
    }

    // Send, disregard or store
    // 1 = Send, 2 = Disregard, 3 = Store
    public String SentMessage(int choice) {
        if (choice == 1) {
            return "Message successfully sent.";
        } else if (choice == 2) {
            return "Press 0 to delete the message.";
        } else if (choice == 3) {
            return "Message successfully stored.";
        }

        return "Invalid option.";
    }

    // Check message length
    public String checkMessageLength() {
        if (messageText == null) {
            return "Message cannot be empty.";
        }

        int length = messageText.length();

        if (length <= 250) {
            return "Message ready to send.";
        }

        return "Message exceeds 250 characters by " +
               (length - 250) +
               "; please reduce the size.";
    }

    // Display full message details
    public String getDetails() {
        return "Message ID: " + messageID
                + "\nMessage Hash: " + createMessageHash()
                + "\nRecipient: " + recipient
                + "\nMessage: " + messageText;
    }

    // Add message information to the ID and hash arrays
    private void addToTrackingArrays() {
        if (messageCount < messageIDsArray.length) {
            messageIDsArray[messageCount] = messageID;
            messageHashesArray[messageCount] = createMessageHash();
            messageCount++;
        }
    }

    // Process the user's choice and populate the appropriate array
    public String processChoice(int choice) {

        if (choice == 1) {

            if (sentCount >= sentMessagesArray.length) {
                return "Sent messages array is full.";
            }

            sentMessagesArray[sentCount++] = this;
            addToTrackingArrays();

            return "Message successfully sent.";

        } else if (choice == 2) {

            if (disregardedCount >= disregardedMessagesArray.length) {
                return "Disregarded messages array is full.";
            }

            disregardedMessagesArray[disregardedCount++] = this;
            addToTrackingArrays();

            return "Message successfully disregarded.";

        } else if (choice == 3) {

            // FIX 3: load previously stored messages first so that
            // storing a new message does not overwrite the saved file
            loadStoredMessagesFromFile();

            if (storedCount >= storedMessagesArray.length) {
                return "Stored messages array is full.";
            }

            storedMessagesArray[storedCount++] = this;
            addToTrackingArrays();

            return storeMessage();
        }

        return "Invalid option.";
    }

    // Retain the original Part 2 method
    public void addToSent() {
        if (sentCount < sentMessagesArray.length) {
            sentMessagesArray[sentCount++] = this;
            addToTrackingArrays();
        }
    }

    // Display all sent messages
    public static String printMessages() {
        StringBuilder all = new StringBuilder();

        for (int i = 0; i < sentCount; i++) {
            all.append(sentMessagesArray[i].getDetails())
               .append("\n\n");
        }

        return all.toString();
    }

    // Return total sent messages
    public static int returnTotalMessages() {
        return sentCount;
    }

    // Convert message into JSON format
    // FIX 2: the message number is now saved so the hash stays the same
    private String toJson() {
        return "{"
                + "\"sender\":\"" + escapeJson(sender) + "\","
                + "\"messageID\":\"" + escapeJson(messageID) + "\","
                + "\"messageNumber\":\"" + messageNumber + "\","
                + "\"messageHash\":\"" + escapeJson(createMessageHash()) + "\","
                + "\"recipient\":\"" + escapeJson(recipient) + "\","
                + "\"message\":\"" + escapeJson(messageText) + "\""
                + "}";
    }

    // Escape special JSON characters
    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    // Convert escaped JSON characters back to normal text
    private static String unescapeJson(String value) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);

                switch (next) {
                    case 'n':
                        result.append('\n');
                        break;
                    case 'r':
                        result.append('\r');
                        break;
                    case 't':
                        result.append('\t');
                        break;
                    case '"':
                        result.append('"');
                        break;
                    case '\\':
                        result.append('\\');
                        break;
                    default:
                        result.append(next);
                }
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    // Store all stored messages in the JSON file
    // FIX 1: now static so deleteMessageByHash can call it
    public static String storeMessage() {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {

            writer.write("[\n");

            for (int i = 0; i < storedCount; i++) {
                writer.write("  " + storedMessagesArray[i].toJson());

                if (i < storedCount - 1) {
                    writer.write(",");
                }

                writer.write("\n");
            }

            writer.write("]");

        } catch (IOException e) {
            return "Message could not be stored: " + e.getMessage();
        }

        return "Message successfully stored.";
    }

    // Retrieve a JSON field from one JSON object
    private static String getJsonValue(String object, String key) {

        String expression = "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"";

        Matcher matcher = Pattern.compile(expression).matcher(object);

        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }

        return "";
    }

    // Read stored messages from JSON into the stored messages array
    public static String loadStoredMessagesFromFile() {

        storedCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new FileReader(FILE_NAME))) {

            StringBuilder json = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            Pattern pattern = Pattern.compile("\\{([^{}]*)\\}");
            Matcher matcher = pattern.matcher(json.toString());

            while (matcher.find() && storedCount < storedMessagesArray.length) {

                String object = matcher.group(1);

                String id = getJsonValue(object, "messageID");
                String recipient = getJsonValue(object, "recipient");
                String message = getJsonValue(object, "message");
                String sender = getJsonValue(object, "sender");

                // FIX 2: restore the original message number
                int number = storedCount + 1;

                try {
                    number = Integer.parseInt(
                            getJsonValue(object, "messageNumber"));
                } catch (NumberFormatException e) {
                    // older file without a saved number: keep fallback
                }

                Message stored = new Message(
                        id,
                        number,
                        recipient,
                        message
                );

                if (!sender.isEmpty()) {
                    stored.sender = sender;
                }

                storedMessagesArray[storedCount++] = stored;
            }

            return "Stored messages successfully loaded.";

        } catch (IOException e) {
            return "No stored messages file found yet.";
        }
    }

    // Display sender and recipient of all stored messages
    public static String displayStoredSendersAndRecipients() {

        loadStoredMessagesFromFile();

        StringBuilder result = new StringBuilder();

        if (storedCount == 0) {
            return "No stored messages available.";
        }

        for (int i = 0; i < storedCount; i++) {
            Message message = storedMessagesArray[i];

            result.append("Sender: ").append(message.sender)
                  .append("\nRecipient: ").append(message.recipient)
                  .append("\n\n");
        }

        return result.toString();
    }

    // Find the longest stored message
    public static String displayLongestStoredMessage() {

        loadStoredMessagesFromFile();

        if (storedCount == 0) {
            return "No stored messages available.";
        }

        Message longest = storedMessagesArray[0];

        for (int i = 1; i < storedCount; i++) {
            if (storedMessagesArray[i].messageText.length()
                    > longest.messageText.length()) {
                longest = storedMessagesArray[i];
            }
        }

        return longest.messageText;
    }

    // Search for a message using its message ID
    public static String searchMessageByID(String id) {

        loadStoredMessagesFromFile();

        for (int i = 0; i < storedCount; i++) {
            Message message = storedMessagesArray[i];

            if (message.messageID.equals(id)) {
                return "Recipient: " + message.recipient
                        + "\nMessage: " + message.messageText;
            }
        }

        for (int i = 0; i < sentCount; i++) {
            Message message = sentMessagesArray[i];

            if (message.messageID.equals(id)) {
                return "Recipient: " + message.recipient
                        + "\nMessage: " + message.messageText;
            }
        }

        for (int i = 0; i < disregardedCount; i++) {
            Message message = disregardedMessagesArray[i];

            if (message.messageID.equals(id)) {
                return "Recipient: " + message.recipient
                        + "\nMessage: " + message.messageText;
            }
        }

        return "Message ID not found.";
    }

    // Search all sent and stored messages for a recipient
    public static String searchMessagesByRecipient(String recipient) {

        loadStoredMessagesFromFile();

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < sentCount; i++) {
            Message message = sentMessagesArray[i];

            if (message.recipient.equals(recipient)) {
                result.append(message.messageText).append("\n");
            }
        }

        for (int i = 0; i < storedCount; i++) {
            Message message = storedMessagesArray[i];

            if (message.recipient.equals(recipient)) {
                result.append(message.messageText).append("\n");
            }
        }

        if (result.length() == 0) {
            return "No messages found for this recipient.";
        }

        return result.toString();
    }

    // Delete a stored message using its message hash
    public static String deleteMessageByHash(String hash) {

        loadStoredMessagesFromFile();

        for (int i = 0; i < storedCount; i++) {

            if (storedMessagesArray[i].createMessageHash()
                    .equalsIgnoreCase(hash)) {

                String deletedMessage =
                        storedMessagesArray[i].messageText;

                for (int j = i; j < storedCount - 1; j++) {
                    storedMessagesArray[j] =
                            storedMessagesArray[j + 1];
                }

                storedMessagesArray[--storedCount] = null;

                removeTrackingInformation(hash);

                // FIX 1: save the updated array back to the JSON file
                storeMessage();

                return "Message: \"" + deletedMessage
                        + "\" successfully deleted.";
            }
        }

        return "Message hash not found.";
    }

    // Remove the ID and hash of a deleted message from tracking arrays
    private static void removeTrackingInformation(String hash) {

        for (int i = 0; i < messageCount; i++) {

            if (messageHashesArray[i] != null &&
                messageHashesArray[i].equalsIgnoreCase(hash)) {

                for (int j = i; j < messageCount - 1; j++) {
                    messageHashesArray[j] = messageHashesArray[j + 1];
                    messageIDsArray[j] = messageIDsArray[j + 1];
                }

                messageHashesArray[--messageCount] = null;
                messageIDsArray[messageCount] = null;
                break;
            }
        }
    }

    // Generate a full report of stored messages
    public static String displayStoredMessagesReport() {

        loadStoredMessagesFromFile();

        if (storedCount == 0) {
            return "No stored messages available.";
        }

        StringBuilder report = new StringBuilder();

        report.append("========== STORED MESSAGES REPORT ==========\n");

        for (int i = 0; i < storedCount; i++) {

            Message message = storedMessagesArray[i];

            report.append("\nMessage ID: ")
                  .append(message.messageID)
                  .append("\nMessage Hash: ")
                  .append(message.createMessageHash())
                  .append("\nRecipient: ")
                  .append(message.recipient)
                  .append("\nMessage: ")
                  .append(message.messageText)
                  .append("\n--------------------------------------------\n");
        }

        return report.toString();
    }

    // Access methods for unit tests
    public static Message[] getSentMessagesArray() {
        return sentMessagesArray;
    }

    public static Message[] getDisregardedMessagesArray() {
        return disregardedMessagesArray;
    }

    public static Message[] getStoredMessagesArray() {
        loadStoredMessagesFromFile();
        return storedMessagesArray;
    }

    public static String[] getMessageHashesArray() {
        return messageHashesArray;
    }

    public static String[] getMessageIDsArray() {
        return messageIDsArray;
    }

    public static int getSentCount() {
        return sentCount;
    }

    public static int getStoredCount() {
        loadStoredMessagesFromFile();
        return storedCount;
    }

    public static int getDisregardedCount() {
        return disregardedCount;
    }

    // Original Part 2 method
    public String messageIDResult() {
        return "Message ID generated: " + messageID;
    }
}