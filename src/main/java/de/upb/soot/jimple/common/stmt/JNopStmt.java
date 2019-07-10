/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
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
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.soot.jimple.common.stmt;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.visitor.StmtVisitor;
import de.upb.soot.jimple.visitor.Visitor;
import de.upb.soot.util.Copyable;
import de.upb.soot.util.printer.StmtPrinter;
import javax.annotation.Nonnull;

public final class JNopStmt extends AbstractStmt implements Copyable {

  public JNopStmt(PositionInfo positionInfo) {
    super(positionInfo);
  }

  @Override
  public String toString() {
    return Jimple.NOP;
  }

  @Override
  public void toString(StmtPrinter up) {
    up.literal(Jimple.NOP);
  }

  @Override
  public void accept(Visitor sw) {
    ((StmtVisitor) sw).caseNopStmt(this);
  }

  @Override
  public boolean fallsThrough() {
    return true;
  }

  @Override
  public boolean branches() {
    return false;
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseNopStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return 42;
  }

  @Nonnull
  public JNopStmt withPositionInfo(PositionInfo positionInfo) {
    return new JNopStmt(positionInfo);
  }
}
