/*
Copyright (c) 2000 The Regents of the University of California.
All rights reserved.

Permission to use, copy, modify, and distribute this software for any
purpose, without fee, and without written agreement is hereby granted,
provided that the above copyright notice and the following two
paragraphs appear in all copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
*/

// This is a project skeleton file

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Stack;
import java.util.Enumeration;
import java.util.ArrayList;

/** This class is used for representing the inheritance tree during code
    generation. You will need to fill in some of its methods and
    potentially extend it in other useful ways. */
class CgenClassTable extends SymbolTable {

    /** All classes in the program, represented as CgenNode */
    private Vector nds;

    Map tagDict = new HashMap();
    /** This is the stream to which assembly instructions are output */
    private PrintStream str;

    private int stringclasstag;
    private int intclasstag;
    private int boolclasstag;
    
    public HashMap<AbstractSymbol, ArrayList<methodName>> dispTbls;
    public AbstractSymbol currClass;
    // The following methods emit code for constants and global
    // declarations.

    /** Emits code to start the .data segment and to
     * declare the global names.
     * */
    private void codeGlobalData() {
	// The following global names must be defined first.

	str.print("\t.data\n" + CgenSupport.ALIGN);
	str.println(CgenSupport.GLOBAL + CgenSupport.CLASSNAMETAB);
	str.print(CgenSupport.GLOBAL); 
	CgenSupport.emitProtObjRef(TreeConstants.Main, str);
	str.println("");
	str.print(CgenSupport.GLOBAL); 
	CgenSupport.emitProtObjRef(TreeConstants.Int, str);
	str.println("");
	str.print(CgenSupport.GLOBAL); 
	CgenSupport.emitProtObjRef(TreeConstants.Str, str);
	str.println("");
	str.print(CgenSupport.GLOBAL); 
	BoolConst.falsebool.codeRef(str);
	str.println("");
	str.print(CgenSupport.GLOBAL); 
	BoolConst.truebool.codeRef(str);
	str.println("");
	str.println(CgenSupport.GLOBAL + CgenSupport.INTTAG);
	str.println(CgenSupport.GLOBAL + CgenSupport.BOOLTAG);
	str.println(CgenSupport.GLOBAL + CgenSupport.STRINGTAG);
	
	str.println(CgenSupport.GLOBAL + CgenSupport.CLASSOBJTAB);
	str.println(CgenSupport.GLOBAL + CgenSupport.CLASSPARENTTAB);
	str.println(CgenSupport.GLOBAL + CgenSupport.CLASSATTRTABTAB);
	for(Enumeration e = nds.elements(); e.hasMoreElements();) {
		CgenNode nd = (CgenNode)e.nextElement();
		str.println(CgenSupport.GLOBAL + nd.name + CgenSupport.PROTOBJ_SUFFIX);
		str.println(CgenSupport.GLOBAL + nd.name + CgenSupport.CLASSINIT_SUFFIX);
		str.println(CgenSupport.GLOBAL + nd.name + CgenSupport.ATTRTAB_SUFFIX);
	}   	
	
	// We also need to know the tag of the Int, String, and Bool classes
	// during code generation.

	str.println(CgenSupport.INTTAG + CgenSupport.LABEL 
		    + CgenSupport.WORD + intclasstag);
	str.println(CgenSupport.BOOLTAG + CgenSupport.LABEL 
		    + CgenSupport.WORD + boolclasstag);
	str.println(CgenSupport.STRINGTAG + CgenSupport.LABEL 
		    + CgenSupport.WORD + stringclasstag);

    }

    /** Emits code to start the .text segment and to
     * declare the global names.
     * */
    private void codeGlobalText() {
	str.println(CgenSupport.GLOBAL + CgenSupport.HEAP_START);
	str.print(CgenSupport.HEAP_START + CgenSupport.LABEL);
	str.println(CgenSupport.WORD + 0);
	str.println("\t.text");
	str.print(CgenSupport.GLOBAL);
	CgenSupport.emitInitRef(TreeConstants.Main, str);
	str.println("");
	str.print(CgenSupport.GLOBAL);
	CgenSupport.emitInitRef(TreeConstants.Int, str);
	str.println("");
	str.print(CgenSupport.GLOBAL);
	CgenSupport.emitInitRef(TreeConstants.Str, str);
	str.println("");
	str.print(CgenSupport.GLOBAL);
	CgenSupport.emitInitRef(TreeConstants.Bool, str);
	str.println("");
	str.print(CgenSupport.GLOBAL);
	CgenSupport.emitMethodRef(TreeConstants.Main, TreeConstants.main_meth, str);
	str.println("");
    }

