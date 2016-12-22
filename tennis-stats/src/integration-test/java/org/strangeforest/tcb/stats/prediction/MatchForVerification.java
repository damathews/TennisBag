package org.strangeforest.tcb.stats.prediction;

import java.util.*;

import org.strangeforest.tcb.stats.model.*;

public class MatchForVerification {

	public final int winnerId;
	public final int loserId;
	public final Date date;
	public final TournamentLevel level;
	public final Surface surface;
	public final Round round;
	public final short best_of;

	public MatchForVerification(int winnerId, int loserId, Date date, String level, String surface, String round, short best_of) {
		this.winnerId = winnerId;
		this.loserId = loserId;
		this.date = date;
		this.level = TournamentLevel.decode(level);
		this.surface = Surface.safeDecode(surface);
		this.round = Round.decode(round);
		this.best_of = best_of;
	}
}