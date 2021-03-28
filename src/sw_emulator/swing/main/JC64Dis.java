/**
 * @(#)JC64Dis.java 2019/12/01
 *
 * ICE Team free software group
 *
 * This file is part of C64 Java Software Emulator.
 * See README for copyright notice.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307  USA.
 */
package sw_emulator.swing.main;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import sw_emulator.software.Disassembly;
import sw_emulator.swing.JDisassemblerFrame;

class CmdOption {
     String flag, opt;
     public CmdOption(String flag, String opt) { this.flag = flag; this.opt = opt; }
}


/**
 * Java C64 disassembler with graphics
 * 
 * @author ice
 */
public class JC64Dis {
 
 	public static void generate_dis (
            String _asmprg, 
            String _mmsave, 
            String _disfile, 
            String _asmin,
            String _prgout)
    {
        // Option to use 
        Option option=new Option();
        FileManager.instance.readOptionFile(FileManager.OPTION_FILE, option);
        // Data table for memory 
       // DataTableModelMemory dataTableModelMemory=new DataTableModelMemory(option);
        // create a new project
        Project project = new Project();
        project.file =  _asmprg;
        project.fileType = FileType.PRG;
        project.targetType=TargetType.VIC20;
        project.name = ""; // project name 
        // go to read the file
        try {
          project.setData(FileManager.instance.readFile(project.file));
          project.fileType=project.fileType.getFileType(project.inB);
        }catch (FileNotFoundException e) {
            throw new IllegalArgumentException( "ROM file not found"); 
        } catch(IOException e) {
            throw new IllegalArgumentException( "Error reading the ROM file"); 
        }
      
        // now read MMSAVE file
        File mmsaveFile = new File (_mmsave); 
        if (!FileManager.instance.readMMSaveFile(mmsaveFile, project)) 
        { 
            System.out.println ("Error reading MMSAVE file"); 
        }
          // Disassembly engine 
        Disassembly disassembly = new Disassembly();
        disassembly.dissassembly(project.fileType, 
                project.inB, 
                option, 
                project.memory, 
                project.mpr, 
                project.chip, 
                project.targetType, 
                false);
        disassembly.dissassembly(project.fileType, 
                project.inB, 
                option, 
                project.memory, 
                project.mpr, 
                project.chip, 
                project.targetType, 
                true);
        // export .dis file 
        File projectFile = new File (_disfile);
        FileManager.instance.writeProjectFile(projectFile, project);
        if (disassembly.source==null) {
            throw new IllegalArgumentException( "There is no source to assemble" ); 
        }
        // Compiler 
        File inputFile = new File( _asmin );
        File outputFile = new File( _prgout );
        try {
            PrintWriter out=new PrintWriter(inputFile);
            out.write(disassembly.source);
            out.flush();
            out.close();
        } catch (Exception e) {
            System.err.println(e);
        }     
        System.out.println("---compiling---");
        sw_emulator.software.asm.Compiler compiler; 
        compiler = new sw_emulator.software.asm.Compiler();
        compiler.setOption(option);   
        String res=compiler.compile(inputFile, outputFile);
        System.out.println(res);
  
    }    
    
  private JDisassemblerFrame jMainFrame;
    
    public JC64Dis() {
      jMainFrame=new JDisassemblerFrame();
      jMainFrame.setVisible(true);
    }
       
 /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) 
    {     
       List<String> argsList = new ArrayList<String>();
       List<CmdOption> optsList = new ArrayList<CmdOption>();
       List<String> doubleOptsList = new ArrayList<String>();
       for (int i = 0; i < args.length; i++) {
        switch (args[i].charAt(0)) 
        {
          case '-':
            if (args[i].length() < 2)
                throw new IllegalArgumentException("Not a valid argument: "+args[i]);
            if (args[i].charAt(1) == '-') {
                if (args[i].length() < 3)
                    throw new IllegalArgumentException("Not a valid argument: "+args[i]);
                // --opt
                doubleOptsList.add(args[i].substring(2, args[i].length() ));
            } 
            else 
            {
               if (args.length-1 == i)
                   throw new IllegalArgumentException("Expected arg after: "+args[i]);
               // -opt
               optsList.add(new CmdOption(args[i], args[i+1]));
               i++;
            }
            break;
        default:
            // arg
            argsList.add(args[i]);
            break;
        }
       }
       Iterator<String> iter;
       boolean enable_gui = true; 
       iter = doubleOptsList.iterator();
       while (iter.hasNext()) {
          if ("cmd".equals(iter.next())) {
               enable_gui = false; 
          }
       }
       String asmprg = "";
       String mmsave = ""; 
       String disfile = "";  
       String asmin = "";
       String prgout = "";
        
       Iterator<CmdOption> iter2;
       iter2 = optsList.iterator(); 
       while (iter2.hasNext()) {
          CmdOption opt = iter2.next(); 
         // System.out.println (String.format( "opt =%s, flag=%s", opt.opt, opt.flag)); 
          if (opt.flag.equals("-prg")) {
              asmprg = opt.opt;
          }
          if (opt.flag.equals("-csv")) {
              mmsave = opt.opt; 
          }
          if (opt.flag.equals("-dis")) {
              disfile = opt.opt;
          }
          if (opt.flag.equals("-asm")) {
              asmin = opt.opt;
          }
           if (opt.flag.equals("-out")) {
              prgout = opt.opt;
          } 
       }
       System.out.println ("-prg "+asmprg); 
       System.out.println ("-csv "+mmsave); 
       System.out.println ("-dis "+disfile);
       System.out.println ("-asm "+asmin); 
       System.out.println ("-out "+prgout);
       if (enable_gui) {
           new JC64Dis();
       }
       else {
            generate_dis (asmprg, mmsave, disfile, asmin, prgout );
       }
    }  
}
