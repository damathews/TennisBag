package org.strangeforest.tcb.stats.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

@Service
public class StatsLeadersService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	@Autowired private MinEntries minEntries;

	private static final int MAX_PLAYER_COUNT =  1000;
	private static final int MIN_MATCHES      =   200;
	private static final int MIN_POINTS       = 10000;

	private static final String STATS_LEADERS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM %1$s\n" +
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE p_%2$s + o_%2$s >= :minEntries%3$s";

	private static final String STATS_LEADERS_QUERY = //language=SQL
		"WITH stats_leaders AS (\n" +
		"  SELECT player_id, %1$s AS value\n" +
		"  FROM %2$s\n" +
		"  WHERE p_%3$s + o_%3$s >= :minEntries%4$s\n" +
		"), stats_leaders_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC NULLS LAST) AS rank, player_id, value\n" +
		"  FROM stats_leaders\n" +
		"  WHERE value IS NOT NULL\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, active, value\n" +
		"FROM stats_leaders_ranked\n" +
		"INNER JOIN player_v USING (player_id)%5$s\n" +
		"ORDER BY %6$s NULLS LAST OFFSET :offset LIMIT :limit";

	private static final String SUMMED_STATS_LEADERS_COUNT_QUERY = //language=SQL
		"WITH player_stats AS (\n" +
		"  SELECT player_id, sum(p_%1$s) AS p_%1$s, sum(o_%1$s) AS o_%1$s\n" +
		"  FROM %2$s\n" +
		"  INNER JOIN player_v USING (player_id)%3$s\n" +
		"  GROUP BY player_id\n" +
		")\n" +
		"SELECT count(player_id) AS player_count FROM player_stats\n" +
		"WHERE p_%1$s + o_%1$s >= :minEntries";

	private static final String SUMMED_STATS_LEADERS_QUERY = //language=SQL
		"WITH player_stats AS (\n" +
		"  SELECT player_id, %1$s AS value, sum(p_%2$s) AS p_%2$s, sum(o_%2$s) AS o_%2$s\n" +
		"  FROM %3$s%4$s\n" +
		"  GROUP BY player_id\n" +
		"), stats_leaders AS (\n" +
		"  SELECT player_id, value\n" +
		"  FROM player_stats\n" +
		"  WHERE p_%2$s + o_%2$s >= :minEntries\n" +
		"), stats_leaders_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC NULLS LAST) AS rank, player_id, value\n" +
		"  FROM stats_leaders\n" +
		"  WHERE value IS NOT NULL\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, active, value\n" +
		"FROM stats_leaders_ranked\n" +
		"INNER JOIN player_v USING (player_id)%5$s\n" +
		"ORDER BY %6$s NULLS LAST OFFSET :offset LIMIT :limit";


	@Cacheable("StatsLeaders.Count")
	public int getPlayerCount(String category, PerfStatsFilter filter) {
		return Math.min(MAX_PLAYER_COUNT, getPlayerCount(StatsCategory.get(category), filter));
	}

	protected int getPlayerCount(StatsCategory statsCategory, PerfStatsFilter filter) {
		if (filter.hasTournamentOrTournamentEvent() || filter.hasSurfaceGroup()) {
			return jdbcTemplate.queryForObject(
				format(SUMMED_STATS_LEADERS_COUNT_QUERY, minEntriesColumn(statsCategory), statsTableName(filter), where(filter.getCriteria(), 2)),
				filter.getParams().addValue("minEntries", getMinEntries(statsCategory, filter)),
				Integer.class
			);
		}
		else {
			return jdbcTemplate.queryForObject(
				format(STATS_LEADERS_COUNT_QUERY, statsTableName(filter), minEntriesColumn(statsCategory), filter.getCriteria()),
				filter.getParams().addValue("minEntries", getMinEntries(statsCategory, filter)),
				Integer.class
			);
		}
	}

	@Cacheable("StatsLeaders.Table")
	public BootgridTable<StatsLeaderRow> getStatsLeadersTable(String category, int playerCount, PerfStatsFilter filter, String orderBy, int pageSize, int currentPage) {
		StatsCategory statsCategory = StatsCategory.get(category);
		BootgridTable<StatsLeaderRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			getTableSQL(statsCategory, filter, orderBy),
			filter.getParams().addValue("minEntries", getMinEntries(statsCategory, filter)).addValue("offset", offset).addValue("limit", pageSize),
			rs -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				Boolean active = !filter.hasActive() && !filter.hasSeason() ? rs.getBoolean("active") : null;
				double value = rs.getDouble("value");
				table.addRow(new StatsLeaderRow(rank, playerId, name, countryId, active, value, statsCategory.getType()));
			}
		);
		return table;
	}

	public String getStatsLeadersMinEntries(String category, PerfStatsFilter filter) {
		StatsCategory statsCategory = StatsCategory.get(category);
		return getMinEntries(statsCategory, filter) + (statsCategory.isNeedsStats() ? " points" : " matches");
	}

	private String getTableSQL(StatsCategory statsCategory, PerfStatsFilter filter, String orderBy) {
		return filter.hasTournamentOrTournamentEvent() || filter.hasSurfaceGroup()
	       ? format(SUMMED_STATS_LEADERS_QUERY, statsCategory.getSummedExpression(), minEntriesColumn(statsCategory), statsTableName(filter), where(filter.getBaseCriteria(), 2), where(filter.getSearchCriteria()), orderBy)
	       : format(STATS_LEADERS_QUERY, statsCategory.getExpression(), statsTableName(filter), minEntriesColumn(statsCategory), filter.getBaseCriteria(), where(filter.getSearchCriteria()), orderBy);
	}

	private static String statsTableName(PerfStatsFilter filter) {
		if (filter.hasTournamentOrTournamentEvent())
			return "player_match_stats_v";
		else
			return format("player%1$s%2$s_stats", filter.hasSeason() ? "_season" : "", filter.hasSurface() ? "_surface" : "");
	}

	private String minEntriesColumn(StatsCategory category) {
		return category.isNeedsStats() ? "sv_pt" : "matches";
	}

	private int getMinEntries(StatsCategory category, PerfStatsFilter filter) {
		return minEntries.getFilteredMinEntries(category.isNeedsStats() ? MIN_POINTS : MIN_MATCHES, filter);
	}
}
