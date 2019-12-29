/* Generated By:JavaCC: Do not edit this line. plan_parser.java */
import java.util.ArrayList;
import alice.tuprolog.*;
import java.io.*;
public class plan_parser implements plan_parserConstants {
 public static void main(String args[]) throws ParseException, FileNotFoundException {
    plan_parser parser = new plan_parser(new FileInputStream("./src/PlanParserTest1.txt"));
    Plan plan = parser.plan();
    System.out.println(plan.toString());
 }

  final public PlanBase plans() throws ParseException {
    Plan buffer;
    ArrayList<Plan> plans = new ArrayList<Plan>();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LBRACE:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      buffer = plan();
                      plans.add(buffer);
    }
     {if (true) return new PlanBase(plans);}
    throw new Error("Missing return statement in function");
  }

  final public Plan plan() throws ParseException {
    SeqPlan plan;
    jj_consume_token(LBRACE);
    plan = seqPlan();
    jj_consume_token(RBRACE);
                                        {if (true) return new Plan(plan);}
    throw new Error("Missing return statement in function");
  }

  final public SeqPlan seqPlan() throws ParseException {
    BasicPlan buffer;
    ArrayList<BasicPlan> components = new ArrayList<BasicPlan>();
    label_2:
    while (true) {
      buffer = basicplan();
                            components.add(buffer);
      jj_consume_token(SEMICOL);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IF:
      case WHILE:
      case SEND:
      case JAVA:
      case LPAR:
      case TRUE:
      case NOT:
      case VAL:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_2;
      }
    }
                                                                  {if (true) return new SeqPlan(components);}
    throw new Error("Missing return statement in function");
  }

  final public BasicPlan basicplan() throws ParseException {
    Query testbuffer;
    ArrayList<Atom> arguments = new ArrayList<Atom>();
    ArrayList<BasicPlan> components = new ArrayList<BasicPlan>();
    BasicPlan buffer;
    Atom argbuffer;
    Token t;
    SeqPlan ifPlan;
    SeqPlan elsePlan;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case JAVA:
      jj_consume_token(JAVA);
      jj_consume_token(LPAR);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case VAL:
      case VAR:
        argbuffer = atom();
                                         arguments.add(argbuffer);
        label_3:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case COMMA:
            ;
            break;
          default:
            jj_la1[2] = jj_gen;
            break label_3;
          }
          jj_consume_token(COMMA);
          argbuffer = atom();
                                                                                                 arguments.add(argbuffer);
        }
        break;
      default:
        jj_la1[3] = jj_gen;
        ;
      }
      jj_consume_token(RPAR);
                                                                                                                                        {if (true) return new JavaAction(arguments);}
      break;
    default:
      jj_la1[9] = jj_gen;
      if (jj_2_1(2147483647)) {
        testbuffer = query();
        jj_consume_token(QUSMARK);
                                                                               {if (true) return new TestAction(testbuffer);}
      } else {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case SEND:
          jj_consume_token(SEND);
          jj_consume_token(LPAR);
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case VAL:
          case VAR:
            argbuffer = atom();
                                           arguments.add(argbuffer);
            label_4:
            while (true) {
              switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
              case COMMA:
                ;
                break;
              default:
                jj_la1[4] = jj_gen;
                break label_4;
              }
              jj_consume_token(COMMA);
              argbuffer = atom();
                                                                                                   arguments.add(argbuffer);
            }
            break;
          default:
            jj_la1[5] = jj_gen;
            ;
          }
          jj_consume_token(RPAR);
                                                                                                                                         {if (true) return new SendAction(arguments);}
          break;
        case VAL:
          t = jj_consume_token(VAL);
          jj_consume_token(LPAR);
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case VAL:
          case VAR:
            argbuffer = atom();
                                               arguments.add(argbuffer);
            label_5:
            while (true) {
              switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
              case COMMA:
                ;
                break;
              default:
                jj_la1[6] = jj_gen;
                break label_5;
              }
              jj_consume_token(COMMA);
              argbuffer = atom();
                                                                                                       arguments.add(argbuffer);
            }
            break;
          default:
            jj_la1[7] = jj_gen;
            ;
          }
          jj_consume_token(RPAR);
                                                                                                                                              {if (true) return new CapAction(t.image, arguments);}
          break;
        case WHILE:
          jj_consume_token(WHILE);
          jj_consume_token(LPAR);
          testbuffer = query();
          jj_consume_token(RPAR);
          jj_consume_token(LBRACE);
          label_6:
          while (true) {
            buffer = basicplan();
                                                                                  components.add(buffer);
            jj_consume_token(SEMICOL);
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case IF:
            case WHILE:
            case SEND:
            case JAVA:
            case LPAR:
            case TRUE:
            case NOT:
            case VAL:
              ;
              break;
            default:
              jj_la1[8] = jj_gen;
              break label_6;
            }
          }
          jj_consume_token(RBRACE);
                                                                                                                                 {if (true) return new WhilePlan(components, testbuffer);}
          break;
        case IF:
          jj_consume_token(IF);
          jj_consume_token(LPAR);
          testbuffer = query();
          jj_consume_token(RPAR);
          jj_consume_token(LBRACE);
          ifPlan = seqPlan();
          jj_consume_token(RBRACE);
          jj_consume_token(ELSE);
          jj_consume_token(LBRACE);
          elsePlan = seqPlan();
          jj_consume_token(RBRACE);
                                                                                                                                   {if (true) return new IfPlan(ifPlan, elsePlan, testbuffer);}
          break;
        default:
          jj_la1[10] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
    }
    throw new Error("Missing return statement in function");
  }

  final public ArrayList<Literal> literals() throws ParseException {
    Literal buffer;
    ArrayList<Literal> result = new ArrayList<Literal>();
    buffer = literal();
                        result.add(buffer);
    label_7:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
        ;
        break;
      default:
        jj_la1[11] = jj_gen;
        break label_7;
      }
      jj_consume_token(COMMA);
      buffer = literal();
                                                                          result.add(buffer);
    }
     {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  final public Query query() throws ParseException {
    Query sub0;
    Query sub1;
    boolean isOr;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case TRUE:
    case NOT:
    case VAL:
      sub0 = singlequery();
                           {if (true) return sub0;}
      break;
    case LPAR:
      jj_consume_token(LPAR);
      sub0 = query();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OR:
        jj_consume_token(OR);
        sub1 = query();
        jj_consume_token(RPAR);
                                                           {if (true) return new wffBinary(true, sub0, sub1);}
        break;
      case AND:
        jj_consume_token(AND);
        sub1 = query();
        jj_consume_token(RPAR);
                                  {if (true) return new wffBinary(false, sub0, sub1);}
        break;
      default:
        jj_la1[12] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[13] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public Query singlequery() throws ParseException {
    Query query;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case TRUE:
      query = truequery();
                          {if (true) return query;}
      break;
    case NOT:
    case VAL:
      query = literal();
                                                                {if (true) return query;}
      break;
    default:
      jj_la1[14] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public TrueQuery truequery() throws ParseException {
    jj_consume_token(TRUE);
            {if (true) return new TrueQuery();}
    throw new Error("Missing return statement in function");
  }

  final public Literal literal() throws ParseException {
    VpredClause clause;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VAL:
      clause = vpredclause();
                             {if (true) return new Literal(false, clause);}
      break;
    case NOT:
      jj_consume_token(NOT);
      clause = vpredclause();
                                                                                                   {if (true) return new Literal(true, clause);}
      break;
    default:
      jj_la1[15] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public GpredClause gpredclause() throws ParseException {
    Token t;
    ArrayList<Atom> arguments = new ArrayList<Atom>();
    Atom buffer;
    t = jj_consume_token(VAL);
    jj_consume_token(LPAR);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VAL:
      buffer = val();
                                      arguments.add(buffer);
      break;
    default:
      jj_la1[16] = jj_gen;
      ;
    }
    label_8:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
        ;
        break;
      default:
        jj_la1[17] = jj_gen;
        break label_8;
      }
      jj_consume_token(COMMA);
      buffer = val();
                                                                                          arguments.add(buffer);
    }
    jj_consume_token(RPAR);
     {if (true) return new GpredClause(t.image, arguments);}
    throw new Error("Missing return statement in function");
  }

  final public VpredClause vpredclause() throws ParseException {
    Token t;
    ArrayList<Atom> arguments = new ArrayList<Atom>();
    Atom buffer;
    t = jj_consume_token(VAL);
    jj_consume_token(LPAR);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VAL:
    case VAR:
      buffer = atom();
                                       arguments.add(buffer);
      break;
    default:
      jj_la1[18] = jj_gen;
      ;
    }
    label_9:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
        ;
        break;
      default:
        jj_la1[19] = jj_gen;
        break label_9;
      }
      jj_consume_token(COMMA);
      buffer = atom();
                                                                                            arguments.add(buffer);
    }
    jj_consume_token(RPAR);
     {if (true) return new VpredClause(t.image, arguments);}
    throw new Error("Missing return statement in function");
  }

  final public Atom val() throws ParseException {
    Token t;
    t = jj_consume_token(VAL);
              {if (true) return new Atom(t.image);}
    throw new Error("Missing return statement in function");
  }

  final public Atom var() throws ParseException {
    Token t;
    t = jj_consume_token(VAR);
              {if (true) return new Atom(t.image);}
    throw new Error("Missing return statement in function");
  }

  final public Atom atom() throws ParseException {
    Atom a;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VAR:
      a = var();
                {if (true) return a;}
      break;
    case VAL:
      a = val();
                                         {if (true) return a;}
      break;
    default:
      jj_la1[20] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_3R_24() {
    if (jj_scan_token(COMMA)) return true;
    if (jj_3R_25()) return true;
    return false;
  }

  private boolean jj_3R_18() {
    if (jj_scan_token(TRUE)) return true;
    return false;
  }

  private boolean jj_3R_29() {
    if (jj_scan_token(VAL)) return true;
    return false;
  }

  private boolean jj_3R_16() {
    if (jj_3R_18()) return true;
    return false;
  }

  private boolean jj_3R_13() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_16()) {
    jj_scanpos = xsp;
    if (jj_3R_17()) return true;
    }
    return false;
  }

  private boolean jj_3R_27() {
    if (jj_3R_29()) return true;
    return false;
  }

  private boolean jj_3R_17() {
    if (jj_3R_19()) return true;
    return false;
  }

  private boolean jj_3R_22() {
    if (jj_scan_token(VAL)) return true;
    if (jj_scan_token(LPAR)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_23()) jj_scanpos = xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_24()) { jj_scanpos = xsp; break; }
    }
    if (jj_scan_token(RPAR)) return true;
    return false;
  }

  private boolean jj_3R_12() {
    if (jj_scan_token(LPAR)) return true;
    if (jj_3R_10()) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_14()) {
    jj_scanpos = xsp;
    if (jj_3R_15()) return true;
    }
    return false;
  }

  private boolean jj_3R_15() {
    if (jj_scan_token(AND)) return true;
    if (jj_3R_10()) return true;
    if (jj_scan_token(RPAR)) return true;
    return false;
  }

  private boolean jj_3R_11() {
    if (jj_3R_13()) return true;
    return false;
  }

  private boolean jj_3R_10() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_11()) {
    jj_scanpos = xsp;
    if (jj_3R_12()) return true;
    }
    return false;
  }

  private boolean jj_3R_21() {
    if (jj_scan_token(NOT)) return true;
    if (jj_3R_22()) return true;
    return false;
  }

  private boolean jj_3R_26() {
    if (jj_3R_28()) return true;
    return false;
  }

  private boolean jj_3R_25() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_26()) {
    jj_scanpos = xsp;
    if (jj_3R_27()) return true;
    }
    return false;
  }

  private boolean jj_3R_19() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_20()) {
    jj_scanpos = xsp;
    if (jj_3R_21()) return true;
    }
    return false;
  }

  private boolean jj_3R_20() {
    if (jj_3R_22()) return true;
    return false;
  }

  private boolean jj_3R_14() {
    if (jj_scan_token(OR)) return true;
    if (jj_3R_10()) return true;
    if (jj_scan_token(RPAR)) return true;
    return false;
  }

  private boolean jj_3_1() {
    if (jj_3R_10()) return true;
    if (jj_scan_token(QUSMARK)) return true;
    if (jj_scan_token(SEMICOL)) return true;
    return false;
  }

  private boolean jj_3R_28() {
    if (jj_scan_token(VAR)) return true;
    return false;
  }

  private boolean jj_3R_23() {
    if (jj_3R_25()) return true;
    return false;
  }

  /** Generated Token Manager. */
  public plan_parserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[21];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_init_0();
      jj_la1_init_1();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x10000,0x38300000,0x2000,0x0,0x2000,0x0,0x2000,0x0,0x38300000,0x10000000,0x8300000,0x2000,0xc0000,0x20000000,0x0,0x0,0x0,0x2000,0x0,0x2000,0x0,};
   }
   private static void jj_la1_init_1() {
      jj_la1_1 = new int[] {0x0,0x26,0x0,0x60,0x0,0x60,0x0,0x60,0x26,0x0,0x20,0x0,0x0,0x26,0x26,0x24,0x20,0x0,0x60,0x0,0x60,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[1];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public plan_parser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public plan_parser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new plan_parserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 21; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 21; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public plan_parser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new plan_parserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 21; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 21; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public plan_parser(plan_parserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 21; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(plan_parserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 21; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[39];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 21; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 39; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 1; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
