package xyz.cofe.odtexport

import java.io.{
	InputStream => IStream,
	InputStreamReader=>ISReaded,
	BufferedReader =>BReader,
	OutputStream => OStream
};
import java.nio.charset.Charset;

object InputStream {
	class InputStreamExtension (val is:IStream) {
		/**
		 * Читает текст из потока, до конца
		 * @param cs - Кодировка текста
		 * @return Декодированный текст
		 */
		def readText(cs:Charset):String = {
			//val res = new StringBuilder();
//			val isr = new ISReaded( is, cs );
//			val br = new BReader( isr );
			var resbuff : Array[Byte] = Array[Byte](0);
			val buff = new Array[Byte](1024*4);
			var stop = false;
			while( !stop ){
				val readed = is.read(buff);
				if( readed<0 ){
					stop = true;
				}else{
					if( readed>0 ){
						resbuff = java.util.Arrays.copyOf(resbuff, resbuff.length + readed);
						System.arraycopy(buff, 0, resbuff, resbuff.length - readed, readed);
					}
				}
			}
			new java.lang.String(resbuff,cs)
		}

		/**
		 * Читает текст из потока, до конца
		 * @param cs - Кодировка текста
		 * @return Декодированный текст
		 */
		def readText( cs:String ):String = readText( Charset.forName(cs) );

		/**
		 * Читает текст из потока, до конца. Исползуется кодировка по умолчанию.
		 * @return Декодированный текст
		 */
		def readText():String = readText( Charset.defaultCharset );
	}

	implicit def extendInputStream( is:IStream ):InputStreamExtension = new InputStreamExtension( is );
}