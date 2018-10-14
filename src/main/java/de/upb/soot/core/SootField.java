package de.upb.soot.core;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 1999 Raja Vallee-Rai
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

import de.upb.soot.jimple.common.type.RefLikeType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.views.IView;

/**
 * Soot's counterpart of the source language's field concept. Soot representation of a Java field. Can be declared to belong
 * to a SootClass.
 *
 * Modified by Linghui Luo
 *
 */

public class SootField extends AbstractViewResident implements ClassMember {

  protected String name;
  protected Type type;
  protected int modifiers;
  protected boolean isDeclared = false;
  protected SootClass declaringClass;
  protected boolean isPhantom = false;
  protected volatile String sig;
  protected volatile String subSig;


  /** Constructs a Soot field with the given name, type and modifiers. */
  public SootField(IView view, String name, Type type, int modifiers) {
    super(view);
    if (name == null || type == null) {
      throw new RuntimeException("A SootField cannot have a null name or type.");
    }
    this.name = name;
    this.type = type;
    this.modifiers = modifiers;
  }

  public SootField(IView view, SootField field)
  {
    this(view, field.name, field.type, field.modifiers);
  }

  /** Constructs a Soot field with the given name, type and no modifiers. */
  public SootField(IView view, String name, Type type) {
    this(view, name, type, 0);
  }

  public int equivHashCode() {
    return type.hashCode() * 101 + modifiers * 17 + name.hashCode();
  }

  public String getSignature() {
    if (sig == null) {
      synchronized (this) {
        if (sig == null) {
          sig = getSignature(getDeclaringClass(), getSubSignature());
        }
      }
    }
    return sig;
  }

  public String getSignature(SootClass cl, String name, Type type) {
    return getSignature(cl, getSubSignature(name, type));
  }

  public String getSignature(SootClass cl, String subSignature) {
    StringBuilder buffer = new StringBuilder();

    buffer.append("<").append(this.getView().quotedNameOf(cl.getName())).append(": ");
    buffer.append(subSignature).append(">");

    return buffer.toString();

  }

  public String getSubSignature() {
    if (subSig == null) {
      synchronized (this) {
        if (subSig == null) {
          subSig = getSubSignature(getName(), getType());
        }
      }
    }
    return subSig;
  }

  private String getSubSignature(String name, Type type) {
    StringBuilder buffer = new StringBuilder();
    buffer.append(type.toQuotedString() + " " + this.getView().quotedNameOf(name));
    return buffer.toString();
  }

  @Override
  public SootClass getDeclaringClass() {
    if (!isDeclared) {
      throw new RuntimeException("not declared: " + getName() + " " + getType());
    }

    return declaringClass;
  }

  public synchronized void setDeclaringClass(SootClass sc) {
    if (sc != null && type instanceof RefLikeType) {
      this.getView().getFieldNumberer().add(this);
    }
    this.declaringClass = sc;
    this.sig = null;
  }

  @Override
  public boolean isPhantom() {
    return isPhantom;
  }

  @Override
  public void setPhantom(boolean value) {
    if (value) {
      if (!this.getView().allowsPhantomRefs()) {
        throw new RuntimeException("Phantom refs not allowed");
      }
      if (!this.getView().getOptions().allow_phantom_elms() && declaringClass != null && !declaringClass.isPhantomClass()) {
        throw new RuntimeException("Declaring class would have to be phantom");
      }
    }
    isPhantom = value;
  }

  @Override
  public boolean isDeclared() {
    return isDeclared;
  }

  public void setDeclared(boolean isDeclared) {
    this.isDeclared = isDeclared;
  }

  public String getName() {
    return name;
  }

  public synchronized void setName(String name) {
    if (name != null) {
      this.name = name;
      this.sig = null;
      this.subSig = null;
    }
  }

  public Type getType() {
    return type;
  }

  public synchronized void setType(Type t) {
    if (t != null) {
      this.type = t;
      this.sig = null;
      this.subSig = null;
    }
  }

  /**
   * Convenience method returning true if this field is public.
   */
  @Override
  public boolean isPublic() {
    return Modifier.isPublic(this.getModifiers());
  }

  /**
   * Convenience method returning true if this field is protected.
   */
  @Override
  public boolean isProtected() {
    return Modifier.isProtected(this.getModifiers());
  }

  /**
   * Convenience method returning true if this field is private.
   */
  @Override
  public boolean isPrivate() {
    return Modifier.isPrivate(this.getModifiers());
  }

  /**
   * Convenience method returning true if this field is static.
   */
  @Override
  public boolean isStatic() {
    return Modifier.isStatic(this.getModifiers());
  }

  /**
   * Convenience method returning true if this field is final.
   */
  public boolean isFinal() {
    return Modifier.isFinal(this.getModifiers());
  }

  @Override
  public void setModifiers(int modifiers) {
    this.modifiers = modifiers;
  }

  @Override
  public int getModifiers() {
    return modifiers;
  }

  @Override
  public String toString() {
    return getSignature();
  }

  private String getOriginalStyleDeclaration() {
    String qualifiers = Modifier.toString(modifiers) + " " + type.toQuotedString();
    qualifiers = qualifiers.trim();

    if (qualifiers.isEmpty()) {
      return this.getView().quotedNameOf(name);
    } else {
      return qualifiers + " " + this.getView().quotedNameOf(name) + "";
    }

  }

  public String getDeclaration() {
    return getOriginalStyleDeclaration();
  }
}