    /** Emits code definitions for boolean constants. */
    private void codeBools(int classtag) {
	BoolConst.falsebool.codeDef(classtag, str);
	BoolConst.truebool.codeDef(classtag, str);
    }

    /** Generates GC choice constants (pointers to GC functions) */
    private void codeSelectGc() {
	str.println(CgenSupport.GLOBAL + "_MemMgr_INITIALIZER");
	str.println("_MemMgr_INITIALIZER:");
	str.println(CgenSupport.WORD 
		    + CgenSupport.gcInitNames[Flags.cgen_Memmgr]);

	str.println(CgenSupport.GLOBAL + "_MemMgr_COLLECTOR");
	str.println("_MemMgr_COLLECTOR:");
	str.println(CgenSupport.WORD 
		    + CgenSupport.gcCollectNames[Flags.cgen_Memmgr]);

	str.println(CgenSupport.GLOBAL + "_MemMgr_TEST");
	str.println("_MemMgr_TEST:");
	str.println(CgenSupport.WORD 
		    + ((Flags.cgen_Memmgr_Test == Flags.GC_TEST) ? "1" : "0"));
    }

    /** Emits code to reserve space for and initialize all of the
     * constants.  Class names should have been added to the string
     * table (in the supplied code, is is done during the construction
     * of the inheritance graph), and code for emitting string constants
     * as a side effect adds the string's length to the integer table.
     * The constants are emmitted by running through the stringtable and
     * inttable and producing code for each entry. */
    private void codeConstants() {
	// Add constants that are required by the code generator.
	AbstractTable.stringtable.addString("");
	AbstractTable.inttable.addString("0");

	AbstractTable.stringtable.codeStringTable(stringclasstag, str);
	AbstractTable.inttable.codeStringTable(intclasstag, str);
	codeBools(boolclasstag);
    }


