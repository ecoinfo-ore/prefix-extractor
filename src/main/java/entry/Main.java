
package entry;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import com.inra.coby.prefixtractor.Writer;
import static java.lang.System.getProperty;
import static com.inra.coby.prefixtractor.Writer.existFile;
import static com.inra.coby.prefixtractor.Writer.checkFile;

 public class Main {

    public static void main(String[] args) throws Exception     {

        if( System.getProperty("H") != null   || System.getProperty("Help") != null )  {
           System.out.println("                                                     ") ;
           System.out.println(" ################################################### ") ;
           System.out.println(" ### Prefix Extractor ############################## ") ;
           System.out.println(" --------------------------------------------------- ") ;
           System.out.println(" Total Arguments :  2                                ") ;
           System.out.println("   -DOntology      : Ontology Location               ") ;
           System.out.println("   -OutPrefixFile  : Out Prefix Locatrion            ") ;
           System.out.println(" --------------------------------------------------- ") ;
           System.out.println(" ################################################### ") ;
           System.out.println("                                                     ") ;
           System.exit(0)    ;
        }
        
        String ontology      = getProperty("Ontology")        ; 
        String outPrefixFile = getProperty("OutPrefixFile")   ; 
        
        if( !existFile(ontology)) {
            System.out.println(" Error : Ontology not found at Path : " + ontology ) ;
            System.exit(0);
        }
        
	System.out.println( " ================================================== ") ;
        System.out.println( " Ontology Path          : "     +      ontology      ) ;
        System.out.println( " Output Prefix Location : "     +      outPrefixFile ) ;
        System.out.println( " ================================================== ") ;
	    
        checkFile(outPrefixFile) ;
        
        List<String> prefixesLines = new ArrayList<>() ;
 
        String xmlns  = null ;
       
        try (Stream<String> stream = Files.lines(Paths.get(ontology))) {

	     xmlns  = stream.map( line -> line.trim())
                            .filter( line -> line.startsWith("<rdf:RDF xmlns=\""))
                            .map(line -> "PREFIX : " + line.split("\"")[1])
                            .findFirst().get() ;
              
	} catch (IOException e) {
	   e.printStackTrace()  ;
	}
       
        try (Stream<String> stream = Files.lines(Paths.get(ontology))) {
 
	      prefixesLines  = stream.map( line -> line.trim())
                                     .filter( line -> line.startsWith("<owl:imports")  && 
                                                      line.contains("rdf:resource=\"") &&
                                                      line.endsWith("/>") )
                                     .map(line -> line.split("rdf:resource=")[1])
                                     .map( line -> line.replace("/>", ""))
                                     .map( line -> buildPrefix( line ) )
                                     .collect(Collectors.toList()) ;

	} catch (IOException e) {
	   e.printStackTrace()  ;
	}
        
        prefixesLines.add(0, "PREFIX xsd: http://www.w3.org/2001/XMLSchema#")           ;
        prefixesLines.add(0, "PREFIX rdfs: http://www.w3.org/2000/01/rdf-schema#")      ;
        prefixesLines.add(0, "PREFIX rdf: http://www.w3.org/1999/02/22-rdf-syntax-ns#") ;
        prefixesLines.add(0, xmlns )                                                    ;
        
        Writer.writeTextFile(prefixesLines, outPrefixFile ) ;
    }    

    private static String buildPrefix( String line )     {
      line           = line.replace("\"", "")  ;
      String[] split = line.split("/")         ;
      String prefix  = split[split.length -1 ] ;
      
      return "PREFIX " + prefix.substring( 0 , prefix.indexOf(".")) +
             ": "       + line  + "#" ;
    }
 }
