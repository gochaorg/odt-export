package odtExport;

import static org.junit.Assert.*;

import org.junit.Test;
import java.io.File;

public class runSample01 {

	@Test
	public void test() {
//		fail("Not yet implemented");
		//xyz.cofe.odtexport.odt.Export.main(new String[]{
		//});
		
		File srcOdtFile = new File( "/home/user/downloads/odt/sample/sample.odt" );
		if( !srcOdtFile.exists() ){
			System.out.println("file not found, end test");
			System.out.println(srcOdtFile.toString());
			return;
		}
		
		File targetHtmlFile = new File( "target/test-sample.html" );
		File targetDir = targetHtmlFile.getParentFile();
		if( !targetDir.exists() ){
			System.out.println("directory not exists, end test");
			return;
		}

		xyz.cofe.odtexport.odt.Export.main(new String[]{
			"-input", srcOdtFile.toString(),
			"-output", targetHtmlFile.toString()
		});
	}
}
