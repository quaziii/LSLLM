package ai.synthesis.LLM;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GPT35Request {
  private static final String URL_GPT_TURBO = "https://api.openai.com/v1/chat/completions";
  private static final String API_KEY = "";
  private static final String REQUEST_BODY_GPT_TURBO =
    "{\n" +
      "  \"model\": \"gpt-3.5-turbo\",\n" +
      "  \"messages\": [{ \"role\": \"user\", \"content\": \"%s\" }],\n" +
      "  \"temperature\": 0.7\n" +
      "}";

  private static final String mapDetails = "I have a 24x24 map of microRTS. Consider this map as a 2 dimensional array with the following units in it:\\n" +
    "- There are total 4 neutral resource centers R located at the indices (0,0), (0,1), (23,22),(23,23)\\n" +
    "- The base B1 of player 1 is located at index (2,2)\\n" +
    "- The base B2 of player 2 is located at index (21,21)\\n" +
    "- The only worker W1 of player 1 is located at index (1,1)\\n" +
    "- The only worker of player 2 is located at index (22,22)\\n\\n";

  private static final String DSL = "Now I have a context free grammar (CFG) of microRTS playing strategy inside the <CFG></CFG> tag written bellow:\\n\\n" +
    "<CFG>\\n" +
    "S -> SS | for(Unit u) S | if(B) then S | if(B) then S else S | C | λ\\n" +
    "B -> u.hasNumberOfUnits(T, N) | u.opponentHasNumberOfUnits(T, N) | u.hasLessNumberOfUnits(T, N) | u.haveQtdUnitsAttacking(N) | u.hasUnitWithinDistanceFromOpponent(N) | u.hasNumberOfWorkersHarvesting(N) | u.is_Type(T) | u.isBuilder() | u.canAttack() | u.hasUnitThatKillsInOneAttack() | u.opponentHasUnitThatKillsUnitInOneAttack() | u.hasUnitInOpponentRange() | u.opponentHasUnitInPlayerRange() | u.canHarvest()\\n" +
    "C -> u.build(T, D, N) | u.train(T, D, N) | u.moveToUnit(T_p, O_p) | u.attack(O_p) | u.harvest(N) | u.idle() | u.moveAway()\\n" +
    "T -> Base | Barracks | Ranged | Heavy | Light | Worker\\n" +
    "N -> 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 15 | 20 | 25 | 50 | 100\\n" +
    "D -> EnemyDir | Up | Down | Right | Left\\n" +
    "O_p -> Strongest | Weakest | Closest | Farthest | LessHealthy | MostHealthy | Random\\n" +
    "T_p -> Ally | Enemy\\n" +
    "</CFG>\\n\\n";

  private static final String DSLExplanation = "This CFG allows nested loops and conditionals. It contains several Boolean functions (B) and command-oriented functions (C) that provide either information about the current state of the game or commands for the ally units.\\n" +
    "The Boolean functions ('B' in the CFG) are described below:\\n" +
    "1. u.hasNumberOfUnits(T, N): Checks if the ally player has N units of type T.\\n" +
    "2. u.opponentHasNumberOfUnits(T, N): Checks if the opponent player has N units of type T.\\n" +
    "3. u.hasLessNumberOfUnits(T, N): Checks if the ally player has less than N units of type T.\\n" +
    "4. u.haveQtdUnitsAttacking(N): Checks if the ally player has N units attacking the opponent.\\n" +
    "5. u.hasUnitWithinDistanceFromOpponent(N): Checks if the ally player has a unit within a distance N from a opponent’s unit.\\n" +
    "6. u.hasNumberOfWorkersHarvesting(N): Checks if the ally player has N units of type Worker harvesting resources.\\n" +
    "7. u.is_Type(T): Checks if a unit is an instance of type T.\\n" +
    "8. u.isBuilder(): Checks if a unit is of type Worker.\\n" +
    "9. u.canAttack(): Checks if a unit can attack.\\n" +
    "10. u.hasUnitThatKillsInOneAttack(): Checks if the ally player has a unit that kills an opponent’s unit with one attack action.\\n" +
    "11. u.opponentHasUnitThatKillsUnitInOneAttack(): Checks if the opponent player has a unit that kills an ally’s unit with one attack action.\\n" +
    "12. u.hasUnitInOpponentRange(): Checks if an unit of the ally player is within attack range of an opponent’s unit.\\n" +
    "13. u.opponentHasUnitInPlayerRange(): Checks if an unit of the opponent player is within attack range of an ally’s unit.\\n" +
    "14. u.canHarvest(): Checks if a unit can harvest resources.\\n\\n" +
    "The Command functions ('C' in the CFG) are described below:\\n" +
    "1. u.build(T, D, N): Builds N units of type T on a cell located on the D direction of the unit.\\n" +
    "2. u.train(T, D, N): Trains N units of type T on a cell located on the D direction of the structure responsible for training them.\\n" +
    "3. u.moveToUnit(T_p, O_p): Commands a unit to move towards the player T_p following a criterion O_p.\\n" +
    "4. u.attack(O_p): Sends N Worker units to harvest resources.\\n" +
    "5. u.harvest(N): Sends N Worker units to harvest resources.\\n" +
    "6. u.idle(): Commands a unit to stay idle and attack if an opponent unit comes within its attack range.\\n" +
    "7. u.moveAway(): Commands a unit to move in the opposite direction of the player's base.\\n\\n" +
    "'T' represents the types a unit can assume. 'N' is a set of integers. 'D' represents the directions available used in action functions.\\n" +
    "O_p is a set of criteria to select an opponent unit based on their current state. T_p represents the set of target players.\\n\\n";

  private static final String strategyWritingGuidelines = "The following 5 are some guidelines of writing the playing strategy:\\n" +
    "1. There is NO NEED TO write classes, initiate objects such as Unit, Worker, etc. There is also NO NEED TO write comments.\\n" +
    "2. Use curly braces like C/C++/Java while writing any 'for' or 'if' or 'if-else' block. Start the curly braces in the same line of the block.\\n" +
    "3. Do not write 'else if(B) {' block. Write 'else { if(B) {...}}' instead." +
    "4. A strategy must be written inside a 'for' block.\\n" +
    "5. A strategy might be the combination of multiple strategies where each strategy is written inside a 'for' block.\\n\\n";

  private static final String strategyExamples = "Some example strategies that satisfy this CFG are:\\n" +
    "Example 1:\\n" +
    "```\\n" +
    "for(Unit u){\\n" +
    "  u.attack(Closest)\\n" +
    "  for(Unit u){\\n" +
    "    for(Unit u){\\n" +
    "      u.idle()\\n" +
    "      u.harvest(6)\\n" +
    "    }\\n" +
    "    u.train(Worker,Left,25)\\n" +
    "    u.moveToUnit(Ally,Closest)\\n" +
    "  }\\n" +
    "}\\n" +
    "```\\n" +
    "Example 2:\\n" +
    "```\\n" +
    "for(Unit u){\\n" +
    "  u.build(Barracks,EnemyDir,6)\\n" +
    "}\\n" +
    "for(Unit u){\\n" +
    "  u.moveToUnit(Enemy,LessHealthy)\\n" +
    "  u.moveToUnit(Enemy,Farthest)\\n" +
    "  for(Unit u){\\n" +
    "    u.harvest(50)\\n" +
    "    for(Unit u){\\n" +
    "      u.train(Light,Left,6)\\n" +
    "    }\\n" +
    "    for(Unit u){\\n" +
    "      u.idle()\\n" +
    "    }\\n" +
    "  }\\n" +
    "}\\n" +
    "```\\n" +
    "Example 3:\\n" +
    "```\\n" +
    "for(Unit u){\\n" +
    "  u.idle()\\n" +
    "  u.attack(Closest)\\n" +
    "  for(Unit u){\\n" +
    "    u.harvest(2)\\n" +
    "  }\\n" +
    "  for(Unit u){\\n" +
    "    u.idle()\\n" +
    "  }\\n" +
    "  for(Unit u){\\n" +
    "    if(u.HasUnitWithinDistanceFromOpponent(1)) then {\\n" +
    "      u.moveToUnit(Ally,Farthest)\\n" +
    "    }\\n" +
    "    u.train(Worker,EnemyDir,4)\\n" +
    "    for(Unit u){\\n" +
    "      if(u.HasUnitWithinDistanceFromOpponent(4)) then {\\n" +
    "        if(u.OpponentHasUnitThatKillsUnitInOneAttack()) then {\\n" +
    "          u.harvest(9)\\n" +
    "        } else {\\n" +
    "          e\\n" +
    "        }\\n" +
    "      } else {\\n" +
    "        u.train(Heavy,Up,5)\\n" +
    "      }\\n" +
    "    }\\n" +
    "    for(Unit u){\\n" +
    "      u.build(Barracks,Right,15)\\n" +
    "      u.harvest(5)\\n" +
    "    }\\n" +
    "    u.attack(Closest)\\n" +
    "  }\\n" +
    "  u.moveToUnit(Ally,MostHealthy)\\n" +
    "  u.train(Ranged,EnemyDir,100)\\n" +
    "}\\n" +
    "```\\n\\n";

  private static final String tasks = "1. Understand the meanings of all the boolean (B) and command (C) functions from above and try to relate them in the context of microRTS playing strategies.\\n" +
    "2. Write a very strong playing strategy pseudocode prioritizing harvesting resources and considering the above map, the above CFG and the playing rules of microRTS. You must follow the guidelines of writing the playing strategy while writing your strategy.\\n" +
    "3. You must not use any symbols (for example: &&, ||, etc.) outside this given CFG. You have to strictly follow this CFG while writing the pseudocode.\\n" +
    "4. Look carefully, the methods of non-terminal symbols B and C have prefixes 'u.' in the examples since they are methods of the object 'Unit u'. You also need to follow the patterns of the examples.\\n" +
    "5. Write only the pseudocode inside '<strategy></strategy>' tag.\\n" +
    "6. Do not write unnecessary symbols of the CFG such as, 'S ->', '->', etc.\\n" +
    "7. Check the pseudocode and ensure it does not violate the rules of the CFG or the guidelines of writing the strategy.";

  private static String fixFormatting(String str) {
    ArrayList<String> lines = new ArrayList<>(Arrays.asList(str.split("\n")));
    ArrayList<String> result = new ArrayList<>();
    String temp = null;

    for (String line : lines) {
      line = line.trim();
      if (line.equals("{")) {
        temp += " {";
      } else if (line.equals("else")) {
        temp += " else";
      } else {
        if (temp != null) {
          result.add(temp);
        }
        temp = line;
      }
    }

    if (temp != null) {
      result.add(temp);
    }

    return String.join("\n", result);
  }

  private static String getResponseFromApi(String postBody) throws IOException, InterruptedException {
    var request = HttpRequest.newBuilder()
      .uri(URI.create(URL_GPT_TURBO))
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer " + API_KEY)
      .POST(HttpRequest.BodyPublishers.ofString(postBody))
      .build();

    var client = HttpClient.newHttpClient();
    var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    String responseBody = response.body();
//    System.out.println(responseBody);

    JSONObject jo = new JSONObject(responseBody);

    JSONArray choices = jo.getJSONArray("choices");

    JSONObject firstChoice = choices.getJSONObject(0);
    String finishReason = firstChoice.getString("finish_reason");
    JSONObject message = firstChoice.getJSONObject("message");
    String role = message.getString("role");
    String content = message.getString("content");

    return content;
  }

  public static String getStartingStrategy() throws Exception {
    String InitialStrategyRequest = mapDetails + DSL + DSLExplanation + strategyWritingGuidelines + "Your tasks are the following 7:\\n" + tasks;
//    String postBody = REQUEST_BODY_GPT_TURBO.formatted(InitialStrategyRequest);
    String postBody = String.format(REQUEST_BODY_GPT_TURBO, InitialStrategyRequest);

//    System.out.println(postBody);

    String content = getResponseFromApi(postBody);
//    System.out.println(content);

    Pattern pattern = Pattern.compile("<strategy>(.*?)</strategy>", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(content);

    String strategy = "";
    if (matcher.find()) {
      strategy = matcher.group(1).trim();
    }

    strategy = fixFormatting(strategy);

    return strategy;
  }

  public static String getBestResponseStrategy(String strategy) throws IOException, InterruptedException {
    String bestResponse = "";
    strategy = strategy.replace("\n", "\\n");
    strategy = strategy.replace("\t", " ");

    String bestResponseStrategyRequest = mapDetails + DSL + DSLExplanation +
      "Now I have the following strategy that satisfies the DSL, written inside <strategy></strategy> tag:\\n" +
      "<strategy>\\n" + strategy + "</strategy>\\n\\n" +
      "Now your tasks are the following:\\n" +
      "1. Analyze this given strategy and try to analyze its weakness.\\n" +
      "2. Write a counter strategy that can dominate and easily defeat the given strategy.\\n" +
      "3. You need to only write the counter strategy inside <counterStrategy></counterStrategy> tag.";

    String postBody = String.format(REQUEST_BODY_GPT_TURBO, bestResponseStrategyRequest);
    String content = getResponseFromApi(postBody);

    Pattern pattern = Pattern.compile("<counterStrategy>(.*?)</counterStrategy>", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(content);

    if (matcher.find()) {
      bestResponse = matcher.group(1).trim();
    }

    bestResponse = fixFormatting(bestResponse);

    return bestResponse;
  }

  public static void main(String[] args) throws IOException, InterruptedException {
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
    System.out.println(getBestResponseStrategy(strategy));
  }
}
