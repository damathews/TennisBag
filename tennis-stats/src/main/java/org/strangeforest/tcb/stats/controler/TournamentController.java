package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@Controller
public class TournamentController extends BaseController {

	@Autowired private TournamentService tournamentService;
	@Autowired private MatchesService matchesService;
	@Autowired private DataService dateService;

	private static final int MAX_RECORD_PLAYERS = 10;

	@RequestMapping("/tournament")
	public ModelAndView tournament(
		@RequestParam(value = "tournamentId") int tournamentId
	) {
		Tournament tournament = tournamentService.getTournament(tournamentId);
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("tournament", tournament);
		modelMap.addAttribute("levels", TournamentLevel.asMap());
		modelMap.addAttribute("surfaces", Surface.asMap());
		return new ModelAndView("tournament", modelMap);
	}

	@RequestMapping("/tournamentEvents")
	public ModelAndView tournamentEvents() {
		List<Integer> seasons = dateService.getSeasons();
		List<TournamentItem> tournaments = tournamentService.getTournaments();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("seasons", seasons);
		modelMap.addAttribute("levels", TournamentLevel.MAIN_TOURNAMENT_LEVELS);
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("tournaments", tournaments);
		return new ModelAndView("tournamentEvents", modelMap);
	}

	@RequestMapping("/tournamentEvent")
	public ModelAndView tournamentEvent(
		@RequestParam(value = "tournamentEventId") int tournamentEventId
	) {
		TournamentEvent tournamentEvent = tournamentService.getTournamentEvent(tournamentEventId);
		TournamentEventDraw draw = matchesService.getTournamentEventDraw(tournamentEventId);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("tournamentEvent", tournamentEvent);
		modelMap.addAttribute("draw", draw);
		modelMap.addAttribute("levels", TournamentLevel.asMap());
		modelMap.addAttribute("surfaces", Surface.asMap());
		return new ModelAndView("tournamentEvent", modelMap);
	}

	@RequestMapping("/tournamentRecords")
	public ModelAndView tournamentRecords(
		@RequestParam(value = "tournamentId") int tournamentId
	) {
		List<PlayerRecordRow> recordTitles = tournamentService.getTournamentRecord(tournamentId, "W", MAX_RECORD_PLAYERS);
		List<PlayerRecordRow> recordFinals = tournamentService.getTournamentRecord(tournamentId, "F", MAX_RECORD_PLAYERS);
		List<PlayerRecordRow> recordSemiFinals = tournamentService.getTournamentRecord(tournamentId, "SF", MAX_RECORD_PLAYERS);
		List<PlayerRecordRow> recordAppearances = tournamentService.getTournamentRecord(tournamentId, "R128", MAX_RECORD_PLAYERS);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("recordTitles", recordTitles);
		modelMap.addAttribute("recordFinals", recordFinals);
		modelMap.addAttribute("recordSemiFinals", recordSemiFinals);
		modelMap.addAttribute("recordAppearances", recordAppearances);
		return new ModelAndView("tournamentRecords", modelMap);
	}
}