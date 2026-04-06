package de.tobias.kontoapp.application;

public enum AccountProfile {
    TOBIAS("Tobias", "transactions.csv", "Tobias"),
    ANNIKA("Annika", "transactions_annika.csv", "Annika");

    private final String displayName;
    private final String fileName;
    private final String primaryPersonName;

    AccountProfile(String displayName, String fileName, String primaryPersonName) {
        this.displayName = displayName;
        this.fileName = fileName;
        this.primaryPersonName = primaryPersonName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPrimaryPersonName() {
        return primaryPersonName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}