    /** Creates data structures representing basic Cool classes (Object,
     * IO, Int, Bool, String).  Please note: as is this method does not
     * do anything useful; you will need to edit it to make if do what
     * you want.
     * */
    private void installBasicClasses() {
	AbstractSymbol filename 
	    = AbstractTable.stringtable.addString("<basic class>");
	
	// A few special class names are installed in the lookup table
	// but not the class list.  Thus, these classes exist, but are
	// not part of the inheritance hierarchy.  No_class serves as
	// the parent of Object and the other special classes.
	// SELF_TYPE is the self class; it cannot be redefined or
	// inherited.  prim_slot is a class known to the code generator.

	addId(TreeConstants.No_class,
	      new CgenNode(new class_c(0,
				      TreeConstants.No_class,
				      TreeConstants.No_class,
				      new Features(0),
				      filename),
			   CgenNode.Basic, this));

	addId(TreeConstants.SELF_TYPE,
	      new CgenNode(new class_c(0,
				      TreeConstants.SELF_TYPE,
				      TreeConstants.No_class,
				      new Features(0),
				      filename),
			   CgenNode.Basic, this));
	
	addId(TreeConstants.prim_slot,
	      new CgenNode(new class_c(0,
				      TreeConstants.prim_slot,
				      TreeConstants.No_class,
				      new Features(0),
				      filename),
			   CgenNode.Basic, this));

	// The Object class has no parent class. Its methods are
	//        cool_abort() : Object    aborts the program
	//        type_name() : Str        returns a string representation 
	//                                 of class name
	//        copy() : SELF_TYPE       returns a copy of the object

	class_c Object_class = 
	    new class_c(0, 
		       TreeConstants.Object_, 
		       TreeConstants.No_class,
		       new Features(0)
			   .appendElement(new method(0, 
					      TreeConstants.cool_abort, 
					      new Formals(0), 
					      TreeConstants.Object_, 
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.type_name,
					      new Formals(0),
					      TreeConstants.Str,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.copy,
					      new Formals(0),
					      TreeConstants.SELF_TYPE,
					      new no_expr(0))),
		       filename);

	installClass(new CgenNode(Object_class, CgenNode.Basic, this));
	
	// The IO class inherits from Object. Its methods are
	//        out_string(Str) : SELF_TYPE  writes a string to the output
	//        out_int(Int) : SELF_TYPE      "    an int    "  "     "
	//        in_string() : Str            reads a string from the input
	//        in_int() : Int                "   an int     "  "     "

	class_c IO_class = 
	    new class_c(0,
		       TreeConstants.IO,
		       TreeConstants.Object_,
		       new Features(0)
			   .appendElement(new method(0,
					      TreeConstants.out_string,
					      new Formals(0)
						  .appendElement(new formalc(0,
								     TreeConstants.arg,
								     TreeConstants.Str)),
					      TreeConstants.SELF_TYPE,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.out_int,
					      new Formals(0)
						  .appendElement(new formalc(0,
								     TreeConstants.arg,
								     TreeConstants.Int)),
					      TreeConstants.SELF_TYPE,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.in_string,
					      new Formals(0),
					      TreeConstants.Str,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.in_int,
					      new Formals(0),
					      TreeConstants.Int,
					      new no_expr(0))),
		       filename);

	CgenNode IO_node = new CgenNode(IO_class, CgenNode.Basic, this);
	installClass(IO_node);

	// The Int class has no methods and only a single attribute, the
	// "val" for the integer.

	class_c Int_class = 
	    new class_c(0,
		       TreeConstants.Int,
		       TreeConstants.Object_,
		       new Features(0)
			   .appendElement(new attr(0,
					    TreeConstants.val,
					    TreeConstants.prim_slot,
					    new no_expr(0))),
		       filename);

	installClass(new CgenNode(Int_class, CgenNode.Basic, this));

	// Bool also has only the "val" slot.
	class_c Bool_class = 
	    new class_c(0,
		       TreeConstants.Bool,
		       TreeConstants.Object_,
		       new Features(0)
			   .appendElement(new attr(0,
					    TreeConstants.val,
					    TreeConstants.prim_slot,
					    new no_expr(0))),
		       filename);

	installClass(new CgenNode(Bool_class, CgenNode.Basic, this));

	// The class Str has a number of slots and operations:
	//       val                              the length of the string
	//       str_field                        the string itself
	//       length() : Int                   returns length of the string
	//       concat(arg: Str) : Str           performs string concatenation
	//       substr(arg: Int, arg2: Int): Str substring selection

	class_c Str_class =
	    new class_c(0,
		       TreeConstants.Str,
		       TreeConstants.Object_,
		       new Features(0)
			   .appendElement(new attr(0,
					    TreeConstants.val,
					    TreeConstants.Int,
					    new no_expr(0)))
			   .appendElement(new attr(0,
					    TreeConstants.str_field,
					    TreeConstants.prim_slot,
					    new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.length,
					      new Formals(0),
					      TreeConstants.Int,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.concat,
					      new Formals(0)
						  .appendElement(new formalc(0,
								     TreeConstants.arg, 
								     TreeConstants.Str)),
					      TreeConstants.Str,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.substr,
					      new Formals(0)
						  .appendElement(new formalc(0,
								     TreeConstants.arg,
								     TreeConstants.Int))
						  .appendElement(new formalc(0,
								     TreeConstants.arg2,
								     TreeConstants.Int)),
					      TreeConstants.Str,
					      new no_expr(0))),
		       filename);

	installClass(new CgenNode(Str_class, CgenNode.Basic, this));
    }
	
    // The following creates an inheritance graph from
    // a list of classes.  The graph is implemented as
    // a tree of `CgenNode', and class names are placed
    // in the base class symbol table.
    
    private void installClass(CgenNode nd) {
	AbstractSymbol name = nd.getName();
	if (probe(name) != null) return;
	nds.addElement(nd);
	addId(name, nd);
    }

    private void installClasses(Classes cs) {
        for (Enumeration e = cs.getElements(); e.hasMoreElements(); ) {
	    installClass(new CgenNode((Class_)e.nextElement(), 
				       CgenNode.NotBasic, this));
        }
    }
 
