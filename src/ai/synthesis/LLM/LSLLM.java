package ai.synthesis.LLM;

import ai.synthesis.ComplexDSL.IAs2.Avaliador;
import ai.synthesis.ComplexDSL.IAs2.AvaliadorPadrao;
import ai.synthesis.ComplexDSL.IAs2.HC;
import ai.synthesis.ComplexDSL.IAs2.Search2;
import ai.synthesis.ComplexDSL.LS_CFG.Node_LS;
import ai.synthesis.ComplexDSL.Synthesis_Base.CFG.Control;
import ai.synthesis.ComplexDSL.Synthesis_Base.CFG.Factory;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

public class LSLLM {    // Local Search + LLM
  Search2 sc;
  Avaliador ava;
  long tempo_ini;

  public LSLLM(Search2 sc, Avaliador ava) {
    // TODO Auto-generated constructor stub
    this.ava=ava;
    this.sc = sc;
  }

  public void run(GameState gs, int max) throws Exception {
    boolean isSuccess = false;
    while (!isSuccess) {
      try {
        String strategy = GPT35Request.getStartingStrategy();
        Node_LS j1 = ASTCreator.createAST(strategy);
        ava.addTojs(j1);
        isSuccess = true;
      } catch (Exception e) {
//        System.out.println(e.toString());
      }
    }
    isSuccess = false;

    while(ava.getBudget() <= 250000) {
      long paraou = System.currentTimeMillis()-this.tempo_ini;
      boolean foundInLLM = false;

      Node_LS j = ava.getIndividuo();
      Node_LS searchStart = j;
      double rLLM = -99999.0;

//      System.out.println("At the beginning: " + Control.save(j));

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
//            System.out.println("-------- AST of CS ----------");
//            System.out.println(c0.translateIndentation(1));
//            System.out.println();
            isSuccess = true;
          } catch (Exception e) {
//            System.out.println(e.toString());
          }
        }
        isSuccess = false;

        double r0 = ava.Avalia(gs, max, c0);
        double r1 = ava.Avalia(gs, max, j);

        if (r0 > r1) {
          ava.update(gs, max, c0);
          foundInLLM = true;
//          System.out.println("LLM counter strategy: " + Control.save(c0));
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

      Node_LS  c0 = sc.run(gs, max, searchStart, ava);

      double r0 = ava.Avalia(gs, max, c0);
      double r1 = ava.Avalia(gs, max, j);

      if(r0>r1) {
        ava.update(gs, max, c0);
//        System.out.println("At the end: " + Control.save(c0));
      }

    }
  }

  public static void main(String[] args) throws Exception {
    UnitTypeTable utt = new UnitTypeTable();
    String path_map = "maps/24x24/basesWorkers24x24A.xml";
    PhysicalGameState pgs = PhysicalGameState.load(path_map, utt);
    GameState gs2 = new GameState(pgs, utt);

    LSLLM test = new LSLLM(new HC(2000), new AvaliadorPadrao(5));
    test.run(gs2, 6000);
  }
}
