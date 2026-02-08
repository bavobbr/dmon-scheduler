package be.dmon.scheduler.rest;

import java.util.List;

public class SessionAnalysis {

    private String sessionId;
    private String teamName;
    private String trainerName;
    private String timeSlotInfo;
    private boolean hasViolations;
    private int totalScore;
    private List<ViolationInfo> violations;

    public SessionAnalysis() {
    }

    public SessionAnalysis(String sessionId, String teamName, String trainerName,
                          String timeSlotInfo, boolean hasViolations, int totalScore,
                          List<ViolationInfo> violations) {
        this.sessionId = sessionId;
        this.teamName = teamName;
        this.trainerName = trainerName;
        this.timeSlotInfo = timeSlotInfo;
        this.hasViolations = hasViolations;
        this.totalScore = totalScore;
        this.violations = violations;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public String getTimeSlotInfo() {
        return timeSlotInfo;
    }

    public void setTimeSlotInfo(String timeSlotInfo) {
        this.timeSlotInfo = timeSlotInfo;
    }

    public boolean isHasViolations() {
        return hasViolations;
    }

    public void setHasViolations(boolean hasViolations) {
        this.hasViolations = hasViolations;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public List<ViolationInfo> getViolations() {
        return violations;
    }

    public void setViolations(List<ViolationInfo> violations) {
        this.violations = violations;
    }

    public static class ViolationInfo {
        private String constraintName;
        private String level; // "HARD" or "SOFT"
        private int score;
        private String message;

        public ViolationInfo() {
        }

        public ViolationInfo(String constraintName, String level, int score, String message) {
            this.constraintName = constraintName;
            this.level = level;
            this.score = score;
            this.message = message;
        }

        public String getConstraintName() {
            return constraintName;
        }

        public void setConstraintName(String constraintName) {
            this.constraintName = constraintName;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
