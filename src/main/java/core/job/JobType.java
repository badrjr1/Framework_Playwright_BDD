package core.job;

public enum JobType {

    BATCH("batch"),
    TRANSACTION("transaction"),
    SH_SCRIPT("sh script"),
    SQL_SCRIPT("sql script"),
    GROOVY_SCRIPT("groovy script");

    private final String value;

    JobType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static JobType from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Job type is empty");
        }

        String normalizedValue = rawValue.trim().toLowerCase();

        for (JobType type : values()) {
            if (type.value.equals(normalizedValue)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                "Unsupported job type: " + rawValue
                        + ". Supported values are: batch, transaction, sh script, sql script, groovy script"
        );
    }

    public boolean isScript() {
        return this == SH_SCRIPT || this == SQL_SCRIPT || this == GROOVY_SCRIPT;
    }
}