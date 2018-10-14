package de.upb.soot.namespaces;

import static com.google.common.base.Strings.isNullOrEmpty;

import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link INamespace} interface for the Java class path. Handles directories, archives (including
 * wildcard denoted archives) as stated in the official documentation:
 * https://docs.oracle.com/javase/8/docs/technotes/tools/windows/classpath.html
 * 
 * @author Manuel Benz created on 22.05.18
 */
public class JavaClassPathNamespace extends AbstractNamespace {
  private static final Logger logger = LoggerFactory.getLogger(JavaClassPathNamespace.class);
  private static final String WILDCARD_CHAR = "*";

  protected Collection<AbstractNamespace> cpEntries;

  /**
   * Creates a {@link JavaClassPathNamespace} which locates classes in the given class path.
   * 
   * @param classPath
   *          The class path to search in
   */
  public JavaClassPathNamespace(String classPath) {
    this(classPath, getDefaultClassProvider());
  }

  public JavaClassPathNamespace(String classPath, IClassProvider provider) {
    super(provider);
    if (isNullOrEmpty(classPath)) {
      throw new InvalidClassPathException("Empty class path given");
    }

    try {
      cpEntries = explode(classPath).flatMap(cp -> Utils.optionalToStream(nsForPath(cp))).collect(Collectors.toList());
    } catch (IllegalArgumentException e) {
      throw new InvalidClassPathException("Malformed class path given: " + classPath, e);
    }

    if (cpEntries.isEmpty()) {
      throw new InvalidClassPathException("Empty class path given");
    }

    logger.trace("{} class path entries registered", cpEntries.size());
  }


  /**
   * Explode the class or modulepath entries, separated by {@link File#pathSeparator}.
   *
   * @param paths
   *          entries as one string
   * @return path entries
   */
  public static Stream<Path> explode(String paths) {
    // the classpath is split at every path separator which is not escaped
    String regex = "(?<!\\\\)" + Pattern.quote(File.pathSeparator);
    final Stream<Path> exploded = Stream.of(paths.split(regex)).flatMap(JavaClassPathNamespace::handleWildCards);
    // we need to filter out duplicates of the same files to not generate duplicate namespaces
    return exploded.map(cp -> cp.normalize()).distinct();
  }

  /**
   * The class path can have directories with wildcards as entries. All jar/JAR files inside those directories have to be
   * added to the class path.
   *
   * @param entry
   *          A class path entry
   * @return A stream of class path entries with wildcards exploded
   */
  private static Stream<Path> handleWildCards(String entry) {
    if (entry.endsWith(WILDCARD_CHAR)) {
      Path baseDir = Paths.get(entry.substring(0, entry.indexOf(WILDCARD_CHAR)));
      try {
        return Utils.iteratorToStream(Files.newDirectoryStream(baseDir, "*.{jar,JAR}").iterator());
      } catch (PatternSyntaxException | NotDirectoryException e) {
        throw new InvalidClassPathException("Malformed wildcard entry", e);
      } catch (IOException e) {
        throw new InvalidClassPathException("Couldn't access entries denoted by wildcard", e);
      }
    } else {
      return Stream.of(Paths.get(entry));
    }
  }

  @Override
  public Collection<AbstractClassSource> getClassSources(SignatureFactory factory) {
    // By using a set here, already added classes won't be overwritten and the class which is found
    // first will be kept
    Set<AbstractClassSource> found = new HashSet<>();
    for (AbstractNamespace ns : cpEntries) {
      found.addAll(ns.getClassSources(factory));
    }
    return found;
  }

  @Override
  public Optional<AbstractClassSource> getClassSource(ClassSignature signature) {
    for (AbstractNamespace ns : cpEntries) {
      final Optional<AbstractClassSource> classSource = ns.getClassSource(signature);
      if (classSource.isPresent()) {
        return classSource;
      }
    }
    return Optional.empty();
  }

  private Optional<AbstractNamespace> nsForPath(Path path) {
    if (Files.exists(path) && (java.nio.file.Files.isDirectory(path) || PathUtils.isArchive(path))) {
      return Optional.of(PathBasedNamespace.createForClassContainer(path));
    } else {
      logger.warn("Invalid/Unknown class path entry: " + path);
      return Optional.empty();
    }
  }

  protected static final class InvalidClassPathException extends IllegalArgumentException {
    /**
     * 
     */
    private static final long serialVersionUID = -5130658516046902470L;

    public InvalidClassPathException(String s) {
      super(s);
    }

    public InvalidClassPathException(String message, Throwable cause) {
      super(message, cause);
    }

    public InvalidClassPathException(Throwable cause) {
      super(cause);
    }
  }
}