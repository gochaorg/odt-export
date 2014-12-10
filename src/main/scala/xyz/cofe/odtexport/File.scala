package xyz.cofe.odtexport

object File {
	import java.io.File;
	import java.util.Arrays;

	implicit def extendFile( f:File ):FileExtension = new FileExtension( f );

	class FileExtension( val file:File )
	{
		import java.nio.charset.Charset;
		import java.io._;
		import xyz.cofe.odtexport.Common.using;

		/**
		 * Дописывает текст в конец файла используя кодировку по умолчанию
		 * @param text Текст который необходимо дописать
		 */
		def append( text: String ):Unit = write( text,Charset.defaultCharset,true );
		
		/**
		 * Дописывает текст в конец файла
		 * @param text Текст который необходимо дописать
		 * @param charSet Кодировка
		 */
		def append( text: String, charSet:String ):Unit = write( text,Charset.forName(charSet),true );
		
		/**
		 * Дописывает текст в конец файла
		 * @param text Текст который необходимо дописать
		 * @param charSet Кодировка
		 */
		def append( text: String, charSet:Charset ):Unit = write( text,charSet,true );

		/**
		 * Пишет текст в файл используя кодировку по умолчанию
		 * @param text Текст
		 */
		def write( text: String ):Unit = write( text, Charset.defaultCharset,false );

		/**
		 * Пишет текст в файл
		 * @param text Текст
		 * @param charSet Кодировка
		 */
		def write( text: String, charSet:String ):Unit = write( text, Charset.forName(charSet),false );

		/**
		 * Пишет текст в файл
		 * @param text Текст
		 * @param charSet Кодировка
		 */
		def write( text: String, charSet:Charset ):Unit = write( text, charSet,false );

		/**
		 * Пишет текст в файл
		 * @param text Текст
		 * @param charSet Кодировка
		 * @param append true - пишет в конец файла, false - пишет в начало
		 */
		def write( text: String, charSet:Charset, append:Boolean):Unit = {
			using( new FileOutputStream(file,append) ) { fout =>
				val sout = new OutputStreamWriter(fout, charSet);
				sout.write(text);
				sout flush;
			}
		}

		/**
		 * Открывает поток для записи символов
		 * @param charSet Кодировка
		 * @param append  true - пишет в конец файла, false - пишет в начало
		 * @return Поток для записи
		 */
		def openWriter( charSet:String=Charset.defaultCharset.name, append:Boolean=false ) : java.io.Writer = {
			val fileStream = new FileOutputStream(file,append);
			val writer = new java.io.OutputStreamWriter(fileStream,charSet);
			writer
		}

		/**
		 * Открывает поток для записи символов
		 * @param charSet Кодировка
		 * @param append  true - пишет в конец файла, false - пишет в начало
		 * @return Поток для записи
		 */
		def openWriter( charSet : Charset, append:Boolean ) : java.io.Writer = {
			val fileStream = new FileOutputStream(file,append);
			val writer = new java.io.OutputStreamWriter(fileStream,charSet);
			writer
		}

		/**
		 * Читает текст из файла используя кодировку по умолчанию
		 */
		def readText():String = readText( Charset.defaultCharset() );
		
		/**
		 * Читает текст из файла
		 * @param cs Кодировка символов
		 */
		def readText( cs: String ):String = readText( Charset.forName(cs) );
		
		/**
		 * Читает текст из файла
		 * @param cs Кодировка символов
		 */
		def readText( cs: Charset ):String = {
			var res:String = null;
			using( new FileInputStream(file) ){ fin =>
				val inReader = new InputStreamReader(fin,cs);
				val bReader = new BufferedReader(inReader);
				var stop = false;
				val strBuilder = new java.lang.StringBuilder();
				val buff = new Array[Char](1024);
				while(!stop){
					var readed = bReader.read(buff,0,buff.length);
					if( readed>=0 ){
						if( readed>0 ){
							strBuilder.append(buff,0,readed);
						}
					}else{
						stop = true;
					}
				}
				res = strBuilder.toString;
			}
			res
		}

		/**
		 * Читает <i>count</i> байтов от начала файла
		 */
		def readBytes( count:Int ):Array[Byte] = {
			var buff = new Array[Byte](count);
			using( new FileInputStream(file) ){ fin =>
				var stop = false;
				var co : Int = 0;
				while( !stop ){
					var readed = fin.read(buff,co,count-co);
					if( readed<0 )stop = true; else {
						if( readed>0 ){
							co += readed;
							if( co >= count ){
								stop = true;
							}
						}
					}
				}
				if( co < count ){
					buff = Arrays.copyOf(buff,co);
				}
			}
			buff
		}

		/**
		 * Читает все содержимое файла и возвращает в виде байтов
		 */
		def readBytes():Array[Byte] = readBytes( file.length().asInstanceOf[Int] );

		/**
		 * Записывает входной поток байтов в файл
		 * @param input Входной поток байтов
		 * @param append Дописывать в конец
		 * @param count Кол-во байт необходимое записать (-1 - записать все что есть; по умолчанию)
		 * @param bufferSize Размер буфера (1024 по умолчанию)
		 */
		def write(input:InputStream, append:Boolean=false, count:Int=(-1), bufferSize:Int=1024 ):Unit = {
			if( input==null )throw new IllegalArgumentException("input==null");
			if( bufferSize<1 )throw new IllegalArgumentException("bufferSize<1");

			using( new FileOutputStream(file,append) ){ fout =>
				if( count!=0 )
				{
					var stop = false;
					val buff = new Array[Byte](bufferSize);
					var readed = -1;
					var total = 0;
					while( !stop ){
						if( count>0 ){
							if( total >= count ){
								stop = true;
							}else{
								readed = input.read(
									buff,
									0,
									if( (count-total)>buff.length )
										buff.length
									else
										count-total
									);
								if( readed<0 ){
									stop = true;
								}else{
									if( readed>0 ){
										fout.write(buff,0,readed);
									}
									total += readed;
								}
							}
						}else{
							readed = input.read(buff,0,buff.length);
							if( readed<0 ){
								stop = true;
							}else{
								if( readed>0 ){
									fout.write(buff,0,readed);
								}
							}
						}
					}
				}
			}
		}
	}
}