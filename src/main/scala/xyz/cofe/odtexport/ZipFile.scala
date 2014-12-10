package xyz.cofe.odtexport

object ZipFile
{
	import java.io.File;
	import xyz.cofe.odtexport.Common._;
	
	implicit def extendFile2ZipExtension( f:File ):ZipFileExtension = new ZipFileExtension( f );

	class ZipFileExtension( val file : File ){
		def readZipFile( f: (java.util.zip.ZipEntry,java.io.InputStream)=>Unit ):Unit = {
			import java.util.zip._;
			import java.io._;

			using( new FileInputStream(file) ){ fin =>
				val zin = new ZipInputStream(fin);
				var ze : ZipEntry = null;
				do{
					ze = zin.getNextEntry();
					if( ze!=null ){
						f(ze,zin);
						zin.closeEntry;
					}
				}while(ze!=null)
			}
		}
	}
}