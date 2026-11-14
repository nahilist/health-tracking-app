 -- ========================================================================
-- Pulse Health: Initial PostgreSQL Schema for Hybrid Sync
-- ========================================================================

-- Enable UUID extension if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ------------------------------------------------------------------------
-- Table: hydration_entries
-- Matches local Room entity `HydrationEntryEntity`
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.hydration_entries (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    amount_milliliters INTEGER NOT NULL CHECK (amount_milliliters > 0),
    recorded_at_epoch_millis BIGINT NOT NULL,
    source TEXT NOT NULL DEFAULT 'manual',
    updated_at_epoch_millis BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    deleted_at_epoch_millis BIGINT DEFAULT NULL
);

-- Indexes for efficient range queries and user isolation
CREATE INDEX IF NOT EXISTS idx_hydration_user_recorded 
    ON public.hydration_entries (user_id, recorded_at_epoch_millis DESC);

CREATE INDEX IF NOT EXISTS idx_hydration_user_updated 
    ON public.hydration_entries (user_id, updated_at_epoch_millis DESC);

-- Enable Row-Level Security (RLS)
ALTER TABLE public.hydration_entries ENABLE ROW LEVEL SECURITY;

-- RLS Policies: Authenticated users can only read, insert, update and delete their own records
CREATE POLICY "Users can only read own hydration entries"
    ON public.hydration_entries
    FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "Users can only insert own hydration entries"
    ON public.hydration_entries
    FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can only update own hydration entries"
    ON public.hydration_entries
    FOR UPDATE
    TO authenticated
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can only delete own hydration entries"
    ON public.hydration_entries
    FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);

-- ------------------------------------------------------------------------
-- Table: daily_health_summaries
-- Matches local Room entity `DailyHealthSummaryEntity`
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.daily_health_summaries (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    steps BIGINT DEFAULT 0,
    heart_rate_bpm BIGINT DEFAULT NULL,
    sleep_minutes BIGINT DEFAULT NULL,
    water_liters DOUBLE PRECISION DEFAULT 0.0,
    active_calories BIGINT DEFAULT NULL,
    stand_hours BIGINT DEFAULT NULL,
    exercise_minutes BIGINT DEFAULT NULL,
    source TEXT NOT NULL DEFAULT 'local',
    updated_at_epoch_millis BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    UNIQUE(user_id, date)
);

CREATE INDEX IF NOT EXISTS idx_summaries_user_date 
    ON public.daily_health_summaries (user_id, date DESC);

-- Enable Row-Level Security (RLS)
ALTER TABLE public.daily_health_summaries ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can only read own summaries"
    ON public.daily_health_summaries
    FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "Users can only insert own summaries"
    ON public.daily_health_summaries
    FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can only update own summaries"
    ON public.daily_health_summaries
    FOR UPDATE
    TO authenticated
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can only delete own summaries"
    ON public.daily_health_summaries
    FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);
