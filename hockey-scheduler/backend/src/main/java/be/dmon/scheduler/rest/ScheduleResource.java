package be.dmon.scheduler.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatch;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatchTotal;
import ai.timefold.solver.core.api.score.constraint.Indictment;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;

import be.dmon.scheduler.domain.FieldConfig;
import be.dmon.scheduler.domain.Team;
import be.dmon.scheduler.domain.TrainingSchedule;
import be.dmon.scheduler.domain.TrainingSession;

import jakarta.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/schedule")
@ApplicationScoped
public class ScheduleResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleResource.class);

    private final SolverManager<TrainingSchedule, String> solverManager;
    private final SolutionManager<TrainingSchedule, HardSoftScore> solutionManager;

    private final ConcurrentMap<String, Job> jobIdToJob = new ConcurrentHashMap<>();

    @Inject
    public ScheduleResource(SolverManager<TrainingSchedule, String> solverManager,
                           SolutionManager<TrainingSchedule, HardSoftScore> solutionManager) {
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
    }

    @POST
    @Path("/solve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String solve(InputDataset input) {
        // Generate TrainingSession objects for each team
        List<TrainingSession> sessions = new ArrayList<>();
        int sessionCounter = 0;
        for (Team team : input.getTeams()) {
            for (int i = 0; i < team.getTrainingsPerWeek(); i++) {
                sessionCounter++;
                sessions.add(new TrainingSession("session-" + sessionCounter, team));
            }
        }

        TrainingSchedule schedule = new TrainingSchedule(
                input.getTrainers(),
                input.getTeams(),
                input.getTimeSlots(),
                List.of(new FieldConfig("field", input.getFieldCapacity())),
                sessions);

        String jobId = UUID.randomUUID().toString();
        jobIdToJob.put(jobId, Job.ofSchedule(schedule));

        solverManager.solveBuilder()
                .withProblemId(jobId)
                .withProblemFinder(id -> jobIdToJob.get(id).schedule)
                .withBestSolutionEventConsumer(event ->
                        jobIdToJob.put(jobId, Job.ofSchedule(event.solution())))
                .withExceptionHandler((id, exception) -> {
                    jobIdToJob.put(jobId, Job.ofException(exception));
                    LOGGER.error("Failed solving jobId ({}).", jobId, exception);
                })
                .run();

        return jobId;
    }

    @GET
    @Path("/{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public TrainingSchedule getSchedule(@PathParam("jobId") String jobId) {
        TrainingSchedule schedule = getScheduleAndCheckForExceptions(jobId);
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        schedule.setSolverStatus(solverStatus);
        return schedule;
    }

    @GET
    @Path("/{jobId}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public TrainingSchedule getStatus(@PathParam("jobId") String jobId) {
        TrainingSchedule schedule = getScheduleAndCheckForExceptions(jobId);
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        return new TrainingSchedule(schedule.getScore(), solverStatus);
    }

    @DELETE
    @Path("/{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public TrainingSchedule stopSolving(@PathParam("jobId") String jobId) {
        solverManager.terminateEarly(jobId);
        return getSchedule(jobId);
    }

    @GET
    @Path("/{jobId}/analysis")
    @Produces(MediaType.APPLICATION_JSON)
    public ScoreAnalysis getScoreAnalysis(@PathParam("jobId") String jobId) {
        TrainingSchedule schedule = getScheduleAndCheckForExceptions(jobId);

        // Calculate statistics
        long assignedCount = schedule.getSessions().stream()
                .filter(s -> s.getTrainer() != null && s.getTimeSlot() != null)
                .count();
        int totalSessions = schedule.getSessions().size();
        int unassignedCount = totalSessions - (int) assignedCount;

        // Get score breakdown
        HardSoftScore score = schedule.getScore();
        int hardScore = score != null ? score.hardScore() : 0;
        int softScore = score != null ? score.softScore() : 0;

        // Get constraint matches using ScoreExplanation
        List<ScoreAnalysis.ConstraintMatchInfo> constraintMatches = new ArrayList<>();

        if (score != null && !score.isSolutionInitialized()) {
            // Score not yet calculated, return basic info
            return new ScoreAnalysis(hardScore, softScore, (int) assignedCount,
                                    unassignedCount, totalSessions, constraintMatches);
        }

        try {
            ScoreExplanation<TrainingSchedule, HardSoftScore> explanation =
                solutionManager.explain(schedule);

            for (ConstraintMatchTotal<HardSoftScore> matchTotal : explanation.getConstraintMatchTotalMap().values()) {
                String constraintName = matchTotal.getConstraintName();
                String constraintPackage = matchTotal.getConstraintPackage();
                HardSoftScore constraintScore = matchTotal.getScore();
                int matchCount = matchTotal.getConstraintMatchCount();

                // Determine level (HARD or SOFT)
                String level = "SOFT";
                int scoreValue = constraintScore.softScore();
                if (constraintScore.hardScore() != 0) {
                    level = "HARD";
                    scoreValue = constraintScore.hardScore();
                }

                // Only include constraints that actually matched (non-zero score)
                if (scoreValue != 0) {
                    constraintMatches.add(new ScoreAnalysis.ConstraintMatchInfo(
                        constraintName, constraintPackage, scoreValue, matchCount, level));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not generate score explanation for jobId ({}): {}", jobId, e.getMessage());
        }

        return new ScoreAnalysis(hardScore, softScore, (int) assignedCount,
                                unassignedCount, totalSessions, constraintMatches);
    }

    @GET
    @Path("/{jobId}/sessions/analysis")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SessionAnalysis> getSessionAnalysis(@PathParam("jobId") String jobId) {
        TrainingSchedule schedule = getScheduleAndCheckForExceptions(jobId);
        List<SessionAnalysis> sessionAnalyses = new ArrayList<>();

        HardSoftScore score = schedule.getScore();
        if (score == null || !score.isSolutionInitialized()) {
            // Return basic info without violations
            for (TrainingSession session : schedule.getSessions()) {
                String timeSlotInfo = session.getTimeSlot() != null
                    ? session.getTimeSlot().getDayOfWeek() + " " + session.getTimeSlot().getStartHour() + ":00"
                    : "Unassigned";
                String trainerName = session.getTrainer() != null
                    ? session.getTrainer().getName()
                    : "Unassigned";

                sessionAnalyses.add(new SessionAnalysis(
                    session.getId(),
                    session.getTeam().getName(),
                    trainerName,
                    timeSlotInfo,
                    false,
                    0,
                    List.of()
                ));
            }
            return sessionAnalyses;
        }

        try {
            ScoreExplanation<TrainingSchedule, HardSoftScore> explanation =
                solutionManager.explain(schedule);

            // Get indictments (violations) per session
            for (TrainingSession session : schedule.getSessions()) {
                String timeSlotInfo = session.getTimeSlot() != null
                    ? session.getTimeSlot().getDayOfWeek() + " " + session.getTimeSlot().getStartHour() + ":00"
                    : "Unassigned";
                String trainerName = session.getTrainer() != null
                    ? session.getTrainer().getName()
                    : "Unassigned";

                List<SessionAnalysis.ViolationInfo> violations = new ArrayList<>();
                int totalSessionScore = 0;

                // Get indictment for this session
                Indictment<HardSoftScore> indictment = explanation.getIndictmentMap().get(session);

                if (indictment != null) {
                    for (ConstraintMatch<HardSoftScore> match : indictment.getConstraintMatchSet()) {
                        HardSoftScore constraintScore = match.getScore();
                        String level = "SOFT";
                        int scoreValue = constraintScore.softScore();

                        if (constraintScore.hardScore() != 0) {
                            level = "HARD";
                            scoreValue = constraintScore.hardScore();
                        }

                        totalSessionScore += scoreValue;

                        // Create a descriptive message
                        String message = createViolationMessage(session, match.getConstraintName());

                        violations.add(new SessionAnalysis.ViolationInfo(
                            match.getConstraintName(),
                            level,
                            scoreValue,
                            message
                        ));
                    }
                }

                sessionAnalyses.add(new SessionAnalysis(
                    session.getId(),
                    session.getTeam().getName(),
                    trainerName,
                    timeSlotInfo,
                    !violations.isEmpty(),
                    totalSessionScore,
                    violations
                ));
            }
        } catch (Exception e) {
            LOGGER.warn("Could not generate session analysis for jobId ({}): {}", jobId, e.getMessage());
        }

        return sessionAnalyses;
    }

    private String createViolationMessage(TrainingSession session, String constraintName) {
        // Create human-readable messages based on constraint name
        switch (constraintName) {
            case "Trainer qualification":
                return session.getTrainer() != null
                    ? "Trainer " + session.getTrainer().getName() + " is not qualified for " + session.getTeam().getAgeGroup()
                    : "No qualified trainer assigned";
            case "Team slot availability":
                return session.getTimeSlot() != null
                    ? "Team " + session.getTeam().getName() + " is not available at " + session.getTimeSlot().getDayOfWeek() + " " + session.getTimeSlot().getStartHour() + ":00"
                    : "Time slot not available for team";
            case "Trainer time conflict":
                return "Trainer has another session at the same time";
            case "Trainer availability":
                return session.getTrainer() != null
                    ? "Trainer " + session.getTrainer().getName() + " is not available at this time"
                    : "Trainer availability issue";
            case "Field capacity exceeded":
                return "Too many players on the field at this time";
            case "Trainer max hours":
                return session.getTrainer() != null
                    ? "Trainer " + session.getTrainer().getName() + " exceeds max weekly hours"
                    : "Trainer hours exceeded";
            case "Trainer team preference":
                return "Trainer prefers a different team";
            default:
                return "Constraint: " + constraintName;
        }
    }

    private TrainingSchedule getScheduleAndCheckForExceptions(String jobId) {
        Job job = jobIdToJob.get(jobId);
        if (job == null) {
            throw new IllegalArgumentException("No schedule found for jobId: " + jobId);
        }
        if (job.exception != null) {
            throw new IllegalStateException("Solving failed for jobId: " + jobId, (Throwable) job.exception);
        }
        return job.schedule;
    }

    private record Job(TrainingSchedule schedule, Object exception) {
        static Job ofSchedule(TrainingSchedule schedule) {
            return new Job(schedule, null);
        }

        static Job ofException(Throwable error) {
            return new Job(null, error);
        }
    }
}
