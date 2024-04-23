import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

// 按两次 Shift 打开“随处搜索”对话框并输入 `show whitespaces`，
// 然后按 Enter 键。现在，您可以在代码中看到空格字符。
public class lr1_analyser extends JFrame {
    JSplitPane splitPane;
    JSplitPane splitPane1;
    JTextArea textArea;
    JTable table;
    JScrollPane scrollPane;
    JScrollPane scrollPane1;
    DefaultTableModel dtm;
    JButton button1;
    JButton button2;
    JPanel panel1;
    public Stack<Integer> StateStack;
    public Stack<Character> SigStack;
    public Stack<Character> InputStack;
    public Integer Step;
    public Vector<Printer> printer = new Vector<Printer>();
    public String TextInput;
    public Integer[] State = {0,1,2,3,4,5,6,7,8,9,10,11};
    public Character[] VtList = {'i','+','*','(',')','#'};
    public Character[] VnList = {'E','T','F'};
    public Map<Integer,Integer> StateMap = new HashMap<>();
    public Map<Character,Integer> VtMap = new HashMap<>();
    public Map<Character,Integer> VnMap = new HashMap<>();
    String [][] Action;
    Integer [][] GoTo;
    public String[] G= new String[]{"","E+T","T","T*F","F","(E)","i"};
    public Character[] Gc = new Character[]{',','E','E','T','T','F','F'};
    //构造函数
    public lr1_analyser(){
        //初始化State、Vt、Vn
        Action = new String[State.length][VtList.length];
        GoTo = new Integer[State.length][VnList.length];
        for(int i = 0;i<State.length;i++){
            for(int j = 0;j<VtList.length;j++){
                Action[i][j] = null;
            }
        }
        for(int i = 0;i<State.length;i++){
            for(int j = 0;j<VnList.length;j++){
                GoTo[i][j] = null;
            }
        }
        //Action表的建立
        Action[0][0]="S5";
        Action[0][3]="S4";
        Action[1][1]="S6";
        Action[1][5]="acc";
        Action[2][1]="r2";
        Action[2][2]="S7";
        Action[2][4]="r2";
        Action[2][5]="r2";
        Action[3][1]="r4";
        Action[3][2]="r4";
        Action[3][4]="r4";
        Action[3][5]="r4";
        Action[4][0]="S5";
        Action[4][3]="S4";
        Action[5][1]="r6";
        Action[5][2]="r6";
        Action[5][4]="r6";
        Action[5][5]="r6";
        Action[6][0]="S5";
        Action[6][3]="S4";
        Action[7][0]="S5";
        Action[7][3]="S4";
        Action[8][1]="S6";
        Action[8][4]="S11";
        Action[9][1]="r1";
        Action[9][2]="S7";
        Action[9][4]="r1";
        Action[9][5]="r1";
        Action[10][1]="r3";
        Action[10][2]="r3";
        Action[10][4]="r3";
        Action[10][5]="r3";
        Action[11][1]="r5";
        Action[11][2]="r5";
        Action[11][4]="r5";
        Action[11][5]="r5";
        //GoTo表的建立
        GoTo[0][0]=1;
        GoTo[0][1]=2;
        GoTo[0][2]=3;
        GoTo[4][0]=8;
        GoTo[4][1]=2;
        GoTo[4][2]=3;
        GoTo[6][1]=9;
        GoTo[6][2]=3;
        GoTo[7][2]=10;
        

        for (int i=0;i<State.length;i++) {
            StateMap.put(State[i],i);
        }
        for (int i=0;i<VtList.length;i++) {
            VtMap.put(VtList[i],i);
        }
        for(int i=0;i<VnList.length;i++) {
            VnMap.put(VnList[i],i);
        }
        //设置区域
        setTitle("LL(1)分析");
        setBounds(500,400,1000,700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        //输入区域
        textArea = new JTextArea();
        textArea.setFont(new Font("宋体",Font.BOLD,20));
        scrollPane1 = new JScrollPane(textArea);
        splitPane1.setTopComponent(scrollPane1);
        splitPane1.setDividerLocation(200);
        //按钮区域
        panel1 = new JPanel();
        panel1.setLayout(null);
        //确认按钮
        button1 = new JButton("确认");
        button1.setBounds(500,30,100,50);
        panel1.add(button1);
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                init();
                processor();
                out();
            }
        });

        //清空按钮
        button2 = new JButton("清空");
        button2.setBounds(350,30,100,50);
        panel1.add(button2);
        splitPane1.setBottomComponent(panel1);
        splitPane.setTopComponent(splitPane1);
        button2.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                textArea.setText("");
            }
        });
        //表格区域
        String[] columnsNames = {"步骤","状态栈","符号栈","输入串","动作说明"};
        dtm = new DefaultTableModel(null,columnsNames);
        table = new JTable(dtm);
        table.setFont(new Font("宋体",Font.BOLD,18));
        table.setRowHeight(20);
        scrollPane = new JScrollPane(table);
        splitPane.setBottomComponent(scrollPane);
        splitPane.setDividerLocation(300);
        add(splitPane);

        //设置输入串部分右对齐
        TableColumn column = table.getColumnModel().getColumn(2);
        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(SwingConstants.LEFT);
        column.setCellRenderer(render);

        TableColumn column1 = table.getColumnModel().getColumn(0);
        DefaultTableCellRenderer render1 = new DefaultTableCellRenderer();
        render1.setHorizontalAlignment(SwingConstants.CENTER);
        column1.setCellRenderer(render1);

        TableColumn column2 = table.getColumnModel().getColumn(1);
        DefaultTableCellRenderer render2 = new DefaultTableCellRenderer();
        render2.setHorizontalAlignment(SwingConstants.LEFT);
        column2.setCellRenderer(render2);

        TableColumn column3 = table.getColumnModel().getColumn(3);
        DefaultTableCellRenderer render3 = new DefaultTableCellRenderer();
        render3.setHorizontalAlignment(SwingConstants.RIGHT);
        column3.setCellRenderer(render3);

        TableColumn column4 = table.getColumnModel().getColumn(4);
        DefaultTableCellRenderer render4 = new DefaultTableCellRenderer();
        render4.setHorizontalAlignment(SwingConstants.CENTER);
        column4.setCellRenderer(render4);
    }
    //初始化
    public void init(){
        if(StateStack!=null)
            StateStack.clear();
        else
            StateStack = new Stack<>();
        if(SigStack!=null)
            SigStack.clear();
        else
            SigStack = new Stack<>();
        if(InputStack!=null)
            InputStack.clear();
        else
            InputStack = new Stack<>();
        printer.clear();
        dtm.setRowCount(0);
        StateStack.push(0);
        InputStack.push('#');
        TextInput = textArea.getText().trim();
        TextInput = TextInput.replace("\\n","");
        for(int i=TextInput.length()-1;i>=0;i--){
            InputStack.push(TextInput.charAt(i));
        }
        SigStack.push('#');
        Step = 0;
    }
    public void processor(){
        while(!SigStack.empty()){
            Step++;
            char VNtop = SigStack.peek();
            char Iptop = InputStack.peek();
            Integer StateTop = StateStack.peek();
            //输入非终结符
            if(!VtMap.containsKey(Iptop)){
                String st = getStateStack();
                String vn = getSigStack();
                String vt = getInputStack();
                printer.add(new Printer(Step,st,vn,vt,"Input Error!"));
                break;
            }
            if(Action[StateMap.get(StateTop)][VtMap.get(Iptop)]==null){
                String st = getStateStack();
                String vn = getSigStack();
                String vt = getInputStack();
                printer.add(new Printer(Step,st,vn,vt,"Error!表达式错误！"));
                break;
            }
            //分析成功
            else if(Action[StateMap.get(StateTop)][VtMap.get(Iptop)].equals("acc")){
                String st = getStateStack();
                String vn = getSigStack();
                String vt = getInputStack();
                printer.add(new Printer(Step,st,vn,vt,"Acc: 分析成功！"));
                break;
            }
            //进栈
            else if(Action[StateMap.get(StateTop)][VtMap.get(Iptop)].charAt(0)=='S'){
                String st = getStateStack();
                String vn = getSigStack();
                String vt = getInputStack();
                printer.add(new Printer(Step,st,vn,vt,"ACTION["+StateTop.toString()+","+Iptop+"]="+Action[StateMap.get(StateTop)][VtMap.get(Iptop)]+",状态"+Action[StateMap.get(StateTop)][VtMap.get(Iptop)].charAt(1)+"进栈"));
                StateStack.push(Action[StateMap.get(StateTop)][VtMap.get(Iptop)].charAt(1)-'0');
                SigStack.push(Iptop);
                InputStack.pop();
            }
            //归约
            else if(Action[StateMap.get(StateTop)][VtMap.get(Iptop)].charAt(0)=='r'){
                String st = getStateStack();
                String vn = getSigStack();
                String vt = getInputStack();


                Integer cnt = G[Action[StateMap.get(StateTop)][VtMap.get(Iptop)].charAt(1)-'0'].length();
                for(int l = 0; l < cnt; l++){
                    SigStack.pop();
                    StateStack.pop();
                }
                if(StateStack.empty()){
                    printer.add(new Printer(Step,st,vn,vt,"Error!表达式错误！"));
                    break;
                }
                Integer sTop = StateStack.peek();
                if(GoTo[StateMap.get(sTop)][VnMap.get(Gc[Action[StateMap.get(StateTop)][VtMap.get(Iptop)].charAt(1)-'0'])]==null){
                    printer.add(new Printer(Step,st,vn,vt,"Error!表达式错误！"));
                    break;
                }
                StateStack.push(GoTo[StateMap.get(sTop)][VnMap.get(Gc[Action[StateMap.get(StateTop)][VtMap.get(Iptop)].charAt(1)-'0'])]);
                SigStack.push(Gc[Action[StateMap.get(StateTop)][VtMap.get(Iptop)].charAt(1)-'0']);
                printer.add(new Printer(Step,st,vn,vt,Action[StateMap.get(StateTop)][VtMap.get(Iptop)]+":"+Gc[Action[StateMap.get(StateTop)][VtMap.get(Iptop)].charAt(1)-'0']+"->"+G[Action[StateMap.get(StateTop)][VtMap.get(Iptop)].charAt(1)-'0']+"归约，GOTO("+sTop.toString()+","+SigStack.peek()+")="+GoTo[StateMap.get(sTop)][VnMap.get(Gc[Action[StateMap.get(StateTop)][VtMap.get(Iptop)].charAt(1)-'0'])]+"入栈"));
            }
            else{
                String st = getStateStack();
                String vn = getSigStack();
                String vt = getInputStack();
                printer.add(new Printer(Step,st,vn,vt,"Error!"));
                break;
            }
        }
    }
    //制作表格
    public void out(){
        for(var i:printer){
            Object[]o={i.step(),i.staStack(),i.sigStack(),i.inputStack(),i.action()};
            dtm.addRow(o);
        }
    }
    //get状态栈String
    public String getStateStack(){
        Iterator value = StateStack.iterator();
        StringBuilder s = new StringBuilder();
        while(value.hasNext()){
            s.append(value.next().toString());
        }
        return s.toString();
    }
    //get符号栈String
    public String getSigStack(){
        Iterator value = SigStack.iterator();
        StringBuilder s = new StringBuilder();
        while(value.hasNext()){
            s.append(value.next().toString());
        }
        return s.toString();
    }
    //get输入串String
    public String getInputStack(){
        Iterator value = InputStack.iterator();
        StringBuilder s = new StringBuilder();
        while(value.hasNext()){
            s.append(value.next().toString());
        }
        return s.reverse().toString();
    }
    //主程序
    public static void main(String[] args) {
        lr1_analyser a = new lr1_analyser();
        a.setVisible(true);
    }
}
record Printer(Integer step, String staStack, String sigStack, String inputStack, String action){}