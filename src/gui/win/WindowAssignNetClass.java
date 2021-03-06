/*
 *  Copyright (C) 2014  Alfons Wirtz  
 *   website www.freerouting.net
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 * AssignNetRulesWindow.java
 *
 * Created on 12. April 2005, 06:09
 */

package gui.win;

import freert.rules.NetClass;
import freert.rules.RuleNet;
import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.GuiResources;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import board.RoutingBoard;

/**
 *
 * @author Alfons Wirtz
 */
public class WindowAssignNetClass extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;

   private final JPanel main_panel;

   private JScrollPane scroll_pane;
   private AssignRuleTable table;
   private AssignRuleTableModel table_model;

   private JComboBox<NetClass> net_rule_combo_box;

   private final GuiResources resources;

   private static final int TEXTFIELD_HEIGHT = 16;
   private static final int TEXTFIELD_WIDTH = 100;
   
   public WindowAssignNetClass(BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      resources = board_frame.newGuiResources("gui.resources.WindowAssignNetClass");
      
      this.setTitle(resources.getString("title"));

      this.main_panel = new JPanel();
      this.main_panel.setLayout(new java.awt.BorderLayout());

      this.table_model = new AssignRuleTableModel();
      this.table = new AssignRuleTable(this.table_model);
      this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.scroll_pane = new JScrollPane(this.table);
      int table_height = TEXTFIELD_HEIGHT * Math.min(this.table_model.getRowCount(), 20);
      int table_width = TEXTFIELD_WIDTH * this.table_model.getColumnCount();
      this.table.setPreferredScrollableViewportSize(new java.awt.Dimension(table_width, table_height));
      this.main_panel.add(scroll_pane, java.awt.BorderLayout.CENTER);
      add_net_class_combo_box();

      p_board_frame.set_context_sensitive_help(this, "WindowNetClasses_AssignNetClass");

      this.add(main_panel);
      this.pack();
      }

   private void add_net_class_combo_box()
      {
      this.net_rule_combo_box = new JComboBox<NetClass>();
      RoutingBoard routing_board = board_frame.board_panel.itera_board.get_routing_board();
      
      for (NetClass a_class : routing_board.brd_rules.net_classes )
         {
         net_rule_combo_box.addItem(a_class);
         }
      
      this.table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(net_rule_combo_box));
      }

   public void refresh()
      {
      // Reinsert the net class column.
      for (int i = 0; i < table_model.getRowCount(); ++i)
         {
         table_model.setValueAt(((RuleNet) table_model.getValueAt(i, 0)).get_class(), i, 1);
         }

      // Reinsert the net rule combobox because a rule may have been added or deleted.
      add_net_class_combo_box();
      }

   private class AssignRuleTable extends JTable
      {
      private static final long serialVersionUID = 1L;

      public AssignRuleTable(AssignRuleTableModel p_table_model)
         {
         super(p_table_model);
         }

      // Implement table header tool tips.
      protected JTableHeader createDefaultTableHeader()
         {
         return new JTableHeader(columnModel)
            {
            private static final long serialVersionUID = 1L;

            public String getToolTipText(java.awt.event.MouseEvent e)
               {
               java.awt.Point p = e.getPoint();
               int index = columnModel.getColumnIndexAtX(p.x);
               int realIndex = columnModel.getColumn(index).getModelIndex();
               return column_tool_tips[realIndex];
               }
            };
         }

      private final String[] column_tool_tips = { resources.getString("net_name_tooltip"), resources.getString("class_name_tooltip") };
      }

   /**
    * Table model of the net rule table.
    */
   private class AssignRuleTableModel extends javax.swing.table.AbstractTableModel
      {
      private static final long serialVersionUID = 1L;

      public AssignRuleTableModel()
         {
         column_names = new String[2];

         column_names[0] = resources.getString("net_name");
         column_names[1] = resources.getString("class_name");

         freert.rules.BoardRules board_rules = board_frame.board_panel.itera_board.get_routing_board().brd_rules;
         data = new Object[board_rules.nets.max_net_no()][];
         for (int i = 0; i < data.length; ++i)
            {
            this.data[i] = new Object[column_names.length];
            }
         set_values();
         }

      /** Calculates the the valus in this table */
      public void set_values()
         {
         freert.rules.BoardRules board_rules = board_frame.board_panel.itera_board.get_routing_board().brd_rules;
         RuleNet[] sorted_arr = new RuleNet[this.getRowCount()];
         for (int i = 0; i < sorted_arr.length; ++i)
            {
            sorted_arr[i] = board_rules.nets.get(i + 1);
            }
         java.util.Arrays.sort(sorted_arr);
         for (int i = 0; i < data.length; ++i)
            {
            this.data[i][0] = sorted_arr[i];
            this.data[i][1] = sorted_arr[i].get_class();
            }
         }

      public String getColumnName(int p_col)
         {
         return column_names[p_col];
         }

      public int getRowCount()
         {
         return data.length;
         }

      public int getColumnCount()
         {
         return column_names.length;
         }

      public Object getValueAt(int p_row, int p_col)
         {
         return data[p_row][p_col];
         }

      public boolean isCellEditable(int p_row, int p_col)
         {
         return p_col > 0;
         }

      public void setValueAt(Object p_value, int p_row, int p_col)
         {
         if (p_col != 1 || !(p_value instanceof NetClass))
            {
            return;
            }
         Object first_row_object = getValueAt(p_row, 0);
         if (!(first_row_object instanceof RuleNet))
            {
            System.out.println("AssignNetRuLesVindow.setValueAt: Net expected");
            return;
            }
         RuleNet curr_net = (RuleNet) first_row_object;
         NetClass curr_net_rule = (NetClass) p_value;
         curr_net.set_class(curr_net_rule);

         this.data[p_row][p_col] = p_value;
         fireTableCellUpdated(p_row, p_col);
         }

      private Object[][] data = null;
      private String[] column_names = null;
      }
   }
