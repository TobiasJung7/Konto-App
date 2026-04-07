package de.tobias.kontoapp.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class StorageConfig {

    private static final String CONFIG_FILE = "storage.properties";

    public static Path getDataDirectory() {
        Properties props = new Properties();

        try (InputStream in = StorageConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException("storage.properties konnte nicht gefunden werden.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("storage.properties konnte nicht gelesen werden.", e);
        }

        String dataDir = props.getProperty("dataDir");
        if (dataDir == null || dataDir.isBlank()) {
            throw new IllegalStateException("In storage.properties fehlt dataDir.");
        }

        return Path.of(dataDir);
    }
}
