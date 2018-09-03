package com.lm.hbase.swing;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.hadoop.hbase.ClusterStatus;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import com.lm.hbase.HbaseUtil;

public class LoginGui extends JDialog {

    private JPanel     contentPanel = new JPanel();
    private JTextField textField;
    private JTextField textField_1;
    private JTextField textField_2;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            LoginGui dialog = new LoginGui(null, null, null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openDialog() {
        try {
            // 读取配置文件
            String zkPort = HandleCore.getStringValue("hbase.zk.port");
            String zkQuorum = HandleCore.getStringValue("hbase.zk.quorum");
            String hbaseMaster = HandleCore.getStringValue("hbase.master");

            com.lm.hbase.swing.SwingConstants.loginGui = new LoginGui(zkPort, zkQuorum, hbaseMaster);
            com.lm.hbase.swing.SwingConstants.loginGui.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            com.lm.hbase.swing.SwingConstants.loginGui.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public LoginGui(String zkPort, String zkQuorum, String hbaseMaster){
        setTitle("配置Hbase");
        setBounds(100, 100, 450, 233);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC,
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC,
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC,
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC,
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC,
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC,
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 ColumnSpec.decode("default:grow"), },
                                              new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                                                              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                                                              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                                                              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                                                              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                                                              FormSpecs.RELATED_GAP_ROWSPEC,
                                                              FormSpecs.DEFAULT_ROWSPEC, }));
        {
            JLabel lblNewLabel = new JLabel("ZK.PORT");
            lblNewLabel.setToolTipText("hbase.zookeeper.property.clientPort");
            contentPanel.add(lblNewLabel, "6, 4, right, default");
        }
        {
            textField = new JTextField();
            contentPanel.add(textField, "10, 4, 5, 1, fill, default");
            textField.setColumns(10);
            textField.setText(zkPort);
        }
        {
            JLabel lblNewLabel_1 = new JLabel("ZK.QUORUM");
            lblNewLabel_1.setToolTipText("hbase.zookeeper.quorum");
            contentPanel.add(lblNewLabel_1, "6, 8, right, default");
        }
        {
            textField_1 = new JTextField();
            contentPanel.add(textField_1, "10, 8, 5, 1, fill, default");
            textField_1.setColumns(10);
            textField_1.setText(zkQuorum);
        }
        {
            JLabel lblNewLabel_2 = new JLabel("HBASE.MASTER");
            lblNewLabel_2.setToolTipText("hbase.master");
            contentPanel.add(lblNewLabel_2, "6, 12, right, default");
        }
        {
            textField_2 = new JTextField();
            contentPanel.add(textField_2, "10, 12, 5, 1, fill, default");
            textField_2.setColumns(10);
            textField_2.setText(hbaseMaster);
        }
        {
            JPanel buttonPane = new JPanel();
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            buttonPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("127px"),
                                                                   ColumnSpec.decode("147px"),
                                                                   FormSpecs.RELATED_GAP_COLSPEC,
                                                                   ColumnSpec.decode("75px"),
                                                                   FormSpecs.RELATED_GAP_COLSPEC,
                                                                   ColumnSpec.decode("86px"), },
                                                new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC,
                                                                RowSpec.decode("29px"), }));
            {
                JButton testButton = new JButton("Test Connection");
                testButton.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {
                        testButton.setEnabled(false);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        try {
                            ClusterStatus clusterStatus = HandleCore.testConf(textField.getText(),
                                                                              textField_1.getText(),
                                                                              textField_2.getText());
                            if (clusterStatus != null) {
                                JOptionPane.showMessageDialog(contentPanel, "连接成功,集群信息如下\n" + clusterStatus.toString(),
                                                              "提示", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(contentPanel, "连接失败", "错误", JOptionPane.ERROR_MESSAGE);
                            }

                        } catch (Exception e2) {
                            JOptionPane.showMessageDialog(contentPanel, "连接失败.\n" + e2.getLocalizedMessage(), "错误",
                                                          JOptionPane.ERROR_MESSAGE);
                        }
                        testButton.setEnabled(true);
                    }
                });
                buttonPane.add(testButton, "1, 2, left, top");
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        System.exit(0);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton, "4, 2, left, top");
            }
            {
                JButton okButton = new JButton("OK");
                okButton.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {
                        okButton.setEnabled(false);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        try {
                            ClusterStatus clusterStatus = HandleCore.testConf(textField.getText(),
                                                                              textField_1.getText(),
                                                                              textField_2.getText());
                            if (clusterStatus != null) {
                                com.lm.hbase.swing.SwingConstants.loginGui.setVisible(false);// 隐藏登陆窗体
                                com.lm.hbase.swing.SwingConstants.hbaseGui.initialize();// 唤出主窗体
                            } else {
                                JOptionPane.showMessageDialog(contentPanel, "连接失败");
                            }

                        } catch (Exception e2) {
                            JOptionPane.showMessageDialog(contentPanel, "连接失败.\n" + e2.getLocalizedMessage());
                        }
                        okButton.setEnabled(true);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton, "6, 2, left, top");
                getRootPane().setDefaultButton(okButton);
            }
        }

        this.addWindowListener(new WindowAdapter() {

            public void windowClosed(WindowEvent e) {
                try {
                    HbaseUtil.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                super.windowClosed(e);

                System.exit(0);

            }
        });
    }

}
