/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util.SOA;

import Standard.StrategyTactics;
import ai.CMAB.CMABBuilder;
import ai.ScriptsGenerator.ChromosomeAI;
import ai.ScriptsGenerator.CommandInterfaces.ICommand;
import ai.ScriptsGenerator.TableGenerator.TableCommandsGenerator;
import ai.RandomBiasedAI;
import ai.abstraction.LightRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.combat.KitterDPS;
import ai.abstraction.combat.NOKDPS;
import ai.abstraction.partialobservability.POHeavyRush;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.partialobservability.PORangedRush;
import ai.abstraction.partialobservability.POWorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.ahtn.AHTNAI;
import ai.aiSelection.AlphaBetaSearch.AlphaBetaSearch;
import ai.asymmetric.PGS.NGS;
import ai.core.AI;
import ai.asymmetric.PGS.PGSSCriptChoice;
import ai.asymmetric.PGS.PGSSCriptChoiceRandom;
import ai.asymmetric.PGS.PGSmRTS;
import ai.asymmetric.SSS.SSSmRTS;
import ai.asymmetric.SSS.SSSmRTSScriptChoice;
import ai.asymmetric.SSS.SSSmRTSScriptChoiceRandom;
import ai.cluster.CABA;
import ai.cluster.CABA_Enemy;
import ai.cluster.CABA_TDLearning;
import ai.cluster.CIA_Enemy;
import ai.cluster.CIA_PlayoutTemporal;
import ai.cluster.CIA_TDLearning;
import ai.competition.capivara.Capivara;
import ai.competition.capivara.CmabAssymetricMCTS;
import ai.competition.tiamat.Tiamat;
import ai.configurablescript.BasicExpandedConfigurableScript;
import ai.configurablescript.ScriptsCreator;
import ai.evaluation.LTD2;
import ai.evaluation.PlayoutFunction;
import ai.evaluation.SimpleEvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.mcts.believestatemcts.BS3_NaiveMCTS;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.mcts.naivemcts.NaiveMCTSNoGamma;
import ai.mcts.uct.UCT;
import ai.minimax.ABCD.ABCD;
import ai.montecarlo.MonteCarlo;
import ai.montecarlo.lsi.LSI;
import ai.puppet.PuppetSearchMCTS;
import ai.scv.SCV;
import ai.scv.SCVPlus;
import gui.PhysicalGameStatePanel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTYpeTableBattle;
import rts.units.UnitTypeTable;
import static tests.ClusterTesteLeve.decodeScripts;

/**
 *
 * @author rubens Classe responsável por rodar os confrontos entre duas IA's.
 * Ambiente totalmente observável.
 */
public class RoundRobinClusterLeve_Cluster_SG {

    static String _nameStrategies = "", _enemy = "";
    static AI[] strategies = null;
    private HashMap<BigDecimal, ArrayList<Integer>> scriptsTable;
    String pathTableScripts;

    public RoundRobinClusterLeve_Cluster_SG(String pathTableScripts) {
        this.pathTableScripts = pathTableScripts;
        buildScriptsTable();

    }

