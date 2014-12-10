package xyz.cofe.odtexport.odt

//TODO Написание справки
//TODO Создание дистрибутива (zip/exe(+-jre)/deb)

import java.io.File;
import sys.exit;

object Export
{
	/**
	 * Аргументы командной строки
	 */
	private var commandLineArguments : Array[String] = null;

	/**
	 * Аргументы строки преобразованные в ключ значение. <br />
	 * Пример: <code>-key value</code> будет преобразовано в key -&gt; value 
	 */
	lazy val commandLineArgsMap : Map[String,String] = {
		var r = Map[String,String]();
		if( commandLineArguments!=null ){
			for( i <- 0 to commandLineArguments.length-2 ){
				if( commandLineArguments(i).startsWith( "-" ) && commandLineArguments(i).length>1 ){
					r = r + (( commandLineArguments(i).substring(1) , commandLineArguments(i+1) ));
				}
			}
		}
		r
	}

	/**
	 * Аргументы программы состоящие из: аргументов к. строки и переменных окружения
	 */
	lazy val programArguments : Map[String,String] = {
		var r = Map[String,String]();
		val itr = System.getenv.keySet.iterator;
		while( itr.hasNext ){
			val k = itr.next;
			r = r + (( k, System.getenv(k) ));
		}
		r = r ++ commandLineArgsMap
		r
	}

	/**
	 * Возвращает значение аргумента программы или знач. по умолчанию
	 * @param name Имя аргумента
	 * @param defValue Значение по умолчанию
	 * @return Значение аргумента из арг. к. строки, или переменных окр.; либо defValue если неопределенно
	 */
	def argument(name:String,defValue:String):String = {
		if( programArguments.contains(name) )return programArguments(name);
		defValue
	}
	
	/**
	 * Входной ODT файл
	 */
	lazy val inputODTFile : File = {
		val arg = argument("input",null);
		if( arg!=null )	new File(arg); else	null;
	}
	
	/**
	 * HTML Файл с результатом
	 */
	lazy val outputHTMLFile : File = {
		val arg = argument("output",null);
		if( arg!=null )	new File(arg); else	null;
	}

	/**
	 * Версия ПО
	 */
	lazy val version : String = {
//		val packages = java.lang.Package.getPackages;
//		for( pack <- packages )
//			println( "package name: \""+pack.getName+"\" impl. ver: \""+pack.getImplementationVersion+"\"" );
		val defVer = "devBuild";
		val pkg = java.lang.Package.getPackage("xyz.cofe.odtexport");
		val pkgImplVer = if( pkg!=null )pkg.getImplementationVersion() else null;
		if( pkgImplVer!=null ) pkgImplVer else defVer
	}

	/**
	 * Сообщения
	 */
	object message {
		private lazy val messages = new ResourceLocalMessages("/xyz/cofe/odtexport/odt/messages.properties").messages;
		private def template(tpl:String,any:AnyRef*):String = xyz.cofe.text.Text.template(tpl,any:_*);

		object error {
			object args {
				lazy val notSetInput : String = messages("error.args.notSetInput");
				lazy val notSetOutput : String = messages("error.args.notSetOutput");
			}
			object io {
				def fileNotExists(file:File):String = template(messages("error.io.fileNotExists"),file);
				def cantReadFile(file:File):String = template(messages("error.io.cantReadFile"),file);
				def notFile(file:File):String = template(messages("error.io.notFile"),file);
			}
		}
		object export {
			def begin(from:File,to:File):String = template(messages("export.begin"),from,to);
			lazy val end : String = messages("export.end");
			def fileCreated(file:File):String = template(messages("export.fileCreated"),file);
			lazy val currentLang:String = template(messages("export.currentLang"),java.util.Locale.getDefault.getLanguage)
			lazy val help:String = template(messages("export.help"),version)
			lazy val hello:String = template(messages("export.hello"),version)
		}
	}

