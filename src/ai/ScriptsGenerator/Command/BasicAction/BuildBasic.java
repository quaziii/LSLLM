/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.ScriptsGenerator.Command.BasicAction;

import ai.ScriptsGenerator.Command.AbstractBasicAction;
import ai.ScriptsGenerator.Command.Enumerators.EnumPositionType;
import ai.ScriptsGenerator.Command.Enumerators.EnumTypeUnits;
import ai.ScriptsGenerator.CommandInterfaces.IUnitCommand;
import ai.ScriptsGenerator.IParameters.IParameters;
import ai.ScriptsGenerator.IParameters.IPriorityPosition;
import ai.ScriptsGenerator.IParameters.IQuantity;
import ai.ScriptsGenerator.ParametersConcrete.ConstructionTypeParam;
import ai.ScriptsGenerator.ParametersConcrete.PriorityPositionParam;
import ai.ScriptsGenerator.ParametersConcrete.QuantityParam;
import ai.ScriptsGenerator.ParametersConcrete.TypeConcrete;
import ai.ScriptsGenerator.ParametersConcrete.UnitTypeParam;
import ai.abstraction.AbstractAction;
import ai.abstraction.Build;
import ai.abstraction.Train;
import ai.abstraction.pathfinding.PathFinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.UnitAction;
import static rts.UnitAction.DIRECTION_DOWN;
import static rts.UnitAction.DIRECTION_LEFT;
import static rts.UnitAction.DIRECTION_RIGHT;
import static rts.UnitAction.DIRECTION_UP;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;
import util.Pair;

/**
 *
 * @author rubens
 */
public class BuildBasic extends AbstractBasicAction implements IUnitCommand {

