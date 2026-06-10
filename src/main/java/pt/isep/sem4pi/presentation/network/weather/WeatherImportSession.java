package pt.isep.sem4pi.presentation.network.weather;

import java.util.Base64;

final class WeatherImportSession {

    private final String fileName;
    private final String fileType;
    private final int expectedChunks;
    private final StringBuilder base64Payload = new StringBuilder();
    private int receivedChunks;

    WeatherImportSession(final String fileName, final String fileType, final int expectedChunks) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.expectedChunks = expectedChunks;
    }

    String getFileName() {
        return fileName;
    }

    String getFileType() {
        return fileType;
    }

    int getExpectedChunks() {
        return expectedChunks;
    }

    int getReceivedChunks() {
        return receivedChunks;
    }

    void addChunk(final String chunk) {
        base64Payload.append(chunk);
        receivedChunks++;
    }

    boolean isComplete() {
        return receivedChunks >= expectedChunks;
    }

    byte[] decodeContent() {
        return Base64.getDecoder().decode(base64Payload.toString());
    }
}
