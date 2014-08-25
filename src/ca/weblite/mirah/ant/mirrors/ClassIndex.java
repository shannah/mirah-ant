package ca.weblite.mirah.ant.mirrors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class ClassIndex {
    
    private boolean fullyScanned = false;
    
    /**
     * A cache of the paths and patterns that have been searched already.
     */
    private final Set<String> scanPatternHistory = new HashSet<>();
    
    
    /**
     * A flag indicating whether we can "cheat" when we scan paths based
     * on the pattern that we're searching for.
     */
    private boolean narrowScanScopeWithPattern = false;
    
    
    public boolean isNarrowScanScopeWithPattern(){
        return narrowScanScopeWithPattern;
    }            
    
    
    public void setNarrowScanScopeWithPattern(boolean narrow){
        this.narrowScanScopeWithPattern = narrow;
    }
    /**
     * Clears the pattern search history.  
     */
    public void clearCache(){
        fullyScanned = false;
        scanPatternHistory.clear();
        
    }

    /**
     * Scans a directory for classes.
     * @param f The directory to scan.
     * @param root The root directory of the classpath.
     * @param pattern Filter pattern in the form of a java import directive. 
     *  E.g. com.example.pkg.* or com.example.pkg.ExampleClass
     * 
     * @throws IOException 
     */
    protected void scanDirectory(File f, File root, String pattern) 
            throws IOException {
        if ( f.equals(root)){
            if ( isAlreadyScanned(f.getPath(), pattern)){
                return;
            } else {
                markScanned(f.getPath(), pattern);
            }
        }
        
        
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(f.toPath())){
            for ( Path p : ds){
                if ( p.toFile().isDirectory() ){
                    scanDirectory(p.toFile(), root, pattern);
                } else {
                    scanFile(p.toFile(), root, pattern);
                    
                }
            }
        } 
    }

    /**
     * Scans a jar file for classes.
     * @param f The jar file
     * @param pattern Filter patter in the form of a java import directive.
     * @throws IOException 
     */
    protected void scanJar(File f, String pattern) throws IOException {
        if ( isAlreadyScanned(f.getPath(), pattern)){
            return;
        } else {
            markScanned(f.getPath(), pattern);
        }
        if ( !f.exists() ){
            return;
        }
        JarFile jar = new JarFile(f);
        Enumeration<JarEntry> entries = jar.entries();
        while ( entries.hasMoreElements() ){
            JarEntry entry = entries.nextElement();
            for ( Indexer idx : indexers){
                if ( idx.canIndex(entry.getName(), entry.getName(), pattern)){
                    
                    InputStream is = null;
                    try {
                        if ( idx.indexingRequiresFileContents(
                                entry.getName(), 
                                entry.getName(), 
                                pattern)
                                ){
                            is = jar.getInputStream(entry);
                        }
                        
                        idx.index(entry.getName(), 
                                entry.getName(), 
                                is, 
                                this, 
                                pattern
                        );
                        break;
                    } finally {
                        if ( is != null ){
                            try { 
                                is.close();
                            } catch (IOException ex){}
                        }
                    }
                }
            }
        }
    }
    
    
    

    /**
     * Scans a file for classes.  Adds classes matching the pattern to the 
     * index.
     * @param f The file to scan.
     * @param root The root file of the classpath.
     * @param pattern The pattern to match.  This can be any valid pattern that 
     * would be included in a java import statement.  E.g. java.util.* or 
     * java.util.Date
     * @throws IOException 
     */
    protected void scanFile(File f, File root, String pattern) 
            throws IOException {
        URI u = f.toURI();
        String relPath = u.getPath().substring(root.toURI().getPath().length());
        if ( relPath.startsWith("/") ){
            relPath = relPath.substring(1);
        }
        for ( Indexer idx : indexers ){
            if ( idx.canIndex(f.getPath(), relPath, pattern)){
                FileInputStream fis = null;
                try {
                    if ( idx.indexingRequiresFileContents(
                            f.getPath(), 
                            relPath, 
                            pattern)
                            ){
                        fis = new FileInputStream(f);
                    }
                    idx.index(f.getPath(), relPath, fis, this, pattern);
                    break;
                } finally {
                    if ( fis != null ){
                        try {
                            fis.close();
                        } catch (IOException ex){}
                    }
                }
            }
        }
    }
    
    /**
     * A node type.  Each node in the index is either a package or a class.
     */
    public static enum NodeType {
        PACKAGE,
        CLASS
    }
    
    /**
     * A single node in the index.  This represents either a class or a package.
     */
    public static class Node {
        
        
        /**
         * The simple name of the node.  E.g. Date
         */
        public String simpleName;
        
        /**
         * The internal name of the node. E.g. java/util/Date or 
         * java/util/Date$InnerClass
         */
        public String internalName;
        
        /**
         * The type of the node.  Either package or class.
         */
        public NodeType type;
        
        /**
         * The children of this node.
         */
        public Map<String,Node> children = new HashMap<>();
        
        public String fullyQualifiedName(){
            return internalName.replaceAll("/", ".").replaceAll("\\$", ".");
        }
        
        public String fullyQualifiedArrayName(int dim){
            if ( dim == 0 ){
                return fullyQualifiedName();
            } else {
                return TypeUtil.getArrayType(fullyQualifiedName(), dim);
            }
        }
        
        public String descriptor(){
            return TypeUtil.getDescriptor(internalName);
        }
        
        public String arrayDescriptor(int dim){
            if ( dim == 0 ){
                return descriptor();
            } else {
                return TypeUtil.getArrayDescriptor(internalName, dim);
            }
        }
    }
    
    
    /**
     * An interface that can be implemented by classes that wish to index 
     * files.  There will be one indexer for each file type.
     */
    public static interface Indexer {
        /**
         * Checks to see if a particular path can be indexed by this indexer.
         * @param path The path to the file that is being indexed.  This might
         * not be a valid path in the file system.  It could be a path within 
         * a jar file or a path in the file system.  This path may be using
         * the current platform's structure.  E.g. C:\path\to\file.java on
         * Windows and /path/to/file.java on *nix systems.
         * @param relPath The relative path from the classpath root to this file
         * using "/" as the separator in all cases. E.g. java/lang/Object.class.
         * @param pattern A pattern to filter whether this file should be 
         * indexed.  This pattern is any valid pattern that could be passed
         * to an import statement.  E.g. java.util.* or java.util.Date
         * @return True if this indexer should be allowed to index this file. 
         * This should return false either if the indexer doesn't not know to 
         * index this type of file, or if it knows that the requested class 
         * pattern would not match any classes that could be found in this file.
         */
        public boolean canIndex(
                String path, 
                String relPath, 
                String pattern);
        
        
        /**
         * Returns true if the indexer will need to look at the file contents
         * in order to index it. This is used by the ClassIndex or efficiency.
         * If it returns true, then the class index will open an input stream
         * to the file and pass this input stream to the {@link #index} method
         * when it calls it.
         * @param path The path to the file that is being indexed.  This might
         * not be a valid path in the file system. It could be a path within 
         * a jar file or a path in the file system.  This path may be using
         * the current platform's structure.  E.g. C:\path\to\file.java on
         * Windows and /path/to/file.java on *nix systems.
         * @param relPath The relative path from the classpath root to this file
         * using "/" as the separator in all cases. E.g. java/lang/Object.class.
         * @param pattern A pattern to filter whether this file should be 
         * indexed.  This pattern is any valid pattern that could be passed
         * to an import statement.  E.g. java.util.* or java.util.Date
         * @return True if this indexer should be allowed to index this file. 
         * This should return false either if the indexer doesn't not know to 
         * index this type of file, or if it knows that the requested class 
         * pattern would not match any classes that could be found in this file.
         */
        public boolean indexingRequiresFileContents(
                String path, 
                String relPath, 
                String pattern);
        
        /**
         * Indexes the specified file, adding any classes matching the pattern
         * to the index.
         * @param path The path to the file that is being indexed.  This might
         * not be a valid path in the file system. It could be a path within 
         * a jar file or a path in the file system.  This path may be using
         * the current platform's structure.  E.g. C:\path\to\file.java on
         * Windows and /path/to/file.java on *nix systems.
         * @param relPath The relative path from the classpath root to this file
         * using "/" as the separator in all cases. E.g. java/lang/Object.class.
         * @param contents A stream with the contents of the file.  If the 
         * {@link #indexingRequiresFileContents} method returns false, then
         * the index will pass null to this parameter.  If true, it will pass
         * the stream with the file contents.
         * @param index The index into which to add any found classes.
         * @param pattern A pattern to filter whether this file should be 
         * indexed.  This pattern is any valid pattern that could be passed
         * to an import statement.  E.g. java.util.* or java.util.Date
         * @throws IOException 
         */
        public void index(
                String path, 
                String relPath, 
                InputStream contents, 
                ClassIndex index, 
                String pattern) 
                throws IOException;
        
        
        
        
    }
    
    
    
    /**
     * The root node of the class path.  All class searches begin here.
     */
    private final Node root=new Node();
    
    /**
     * The indexers that are registered to index files for this index.  
     * Typically there is one indexer per file type.
     */
    private final List<Indexer> indexers = new ArrayList<>();
    
    /**
     * Finds the node at the specified path.
     * @param path The path to the package or class using java FQN dot notation
     * e.g. "java.util.Date" or "java.util.Date.InnerClass"
     * <p>It is worth noting that this will only check for already scanned files
     * in the index.  It will not initiate a scan so it is a fast operation.</p>
     * @return The matching node or null if there is none.
     */
    public Node find(String path){
        String[] parts = path.split("\\.");
        Node node = root;
        for ( String part : parts ){
            if ( node.children.containsKey(part)){
                node = node.children.get(part);
            } else {
                
                return null;
            }
        }
        return node;
    }
    
    /**
     * Adds a package to the index at the given path.  If a package or class
     * already exists at this path, it will return the existing node.  
     * <p><em>Warning</em>: This may return the node for a class if there 
     * already exists a class at this path.
     * @param path The path to the package to add using FQN dot notation. E.g.
     * "java.util" or "com.codename1.ui"
     * @return The node for the specified package, or the node for an existing
     * class if there is a class already indexed with that FQN.
     */
    public Node addPackage(String path){
        Node existing = find(path);
        if ( existing == null ){
            existing = new Node();
            existing.simpleName = path;
            int pos;
            if ( (pos =path.lastIndexOf(".")) != -1 ){
                existing.simpleName = existing.simpleName.substring(pos+1);
            }
            existing.internalName = path.replaceAll("\\.", "/");
            existing.type = NodeType.PACKAGE;
            
            Node parent = root;
            if ( pos != -1 ){
                parent = addPackage(path.substring(0,pos));
            }
            parent.children.put(existing.simpleName, existing);
        }
        return existing;
    }
    
    /**
     * Adds a class to the index at the given path with the specified internal
     * name. If a class or package was previously added at the same path, then
     * this will just return the node for that existing class <em>or package
     * </em>.
     * @param path The path to the class to add in its FQN dot notation.  E.g.
     * "java.util.Date" or "java.util.Date.InnerClass".
     * @param internalName The internal name to the class.  This is the internal
     * representation as recognized in the JVM.  E.g. "java/util/Date" or
     * "java/util/Date$InnerClass"
     * @return The node to the class that was added or an existing class/package
     * if one already exists at this location.
     */
    public Node addClass(String path, String internalName){
        Node existing = find(path);
        if ( existing == null ){
            existing = new Node();
            
            existing.internalName = internalName;
            existing.type = NodeType.CLASS;
            
            int pos;
            if ( (pos = path.lastIndexOf(".")) != -1 ){
                existing.simpleName = path.substring(pos+1);
            } else {
                existing.simpleName = path;
            }
            Node parent = root;
            if ( pos != -1 ){
                parent = addPackage(path.substring(0, pos));
            }
            parent.children.put(existing.simpleName, existing);
            
        }
        return existing;
    }
    
    /**
     * Scans a path for files to index.  Any found files in the path, that 
     * registered indexers can handle, will be indexed, and classes contained 
     * therein added to the index.
     * @param path The path to scan.  This should be a valid classpath. I.e. 
     * you can separate multiple paths using the standard path separator char
     * (":" on *nix and ";" on Windows).  This supports both directories as
     * paths and jar files.
     * 
     * @throws IOException 
     */
    public final void scanPath(String path) throws IOException{
        scanPath(path, null);
    }
    
    /**
     * Generates the key that will be used in the {@link #scanPatternHistory}
     * 
     * @param path The path that is to be scanned.  This may be the path
     * to a directory or a jar file.
     * @param pattern The class pattern that is to be loaded.  E.g. java.util.*
     * or java.util.Date
     * @return A String key that is used in the {@link #scanPatternHistory}
     * map.
     */
    private String getScanPatternKey(String path, String pattern){
        if ( pattern == null ){
            pattern = "*";
        }
        String fullPattern = path+"?"+pattern;
        return fullPattern;
    }
    
    /**
     * Checks to see if the provided path has already been scanned.
     * @param path The path to a directory or jar file.
     * @param pattern The class pattern that is to be scanned. 
     * E.g. java.util.Date or java.util.*
     * @return True if the path/pattern combination has already been scanned.
     */
    private boolean isAlreadyScanned(String path, String pattern){
        if ( pattern == null ){
            pattern = "**";
        }
        return scanPatternHistory.contains("*") 
                || scanPatternHistory
                        .contains(getScanPatternKey(path, pattern))
                || scanPatternHistory
                        .contains(getScanPatternKey(path, 
                                pattern.replace("\\.[\\.]+$", ".*")));
    }
    
    /**
     * Marks a path/pattern pair as having already been scanned.
     * @param path The path to a directory or jar file.
     * @param pattern The class pattern to scan for.  E.g. java.util.* or 
     * java.util.Date
     */
    private void markScanned(String path, String pattern){
        String fullPattern = getScanPatternKey(path, pattern);
        scanPatternHistory.add(fullPattern);
    }
    
    /**
     * Scans a path for a particular pattern.
     * @param path The path to scan.  This should be a valid classpath. I.e. 
     * you can separate multiple paths using the standard path separator char
     * (":" on *nix and ";" on Windows).  This supports both directories as
     * paths and jar files.
     * @param pattern A classpath pattern on which to filter.  Only matching
     * classes will be added to the index.  Pattern should be in a form valid
     * for java import statements.  E.g. java.util.* or java.util.Date
     * @throws IOException 
     */
    public void scanPath(String path, String pattern) throws IOException {
        if ( fullyScanned ){
             return;
         }
         
         if ( path.indexOf(File.pathSeparator) != -1 ){
            String[] parts = path.split(Pattern.quote(File.pathSeparator));
            for ( String part : parts ){
                scanPath(part, pattern);
                fullyScanned = false;
            }
            
        } else {
        
            File f = new File(path);
            if ( f.isDirectory()){
                scanDirectory(f, f, pattern);
            } else if ( path.endsWith(".jar")){
                scanJar(f, pattern);
            } else if ( f.exists() ){
                scanFile(f, f, pattern);
            } 
         }
         if ( pattern == null ){
             fullyScanned = true;
         }
         
    }
    
    
    
    
    /**
     * Adds an indexer to this index.
     * @param indexer 
     */
    public void addIndexer(Indexer indexer){
        indexers.add(indexer);
    }
    
    /**
     * A class that is able to load an index
     */
    public static class Loader {
        
        
        public static enum LoaderType {
            BOOT_CLASSPATH,
            LIB_CLASSPATH,
            LIB_SOURCEPATH,
            BUILD_CLASSPATH,
            SOURCEPATH
            
        }
        
        private LoaderType type;
        private final Set<String> mimetypes = new HashSet<>();
        private String path;
        private final Map<String,String> paths = new HashMap<>();
        private final Loader parent;
        private ClassIndex index;
        
        public Loader(LoaderType type, Loader parent){
            this.parent = parent;
            this.type = type;
        }
        
        protected ClassIndex createClassIndex(){
            return new ClassIndex();
        }
        
        public ClassIndex getIndex(){
            if ( index == null ){
                index = createClassIndex();
            }
            return index;
        }
        
        public final void addMimetype(String type){
            mimetypes.add(type);
        }
        
        public final void removeMimetype(String type){
            mimetypes.remove(type);
        }
        
        public final  Set<String> getMimetypes(){
            return Collections.unmodifiableSet(mimetypes);
        }
        
        public final void setPath(
                String path, 
                String mimetype, 
                LoaderType type){
            if ( this.type == type && mimetypes.contains(mimetype)){
                this.paths.put(mimetype, path);
                StringBuilder sb = new StringBuilder();
                for ( String p : paths.values()){
                    sb.append(p).append(File.pathSeparator);
                }
                if ( sb.length() > 0 ){
                    sb.setLength(sb.length()-1);
                }
                this.path = sb.toString();
            }
            if ( parent != null ){
                parent.setPath(path, mimetype, type);
            }
        }
        
        public void clearCache(String withPath){
            if ( withPath == null ){
                getIndex().clearCache();
            } else if (path != null) {
                String[] paths = path.split(Pattern.quote(File.pathSeparator));
                for (String p : paths ){
                    if ( p.equals(withPath)){
                        getIndex().clearCache();
                        break;
                    }
                }
                
            }
            if ( parent != null ){
                parent.clearCache(withPath);
            }
        }
        
        public void clearCache(LoaderType forType){
            if ( forType == null ){
                getIndex().clearCache();
            } else if ( forType == type){
                getIndex().clearCache();
            }
            
            if ( parent != null ){
                parent.clearCache(forType);
            }
        }
        
        public void fillIndex() throws IOException{
            if ( path != null ){
                getIndex().scanPath(path);
            }
            if ( parent != null ){
                parent.fillIndex();
            }
        }
        
        
        
        public final void addImport(String pattern) 
                throws IOException{
            addImportImpl(pattern);
            if ( parent != null ){
                parent.addImport(pattern);
            }
        }
        
        protected void addImportImpl(String pattern) 
                throws IOException{
            if ( path != null ){
                getIndex().scanPath(path, pattern);
            } 
        }
        
        public ClassIndex.Node find(String path){
            ClassIndex.Node out = getIndex().find(path);
            if ( out == null && parent != null ){
                out = parent.find(path);
            }
            return out;
        }
    }
    
}
