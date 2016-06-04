package board.infos;
/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
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
 */

import gui.varie.ObjectInfoPanel;


/**
 * Functionality needed for objects to print information into an ObjectInfoWindow
 */
public interface PrintableInfo
   {
   /**
    * Prints information about an ObjectInfoWindow.Printable object into the input window.
    */
   void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale);
   }
