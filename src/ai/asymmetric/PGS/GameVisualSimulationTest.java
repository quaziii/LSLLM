/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.asymmetric.PGS;

import tests.*;
import PVAI.EconomyRush;
import PVAI.EconomyRushBurster;
import PVAI.WorkerDefense;
import PVAI.RangedDefense;
import PVAI.util.Permutation;
import Standard.CombinedEvaluation;
import Standard.StrategyTactics;
import ai.core.AI;
import ai.*;
import ai.CMAB.CMABBuilder;
import ai.CMAB.CmabNaiveMCTS;
import ai.abstraction.HeavyRush;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.combat.Cluster;
import ai.abstraction.combat.KitterDPS;
import ai.abstraction.combat.NOKDPS;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.partialobservability.PORangedRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.aiSelection.AlphaBetaSearch.AlphaBetaSearch;
import ai.asymmetric.GAB.GAB_oldVersion;
import ai.asymmetric.GAB.SandBox.AlphaBetaSearchAbstract;
import ai.asymmetric.GAB.SandBox.GAB;
import ai.asymmetric.GAB.SandBox.GABScriptChoose;
import ai.asymmetric.GAB.SandBox.GAB_SandBox_Parcial_State;
import ai.asymmetric.GAB.SandBox.LightPGSLimit;
import ai.asymmetric.PGS.SandBox.PGSmRTS_Paralel_JulianTest;
import ai.asymmetric.PGS.SandBox.PGSmRTS_Paralel_SandBox;
import ai.asymmetric.PGS.SandBox.PGSmRTS_SandBox;
import ai.asymmetric.SAB.SAB;
import ai.asymmetric.SAB.SABScriptChoose;
import ai.asymmetric.SAB.SAB_seed;
import ai.asymmetric.SSS.LightSSS;
import ai.asymmetric.SSS.NSSS;
import ai.asymmetric.SSS.NSSSLimit;
import ai.asymmetric.SSS.NSSSRandom;
import ai.asymmetric.SSS.SSSIterationRandom;
import ai.asymmetric.SSS.SSSResponseMRTS;
import ai.asymmetric.SSS.SSSResponseMRTSRandom;
import ai.asymmetric.SSS.SSSmRTS;
import ai.asymmetric.SSS.SSSmRTSScriptChoice;
import ai.asymmetric.SSS.SSSmRTSScriptChoiceRandom;
import ai.asymmetric.SSSDavid.SSSDavid;
import ai.cluster.CABA;
import ai.cluster.CABA_Enemy;
import ai.cluster.CIA;
import ai.cluster.CIA_Enemy;
import ai.cluster.CIA_EnemyEuclidieanInfluence;
import ai.cluster.CIA_EnemyWithTime;
import ai.cluster.CIA_PlayoutCluster;
import ai.cluster.CIA_PlayoutPower;
import ai.cluster.CIA_PlayoutTemporal;
import ai.cluster.CIA_TDLearning;
import ai.competition.IzanagiBot.Izanagi;
import ai.competition.capivara.Capivara;
import ai.competition.capivara.CmabAssymetricMCTS;
import ai.competition.dropletGNS.Droplet;
import ai.competition.tiamat.Tiamat;
import ai.competition.tiamat.mixedBotmRTS.MixedBot;
import ai.configurablescript.BasicExpandedConfigurableScript;
import ai.configurablescript.ScriptsCreator;
import ai.evaluation.EvaluationFunctionForwarding;
import ai.evaluation.LTD2;
import ai.evaluation.LanchesterEvaluationFunction;
import ai.evaluation.PlayoutFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction2;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.minimax.ABCD.IDABCD;
import ai.portfolio.PortfolioAI;
import ai.puppet.PuppetSearchMCTS;
import ai.scv.SCVPlus;
import gui.PhysicalGameStatePanel;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitTYpeTableBattle;
import rts.units.UnitTypeTable;
import static tests.ClusterTesteLeve.decodeScripts;
import static util.SOA.RoundRobinClusterLeve.decodeScripts;
import static util.SOA.RoundRobinTOWRDominance.decodeScripts;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class GameVisualSimulationTest {

    static String _nameStrategies = "", _enemy = "";
    static AI[] strategies = null;

    public static void main(String args[]) throws Exception {
        UnitTypeTable utt = new UnitTypeTable();
        //UnitTypeTable utt = new UnitTYpeTableBattle();
        //PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16.xml", utt);
        //PhysicalGameState pgs = PhysicalGameState.load("maps/8x8/basesWorkers8x8A.xml", utt);
        //PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16A.xml", utt);        
        //PhysicalGameState pgs = PhysicalGameState.load("maps/BWDistantResources32x32.xml", utt);
        //PhysicalGameState pgs = PhysicalGameState.load("maps/32x32/basesWorkers32x32A.xml", utt);
        //PhysicalGameState pgs = PhysicalGameState.load("maps/24x24/basesWorkers24x24A.xml", utt);
        //PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)BloodBath.scmB.xml", utt);
        //PhysicalGameState pgs = PhysicalGameState.load("maps/8x8/FourBasesWorkers8x8.xml", utt);
        PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/TwoBasesBarracks16x16.xml", utt);
        //PhysicalGameState pgs = PhysicalGameState.load("maps/NoWhereToRun9x8.xml", utt);
        //PhysicalGameState pgs = PhysicalGameState.load("maps/DoubleGame24x24.xml", utt);
        //PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();
        //testes 
        //PhysicalGameState pgs = PhysicalGameState.load("maps/24x24/basesWorkers24x24A.xml", utt);
        //PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)EmpireoftheSun.scmC.xml", utt);

        GameState gs = new GameState(pgs, utt);
        int MAXCYCLES = 8000;
        int PERIOD = 20;
        boolean gameover = false;

        //AI ai1 = new RangedRush(utt);
        AI ai1 = new LightRush(utt);
        //AI ai1 = new HeavyRush(utt);
        //AI ai1 = new PassiveAI();
        //AI ai1 = new POLightRush(utt);
        //AI ai1 = new EconomyRush(utt);        
        //AI ai1 = new RangedDefense(utt);
        //AI ai1 = new EconomyRushBurster(utt);        
        //AI ai1 = new PassiveAI(utt);
        //AI ai1 = new NaiveMCTS(utt);
        //AI ai1 = new PortfolioAI(utt);
        //AI ai1 = new PVAI(utt);
        //AI ai1 = new WorkerRushPlusPlus(utt);
        //AI ai1 = new RandomBiasedAI(utt);
        //AI ai1 = new PuppetSearchMCTS(utt);
        //AI ai1 = new PortfolioAI(utt);
        //AI ai1 = new POLightRush(utt);
        //AI ai1 = new BasicExpandedConfigurableScript(utt, new AStarPathFinding(), 18,0,0,1,2,2,-1,-1,3); //WR
        //AI ai1 = new PGSSCriptChoice(utt, decodeScripts2(utt, "0;1;;"), "PGSRSym");
        //AI ai1 = new CmabNaiveMCTS(100, -1, 200, 1, 0.3f, 0.0f, 0.4f, 0, new RandomBiasedAI(utt), new SimpleSqrtEvaluationFunction3(),
        //                          true, "CmabCombinatorialGenerator", utt, decodeScriptsFull(utt, "0;"), "A1N_W");
        //AI ai1 = new SABScriptChoose(utt, 2, 2, decodeScriptsFull(utt, "0;"), "GAB_W");
        //AI ai1 = new WorkerRush(utt);
        //AI ai1 = new PGSmRTS_SandBox(utt);
        //AI ai1 = new PGSmRTS(utt); 
        //AI ai1 = new GAB(utt);
        //AI ai1 = new SAB(utt);
        //AI ai1 = new IDABCD(utt);
        //AI ai1 = new StrategyTactics(utt);
        //AI ai1 = new PGSSCriptChoice(utt, decodeScripts(utt, "65;184;217;"), "bGA");
        //AI ai1 = new SSSmRTS(utt);
        //AI ai1 = new AlphaBetaSearch(utt, new LTD2(), "LTD2");
        //AI ai1 = new AlphaBetaSearch(utt, new PlayoutFunction(new RandomBiasedAI(utt), new RandomBiasedAI(utt), new LTD2()), "Play_Rand_LTD2");
        //AI ai1 = new AlphaBetaSearch(utt, new PlayoutFunction(new KitterDPS(utt), new KitterDPS(utt), new LTD2()), "Play_KitterDPS_LTD2");
        //AI ai1 = new AlphaBetaSearch(utt, new PlayoutFunction(new NOKDPS(utt), new NOKDPS(utt), new LTD2()), "Play_NOKDPS_LTD2");
        //AI ai1 = new AlphaBetaSearch(utt, new PlayoutFunction(new POLightRush(utt), new POLightRush(utt), new LTD2()), "Play_POLightRush_LTD2");        
        //AI ai1 = new CmabNaiveMCTS(utt);
        //AI ai1 = new PGSmRTS_Paralel_JulianTest(utt);
        //AI ai1 = new PGSmRTSRandom(utt, 4, 200);
        //AI ai2 = new PGSSCriptChoiceRandom(utt, decodeScripts2(utt, "0;"), "PGSRSym",2,200);
        //AI ai1 = new PGSResponseMRTS(utt);
        //AI ai1 = new NGSRandom(utt);
        //AI ai1 = new NSSSRandom(utt);
        //AI ai1 = new NGS(utt);
        //AI ai1 = new NGSLimit(utt);
        //AI ai1 = new NSSS(utt);
        //AI ai1 = new NSSSLimit(utt);
        //AI ai1 = new SSSmRTSScriptChoiceRandom(utt, decodeScripts(utt, "46;141;273;195;"), "GA_PGSRLim",4,200);
        //AI ai1 = new StrategyTactics(utt);
        //AI ai1 = new WorkerRush(utt);
        //AI ai1 = new GABScriptChoose(utt, 150, 6, 6, // MoreLife
        //                decodeScriptsFull(utt, "0;1;2;3;"), "GAB");
        //AI ai1 = new Izanagi(utt);
        //AI ai1 = new PGSmRTS_Paralel_JulianTest(utt);
        //AI ai1 = new PGSmRTS_Paralel_SandBox(utt);
        //AI ai1 = new SSSDavid(utt, 5);
        //AI ai1 = new Droplet(utt);
        //AI ai1 = new MixedBot(utt);
        AI ai2 = new botEmptyBase(utt, "train(Worker,1,Left) for(u) (if(HaveQtdEnemiesbyType(Worker,2,u)) (train(Light,20,EnemyDir,u))) harvest(50) for(u) (attack(Light,closest,u))", "teste");
        //AI ai2 = new CmabAssymetricMCTS(100, -1, 50, 2, 0.3f, 0.0f, 0.4f, 0, new RandomBiasedAI(utt),
        //                new SimpleSqrtEvaluationFunction3(), true, utt, "ManagerClosestEnemy", 1,
        //                decodeScriptsFull(utt, "0;1;"), "A3N");
        
        //AI ai2 = new CmabAssymetricMCTS(100, -1, 100, 1, 0.3f, 
        //                                     0.0f, 0.4f, 0, new RandomBiasedAI(utt), 
        //                                     new SimpleSqrtEvaluationFunction3(), true, utt, 
        //                                    "ManagerClosestEnemy", 0, decodeScripts2(utt, "0;1;2;3;")); //A3N
        //AI ai2 = new BasicExpandedConfigurableScript(utt, new AStarPathFinding(), 18,0,0,1,2,2,-1,-1,3); //WR
        //AI ai2 = new LightPGS(utt, decodeScripts2(utt, "0;1;2;3;"), "LightPGS",2,200);
        //AI ai2 = new LightSSS(utt, decodeScripts2(utt, "0;1;2;3;"), "LightSSS",2,200);
        //AI ai2 = new LightPGSLimit(utt);
        //AI ai2 = new GABScriptChoose(utt, 100, 1, 1, //closest
        //                decodeScripts2(utt, "0;1;3;"), "GAB");
        //AI ai2 = new SABScriptChoose(utt, 200, 1, 1, //closest
        //        decodeScripts2(utt, "0;1;2;3;"), "SAB");

        //AI ai2 = new GABScriptChoose(utt, 2, 2, decodeScripts2(utt, "0;1;2;3;"), "GAB");
        //AI ai2 = new SABScriptChoose(utt, 2, 2, decodeScripts2(utt, "0;1;2;3;"), "GAB");
        //AI ai2 = new GABScriptChoose(utt, 2, 2, decodeScripts2(utt, "0;1;2;3;"), decodeScripts2(utt, "1;"), "GAB");
        // AI ai2 = new SABScriptChoose(utt, 2, 2, decodeScripts2(utt, "0;1;2;3;"), decodeScripts2(utt, "1;"), "SAB");
        //AI ai2 = new GAB(utt);
        //AI ai2 = new SAB(utt);
        //AI ai1 = new Tiamat(utt);
        //AI ai2 = new Capivara(utt);
        //AI ai2 = new SCVPlus(utt);
        //AI ai2 = new SCVPlus(utt, pgs.getHeight(), pgs.getWidth());
        //AI ai2 = new CMABBuilder(100, -1, 100, 1, 0, new RandomBiasedAI(utt), new SimpleSqrtEvaluationFunction3(), 0, utt, new ArrayList<>(), "CmabCombinatorialGenerator", "ManagerClosestEnemy", 1);
        //AI ai1 = new StrategyTactics(utt);
        //AI ai2 = new SSSmRTS(utt);
        //AI ai2 = new PGSIterationRandom(utt); 
        //AI ai2 = new SSSIterationRandom(utt); 
        //AI ai1 = new SSSResponseMRTS(utt);
        //AI ai2 = new NaiveMCTS(utt);
        //AI ai2 = new CIA_TDLearning(utt);
        //AI ai2 = new CIA_PlayoutTemporal(utt);
        //AI ai2 = new CIA_PlayoutPower(utt);
        //AI ai2 = new CIA_PlayoutCluster(utt);
        //AI ai1 = new AlphaBetaSearch(utt);
        //AI ai2 = new CABA(utt);
        //AI ai2 = new CABA_Enemy(utt);
        //AI ai2 = new CIA_Enemy(utt);
        //AI ai2 = new CIA(utt);
        //AI ai2 = new CIA_EnemyEuclidieanInfluence(utt);
        //AI ai2 = new CIA_EnemyWithTime(utt);
        //AI ai1 = new PassiveAI();
        //AI ai2 = new SAB(utt);
        //AI ai2 = new SAB_seed(utt);
        //AI ai2 = new Cluster(utt);
        //AI ai2 = new KitterDPS(utt);
        //AI ai1 = new NOKDPS(utt);
        //AI ai2 = new GAB(utt);
        //AI ai2 = new GAB(utt);
        //AI ai2 = new AlphaBetaSearchAbstract(utt);
        //AI ai2 = new GAB_SandBox_Parcial_State(utt);
        //AI ai2 = new GAB(utt);
        //AI ai2 = new WorkerRush(utt);
        //AI ai2 = new PuppetSearchMCTS(utt);
        //AI ai2 = new POLightRush(utt);
        //AI ai2 = new RangedDefense(utt);
        //AI ai2 = new PVAI(utt);
        //AI ai2 = new PVAIML_onlyEnemy(utt);
        //AI ai2 = new PVAIML_SLMS(utt);
        //AI ai2 = new PVAIML_NaiveMS(utt);
        //AI ai2 = new PVAIML_ED(utt);
        //AI ai2 = new PVAIML_SLFW(utt);
        //AI ai2 = new PVAIML_SL(utt);
        //AI ai2 = new PVAIML_Naive(utt);
        //AI ai2 = new PVAIML_NaiveFW(utt);
        //AI ai2 = new PVAIML_FW(utt);
        //AI ai2 = new PVAIML_EDP(utt);
        //AI ai2 = new PVAIML_SLFWMS(utt);
        //AI ai2 = new PVAICluster(4, utt, "EconomyRush(AStarPathFinding)");
        System.out.println("---------AI's---------");
        System.out.println("AI 1 = " + ai1.toString());
        System.out.println("AI 2 = " + ai2.toString() + "\n");

        //método para fazer a troca dos players
        JFrame w = PhysicalGameStatePanel.newVisualizer(gs, 720, 720, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
        //JFrame w = PhysicalGameStatePanel.newVisualizer(gs, 720, 720, false, PhysicalGameStatePanel.COLORSCHEME_WHITE); 
        long startTime = System.currentTimeMillis();
        long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
        do {
            if (System.currentTimeMillis() >= nextTimeToUpdate) {
                startTime = System.currentTimeMillis();
                PlayerAction pa1 = ai1.getAction(0, gs);
                //if ((System.currentTimeMillis() - startTime) > 0) {
                //    System.out.println("Tempo de execução P1=" + (startTime = System.currentTimeMillis() - startTime));
                //}
                //System.out.println("Action A1 ="+ pa1.toString());

                startTime = System.currentTimeMillis();
                PlayerAction pa2 = ai2.getAction(1, gs);
                if ((System.currentTimeMillis() - startTime) > 100) {
                    System.out.println("Tempo de execução P2=" + (startTime = System.currentTimeMillis() - startTime));
                }
                //System.out.println("Action A2 ="+ pa2.toString());

                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

                // simulate:
                gameover = gs.cycle();
                w.repaint();
                nextTimeToUpdate += PERIOD;
            } else {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            /* PhysicalGameState physical = gs.getPhysicalGameState();
            System.out.println("---------TIME---------");
            System.out.println(gs.getTime());
            for (Unit u : physical.getUnits()) {
                if (u.getPlayer() == 1) {
                    System.out.println("Player 1: Unity - " + u.toString());
                }
                else if (u.getPlayer() == 0) {
                     System.out.println("Player 0: Unity - " + u.toString());
                } 
            }
             */
        } while (!gameover && gs.getTime() < MAXCYCLES);

        System.out.println("Winner " + Integer.toString(gs.winner()));
        System.out.println("Game Over");
    }

    public static List<AI> decodeScripts2(UnitTypeTable utt, String sScripts) {

        //decompõe a tupla
        ArrayList<Integer> iScriptsAi1 = new ArrayList<>();
        String[] itens = sScripts.split(";");

        for (String element : itens) {
            iScriptsAi1.add(Integer.decode(element));
        }

        List<AI> scriptsAI = new ArrayList<>();

        ScriptsCreator sc = new ScriptsCreator(utt, 300);
        ArrayList<BasicExpandedConfigurableScript> scriptsCompleteSet = sc.getScriptsMixReducedSet();

        iScriptsAi1.forEach((idSc) -> {
            scriptsAI.add(scriptsCompleteSet.get(idSc));
        });

        return scriptsAI;
    }

    public static List<AI> decodeScripts(UnitTypeTable utt, ArrayList<Integer> iScripts) {
        List<AI> scriptsAI = new ArrayList<>();

        ScriptsCreator sc = new ScriptsCreator(utt, 300);
        ArrayList<BasicExpandedConfigurableScript> scriptsCompleteSet = sc.getScriptsMixReducedSet();

        BasicExpandedConfigurableScript[] AIs = new BasicExpandedConfigurableScript[10];
        AIs[0] = scriptsCompleteSet.get(0);
        AIs[1] = scriptsCompleteSet.get(1);
        AIs[2] = scriptsCompleteSet.get(2);
        AIs[3] = scriptsCompleteSet.get(3);
        AIs[4] = scriptsCompleteSet.get(4);
        AIs[5] = scriptsCompleteSet.get(7);
        AIs[6] = scriptsCompleteSet.get(8);
        AIs[7] = scriptsCompleteSet.get(100);
        AIs[8] = scriptsCompleteSet.get(101);
        AIs[9] = scriptsCompleteSet.get(102);

        for (Integer idSc : iScripts) {
            scriptsAI.add(AIs[idSc]);
        }

        return scriptsAI;
    }

    public static List<AI> decodeScriptsFull(UnitTypeTable utt, String sScripts) {

        //decompõe a tupla
        ArrayList<Integer> iScriptsAi1 = new ArrayList<>();
        String[] itens = sScripts.split(";");

        for (String element : itens) {
            iScriptsAi1.add(Integer.decode(element));
        }

        List<AI> scriptsAI = new ArrayList<>();

        ScriptsCreator sc = new ScriptsCreator(utt, 300);
        ArrayList<BasicExpandedConfigurableScript> scriptsCompleteSet = sc.getScriptsMixReducedSet();

        iScriptsAi1.forEach((idSc) -> {
            scriptsAI.add(scriptsCompleteSet.get(idSc));
        });

        return scriptsAI;
    }

}
