package be.dmon.scheduler.rest;

import java.util.List;

public class ScoreAnalysis {

    private int hardScore;
    private int softScore;
    private int assignedSessions;
    private int unassignedSessions;
    private int totalSessions;
    private List<ConstraintMatchInfo> constraintMatches;

    public ScoreAnalysis() {
    }

    public ScoreAnalysis(int hardScore, int softScore, int assignedSessions,
                        int unassignedSessions, int totalSessions,
                        List<ConstraintMatchInfo> constraintMatches) {
        this.hardScore = hardScore;
        this.softScore = softScore;
        this.assignedSessions = assignedSessions;
        this.unassignedSessions = unassignedSessions;
        this.totalSessions = totalSessions;
        this.constraintMatches = constraintMatches;
    }

    public int getHardScore() {
        return hardScore;
    }

    public void setHardScore(int hardScore) {
        this.hardScore = hardScore;
    }

    public int getSoftScore() {
        return softScore;
    }

    public void setSoftScore(int softScore) {
        this.softScore = softScore;
    }

    public int getAssignedSessions() {
        return assignedSessions;
    }

    public void setAssignedSessions(int assignedSessions) {
        this.assignedSessions = assignedSessions;
    }

    public int getUnassignedSessions() {
        return unassignedSessions;
    }

    public void setUnassignedSessions(int unassignedSessions) {
        this.unassignedSessions = unassignedSessions;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public List<ConstraintMatchInfo> getConstraintMatches() {
        return constraintMatches;
    }

    public void setConstraintMatches(List<ConstraintMatchInfo> constraintMatches) {
        this.constraintMatches = constraintMatches;
    }

    public static class ConstraintMatchInfo {
        private String constraintName;
        private String constraintPackage;
        private int score;
        private int matchCount;
        private String level; // "HARD" or "SOFT"

        public ConstraintMatchInfo() {
        }

        public ConstraintMatchInfo(String constraintName, String constraintPackage,
                                  int score, int matchCount, String level) {
            this.constraintName = constraintName;
            this.constraintPackage = constraintPackage;
            this.score = score;
            this.matchCount = matchCount;
            this.level = level;
        }

        public String getConstraintName() {
            return constraintName;
        }

        public void setConstraintName(String constraintName) {
            this.constraintName = constraintName;
        }

        public String getConstraintPackage() {
            return constraintPackage;
        }

        public void setConstraintPackage(String constraintPackage) {
            this.constraintPackage = constraintPackage;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public int getMatchCount() {
            return matchCount;
        }

        public void setMatchCount(int matchCount) {
            this.matchCount = matchCount;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }
    }
}