    private void buildInheritanceTree() {
	for (Enumeration e = nds.elements(); e.hasMoreElements(); ) {
	    setRelations((CgenNode)e.nextElement());
	}
    }

    private void setRelations(CgenNode nd) {
	CgenNode parent = (CgenNode)probe(nd.getParent());
	nd.setParentNd(parent);
	parent.addChild(nd);
    }

    /** Constructs a new class table and invokes the code generator */
    public CgenClassTable(Classes cls, PrintStream str) {
	nds = new Vector();
	dispTbls = new HashMap<AbstractSymbol, ArrayList<methodName>>();
	this.str = str;

	stringclasstag = 4 /* Change to your String class tag here */;
	intclasstag = 2 /* Change to your Int class tag here */;
	boolclasstag = 3 /* Change to your Bool class tag here */;

	enterScope();
	if (Flags.cgen_debug) System.out.println("Building CgenClassTable");
	
	installBasicClasses();
	installClasses(cls);
	buildInheritanceTree();

	code();

	exitScope();
    }

    /** This method is the meat of the code generator.  It is to be
        filled in programming assignment 5 */
    public void code() {
	if (Flags.cgen_debug) System.out.println("coding global data");
	codeGlobalData();

	if (Flags.cgen_debug) System.out.println("choosing gc");
	codeSelectGc();

	if (Flags.cgen_debug) System.out.println("coding constants");
	codeConstants();

	//                 Add your code to emit
	//                   - prototype objects
	//                   - class_nameTab
	//                   - dispatch tables
	//System.out.println(CgenSupport.WORD + "-1"); //what is this
	emitClassNameTbl();
	emitClassObjTbl();
	tagClass();	
	emitMaxTag();
	emitClassParentTbl();
	emitClassAttrTblTbl();
	emitAttrTbls();
	emitDispTbls();
	emitClassProtObjs();
	if (Flags.cgen_debug) System.out.println("coding global text");

	codeGlobalText();

	//                 Add your code to emit
	//                   - object initializer
	//                   - the class methods
	//                   - etc...
	emitObjInit();
	emitClassMethods();
    }

    private void emitClassMethods() {
	for(Enumeration e = nds.elements(); e.hasMoreElements();) {
		CgenNode nd = (CgenNode) e.nextElement();
		enterScope();
		currClass = nd.name;
		int loc = 0;
		for(Enumeration f = nd.getFeatures().getElements(); f.hasMoreElements();) {
			Feature feat = (Feature) f.nextElement();
			if ((nd.name != (TreeConstants.Object_)) 
			&&   (nd.name != (TreeConstants.Str)) 
			&&   (nd.name != (TreeConstants.Int)) 
			&&   (nd.name != (TreeConstants.Bool))
			&&   (nd.name != (TreeConstants.IO))) {
	    			if (feat instanceof attr) {	
	    			addId(((attr)feat).name, new Entry( ((attr)feat).name, false, loc));
	    			loc += 1;
	    			}		
	    			else if (feat instanceof method) {	
	    				Formals fs = ((method) feat).formals;
	    				for(int i = fs.getLength()-1; i>=0; i--) {
	    					enterScope();
	    					AbstractSymbol n = ((formalc)fs.getNth(i)).name;
	    					addId(n, new Entry(n, true, 0));
	    				}
	    				str.print(nd.name + CgenSupport.METHOD_SEP + ((method)feat).name() + CgenSupport.LABEL);
	    				CgenSupport.emitAddiu(CgenSupport.SP, CgenSupport.SP, -12, str);
	    				CgenSupport.emitStore(CgenSupport.FP, 3, CgenSupport.SP, str);
	    				CgenSupport.emitStore(CgenSupport.SELF, 2, CgenSupport.SP, str);
	    				CgenSupport.emitStore(CgenSupport.RA, 1, CgenSupport.SP, str);
	    				CgenSupport.emitAddiu(CgenSupport.FP, CgenSupport.SP, 16, str);
	    				CgenSupport.emitMove(CgenSupport.SELF, CgenSupport.ACC, str);
	    		
			    		feat.code(this, str);
	
	    				CgenSupport.emitLoad(CgenSupport.FP, 3, CgenSupport.SP, str);
	    				CgenSupport.emitLoad(CgenSupport.SELF, 2, CgenSupport.SP, str);
	    				CgenSupport.emitLoad(CgenSupport.RA, 1, CgenSupport.SP, str);
	    				CgenSupport.emitAddiu(CgenSupport.SP, CgenSupport.SP, 12+((method)feat).formals.getLength()*4, str);
	    				CgenSupport.emitReturn(str);
	    				for(int i = 0; i < fs.getLength(); i++) {
	    					exitScope();
	    				}
	    			}	
			}
		}
		exitScope();
    	}   	
    }

