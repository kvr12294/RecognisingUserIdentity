/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package AdminPack;

import MyPack.FilterRules;
import MyPack.RuleDB;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import sun.net.smtp.SmtpClient;

public class PostTweetFrm extends javax.swing.JFrame {
    
    MainForm parent;
    Connection con;
    DefaultComboBoxModel cm;
    DefaultListModel lm;
    Statement stmt;
    boolean found;
    Vector<String> ignorables;
    Vector<String> TagVector, smileyVector;
    Matcher matcher;
    Pattern patt;
    String parseResult = "";
    RuleDB singleDb;
    public Vector<FilterRules> allRules, smileyRules;
    public double finalScore = 0;
    String otp = "";
    
    public PostTweetFrm(MainForm parent) {
        this.parent = parent;
        initComponents();
        
        Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(sd.width / 2 - this.getWidth() / 2, sd.height / 2 - this.getHeight() / 2);
        fill_Rules_Vector();
        fill_Smiley_Rule();
        txtOTP.setEnabled(false);
        btnReTry.setEnabled(false);
        
    }
    
    public void fill_Smiley_Rule() {
        smileyRules = new Vector<FilterRules>();
        FilterRules ss = new FilterRules();
        ss.Rule = ":)";//Smile
        ss.ruleCount = 0;
        smileyRules.add(ss);
        
        ss = new FilterRules();
        ss.Rule = ":(";//Sad
        ss.ruleCount = 0;
        smileyRules.add(ss);
        
        ss = new FilterRules();
        ss.Rule = ":'(";//Crying
        ss.ruleCount = 0;
        smileyRules.add(ss);
        
        ss = new FilterRules();
        ss.Rule = ":@";//Angry
        ss.ruleCount = 0;
        smileyRules.add(ss);
        
        ss = new FilterRules();
        ss.Rule = ":D";//Show Teeth
        ss.ruleCount = 0;
        smileyRules.add(ss);
        
        ss = new FilterRules();
        ss.Rule = "(Y)";//Yes/Good
        ss.ruleCount = 0;
        smileyRules.add(ss);
        
        ss = new FilterRules();
        ss.Rule = "(N)";//No/Bad
        ss.ruleCount = 0;
        smileyRules.add(ss);
    }

    //Fill The Ignorables Vector
    public void fill_ignorables() {
        ignorables = new Vector<String>();
        ignorables.add("!");
        ignorables.add("@");
        ignorables.add("#");
        ignorables.add("$");
        ignorables.add("%");
        ignorables.add("^");
        ignorables.add("&");
        ignorables.add("*");
        ignorables.add("(");
        ignorables.add(")");
        ignorables.add("-");
        ignorables.add("+");
        ignorables.add("=");
        ignorables.add("_");
        ignorables.add("?");
        ignorables.add("/");
        ignorables.add("|");
        ignorables.add("*");
        ignorables.add("-");
        ignorables.add(",");
        ignorables.add(".");
        ignorables.add(":");
        ignorables.add("'");
        ignorables.add(";");
        ignorables.add("}");
        ignorables.add("{");
    }

    //Fill Rules Vector Whos Count Will Be Considered AND  All This Features Are Considered
    public void fill_Rules_Vector() {
        allRules = new Vector<FilterRules>();
        FilterRules singleRule = new FilterRules();
        singleRule.Rule = "!";
        singleRule.ruleCount = 0;
        allRules.add(singleRule);
        
        singleRule = new FilterRules();
        singleRule.Rule = "@";
        singleRule.ruleCount = 0;
        allRules.add(singleRule);
        
        singleRule = new FilterRules();
        singleRule.Rule = "$";
        singleRule.ruleCount = 0;
        allRules.add(singleRule);
        
        singleRule = new FilterRules();
        singleRule.Rule = "_";
        singleRule.ruleCount = 0;
        allRules.add(singleRule);
        
        singleRule = new FilterRules();
        singleRule.Rule = "URL";
        singleRule.ruleCount = 0;
        allRules.add(singleRule);
        
        singleRule = new FilterRules();
        singleRule.Rule = "CAPS";
        singleRule.ruleCount = 0;
        allRules.add(singleRule);
    }

