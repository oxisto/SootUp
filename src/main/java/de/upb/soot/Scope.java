package de.upb.soot;

import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.PackageSignature;

/**
 * Definition of a scope
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public class Scope {

  /**
   * Define a scope consists of multiple namespaces.
   * 
   * @param namespaces
   */
  public Scope(INamespace... namespaces) {
    // TODO Auto-generated constructor stub
  }

  /**
   * Define a scope consists of multiple packages.
   * 
   * @param packages
   */
  public Scope(PackageSignature... packages) {
    // TODO Auto-generated constructor stub
  }

  /**
   * Define a scope consists of multiple classes.
   * 
   * @param classSignatures
   */
  public Scope(ClassSignature... classSignatures) {
    // TODO Auto-generated constructor stub
  }
}