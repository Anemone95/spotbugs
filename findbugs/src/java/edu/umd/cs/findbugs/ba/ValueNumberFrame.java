/*
 * Bytecode Analysis Framework
 * Copyright (C) 2003,2004 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs.ba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A dataflow value representing a Java stack frame with value number
 * information.
 *
 * @see ValueNumber
 * @see ValueNumberAnalysis
 * @author David Hovemeyer
 */
public class ValueNumberFrame extends Frame<ValueNumber> implements ValueNumberAnalysisFeatures {

	private ValueNumberFactory factory;
	private ArrayList<ValueNumber> mergedValueList;
	private Map<AvailableLoad, ValueNumber[]> availableLoadMap;

	public ValueNumberFrame(int numLocals, final ValueNumberFactory factory) {
		super(numLocals);
		this.factory = factory;
		if (REDUNDANT_LOAD_ELIMINATION) {
			this.availableLoadMap = new HashMap<AvailableLoad, ValueNumber[]>();
		}
	}

	/**
	 * Look for an available load.
	 * @param availableLoad the AvailableLoad (reference and field)
	 * @return the value(s) available, or null if no matching entry is found
	 */
	public ValueNumber[] getAvailableLoad(AvailableLoad availableLoad) {
		return availableLoadMap.get(availableLoad);
	}

	/**
	 * Add an available load.
	 * @param availableLoad the AvailableLoad (reference and field)
	 * @param value the value(s) loaded
	 */
	public void addAvailableLoad(AvailableLoad availableLoad, ValueNumber[] value) {
		if (value == null) throw new IllegalStateException();
		availableLoadMap.put(availableLoad, value);
	}

	/**
	 * Kill all loads of given field.
	 * @param field the field
	 */
	public void killLoadsOfField(XField field) {
		Iterator<AvailableLoad> i = availableLoadMap.keySet().iterator();
		while (i.hasNext()) {
			AvailableLoad availableLoad = i.next();
			if (availableLoad.getField().equals(field)) {
				i.remove();
			}
		}
	}

	public void mergeWith(Frame<ValueNumber> other_) throws DataflowAnalysisException {
		ValueNumberFrame other = (ValueNumberFrame) other_;

		if (REDUNDANT_LOAD_ELIMINATION) {
			// Merge available load sets.
			// Only loads that are available in both frames
			// remain available. All others are discarded.
			if (other.isBottom())
				this.availableLoadMap.clear();
			else if (!other.isTop())
				this.availableLoadMap.entrySet().retainAll(other.availableLoadMap.entrySet());
		}

		// Merge slot values.
		super.mergeWith(other);
	}

	public ValueNumber mergeValues(int slot, ValueNumber mine, ValueNumber other) {
		// Merging slot values:
		//   - Merging identical values results in no change
		//   - If the values are different, and the value in the result
		//     frame is not the result of a previous result, a fresh value
		//     is allocated.
		//   - If the value in the result frame is the result of a
		//     previous merge, IT STAYS THE SAME.
		//
		// The "one merge" rule means that merged values are essentially like
		// phi nodes.  They combine some number of other values.

		// I believe (but haven't proved) that this technique is a dumb way
		// of computing SSA.

		if (mine != getValue(slot)) throw new IllegalStateException();

		if (mine.equals(other))
			return mine;

		ValueNumber mergedValue = mergedValueList.get(slot);
		if (mergedValue == null) {
			mergedValue = factory.createFreshValue();
			mergedValue.setFlags(mine.getFlags() | other.getFlags());
			mergedValueList.set(slot, mergedValue);
		}

		return mergedValue;
	}

	public ValueNumber getDefaultValue() {
		// Default values should never be looked at.
		return null;
	}

	public void copyFrom(Frame<ValueNumber> other) {
		// If merged value list hasn't been created yet, create it.
		if (mergedValueList == null && other.isValid()) {
			// This is where this frame gets its size.
			// It will have the same size as long as it remains valid.
			mergedValueList = new ArrayList<ValueNumber>();
			int numSlots = other.getNumSlots();
			for (int i = 0; i < numSlots; ++i)
				mergedValueList.add(null);
		}

		if (REDUNDANT_LOAD_ELIMINATION) {
			// Copy available load set.
			availableLoadMap.clear();
			availableLoadMap.putAll(((ValueNumberFrame)other).availableLoadMap);
		}

		super.copyFrom(other);
	}

	public String toString() {
		String frameValues = super.toString();
		if (RLE_DEBUG) {
			StringBuffer buf = new StringBuffer();
			buf.append(frameValues);
			Iterator<Map.Entry<AvailableLoad,ValueNumber[]>> i = availableLoadMap.entrySet().iterator();
			boolean first = true;
			while (i.hasNext()) {
				Map.Entry<AvailableLoad,ValueNumber[]> entry = i.next();
				if (first)
					first = false;
				else
					buf.append(',');
				buf.append(entry.getKey() + "=" + valueToString(entry.getValue()));
			}
			return buf.toString();
		} else {
			return frameValues;
		}
	}

	private static String valueToString(ValueNumber[] valueNumberList) {
		StringBuffer buf = new StringBuffer();
		buf.append('[');
		boolean first = true;
		for (int i = 0; i < valueNumberList.length; ++i) {
			if (first)
				first = false;
			else
				buf.append(',');
			buf.append(valueNumberList[i].getNumber());
		}
		buf.append(']');
		return buf.toString();
	}
}

// vim:ts=4
