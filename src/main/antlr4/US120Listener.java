// Generated from US120.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link US120Parser}.
 */
public interface US120Listener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link US120Parser#flightPlan}.
	 * @param ctx the parse tree
	 */
	void enterFlightPlan(US120Parser.FlightPlanContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#flightPlan}.
	 * @param ctx the parse tree
	 */
	void exitFlightPlan(US120Parser.FlightPlanContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#flight}.
	 * @param ctx the parse tree
	 */
	void enterFlight(US120Parser.FlightContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#flight}.
	 * @param ctx the parse tree
	 */
	void exitFlight(US120Parser.FlightContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#flightType}.
	 * @param ctx the parse tree
	 */
	void enterFlightType(US120Parser.FlightTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#flightType}.
	 * @param ctx the parse tree
	 */
	void exitFlightType(US120Parser.FlightTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#leg}.
	 * @param ctx the parse tree
	 */
	void enterLeg(US120Parser.LegContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#leg}.
	 * @param ctx the parse tree
	 */
	void exitLeg(US120Parser.LegContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#departure}.
	 * @param ctx the parse tree
	 */
	void enterDeparture(US120Parser.DepartureContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#departure}.
	 * @param ctx the parse tree
	 */
	void exitDeparture(US120Parser.DepartureContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#arrival}.
	 * @param ctx the parse tree
	 */
	void enterArrival(US120Parser.ArrivalContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#arrival}.
	 * @param ctx the parse tree
	 */
	void exitArrival(US120Parser.ArrivalContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#route}.
	 * @param ctx the parse tree
	 */
	void enterRoute(US120Parser.RouteContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#route}.
	 * @param ctx the parse tree
	 */
	void exitRoute(US120Parser.RouteContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#segment}.
	 * @param ctx the parse tree
	 */
	void enterSegment(US120Parser.SegmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#segment}.
	 * @param ctx the parse tree
	 */
	void exitSegment(US120Parser.SegmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#beginNode}.
	 * @param ctx the parse tree
	 */
	void enterBeginNode(US120Parser.BeginNodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#beginNode}.
	 * @param ctx the parse tree
	 */
	void exitBeginNode(US120Parser.BeginNodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#endNode}.
	 * @param ctx the parse tree
	 */
	void enterEndNode(US120Parser.EndNodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#endNode}.
	 * @param ctx the parse tree
	 */
	void exitEndNode(US120Parser.EndNodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#altSlot}.
	 * @param ctx the parse tree
	 */
	void enterAltSlot(US120Parser.AltSlotContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#altSlot}.
	 * @param ctx the parse tree
	 */
	void exitAltSlot(US120Parser.AltSlotContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#width}.
	 * @param ctx the parse tree
	 */
	void enterWidth(US120Parser.WidthContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#width}.
	 * @param ctx the parse tree
	 */
	void exitWidth(US120Parser.WidthContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#windAngle}.
	 * @param ctx the parse tree
	 */
	void enterWindAngle(US120Parser.WindAngleContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#windAngle}.
	 * @param ctx the parse tree
	 */
	void exitWindAngle(US120Parser.WindAngleContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#speed}.
	 * @param ctx the parse tree
	 */
	void enterSpeed(US120Parser.SpeedContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#speed}.
	 * @param ctx the parse tree
	 */
	void exitSpeed(US120Parser.SpeedContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#coord}.
	 * @param ctx the parse tree
	 */
	void enterCoord(US120Parser.CoordContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#coord}.
	 * @param ctx the parse tree
	 */
	void exitCoord(US120Parser.CoordContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#routes}.
	 * @param ctx the parse tree
	 */
	void enterRoutes(US120Parser.RoutesContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#routes}.
	 * @param ctx the parse tree
	 */
	void exitRoutes(US120Parser.RoutesContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#fuel}.
	 * @param ctx the parse tree
	 */
	void enterFuel(US120Parser.FuelContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#fuel}.
	 * @param ctx the parse tree
	 */
	void exitFuel(US120Parser.FuelContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#airport}.
	 * @param ctx the parse tree
	 */
	void enterAirport(US120Parser.AirportContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#airport}.
	 * @param ctx the parse tree
	 */
	void exitAirport(US120Parser.AirportContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#date}.
	 * @param ctx the parse tree
	 */
	void enterDate(US120Parser.DateContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#date}.
	 * @param ctx the parse tree
	 */
	void exitDate(US120Parser.DateContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#time}.
	 * @param ctx the parse tree
	 */
	void enterTime(US120Parser.TimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#time}.
	 * @param ctx the parse tree
	 */
	void exitTime(US120Parser.TimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(US120Parser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(US120Parser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link US120Parser#unit}.
	 * @param ctx the parse tree
	 */
	void enterUnit(US120Parser.UnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link US120Parser#unit}.
	 * @param ctx the parse tree
	 */
	void exitUnit(US120Parser.UnitContext ctx);
}