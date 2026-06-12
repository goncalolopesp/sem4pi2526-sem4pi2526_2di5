package pt.isep.sem4pi.presentation.network.weather;

import java.util.Base64;

public final class WeatherImportSession {

    private final String fileName;
    private final String fileType;
    private final int expectedChunks;
    private final StringBuilder base64Payload = new StringBuilder();
    private int receivedChunks;

    public WeatherImportSession(final String fileName, final String fileType, final int expectedChunks) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.expectedChunks = expectedChunks;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public int getExpectedChunks() {
        return expectedChunks;
    }

    public int getReceivedChunks() {
        return receivedChunks;
    }

    public void addChunk(final String chunk) {
        base64Payload.append(chunk);
        receivedChunks++;
    }

    public boolean isComplete() {
        return receivedChunks >= expectedChunks;
    }

    public byte[] decodeContent() {
        return Base64.getDecoder().decode(base64Payload.toString());
    }
}
