import java.io.*;
import java.util.Vector;

/** Class for evaluating an N-dimensional quadratic function.
 * @author <A HREF="mailto:MWetter@lbl.gov">Michael Wetter</A>
 */

public class Quad
{
    /** System dependent line separator */
    private final static String LS = System.getProperty("line.separator");
	
    /** Initializes the x vector from a input file
     *@param path of file (or null pointer if file is in current directory 
     *@param name of file
     *@return a vector with the x values
     */
    public static double[] getX(String path, String name)
	throws IOException{
	String s = new String();
	
	File TemFil = new File(path, name);
	String FilNam = TemFil.getAbsolutePath();
	
	RandomAccessFile raf = null;
	try{
	    raf = new RandomAccessFile(FilNam, "r"); 
	}
	catch(IOException e){
	    String ErrMes=LS + "Error: " + e.getClass().getName() +
		"; Message: Cannot open file" + LS
		+ "   " + FilNam;	
	    throw new IOException(ErrMes);
	}
	
	try{
	    do{
		s = new String(new String(s) + new String(raf.readLine()));
	    }while(true);
	    
	}
	catch(Exception e)  { }
	
	Vector vec = new Vector(0);
	int end;
	do{
	    s = new String(s.trim());
	    end = s.indexOf(" ", 0);
	    String su = (end == -1) ? new String(s) : new String(s.substring(0, end));
	    su.trim();
	    vec.add(new Double(su));
	    if (end != -1)
		s = new String(s.substring(end));
	}while( end != -1 );
	
	double[] x = new double[ vec.size() ];
	for(int i = 0; i < x.length; i++)
	    x[i] = ((Double)(vec.elementAt(i))).doubleValue();
	return x;
    }
    
    public static void writeFile(String s, String name) throws IOException
    {
	FileWriter FilWri = new FileWriter(name);
	FilWri.write(s);
	FilWri.close();
    }
    
    /** Main routine
     */
    public static void main(String[] args) throws IOException{
	final String fnLog = "sim.log";
	final String fnIn  = "x.txt";
	final String fnOut = "f.txt";

	try{	
	    double[] x = getX(null, fnIn);
		
	    double f = 0;
	    for(int i = 0; i < x.length; i++)
		f += x[i] * x[i];
	    f = StrictMath.sqrt( f );
	    String s = "f(x) = \t " + String.valueOf(f);
	    writeFile(s, fnOut);
	    writeFile("Simulation completed successfully." + LS, fnLog);
	    System.exit(0);
	}
	    
	catch(Exception e){
	    e.printStackTrace();
	    String s = e.getMessage();
	    writeFile(s, fnLog);
	    System.exit(1);
	}
    }
}
