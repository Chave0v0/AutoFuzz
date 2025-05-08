package com.chave.ui;

import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import com.chave.Main;
import com.chave.config.UserConfig;
import com.chave.handler.AutoFuzzHandler;
import com.chave.pojo.*;
import com.chave.utils.Util;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.chave.pojo.Data.*;

@lombok.Data
public class MainUI {
    // 创建组件

    private JRadioButton blackButton;
    private JRadioButton whiteButton;
    private ButtonGroup blackOrWhiteGroup;

    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel rightTopPanel;
    private JPanel rightBottomPanel;
    private JPanel turnOnPanel;
    private JPanel domainOperatePanel;
    private JPanel domainMainPanel;
    private JPanel listenProxyPanel;
    private JPanel listenRepeterPanel;
    private JPanel basicTitlePanel;
    private JPanel domainTitlePanel;
    private JPanel payloadTitlePanel;
    private JPanel cleanRequestListPanel;
    private JPanel payloadMainPanel;
    private JPanel payloadOperatePanel;
    private JPanel authHeaderTitlePanel;
    private JPanel authHeaderMainPanel;
    private JPanel authHeaderOperatePanel;
    private JPanel searchPanel;
    private JPanel tablePanel;
    private JSplitPane mainSplitPane;
    private JSplitPane rightSplitPane;
    private JTable fuzzRequestItemTable;
    private JTable originRequestItemTable;
    private JTable domainTable;
    private JTable payloadTable;
    private JTable authHeaderTable;
    private JButton addDomainButton;
    private JButton removeDomainButton;
    private JButton cleanRequestItemButton;
    private JButton editDomainButton;
    private JButton addPayloadButton;
    private JButton editPayloadButton;
    private JButton removePayloadButton;
    private JButton searchButton;
    private JButton cleanSearchResultButton;
    private JButton addAuthHeaderButton;
    private JButton editAuthHeaderButton;
    private JButton removeAuthHeaderButton;
    private JCheckBox turnOnCheckBox;
    private JCheckBox listenProxyCheckBox;
    private JCheckBox listenRepeterCheckBox;
    private JCheckBox includeSubDomainCheckBox;
    private JCheckBox emptyParamCheckBox;
    private JCheckBox paramURLEncodeCheckBox;
    private JCheckBox unauthCheckBox;
    private JCheckBox appendModCheckBox;
    private JLabel basicTitleLabel;
    private JLabel domainTitleLabel;
    private JLabel payloadTitleLabel;
    private JLabel authHeaderTitleLabel;
    private JTextField searchTextField;
    private JComboBox<String> searchScopeComboBox;
    private HttpRequestEditor requestEditor;
    private HttpResponseEditor responseEditor;
    private HashMap<Integer, ArrayList<Integer>> highlightMap;


    public MainUI() {
        init();
    }

