package org.koekepan.Performance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PacketCapture {

    // Enum to define log categories
    public enum LogCategory {
        SERVERBOUND_IN,
        SERVERBOUND_OUT,
        CLIENTBOUND_IN,
        CLIENTBOUND_OUT,
        DELETED_PACKETS_TIME,
        DELETED_PACKETS,
        INIT_SEND,
        ADD_TO_OUTGOING_QUEUE,
        PROCESSING_START,
        UNKNOWN,
        CHUNK_DELETED_PACKETS_TIME,
        SERVERBOUND_BEH_TIME,
        PACKET_BEH_QUEUE,
        SERVERBOUND_BEH,
        PACKET_BEH_HANDLER,
        SERVERBOUND_QUEUE,
    }

    // Directory where logs will be stored
    private static final String LOG_DIR = "./packet_results/";

    // Set to keep track of files that have already been prepared
    private static final Set<String> preparedFiles = new HashSet<>();

    private static void prepareFile(String filename) throws IOException {
        synchronized (preparedFiles) {
            if (preparedFiles.contains(filename)) {
                return;
            }

            File file = new File(filename);
            File parentDir = file.getParentFile();

            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            if (file.exists()) {
                new FileWriter(filename, false).close(); // This will empty the file
            }

            preparedFiles.add(filename);
        }
    }

    // Synchronized to make it thread-safe
    public static synchronized void log(String message, LogCategory category) {
//        return;
        FileWriter fileWriter = null;
        String targetFilename = getFilenameByCategory(category);

        try {
            prepareFile(targetFilename);

            // Append to the existing file or create a new one if it doesn't exist
            fileWriter = new FileWriter(targetFilename, true);

            // Create a SimpleDateFormat object to format date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

            // Get the current date and time
            String currentTime = dateFormat.format(new Date());

            // Write the date, time, and message to the file
            fileWriter.append(currentTime);
            fileWriter.append(",");
            fileWriter.append(message);
            fileWriter.append("\n");

        } catch (IOException e) {
            // Handle exceptions
            e.printStackTrace();
        } finally {
            try {
                // Close the FileWriter
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
            } catch (IOException e) {
                // Handle exceptions
                e.printStackTrace();
            }
        }
    }

    // Helper method to get filename based on log category
    private static String getFilenameByCategory(LogCategory category) {
        String filename = "";
        switch (category) {
            case SERVERBOUND_IN:
                filename = "cp_serverbound_in_packet_log.csv";
                break;
            case SERVERBOUND_OUT:
                filename = "cp_serverbound_out_packet_log.csv";
                break;
            case CLIENTBOUND_IN:
                filename = "cp_clientbound_in_packet_log.csv";
                break;
            case CLIENTBOUND_OUT:
                filename = "cp_clientbound_out_packet_log.csv";
                break;
            case ADD_TO_OUTGOING_QUEUE:
                filename = "cp_add_to_outgoing_queue_packet_log.csv";
                break;
            case INIT_SEND:
                filename = "cp_init_send_packet_log.csv";
                break;
            case DELETED_PACKETS:
                filename = "cp_deleted_packets.csv";
                break;
            case DELETED_PACKETS_TIME:
                filename = "cp_deleted_packets_time.csv";
                break;
            case PROCESSING_START:
                filename = "cp_processing_START_packet_log.csv";
                break;
            case UNKNOWN:
                filename = "cp_clientbound_in_unknownRecipient_packet_log.csv";
                break;
            case CHUNK_DELETED_PACKETS_TIME:
                filename = "cp_chunk_deleted_packets_time.csv";
                break;
            case SERVERBOUND_BEH_TIME:
                filename = "cp_serverbound_beh_time.csv";
                break;
            case PACKET_BEH_QUEUE:
                filename = "cp_packet_beh_queue.csv";
                break;
            case SERVERBOUND_BEH:
                filename = "cp_serverbound_beh.csv";
                break;
            case PACKET_BEH_HANDLER:
                filename = "cp_packet_beh_handler.csv";
                break;
            case SERVERBOUND_QUEUE:
                filename = "cp_serverbound_queue.csv";
                break;
        }
        return LOG_DIR + filename;
    }
}