    private void tagClass() {
    	int count = 0;
    	for (Enumeration e = nds.elements(); e.hasMoreElements(); ) {
    		tagDict.put(((CgenNode)e.nextElement()).name,count);
    		count++;
    	}
    	tagDict.put(TreeConstants.prim_slot, -2);
    	tagDict.put(TreeConstants.No_class, -2);
    }
    private void emitClassNameTbl() {
    	str.print(CgenSupport.CLASSNAMETAB+CgenSupport.LABEL);
    	for(Enumeration e = nds.elements(); e.hasMoreElements();) {
    		String className = ((CgenNode)e.nextElement()).name.toString();
    		StringSymbol c = (StringSymbol) AbstractTable.stringtable.lookup(className);
    		str.print(CgenSupport.WORD);
    		c.codeRef(str); str.println();
    	}   	
    }
    private void emitClassProtObjs() {
    int count = 0;
    	for (Enumeration e = nds.elements(); e.hasMoreElements(); ) {
		emitClassProtObj((CgenNode)e.nextElement());
    	}
    }
    private void emitClassProtObj(CgenNode nd) {
		str.println(CgenSupport.WORD + "-1");
	    str.print(nd.name+CgenSupport.PROTOBJ_SUFFIX + CgenSupport.LABEL);
		str.println(CgenSupport.WORD + tagDict.get(nd.name));
		int size = 0;
		for(Enumeration e = nd.getFeatures().getElements(); e.hasMoreElements();) {
		    	Feature feat = (Feature) e.nextElement();
			if (feat instanceof attr) {	
			    size++;
			}
		}
		size = size + 3;
		str.println(CgenSupport.WORD + size);
		str.println(CgenSupport.WORD + nd.name + CgenSupport.DISPTAB_SUFFIX);
		for(Enumeration e = nd.getFeatures().getElements(); e.hasMoreElements();) {
	    	Feature feat = (Feature) e.nextElement();
	    	if (feat instanceof attr) {	
	    		attr a = (attr) feat;
	    		if ((a.type_decl == TreeConstants.Int)) {
	    			str.println(CgenSupport.WORD + CgenSupport.INTCONST_PREFIX + (AbstractTable.inttable.lookup("0")).index);
	    		} else if (a.type_decl == TreeConstants.Str) {
	    			str.println(CgenSupport.WORD + CgenSupport.STRCONST_PREFIX + (AbstractTable.stringtable.lookup("")).index);
	    		} else if (a.type_decl == TreeConstants.Bool) {
	    			str.println(CgenSupport.WORD + "bool_const0");
	    		} else {
	    			str.println(CgenSupport.WORD + "0");
	    		}
	    	}
    	}
    }

