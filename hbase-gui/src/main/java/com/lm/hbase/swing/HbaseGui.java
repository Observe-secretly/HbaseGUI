package com.lm.hbase.swing;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import com.lm.hbase.HbaseUtil;
import com.lm.hbase.tab.CreateTab;
import com.lm.hbase.tab.MetaDataTab;
import com.lm.hbase.tab.QueryTab;
import com.lm.hbase.tab.TabInterface;

public class HbaseGui {

    public JFrame frmHbaseGui;

    /**
     * Launch the application.
     * 
     * @wbp.parser.entryPoint
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {

                try {
                    // WebLookAndFeel.install();

                    com.lm.hbase.swing.SwingConstants.hbaseGui = new HbaseGui();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     * 
     * @wbp.parser.entryPoint
     */
    public HbaseGui(){
        // 弹出登陆框
        LoginGui.openDialog();
        // initialize();
    }

    /**
     * Initialize the contents of the frame.
     * 
     * @wbp.parser.entryPoint
     */
    public void initialize() {
        frmHbaseGui = new JFrame();
        frmHbaseGui.setTitle("Hbase Gui");
        frmHbaseGui.setBounds(10, 10, 1400, 800);
        frmHbaseGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmHbaseGui.getContentPane().setLayout(new BorderLayout(0, 0));
        frmHbaseGui.setResizable(false);// 禁止拉边框拉长拉短

        JPanel panel = new JPanel();
        frmHbaseGui.getContentPane().add(panel, BorderLayout.SOUTH);

        JLabel lblHBaSe = new JLabel("Message");
        panel.add(lblHBaSe);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        frmHbaseGui.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        TabInterface queryTab = new QueryTab(frmHbaseGui);
        tabbedPane.addTab(queryTab.getTitle(), queryTab.getIcon(), queryTab.getComponent(), queryTab.getTip());

        TabInterface metaDataTab = new MetaDataTab(frmHbaseGui);
        tabbedPane.addTab(metaDataTab.getTitle(), metaDataTab.getIcon(), metaDataTab.getComponent(),
                          metaDataTab.getTip());

        TabInterface createTab = new CreateTab(frmHbaseGui);
        tabbedPane.addTab(createTab.getTitle(), createTab.getIcon(), createTab.getComponent(), createTab.getTip());

        frmHbaseGui.setVisible(true);

        // 添加关闭事件
        frmHbaseGui.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    HbaseUtil.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                super.windowClosing(e);
            }

        });

    }

}
