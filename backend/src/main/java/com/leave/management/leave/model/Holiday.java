package com.leave.management.leave.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "holidays")
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false)
    private String name;

    public Holiday() {}

    public Holiday(Long id, LocalDate date, String name) {
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

        public Holiday build() {
            return new Holiday(id, date, name);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; return; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