    private void emitClassObjTbl() {
    	str.print(CgenSupport.CLASSOBJTAB+CgenSupport.LABEL);
    	for(Enumeration e = nds.elements(); e.hasMoreElements();) {
    		CgenNode nd = (CgenNode)e.nextElement();
    		str.println(CgenSupport.WORD + nd.name + CgenSupport.PROTOBJ_SUFFIX);
    		str.println(CgenSupport.WORD + nd.name + CgenSupport.CLASSINIT_SUFFIX);
    	}   	
    }
    private void emitMaxTag() {
    	int numClasses = nds.size()-1; //all classes minus object
    	str.print("_max_tag"+CgenSupport.LABEL);
    	str.println(CgenSupport.WORD+numClasses);
    }
    private void emitClassParentTbl() {
    	//TODO
    	str.print(CgenSupport.CLASSPARENTTAB+CgenSupport.LABEL);
    	for(Enumeration e = nds.elements(); e.hasMoreElements();) {
    		CgenNode nd = (CgenNode)e.nextElement();
    		str.println(CgenSupport.WORD + tagDict.get(nd.getParentNd().name));
    	}
    }
    private void emitClassAttrTblTbl() {
    	str.print(CgenSupport.CLASSATTRTABTAB+CgenSupport.LABEL);
    	for(Enumeration e = nds.elements(); e.hasMoreElements();) {
    		CgenNode nd = (CgenNode)e.nextElement();
    		str.println(CgenSupport.WORD + nd.name + CgenSupport.ATTRTAB_SUFFIX);
    	}
    }
    private void emitAttrTbls() {
    	for (Enumeration e = nds.elements(); e.hasMoreElements(); ) {
    	    CgenNode nd = (CgenNode)e.nextElement();
    	    Stack<CgenNode> parents = parents(nd);
    	    str.print(nd.name+CgenSupport.ATTRTAB_SUFFIX+CgenSupport.LABEL);
    	    while(!parents.empty()) {
    	    	CgenNode parent = parents.pop();
    	    	emitAttr(parent);
    	    }
    	    emitAttr(nd);	    
    	}
    }
    private void emitAttr(CgenNode nd) {
    	//TODO
    	for(Enumeration e = nd.getFeatures().getElements(); e.hasMoreElements();) {
	    	Feature feat = (Feature) e.nextElement();
	    	if (feat instanceof attr) {	
	    		str.println(CgenSupport.WORD + tagDict.get(((attr) feat).type_decl));
	    	}
    	}
    }
    private void emitDispTbls() {
    	for (Enumeration e = nds.elements(); e.hasMoreElements(); ) {
    		String methodName;
    	    CgenNode nd = (CgenNode)e.nextElement();
    	    dispTbls.put(nd.name, new ArrayList<methodName>());
    	    Stack<CgenNode> parents = parents(nd);
    	    str.print(nd.name+CgenSupport.DISPTAB_SUFFIX+CgenSupport.LABEL);
    	    /*for(Enumeration ee = nd.getFeatures().getElements(); ee.hasMoreElements();) { //add  nd methods to table
    	    	Feature feat = (Feature) ee.nextElement();
    	    	if (feat instanceof method) {	
    	    		methodName = nd.name + CgenSupport.METHOD_SEP + ((method) feat).name;
    	    		dispTbls.get(nd.name).add(new methodName(nd.name,((method) feat).name));
    	    	}
    	    }*/
    	    while(!parents.empty()) {
    	    	CgenNode parent = parents.pop();
    	    	emitMethods(nd, parent);
    	    }
    	    emitMethods(nd, nd);
        	ArrayList<methodName> dispTbl = dispTbls.get(nd.name);
        	System.out.println(nd.name);
        	/*for(int i = 0; i < dispTbl.size(); i++) {
        		System.out.println(dispTbl.get(i).methName);
        	}*/
    	}
    }
    private void emitMethods(CgenNode currClass, CgenNode nd) {
    	String methodName;
    	boolean lowestChild = currClass.equals(nd);
    	for(Enumeration e = nd.getFeatures().getElements(); e.hasMoreElements();) {
	    	Feature feat = (Feature) e.nextElement();
	    	if (feat instanceof method) {	
    			if(containsMethod(currClass, ((method) feat).name) && !lowestChild) { //override
    				str.print(CgenSupport.WORD);
    				methodName = currClass.name + CgenSupport.METHOD_SEP + ((method) feat).name;
		    		str.println(methodName);
		    		dispTbls.get(currClass.name).add(new methodName(currClass.name,((method) feat).name));
    			}
    			else if(!containsMethod(nd.getParentNd(), ((method) feat).name) && lowestChild) { //new method
    				str.print(CgenSupport.WORD);
    				methodName = nd.name + CgenSupport.METHOD_SEP + ((method) feat).name;
		    		str.println(methodName);
		    		dispTbls.get(currClass.name).add(new methodName(nd.name,((method) feat).name));
    			}
    			else if(!containsMethod(currClass, ((method) feat).name)) { //inherited method
    				str.print(CgenSupport.WORD);
    				methodName = nd.name + CgenSupport.METHOD_SEP + ((method) feat).name;
		    		str.println(methodName);
		    		dispTbls.get(currClass.name).add(new methodName(nd.name,((method) feat).name));
    			}
		    }
    	}
    }
    private boolean containsMethod(CgenNode nd, AbstractSymbol methodName) {
    	for(Enumeration e = nd.getFeatures().getElements(); e.hasMoreElements();) {
    		Feature feat = (Feature) e.nextElement();
    		if (feat instanceof method) {
    			method m = (method) feat;
    			if(m.name == methodName) {
    				return true;
    			}
    		}
    	}
    	return false;
    }

