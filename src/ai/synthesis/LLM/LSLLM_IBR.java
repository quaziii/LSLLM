package ai.synthesis.LLM;

import ai.core.AI;
import ai.synthesis.ComplexDSL.EvaluateGameState.SimplePlayout;
import ai.synthesis.ComplexDSL.IAs2.Avaliador;
import ai.synthesis.ComplexDSL.IAs2.Search2;
import ai.synthesis.ComplexDSL.LS_CFG.FactoryLS;
import ai.synthesis.ComplexDSL.LS_CFG.Node_LS;
import ai.synthesis.ComplexDSL.Synthesis_Base.AIs.Interpreter;
import ai.synthesis.ComplexDSL.Synthesis_Base.CFG.Control;
import ai.synthesis.ComplexDSL.Synthesis_Base.CFG.Factory;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LSLLM_IBR {    // Local Search + LLM
  long tempo_ini;

  static int budget = 250000;

  Factory f = new FactoryLS();

  SimplePlayout playout = new SimplePlayout();

  public Node_LS searchBestResponse(GameState gs, int max, Node_LS best, double v) throws Exception {
    // TODO Auto-generated method stub
    long Tini = System.currentTimeMillis();
    long tempo = System.currentTimeMillis()-Tini;
    int tempo_limite = 2000;
    Random r = new Random();


    while( (tempo*1.0)/1000.0 <tempo_limite) {

      Node_LS melhor_vizinho = (Node_LS) best.Clone(f) ;
      double v_vizinho = -111111;
      for(int i= 0;i<1000;i++) {

        Node_LS aux = (Node_LS) (melhor_vizinho.Clone(f));
        List<Node_LS> list =new ArrayList<>();
        for(int ii=0;ii<1;ii++) {

          aux.countNode(list);
          int custo = r.nextInt(9)+1;
          int no = r.nextInt(list.size());

          list.get(no).mutation(0, custo, false);

        }
        UnitTypeTable utt = new UnitTypeTable();
        AI ai1 = new Interpreter(utt, aux);
        AI ai2 = new Interpreter(utt, melhor_vizinho);

        double v2 = playout.run(gs, utt, 0, max, ai1, ai2, false).m_a;

        if(v2 == 1.0) {
          best = (Node_LS) aux.Clone(f);
          return best;
        }

        tempo = System.currentTimeMillis()-Tini;
        if((tempo*1.0)/1000.0 >tempo_limite)break;

      }

      tempo = System.currentTimeMillis()-Tini;
    }
    return best;
  }

  public void run(GameState gs, int max) throws Exception {
    boolean isSuccess = false;
    Node_LS j = null;
    while (!isSuccess) {
      try {
        String strategy = GPT35Request.getStartingStrategy();
        j = ASTCreator.createAST(strategy);
//        ava.addTojs(j1);
        isSuccess = true;
      } catch (Exception e) {
        System.out.println(e.toString());
      }
    }
    isSuccess = false;

    while(true) {
      long paraou = System.currentTimeMillis()-this.tempo_ini;
      boolean foundInLLM = false;

//      Node_LS j = ava.getIndividuo();
      Node_LS searchStart = j;
      double rLLM = -99999.0;

      System.out.println("At the beginning: " + Control.save(j));

      for (int i = 0; i < 7; i++) {
//        System.out.println("i = " + i);
//        System.out.println();
//        System.out.println();
//        System.out.println("------------Individuo-----------");
//        System.out.println(j.translateIndentation(1));
//        System.out.println();
//        System.out.println();
        String counterStrategy = "";
        Node_LS c0 = null;
        while (!isSuccess) {
          try {
            counterStrategy = GPT35Request.getBestResponseStrategy(j.translateIndentation(1));
            c0 = ASTCreator.createAST(counterStrategy);
//            System.out.println();
//            System.out.println("-------- Counter Strategy ---------");
//            System.out.println(counterStrategy);
//            System.out.println();
            System.out.println("--------- C0 ----------");
            System.out.println(Control.save(c0));
            System.out.println();
            isSuccess = true;
          } catch (Exception e) {
            System.out.println(e.toString());
          }
        }
        isSuccess = false;

        UnitTypeTable utt = new UnitTypeTable();
        AI ai1 = new Interpreter(utt, c0);
        AI ai2 = new Interpreter(utt, j);

        double r0 = playout.run(gs, utt,0, max, ai1, ai2, false).m_a;
        System.out.println("r0 = " + r0);

        if (r0 == 1.0) {
          j = c0;
          foundInLLM = true;
          break;
        }

        if (r0 > rLLM) {
          rLLM = r0;
          searchStart = c0;
        }
      }
      if (foundInLLM) {
        System.out.println();
        System.out.println();
        System.out.println("------- Found in LLM --------");
        System.out.println();
        System.out.println();
        continue;
      }
      System.out.println();
      System.out.println("Starting search from this:");
      System.out.println(Control.save(searchStart));
      System.out.println();

      Node_LS  c0 = this.searchBestResponse(gs, max, searchStart, rLLM);

      UnitTypeTable utt = new UnitTypeTable();
      AI ai1 = new Interpreter(utt, c0);
      AI ai2 = new Interpreter(utt, j);

      double r0 = playout.run(gs, utt,0, max, ai1, ai2, false).m_a;

      if(r0 == 1.0) {
        j = c0;
        System.out.println("At the end: " + Control.save(j));
      }

    }
  }

  public static void main(String[] args) throws Exception {
    UnitTypeTable utt = new UnitTypeTable();
    String path_map = "maps/24x24/basesWorkers24x24A.xml";
    PhysicalGameState pgs = PhysicalGameState.load(path_map, utt);
    GameState gs2 = new GameState(pgs, utt);

    LSLLM_IBR test = new LSLLM_IBR();
    test.run(gs2, 6000);
  }
}
