package model;

import java.time.LocalDateTime;

/**
 * Encapsulates an application configuration or localized UI text setting.
 */
public class ApplicationSetting {
    private String settingKey;
    private String settingValue;
    private String category;    // "BRANDING", "NAVIGATION", "DASHBOARD", "FORMS", "AUTH", "ALERTS"
    private String description;
    private LocalDateTime updatedAt;
    private int updatedBy;

    public ApplicationSetting() {}

    public ApplicationSetting(String settingKey, String settingValue, String category, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.category = category;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public ApplicationSetting(String settingKey, String settingValue, String category,
                              String description, LocalDateTime updatedAt, int updatedBy) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.category = category;
        this.description = description;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(int updatedBy) { this.updatedBy = updatedBy; }

    @Override
    public String toString() {
        return "ApplicationSetting{" +
                "key='" + settingKey + '\'' +
                ", value='" + settingValue + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
