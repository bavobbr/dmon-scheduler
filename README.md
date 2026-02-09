# Hockey Training Scheduler

An intelligent scheduling application for field hockey clubs to automatically assign trainers and time slots to team training sessions while respecting complex constraints like field capacity, trainer availability, team preferences, and qualification requirements.

Built with **Timefold Solver** (constraint satisfaction AI) and a modern full-stack architecture.

---

## Project Structure

```
dmon-scheduler/
├── CLAUDE.md                  # AI knowledge base for Timefold development
├── README.md
├── docs/timefold-reference/   # Condensed Timefold reference guides
├── timefold-scheduling/       # 7 working Timefold quickstart examples
│   └── examples/
│       ├── employee-scheduling/
│       ├── school-timetabling/
│       ├── flight-crew-scheduling/
│       ├── conference-scheduling/
│       ├── meeting-scheduling/
│       ├── bed-allocation/
│       └── task-assigning/
└── hockey-scheduler/          # The hockey training scheduler application
    ├── backend/               # Quarkus + Timefold Solver (Java 17)
    └── frontend/              # React + TypeScript + Tailwind CSS v4
```

### CLAUDE.md — AI-Assisted Development

This project uses a comprehensive `CLAUDE.md` file that serves as a knowledge base for AI-assisted development with [Claude Code](https://claude.ai/). It contains:

- Timefold Solver modeling patterns, constraint stream recipes, and annotation references
- Links to 7 ground-truth quickstart examples in `timefold-scheduling/examples/`
- Common pitfalls, troubleshooting guides, and import cheat sheets
- A constraint catalog covering common scheduling use cases

This enables Claude to generate correct Timefold code by referencing real working examples rather than relying on training data alone.

---

## 🎯 Problem Statement

A field hockey club needs to schedule weekly training sessions for multiple teams on a shared field. The system must:

- Assign trainers to teams based on their qualifications and preferences
- Assign teams to time slots based on availability and constraints
- Respect field capacity (maximum players on the field at once)
- Honor trainer availability (max hours per week, max sessions per day)
- Balance soft preferences (youngest teams train earlier, minimize trainer gaps, preferred team assignments)

This is a **constraint satisfaction problem** — finding a valid assignment that satisfies all hard rules while optimizing soft preferences.

---

## 🏗️ Tech Stack

### Backend
- **Java 17** — Modern Java with records and pattern matching
- **Quarkus 3.30.6** — Supersonic Subatomic Java framework
- **Timefold Solver 1.30.0** — Open-source constraint satisfaction AI (continuation of OptaPlanner)
- **Maven** — Build and dependency management
- **JUnit 5 + AssertJ** — Testing framework

### Frontend
- **React 18** — UI library
- **TypeScript** — Type-safe JavaScript
- **Vite** — Lightning-fast build tool
- **Tailwind CSS v4** — Utility-first CSS framework

### API
- **REST** — JSON over HTTP
- **CORS** — Cross-origin resource sharing for local development

---

## 📐 Domain Model

### Problem Facts (Fixed Input)

**Trainer** — Person who can train teams
- `id`, `name`
- `maxHoursPerWeek` — Maximum total hours (e.g., 6)
- `trainableAgeGroups` — Which age groups this trainer can train (e.g., U8, U10, U12)
- `preferredTeamId` — Optional preference (e.g., parent wants to train their kid's team)

**Team** — Group of players that needs training
- `id`, `name`, `ageGroup` (U6, U7, U8, U10, U11, U12, U14, U16, U19, SENIOR)
- `size` — Number of players
- `trainingsPerWeek` — How many sessions needed (e.g., 2)
- `availableDays` — Days the team can train (e.g., Mon, Wed, Fri)
- `earliestHour`, `latestHour` — Time window (e.g., 17:00 - 20:00)

**TimeSlot** — Available training time
- `id` (e.g., "MONDAY-18")
- `dayOfWeek`, `startHour` (duration: 1 hour)

**FieldConfig** — Shared resource constraint
- `capacity` — Max total players on field at once (e.g., 60)

### Planning Entity (Solver Assigns)

**TrainingSession** — A specific training that needs scheduling
- `team` — Which team (fixed)
- `@PlanningVariable timeSlot` — When to train (solver assigns)
- `@PlanningVariable trainer` — Who trains (solver assigns)

The solver generates one `TrainingSession` per team per `trainingsPerWeek` and assigns the two planning variables.

### Solution

**TrainingSchedule** — Complete problem + solution
- Collections of trainers, teams, time slots, field config
- List of `TrainingSession` entities
- `HardSoftScore` — Quality metric (hard = constraints, soft = preferences)

---

## ⚖️ Constraints

### Hard Constraints (Must Be Satisfied)

| ID | Name | Description |
|----|------|-------------|
| **H1** | Team slot availability | Session must be on a day the team is available and within their hour range |
| **H2** | Trainer qualification | Trainer must be qualified for the team's age group |
| **H3** | Trainer conflict | Trainer cannot have two sessions at the same time |
| **H4** | Team conflict | Team cannot have two sessions at the same time |
| **H5** | Team separate days | All of a team's sessions must be on different days |
| **H6** | Field capacity | Total players on field at any time slot ≤ field capacity |
| **H7** | Trainer max hours | Trainer cannot exceed their max hours per week |
| **H8** | Trainer max 3/day | Trainer cannot have more than 3 sessions on the same day |

### Soft Constraints (Preferences to Optimize)

| ID | Name | Weight | Description |
|----|------|--------|-------------|
| **S1** | Team non-consecutive days | 3 | Avoid scheduling team sessions on adjacent days (recovery time) |
| **S2** | Youngest teams first | 1 | Younger teams (U6, U8) should train earlier in the day |
| **S3** | Trainer no gaps | 4 | Minimize gaps between a trainer's sessions on the same day |
| **S4** | Trainer consistency | 1 | Same team should have the same trainer across sessions |
| **S5** | Trainer preferred team | 5 | Reward trainers assigned to their preferred team (e.g., parent-coach) |

**Score Format:** `0hard/-12soft`
- Hard score = 0 means all hard constraints satisfied (feasible solution)
- Soft score measures quality (higher = better preferences satisfied)

---

## 🧪 Testing

### Constraint Unit Tests (26 tests)
Each constraint is tested in isolation using Timefold's `ConstraintVerifier`:
- H1-H8: Verify violations are penalized correctly
- S1-S5: Verify preferences are scored correctly

### Integration Tests (3 tests)
Full end-to-end solving with realistic datasets:

1. **Small Club** — 3 teams, 3 trainers, 6 sessions → Verifies basic feasibility
2. **Realistic Club** — 6 teams across age groups (U6-U16), 5 trainers, 11 sessions → Tests mixed qualifications, parent preferences, tight scheduling
3. **Tight Capacity** — 3 teams × 20 players, field capacity 35 → Tests field capacity constraint

Each integration test verifies:
- ✅ All sessions assigned (no nulls)
- ✅ Trainers qualified for assigned teams
- ✅ Teams scheduled on available days/hours
- ✅ No trainer/team conflicts
- ✅ Field capacity respected
- ✅ Trainer hour limits respected

**Run tests:**
```bash
cd hockey-scheduler/backend
mvn test
```

---

## 🚀 Local Deployment

### Prerequisites
- **Java 17+** (check: `java -version`)
- **Maven 3.8+** (check: `mvn -version`)
- **Node.js 18+** (check: `node -version`)
- **npm** (check: `npm -version`)

### 1. Clone the Repository
```bash
git clone https://github.com/bavobbr/dmon-scheduler.git
cd dmon-scheduler
```

### 2. Start the Backend (Quarkus + Timefold)
```bash
cd hockey-scheduler/backend
mvn quarkus:dev
```

The backend starts on **http://localhost:8080**

**Endpoints:**
- `POST /schedule/solve` — Submit a scheduling problem (returns job ID)
- `GET /schedule/{jobId}` — Get current best solution
- `GET /schedule/{jobId}/status` — Get solver status
- `DELETE /schedule/{jobId}` — Stop solving early

**Dev Mode Features:**
- Live reload on code changes
- Solver runs for 30s (configurable in `application.properties`)

### 3. Start the Frontend (React + Vite)
```bash
cd hockey-scheduler/frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:5173**

**Pages:**
- **Trainers** — Add/edit trainers with qualifications and preferences
- **Teams** — Add/edit teams with availability and size
- **Field & Slots** — Select available time slots and set field capacity
- **Schedule** — Click "Solve" to generate optimal schedule, view as weekly calendar

### 4. Using the Application

1. **Add Trainers** — Define who can train, their availability, and qualifications
   - Example: "Jan" can train U8/U10/U12, max 6 hours/week, prefers "U10A" team

2. **Add Teams** — Define teams that need training
   - Example: "U10A" (14 players, 2 trainings/week, available Mon/Wed/Fri 17:00-20:00)

3. **Select Time Slots** — Click or drag on the weekly grid to enable slots
   - Example: Mon-Fri, 17:00-19:00 (15 slots total)

4. **Set Field Capacity** — Max players allowed on field at once (e.g., 60)

5. **Export/Import JSON** — Save/load datasets for testing or backup

6. **Solve** — Click "Solve" to run the constraint solver
   - Polls every 2 seconds for progress
   - Shows score and solver status
   - Renders solution as a color-coded weekly calendar

---

## 📊 Example Solution

**Input:**
- 3 teams (U8A, U10A, U12A), each needs 2 trainings/week
- 3 trainers with overlapping qualifications
- Mon-Fri, 17:00-19:00 time slots
- Field capacity: 60 players

**Output (after 5 seconds):**
```
Score: 0hard/-9soft
Status: NOT_SOLVING

Weekly Calendar:
         Mon         Tue         Wed         Thu         Fri
17:00    U8A         U10A        U8A         U12A        U10A
         Jan (12p)   Piet (14p)  Els (12p)   Piet (16p)  Jan (14p)

18:00    U12A        -           -           -           -
         Els (16p)
```

All hard constraints satisfied + good soft score (minimal gaps, preferred trainers matched).

---

## 🔧 Configuration

### Backend (`hockey-scheduler/backend/src/main/resources/application.properties`)

```properties
# Solver termination
quarkus.timefold.solver.termination.spent-limit=5m
%dev.quarkus.timefold.solver.termination.spent-limit=30s

# CORS for local development
quarkus.http.cors.enabled=true
quarkus.http.cors.origins=http://localhost:5173

# Logging
quarkus.log.category."ai.timefold.solver".level=INFO
```

**Adjust solver time:**
- Production: 5 minutes
- Development: 30 seconds
- Tests: 5 seconds

### Frontend (`hockey-scheduler/frontend/src/api/scheduleApi.ts`)

```typescript
const BASE_URL = 'http://localhost:8080/schedule';
```

Change `BASE_URL` for production deployment.

---

## 📦 Production Build

### Backend (Native Binary with GraalVM)
```bash
cd hockey-scheduler/backend
mvn package -Dnative
./target/hockey-scheduler-1.0-SNAPSHOT-runner
```

### Backend (JVM JAR)
```bash
cd hockey-scheduler/backend
mvn package
java -jar target/quarkus-app/quarkus-run.jar
```

### Frontend (Static Assets)
```bash
cd hockey-scheduler/frontend
npm run build
# Output: hockey-scheduler/frontend/dist/
# Serve with nginx, Apache, or CDN
```

---

## 🏛️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│  Frontend (React + TypeScript)                          │
│  - Data entry forms (trainers, teams, slots)            │
│  - Weekly calendar visualization                        │
│  - JSON import/export                                   │
└────────────────┬────────────────────────────────────────┘
                 │ HTTP/JSON
                 │
┌────────────────┴────────────────────────────────────────┐
│  Backend (Quarkus REST API)                             │
│  - ScheduleResource (POST /solve, GET /{id})            │
│  - Async solving with SolverManager                     │
│  - CORS, JSON serialization                             │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────┴────────────────────────────────────────┐
│  Timefold Solver (Constraint Satisfaction Engine)       │
│  - TrainingScheduleConstraintProvider (13 constraints)  │
│  - Construction heuristic + Local search                │
│  - HardSoftScore evaluation (incremental)               │
└─────────────────────────────────────────────────────────┘
```

**Data Flow:**
1. Frontend collects trainers, teams, slots, capacity
2. POST to `/schedule/solve` with JSON dataset
3. Backend generates `TrainingSession` entities (team × trainingsPerWeek)
4. Solver assigns `timeSlot` and `trainer` to each session
5. Frontend polls `/schedule/{jobId}` for progress
6. Solution rendered as interactive weekly calendar

---

## 🎓 Key Concepts

### Constraint Satisfaction vs. Brute Force
- **Brute force**: Try all combinations → O(n^m) complexity
  - 11 sessions × 47 timeslots × 5 trainers = 3.5 × 10²⁵ combinations
- **Constraint solver**: Intelligent search using heuristics and incremental scoring
  - Finds feasible solutions in seconds, continues optimizing until time limit

### Incremental Score Calculation
Timefold tracks which constraints are affected by each move:
- Moving one session only recalculates 5-10 constraint matches
- Not 10,000+ per step (full recalc would be too slow)

### Construction Heuristic + Local Search
1. **Phase 1: Construction Heuristic** (0.5s)
   - Assign all sessions greedily (first feasible solution)
2. **Phase 2: Local Search** (29.5s)
   - Swap assignments, move sessions, try to improve score
   - Uses tabu search to avoid getting stuck in local optima

---

## 📚 References

- **Timefold Solver** — https://timefold.ai/
- **Quarkus** — https://quarkus.io/
- **Constraint Satisfaction** — https://en.wikipedia.org/wiki/Constraint_satisfaction_problem
- **Employee Scheduling Example** (similar domain) — https://github.com/TimefoldAI/timefold-quickstarts/tree/stable/use-cases/employee-scheduling

---

## 🤝 Contributing

Contributions welcome! Areas for improvement:

- **More constraints**: No back-to-back sessions, team rivalry avoidance, weather preferences
- **Multi-week scheduling**: Plan entire season, handle holidays/tournaments
- **Real-time updates**: WebSocket for live score updates during solving
- **Conflict resolution UI**: Show which constraints are violated and suggestions
- **Export to calendar**: iCal/Google Calendar integration
- **Analytics dashboard**: Historical statistics, trainer utilization, field usage

---

## 📄 License

MIT License — See LICENSE file for details

---

## 👥 Authors

- **Bavo Bruylandt** ([@bavobbr](https://github.com/bavobbr))
- Built with assistance from **Claude** (Anthropic) — Sonnet 4.5 & Opus 4.6

---

## 🙏 Acknowledgments

- **Timefold team** for the excellent constraint solver and documentation
- **OptaPlanner community** for pioneering open-source constraint satisfaction
- Field hockey clubs worldwide for the real-world scheduling challenges that inspired this project
