package ai.synthesis.LLM;

import ai.abstraction.WorkerRush;
import ai.asymmetric.ManagerUnits.IManagerAbstraction;
//import ai.competition.mayariBot.mayari;
import ai.core.AI;
import ai.synthesis.ComplexDSL.LS_CFG.FactoryLS;
import ai.synthesis.ComplexDSL.LS_CFG.Node_LS;
import ai.synthesis.ComplexDSL.Synthesis_Base.AIs.Interpreter;
import ai.synthesis.ComplexDSL.Synthesis_Base.CFG.Control;
import ai.synthesis.ComplexDSL.Synthesis_Base.CFG.Node;
import ai.synthesis.ComplexDSL.Synthesis_Base.CFG.S;
import gui.PhysicalGameStatePanel;
import rts.*;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GameVisualSimulationTest {

  static String _nameStrategies = "", _enemy = "";
  static AI[] strategies = null;
  private static final List<Pair<Unit, List<UnitAction>>> choices = new ArrayList<>();
  private static IManagerAbstraction behaviorAbs;
  static PhysicalGameState pgs;
  static ResourceUsage base_ru;
  static long size;
  static HashSet<Unit> unitsControled = new HashSet<>();
  private static List<AI> scripts;
  static FactoryLS f = new FactoryLS();

  public static void main(String[] args) throws Exception {
    for (int i = 0; i < 100; i++) { // TODO: change it to 4 later
      try {
        run();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static int run() throws Exception {
    UnitTypeTable utt = new UnitTypeTable();
    //UnitTypeTable utt = new UnitTYpeTableBattle();
    //PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16.xml", utt);
//        PhysicalGameState pgs = PhysicalGameState.load("maps/8x8/basesWorkers8x8A.xml", utt);
    //PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16A.xml", utt);
    //PhysicalGameState pgs = PhysicalGameState.load("maps/BWDistantResources32x32.xml", utt);
    //PhysicalGameState pgs = PhysicalGameState.load("maps/32x32/basesWorkers32x32A.xml", utt);
    PhysicalGameState pgs = PhysicalGameState.load("maps/24x24/basesWorkers24x24A.xml", utt);
    //PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)BloodBath.scmB.xml", utt);
    //PhysicalGameState pgs = PhysicalGameState.load("maps/8x8/FourBasesWorkers8x8.xml", utt);
    //PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/TwoBasesBarracks16x16.xml", utt);
    //PhysicalGameState pgs = PhysicalGameState.load("maps/NoWhereToRun9x8.xml", utt);
    //PhysicalGameState pgs = PhysicalGameState.load("maps/DoubleGame24x24.xml", utt);
    //PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();
    //testes
    //PhysicalGameState pgs = PhysicalGameState.load("maps/24x24/basesWorkers24x24A.xml", utt);
    //PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)EmpireoftheSun.scmC.xml", utt);

    GameState gs = new GameState(pgs, utt);
    int MAXCYCLES = 4000;
    if (pgs.getHeight() == 8) {
      MAXCYCLES = 3000;
    }
    if (pgs.getHeight() == 16) {
      MAXCYCLES = 5000;
      //MAXCYCLES = 1000;
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
    int PERIOD = 20;
    boolean gameover = false;

//    String code = "S;For_S;S;If_B_then_S;B;CanAttack;S;For_S;S;If_B_then_S_else_S;B;CanAttack;S;S_S;S;C;Harvest;50;S;For_S;S;C;Attack;Strongest;S;C;Train;Worker;Right;20";

//    GPT35Request newGPT35Request = new GPT35Request();
//    String strategy = GPT35Request.getStartingStrategy();
//    System.out.println();
//    System.out.println();
//    System.out.println("==============Strategy===========");
//    System.out.println(strategy);
//    Node AST = ASTCreator.createAST(strategy);
//    System.out.println("==============AST=============");
//    System.out.println(AST.translateIndentation(1));
//    System.out.println("================================");
//    System.out.println();
//    System.out.println();

    String strategy = "for(Unit u){\n" +
      "  if(u.isBuilder()) then {\n" +
      "    if(HasNumberOfWorkersHarvesting(0)) then {\n" +
      "      u.harvest(1)\n" +
      "    }\n" +
      "    u.moveToUnit(Ally,Random)\n" +
      "  }\n" +
      "  if(u.HasUnitWithinDistanceFromOpponent(2)) then {\n" +
      "    u.attack(Closest)\n" +
      "  }\n" +
      "  if(u.OpponentHasUnitInPlayerRange()) then {\n" +
      "    u.moveAway()\n" +
      "  }\n" +
      "  u.idle()\n" +
      "}";
    Node_LS AST = ASTCreator.createAST(strategy);
    AI ai1 = new Interpreter(utt, AST);
    String counter = GPT35Request.getBestResponseStrategy(strategy);
    Node_LS AST2 = ASTCreator.createAST(counter);
    System.out.println(AST2.translateIndentation(1));
    System.out.println(Control.save(AST2));
    AI ai2 = new Interpreter(utt, AST2);
//        AI ai2 = new Interpreter(utt, pre_ai2);
    //AI ai2 = new StrategyTactics(utt);
    //AI ai2 = new AHTNAI(utt);
    //AI ai1 = new WorkerRushPlusPlus(utt);
//        AI ai2 = new WorkerRush(utt);
//    AI ai2 = new mayari(utt);
//      AI ai2 = new CoacAI(utt);
    //AI ai1 = new LightSSSmRTSScriptChoice(utt, RoundRobinClusterLeve.decodeScripts(utt, "1;2;3;"), 200, "SSS");
    //AI ai2 = new LightSSSmRTSScriptChoice(utt, RoundRobinClusterLeve.decodeScripts(utt, "1;2;3;"), 200, "SSS");
    //AI ai1 = new GABScriptChoose(utt, 200, 3, 4, RoundRobinClusterLeve_Cluster.decodeScripts(utt, "0;1;2;3;"), "GAB");
    //AI ai1 = new SABScriptChoose(utt, 200, 3, 4, RoundRobinClusterLeve_Cluster.decodeScripts(utt, "0;1;2;3;"), "SAB");
//    AI ai2 = new WorkerRush(utt);
    //AI ai1 = new RangedRush(utt);
    //AI ai2 = new LightRush(utt);
    //AI ai1 = new HeavyRush(utt);
    //AI ai1 = new PassiveAI();
    //AI ai1 = new POLightRush(utt);
    //AI ai1 = new EconomyRush(utt);
    //AI ai1 = new RangedDefense(utt);
    //AI ai1 = new EconomyRushBurster(utt);
    //AI ai1 = new PassiveAI(utt);
//        AI ai2 = new NaiveMCTS(utt);
    //AI ai1 = new PortfolioAI(utt);
    //AI ai1 = new PVAI(utt);
    //AI ai1 = new WorkerRushPlusPlus(utt);
    //AI ai1 = new RandomBiasedAI(utt);
//        AI ai1 = new PuppetSearchMCTS(utt);
//        AI ai2 = new PuppetSearchMCTS(utt);
    //AI ai1 = new PortfolioAI(utt);
    //AI ai1 = new POLightRush(utt);
    //AI ai1 = new BasicExpandedConfigurableScript(utt, new AStarPathFinding(), 18,0,0,1,2,2,-1,-1,3); //WR
    //AI ai1 = new PGSSCriptChoice(utt, decodeScripts2(utt, "0;1;;"), "PGSRSym");
    //AI ai1 = new CmabNaiveMCTS(100, -1, 200, 1, 0.3f, 0.0f, 0.4f, 0, new RandomBiasedAI(utt), new SimpleSqrtEvaluationFunction3(),
    //                          true, "CmabCombinatorialGenerator", utt, decodeScriptsFull(utt, "0;"), "A1N_W");
    //AI ai1 = new SABScriptChoose(utt, 2, 2, decodeScriptsFull(utt, "0;"), "GAB_W");
    //AI ai1 = new WorkerRush(utt);
//        AI ai2 = buildCommandsIA(utt, (new TradutorDSL("attack(Heavy,weakest) harvest(2) if(!HaveUnitsinEnemyRange(Worker)) then(attack(Light,closest) moveaway(Worker)) else(train(Heavy,3,Up)) for(u) (if(!HaveQtdUnitsHarversting(7)) then(moveaway(Worker,u) build(Barrack,1,Down,u) harvest(1,u)) train(Worker,8,Left) if(!HaveQtdUnitsHarversting(7)) then(train(Light,3,Left) moveaway(Light,u) harvest(3,u)))")).getAST(), "DO-1");
    //AI ai2 = buildCommandsIA(utt, (new TradutorDSL("for(u) (if(!HaveUnitsToDistantToEnemy(Worker,3,u)) then(harvest(3,u) train(Worker,4,Right)) attack(Worker,closest,u)) if(!HaveEnemiesinUnitsRange(Heavy)) then(train(Worker,9,Left) harvest(1)) if(!HaveUnitsinEnemyRange(Ranged)) then(harvest(2))")).getAST(), "DO-1");
    //AI ai1 = buildCommandsIA(utt, (new TradutorDSL("harvest(2) if(HaveEnemiesinUnitsRange(Ranged)) then(moveaway(Worker) attack(Ranged,strongest)) else(harvest(2) attack(Ranged,closest) harvest(3)) for(u) (if(HaveEnemiesStrongest(Worker,u)) then(train(Light,8,Right)) else(moveToUnit(Light,Enemy,closest,u) harvest(1,u)) train(Worker,9,Up) moveaway(Light,u) attack(Worker,strongest,u) build(Barrack,1,Left,u) if(HaveQtdEnemiesbyType(Worker,7)) then(train(Ranged,6,EnemyDir)) attack(Light,weakest,u))")).getAST(), "DO-1");

//        AI ai2 = new WorkerRush(utt);
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
    //AI ai1 = new GAS(utt, 200, 2, 1, //closest
    //                decodeScriptsFull(utt, "0;1;2;3;"), "GAS");
    //AI ai1 = new CMABBuilder(utt);
    //AI ai1 = new SAS(utt, 200, 2, 1, //closest
    //                decodeScriptsFull(utt, "0;1;2;3;"), "SAS");
    //AI ai1 = new SABp(utt, 200, 2, 1, //closest
    //                decodeScriptsFull(utt, "0;1;2;3;"), "SABp");
    //AI ai2 = new GABScriptChoose(utt, 200, 2, 1, //closest
    //                decodeScriptsFull(utt, "0;1;2;3;"), "GAB");
    //AI ai1 = new GAB_oldVersion(utt);
    //AI ai1 = new GABScriptChoose(utt, 2, 4);
    //AI ai1 = new SAB_oldVersion(utt);
    //AI ai1 = new WorkerRush(utt);
    //AI ai1 = new POLightRush(utt);
    //AI ai1 = new CmabNaiveMCTS(100, -1, 200, 10, 0.3f, 0.0f, 0.4f, 0, new RandomBiasedAI(utt),
    //                new SimpleSqrtEvaluationFunction3(), true, "CmabCombinatorialGenerator", utt,
    //                RoundRobinClusterLeve.decodeScripts(utt, "0;1;2;3;"), "A1N");
    //AI ai2 = new botEmptyBase(utt, "for(u) (harvest(7,u) train(Worker,17,Down) attack(Worker,weakest,u))", "t2");
//        ArrayList<AI> bots2 = new ArrayList<>();
//        bots2.add(new botEmptyBase(utt, "build(Barrack,1,Right) harvest(1) train(Ranged,5,EnemyDir) for(u) (moveOnceToCoord(Ranged,1,5,5,u))", "t3"));
//        AI ai2 = new CmabAssymetricMCTS(100, -1, 100, 2, 0.3F, 0.0F, 0.4F, 0, new RandomBiasedAI(utt),
//                new SimpleSqrtEvaluationFunction3(), true, utt, "ManagerClosestEnemy",
//                1, bots2, "A3N1");
//        //AI ai2 = new botEmptyBase(utt, "build(Barrack,1,Right) harvest(1) train(Ranged,5,EnemyDir) for(u) (moveOnceToCoord(Ranged,1,5,5,u))", "t3");
//        AI ai1 = new botEmptyBase(utt, "build(Barrack,1,Right) harvest(1) train(Ranged,5,EnemyDir) for(u) (moveOnceToCoord(Ranged,1,5,5,u))", "t3");
//        ArrayList<AI> bots1 = new ArrayList<>();
//        bots1.add(new botEmptyBase(utt, "build(Barrack,1,Right) harvest(1) train(Ranged,5,EnemyDir) for(u) (moveOnceToCoord(Ranged,1,5,5,u))", "t3"));
//        AI ai1 = new CmabAssymetricMCTS(100, -1, 100, 2, 0.3F, 0.0F, 0.4F, 0, new RandomBiasedAI(utt),
//                new SimpleSqrtEvaluationFunction3(), true, utt, "ManagerClosestEnemy",
//                1, bots2, "A3N1");
    //AI ai2 = new GuidedRojoA3N(utt);
    //AI ai1 = new Rojo(utt);
    //AI ai2 = new WorkerRush(utt);
//        AI ai1 = new CmabAssymetricMCTS(100, -1, 100, 1, 0.3f, 0.0f, 0.4f, 0, new RandomBiasedAI(utt),
//                new SimpleSqrtEvaluationFunction3(), true, utt, "ManagerClosestEnemy", 2,
//                decodeScriptsFull(utt, "1;2;3;"), "A3N");
//        scripts = decodeScriptsFull(utt, "1;2;3;");
    //AI ai2 = new SABScriptChoose(utt, 200, 2, 1, //closest
    //                decodeScriptsFull(utt, "0;1;2;3;"), "SAB");
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
//        AI ai2 = new Capivara(utt);
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
//        System.out.println("---------AI's---------");
//        System.out.println("AI 1 = " + ai1.toString());
//        System.out.println("AI 2 = " + ai2.toString() + "\n");
    ai1.preGameAnalysis(gs, 1);
    ai2.preGameAnalysis(gs, 1);

//    JFrame w = PhysicalGameStatePanel.newVisualizer(gs, 720, 720, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
    //JFrame w = PhysicalGameStatePanel.newVisualizer(gs, 720, 720, false, PhysicalGameStatePanel.COLORSCHEME_WHITE);
    long startTime = System.currentTimeMillis();
    long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
    do {
      if (System.currentTimeMillis() >= nextTimeToUpdate) {
        startTime = System.currentTimeMillis();

        PlayerAction pa1 = ai1.getAction(0, gs);

        startTime = System.currentTimeMillis();
        PlayerAction pa2 = ai2.getAction(1, gs);

        gs.issueSafe(pa1);
        gs.issueSafe(pa2);

        // simulate:
        gameover = gs.cycle();
//        w.repaint();
        nextTimeToUpdate += PERIOD;
      } else {
        try {
          Thread.sleep(1);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

    } while (!gameover && gs.getTime() < MAXCYCLES);
    System.out.println(gs.getTime());
    System.out.println("Winner " + gs.winner());
    System.out.println("Game Over");

    return gs.winner();
  }



}