    private Stack<CgenNode> parents(CgenNode nd) {
    	Stack<CgenNode> s = new Stack<CgenNode>();
    	CgenNode ndp = nd.getParentNd();
    	while(!ndp.name.equals(TreeConstants.No_class)) {
    		s.push(ndp);
    		ndp = ndp.getParentNd();
    	}
    	return s;
    }
    private void emitObjInit() {
    	for (Enumeration e = nds.elements(); e.hasMoreElements(); ) {
    		enterScope();
    		CgenNode nd = (CgenNode)e.nextElement();
    		currClass = nd.name;
    		str.print(nd.name + CgenSupport.CLASSINIT_SUFFIX + CgenSupport.LABEL);
    		CgenSupport.emitAddiu(CgenSupport.SP, CgenSupport.SP, -12, str);
    		CgenSupport.emitStore(CgenSupport.FP, 3, CgenSupport.SP, str);
    		CgenSupport.emitStore(CgenSupport.SELF, 2, CgenSupport.SP, str);
    		CgenSupport.emitStore(CgenSupport.RA, 1, CgenSupport.SP, str);
    		CgenSupport.emitAddiu(CgenSupport.FP, CgenSupport.SP, 16, str);
    		CgenSupport.emitMove(CgenSupport.SELF, CgenSupport.ACC, str);
    		
		if(!nd.name.toString().equals("Object")) {
    			CgenSupport.emitJal(nd.getParentNd().name+CgenSupport.CLASSINIT_SUFFIX, str);
    		}
		int loc = 0;
		for(Enumeration f = nd.getFeatures().getElements(); f.hasMoreElements();) {
	    	Feature feat = (Feature) f.nextElement();
	    		if (feat instanceof attr) {
				addId(((attr)feat).name, new Entry( ((attr)feat).name, false, loc));
	    			loc += 1;
				feat.code(this, str);
	    		}
		}
    		CgenSupport.emitMove(CgenSupport.ACC, CgenSupport.SELF, str);
    		CgenSupport.emitLoad(CgenSupport.FP, 3, CgenSupport.SP, str);
    		CgenSupport.emitLoad(CgenSupport.SELF, 2, CgenSupport.SP, str);
    		CgenSupport.emitLoad(CgenSupport.RA, 1, CgenSupport.SP, str);
    		CgenSupport.emitAddiu(CgenSupport.SP, CgenSupport.SP, 12, str);
    		CgenSupport.emitReturn(str);
		exitScope();
    	}
    }
   
    /** Gets the root of the inheritance tree */
    public CgenNode root() {
	return (CgenNode)probe(TreeConstants.Object_);
    }
    
    public int methodIndex(AbstractSymbol clsName, AbstractSymbol methName) {
    	ArrayList<methodName> dispTbl = dispTbls.get(clsName);
    	for(int i = 0; i < dispTbl.size(); i++) {
    		//sSystem.out.println(dispTbl.get(i).methName+" ");
    		if(methName.equals(dispTbl.get(i).methName)) {
    			return i;
    		}
    	}
    	return -1;
    }
    private class methodName {
    	public AbstractSymbol clsName;
    	public AbstractSymbol methName;
    	public methodName(AbstractSymbol clsName, AbstractSymbol methName) {
    		this.clsName = clsName;
    		this.methName = methName;
    	}
    	public String toString() {    		
    		return clsName.getString()+"."+methName.getString();
    	}
    }
}		
    
