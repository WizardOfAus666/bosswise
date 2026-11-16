package com.bosswise.model;

public class WikiMetadata {
    public enum VerificationStatus {
        VERIFIED("Verified"),
        PARTIALLY_VERIFIED("Partially Verified"),
        UNAVAILABLE("Unavailable / Offline");

        private final String label;
        VerificationStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final String sourceUrl;
    private final long lastFetchedTimestamp;
    private final VerificationStatus status;
    private final double confidenceScore;
    private final String revisionId;
    private final String notes;

    public WikiMetadata(
            String sourceUrl,
            long lastFetchedTimestamp,
            VerificationStatus status,
            double confidenceScore,
            String revisionId,
            String notes) {
        this.sourceUrl = sourceUrl != null ? sourceUrl : "https://oldschool.runescape.wiki/";
        this.lastFetchedTimestamp = lastFetchedTimestamp > 0 ? lastFetchedTimestamp : System.currentTimeMillis();
        this.status = status != null ? status : VerificationStatus.PARTIALLY_VERIFIED;
        this.confidenceScore = Math.max(0.0, Math.min(1.0, confidenceScore));
        this.revisionId = revisionId != null ? revisionId : "baseline";
        this.notes = notes != null ? notes : "";
    }

    public static WikiMetadata baseline(String article) {
        return new WikiMetadata(
                "https://oldschool.runescape.wiki/w/" + (article != null ? article.replace(" ", "_") : ""),
                System.currentTimeMillis(),
                VerificationStatus.VERIFIED,
                0.98,
                "bundled-verified",
                "Verified from official OSRS Wiki data"
        );
    }

    public static WikiMetadata verified(String url, String revisionId) {
        return new WikiMetadata(
                url,
                System.currentTimeMillis(),
                VerificationStatus.VERIFIED,
                1.0,
                revisionId,
                "Live confirmed via MediaWiki API"
        );
    }

    public static WikiMetadata partiallyVerified(String source, String note) {
        return new WikiMetadata(
                source,
                System.currentTimeMillis(),
                VerificationStatus.PARTIALLY_VERIFIED,
                0.85,
                "partial",
                note
        );
    }

    public static WikiMetadata offlineFallback(String target) {
        return new WikiMetadata(
                "https://oldschool.runescape.wiki/w/" + (target != null ? target.replace(" ", "_") : ""),
                System.currentTimeMillis(),
                VerificationStatus.PARTIALLY_VERIFIED,
                0.90,
                "offline-cache",
                "Loaded from local bundled verified baseline"
        );
    }

    public String getSourceUrl() { return sourceUrl; }
    public long getLastFetchedTimestamp() { return lastFetchedTimestamp; }
    public VerificationStatus getStatus() { return status; }
    public double getConfidenceScore() { return confidenceScore; }
    public String getRevisionId() { return revisionId; }
    public String getNotes() { return notes; }

    public boolean isFresh(long ttlMs) {
        return (System.currentTimeMillis() - lastFetchedTimestamp) < ttlMs;
    }
}
