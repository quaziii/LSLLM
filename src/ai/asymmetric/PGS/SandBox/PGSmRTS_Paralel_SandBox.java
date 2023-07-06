/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.asymmetric.PGS.SandBox;

import PVAI.Loop;
import ai.asymmetric.PGS.*;
import ai.abstraction.partialobservability.POHeavyRush;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.partialobservability.PORangedRush;
import ai.abstraction.partialobservability.POWorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.FloodFillPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.asymmetric.common.UnitScriptData;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.InterruptibleAI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import rts.GameState;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public class PGSmRTS_Paralel_SandBox extends AIWithComputationBudget implements InterruptibleAI {

    int LOOKAHEAD = 200;
    int I = 1;  // number of iterations for improving a given player
    int R = 0;  // number of times to improve with respect to the response fo the other player
    EvaluationFunction evaluation = null;
    List<AI> scripts = null;
    UnitTypeTable utt;
    PathFinding pf;
    int _startTime;

    AI defaultScript = null;

    long start_time = 0;
    int nplayouts = 0;

    GameState gs_to_start_from = null;
    int playerForThisComputation;
    
    seedPlayerThread t1;
    seedPlayerThread t2;
    seedPlayerThread t3;
    seedPlayerThread t4;
    
    long startTime;
    
    
        

    public PGSmRTS_Paralel_SandBox(UnitTypeTable utt) {
        this(100, -1, 200, 1, 1,
                new SimpleSqrtEvaluationFunction3(),
                //new SimpleSqrtEvaluationFunction2(),
                //new LanchesterEvaluationFunction(),
                utt,
                new AStarPathFinding()        
        );
    }

    public PGSmRTS_Paralel_SandBox(int time, int max_playouts, int la, int a_I, int a_R, EvaluationFunction e, UnitTypeTable a_utt, PathFinding a_pf) {
        super(time, max_playouts);

        LOOKAHEAD = la;
        I = a_I;
        R = a_R;
        evaluation = e;
        utt = a_utt;
        pf = a_pf;
        defaultScript = new POLightRush(a_utt);
        scripts = new ArrayList<>();
        buildPortfolio();
        t1 = new seedPlayerThread();
        //t2 = new seedPlayerThread();
        //t3 = new seedPlayerThread();
        //t4 = new seedPlayerThread();
    }

    protected void buildPortfolio() {
        this.scripts.add(new POHeavyRush(utt));
        this.scripts.add(new POLightRush(utt));
        this.scripts.add(new PORangedRush(utt));
        this.scripts.add(new POWorkerRush(utt));
        
        //this.scripts.add(new EconomyMilitaryRush(utt));
        
        //this.scripts.add(new POHeavyRush(utt, new FloodFillPathFinding()));
        //this.scripts.add(new POLightRush(utt, new FloodFillPathFinding()));
        //this.scripts.add(new PORangedRush(utt, new FloodFillPathFinding()));
       // this.scripts.add(new POWorkerRush(utt, new FloodFillPathFinding()));

    }

    @Override
    public void reset() {

    }
    
    protected void evalPortfolio(int heightMap){
        if(heightMap <= 16 && !portfolioHasWorkerRush()){
            this.scripts.add(new POWorkerRush(utt));
        }
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.canExecuteAnyAction(player)) {
            
            evalPortfolio(gs.getPhysicalGameState().getHeight());
            startNewComputation(player, gs);
            return getBestActionSoFar();
        } else {
            return new PlayerAction();
        }

    }

    @Override
    public PlayerAction getBestActionSoFar() throws Exception {
        System.out.println("-----------------------------------------------------------------------");
        
        //pego o melhor script do portfolio para ser a semente
        startTime = System.currentTimeMillis();
        AI seedPlayer = getSeedPlayer(playerForThisComputation);
        System.out.println("Tempo gasto definição seed player= "+ (System.currentTimeMillis()-startTime));
        
        startTime = System.currentTimeMillis();
        AI seedEnemy = getSeedPlayer(1 - playerForThisComputation);
        System.out.println("Tempo gasto definição seed Enemy= "+ (System.currentTimeMillis()-startTime));

        defaultScript = seedPlayer;

        UnitScriptData currentScriptData = new UnitScriptData(playerForThisComputation);
        currentScriptData.setSeedUnits(seedPlayer);
        setAllScripts(playerForThisComputation, currentScriptData, seedPlayer);
        
        System.out.println("Total de unidades ="+(gs_to_start_from.getUnits().size()-4));
        System.out.println("Quantidade de unidades ="+currentScriptData.getUnits().size());
        System.out.println("Tempo gasto até o momento= "+(System.currentTimeMillis()-start_time ));
        
        if( (System.currentTimeMillis()-start_time ) < TIME_BUDGET){
            currentScriptData = doPortfolioSearch(playerForThisComputation, currentScriptData, seedEnemy);
        }

        System.out.println("Tempo gasto pelo PGS = "+(System.currentTimeMillis()-start_time ));
        return getFinalAction(currentScriptData);
        
    }

    protected AI getSeedPlayer(int player) throws Exception {
        AI seed = null;
        double bestEval = -9999;
        AI enemyAI = new POLightRush(utt);
        //vou iterar para todos os scripts do portfolio
        /*
        for (AI script : scripts) {
            t1.initialInf(player, script.clone(), scripts.get(1).clone(), gs_to_start_from.clone(), LOOKAHEAD, evaluation);
            //double tEval = eval(player, gs_to_start_from, script, enemyAI);
            Thread th1 = new Thread(t1);            
            th1.start();
            while(th1.isAlive()){
                
            }
            double tEval = t1.getEval();
            
            
            if (tEval > bestEval) {
                bestEval = tEval;
                seed = script;
            }
        }
        */
        
        double[] values = new double[scripts.size()];
        Loop.withIndex(0, scripts.size(), new Loop.Each() {
            public void run(int i) {                    
                try {
                    values[i] = eval(player, gs_to_start_from, scripts.get(i), enemyAI);
                } catch (Exception ex) {
                    Logger.getLogger(PGSmRTS_Paralel_SandBox.class.getName()).log(Level.SEVERE, null, ex);
                }
               
            }
        });
        
        double best = values[0];
        int id = 0;
        for (int i = 1; i < values.length; i++) {
            if(values[i] > best){
                best = values[i];
                id = i;
            }
            
        }
        seed = scripts.get(id);
       
         
        return seed;
    }

    /*
    * Executa um playout de tamanho igual ao @LOOKAHEAD e retorna o valor
     */
    public double eval(int player, GameState gs, AI aiPlayer, AI aiEnemy) throws Exception {
        AI ai1 = aiPlayer.clone();
        AI ai2 = aiEnemy.clone();

        GameState gs2 = gs.clone();
        ai1.reset();
        ai2.reset();
        int timeLimit = gs2.getTime() + LOOKAHEAD;
        boolean gameover = false;
        while (!gameover && gs2.getTime() < timeLimit) {
            if (gs2.isComplete()) {
                gameover = gs2.cycle();
            } else {
                gs2.issue(ai1.getAction(player, gs2));
                gs2.issue(ai2.getAction(1 - player, gs2));
            }
        }
        double e = evaluation.evaluate(player, 1 - player, gs2);

        return e;
    }

    /**
     * Realiza um playout (Dave playout) para calcular o improve baseado nos
     * scripts existentes.
     *
     * @param player
     * @param gs
     * @param uScriptPlayer
     * @param aiEnemy
     * @return a avaliação para ser utilizada como base.
     * @throws Exception
     */
    public double eval(int player, GameState gs, UnitScriptData uScriptPlayer, AI aiEnemy) throws Exception {
        //AI ai1 = defaultScript.clone();
        AI ai2 = aiEnemy.clone();

        GameState gs2 = gs.clone();
        //ai1.reset();
        ai2.reset();
        int timeLimit = gs2.getTime() + LOOKAHEAD;
        boolean gameover = false;
        while (!gameover && gs2.getTime() < timeLimit) {
            if (gs2.isComplete()) {
                gameover = gs2.cycle();
            } else {
                //gs2.issue(ai1.getAction(player, gs2));
                gs2.issue(uScriptPlayer.getAction(player, gs2));
                //
                gs2.issue(ai2.getAction(1 - player, gs2));
            }
        }

        return evaluation.evaluate(player, 1 - player, gs2);
    }

    @Override
    public AI clone() {
        return new PGSmRTS_Paralel_SandBox(TIME_BUDGET, ITERATIONS_BUDGET, LOOKAHEAD, I, R, evaluation, utt, pf);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();

        parameters.add(new ParameterSpecification("TimeBudget", int.class, 100));
        parameters.add(new ParameterSpecification("IterationsBudget", int.class, -1));
        parameters.add(new ParameterSpecification("PlayoutLookahead", int.class, 100));
        parameters.add(new ParameterSpecification("I", int.class, 1));
        parameters.add(new ParameterSpecification("R", int.class, 1));
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new SimpleSqrtEvaluationFunction3()));
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + TIME_BUDGET + ", " + ITERATIONS_BUDGET + ", " + LOOKAHEAD + ", " + I + ", " + R + ", " + evaluation + ", " + pf + ")";
    }

    public int getPlayoutLookahead() {
        return LOOKAHEAD;
    }

    public void setPlayoutLookahead(int a_pola) {
        LOOKAHEAD = a_pola;
    }

    public int getI() {
        return I;
    }

    public void setI(int a) {
        I = a;
    }

    public int getR() {
        return R;
    }

    public void setR(int a) {
        R = a;
    }

    public EvaluationFunction getEvaluationFunction() {
        return evaluation;
    }

    public void setEvaluationFunction(EvaluationFunction a_ef) {
        evaluation = a_ef;
    }

    public PathFinding getPathFinding() {
        return pf;
    }

    public void setPathFinding(PathFinding a_pf) {
        pf = a_pf;
    }

    @Override
    public void startNewComputation(int player, GameState gs) throws Exception {
        playerForThisComputation = player;
        gs_to_start_from = gs;
        nplayouts = 0;
        _startTime = gs.getTime();
        start_time = System.currentTimeMillis();
    }

    @Override
    public void computeDuringOneGameFrame() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void setAllScripts(int playerForThisComputation, UnitScriptData currentScriptData, AI seedPlayer) {
        currentScriptData.reset();
        for (Unit u : gs_to_start_from.getUnits()) {
            if (u.getPlayer() == playerForThisComputation) {
                currentScriptData.setUnitScript(u, seedPlayer);
            }
        }
    }

    private UnitScriptData doPortfolioSearch(int player, UnitScriptData currentScriptData, AI seedEnemy) throws Exception {
        int enemy = 1 - player;

        UnitScriptData bestScriptData = currentScriptData.clone();
        double bestScore = eval(player, gs_to_start_from, bestScriptData, seedEnemy);
        ArrayList<Unit> unitsPlayer = getUnitsPlayer(player);
        //controle pelo número de iterações
        for (int i = 0; i < I; i++) {
            //fazer o improve de cada unidade
            for (Unit unit : unitsPlayer) {
                //inserir controle de tempo
                if (System.currentTimeMillis() >= (start_time + (TIME_BUDGET - 10))) {
                    return currentScriptData;
                }
                //iterar sobre cada script do portfolio
                long startTime ;
                for (AI ai : scripts) {
                    currentScriptData.setUnitScript(unit, ai);
                    startTime = System.currentTimeMillis();
                    double scoreTemp = eval(player, gs_to_start_from, currentScriptData, seedEnemy);
                    System.out.println("------durante o doPortfolioSearch do PGS "+ (System.currentTimeMillis()-startTime));

                    if (scoreTemp > bestScore) {
                        bestScriptData = currentScriptData.clone();
                        bestScore = scoreTemp;
                    }
                }
                //seto o melhor vetor para ser usado em futuras simulações
                currentScriptData = bestScriptData.clone();
            }
        }
        return currentScriptData;
    }

    private ArrayList<Unit> getUnitsPlayer(int player) {
        ArrayList<Unit> unitsPlayer = new ArrayList<>();
        for (Unit u : gs_to_start_from.getUnits()) {
            if (u.getPlayer() == player) {
                unitsPlayer.add(u);
            }
        }

        return unitsPlayer;
    }

    private PlayerAction getFinalAction(UnitScriptData currentScriptData) throws Exception {
        PlayerAction pAction = new PlayerAction();
        HashMap<String, PlayerAction> actions = new HashMap<>();
        for (AI script : scripts) {
            actions.put(script.toString(), script.getAction(playerForThisComputation, gs_to_start_from));
        }
        for (Unit u : currentScriptData.getUnits()) {
            AI ai = currentScriptData.getAIUnit(u);

            UnitAction unt = actions.get(ai.toString()).getAction(u);
            if (unt != null) {
                pAction.addUnitAction(u, unt);
            }
        }
        
        

        return pAction;
    }

    private boolean portfolioHasWorkerRush() {
        for (AI script : scripts) {
            if(script.toString().contains("POWorkerRush")){
                return true;
            }
        }
        return false;
    }

}
