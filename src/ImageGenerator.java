/**
 * Created by Mohit on 10/21/2015.
 */
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.OutputStream;
        import net.sourceforge.plantuml.SourceStringReader;

public class ImageGenerator
{
    public  void umlGenerator(String umlString, String outputLine) //(UML grammar, Output Path)
    {
        String add = "@startuml"+ umlString;    //@startuml --grammar-- @enduml as per the format of the PlantUML
        add+="\n"+ "@enduml";
        System.out.println(add);
        OutputStream diagram = null;
        try
        {
            diagram = new FileOutputStream(outputLine);//Output Path specified in console.
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        SourceStringReader reader = new SourceStringReader(add);
        // Write the first image to "diagram"
        try
        {
            reader.generateImage(diagram);//Generate UML Diagram
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        // Return a null string if no generation
    }
}