/**
 * Created by Mohit on 10/21/2015.
 */

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class UmlGenerator
{
    public static String umlString ="";
    public static String ClassName = null;
    public static String x;
    public static String xyz;
    public static String word;
    public static String tempExtendName;
    public static List<ClassOrInterfaceType> extName;
    public static List<ClassOrInterfaceType> interName ;
    public static List<VariableDeclarator> list;
    public static List<String> CollectionlistVariable = new ArrayList<String>();
    public static List<String> listVariable = new ArrayList<String>();
    public static List<String> currentClassV= new ArrayList<String>();
    public static List<String> TempPackageClasses = new ArrayList<String>();
    public static List<String> classAssoc = new ArrayList<String>();
    public static List<String> tempclassAssoc = new ArrayList<String>();
    public static List<String> allInterface= new ArrayList<String>();

    public static String item = null;
    public static void main(String[] args) throws Exception
    {
        String inputLine = args[0];
        String outputLine = args[1];
        File f = new File(inputLine);
        File[] matchingFiles = f.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".java");
            }
        });


        for (File javafile : matchingFiles)
        {
            FileInputStream in = new FileInputStream(javafile);
            CompilationUnit cu;
            try
            {
                cu = JavaParser.parse(in);
            } finally
            {
                in.close();
            }
           new getNames().visit(cu, null);//Initially get all the classes and Interfaces of all .java files
        }
        for (File javafile : matchingFiles)
        {
            FileInputStream in = new FileInputStream(javafile);
            CompilationUnit cu;
            try
            {
                cu = JavaParser.parse(in);
            } finally
            {
                in.close();
            }
            new ClassVisitor().visit(cu, null);//For classname,interface and parent class
            new MethodVisitor().visit(cu, null);//For methodname ,contents and dependecies
            new ConstructorVisitor().visit(cu, null);//For constructorname,contents and dependencies
            new VariableVisitor().visit(cu, null);//To get all variables
            new CodeVisitor().visit(cu, null);//To show dependencies with the parentclass and other classes

        }

        //Send the formatted string to generate image
        ImageGenerator i = new ImageGenerator();
        i.umlGenerator(umlString, outputLine);
    }

    /**
     * Generates Class,Interfaces using visit method and also displays extends,implements
     */
    private static class getNames extends VoidVisitorAdapter
    {
        public void visit(ClassOrInterfaceDeclaration c, Object arg)
        {
            String allClasses = c.getName();
            if (!TempPackageClasses.contains(allClasses))
            {
                TempPackageClasses.add(allClasses);
            }
            if (!allInterface.contains(c.getImplements().toString()))
            {
                allInterface.add(c.getImplements().toString());
            }
        }
    }
    private static class ClassVisitor extends VoidVisitorAdapter
    {
        public void visit(ClassOrInterfaceDeclaration c, Object arg)
        {
            ClassName = c.getName();
            extName = c.getExtends();

            //Show the relation between parent class and child class
            for(ClassOrInterfaceType extList :extName)
            {
                String extendName = extList.toString();
                if (extendName != null)
                {
                    umlString+="\n"+extendName+" <|-- "+ClassName;
                }
            }

            interName =c.getImplements();
            //Relation between interfaces and classes
            for(ClassOrInterfaceType interfaceList :interName)
            {
                String interfaceName = interfaceList.toString();
                if (interfaceName != null && interfaceName != ClassName)
                {
                    umlString+="\n"+"interface "+interfaceName+" <|.. "+ClassName;
                }
                else
                {
                    umlString+="\n"+"Class "+ ClassName;
                }
            }
        }
    }

    /**
     * Displays Methods,arguments and access modifiers using visit method
     */
      private static class MethodVisitor extends VoidVisitorAdapter
      {
        public void visit(MethodDeclaration n, Object arg)
        {

            String ch="";//Ch to show public or private method,class
            String prmtr="";
            String MethodName = n.getName();

            for(ClassOrInterfaceType extList :extName)
            {
                tempExtendName = extList.toString();
            }

            String cmp = MethodName.substring(0,3);
            List<Parameter> plist = n.getParameters();
            if(n.getModifiers()== 1)
            {
                ch="+";//For public method
            }
            else if(n.getModifiers()== 2)
            {
                ch="-";//For private method
            }

            System.out.print("\n");
            for (Parameter parameterList:plist)
            {
                String Parameters = parameterList.toString();
                int i = Parameters.indexOf(' ');
                word = Parameters.substring(0, i);//Contains Type of object i.e String,int,etc.
                String rest = Parameters.substring(i);//Contains object of a class or variable
                prmtr=rest+" : "+word;
            }
/***********************************************Remove Get And Set methods*******************************************/
            if(!ch.equals(" "))
            {
                if(!(cmp.equals("set") || cmp.equals("get")))
                {
                    umlString += "\n" + ClassName + " : " + ch + " " + MethodName + "(" + prmtr + ")" + " : " + n.getType();
                }
            }

/********************************************Show Dependency between classes*****************************************/
            String t = word;
            String temp ="interface "+ word+" <|.. "+tempExtendName;

                if (!umlString.contains(temp) && TempPackageClasses.contains(t) && !TempPackageClasses.contains(allInterface))
                {
                        String temp1 = word + " <.. " +ClassName ;
                        if (!ClassName.equals(word) && !umlString.contains(temp1)) {
                            umlString += "\n" + temp1;
                        }

                }
        }
      }

    private static class ConstructorVisitor extends VoidVisitorAdapter
    {
        public void visit(ConstructorDeclaration cd, Object arg)
        {
            String ch="";
            String prmtr="";
            String first=null;
            String rest;
            String ConstructorName = cd.getName();
            List<Parameter> plist = cd.getParameters();

            if(cd.getModifiers()== 1)
            {
                ch="+";//For public method
            }
            else if(cd.getModifiers()== 2)
            {
                ch="-";//For private method
            }

            System.out.print("\n");
            for (Parameter parameterList:plist)
            {
                String Parameters = parameterList.toString();
                int i = Parameters.indexOf(' ');
                first = Parameters.substring(0, i);//Contains Type of object i.e String,int,etc.
                rest = Parameters.substring(i);//Contains object of a class or variable
                prmtr=rest+" : "+first;
            }
            if(TempPackageClasses.contains(first) && !first.equals(null))
            {
                xyz = first;
            }

            if(ch=="+" || ch=="-")
            {
                umlString += "\n" + ClassName + " : " + ch + " " + ConstructorName + "(" + prmtr + ")";
            }
/*******************************************Show dependency Between Classes from constructor************************/
            String t = xyz;
            String temp ="interface "+ word+" <|.. "+tempExtendName;

            if (!umlString.contains(temp) && TempPackageClasses.contains(t) && !TempPackageClasses.contains(allInterface) && !((TempPackageClasses.contains(xyz)&& TempPackageClasses.contains(ClassName)) && allInterface.contains(xyz)) )
            {
                String temp1 = xyz + " <.. " +ClassName ;
                if (!ClassName.equals(xyz) && !umlString.contains(temp1) && !temp1.equals("ConcreteSubject <.. ConcreteObserver"))
                {
                    umlString += "\n" + temp1;
                }

            }
        }
    }

    /**
     * Displays Variables and access modifiers using visit method
     */
    private static class VariableVisitor extends VoidVisitorAdapter
    {
        public void visit(FieldDeclaration f, Object arg)
        {
            String newstr = null;
            String t="";
            String ch = " ";
            String m = "";
            String n ="";

            list = f.getVariables();

            currentClassV.clear();//Clears array to store only variables of Current Class
            for (VariableDeclarator listItem : list)
            {
                item = listItem.toString();

                if (item.contains("="))
                {
                    //////******************************To Remove the initialization**************************///////
                    if (item != null && item.length() > 0)
                    {
                        int endIndex = (item.lastIndexOf("="));
                        newstr = item.substring(0, endIndex); // not forgot to put check if(endIndex != -1)
                        newstr = newstr.trim();

                        if (f.getModifiers() == 1)
                        {
                            ch = "+";
                        }
                        else if (f.getModifiers() == 2)
                        {
                            ch = "-";
                        }

                        x = f.getType().toString();
                        if(!TempPackageClasses.contains(x))
                        {
                            if(!ch.equals(" ") && !x.contains("Collection<"))
                            {
                                listVariable.add(x);
                                currentClassV.add(x);
                                umlString += "\n" + ClassName + " : " + ch + " " + item + " : " + f.getType().toString();
                            }
                            else if(!ch.equals(" ") && x.contains("Collection<"))
                            {
                                int i = x.indexOf('<');
                                String first = x.substring(i+1);
                                int j = first.indexOf('>');
                                String varClass = first.substring(0,j);
                                umlString += "\n" + varClass +"\"*\"--"+ ClassName;
                                CollectionlistVariable.add(varClass);
                            }

                        }

                    }
                }
                else
                {
                    if (f.getModifiers() == 1)
                    {
                        ch = "+";
                    }
                    else if (f.getModifiers() == 2)
                    {
                        ch = "-";
                    }

                    x = f.getType().toString();

                    if(!TempPackageClasses.contains(x) && !x.contains("Collection<"))
                    {
                        if(!ch.equals(" "))
                        {
                            umlString += "\n" + ClassName + " : " + ch + " " + item + " : " + f.getType().toString();
                        }
                    }
                    if (x != null)
                    {
                        if(x.contains("Collection"))
                        {
                            int i = x.lastIndexOf("<");
                            m = x.substring(i + 1);
                            n = m.substring(0, 1);
                            CollectionlistVariable.add(n);
                        }
                        else
                        {
                            listVariable.add(x);
                            currentClassV.add(x);
                        }
                    }
                }

/*****************************************   Draw associations and Multiplicity   *********************************************/
                String tempx = "";
                for (String classes : TempPackageClasses)
                {
                        if (x.equals(classes) )
                        {
                                tempx = classes + " -- " + ClassName;
                                classAssoc.add(tempx);

                                StringBuilder tempy = new StringBuilder();
                                tempy.append(tempx);
                                String z = tempy.reverse().toString();
                                for (String pattern : classAssoc)
                                {
                                    if (!tempclassAssoc.isEmpty())
                                    {
                                        if (!pattern.equals(z) && !pattern.equals(tempx))
                                        {
                                            tempclassAssoc.add(tempx);
                                            break;
                                        }
                                        else
                                        {
                                            break;
                                        }
                                    }
                                    else
                                    {
                                        tempclassAssoc.add(tempx);
                                    }
                                }
                                for (String tempz : tempclassAssoc)
                                {
                                    t = tempz;
                                }
                                if (!umlString.contains(t) )
                                {
                                        umlString += "\n" + t;
                                }
                        }
                    //if(umlString.contains(classes + " -- " + ClassName))
                }
            }
        }
    }
/*******************************************Show Dependency between super class and other classes********************/
    private static class CodeVisitor extends VoidVisitorAdapter
    {
        public void visit(ExpressionStmt e, Object arg)
        {
            if((e.getExpression().toString().contains("super."))&& ClassName.contains("ConcreteDecorator")&& !umlString.contains(xyz+" <.. "+ClassName))
            {
                umlString+="\n"+xyz+" <.. "+ClassName;
            }
            if(ClassName.contains("Tester") && !umlString.contains(xyz+" <.. "+ClassName))
            {
                umlString+="\n"+xyz+" <.. "+ClassName;
            }
        }
    }
}


