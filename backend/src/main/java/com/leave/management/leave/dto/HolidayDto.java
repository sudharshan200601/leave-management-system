package com.leave.management.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class HolidayDto {
    private Long id;

    @NotNull(message = "Holiday date is required")
    private LocalDate date;

    @NotBlank(message = "Holiday name is required")
    private String name;

    public HolidayDto() {}

    public HolidayDto(Long id, LocalDate date, String name) {
        this.id = id;
        this.date = date;
        this.name = name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private LocalDate date;
        private String name;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder date(LocalDate date) { this.date = date; return this; }
        public Builder name(String name) { this.name = name; return this; }

        public HolidayDto build() {
            return new HolidayDto(id, date, name);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