    public boolean run(String sIA1, String sIA2, String sMap, String sIte, String pathLog) throws Exception {
        int iAi1 = Integer.parseInt(sIA1);
        int iAi2 = Integer.parseInt(sIA2);
        int map = Integer.parseInt(sMap);

        ArrayList<String> log = new ArrayList<>();
        //controle de tempo
        Instant timeInicial = Instant.now();
        Duration duracao;

        List<String> maps = new ArrayList<>(Arrays.asList(
                //"maps/24x24/basesWorkers24x24A.xml",
                //"maps/DoubleGame24x24.xml",
                //"maps/32x32/basesWorkers32x32A.xml"
                //"maps/BWDistantResources32x32.xml"
                //"maps/BroodWar/(4)BloodBath.scmB.xml"
                //"maps/8x8/basesWorkers8x8A.xml"
                //"maps/16x16/BasesWithWalls16x16.xml"
                "maps/16x16/basesWorkers16x16A.xml"
        //"maps/NoWhereToRun9x8.xml"
        ));

        //UnitTypeTable utt = new UnitTYpeTableBattle();
        UnitTypeTable utt = new UnitTypeTable();
        PhysicalGameState pgs = PhysicalGameState.load(maps.get(map), utt);

        GameState gs = new GameState(pgs, utt);
        int MAXCYCLES = 20000;
        int PERIOD = 20;
        boolean gameover = false;

        if (pgs.getHeight() == 8) {
            MAXCYCLES = 4000;
        }
        if (pgs.getHeight() == 16) {
            MAXCYCLES = 5000;
        }
        if (pgs.getHeight() == 24) {
            MAXCYCLES = 6000;
        }
        if (pgs.getHeight() == 32) {
            MAXCYCLES = 7000;
        }
        if (pgs.getHeight() == 64) {
            MAXCYCLES = 12000;
        }
        String bestGAAAAI = "198;272;100;168;78;86;27;120;279;93;";
        List<AI> ais;
        ais = new ArrayList<>(Arrays.asList(
                new AHTNAI(utt),
                new NaiveMCTS(utt),
                new PuppetSearchMCTS(utt),
                new StrategyTactics(utt),
                new PGSSCriptChoice(utt, decodeScripts(utt, "0;1;2;3;"), "PGS"),
                new SSSmRTSScriptChoice(utt, decodeScripts(utt, "0;1;2;3;"), "SSS"),
                new BasicExpandedConfigurableScript(utt, new AStarPathFinding(), 18, 0, 0, 1, 2, 2, -1, -1, 4), //lr
                new BasicExpandedConfigurableScript(utt, new AStarPathFinding(), 18, 0, 0, 1, 2, 2, -1, -1, 5), //HR
                new BasicExpandedConfigurableScript(utt, new AStarPathFinding(), 18, 0, 0, 1, 2, 2, -1, -1, 6), //RR
                new BasicExpandedConfigurableScript(utt, new AStarPathFinding(), 18, 0, 0, 1, 2, 2, -1, -1, 3), //WR
                //new SCVPlus(utt),
                //new CmabAssymetricMCTS(100, -1, 100, 1, 0.3F, 0.0F, 0.4F, 0, new RandomBiasedAI(utt),
                //        new SimpleSqrtEvaluationFunction3(), true, utt, "ManagerClosestEnemy", 1,
                //        decodeScripts(utt, bestGAAAAI), "A3N_GA-3AI"), //12
                /*
                new CmabAssymetricMCTS(100, -1, 100, 1, 0.3F, 0.0F, 0.4F, 0, new RandomBiasedAI(utt),
                        new SimpleSqrtEvaluationFunction3(), true, utt, "ManagerClosestEnemy", 1,
                        decodeScripts2(utt, new ArrayList<>(getListIfInteger("15187;15781;15914;14726;11902;15767;14569;14658;"))), "GA-run2-400") /*,
                
                 */
                //bg1
                //new PGSSCriptChoiceRandom(utt, decodeScripts(utt, GA_PGS), "GA_PGS",2,200),
                //new SSSmRTSScriptChoiceRandom(utt, decodeScripts(utt, GA_SSS), "GA_SSS",2,200),
                //plus
                //new PGSSCriptChoiceRandom(utt, decodeScripts(utt, "0;1;2;3;100;101;102;103;299;"), "PGS+",2,200),
                //new SSSmRTSScriptChoiceRandom(utt, decodeScripts(utt, "0;1;2;3;100;101;102;103;299;"), "SSS+",2,200),
                //new CmabAssymetricMCTS(100, -1, 100, 1, 0.3f, 0.0f, 0.4f, 0, new RandomBiasedAI(utt), new SimpleSqrtEvaluationFunction3(), true, utt, "ManagerClosestEnemy", 1,decodeScripts(utt, GA_A3N)),
                /*
                new PGSSCriptChoice(utt, decodeScripts2(utt, new ArrayList<>(getListIfInteger("8756;6292;7903;7486;8265;9031;8953;"))), "PGS_GA"),
                new SSSmRTSScriptChoice(utt, decodeScripts2(utt, new ArrayList<>(getListIfInteger("8756;6292;7903;7486;8265;9031;8953;"))), "SSS_GA"),
                new CmabAssymetricMCTS(100, -1, 100, 1, 0.3F, 0.0F, 0.4F, 0, new RandomBiasedAI(utt),
                        new SimpleSqrtEvaluationFunction3(), true, utt, "ManagerClosestEnemy", 1,
                        decodeScripts2(utt, new ArrayList<>(getListIfInteger("8756;6292;7903;7486;8265;9031;8953;"))), "GA-225")
                 */
                new CmabAssymetricMCTS(100, -1, 100, 1, 0.3F, 0.0F, 0.4F, 0, new RandomBiasedAI(utt),
                        new SimpleSqrtEvaluationFunction3(), true, utt, "ManagerClosestEnemy", 1,
                        decodeScripts2(utt, new ArrayList<>(getListIfInteger("429;1349;1632;"))), "GA-21")
        ));

        AI ai1 = ais.get(iAi1);
        AI ai2 = ais.get(iAi2);

        /*
            Variáveis para coleta de tempo
         */
        double ai1TempoMin = 9999, ai1TempoMax = -9999;
        double ai2TempoMin = 9999, ai2TempoMax = -9999;
        double sumAi1 = 0, sumAi2 = 0;
        int totalAction = 0;

        log.add("---------AIs---------");
        log.add("AI 1 = " + ai1.toString());
        log.add("AI 2 = " + ai2.toString() + "\n");

        log.add("---------Mapa---------");
        log.add("Mapa= " + maps.get(map) + "\n");

        //método para fazer a troca dos players
        //JFrame w = PhysicalGameStatePanel.newVisualizer(gs, 840, 840, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
//        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);
        long startTime;
        long timeTemp;
        //System.out.println("Tempo de execução P2="+(startTime = System.currentTimeMillis() - startTime));
        long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
        do {
            if (System.currentTimeMillis() >= nextTimeToUpdate) {
                totalAction++;
                startTime = System.currentTimeMillis();

                PlayerAction pa1 = ai1.getAction(0, gs);
                //dados de tempo ai1
                timeTemp = (System.currentTimeMillis() - startTime);
                sumAi1 += timeTemp;
                //coleto tempo mínimo
                if (ai1TempoMin > timeTemp) {
                    ai1TempoMin = timeTemp;
                }
                //coleto tempo maximo
                if (ai1TempoMax < timeTemp) {
                    ai1TempoMax = timeTemp;
                }

                startTime = System.currentTimeMillis();
                PlayerAction pa2 = ai2.getAction(1, gs);
                //dados de tempo ai2
                timeTemp = (System.currentTimeMillis() - startTime);
                sumAi2 += timeTemp;
                //coleto tempo mínimo
                if (ai2TempoMin > timeTemp) {
                    ai2TempoMin = timeTemp;
                }
                //coleto tempo maximo
                if (ai2TempoMax < timeTemp) {
                    ai2TempoMax = timeTemp;
                }

                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

                // simulate:
                gameover = gs.cycle();
                //w.repaint();
                nextTimeToUpdate += PERIOD;
            } else {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //avaliacao de tempo
            duracao = Duration.between(timeInicial, Instant.now());

        } while (!gameover && (gs.getTime() < 5000) && (duracao.toMinutes() < 7));

        log.add("Total de actions= " + totalAction + " sumAi1= " + sumAi1 + " sumAi2= " + sumAi2 + "\n");

        log.add("Tempos de AI 1 = " + ai1.toString());
        log.add("Tempo minimo= " + ai1TempoMin + " Tempo maximo= " + ai1TempoMax + " Tempo medio= " + (sumAi1 / (long) totalAction));

        log.add("Tempos de AI 2 = " + ai2.toString());
        log.add("Tempo minimo= " + ai2TempoMin + " Tempo maximo= " + ai2TempoMax + " Tempo medio= " + (sumAi2 / (long) totalAction) + "\n");

        log.add("Winner " + Integer.toString(gs.winner()));
        log.add("Game Over");

        if (gs.winner() == -1) {
            System.out.println("Empate!" + ai1.toString() + " vs " + ai2.toString() + " Max Cycles =" + MAXCYCLES + " Time:" + duracao.toMinutes());
        }

        gravarLog(log, sIA1, sIA2, sMap, sIte, pathLog);
        //System.exit(0);
        return true;
    }

    private void gravarLog(ArrayList<String> log, String sIA1, String sIA2, String sMap, String sIte, String pathLog) throws IOException {
        if (!pathLog.endsWith("/")) {
            pathLog += "/";
        }
        String nameArquivo = pathLog + "match_" + sIA1 + "_" + sIA2 + "_" + sMap + "_" + sIte + ".scv";
        File arqLog = new File(nameArquivo);
        if (!arqLog.exists()) {
            arqLog.createNewFile();
        }
        //abre o arquivo e grava o log
        try {
            FileWriter arq = new FileWriter(arqLog, false);
            PrintWriter gravarArq = new PrintWriter(arq);
            for (String l : log) {
                gravarArq.println(l);
            }

            gravarArq.flush();
            gravarArq.close();
            arq.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static List<AI> decodeScripts(UnitTypeTable utt, String sScripts) {

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

    public List<AI> decodeScripts2(UnitTypeTable utt, ArrayList<Integer> iScripts) {
        List<AI> scriptsAI = new ArrayList<>();

        for (Integer idSc : iScripts) {
            //System.out.println("tam tab"+scriptsTable.size());
            //System.out.println("id "+idSc+" Elems "+scriptsTable.get(BigDecimal.valueOf(idSc)));
            scriptsAI.add(buildScript(utt, scriptsTable.get(BigDecimal.valueOf(idSc))));
        }

        return scriptsAI;
    }

    public static AI buildScript(UnitTypeTable utt, ArrayList<Integer> iRules) {
        //System.out.println("laut");
        TableCommandsGenerator tcg = TableCommandsGenerator.getInstance(utt);
        List<ICommand> commands = new ArrayList<>();
        //System.out.println("sizeeiRules "+iRules.size());
        for (Integer idSc : iRules) {
            //System.out.println("idSc "+idSc);
            commands.add(tcg.getCommandByID(idSc));;
        }
        AI aiscript = new ChromosomeAI(utt, commands, "P1", "", new HashSet<String>(),new HashMap<Long, String>());

        return aiscript;
    }

    public HashMap<BigDecimal, ArrayList<Integer>> buildScriptsTable() {

        scriptsTable = new HashMap<BigDecimal, ArrayList<Integer>>();
        ArrayList<Integer> idsRulesList;
        try (BufferedReader br = new BufferedReader(new FileReader(pathTableScripts + "/ScriptsTable.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                idsRulesList = new ArrayList<>();
                String[] strArray = line.split(" ");
                int[] intArray = new int[strArray.length];
                for (int i = 0; i < strArray.length; i++) {
                    intArray[i] = Integer.parseInt(strArray[i]);
                }
                int idScript = intArray[0];
                int[] rules = Arrays.copyOfRange(intArray, 1, intArray.length);

                for (int i : rules) {
                    idsRulesList.add(i);
                }

                scriptsTable.put(BigDecimal.valueOf(idScript), idsRulesList);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return scriptsTable;
    }

    public ArrayList<Integer> getListIfInteger(String conf) {
        ArrayList<Integer> iScriptsAi1 = new ArrayList<>();
        String[] itens = conf.split(";");

        for (String element : itens) {
            iScriptsAi1.add(Integer.decode(element));
        }

        return iScriptsAi1;
    }
}
