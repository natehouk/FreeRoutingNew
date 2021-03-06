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
 */

package board.items;

import java.awt.Color;
import board.RoutingBoard;
import board.awtree.AwtreeShapeSearch;
import board.infos.BrdComponent;
import board.varie.ItemFixState;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;
import freert.graphics.GdiContext;
import freert.planar.PlaArea;
import freert.planar.PlaPointFloat;
import freert.planar.PlaPointInt;
import freert.planar.PlaVectorInt;
import freert.planar.ShapeTile;
import freert.planar.ShapeTileBox;
import freert.varie.NetNosList;
import gui.varie.ObjectInfoPanel;

/**
 *
 * An item on the board with an relative_area shape, for example keepout, conduction relative_area
 * 
 * @author Alfons Wirtz
 */

public  class BrdArea extends BrdItem implements java.io.Serializable
   {
   private static final long serialVersionUID = 1L;
   // For debugging the division into tree shapes 
   private static boolean display_tree_shapes = false;   

   // The name of this ObstacleArea, which is null, if the ObstacleArea does not belong to a component.
   public final String area_name;
   // the layer of this relative_area
   private int layer_no;
   private PlaArea relative_area;
   private PlaVectorInt translation;
   private int rotation_in_degree;
   private boolean side_changed;

   private transient PlaArea precalculated_absolute_area = null;
   
   /**
    * Creates a new relative_area item which may belong to several nets. 
    * p_name is null, if the ObstacleArea does not belong to a component.
    */
   public BrdArea(
         PlaArea p_area, 
         int p_layer_no, 
         PlaVectorInt p_translation, 
         int p_rotation_in_degree, 
         boolean p_side_changed, 
         NetNosList p_net_no_arr, 
         int p_clearance_type, 
         int p_id_no, 
         int p_cmp_no, 
         String p_name,
         ItemFixState p_fixed_state, 
         RoutingBoard p_board)
      {
      super(p_net_no_arr, p_clearance_type, p_id_no, p_cmp_no, p_fixed_state, p_board);
      relative_area = p_area;
      layer_no = p_layer_no;
      translation = p_translation;
      rotation_in_degree = p_rotation_in_degree;
      side_changed = p_side_changed;
      area_name = p_name;
      }

   /**
    * Creates a new relative_area item without net. 
    * p_name is null, if the ObstacleArea does not belong to a component.
    */
   public BrdArea(
         PlaArea p_area, 
         int p_layer, 
         PlaVectorInt p_translation, 
         int p_rotation_in_degree, 
         boolean p_side_changed, 
         int p_clearance_type, 
         int p_id_no, 
         int p_group_no, 
         String p_name,
         ItemFixState p_fixed_state, 
         RoutingBoard p_board)
      {
      this(p_area, p_layer, p_translation, p_rotation_in_degree, p_side_changed, NetNosList.EMPTY, p_clearance_type, p_id_no, p_group_no, p_name, p_fixed_state, p_board);
      }
   
   protected BrdArea ( BrdArea p_other, int p_id_no )
      {
      super(p_other, p_id_no);
      
      area_name = p_other.area_name;
      layer_no = p_other.layer_no;
      relative_area = p_other.relative_area;
      translation = p_other.translation;
      rotation_in_degree = p_other.rotation_in_degree;
      side_changed = p_other.side_changed;
      }

   @Override
   public BrdArea copy(int p_id_no)
      {
      return new BrdArea(this, p_id_no);
      }

   public PlaArea get_area()
      {
      if ( precalculated_absolute_area != null) return precalculated_absolute_area;

      PlaArea turned_area =  relative_area;

      if ( side_changed && ! r_board.brd_components.get_flip_style_rotate_first())
         {
         turned_area = turned_area.mirror_vertical(PlaPointInt.ZERO);
         }
      
      if ( rotation_in_degree != 0)
         {
         if (rotation_in_degree % 90 == 0)
            {
            turned_area = turned_area.rotate_90_deg(rotation_in_degree / 90, PlaPointInt.ZERO);
            }
         else
            {
            turned_area = turned_area.rotate_rad(Math.toRadians(rotation_in_degree), PlaPointFloat.ZERO);
            }
         }

      if ( side_changed && r_board.brd_components.get_flip_style_rotate_first())
         {
         turned_area = turned_area.mirror_vertical(PlaPointInt.ZERO);
         }
      
      precalculated_absolute_area = turned_area.translate_by(translation);

      return precalculated_absolute_area;
      }

   public PlaArea get_relative_area()
      {
      return relative_area;
      }

   @Override
   public boolean is_on_layer(int p_layer)
      {
      return layer_no == p_layer;
      }

   @Override
   public int first_layer()
      {
      return layer_no;
      }

   @Override
   public int last_layer()
      {
      return layer_no;
      }

   public int get_layer()
      {
      return layer_no;
      }

   @Override
   public ShapeTileBox bounding_box()
      {
      return get_area().bounding_box();
      }

   @Override
   public boolean is_obstacle(BrdItem p_other)
      {
      if (p_other.shares_net(this)) return false;

      return p_other instanceof BrdTracep || p_other instanceof BrdAbitVia;
      }

   @Override
   protected final ShapeTile[] calculate_tree_shapes(AwtreeShapeSearch p_search_tree)
      {
      return p_search_tree.calculate_tree_shapes(this);
      }

   @Override
   public int tile_shape_count()
      {
      ShapeTile[] tile_shapes = split_to_convex();
      if (tile_shapes == null)
         {
         // an error accured while dividing the relative_area
         return 0;
         }
      return tile_shapes.length;
      }

   @Override
   public ShapeTile tile_shape_get(int p_no)
      {
      ShapeTile[] tile_shapes = split_to_convex();
      if (tile_shapes == null || p_no < 0 || p_no >= tile_shapes.length)
         {
         System.out.println("ConvexObstacle.get_tile_shape: p_no out of range");
         return null;
         }

      return tile_shapes[p_no];
      }

   @Override
   public void translate_by(PlaVectorInt p_vector)
      {
      translation = translation.add(p_vector);
      clear_derived_data();
      }

   @Override
   public void rotate_90_deg(int p_factor, PlaPointInt p_pole)
      {
      rotation_in_degree = rotate_deg_reduce(rotation_in_degree + p_factor * 90);

      PlaPointInt rel_location = translation.to_int();
      
      translation = rel_location.rotate_90_deg(p_factor, p_pole).to_vector();
      
      clear_derived_data();
      }

   @Override
   public void rotate_deg(int p_angle_in_degree, PlaPointFloat p_pole)
      {
      if (side_changed && r_board.brd_components.get_flip_style_rotate_first())
         {
         p_angle_in_degree = 360 - p_angle_in_degree;
         }

      rotation_in_degree = rotate_deg_reduce ( rotation_in_degree + p_angle_in_degree );

      PlaPointFloat new_translation = translation.to_float().rotate_rad(Math.toRadians(p_angle_in_degree), p_pole);
      translation = new_translation.to_vector();
      clear_derived_data();
      }

   @Override
   public void change_placement_side(PlaPointInt p_pole)
      {
      side_changed = !side_changed;

      // why is it changing layer ? damiano
      layer_no = r_board.get_layer_count() - layer_no - 1;

      PlaPointInt rel_location = PlaPointInt.ZERO.translate_by(translation);
      
      translation = rel_location.mirror_vertical(p_pole).to_vector();
      
      clear_derived_data();
      }

   @Override
   public boolean is_selected_by_filter(ItemSelectionFilter p_filter)
      {
      if (!is_selected_by_fixed_filter(p_filter)) return false;

      return p_filter.is_selected(ItemSelectionChoice.KEEPOUT);
      }

   @Override
   public Color[] get_draw_colors(GdiContext p_graphics_context)
      {
      return p_graphics_context.get_obstacle_colors();
      }

   @Override
   public double get_draw_intensity(GdiContext p_graphics_context)
      {
      return p_graphics_context.get_obstacle_color_intensity();
      }

   @Override
   public int get_draw_priority()
      {
      return freert.graphics.GdiDrawable.MIN_DRAW_PRIORITY;
      }

   @Override
   public void draw(java.awt.Graphics p_g, GdiContext p_graphics_context, Color[] p_color_arr, double p_intensity)
      {
      if (p_graphics_context == null || p_intensity <= 0) return;

      Color color = p_color_arr[layer_no];
      double intensity = p_graphics_context.get_layer_visibility(layer_no) * p_intensity;
      p_graphics_context.fill_area(get_area(), p_g, color, intensity);
      if (display_tree_shapes && intensity > 0 )
         {
         AwtreeShapeSearch default_tree = r_board.search_tree_manager.get_default_tree();
         for (int i = 0; i < tree_shape_count(default_tree); ++i)
            {
            p_graphics_context.draw_boundary(get_tree_shape(default_tree, i), 1, Color.white, p_g, 1);
            }
         }
      }

   @Override
   public int shape_layer(int p_index)
      {
      return layer_no;
      }

   public PlaVectorInt get_translation()
      {
      return translation;
      }

   public int get_rotation_in_degree()
      {
      return rotation_in_degree;
      }

   public boolean get_side_changed()
      {
      return side_changed;
      }

   @Override
   public void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("keepout"));
      int cmp_no = get_component_no();
      if (cmp_no > 0)
         {
         p_window.append(" " + resources.getString("of_component") + " ");
         BrdComponent component = r_board.brd_components.get(cmp_no);
         p_window.append(component.name, resources.getString("component_info"), component);
         }
      print_shape_info(p_window, p_locale);
      print_item_info(p_window, p_locale);
      p_window.newline();
      }

   /**
    * Used in the implementation of print_info for this class and derived classes.
    */
   protected final void print_shape_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append(" " + resources.getString("at") + " ");
      freert.planar.PlaPointFloat center = get_area().get_border().centre_of_gravity();
      p_window.append(center);
      Integer hole_count = relative_area.get_holes().length;
      if (hole_count > 0)
         {
         p_window.append(" " + resources.getString("with") + " ");
         java.text.NumberFormat nf = java.text.NumberFormat.getInstance(p_locale);
         p_window.append(nf.format(hole_count));
         if (hole_count == 1)
            {
            p_window.append(" " + resources.getString("hole"));
            }
         else
            {
            p_window.append(" " + resources.getString("holes"));
            }
         }
      p_window.append(" " + resources.getString("on_layer") + " ");
      p_window.append(r_board.layer_structure.get_name(get_layer()));
      }

   public ShapeTile[] split_to_convex()
      {
      if (relative_area == null)
         {
         System.out.println("ObstacleArea.split_to_convex: area is null");
         return null;
         }
      return get_area().split_to_convex();
      }

   @Override
   public void clear_derived_data()
      {
      super.clear_derived_data();
      
      precalculated_absolute_area = null;
      }

   @Override
   public boolean write(java.io.ObjectOutputStream p_stream)
      {
      try
         {
         p_stream.writeObject(this);
         }
      catch (java.io.IOException e)
         {
         return false;
         }
      return true;
      }

   }