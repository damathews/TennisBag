package org.strangeforest.tcb.stats.model;

import java.util.*;

public class Match {

	private final long id;
	private final Date date;
	private final String tournament;
	private final String level;
	private final String surface;
	private final boolean indoor;
	private final String round;
	private final MatchPlayer winner;
	private final MatchPlayer loser;
	private final String score;

	public Match(long id, Date date, String tournament, String level, String surface, boolean indoor, String round,
	             MatchPlayer winner, MatchPlayer loser, String score) {
		this.id = id;
		this.date = date;
		this.tournament = tournament;
		this.level = level;
		this.surface = surface;
		this.indoor = indoor;
		this.round = round;
		this.winner = winner;
		this.loser = loser;
		this.score = score;
	}

	public long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public String getTournament() {
		return tournament;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public boolean isIndoor() {
		return indoor;
	}

	public String getRound() {
		return round;
	}

	public MatchPlayer getWinner() {
		return winner;
	}

	public MatchPlayer getLoser() {
		return loser;
	}

	public String getScore() {
		return score;
	}
}
