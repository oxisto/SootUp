package de.upb.soot.signatures;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018 Secure Software Engineering Department, University of Paderborn
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

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import de.upb.soot.namespaces.FileType;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

/** Represents the unique fully-qualified name of a Class (aka its signature). */
public class ClassSignature extends TypeSignature {

  /** The simple class name. */
  public final String className;

  /** The package in which the class resides. */
  public final PackageSignature packageSignature;

  /**
   * Internal: Constructs the fully-qualified ClassSignature. Instances should only be created by a {@link SignatureFactory}
   *
   * @param className
   *          the simple name of the class, e.g., ClassA NOT my.package.ClassA
   * @param packageSignature
   *          the corresponding package
   */
  protected ClassSignature(final String className, final PackageSignature packageSignature) {
    this.className = className;
    this.packageSignature = packageSignature;
  }

  public static ClassSignature fromPath(Path path, SignatureFactory fac) {
    return fac.getClassSignature(FilenameUtils.removeExtension(path.toString()).replace('/', '.'));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClassSignature that = (ClassSignature) o;
    return Objects.equal(className, that.className) && Objects.equal(packageSignature, that.packageSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(className, packageSignature);
  }

  /**
   * The fully-qualified name of the class. Concat package and class name , e.g., "java.lang.System".
   *
   * @return fully-qualified name
   */
  public String getFullyQualifiedName() {
    StringBuilder sb = new StringBuilder();
    if (!Strings.isNullOrEmpty(packageSignature.packageName)) {
      sb.append(packageSignature.toString());
      sb.append('.');
    }
    sb.append(className);
    return sb.toString();
  }

  @Override
  public String toString() {
    return getFullyQualifiedName();
  }

  public Path toPath(FileType fileType) {
    return toPath(fileType, FileSystems.getDefault());
  }

  public Path toPath(FileType fileType, FileSystem fs) {
    return fs.getPath(getFullyQualifiedName().replace('.', '/') + "." + fileType.getExtension());
  }
}