	/**
	 * Входная точка приложения
	 */
	def main(args:Array[String]):Unit = {
		commandLineArguments = args;
		
		if( inputODTFile==null ){
//			println( "Не указан ODT файл" );
			println( message.error.args.notSetInput );
			help();
			exit(1);
			return;
		}
		
		if( outputHTMLFile==null ){
//			println( "Не указан HTML файл" );
			println( message.error.args.notSetOutput );
			help();
			exit(1);
			return;
		}
		
		if( !inputODTFile.exists ){
//			println( "Нет файла: "+inputODTFile );
			println( message.error.io.fileNotExists(inputODTFile) );
			exit(2);
			return;
		}
		
		if( !inputODTFile.canRead ){
//			println( "Нельзя прочесть файл: "+inputODTFile );
			println( message.error.io.cantReadFile(inputODTFile) );
			exit(2);
			return;
		}
		
		if( !inputODTFile.isFile ){
//			println( "Не является файлом: "+inputODTFile );
			println( message.error.io.notFile(inputODTFile) );
			exit(2);
			return;
		}
		
		export( inputODTFile, outputHTMLFile, verbose );
		exit(0);
	}
	
	/**
	 * Имя программы для справки
	 */
	private lazy val programmeName : String = argument("PROGRAMME_NAME","odtExport");
	
	/**
	 * Выводить сообщения о процессе
	 */
	lazy val verbose : Boolean = {
		val v = argument("verbose","true");
		v.equalsIgnoreCase("true") || v.equalsIgnoreCase("on") || v.equals("1")
	}
	
	/**
	 * Справка по использованию
	 */
	def help():Unit = {
		println( message.export.help );
	}

	/**
	 * Экспорт odt файла в html
	 * @param odt Файл ODT
	 * @param html Файл HTML
	 * @param verbose Выводить на STDOUT сообщения о процессе
	 */
	def export(odt:File,html:File,verbose:Boolean):Unit = {
		import java.util.zip._;
		import java.io._;
		import xyz.cofe.odtexport.ZipFile._;
		import xyz.cofe.odtexport.InputStream._;
		import xyz.cofe.odtexport.File._;

		if( verbose ){
			println( message.export.hello );
			println( message.export.currentLang );
			println( message.export.begin(odt,html) );
		}

		var content : String = null;

		odt.readZipFile( (ze:ZipEntry,fin:InputStream) => {
			if( ze.getName().matches( "(?is)^content.xml$" ) && !ze.isDirectory ){
				content = fin.readText("UTF-8");
			}
			if( ze.getName().matches( "(?is)^.*?\\.(jpe?g|gif|png|bmp)$" ) && !ze.isDirectory ){
				val imageFile = new File( html.getParentFile, ze.getName() );
				val imageFolder = imageFile.getParentFile;
				if( !imageFolder.exists ){
					imageFolder.mkdirs;
				}
				imageFile.write(fin);
				if( verbose )println( message.export.fileCreated(imageFile) );
			}
		});

		if( content==null )return;

		// xml документ в начале не должен содержать символы кроме <, 
		// в том числе и пробельные иначе выскакивает 
		// Java parsing XML document gives “Content not allowed in prolog.” error [duplicate]
		//    http://stackoverflow.com/questions/2599919/
		if( content.indexOf("<")>0 ){
			val cut = content.indexOf("<");
			content = content.substring(cut);
		}
		
		val xmlDoc = xyz.cofe.xml.XMLUtil.parseXML(content);

		html.write( new ODTHtmlBuilder().encode(xmlDoc) ,"UTF-8" );
		
		if( verbose )println( message.export.fileCreated(html) );
		
		val cls = Export.getClass();
		val cssURL = cls.getResource("style.css");
		if( cssURL!=null ){
			import xyz.cofe.odtexport.Common._;
			val targetFile = new File(html.getParentFile(),"style.css");
			using( cssURL.openStream(), new FileOutputStream(targetFile) ){
				case (inStream,outStream) => {
					var stop = false;
					var buff = new Array[Byte](1024);
					while( !stop ){
						val readed = inStream.read(buff);
						if( readed<0 )stop = true;
						if( readed>0 ){
							outStream.write(buff, 0, readed);
						}
					}
				}
			}
			if( verbose )println( message.export.fileCreated(targetFile) );
		}
		if( verbose )println( message.export.end );
	}
}