    //Function Will Count The Presence Of Rules
    public void apply_filter() {
        String dispCnt = "";
        String dispFilterResult = "";
        String inputTweet = "";
        int totalWrdCnt = 0;
        TagVector = new Vector<String>();
        smileyVector = new Vector<String>();
        inputTweet = txtTweet.getText();
        //  System.out.println("Input: " + inputTweet);
        StringTokenizer st = new StringTokenizer(inputTweet, " ");
        while (st.hasMoreTokens()) {
            String temp = st.nextToken();
            if (temp.equals("")) {
                break;
            } else {
                totalWrdCnt++;
                for (int i = 0; i < allRules.size() - 2; i++) {
                    for (int j = 0; j < temp.length(); j++) {
                        if (temp.charAt(j) == allRules.get(i).Rule.charAt(0)) {
                            allRules.get(i).ruleCount += 1;
                        }
                    }
                }
            }
            //Check For Hash Tag
            String re = "#[a-zA-Z]+";
            patt = Pattern.compile(re);
            matcher = patt.matcher(temp);
            found = matcher.matches();
            if (found) {
                // allRules.get(allRules.size() - 2).ruleCount += 1;
                //      System.out.println("Found #Tag: " + temp);
                TagVector.add(temp);
            }

            //Check Capital Letters
            for (int i = 0; i < temp.length(); i++) {
                int asc = (int) temp.charAt(i);
                if (asc >= 65 && asc <= 90) {
                    allRules.get(allRules.size() - 1).ruleCount += 1;
                }
            }
            //Check URL
            String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
            patt = Pattern.compile(regex);
            matcher = patt.matcher(temp);
            found = matcher.matches();
            if (found) {
                allRules.get(allRules.size() - 2).ruleCount += 1;
            }

            //Check For Smilies
            for (int i = 0; i < smileyRules.size(); i++) {
                String smiley = smileyRules.get(i).Rule;
                if (temp.contains(smiley)) {
                    //smileyRules.get(i).ruleCount += 1;
                    smileyVector.add(smiley);
                    //    System.out.println("Found Smiley: " + smiley);
                }
            }
        }
        
        dispCnt += "Total Tokens: " + totalWrdCnt + "\r\n";
        for (int i = 0; i < allRules.size(); i++) {
            dispCnt += "Rule: '" + allRules.get(i).Rule + "' Count: " + allRules.get(i).ruleCount + "\r\n";
        }
        txtCountStatus.setText(dispCnt);
    }

