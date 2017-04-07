DROP FUNCTION weeks(DATE, DATE);
DROP FUNCTION season_start(INTEGER);
DROP FUNCTION season_end(INTEGER);
DROP FUNCTION season_weeks(DATE, DATE);
DROP FUNCTION next_season_weeks(DATE, DATE);
DROP FUNCTION tournament_end(DATE, tournament_level, SMALLINT);
DROP FUNCTION player_rank(INTEGER, DATE);
DROP FUNCTION player_rank_points(INTEGER, DATE);
DROP FUNCTION adjust_atp_rank_points(INTEGER, DATE);
DROP FUNCTION player_elo_rating(INTEGER, DATE);
DROP FUNCTION merge_elo_ranking(DATE, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER);
DROP FUNCTION performance_min_entries(TEXT);
DROP FUNCTION statistics_min_entries(TEXT);
DROP FUNCTION max_event_participation(INTEGER);
DROP FUNCTION tournament_level_factor(tournament_level);
