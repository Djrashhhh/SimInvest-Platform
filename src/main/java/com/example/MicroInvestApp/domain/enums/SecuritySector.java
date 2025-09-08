package com.example.MicroInvestApp.domain.enums;

public enum SecuritySector {
    TECHNOLOGY("Technology", "Tech"),      // Represents the technology sector
    HEALTHCARE("Healthcare", "Health"),      // Represents the healthcare sector
    FINANCIALS("Financials", "Fin"),      // Represents the financials sector
    CONSUMER_DISCRETIONARY("Consumer Discretionary", "Discretion"), // Represents the consumer discretionary sector
    CONSUMER_STAPLES("Consumer Staples", "Staples"),       // Represents the consumer staples sector
    COMMUNICATION_SERVICES("Communication Services", "Comm"), // Represents the communication services sector
    INDUSTRIALS("Industrials", "Ind"),            // Represents the industrials sector
    ENERGY("Energy", "EN"),                 // Represents the energy sector
    MATERIALS("Materials", "Mat"),              // Represents the materials sector
    UTILITIES("Utilities", "Util"),              // Represents the utilities sector
    REAL_ESTATE("Real Estate", "RE");            // Represents the real estate sector

    private final String fullName;
    private final String abbreviation;

    SecuritySector(String fullName, String abbreviation) {
        this.fullName = fullName;
        this.abbreviation = abbreviation;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}