    //function Will Filter And Remove Ignorables Fromt Tweet
    public String apply_preProcessing() {
        fill_ignorables();
        String finalString = "";
        boolean found = false;
        String inpString = txtTweet.getText();
        StringTokenizer st = new StringTokenizer(inpString, " ");
        while (st.hasMoreTokens()) {
            finalString += " ";
            String temp = st.nextToken();
            if (temp.equals("")) {
                break;
            } else {
                for (int i = 0; i < temp.length(); i++) {
                    found = false;
                    for (int j = 0; j < ignorables.size(); j++) {
                        if (temp.charAt(i) == ignorables.get(j).charAt(0)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        finalString += temp.charAt(i);
                    }
                }
            }
        }
        //  System.out.println("Proccessed String: " + finalString);
        return finalString;
        
    }

    //Normalize And Store The Features
    public boolean normalize_Features() {
        //parent.ruleDB = new Vector<RuleDB>();
        boolean status = false;
        parent.readDatabase();
        //  System.out.println("Size of DB: " + parent.ruleDB.size());
        singleDb = new RuleDB();
        for (int i = 0; i < allRules.size(); i++) {
            singleDb.normalizeFeatures.add(0);
        }
        for (int i = 0; i < allRules.size(); i++) {
            //System.out.println("   Rule:  " + allRules.get(i).Rule + "  Count: " + allRules.get(i).ruleCount);
            if (allRules.get(i).ruleCount > 0) {
                singleDb.normalizeFeatures.set(i, 1);
                
            } else {
                singleDb.normalizeFeatures.set(i, 0);
            }
        }
        for (int i = 0; i < TagVector.size(); i++) {
            //  System.out.println("Adding: " + TagVector.get(i));
            singleDb.TagVector.add(TagVector.get(i));
        }
        if (parent.ruleDB.size() <= 0) {
            System.out.println("Tweet For First Time");
            parent.ruleDB.add(singleDb);
            parent.writeDatabase();
        } else {
            
            status = apply_Check();
        }
        
        return status;
    }

    //First Check For #Tags if Correct Then Allow To Tweet else check for Smileys used
    public boolean apply_Check() {
        // parent.readDatabase();
        boolean tagFound = false;
        boolean status = false;
        //First Check if Size Of Atg Vector Zero Directlly Switch to smiley Check
        if (TagVector.size() > 0) {
            for (int i = 0; i < parent.ruleDB.size(); i++) {
                for (int k = 0; k < TagVector.size(); k++) {
                    for (int j = 0; j < parent.ruleDB.get(i).TagVector.size(); j++) {
                        //  System.out.println("DB TAG: " + parent.ruleDB.get(i).TagVector.get(j) + "  Current: " + TagVector.get(k));
                        if (TagVector.get(k).equals(parent.ruleDB.get(i).TagVector.get(j))) {
                            tagFound = true;
                        }
                    }
                }
            }
        }
        if (!tagFound) {
            // System.out.println("Invalid Tweet");
            status = check_Smileys();//Call To Check Smiley Function
        } else {
            //System.out.println("Valid Tweet");
            return true;
            
        }
        return status;
    }

    //Function Will Check For Valid Smileys
    public boolean check_Smileys() {
        boolean smileyFound = false;
        if (smileyVector.size() > 0) {
            for (int j = 0; j < smileyVector.size(); j++) {
                for (int k = 0; k < smileyRules.size(); k++) {
                    if (smileyRules.get(k).Rule.equals(smileyVector.get(j))) {
                        smileyFound = true;
                    }
                }
            }
        }
        
        if (!smileyFound) {
            //  System.out.println("Invalid Smiley");
            boolean status = apply_Similarity();
            return status;
        } else {
            //  System.out.println("Valid Smiley");
            return true;
        }
        
    }

    //Function Apply Smilarity Measures And check For Features
    public boolean apply_Similarity() {
        Vector<Integer> tempVector = new Vector<Integer>();
        for (int i = 0; i < allRules.size(); i++) {
            tempVector.add(0);
        }
        if (parent.ruleDB.size() > 0) {
            //Obtain Summation Of All Tweets
            for (int i = 0; i < parent.ruleDB.size(); i++) {
                for (int j = 0; j < parent.ruleDB.get(i).normalizeFeatures.size(); j++) {
                    int tt = tempVector.get(j);
                    tempVector.set(j, (tt + parent.ruleDB.get(i).normalizeFeatures.get(j)));
                }
            }

            //Normalize To 1 and 0
            for (int i = 0; i < tempVector.size(); i++) {
                
                if (tempVector.get(i) > 0) {
                    tempVector.set(i, 1);
                } else {
                    tempVector.set(i, 0);
                }
            }
            for (int i = 0; i < tempVector.size(); i++) {
                System.out.print("   " + tempVector.get(i));
            }
            System.out.println();
            finalScore = 0;
            double sum_xy = 0;
            double sum_x2 = 0;
            double sum_y2 = 0;
            for (int i = 0; i < tempVector.size(); i++) {
                sum_xy += (tempVector.get(i) * singleDb.normalizeFeatures.get(i));
                sum_x2 += Math.pow(tempVector.get(i), 2);
                sum_y2 += Math.pow(singleDb.normalizeFeatures.get(i), 2);
            }
            if (sum_x2 == 0 || sum_y2 == 0) {
                finalScore = 0;
            } else {
                finalScore = sum_xy / Math.sqrt(sum_x2 * sum_y2);
            }
            System.out.println("Fianl Score: " + finalScore);
            if (finalScore >= 0.5) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
        
    }
    
    public String generate_OTP() {
        String smsOTP = "";
        Random r = new Random();
        for (int i = 0; i < 5; i++) {
            smsOTP += "" + (Math.abs(r.nextInt()) % 10);
        }
        System.out.println("OTP GENERATED: " + smsOTP + " Sending ");
        return smsOTP;
    }
    
    String sendMail(String mailTo, String contents) {
        String ret = "Mail Sent!";
        try {
            // from and to
            SmtpClient sc = new SmtpClient("smtp.net4india.com");
            sc.from("smtpsender@myprojectspace.co.in");
            sc.to(mailTo);
            // open a socket connection to server for communication
            PrintStream ps = sc.startMessage();

            // additional headers, subject et al.
            ps.println("From: " + "smtpsender@myprojectspace.co.in");
            ps.println("To: " + mailTo);
            ps.println("Subject: " + "PASSWORD");
            // blank line separates thel headers and message
            ps.println();
            ps.println(contents);
            sc.closeServer();
            System.out.println("Mail Sent Successfully!");
        } catch (IOException e) {
            // Should really put up a dialog box informing user of the error
            System.out.println("Error Sending EMail!");
            ret = "Error Sending EMail : " + e.getMessage();
            System.err.println(e);
        }
        return ret;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtTweet = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtCountStatus = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        btnSubmit = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtOTP = new javax.swing.JTextField();
        btnReTry = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        jPanel1.setAutoscrolls(true);

        new JavaLib.LoadForm();
        jLabel1.setFont(new java.awt.Font("Andalus", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(51, 0, 204));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("POST YOUR TWEET ");
        jLabel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel1.setOpaque(true);

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel2.setForeground(new java.awt.Color(153, 153, 153));

        jLabel3.setFont(new java.awt.Font("Andalus", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(51, 0, 204));
        jLabel3.setText("ENTER TWEET");
        jLabel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        txtTweet.setColumns(20);
        txtTweet.setRows(5);
        txtTweet.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtTweet.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtTweetKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(txtTweet);

        jPanel3.setBackground(new java.awt.Color(0, 0, 0));
        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        txtCountStatus.setBackground(new java.awt.Color(51, 51, 51));
        txtCountStatus.setColumns(20);
        txtCountStatus.setFont(new java.awt.Font("Calibri", 1, 14)); // NOI18N
        txtCountStatus.setForeground(new java.awt.Color(204, 204, 204));
        txtCountStatus.setRows(5);
        txtCountStatus.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane3.setViewportView(txtCountStatus);

        jLabel5.setFont(new java.awt.Font("Aparajita", 1, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 255, 0));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("COUNT STATUS");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        new JavaLib.LoadForm();
        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        btnSubmit.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
        btnSubmit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ImgPack/twe.png"))); // NOI18N
        btnSubmit.setBorder(null);
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Andalus", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 0, 204));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("ENTER OTP");

        txtOTP.setFont(new java.awt.Font("Calibri", 1, 14)); // NOI18N

        btnReTry.setFont(new java.awt.Font("Calibri", 1, 14)); // NOI18N
        btnReTry.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ImgPack/re.png"))); // NOI18N
        btnReTry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReTryActionPerformed(evt);
            }
        });

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ImgPack/tt.png"))); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnReTry, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(txtOTP)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSubmit)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtOTP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnReTry, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(209, 209, 209))
        );

        jButton3.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ImgPack/back.png"))); // NOI18N
        jButton3.setBorder(null);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(24, 24, 24))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(97, 97, 97)
                .addComponent(jButton3)
                .addContainerGap(99, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        this.setVisible(false);
        parent.setVisible(true);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        // TODO add your handling code here:
        parseResult = "";
        TagVector = new Vector<String>();
        apply_filter();
        boolean status = normalize_Features();
        if (status) {
            System.out.println("Identity Proved Tweet Posted Successfully");
            parent.ruleDB.add(singleDb);
            parent.writeDatabase();
            SingleSentense.TweetData = txtTweet.getText();
            boolean tweet_status = NamexTweet.fetch_tweet();
            if (tweet_status) {
                JOptionPane.showMessageDialog(this, "Identity Proved Tweet Posted Successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Error Encountered While Posting");                
            }
            
        } else {
            System.out.println("Invalid Tweet");
            otp = generate_OTP();
            String email_Contents = "OTP: " + otp;
            sendMail(SingleSentense.eamilId, email_Contents);
            txtOTP.setEnabled(true);
            btnReTry.setEnabled(true);
            JOptionPane.showMessageDialog(this, "User Failed Too Prove Identity Try By Entering OTP ");
        }
        

    }//GEN-LAST:event_btnSubmitActionPerformed

    private void btnReTryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReTryActionPerformed
        // TODO add your handling code here:
        try {
            if (txtOTP.getText().equals(otp)) {
                SingleSentense.TweetData = txtTweet.getText();
                NamexTweet.fetch_tweet();
                JOptionPane.showMessageDialog(this, "Identity Proved Tweet Posted Successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Identity Failed Cannot Post Tweet");
                txtTweet.setText("");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
        }

    }//GEN-LAST:event_btnReTryActionPerformed

    private void txtTweetKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTweetKeyTyped
        // TODO add your handling code here:
        txtOTP.setEnabled(false);
        btnReTry.setEnabled(false);
    }//GEN-LAST:event_txtTweetKeyTyped
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnReTry;
    private javax.swing.JButton btnSubmit;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea txtCountStatus;
    private javax.swing.JTextField txtOTP;
    private javax.swing.JTextArea txtTweet;
    // End of variables declaration//GEN-END:variables
}
