/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.ScriptsGenerator.Command.BasicBoolean;

import ai.ScriptsGenerator.Command.AbstractBasicAction;
import ai.ScriptsGenerator.Command.AbstractBooleanAction;
import ai.ScriptsGenerator.Command.BasicAction.AttackBasic;
import ai.ScriptsGenerator.Command.Enumerators.EnumTypeUnits;
import ai.ScriptsGenerator.CommandInterfaces.ICommand;
import ai.ScriptsGenerator.IParameters.IBehavior;
import ai.ScriptsGenerator.IParameters.IParameters;
import ai.ScriptsGenerator.ParametersConcrete.ClosestEnemy;
import ai.ScriptsGenerator.ParametersConcrete.PlayerTargetParam;
import ai.ScriptsGenerator.ParametersConcrete.TypeConcrete;
import ai.ScriptsGenerator.ParametersConcrete.UnitTypeParam;
import ai.abstraction.AbstractAction;
import ai.abstraction.Attack;
import ai.abstraction.pathfinding.PathFinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens Julian This condition evaluates if some enemy unit is in the
 * attack range of an ally unit
 */
public class AllyRange extends AbstractBooleanAction {

    public AllyRange(List<ICommand> commandsBoolean) {
        this.commandsBoolean = commandsBoolean;
    }

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, HashSet<String> usedCommands, HashMap<Long, String> counterByFunction) {
        utt = a_utt;
        ResourceUsage resources = new ResourceUsage();
        PhysicalGameState pgs = game.getPhysicalGameState();
        ArrayList<Unit> unitstoApplyWait = new ArrayList<>();
        //update variable resources
        resources = getResourcesUsed(currentPlayerAction, pgs);

        //now we iterate for all ally units in order to discover wich one satisfy the condition
        for (Unit unAlly : getPotentialUnits(game, currentPlayerAction, player)) {
            boolean applyWait = true;
            if (currentPlayerAction.getAction(unAlly) == null) {

                for (Unit u2 : pgs.getUnits()) {

                    if (u2.getPlayer() >= 0 && u2.getPlayer() != player) {

                        int dx = u2.getX() - unAlly.getX();
                        int dy = u2.getY() - unAlly.getY();
                        double d = Math.sqrt(dx * dx + dy * dy);

                        //If satisfies, an action is applied to that unit. Units that not satisfies will be set with
                        // an action wait.
                        if ((d <= unAlly.getAttackRange())) {

                            applyWait = false;
                        }
                    }

                }
                if (applyWait) {
                    unitstoApplyWait.add(unAlly);
                }
            }
        }
        //here we set with wait the units that dont satisfy the condition
        temporalWaitActions(game, player, unitstoApplyWait, currentPlayerAction);
        //here we apply the action just over the units that satisfy the condition
        currentPlayerAction = appendCommands(player, game, currentPlayerAction,usedCommands,counterByFunction);
        //here we remove the wait action f the other units and the flow continues
        restoreOriginalActions(game, player, unitstoApplyWait, currentPlayerAction);
        return currentPlayerAction;
    }

    @Override
    public String toString() {
        String listParam = "Params:{";
        for (IParameters parameter : getParameters()) {
            listParam += parameter.toString() + ",";
        }
        listParam += "Actions:{";

        for (ICommand command : commandsBoolean) {
            listParam += command.toString();
        }
        //remove the last comma.
//        listParam = listParam.substring(0, listParam.lastIndexOf(","));
//        listParam += "}";

        return "{AllyRange:{" + listParam + "}}";
    }

}
