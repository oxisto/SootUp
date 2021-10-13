package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 *
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 *
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.types.Type;
import soot.Scene;
import soot.SootField;
import soot.tagkit.*;

/**
 * This class represents all instance and static fields of a dex class. It holds its name, its modifier, and the type
 */
public class DexField {
  private DexField() {
  }

  /**
   * Add constant tag. Should only be called if field is final.
   * 
   * @param df
   * @param sf
   */
  private static void addConstantTag(Field df, Field sf) {
    Tag tag = null;

    EncodedValue ev = sf.getInitialValue();

    if (ev instanceof BooleanEncodedValue) {
      tag = new IntegerConstantValueTag(((BooleanEncodedValue) ev).getValue() == true ? 1 : 0);
    } else if (ev instanceof ByteEncodedValue) {
      tag = new IntegerConstantValueTag(((ByteEncodedValue) ev).getValue());
    } else if (ev instanceof CharEncodedValue) {
      tag = new IntegerConstantValueTag(((CharEncodedValue) ev).getValue());
    } else if (ev instanceof DoubleEncodedValue) {
      tag = new DoubleConstantValueTag(((DoubleEncodedValue) ev).getValue());
    } else if (ev instanceof FloatEncodedValue) {
      tag = new FloatConstantValueTag(((FloatEncodedValue) ev).getValue());
    } else if (ev instanceof IntEncodedValue) {
      tag = new IntegerConstantValueTag(((IntEncodedValue) ev).getValue());
    } else if (ev instanceof LongEncodedValue) {
      tag = new LongConstantValueTag(((LongEncodedValue) ev).getValue());
    } else if (ev instanceof ShortEncodedValue) {
      tag = new IntegerConstantValueTag(((ShortEncodedValue) ev).getValue());
    } else if (ev instanceof StringEncodedValue) {
      tag = new StringConstantValueTag(((StringEncodedValue) ev).getValue());
    }

    if (tag != null) {
      df.addTag(tag);
    }
  }

  /**
   *
   * @return the Soot equivalent of a field
   */
  public static Field makeSootField(Field f) {
    String name = f.getName();
    Type type = DexType.toSoot(f.getType());
    int flags = f.getAccessFlags();
    Field sf = Scene.v().makeSootField(name, type, flags);
    DexField.addConstantTag(sf, f);
    return sf;
  }
}