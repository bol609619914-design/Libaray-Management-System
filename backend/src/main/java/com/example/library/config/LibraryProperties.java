package com.example.library.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "library")
public class LibraryProperties {
    private Jwt jwt = new Jwt();
    private Borrowing borrowing = new Borrowing();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Borrowing getBorrowing() {
        return borrowing;
    }

    public void setBorrowing(Borrowing borrowing) {
        this.borrowing = borrowing;
    }

    public static class Jwt {
        private String issuer;
        private String secret;
        private long accessTokenMinutes;

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getAccessTokenMinutes() {
            return accessTokenMinutes;
        }

        public void setAccessTokenMinutes(long accessTokenMinutes) {
            this.accessTokenMinutes = accessTokenMinutes;
        }
    }

    public static class Borrowing {
        private int defaultDays = 30;
        private int maxRenewCount = 2;
        private BigDecimal finePerDay = BigDecimal.valueOf(0.5);

        public int getDefaultDays() {
            return defaultDays;
        }

        public void setDefaultDays(int defaultDays) {
            this.defaultDays = defaultDays;
        }

        public int getMaxRenewCount() {
            return maxRenewCount;
        }

        public void setMaxRenewCount(int maxRenewCount) {
            this.maxRenewCount = maxRenewCount;
        }

        public BigDecimal getFinePerDay() {
            return finePerDay;
        }

        public void setFinePerDay(BigDecimal finePerDay) {
            this.finePerDay = finePerDay;
        }
    }
}
