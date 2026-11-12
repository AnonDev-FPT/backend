-- V3: Add Tournament Problems, Participants, and Submissions Tables for Code Arena

-- 17. Table tournament_problems
CREATE TABLE IF NOT EXISTS tournament_problems (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tournament_id UUID NOT NULL REFERENCES coding_tournaments(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    problem_statement TEXT NOT NULL,
    points INT NOT NULL DEFAULT 100,
    time_limit_ms INT NOT NULL DEFAULT 1000,
    memory_limit_kb INT NOT NULL DEFAULT 256000,
    starter_code_json JSONB DEFAULT '{}',
    test_cases_json JSONB NOT NULL DEFAULT '[]',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 18. Table tournament_participants
CREATE TABLE IF NOT EXISTS tournament_participants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tournament_id UUID NOT NULL REFERENCES coding_tournaments(id) ON DELETE CASCADE,
    candidate_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    registered_at TIMESTAMPTZ DEFAULT NOW(),
    total_score INT NOT NULL DEFAULT 0,
    penalty_seconds INT NOT NULL DEFAULT 0,
    rank_position INT,
    is_disqualified BOOLEAN DEFAULT FALSE,
    UNIQUE (tournament_id, candidate_id)
);

-- 19. Table tournament_submissions
CREATE TABLE IF NOT EXISTS tournament_submissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tournament_id UUID NOT NULL REFERENCES coding_tournaments(id) ON DELETE CASCADE,
    problem_id UUID NOT NULL REFERENCES tournament_problems(id) ON DELETE CASCADE,
    candidate_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    language VARCHAR(50) NOT NULL DEFAULT 'JAVA',
    source_code TEXT NOT NULL,
    verdict VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (verdict IN ('ACCEPTED', 'WRONG_ANSWER', 'COMPILE_ERROR', 'RUNTIME_ERROR', 'TIME_LIMIT_EXCEEDED', 'PENDING')),
    passed_test_cases INT NOT NULL DEFAULT 0,
    total_test_cases INT NOT NULL DEFAULT 0,
    execution_time_ms INT DEFAULT 0,
    score_awarded INT NOT NULL DEFAULT 0,
    submitted_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tournament_problems_tournament ON tournament_problems(tournament_id);
CREATE INDEX IF NOT EXISTS idx_tournament_participants_rank ON tournament_participants(tournament_id, total_score DESC, penalty_seconds ASC);
CREATE INDEX IF NOT EXISTS idx_tournament_submissions_cand ON tournament_submissions(tournament_id, problem_id, candidate_id);