    private void init() {
        // 初始化各个组件
        blackButton = new JRadioButton("黑名单");
        whiteButton = new JRadioButton("白名单");
        blackOrWhiteGroup = new ButtonGroup();
        blackOrWhiteGroup.add(blackButton);
        blackOrWhiteGroup.add(whiteButton);
        blackButton.setSelected(BLACK_OR_WHITE_CHOOSE);

        leftPanel = new JPanel();
        rightPanel = new JPanel();
        rightTopPanel = new JPanel();
        rightBottomPanel = new JPanel();
        turnOnPanel = new JPanel();
        domainOperatePanel = new JPanel();
        domainMainPanel = new JPanel();
        listenProxyPanel = new JPanel();
        listenRepeterPanel = new JPanel();
        cleanRequestListPanel = new JPanel();
        basicTitlePanel = new JPanel();
        domainTitlePanel = new JPanel();
        payloadTitlePanel = new JPanel();
        payloadMainPanel = new JPanel();
        payloadOperatePanel = new JPanel();
        authHeaderTitlePanel = new JPanel();
        authHeaderMainPanel = new JPanel();
        authHeaderOperatePanel = new JPanel();
        searchPanel = new JPanel();
        tablePanel = new JPanel();
        turnOnCheckBox = new JCheckBox("启用插件");
        listenProxyCheckBox = new JCheckBox("监听Proxy");
        listenRepeterCheckBox = new JCheckBox("监听Repeter");
        emptyParamCheckBox = new JCheckBox("参数置空");
        paramURLEncodeCheckBox = new JCheckBox("URL编码");
        includeSubDomainCheckBox = new JCheckBox("包含子域名");
        unauthCheckBox = new JCheckBox("未授权访问");
        appendModCheckBox = new JCheckBox("追加模式");
        addDomainButton = new JButton("添加");
        editDomainButton = new JButton("编辑");
        removeDomainButton = new JButton("删除");
        cleanRequestItemButton = new JButton("清空请求记录");
        addPayloadButton = new JButton("添加");
        editPayloadButton = new JButton("编辑");
        removePayloadButton = new JButton("删除");
        searchButton = new JButton("查找");
        cleanSearchResultButton = new JButton("清空查找结果");
        addAuthHeaderButton = new JButton("添加");
        editAuthHeaderButton = new JButton("编辑");
        removeAuthHeaderButton = new JButton("删除");
        searchTextField = new JTextField();

        basicTitleLabel = new JLabel("-----------------------------基本功能-----------------------------");
        domainTitleLabel = new JLabel("-----------------------------域名设置-----------------------------");
        payloadTitleLabel = new JLabel("---------------------------Payload设置---------------------------");
        authHeaderTitleLabel = new JLabel("-------------------------Auth Header设置-------------------------");
        requestEditor = Main.API.userInterface().createHttpRequestEditor(EditorOptions.READ_ONLY);
        responseEditor = Main.API.userInterface().createHttpResponseEditor(EditorOptions.READ_ONLY);
        highlightMap = new HashMap<>();  // 这里防止空指针 后面还会重新初始化

        /**
         * **自定义 TableModel，确保 ID 列按数值大小排序**
         */
        class SortedTableModel extends DefaultTableModel {
            public SortedTableModel(Object[] columnNames, int rowCount) {
                super(columnNames, rowCount);
            }

            @Override
            public void addRow(Object[] rowData) {
                super.addRow(rowData);
                sortData(); // 添加数据后自动排序
                fireTableDataChanged(); // 通知 UI 更新
            }

            @Override
            public void removeRow(int row) {
                super.removeRow(row);
                sortData(); // 删除数据后自动排序
                fireTableDataChanged(); // 通知 UI 更新
            }

            @Override
            public void fireTableDataChanged() {
                sortData(); // 确保每次数据更新后排序
                super.fireTableDataChanged();
            }

            /**
             * **对数据按 ID（第 0 列）排序**
             */
            private void sortData() {
                // 获取数据
                Vector<Vector> dataVector = getDataVector();
                // 按照 ID 列（第 0 列）升序排序
                dataVector.sort((v1, v2) -> {
                    Integer id1 = (Integer) v1.get(0);  // 获取第 0 列 ID，强制转换为 Integer
                    Integer id2 = (Integer) v2.get(0);  // 获取第 0 列 ID，强制转换为 Integer
                    return id1.compareTo(id2);  // 按升序排序
                });
            }
        }

        // 创建左边表格
        String[] originRequestItemTableColumnName = {"#", "Method", "Host", "Path", "返回包长度", "状态码"};
        SortedTableModel originRequestItemTableModel = new SortedTableModel(originRequestItemTableColumnName, 0);
        originRequestItemTable = new JTable(originRequestItemTableModel);
        // 设置列默认宽度
        originRequestItemTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        originRequestItemTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        originRequestItemTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        originRequestItemTable.getColumnModel().getColumn(5).setPreferredWidth(20);
        // 禁止整个表格编辑
        originRequestItemTable.setDefaultEditor(Object.class, null);
        // 创建表格滚动面板
        JScrollPane originRequestItemTableScrollPane = new JScrollPane(originRequestItemTable);


        // 创建右边表格
        String[] fuzzRequestItemTableColumnName = {"Param", "Payload", "返回包长度", "返回包变化", "状态码"};
        DefaultTableModel fuzzRequestItemTableModel = new DefaultTableModel(fuzzRequestItemTableColumnName, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        fuzzRequestItemTable = new JTable(fuzzRequestItemTableModel);
        // 禁止整个表格编辑
        fuzzRequestItemTable.setDefaultEditor(Object.class, null);
        // 创建表格滚动面板
        JScrollPane fuzzRequestItemTableScrollPane = new JScrollPane(fuzzRequestItemTable);


        // 绘制左侧面板(用户配置面板)
        // 设置左边主面板布局
        BoxLayout leftBoxLayout = new BoxLayout(leftPanel, BoxLayout.Y_AXIS);
        leftPanel.setLayout(leftBoxLayout);
        // 设置四个标题label
        BoxLayout basicTitleLayout = new BoxLayout(basicTitlePanel, BoxLayout.X_AXIS);
        BoxLayout domainTitleLayout = new BoxLayout(domainTitlePanel, BoxLayout.X_AXIS);
        BoxLayout payloadTitleLayout = new BoxLayout(payloadTitlePanel, BoxLayout.X_AXIS);
        BoxLayout authHeaderTitleLayout = new BoxLayout(authHeaderTitlePanel, BoxLayout.X_AXIS);
        basicTitlePanel.setLayout(basicTitleLayout);
        domainTitlePanel.setLayout(domainTitleLayout);
        payloadTitlePanel.setLayout(payloadTitleLayout);
        authHeaderTitlePanel.setLayout(authHeaderTitleLayout);
        basicTitlePanel.add(basicTitleLabel, Component.CENTER_ALIGNMENT);
        domainTitlePanel.add(domainTitleLabel, Component.CENTER_ALIGNMENT);
        payloadTitlePanel.add(payloadTitleLabel, Component.CENTER_ALIGNMENT);
        authHeaderTitlePanel.add(authHeaderTitleLabel, Component.CENTER_ALIGNMENT);
        // 设置复选框默认勾选状态 居中放置
        BoxLayout turnOnLayout = new BoxLayout(turnOnPanel, BoxLayout.X_AXIS);
        turnOnPanel.setLayout(turnOnLayout);
        turnOnCheckBox.setSelected(true);
        turnOnPanel.add(Box.createHorizontalStrut(100));
        turnOnPanel.add(turnOnCheckBox);
        turnOnPanel.add(Box.createHorizontalGlue());
        turnOnPanel.setMaximumSize(new Dimension(20000, 22));
        // 选择监听proxy 监听repeter 居中放置
        BoxLayout listenProxyLayout = new BoxLayout(listenProxyPanel, BoxLayout.X_AXIS);
        listenProxyPanel.setLayout(listenProxyLayout);
        listenProxyPanel.add(Box.createHorizontalStrut(100));
        listenProxyPanel.add(listenProxyCheckBox);
        listenProxyPanel.add(Box.createHorizontalGlue());
        listenProxyPanel.setMaximumSize(new Dimension(20000, 22));
        BoxLayout listenRepeterLayout = new BoxLayout(listenRepeterPanel, BoxLayout.X_AXIS);
        listenRepeterPanel.setLayout(listenRepeterLayout);
        listenRepeterPanel.add(Box.createHorizontalStrut(100));
        listenRepeterPanel.add(listenRepeterCheckBox);
        listenRepeterPanel.add(Box.createHorizontalGlue());
        listenRepeterPanel.setMaximumSize(new Dimension(20000, 22));
        // 清空请求列表
        BoxLayout cleanRequestListLayout = new BoxLayout(cleanRequestListPanel, BoxLayout.X_AXIS);
        cleanRequestListPanel.setLayout(cleanRequestListLayout);
        cleanRequestListPanel.add(Box.createHorizontalStrut(100));
        cleanRequestListPanel.add(cleanRequestItemButton);
        cleanRequestListPanel.add(Box.createHorizontalGlue());
        cleanRequestListPanel.setMaximumSize(new Dimension(20000, 22));
        // 域名配置部分绘制
        // 用户操作部分
        BoxLayout domainMainLayout = new BoxLayout(domainMainPanel, BoxLayout.X_AXIS);
        BoxLayout domainOperateLayout = new BoxLayout(domainOperatePanel, BoxLayout.Y_AXIS);
        domainMainPanel.setLayout(domainMainLayout);
        domainOperatePanel.setLayout(domainOperateLayout);
        domainOperatePanel.add(blackButton, Component.CENTER_ALIGNMENT);
        domainOperatePanel.add(Box.createVerticalStrut(5));
        domainOperatePanel.add(whiteButton, Component.CENTER_ALIGNMENT);
        domainOperatePanel.add(Box.createVerticalStrut(5));
        domainOperatePanel.add(addDomainButton, Component.CENTER_ALIGNMENT);
        domainOperatePanel.add(Box.createVerticalStrut(5));
        domainOperatePanel.add(editDomainButton, Component.CENTER_ALIGNMENT);
        domainOperatePanel.add(Box.createVerticalStrut(5));
        domainOperatePanel.add(removeDomainButton, Component.CENTER_ALIGNMENT);
        domainOperatePanel.add(Box.createVerticalStrut(5));
        domainOperatePanel.add(includeSubDomainCheckBox, Component.CENTER_ALIGNMENT);
        domainMainPanel.add(Box.createHorizontalStrut(5));
        domainMainPanel.add(domainOperatePanel);
        // 初始化域名表格
        String[] domainTableColumnName = {"Domain"};
        DefaultTableModel domainModel = new DefaultTableModel(domainTableColumnName, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        domainTable = new JTable(domainModel);
        // 支持多行选中
        domainTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // 禁止表格编辑
        domainTable.setDefaultEditor(Object.class, null);
        // 创建表格滚动面板
        JScrollPane domainTableScrollPane = new JScrollPane(domainTable);
        domainTableScrollPane.setMaximumSize(new Dimension(150, 2000));
        domainMainPanel.add(Box.createHorizontalStrut(5));
        domainMainPanel.add(domainTableScrollPane);
        domainMainPanel.add(Box.createHorizontalStrut(5));
        // payload配置部分绘制
        // 用户操作部分
        BoxLayout payloadMainLayout = new BoxLayout(payloadMainPanel, BoxLayout.X_AXIS);
        BoxLayout payloadOperateLayout = new BoxLayout(payloadOperatePanel, BoxLayout.Y_AXIS);
        payloadMainPanel.setLayout(payloadMainLayout);
        payloadOperatePanel.setLayout(payloadOperateLayout);

        payloadOperatePanel.add(addPayloadButton, Component.CENTER_ALIGNMENT);
        payloadOperatePanel.add(Box.createVerticalStrut(10));
        payloadOperatePanel.add(editPayloadButton, Component.CENTER_ALIGNMENT);
        payloadOperatePanel.add(Box.createVerticalStrut(10));
        payloadOperatePanel.add(removePayloadButton, Component.CENTER_ALIGNMENT);
        payloadOperatePanel.add(Box.createVerticalStrut(10));
        payloadOperatePanel.add(appendModCheckBox, Component.CENTER_ALIGNMENT);
        payloadOperatePanel.add(Box.createVerticalStrut(10));
        payloadOperatePanel.add(emptyParamCheckBox, Component.CENTER_ALIGNMENT);
        payloadOperatePanel.add(Box.createVerticalStrut(10));
        payloadOperatePanel.add(paramURLEncodeCheckBox, Component.CENTER_ALIGNMENT);
        payloadMainPanel.add(Box.createHorizontalStrut(5));
        payloadMainPanel.add(payloadOperatePanel);
        // 初始化payload表格
        String[] payloadTableColumnName = {"Payload"};
        DefaultTableModel payloadModel = new DefaultTableModel(payloadTableColumnName, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        payloadTable = new JTable(payloadModel);
        getPayloadCache();
        // 支持多行选中
        payloadTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // 禁止表格编辑
        payloadTable.setDefaultEditor(Object.class, null);
        // 创建表格滚动面板
        JScrollPane payloadTableScrollPane = new JScrollPane(payloadTable);
        payloadTableScrollPane.setMaximumSize(new Dimension(150, 2000));
        payloadMainPanel.add(Box.createHorizontalStrut(11));
        payloadMainPanel.add(payloadTableScrollPane);
        payloadMainPanel.add(Box.createHorizontalStrut(5));


        // Auth Header配置部分绘制
        // 用户操作部分
        BoxLayout authHeaderMainLayout = new BoxLayout(authHeaderMainPanel, BoxLayout.X_AXIS);
        BoxLayout authHeaderOperateLayout = new BoxLayout(authHeaderOperatePanel, BoxLayout.Y_AXIS);
        authHeaderMainPanel.setLayout(authHeaderMainLayout);
        authHeaderOperatePanel.setLayout(authHeaderOperateLayout);
        authHeaderOperatePanel.add(addAuthHeaderButton, Component.CENTER_ALIGNMENT);
        authHeaderOperatePanel.add(Box.createVerticalStrut(10));
        authHeaderOperatePanel.add(editAuthHeaderButton, Component.CENTER_ALIGNMENT);
        authHeaderOperatePanel.add(Box.createVerticalStrut(10));
        authHeaderOperatePanel.add(removeAuthHeaderButton, Component.CENTER_ALIGNMENT);
        authHeaderOperatePanel.add(Box.createVerticalStrut(10));
        authHeaderOperatePanel.add(unauthCheckBox, Component.CENTER_ALIGNMENT);
        authHeaderMainPanel.add(Box.createHorizontalStrut(5));
        authHeaderMainPanel.add(authHeaderOperatePanel);
        // 初始化payload表格
        String[] authHeaderTableColumnName = {"Header", "Value"};
        DefaultTableModel authHeaderModel = new DefaultTableModel(authHeaderTableColumnName, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        authHeaderTable = new JTable(authHeaderModel);
        // 支持多行选中
        authHeaderTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // 禁止表格编辑
        authHeaderTable.setDefaultEditor(Object.class, null);
        // 设置首选宽度
        authHeaderTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        // 创建表格滚动面板
        JScrollPane authHeaderTableScrollPane = new JScrollPane(authHeaderTable);
        authHeaderTableScrollPane.setMaximumSize(new Dimension(150, 2000));
        authHeaderMainPanel.add(Box.createHorizontalStrut(5));
        authHeaderMainPanel.add(authHeaderTableScrollPane);
        authHeaderMainPanel.add(Box.createHorizontalStrut(5));


        // 左侧面板添加各个组件
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(basicTitlePanel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(turnOnPanel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(listenProxyPanel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(listenRepeterPanel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(cleanRequestListPanel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(domainTitlePanel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(domainMainPanel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(payloadTitlePanel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(payloadMainPanel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(authHeaderTitlePanel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(authHeaderMainPanel);
        leftPanel.add(Box.createVerticalStrut(5));

        BoxLayout tableLayout = new BoxLayout(tablePanel, BoxLayout.X_AXIS);
        tablePanel.setLayout(tableLayout);
        tablePanel.add(originRequestItemTableScrollPane);
        tablePanel.add(fuzzRequestItemTableScrollPane);

        // 右上方面板绘制
        BoxLayout rightTopLayout = new BoxLayout(rightTopPanel, BoxLayout.Y_AXIS);
        rightTopPanel.setLayout(rightTopLayout);
        // 搜索框绘制
        String[] searchScopeOptions = {"request", "response"};
        searchScopeComboBox = new JComboBox<>(searchScopeOptions);
        searchScopeComboBox.setSelectedItem("request");
        UserConfig.SEARCH_SCOPE = SearchScope.REQUEST;
        BoxLayout searchPanelLayout = new BoxLayout(searchPanel, BoxLayout.X_AXIS);
        searchPanel.setLayout(searchPanelLayout);
        searchPanel.add(Box.createHorizontalStrut(2));
        searchPanel.add(searchScopeComboBox);
        searchPanel.add(Box.createHorizontalStrut(2));
        searchPanel.add(searchTextField);
        searchPanel.add(Box.createHorizontalStrut(2));
        searchPanel.add(searchButton);
        searchPanel.add(Box.createHorizontalStrut(2));
        searchPanel.add(cleanSearchResultButton);
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.setMaximumSize(new Dimension(20000, 30));
        rightTopPanel.add(tablePanel);
        rightTopPanel.add(Box.createVerticalStrut(3));
        rightTopPanel.add(searchPanel);
        rightTopPanel.add(Box.createVerticalStrut(2));

        // 创建request/response展示面板
        BoxLayout rightBottomLayout = new BoxLayout(rightBottomPanel, BoxLayout.X_AXIS);
        rightBottomPanel.setLayout(rightBottomLayout);
        rightBottomPanel.add(requestEditor.uiComponent());
        rightBottomPanel.add(responseEditor.uiComponent());

        // 创建右侧分隔面板 上下分隔
        rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightTopPanel, rightBottomPanel);
        rightSplitPane.setDividerLocation(400);

        // 创建主分隔面板 左右分隔
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightSplitPane);
        mainSplitPane.setDividerLocation(300);
        mainSplitPane.setEnabled(false);

        for (int i = 0; i < originRequestItemTable.getColumnCount(); i++) {
            originRequestItemTable.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    // 调用默认的渲染器来获取单元格组件
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    if (column == 0 || column == 1 || column == 4 || column == 5) {
                        ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.CENTER);
                    }

                    if (highlightMap.containsKey(row)) {
                        c.setForeground(Color.RED);
                    } else {
                        c.setForeground(originRequestItemTable.getForeground());
                    }

                    return c;
                }
            });
        }

        for (int i = 0; i < fuzzRequestItemTable.getColumnCount(); i++) {
            fuzzRequestItemTable.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    // 调用默认的渲染器来获取单元格组件
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    try {
                        int originRequestSelectedRow = originRequestItemTable.getSelectedRows()[0];

                        if (column == 0 || column == 1 || column == 2 || column == 3 || column == 4) {
                            ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.CENTER);
                        }

                        if (highlightMap.get(originRequestSelectedRow).contains(row)) {
                            c.setForeground(Color.RED);
                        } else {
                            c.setForeground(originRequestItemTable.getForeground());
                        }
                    } catch (Exception e) {
                        c.setForeground(originRequestItemTable.getForeground());
                    }

                    return c;
                }
            });
        }

        ActionListener listener = e -> {
            JRadioButton selected = (JRadioButton)e.getSource();
            if(selected == blackButton) {
                BLACK_OR_WHITE_CHOOSE = Boolean.TRUE;
            } else if(selected == whiteButton){
                BLACK_OR_WHITE_CHOOSE = Boolean.FALSE;
            }
        };

        blackButton.addActionListener(listener);
        whiteButton.addActionListener(listener);


        // 设置启用插件复选框监听器
        turnOnCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    UserConfig.TURN_ON = Boolean.TRUE;
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    UserConfig.TURN_ON = Boolean.FALSE;
                }
            }
        });


        // 监听proxy复选框监听器
        listenProxyCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    UserConfig.LISTEN_PROXY = Boolean.TRUE;
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    UserConfig.LISTEN_PROXY = Boolean.FALSE;
                }
            }
        });


        // 监听repeter复选框监听器
        listenRepeterCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    UserConfig.LISTEN_REPETER = Boolean.TRUE;
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    UserConfig.LISTEN_REPETER = Boolean.FALSE;
                }
            }
        });


        // 清空请求记录按钮监听器
        cleanRequestItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Data.ORIGIN_REQUEST_TABLE_DATA.clear();
                Data.NEW_REQUEST_TO_BE_SENT_DATA.clear();
                originRequestItemTableModel.setRowCount(0);
                fuzzRequestItemTableModel.setRowCount(0);

                // 清空editor
                requestEditor.setRequest(null);
                responseEditor.setResponse(null);

                try {
                    Field executorField = AutoFuzzHandler.class.getDeclaredField("executor");
                    executorField.setAccessible(true);
                    executorField.set(null, new ThreadPoolExecutor(10, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(200), new ThreadPoolExecutor.AbortPolicy()));
                } catch (NoSuchFieldException ex) {
                    Main.LOG.logToError(ex.getMessage() + "重置线程池异常");
                } catch (IllegalAccessException ex) {
                    Main.LOG.logToError(ex.getMessage() + "重置线程池异常");
                }
            }
        });


        // 添加域名按钮监听器
        addDomainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddDataDialog("domain");
            }
        });

        // 添加payload按钮监听器
        addPayloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddDataDialog("payload");
            }
        });

        // 添加域名按钮监听器
        addAuthHeaderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddDataDialog("header");
            }
        });

        // 编辑域名按钮监听器
        editDomainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 如果选择多行 默认编辑选中的第一行
                showEditDataDialog("domain", domainTable.getSelectedRows()[0]);
            }
        });

        // 编辑payload按钮监听器
        editPayloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 如果选中多行 默认编辑选中的第一行
                showEditDataDialog("payload", payloadTable.getSelectedRows()[0]);
            }
        });

        // 编辑AuthHeader按钮监听器
        editAuthHeaderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 如果选中多行 默认编辑选中的第一行
                showEditDataDialog("header", authHeaderTable.getSelectedRows()[0]);
            }
        });

        // 删除domain按钮监听器
        removeDomainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Util.removeConfigData("domain", domainTable.getSelectedRows());
                Util.flushConfigTable("domain", domainTable);
            }
        });

        // 删除payload按钮监听器
        removePayloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Util.removeConfigData("payload", payloadTable.getSelectedRows());
                Util.flushConfigTable("payload", payloadTable);
            }
        });

        // 删除payload按钮监听器
        removeAuthHeaderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Util.removeConfigData("header", authHeaderTable.getSelectedRows());
                Util.flushConfigTable("header", authHeaderTable);
            }
        });

        // appendMod复选框监听器
        appendModCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    UserConfig.APPEND_MOD = Boolean.TRUE;
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    UserConfig.APPEND_MOD = Boolean.FALSE;
                }
            }
        });

        // 参数置空监听器
        emptyParamCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    PAYLOAD_LIST.add(0, "");
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    PAYLOAD_LIST.remove("");
                }

                Util.flushConfigTable("payload", payloadTable);

            }
        });

        paramURLEncodeCheckBox.addItemListener((new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    UserConfig.PARAM_URL_ENCODE = Boolean.TRUE;
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    UserConfig.PARAM_URL_ENCODE = Boolean.FALSE;
                }
            }
        }));

        // 包含子域名复选框监听器
        includeSubDomainCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    UserConfig.INCLUDE_SUBDOMAIN = Boolean.TRUE;
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    UserConfig.INCLUDE_SUBDOMAIN = Boolean.FALSE;
                }
            }
        });

        // 包含子域名复选框监听器
        unauthCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    UserConfig.UNAUTH = Boolean.TRUE;
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    UserConfig.UNAUTH = Boolean.FALSE;
                }
            }
        });

        // 查找作用域下拉框监听器
        searchScopeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取用户选择
                String selectedOption = (String) searchScopeComboBox.getSelectedItem();

                if (SearchScope.REQUEST.scopeName().equals(selectedOption)) {
                    UserConfig.SEARCH_SCOPE = SearchScope.REQUEST;
                } else if (SearchScope.RESPONSE.scopeName().equals(selectedOption)) {
                    UserConfig.SEARCH_SCOPE = SearchScope.RESPONSE;
                }
            }
        });


        // 查找按钮监听器
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String keyword = searchTextField.getText().trim();

                if (UserConfig.SEARCH_SCOPE == SearchScope.REQUEST) {
                    searchAllRequestResponse(keyword, SearchScope.REQUEST);
                } else if (UserConfig.SEARCH_SCOPE == SearchScope.RESPONSE) {
                    searchAllRequestResponse(keyword, SearchScope.RESPONSE);
                }
            }
        });

        // 清空查找结果按钮监听器
        cleanSearchResultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightMap.clear();

                originRequestItemTable.repaint();
                fuzzRequestItemTable.repaint();
            }
        });

        // 创建fuzzRequestItem被点击时的监听事件  用于展示request response
        fuzzRequestItemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fuzzRow = fuzzRequestItemTable.getSelectedRow();
                int originRow = originRequestItemTable.getSelectedRow();
                if (fuzzRow < 0 || originRow < 0) return;

                Integer id = (Integer) originRequestItemTableModel.getValueAt(originRow, 0);
                String method = (String) originRequestItemTableModel.getValueAt(originRow, 1);
                String host = (String) originRequestItemTableModel.getValueAt(originRow, 2);
                String path = (String) originRequestItemTableModel.getValueAt(originRow, 3);
                OriginRequestItem tempItem = new OriginRequestItem(id, method, host, path, null, null);

                for (OriginRequestItem item : Data.ORIGIN_REQUEST_TABLE_DATA.values()) {
                    if (item.equals(tempItem) && item.getId().equals(id)) {
                        if (fuzzRow < item.getFuzzRequestArrayList().size()) {
                            FuzzRequestItem fuzzItem = item.getFuzzRequestArrayList().get(fuzzRow);
                            requestEditor.setRequest(fuzzItem.getFuzzRequestResponse().request());
                            responseEditor.setResponse(fuzzItem.getFuzzRequestResponse().response());
                        }
                        break;
                    }
                }
            }
        });


        // 创建oritinRequestItem被点击时的监听事件  用于展示request response
        originRequestItemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = originRequestItemTable.getSelectedRow();
                if (row < 0) return;

                Integer id = (Integer) originRequestItemTableModel.getValueAt(row, 0);
                String methodText = (String) originRequestItemTableModel.getValueAt(row, 1);
                String hostText = (String) originRequestItemTableModel.getValueAt(row, 2);
                String pathText = (String) originRequestItemTableModel.getValueAt(row, 3);
                OriginRequestItem tempItem = new OriginRequestItem(id, methodText, hostText, pathText, null, null);

                for (OriginRequestItem item : Data.ORIGIN_REQUEST_TABLE_DATA.values()) {
                    if (item.equals(tempItem) && item.getId().equals(id)) {
                        requestEditor.setRequest(item.getOriginRequest());
                        responseEditor.setResponse(item.getOriginResponse());

                        // 刷新fuzz子项
                        fuzzRequestItemTableModel.setRowCount(0);
                        for (FuzzRequestItem fuzzItem : item.getFuzzRequestArrayList()) {
                            fuzzRequestItemTableModel.addRow(new Object[]{
                                    fuzzItem.getParam(), fuzzItem.getPayload(),
                                    fuzzItem.getResponseLength(), fuzzItem.getResponseLengthChange(),
                                    fuzzItem.getResponseCode()
                            });
                        }
                        break;
                    }
                }
            }
        });

    }

    private void showAddDataDialog(String type) {
        TitledBorder titledBorder = null;
        if (type.equals("domain")) {
            titledBorder = BorderFactory.createTitledBorder("添加域名 每行一个");
        } else if (type.equals("payload")) {
            titledBorder = BorderFactory.createTitledBorder("添加Payload 每行一个");
        } else if (type.equals("header")) {
            titledBorder = BorderFactory.createTitledBorder("添加Header 每行一个");
        }

        JTextArea userInputTextArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(userInputTextArea);
        userInputTextArea.setBorder(BorderFactory.createCompoundBorder(userInputTextArea.getBorder(), titledBorder));
        scrollPane.setPreferredSize(new Dimension(350, 250));

        int option = 0;
        if (type.equals("domain")) {
            option = JOptionPane.showConfirmDialog(null, scrollPane, "添加域名", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        } else if (type.equals("payload")) {
            option = JOptionPane.showConfirmDialog(null, scrollPane, "添加Payload", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        } else if (type.equals("header")) {
            option = JOptionPane.showConfirmDialog(null, scrollPane, "添加AuthHeader", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        }


        // 检测用户选择
        if (option == JOptionPane.YES_OPTION) {
            // 用户选择了 "是" 则添加数据，其他情况不做操作
            Util.addConfigData(type, userInputTextArea);

            if (type.equals("domain")) {
                Util.flushConfigTable(type, domainTable);
            } else if (type.equals("payload")) {
                Util.flushConfigTable(type, payloadTable);
            } else if (type.equals("header")) {
                Util.flushConfigTable(type, authHeaderTable);
            }
        }
    }

    private void showEditDataDialog(String type, int row) {
        JTextField dataTextField = new JTextField();

        if (type.equals("domain")) {
            dataTextField.setText((String) domainTable.getModel().getValueAt(row, 0));
        } else if (type.equals("payload")) {
            dataTextField.setText((String) payloadTable.getModel().getValueAt(row, 0));
        } else if (type.equals("header")) {
            dataTextField.setText(authHeaderTable.getModel().getValueAt(row, 0) + ": " + authHeaderTable.getModel().getValueAt(row, 1));
        }

        int option = 0;
        if (type.equals("domain")) {
            option = JOptionPane.showConfirmDialog(null, dataTextField, "编辑域名", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        } else if (type.equals("payload")) {
            option = JOptionPane.showConfirmDialog(null, dataTextField, "编辑Payload", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        } else if (type.equals("header")) {
            option = JOptionPane.showConfirmDialog(null, dataTextField, "编辑AuthHeader", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        }


        // 检测用户选择
        if (option == JOptionPane.YES_OPTION) {
            // 用户选择了 "是" 则添加数据，其他情况不做操作
            Util.editConfigData(type, dataTextField, row);

            if (type.equals("domain")) {
                Util.flushConfigTable(type, domainTable);
            } else if (type.equals("payload")) {
                Util.flushConfigTable(type, payloadTable);
            } else if (type.equals("header")) {
                Util.flushConfigTable(type, authHeaderTable);
            }
        }
    }

    private void searchAllRequestResponse(String keyword, SearchScope searchScope) {
        // 先初始化map
        highlightMap = new HashMap<>();

        // 获取列表中所有origin的行
        TableModel model = originRequestItemTable.getModel();
        int rowCount = model.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            // 由于顺序不一样 根据表中的值先创建一个originRequest
            Integer id = (Integer) model.getValueAt(i, 0);
            String method = (String) model.getValueAt(i, 1);
            String host = (String) model.getValueAt(i, 2);
            String path = (String) model.getValueAt(i, 3);
            OriginRequestItem selectOriginRequestItem = new OriginRequestItem(id, method, host, path, null, null);

            // 遍历data中的数据找到对应的originRequest 检查内容
            for (Map.Entry<Integer, OriginRequestItem> originRequestItemEntry : Data.ORIGIN_REQUEST_TABLE_DATA.entrySet()) {
                OriginRequestItem originRequestItem = originRequestItemEntry.getValue();
                if (originRequestItem.equals(selectOriginRequestItem) && originRequestItem.getId().equals(id)) {
                    String originRequestString = originRequestItem.getOriginRequest().toString();
                    String originResponseString = originRequestItem.getOriginResponse().toString();
                    // 进行不区分大小写的比对originRequest
                    if (originRequestString.toLowerCase().contains(keyword.toLowerCase()) && searchScope.equals(SearchScope.REQUEST)) {
                        highlightMap.put(i, new ArrayList<>());
                    } else if (originResponseString.toLowerCase().contains(keyword.toLowerCase()) && searchScope.equals(SearchScope.RESPONSE)) {
                        highlightMap.put(i, new ArrayList<>());
                    }

                    // 比对fuzzRequest
                    ArrayList<FuzzRequestItem> fuzzRequestArrayList = originRequestItem.getFuzzRequestArrayList();
                    int index = 0;
                    for (FuzzRequestItem fuzzRequestItem : fuzzRequestArrayList) {
                        String fuzzRequestString = fuzzRequestItem.getFuzzRequestResponse().request().toString();
                        String fuzzResponseString = fuzzRequestItem.getFuzzRequestResponse().response().toString();
                        if (fuzzRequestString.toLowerCase().contains(keyword.toLowerCase()) && searchScope.equals(SearchScope.REQUEST)) {
                            if (highlightMap.containsKey(i)) {
                                highlightMap.get(i).add(index);
                            } else {  // 如果originRequest没匹配到 fuzzRequest匹配到了 也加入map
                                ArrayList<Integer> fuzzRequestHighlightList = new ArrayList<>();
                                fuzzRequestHighlightList.add(index);
                                highlightMap.put(i, fuzzRequestHighlightList);
                            }
                        } else if (fuzzResponseString.toLowerCase().contains(keyword.toLowerCase()) && searchScope.equals(SearchScope.RESPONSE)) {
                            if (highlightMap.containsKey(i)) {
                                highlightMap.get(i).add(index);
                            } else {  // 如果originResponse没匹配到 fuzzResponse匹配到了 也加入map
                                ArrayList<Integer> fuzzRequestHighlightList = new ArrayList<>();
                                fuzzRequestHighlightList.add(index);
                                highlightMap.put(i, fuzzRequestHighlightList);
                            }
                        }

                        index++;
                    }
                }
            }

            // 遍历完之后 重新渲染表格
            originRequestItemTable.repaint();
            fuzzRequestItemTable.repaint();
        }
    }

    // 初始化Payload缓存
    private void getPayloadCache(){
        List<String> content = null;
        content = Util.handleAutoFuzzPayload();
        if (!content.isEmpty()) {
           PAYLOAD_LIST.addAll(content);
            Util.flushConfigTable("payload", payloadTable);
        }
    }

}