    boolean needUnit = false;
    String originalPieceGrammar;
    String originalPieceGrammarWord;

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, HashSet<String> usedCommands, HashMap<Long, String> counterByFunction) {
        //get the unit that it will be builded     	
        ConstructionTypeParam unitToBeBuilded = getUnitToBuild();
        if (unitToBeBuilded != null) {
            //verify if the limit of units are reached
            if (!limitReached(game, player, currentPlayerAction)) {
                //check if we have resources
                int resourcesUsed = getResourcesInCurrentAction(currentPlayerAction) + game.getResourceUsage().getResourcesUsed(player);        
                if ((game.getPlayer(player).getResources()- resourcesUsed) >= getResourceCost(unitToBeBuilded, a_utt)) {
                    //pick one work to build
                    Unit workToBuild = getWorkToBuild(player, game, currentPlayerAction, a_utt, pf);
                    if (workToBuild != null) {
                        //execute the build action
                        UnitAction unAcTemp = translateUnitAction(game, a_utt, workToBuild, currentPlayerAction, player, pf);
                        if (unAcTemp != null) {
                            usedCommands.add(getOriginalPieceGrammar());
                        	if(counterByFunction.containsKey(workToBuild.getID()))
                        	{
                        		if(!counterByFunction.get(workToBuild.getID()).equals("build"))
                        			counterByFunction.put(workToBuild.getID(), "build");
                        	}
                        	else
                        	{
                        		counterByFunction.put(workToBuild.getID(), "build");
                        	}
                            currentPlayerAction.addUnitAction(workToBuild, unAcTemp);
                        }

                    }
                }
            }
        }
        return currentPlayerAction;
    }

    private Unit getWorkToBuild(int player, GameState game, PlayerAction currentPlayerAction, UnitTypeTable a_utt, PathFinding pf) {
        for (Unit unit : game.getUnits()) {
        	//verify if the unit is free
            if (unit.getPlayer() == player && unit.getType() == a_utt.getUnitType("Worker")
                    && game.getActionAssignment(unit) == null && currentPlayerAction.getAction(unit) == null && translateUnitAction(game, a_utt, unit, currentPlayerAction, player, pf)!= null) {
            	return unit;
            }
        }

        return null;
    }
    
    protected boolean hasInPotentialUnitsWorkers(GameState game, PlayerAction currentPlayerAction, Unit uAlly, int player, UnitTypeTable a_utt, PathFinding pf) {
    	if(uAlly.getPlayer() == player && currentPlayerAction.getAction(uAlly) == null 
                && game.getActionAssignment(uAlly) == null && uAlly.getResources() == 0 && uAlly.getType() == a_utt.getUnitType("Worker") && translateUnitAction(game, a_utt, uAlly, currentPlayerAction, player, pf)!= null)
        {
    		
        	return true;
        }
        else
        {
        	return false;
        }
    }

    private UnitAction translateUnitAction(GameState game, UnitTypeTable a_utt, Unit unit, PlayerAction currentPlayerAction, int player, PathFinding pf) {
        List<Integer> reservedPositions = new LinkedList<>();
        reservedPositions.addAll(game.getResourceUsage().getPositionsUsed());
        reservedPositions.addAll(currentPlayerAction.getResourceUsage().getPositionsUsed());
        PhysicalGameState pgs = game.getPhysicalGameState();
        /*
        List<Unit> bases = new LinkedList<Unit>();
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType().name == "Base"
                    && u2.getPlayer() == player) {
                bases.add(u2);
            }
        }

        List<Unit> barracks = new LinkedList<Unit>();
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType().name == "Barracks"
                    && u2.getPlayer() == player) {
                bases.add(u2);
            }
        }

        
        this method returns the build to the old version.
        return buildWithRelativePosition(barracks, bases, player, reservedPositions, pgs, a_utt, unit, currentPlayerAction, game, pf);
         */
        return buildConsideringPosition(player, reservedPositions, pgs, a_utt, unit, currentPlayerAction, game, pf);
    }

    private UnitAction buildConsideringPosition(int player, List<Integer> reservedPositions, PhysicalGameState pgs, UnitTypeTable a_utt, Unit unit, PlayerAction currentPlayerAction, GameState game, PathFinding pf) {
        PriorityPositionParam order = getPriorityParam();
        UnitAction ua = null;
        //pick the type to be builded
        UnitType unitType = getUnitTyppe(a_utt);
        
        for (EnumPositionType enumPositionType : order.getSelectedPosition()) {
            if (enumPositionType.code() == 4) {
                for (int enumCodePosition : getDirectionByEnemy(game, unit)) {
                    ua = new UnitAction(UnitAction.TYPE_PRODUCE, enumCodePosition, unitType);
                    if (game.isUnitActionAllowed(unit, ua) && isPositionFree(game, ua, unit)) {
                    	return ua;
                    }
                }
            } else {
                ua = new UnitAction(UnitAction.TYPE_PRODUCE, enumPositionType.code(), unitType);
            }
            if (game.isUnitActionAllowed(unit, ua) && isPositionFree(game, ua, unit)) {
            	return ua;
            }
        }
        return null;
    }
    
    
    private List<Integer> getDirectionByEnemy(GameState game, Unit unit) {
        int player = unit.getPlayer();
        int enemy = (1 - player);
        ArrayList<Integer> directions = new ArrayList<>();

        //get (following the order) base, barrack or enemy.
        Unit enUnit = getOrderedUnit(enemy, game);
        //check if the enemy is left or right
        if (enUnit.getX() >= unit.getX()) {
            directions.add(DIRECTION_RIGHT);
        } else {
            directions.add(DIRECTION_LEFT);
        }
        //check if the enemy is up or bottom
        if (enUnit.getY() >= unit.getY()) {
            directions.add(DIRECTION_DOWN);
        } else {
            directions.add(DIRECTION_UP);
        }

        //return all possible positions
        return directions;
    }
    
    private Unit getOrderedUnit(int enemy, GameState game) {
        Unit base = null;
        Unit barrack = null;
        Unit other = null;

        for (Unit unit : game.getUnits()) {
            if (unit.getPlayer() == enemy) {
                if (base == null && unit.getType().ID == 1) {
                    base = unit;
                } else if (barrack == null && unit.getType().ID == 2) {
                    barrack = unit;
                } else if (other == null) {
                    other = unit;
                } else {
                    break;
                }
            }
        }

        if (base != null) {
            return base;
        } else if (barrack != null) {
            return barrack;
        }
        return other;
    }

    //old code
    private UnitAction buildWithRelativePosition(List<Unit> barracks, List<Unit> bases, int player, List<Integer> reservedPositions, PhysicalGameState pgs, UnitTypeTable a_utt, Unit unit, PlayerAction currentPlayerAction, GameState game, PathFinding pf) {
        int pos;

        if (barracks.isEmpty() && bases.size() > 0) {
            Unit b = bases.get(0);

            int xCoord = b.getX();
            int yCoord = b.getY();

            if (player == 0) {
                xCoord = b.getX() + 2;
                yCoord = b.getY() + 2;
            }

            pos = findBuildingPosition(reservedPositions, xCoord, yCoord, game.getPlayer(player), pgs);
        } else {
            pos = findBuildingPosition(reservedPositions, unit.getX(), unit.getY(), game.getPlayer(player), pgs);
        }

        //pick the type to be builded
        UnitType unType = getUnitTyppe(a_utt);

        AbstractAction buildAct = new Build(unit, unType, pos % pgs.getWidth(), pos / pgs.getWidth(), pf);
        ResourceUsage res = game.getResourceUsage();
        res.merge(currentPlayerAction.getResourceUsage());
        UnitAction unAct = buildAct.execute(game, res);
        if (unAct != null) {
            return unAct;
        }
        return null;
    }

    private boolean limitReached(GameState game, int player, PlayerAction currentPlayerAction) {
        IQuantity qtt = getQuantityFromParam();

        //verify if the quantity of units associated with the specific type were reached.
        if (qtt.getQuantity() > getQuantityUnitsBuilded(game, player, currentPlayerAction)) {
            return false;
        }
        return true;
    }

    private int getQuantityUnitsBuilded(GameState game, int player, PlayerAction currentPlayerAction) {
        int ret = 0;
        HashSet<EnumTypeUnits> types = new HashSet<>();
        //get types in EnumTypeUnits
        for (IParameters param : getParameters()) {
            if (param instanceof ConstructionTypeParam) {
                types.addAll(((ConstructionTypeParam) param).getParamTypes());
            }
        }
        //count
        for (EnumTypeUnits type : types) {
            ret += countUnitsByType(game, player, currentPlayerAction, type);
        }

        return ret;
    }

    private int countUnitsByType(GameState game, int player, PlayerAction currentPlayerAction, EnumTypeUnits type) {
        int qtt = 0;

        //count units in state
        for (Unit unit : game.getUnits()) {
            if (unit.getPlayer() == player && unit.getType().ID == type.code()) {
                qtt++;
            }
        }
        // count units in currentPlayerAction 
        for (Pair<Unit, UnitAction> action : currentPlayerAction.getActions()) {
            if (action.m_a.getType().ID == type.code()) {
                qtt++;
            }
        }

        return qtt;
    }

    private UnitAction trainUnitBasedInPriorityPosition(GameState game, Unit unit, UnitType unitType) {
        PriorityPositionParam order = getPriorityParam();
        UnitAction ua = null;
        for (EnumPositionType enumPositionType : order.getSelectedPosition()) {
            ua = new UnitAction(UnitAction.TYPE_PRODUCE, enumPositionType.code(), unitType);
            if (game.isUnitActionAllowed(unit, ua) && isPositionFree(game, ua, unit)) {
                return ua;
            }
        }

        return null;
    }

    private PriorityPositionParam getPriorityParam() {
        for (IParameters param : getParameters()) {
            if (param instanceof IPriorityPosition) {
                return (PriorityPositionParam) param;
            }
        }
        return null;
    }

    private boolean isPositionFree(GameState game, UnitAction ua, Unit trainUnit) {
        int x, y;
        x = trainUnit.getX();
        y = trainUnit.getY();
        //define direction
        switch (ua.getDirection()) {
            case DIRECTION_UP:
                y--;
                break;
            case DIRECTION_RIGHT:
                x++;
                break;
            case DIRECTION_DOWN:
                y++;
                break;
            case DIRECTION_LEFT:
                x--;
                break;
        }
        int width = game.getPhysicalGameState().getWidth();
        int height = game.getPhysicalGameState().getHeight();
        if( (x + y * width) >= (width * height) ){
            return false;
        }
        
        if((x + y * width) < 0){
            return false;
        }        
        
        if (game.free(x, y)) {
            return true;
        }

        return false;
    }

    private ConstructionTypeParam getUnitToBuild() {
        for (IParameters param : getParameters()) {
            if (param instanceof ConstructionTypeParam) {
                return (ConstructionTypeParam) param;
            }
        }
        return null;
    }

    private int getResourceCost(ConstructionTypeParam unitToBeBuilded, UnitTypeTable a_utt) {
        if (unitToBeBuilded.getParamTypes().get(0) == EnumTypeUnits.Base) {
            return a_utt.getUnitType("Base").cost;
        } else {
            return a_utt.getUnitType("Barracks").cost;
        }
    }

    public int findBuildingPosition(List<Integer> reserved, int desiredX, int desiredY, Player p, PhysicalGameState pgs) {

        boolean[][] free = pgs.getAllFree();
        int x, y;

        /*
        System.out.println("-" + desiredX + "," + desiredY + "-------------------");
        for(int i = 0;i<free[0].length;i++) {
            for(int j = 0;j<free.length;j++) {
                System.out.print(free[j][i] + "\t");
            }
            System.out.println("");
        }
         */
        for (int l = 1; l < Math.max(pgs.getHeight(), pgs.getWidth()); l++) {
            for (int side = 0; side < 4; side++) {
                switch (side) {
                    case 0://up
                        y = desiredY - l;
                        if (y < 0) {
                            continue;
                        }
                        for (int dx = -l; dx <= l; dx++) {
                            x = desiredX + dx;
                            if (x < 0 || x >= pgs.getWidth()) {
                                continue;
                            }
                            int pos = x + y * pgs.getWidth();
                            if (!reserved.contains(pos) && free[x][y]) {
                                return pos;
                            }
                        }
                        break;
                    case 1://right
                        x = desiredX + l;
                        if (x >= pgs.getWidth()) {
                            continue;
                        }
                        for (int dy = -l; dy <= l; dy++) {
                            y = desiredY + dy;
                            if (y < 0 || y >= pgs.getHeight()) {
                                continue;
                            }
                            int pos = x + y * pgs.getWidth();
                            if (!reserved.contains(pos) && free[x][y]) {
                                return pos;
                            }
                        }
                        break;
                    case 2://down
                        y = desiredY + l;
                        if (y >= pgs.getHeight()) {
                            continue;
                        }
                        for (int dx = -l; dx <= l; dx++) {
                            x = desiredX + dx;
                            if (x < 0 || x >= pgs.getWidth()) {
                                continue;
                            }
                            int pos = x + y * pgs.getWidth();
                            if (!reserved.contains(pos) && free[x][y]) {
                                return pos;
                            }
                        }
                        break;
                    case 3://left
                        x = desiredX - l;
                        if (x < 0) {
                            continue;
                        }
                        for (int dy = -l; dy <= l; dy++) {
                            y = desiredY + dy;
                            if (y < 0 || y >= pgs.getHeight()) {
                                continue;
                            }
                            int pos = x + y * pgs.getWidth();
                            if (!reserved.contains(pos) && free[x][y]) {
                                return pos;
                            }
                        }
                        break;
                }
            }
        }
        return -1;
    }

    private UnitType getUnitTyppe(UnitTypeTable a_utt) {
        ConstructionTypeParam unitToBeBuilded = getUnitToBuild();
        if (unitToBeBuilded.getParamTypes().get(0) == EnumTypeUnits.Base) {
            return a_utt.getUnitType("Base");
        } else {
            return a_utt.getUnitType("Barracks");
        }
    }

    @Override
    public String toString() {
        String listParam = "Params:{";
        for (IParameters parameter : getParameters()) {
            listParam += parameter.toString() + ",";
        }
        //remove the last comma.
        listParam = listParam.substring(0, listParam.lastIndexOf(","));
        listParam += "}";

        return "{BuildBasic:{" + listParam + "}}";
    }

    public void setUnitIsNecessary() {
        this.needUnit = true;
    }

    public void setUnitIsNotNecessary() {
        this.needUnit = false;
    }

    @Override
    public Boolean isNecessaryUnit() {
        return needUnit;
    }

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, Unit workToBuild, HashSet<String> usedCommands, HashMap<Long, String> counterByFunction) {
        //usedCommands.add(getOriginalPieceGrammar()+")");
        if (workToBuild.getPlayer() != player) {
            return currentPlayerAction;
        }
        //get the unit that it will be builded 
        ConstructionTypeParam unitToBeBuilded = getUnitToBuild();
        if (unitToBeBuilded != null) {
            //verify if the limit of units are reached
            if (!limitReached(game, player, currentPlayerAction)) {
                //check if we have resources
                if (game.getPlayer(player).getResources() >= getResourceCost(unitToBeBuilded, a_utt)) {
                    //pick one work to build
                    //Unit workToBuild = getWorkToBuild(player, game, currentPlayerAction, a_utt);
                    if (workToBuild != null && hasInPotentialUnitsWorkers(game, currentPlayerAction, workToBuild, player,a_utt,pf)) {
                        //execute the build action                    	
                        UnitAction unAcTemp = translateUnitAction(game, a_utt, workToBuild, currentPlayerAction, player, pf);
                        if (unAcTemp != null) {
                            usedCommands.add(getOriginalPieceGrammar());
                        	if(counterByFunction.containsKey(workToBuild.getID()))
                        	{
                        		if(!counterByFunction.get(workToBuild.getID()).equals("build"))
                        			counterByFunction.put(workToBuild.getID(), "build");
                        	}
                        	else
                        	{
                        		counterByFunction.put(workToBuild.getID(), "build");
                        	}
                            currentPlayerAction.addUnitAction(workToBuild, unAcTemp);
                            
                        }

                    }
                }
            }
        }
        return currentPlayerAction;
    }

    /**
     * @return the originalPieceGrammar
     */
    public String getOriginalPieceGrammar() {
        return originalPieceGrammar;
    }

    /**
     * @param originalPieceGrammar the originalPieceGrammar to set
     */
    public void setOriginalPieceGrammar(String originalPieceGrammar) {
        this.originalPieceGrammar = originalPieceGrammar;
    }
    
	public String getOriginalPieceGrammarWord() {
		return originalPieceGrammarWord;
	}

	public void setOriginalPieceGrammarWord(String originalPieceGrammarWord) {
		this.originalPieceGrammarWord = originalPieceGrammarWord;
	}

}
