//    Chromis POS  - The New Face of Open Source POS
//    Copyright (c) 2015 
//    http://www.chromis.co.uk
//
//    This file is part of Chromis POS
//
//     Chromis POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Chromis POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Chromis POS.  If not, see <http://www.gnu.org/licenses/>.

package uk.chromis.data.loader;

import uk.chromis.basic.BasicException;

/**
 *
 *   
 */
public interface DataResultSet extends DataRead {
    
    /**
     *
     * @return
     * @throws BasicException
     */
    public Object getCurrent() throws BasicException;

    /**
     *
     * @return
     * @throws BasicException
     */
    public boolean next() throws BasicException;    

    /**
     *
     * @throws BasicException
     */
    public void close() throws BasicException;
    
    /**
     *
     * @return
     * @throws BasicException
     */
    public int updateCount() throws BasicException;           
}